# 개발 체크리스트 (Checklist)

- [x] **Phase 1: 기획 및 UI 설계**
  - [x] 화면 기획 (Profile, Diary, Food List, Food Edit)
  - [x] Jetpack Compose 기반 Navigation 세팅
  - [x] Material 3 테마 및 컬러 팔레트 적용

- [x] **Phase 2: 로컬 데이터베이스 (Room) 구축**
  - [x] Entity 정의 (Food, MealRecord, Profile)
  - [x] DAO 및 Repository 구현
  - [x] MVVM 아키텍처 연동 및 StateFlow 상태 관리

- [x] **Phase 3: 공공데이터 API 연동**
  - [x] 식약처 (data.go.kr) 영양성분 API 연동
  - [x] Retrofit/HttpURLConnection 기반 Network 통신
  - [x] API 응답 지능형 필터링 로직 (최적의 검색어 매칭)

- [x] **Phase 4: 고급 기능 구현**
  - [x] 카메라/갤러리 사진 첨부 기능 (Photo Picker)
  - [x] 직접 만든 음식 (재료별 칼로리 합산) 로직
  - [x] Double 기반 소수점 정밀 계산 (0.5g 입력 등)
  - [x] 글로벌 키보드 숨김(Tap 제스처) UX 개선

- [x] **Phase 5: 테스트 및 산출물 작성**
  - [x] 단위 테스트 및 에지 케이스 점검
  - [x] `docs/` 내 필수 요구사항 마크다운 문서 작성
