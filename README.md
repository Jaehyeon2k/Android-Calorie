# Calorie

음식 사진, 음식 사전, 외부 레시피 백과를 기반으로 칼로리를 기록하는 Android Native 앱입니다. 핵심 데이터는 Room 로컬 DB에 저장하므로 오프라인에서도 음식 CRUD 및 식사 기록, BMR/TDEE 프로필 조회가 완벽하게 동작하며, 네트워크가 연결된 상태에서는 식약처 공공데이터(data.go.kr) API를 활용해 재료 칼로리를 정밀하게 검색하거나 '요리백과'에서 공식 레시피 데이터를 가져와 내 음식 사전에 캐싱할 수 있습니다.

## 주요 기능

- 오늘의 다이어리: 날짜별, 식사 시간대별 섭취 칼로리를 기록하고 일일 목표(TDEE) 대비 진행률 확인
- 내 정보 (프로필): 사용자 신체 정보를 기반으로 오프라인에서도 BMR/TDEE 자동 계산
- 음식 사전 CRUD: 음식 생성, 목록 조회, 상세 조회, 수정, 삭제
- 직접 만든 음식 지원: 식약처 공공데이터 API로 재료를 검색해 추가하고, 전체 칼로리를 자동 합산
- 요리백과 API (COOKRCP01): 1,000개 이상의 공식 한국 음식 레시피를 검색하고 로컬 DB로 Import
- 카메라 연동: 갤러리/카메라 사진을 가져와 앱 내부 저장소에 저장하고 썸네일 표시
- 지능형 필터: 검색 시 '가공', '튀김' 등 2차 가공식품을 필터링해 가장 순수한 원물 식재료 반환

## 기술 스택

- Kotlin
- Jetpack Compose
- Material 3
- Navigation Compose
- MVVM
- Room (Offline First)
- Coil Compose
- 공공데이터포털(data.go.kr) OpenAPI

## 실행 방법

```bash
./gradlew installDebug
```

Android Studio에서는 프로젝트를 연 뒤 `app` 실행 구성을 선택해 실행합니다.

## 빌드 방법

```bash
./gradlew assembleDebug
```

## 테스트 방법

```bash
./gradlew test
./gradlew connectedAndroidTest
./gradlew lint
```

테스트와 정적 분석 실행 결과는 `docs/test_result.md`에 기록합니다.

## 문서

과제 제출용 문서는 `docs/` 폴더에 정리했습니다.
