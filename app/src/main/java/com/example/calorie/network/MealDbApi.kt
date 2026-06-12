package com.example.calorie.network

import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

class MealDbApi {
    // 사용자님이 제공해주신 식품안전나라 API 키를 적용합니다.
    private val apiKey = "8a8beaffd5f4404e83ed"

    suspend fun searchRecipes(query: String): List<MealDbRecipe> {
        val originalQuery = query.trim()
        if (originalQuery.isBlank()) return emptyList()

        val encoded = URLEncoder.encode(originalQuery, "UTF-8")
        // 식약처 레시피 OpenAPI 엔드포인트
        val url = URL("http://openapi.foodsafetykorea.go.kr/api/$apiKey/COOKRCP01/json/1/15/RCP_NM=$encoded")
        
        return runCatching {
            val connection = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = 8_000
                readTimeout = 8_000
            }
            connection.inputStream.bufferedReader().use { reader ->
                val responseText = reader.readText()
                parseMeals(JSONObject(responseText))
            }.also {
                connection.disconnect()
            }
        }.getOrDefault(emptyList())
    }

    private fun parseMeals(json: JSONObject): List<MealDbRecipe> {
        val root = json.optJSONObject("COOKRCP01") ?: return emptyList()
        val rows = root.optJSONArray("row") ?: return emptyList()
        
        return List(rows.length()) { index ->
            val item = rows.getJSONObject(index)
            
            // 재료 파싱 (RCP_PARTS_DTLS)
            val partsStr = item.optString("RCP_PARTS_DTLS", "")
            val ingredients = partsStr.split(",", "\n").map { it.trim() }.filter { it.isNotBlank() }.take(15)
            
            // 조리법 파싱 (MANUAL01 ~ MANUAL20)
            val instructions = buildString {
                for (i in 1..20) {
                    val key = "MANUAL${i.toString().padStart(2, '0')}"
                    val manual = item.optString(key, "")
                    if (manual.isNotBlank()) {
                        appendLine(manual)
                    }
                }
            }.trim()

            MealDbRecipe(
                externalId = "kr_recipe_${item.optString("RCP_SEQ", System.currentTimeMillis().toString())}",
                name = item.optString("RCP_NM", "이름 없음"),
                category = item.optString("RCP_PAT2", "한식"),
                area = "Korean",
                instructions = instructions.ifBlank { "조리법 정보가 없습니다." },
                imageUrl = item.optString("ATT_FILE_NO_MAIN").takeIf { it.isNotBlank() },
                sourceUrl = null,
                ingredients = ingredients,
                kcal = item.optString("INFO_ENG", "").replace(Regex("[^0-9.]"), "").ifBlank { "100" },
                servingSize = item.optString("INFO_WGT", "").replace(Regex("[^0-9.]"), "").ifBlank { "100" }
            )
        }
    }

    suspend fun searchIngredientInfo(name: String): Pair<Int, Int>? {
        val originalQuery = name.trim()
        if (originalQuery.isBlank()) return null
        val encoded = URLEncoder.encode(originalQuery, "UTF-8")
        
        // 사용자가 제공한 공공데이터포털(data.go.kr) API 키
        val serviceKey = "7b9fc8705309ce44de2a3300de62d15ec2b4fa5a48c50b77ba6172d279cc206b"
        val url = URL("https://apis.data.go.kr/1471000/FoodNtrCpntDbInfo02/getFoodNtrCpntDbInq02?serviceKey=$serviceKey&pageNo=1&numOfRows=20&type=json&FOOD_NM_KR=$encoded")
        
        return runCatching {
            val connection = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = 3_000
                readTimeout = 3_000
                setRequestProperty("User-Agent", "Mozilla/5.0")
            }
            connection.inputStream.bufferedReader().use { reader ->
                val responseText = reader.readText()
                val json = JSONObject(responseText)
                
                // 공공데이터포털 JSON 구조: 보통 root 바로 아래에 body가 있거나 response 안에 body가 있음
                val bodyObj = json.optJSONObject("body") ?: json.optJSONObject("response")?.optJSONObject("body")
                
                // items가 바로 배열일 수도 있고, items 안에 item 배열이 있을 수도 있음
                var itemArray = bodyObj?.optJSONArray("items")
                if (itemArray == null) {
                    itemArray = bodyObj?.optJSONObject("items")?.optJSONArray("item")
                }
                
                if (itemArray != null && itemArray.length() > 0) {
                    var bestItem: JSONObject? = null
                    var minLength = Int.MAX_VALUE
                    
                    for (i in 0 until itemArray.length()) {
                        val item = itemArray.getJSONObject(i)
                        val name = item.optString("FOOD_NM_KR", "")
                        
                        if (name == originalQuery) {
                            bestItem = item
                            break
                        }
                        
                        // "튀김", "가공", "샌드위치" 등 원치 않는 키워드가 들어간 것은 우선순위 낮춤
                        val penalty = if (name.contains("튀김") || name.contains("샌드위치") || name.contains("양념") || name.contains("가공")) 100 else 0
                        val score = name.length + penalty
                        
                        if (score < minLength) {
                            minLength = score
                            bestItem = item
                        }
                    }
                    
                    if (bestItem != null) {
                        // NUTR_CONT1: 열량(kcal), AMT_NUM1: 열량 대체 필드
                        val kcalStr = bestItem.optString("NUTR_CONT1", "").ifEmpty { bestItem.optString("AMT_NUM1", "0") }
                        val kcal = kcalStr.replace(Regex("[^0-9.]"), "").toDoubleOrNull()?.toInt() ?: 0
                        
                        // SERVING_SIZE: 1회제공량
                        val servingStr = bestItem.optString("SERVING_SIZE", "").ifEmpty { bestItem.optString("SERVING_WT", "100") }
                        val servingSize = servingStr.replace(Regex("[^0-9.]"), "").toDoubleOrNull()?.toInt() ?: 100
                        
                        Pair(kcal, servingSize)
                    } else null
                } else null
            }.also {
                connection.disconnect()
            }
        }.getOrNull()
    }
}
