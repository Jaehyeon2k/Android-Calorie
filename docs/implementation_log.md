# 구현 이력 (Implementation Log)

## 1. 주요 기술 스택
* Language: Kotlin
* UI Toolkit: Jetpack Compose (Material 3)
* Local DB: Room Database
* Asynchronous: Kotlin Coroutines & Flow
* Network: HttpURLConnection / JSONObject (외부 라이브러리 최소화)

## 2. 주요 구현 내역 및 트레이드오프

### [Phase 1] 아키텍처 및 뼈대 구축
* **결정:** MVVM 패턴 도입. ViewModel이 Room DAO와 통신하고, UI는 `StateFlow`를 observe하도록 설계.
* **트레이드오프:** Repository 레이어를 별도로 두지 않고 ViewModel에서 DAO를 직접 호출. 중소형 앱 규모를 고려하여 보일러플레이트 코드를 줄이고 개발 속도를 높임.

### [Phase 2] 오프라인 퍼스트 DB 설계
* **결정:** `FoodEntity`와 `MealRecordEntity` 분리. 직접 만든 음식의 경우 재료를 별도 테이블로 빼지 않고 `ingredientsJson` 컬럼에 문자열로 직렬화하여 저장.
* **이유:** 재료 조회 시 매번 JOIN을 수행하는 비용을 줄이고, NoSQL처럼 유연하게 재료를 편집하기 위함.

### [Phase 3] API 연동 및 고도화
* **문제:** 공공데이터포털 검색 시 "닭가슴살"의 첫 결과가 "샌드위치_닭가슴살(240kcal)"로 나옴.
* **해결:** 검색 결과를 20개 가져와서 문자열 길이를 측정하고, "튀김, 가공, 샌드위치" 키워드에 페널티(100점)를 부여하여 가장 점수가 낮고 순수한 원물 식재료를 매칭하는 지능형 스코어링 로직 도입.
* **추가:** 식약처 '조리식품 레시피 API(COOKRCP01)'를 추가 연동하여 사용자가 1,000개 이상의 공식 레시피를 검색하고 자신의 오프라인 DB로 복사(Import)할 수 있는 '요리백과' 기능 구현.

### [Phase 4] UX/기능 폴리싱
* **사진 첨부:** `ActivityResultContracts.PickVisualMedia`를 사용하여 권한 문제 없이 갤러리 접근 및 로컬 스토리지에 사진 복사 보관.
* **소수점 계산:** 무게 입력 시 `toDoubleOrNull()`을 사용하여 0.5g 입력 시에도 비율에 맞게 정확한 소수점 칼로리가 도출되도록 수식 개선.
