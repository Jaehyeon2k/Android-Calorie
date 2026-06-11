package com.example.calorie.network

import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

class MealDbApi {
    // 국내 공공데이터 (식품안전나라 조리식품 레시피 API) 키
    // 현재는 샘플키("sample")를 임시로 사용합니다. 호출 횟수 초과나 빈 결과가 나올 수 있으므로,
    // 실제로는 https://www.foodsafetykorea.go.kr/api/ 에서 발급받은 키로 교체해야 완벽합니다.
    private val apiKey = "sample"

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
                ingredients = ingredients
            )
        }
    }

    suspend fun searchIngredientKcal(name: String): Int? {
        val originalQuery = name.trim()
        if (originalQuery.isBlank()) return null
        val encoded = URLEncoder.encode(originalQuery, "UTF-8")
        
        // 식품안전나라 식품영양성분 DB (I2790) - 샘플키 사용 (실제 서비스시 키 발급 필요)
        val url = URL("http://openapi.foodsafetykorea.go.kr/api/$apiKey/I2790/json/1/1/DESC_KOR=$encoded")
        
        return runCatching {
            val connection = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = 3_000
                readTimeout = 3_000
            }
            connection.inputStream.bufferedReader().use { reader ->
                val responseText = reader.readText()
                val json = JSONObject(responseText)
                val root = json.optJSONObject("I2790")
                val rows = root?.optJSONArray("row")
                if (rows != null && rows.length() > 0) {
                    val firstItem = rows.getJSONObject(0)
                    // NUTR_CONT1: 열량(kcal)
                    firstItem.optString("NUTR_CONT1").toDoubleOrNull()?.toInt()
                } else null
            }.also {
                connection.disconnect()
            }
        }.getOrNull()
    }
}
