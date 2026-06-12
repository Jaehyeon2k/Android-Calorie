# 테스트 결과 (Test Results)

## 1. 테스트 환경
* **Device:** Android Emulator (API 34), Samsung Galaxy S23
* **OS:** Android 14
* **Date:** 2026-06-12

## 2. 테스트 수행 결과

| TC-ID | 기능 | 실제 결과 | 상태 | 비고 |
|-------|------|-----------|------|------|
| TC-01 | 소수점 정밀 칼로리 계산 | 50.5g 입력 시 정상적으로 소수점 비례 배분되어 소수점 버림 처리된 Int 결과 도출 확인 | **PASS** | Double 캐스팅 로직 완벽 작동 |
| TC-02 | 직접 만든 음식 합산 | 재료 3개 입력 후 저장 시 총합 칼로리가 실시간 반영되고 DB에 JSON 직렬화 저장 완료 | **PASS** | |
| TC-03 | API 검색 지능형 필터 | `FOOD_NM_KR` 길이를 비교하고 튀김/가공/양념 페널티를 부여하여 최적의 생식품 반환 | **PASS** | 기존 240kcal 버그 해결 |
| TC-04 | API 없는 음식 0 리셋 | 매칭 실패 시 `manualCalories = "0"` 처리 확인 | **PASS** | |
| TC-05 | UX 키보드 숨김 | `detectTapGestures` 및 `focusManager.clearFocus()` 호출 정상 작동 | **PASS** | 스크롤 컨테이너에도 적용 완료 |
| TC-06 | UI(Compose) 화면 렌더링 테스트 | Compose Test Rule 기반으로 주요 화면 3개(다이어리/음식사전/프로필) 렌더링 검증 완료 | **PASS** | `CalorieAppTest.kt` 실행 완료 |
| TC-07 | 정적 분석 (Android Lint) | `./gradlew lint` 실행 결과, 치명적 에러 없음 및 사용하지 않는 리소스/코드 경고(Warning) 제거 완료 | **PASS** | |

## 3. 종합 의견
모든 핵심 CRUD, API 연동, 비즈니스 로직(칼로리 계산)이 엣지 케이스(소수점, 통신 실패) 환경에서도 크래시 없이 견고하게 동작함을 확인하였다.
