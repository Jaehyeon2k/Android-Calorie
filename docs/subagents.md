# Agent 역할 분담 및 협업 (Subagents & AI Collaboration)

본 과제는 개발자인 인간(USER)과 AI 코딩 어시스턴트(Antigravity Agent)가 협업하여 기획부터 구현, 버그 수정까지 전 과정을 진행하였다.

## 1. 역할 분담
* **USER (인간 개발자):**
  * 도메인 지식 제공 (칼로리 트래킹 개념, 사용자 니즈 식별)
  * 기획 방향 설정 및 요구사항 도출
  * API 키(식약처, FatSecret 등) 발급 및 제공
  * 실제 에뮬레이터 테스트 및 사용성 피드백
* **AI Agent (Antigravity):**
  * Android / Jetpack Compose 코드 아키텍처 설계
  * Room DB Entity 및 DAO 구현
  * 네트워크 통신(HttpURLConnection) 로직 및 JSON 파싱 로직 구현
  * 런타임 버그 원인 분석(로그 분석) 및 즉각적인 트러블슈팅(코드 패치)
  * 과제 산출물(Markdown) 자동화 작성

## 2. 주요 협업 성과
1. **API 교체 및 개선 결단:** FatSecret API 도입을 시도했으나 무료 버전의 한계(한국어 미지원)를 확인한 후, 즉시 기존 식약처 API의 검색 알고리즘을 고도화(패널티 스코어링 도입)하는 방향으로 피봇(Pivot)하여 문제를 해결함.
2. **데이터 타입 마이그레이션:** 칼로리 정밀도 오류(정수 나눗셈에 의한 데이터 유실)를 발견하고, Agent가 전체 DB 및 도메인 로직을 `Int`에서 `Double` 기반으로 일괄 리팩토링함.
