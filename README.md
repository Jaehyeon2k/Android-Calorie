# Calorie

음식 사진, 음식 사전, 외부 레시피 백과를 기반으로 칼로리를 기록하는 Android Native 앱입니다. 핵심 데이터는 Room 로컬 DB에 저장하므로 오프라인에서도 음식 CRUD가 동작하고, 네트워크가 가능할 때는 TheMealDB API에서 레시피와 이미지를 가져와 음식 사전에 저장할 수 있습니다.

## 주요 기능

- 음식 사전 CRUD: 음식 생성, 목록 조회, 상세 조회, 수정, 삭제
- 직접 만든 음식 지원: 재료별 중량과 100g당 kcal 입력 후 합산
- 최종 kcal 보정: 재료 합산값을 기본으로 사용하되 직접 수정 가능
- 카메라 연동: 촬영 이미지를 앱 내부 저장소에 저장하고 URI를 Room에 저장
- 요리백과 API: TheMealDB에서 레시피, 재료, 이미지 URL을 검색하고 로컬 음식 사전에 저장
- 이미지 표시: 카메라 사진 URI와 외부 레시피 이미지 URL을 카드 UI에 표시
- 입력 검증: 음식명, 제공량, kcal, 재료 입력 오류를 Snackbar로 안내

## 기술 스택

- Kotlin
- Jetpack Compose
- Material 3
- Navigation Compose
- MVVM
- Room
- Coil Compose
- TheMealDB API

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
