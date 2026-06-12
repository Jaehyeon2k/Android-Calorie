# 초기 데이터 (Seed Data)

본 앱은 최초 실행 시 사용자의 편의를 위해 기본적인 음식 데이터를 Room DB에 Seed 데이터로 주입할 수 있도록 설계되었습니다. (현재는 빈 상태에서 시작하며, 사용자가 API를 통해 동적으로 구축하도록 유도)

## 테스트용 음식 데이터 예시
* **일반 음식 1:**
  * name: "계란프라이"
  * category: "단백질"
  * servingSizeGram: 50.0
  * calories: 89
* **일반 음식 2:**
  * name: "햇반"
  * category: "탄수화물"
  * servingSizeGram: 210.0
  * calories: 315
* **직접 만든 음식 (레시피) 1:**
  * name: "닭가슴살 샐러드"
  * isCustom: true
  * ingredients: [{"name": "닭가슴살", "weightGram": 100, "kcalPer100Gram": 120}, {"name": "양상추", "weightGram": 50, "kcalPer100Gram": 11}]
  * calories: 125 (자동 계산됨)
