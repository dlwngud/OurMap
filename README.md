# OurMap

소중한 사람과 장소와 순간을 함께 기록하는 Android 앱.

## 현재: 4주차 기록 작성·사진 선택

- 시스템 Photo Picker로 이미지 최대 8장 선택, 중복 제거·삭제·대표 사진 지정
- 앨범 사진을 기록 카드·상세·갤러리·사진 탭·Space 사진 통계에 반영
- 작성 입력 상태 복원, 저장 처리 중 상호작용 차단, 사진 접근 실패 안내·입력 유지·재시도
- Coil 3.4.0으로 화면 크기에 맞춰 사진 로딩, 접근 불가 사진에 오류 표시
- 메모 최대 200자, 장소·날짜 검증, 기분 5종, 사진 없이 기록 작성

사진은 URI만 현재 세션에서 참조한다. 원본 복사·업로드·영구 보관은 하지 않으며
사진 접근 권한이 해제되거나 원본이 삭제되면 재선택해야 한다. 시스템 선택창 취소 시 기존 선택은 유지된다.
앨범과 샘플 사진은 혼합하지 않는다. 샘플 사진을 모두 제거하면 앨범을 사용할 수 있다.
최대 장수는 시스템 선택창의 제한과 별개로 앱에서도 검사한다.

### 유지되는 3주차 로컬 온보딩·Space

- 로컬 체험 시작 → 프로필 저장 → Space 생성·샘플 참여 → 우리 화면
- DataStore에 사용자·프로필·Space·멤버·초대 코드 저장, 앱 재실행 시 복원
- 프로필 편집, 로컬 로그아웃·이어하기, 확인 후 로컬 데이터 초기화
- 무작위 초대 코드 생성, 7일 만료, 재발급, 상대 참여 시뮬레이션
- 저장 완료 후 화면 전환, 중복 요청 방지, 로딩·저장 실패·읽기 실패 처리

**실제 인증은 아직 없다.** 로컬 로그아웃은 접근 보안이나 본인 인증 기능이 아니며,
같은 기기를 사용하는 사람이 저장된 프로필로 다시 계속할 수 있다. 비밀번호·토큰은 저장하지 않는다.
생성된 초대는 이 기기의 시뮬레이션에서만 유효하다. 다른 기기와의 공유는 서버 연결 후 구현한다.
샘플 참여 코드는 `8A2F-91B`이며 입력 후 빈 샘플 Space와 멤버가 로컬에 저장된다.

프로필·Space는 앱을 종료해도 유지되지만 **추억·사진·장소 찜은 여전히 미리보기 상태**다.
기록 영속 저장은 Room 단계에서 진행한다. 기기 로컬 식별자가 다른 기기로 복원되지 않도록
온보딩 파일은 자동 백업·기기 이전 대상에서 제외한다. 앱 삭제·데이터 초기화 시 로컬 정보가 사라진다.

## 유지되는 1~2주차 기능

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

지도는 지리적으로 정확하지 않은 샘플 화면이고 장소 3곳과 로컬 일러스트를 사용한다.
알림·기념일·앱 설정은 후속 구현을 설명하는 안내 화면이다.

2주차의 전체 Fake 화면은 Preview와 기존 UI 회귀 테스트에서 유지한다.
실행 앱의 시작점은 로컬 온보딩이며, 기존 Fake 로그인 버튼은 실제 인증으로 사용하지 않는다.

## 직접 확인할 흐름

1. **새 Space**: 로컬 체험 시작하기 → 프로필 → 새 Space 만들기 → 이름·시작일·스타일 → 우리 공간으로 이동.
2. **초대 참여**: 프로필 저장 → 초대 코드 입력 → 이름·샘플 코드 입력 → 우리 화면. 잘못된 코드는 오류를 표시한다.
3. **첫 기록**: 중앙 + 또는 첫 장소 기록하기 → 장소·날짜·메모 → 앨범에서 사진 선택(선택 사항) → 미리보기 저장 → 기록 상세.
4. **다시 보기**: 지도 마커 → 장소 상세 → 기록 상세 → 사진 갤러리. 기록 탭에서도 같은 기록을 조회한다.
5. **다음 장소**: 우리 → 가보고 싶은 곳 → 샘플 장소 추가 또는 해제.
6. **복원**: 앱을 종료한 뒤 재실행하면 프로필·Space가 남고 우리 화면으로 이동한다.
7. **계정 관리**: 마이 → 로컬 계정 관리 → 로컬 로그아웃 또는 확인 후 로컬 데이터 초기화.

## 코드 구조

현재는 app 단일 Gradle 모듈 안에서 책임별 패키지를 구분한다.

| 경로 (com.wngud.ourmap 아래) | 책임 |
| --- | --- |
| navigation | 앱 Scaffold, AppRoute, 저장 가능한 데모 세션·탭 상태, MainViewModel |
| feature/onboarding | 로그인 진입, 프로필, Space 생성·참여, 초대 대기 |
| domain/onboarding | 순수 Kotlin 모델·상태 전이 규칙·Repository 계약 |
| data/local | DataStore DTO·Serializer·Repository·Hilt 바인딩 |
| feature/map, records, us, my | 메인 화면과 가보고 싶은 곳 |
| feature/memory | 기록 작성, 장소 선택, 장소·기록 상세, 사진 갤러리 |
| ui/theme | 브랜드 색상·타이포그래피·모서리 |
| ui/components | 공통 UI 구성요소 |
| core/model | SDK에 의존하지 않는 샘플 도메인 모델 |
| data/demo | Hilt로 제공하는 샘플과 불변 DemoSession, 날짜·초대 검증 |

최초 실행은 로그인 진입 화면에서 시작하며 생성·참여 완료 후에는 우리 탭으로 이동한다.
탭 루트에서만 하단 내비게이션을 표시한다. 상세 화면은 이전 화면으로, 다른 탭 루트에서는 지도로,
지도 루트에서는 앱 밖으로 돌아간다. 작성 중 변경사항이 있으면 종료를 확인한다.
탭 전환 시 검색어와 필터를 보존한다. 온보딩 상태는 OnboardingViewModel의 StateFlow가 소유하며
LocalOnboardingContent는 상태·콜백만 받는다. 입력값과 NavBackStack은 UI에서 관리한다.
Repository의 DataStore 트랜잭션이 성공해야 완료 상태를 노출한다. 읽기 오류나 손상된 JSON은
자동 초기화하지 않는다. 온보딩 모델을 기존 화면용 DemoSession으로 투영하되,
추억·장소 찜은 아직 OurMapAppContent의 미리보기 상태에 남긴다.

기록 작성은 시스템 선택창을 연결하는 `MemoryEditorScreen`과 입력·콜백으로 테스트 가능한
`MemoryEditorContent`로 구분한다. 사진 접근 검사는 IO 디스패처에서 수행하며 처리 중에는
중복 클릭과 이탈을 차단한다. 취소는 오류로 변환하지 않는다. 화면 재생성 시 입력은 복원하되
중단된 처리 상태는 초기화해 다시 저장할 수 있다. 처리 성공 뒤에만 부모의 세션 반영 콜백을 호출한다.
이 단계의 저장 대상은 여전히 미리보기 세션이며, Room Repository 연결은 5주차 범위다.

## 빌드와 검증

JDK 17 이상, Android SDK 36이 필요하다. Android Studio 내장 JDK도 사용할 수 있다.

```sh
./gradlew :app:assembleDebug :app:testDebugUnitTest :app:lintDebug
./gradlew :app:assembleDebugAndroidTest
# adb devices로 확인한 테스트 기기 한 대만 지정
ANDROID_SERIAL=emulator-5554 ./gradlew :app:connectedDebugAndroidTest
```

단위 테스트는 초대 코드, 날짜, 데모 세션 직렬화, 찜 토글에 더해 저장 파일 재열기,
중복 Space 생성, 초대 만료·재발급, 저장 실패·재시도와 중복 클릭 방지를 검증한다.
UI 테스트는 탭 이동·작성 취소, 검색 상태 복원, Space 생성과 첫 기록 저장,
초대 코드 오류·참여, 장소 → 기록 → 갤러리 이동을 검증한다.
Android Studio에서 MainActivity를 실행해 실제 Hilt 진입, 다크 모드, 큰 글씨도 확인한다.

4주차 검증(2026-09-07): Debug 빌드, 단위 테스트 18개, 계측 테스트 12개 통과.
계측 테스트는 임시 Android 16 에뮬레이터에서 기본 설정과 글씨 2배·다크 모드로 통과했다.
사진 선택 병합·중복·장수 제한·대표 순서·직렬화, 저장 실패 후 입력 유지·동일 ID 재시도,
처리 중 상태와 입력 복원을 검증한다. Lint 오류 0개, 경고 25개(업데이트·템플릿 리소스).
시스템 앨범을 직접 조작하는 사진 선택/취소, 실제 사진 디코딩과 실물 기기 검증은 아직 수행하지 않았다.
이 테스트 통과가 사진 영구 저장이나 서버 업로드를 보장하지는 않는다.

3주차 검증(2026-09-07): Debug 빌드, 단위 테스트 14개, 계측 테스트 9개 통과.
UI 테스트는 임시 Android 16 에뮬레이터에서 큰 글씨(1.5배)·다크 모드와
기본 글씨·밝은 모드로 각각 통과했다. 실제 MainActivity에서 로컬 프로필과 Space를 생성하고
강제 종료·재실행했을 때 사용자 ID·Space·초대 코드가 유지되는 것도 확인했다.
Lint 오류 0개, 경고 24개(업데이트 권고·템플릿 리소스 등). 실물 기기와 원격 CI는 이번 주차에 검증하지 않았다.

2주차 검증 기록: Debug 빌드·단위 테스트 5개 통과, Android 16 임시 에뮬레이터에서
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
3주차에는 작은 로컬 온보딩 데이터용 DataStore 1.2.1과 코루틴 테스트 1.10.2만 추가했다.
Room·인증 SDK·지도 SDK는 해당 주차에 추가한다.
4주차에는 로컬 사진 표시용 Coil Compose 3.4.0만 추가했다. 네트워크 이미지 모듈과
전체 앨범 읽기 권한은 추가하지 않았다.

- [AGP 9 내장 Kotlin](https://developer.android.com/build/releases/agp-9-0-0-release-notes)
- [Hilt 설정](https://dagger.dev/hilt/gradle-setup.html)
- [Navigation 3 릴리스](https://developer.android.com/jetpack/androidx/releases/navigation3)
- [KSP 릴리스](https://github.com/google/ksp/releases)
- [DataStore 공식 가이드](https://developer.android.com/topic/libraries/architecture/datastore)
- [시스템 Photo Picker](https://developer.android.com/training/data-storage/shared/photo-picker)
- [Coil Compose](https://coil-kt.github.io/coil/compose/)

## 이후 일정

| 주차 | 범위 |
| --- | --- |
| 5 | Room·Repository·기록 목록/상세/필터 |
| 6 | 지도 SDK·장소 검색·마커 |
| 7 | Supabase Auth·DB·실제 계정 |
| 8 | 공동 Space·초대 검증·Offline-first 동기화 |
| 9 | 이미지 업로드·WorkManager |
| 10 | 테스트·성능 |
| 11 | 배포·사용자 피드백 |
| 12 | App Links·알림·Analytics·고도화 |

기존 init 커밋을 출발점으로 사용한다. 원격 push는 별도 수행한다.
