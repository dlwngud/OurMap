# OurMap

소중한 사람과 장소와 순간을 함께 기록하는 Android 앱.

## 현재: 2주차 UI 프로토타입

- Kotlin / Compose / Material 3, 시스템 Light·Dark 테마
- Hilt Application → Activity → ViewModel → DemoContent 의존성 주입
- Navigation 3의 저장 가능한 화면 경로: 온보딩, 메인 탭, 작성, 상세
- 지도 / 기록 / 우리 / 마이 탭과 중앙 기록 작성 버튼
- Button / Card / Chip / BottomSheet 공통 컴포넌트와 Preview
- 프로필 설정 → Space 생성 또는 초대 코드 참여 → 첫 기록 작성
- 장소 선택, 날짜·기분·메모 입력, 샘플 사진 선택, 작성 취소 확인
- 기록 검색·찜 필터, 전체/사진/장소 보기, 장소·기록 상세, 사진 갤러리
- Space 통계·초대 대기, 프로필 편집, 가보고 싶은 곳 추가·해제
- 입력 검증, 빈 화면, 저장 가능한 UI 상태와 복원 테스트
- PR 및 main push 시 빌드·단위 테스트·Lint CI 정의

현재는 사용자 흐름을 검증하는 데모다. 로그인 버튼은 외부 인증 없이 프로필 설정으로 연결되며,
초대 코드 `8A2F-91B`도 샘플 참여용이다. 실제 초대 전송이나 다른 기기와의 공유는 하지 않는다.
지도는 지리적으로 정확하지 않은 샘플 화면이고 장소 3곳과 로컬 일러스트를 사용한다.
알림·기념일·앱 설정은 후속 구현을 설명하는 안내 화면이다.

작성 결과는 현재 데모 세션에 반영된다. 화면 재생성 시 저장 상태로 복원하지만 DB 저장은 아니므로
새로 앱을 시작하거나 체험을 초기화하면 유실될 수 있다. 실제 로그인, 사진 접근, Room 영속 저장,
지도 SDK, 서버와의 동기화는 이후 단계에서 구현한다. 2주차에는 의존성을 추가하지 않았다.

## 직접 확인할 흐름

1. **새 Space**: 시작하기 → 프로필 → 새 Space 만들기 → 이름·시작일·스타일 → 초대 또는 나중에 시작.
2. **초대 참여**: 초대 코드로 참여 → 이름·체험 코드 입력 → 우리 화면. 잘못된 코드는 오류를 표시한다.
3. **첫 기록**: 중앙 + 또는 첫 장소 기록하기 → 장소·날짜·메모 → 미리보기 저장 → 기록 상세.
4. **다시 보기**: 지도 마커 → 장소 상세 → 기록 상세 → 사진 갤러리. 기록 탭에서도 같은 기록을 조회한다.
5. **다음 장소**: 우리 → 가보고 싶은 곳 → 샘플 장소 추가 또는 해제.
6. **빠른 체험**: 첫 화면의 샘플 Space 둘러보기. 마이 → 처음부터 다시 체험으로 초기화한다.

## 코드 구조

현재는 app 단일 Gradle 모듈 안에서 책임별 패키지를 구분한다.

| 경로 (com.wngud.ourmap 아래) | 책임 |
| --- | --- |
| navigation | 앱 Scaffold, AppRoute, 저장 가능한 데모 세션·탭 상태, MainViewModel |
| feature/onboarding | 로그인 진입, 프로필, Space 생성·참여, 초대 대기 |
| feature/map, records, us, my | 메인 화면과 가보고 싶은 곳 |
| feature/memory | 기록 작성, 장소 선택, 장소·기록 상세, 사진 갤러리 |
| ui/theme | 브랜드 색상·타이포그래피·모서리 |
| ui/components | 공통 UI 구성요소 |
| core/model | SDK에 의존하지 않는 샘플 도메인 모델 |
| data/demo | Hilt로 제공하는 샘플과 불변 DemoSession, 날짜·초대 검증 |

최초 실행은 로그인 진입 화면에서 시작하며 생성·참여 완료 후에는 우리 탭으로 이동한다.
탭 루트에서만 하단 내비게이션을 표시한다. 상세 화면은 이전 화면으로, 다른 탭 루트에서는 지도로,
지도 루트에서는 앱 밖으로 돌아간다. 작성 중 변경사항이 있으면 종료를 확인한다.
탭 전환 시 검색어와 필터를 보존한다. 기록·찜·Space 정보는 하나의 DemoSession에서 파생해
여러 화면에서 일관되게 보인다. 프로토타입의 상태 소유자는 OurMapAppContent이며,
영속 저장 도입 시 Repository/ViewModel로 이전한다.

## 빌드와 검증

JDK 17 이상, Android SDK 36이 필요하다. Android Studio 내장 JDK도 사용할 수 있다.

```sh
./gradlew :app:assembleDebug :app:testDebugUnitTest :app:lintDebug
./gradlew :app:assembleDebugAndroidTest
# adb devices로 확인한 테스트 기기 한 대만 지정
ANDROID_SERIAL=emulator-5554 ./gradlew :app:connectedDebugAndroidTest
```

단위 테스트는 초대 코드, 날짜, 데모 세션 직렬화, 찜 토글을 검증한다.
UI 테스트는 탭 이동·작성 취소, 검색 상태 복원, Space 생성과 첫 기록 저장,
초대 코드 오류·참여, 장소 → 기록 → 갤러리 이동을 검증한다.
Android Studio에서 MainActivity를 실행해 실제 Hilt 진입, 다크 모드, 큰 글씨도 확인한다.

2026-09-07 로컬 검증: Debug 빌드·단위 테스트 5개 통과, Android 16 임시 에뮬레이터에서
계측 테스트 6개 통과(위 사용자 흐름 5개와 기존 앱 컨텍스트 테스트).
같은 테스트 6개를 큰 글씨(1.5배)·다크 테마에서도 재실행해 모두 통과했다.
실제 MainActivity의 Hilt 진입, 로그인·지도·우리 화면과 큰 글씨(1.5배)·다크 모드의
우리·기록 작성 화면을 확인했다. 화면 전환 테스트는 대상 노드가 나타날 때까지 기다린다.
Lint는 오류 0개, 경고 23개이며 라이브러리 업데이트 권고와 템플릿 미사용 리소스 등이 남아 있다.
연결된 실물 기기에 함께 실행된 첫 테스트에서는 Compose 화면 탐색에 실패했으므로
실물 기기 검증 완료로 간주하지 않는다. 이후 검증은 ANDROID_SERIAL로 임시 에뮬레이터만 지정했다.
CI는 워크플로 파일만 추가한 상태이며 원격 실행은 하지 않았다.

## 의존성 선택

버전은 gradle/libs.versions.toml에서 관리한다. AGP 9의 내장 Kotlin에 맞춰 Compose와
Serialization 플러그인을 2.2.10으로 정렬했다. KSP 2.3.4와 Hilt 2.59.2를 사용한다.

- [AGP 9 내장 Kotlin](https://developer.android.com/build/releases/agp-9-0-0-release-notes)
- [Hilt 설정](https://dagger.dev/hilt/gradle-setup.html)
- [Navigation 3 릴리스](https://developer.android.com/jetpack/androidx/releases/navigation3)
- [KSP 릴리스](https://github.com/google/ksp/releases)

기존 init 커밋을 출발점으로 사용한다. 원격 push는 별도 수행한다.
