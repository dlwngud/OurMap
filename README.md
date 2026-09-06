# OurMap

소중한 사람과 장소와 순간을 함께 기록하는 Android 앱.

## 현재: 1주차 기반 구축

- Kotlin / Compose / Material 3, 시스템 Light·Dark 테마
- Hilt Application → Activity → ViewModel → DemoContent 의존성 주입
- Navigation 3의 저장 가능한 탭 경로: 지도 / 기록 / 우리 / 마이
- 중앙 작성 버튼 → 공통 BottomSheet. 닫으면 원래 탭 유지
- Button / Card / Chip / BottomSheet 공통 컴포넌트와 Preview
- 고정 샘플 데이터, 기록 태그 필터, UI 복원 테스트
- PR 및 main push 시 빌드·단위 테스트·Lint CI 정의

지도 영역과 기록 작성은 현재 안내 화면이다. 샘플 데이터는 계정이나 실제 저장 데이터가 아니다.
로그인·온보딩 전체 UI는 2주차, 영속 저장·지도 SDK·서버 연동은 이후 일정에서 구현한다.

## 코드 구조

현재는 app 단일 Gradle 모듈 안에서 책임별 패키지를 구분한다.

| 경로 (com.wngud.ourmap 아래) | 책임 |
| --- | --- |
| navigation | 앱 Scaffold, 저장 가능한 탭 경로, MainViewModel |
| feature/map, records, us, my | 상태와 콜백을 받는 화면 UI |
| ui/theme | 브랜드 색상·타이포그래피·모서리 |
| ui/components | 공통 UI 구성요소 |
| core/model | SDK에 의존하지 않는 샘플 도메인 모델 |
| data/demo | Hilt로 제공하는 고정 샘플 데이터 |

탭은 동등한 목적지로 취급하고 지도에서 시작한다. 다른 탭에서 시스템 뒤로 가기를 하면 지도로
돌아가고 지도에서는 앱을 나간다. 작성 Sheet를 먼저 닫는다. 선택한 탭과 화면 내부 상태는
저장 가능한 상태로 구성한다. 각 탭의 상세 화면과 다중 back stack은 해당 기능 도입 시 확장한다.

## 빌드와 검증

JDK 17 이상, Android SDK 36이 필요하다. Android Studio 내장 JDK도 사용할 수 있다.

```sh
./gradlew :app:assembleDebug :app:testDebugUnitTest :app:lintDebug
./gradlew :app:assembleDebugAndroidTest
# 에뮬레이터 또는 실제 기기 연결 후
./gradlew :app:connectedDebugAndroidTest
```

UI 테스트는 네 탭 이동, 작성 Sheet 닫기, 선택 탭과 필터의 상태 복원을 검증한다.
Android Studio에서 MainActivity를 실행해 다크 모드, 시스템 뒤로 가기, 큰 글씨도 확인한다.

2026-09-06 로컬 검증: Debug 빌드·기본 단위 테스트 통과, Android 16 임시 에뮬레이터에서
계측 테스트 3개 통과(탭/Sheet 동작, 상태 복원, 기존 앱 컨텍스트 테스트).
실제 MainActivity의 Hilt 진입과 Light/Dark, 글씨 크기 1.5배 화면도 확인했다.
Lint 오류는 없으며 라이브러리 업데이트 권고와 기본 템플릿 미사용 리소스 등의 경고가 남아 있다.
CI는 워크플로 파일만 추가한 상태이며 원격 실행은 하지 않았다.

## 의존성 선택

버전은 gradle/libs.versions.toml에서 관리한다. AGP 9의 내장 Kotlin에 맞춰 Compose와
Serialization 플러그인을 2.2.10으로 정렬했다. KSP 2.3.4와 Hilt 2.59.2를 사용한다.

- [AGP 9 내장 Kotlin](https://developer.android.com/build/releases/agp-9-0-0-release-notes)
- [Hilt 설정](https://dagger.dev/hilt/gradle-setup.html)
- [Navigation 3 릴리스](https://developer.android.com/jetpack/androidx/releases/navigation3)
- [KSP 릴리스](https://github.com/google/ksp/releases)

기존 init 커밋을 출발점으로 사용한다. 원격 push는 별도 수행한다.
