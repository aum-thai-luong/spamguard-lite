# SpamGuard Lite

App Android mẫu của khóa AI-Assisted Android Development: đọc nhật ký cuộc gọi, cho người dùng tạo quy tắc chặn (đúng số hoặc theo đầu số), đánh dấu cuộc gọi khớp quy tắc. Kotlin, Clean Architecture + MVVM, toàn bộ UI bằng Jetpack Compose (một Activity, Navigation 3 + bottom navigation), Hilt, Room.

## Architecture

@docs/ARCHITECTURE.md

## BMAD

@AGENTS.md

## Mandatory rules

- IMPORTANT: repo **cố ý chứa 3 bug** cho Lab 2 (`BUGS.md`). Không bao giờ sửa chúng khi đang làm việc khác. Khi được giao sửa: viết test fail trước → giải thích nguyên nhân gốc → sửa tối thiểu → mỗi bug một commit.
- `feature/*` không import `com.spamguard.core.data.*`; `core/domain` không import `android.*` hay `androidx.*`.
- ViewModel phát `StateFlow`; Route collect bằng `collectAsStateWithLifecycle()`. Không dùng LiveData.
- ViewModel chỉ catch lỗi domain đã biết (ví dụ `CallLogUnavailableException`). Không `catch (e: Exception)`, không nuốt lỗi khác.
- Dialog là composable trong `XxxScreen`, hiện theo `uiState` (ví dụ `pendingDeletion`). Mở/đóng và dữ liệu cần xác nhận (id, số) nằm trong ViewModel; nút xác nhận gọi `onXxxConfirmed()` không tham số, ViewModel tự lấy dữ liệu đã giữ. State của ô nhập trong dialog dùng `rememberSaveable`.
- Điều hướng: mỗi feature có module `feature/<tên>-navigation` chỉ chứa `NavKey` (`@Serializable`). Feature đăng ký màn hình bằng hàm `EntryProviderScope<NavKey>.xxxEntries(...)`; `:app` ghép chúng trong `NavDisplay`. Feature không phụ thuộc feature khác, chỉ phụ thuộc module navigation của feature đích.
- Luôn inject `Clock`, `ZoneId`, `CoroutineDispatcher`; không gọi trực tiếp `System.currentTimeMillis()`, `LocalDateTime.now()`, `Dispatchers.IO`.
- Chuỗi hiển thị nằm trong `strings.xml`, tiền tố theo module (`call_log_*`, `blocklist_*`).
- Không log số điện thoại hay tên danh bạ. Test và Preview chỉ dùng dữ liệu giả.
- Test dùng fake và các builder `aXxx()` trong `core/testing/.../TestData.kt` (ví dụ `aCallRecord()`); cần dữ liệu mẫu mới thì thêm builder vào file đó, không tạo `object TestData` hay helper riêng trong module test. Không mock repository.
- Chuẩn hóa số bằng `PhoneNumberNormalizer` trước khi so khớp (`+84 900-123-456` → `0900123456`); pattern quy tắc lưu ở dạng đã chuẩn hóa.
- Không sửa tay `core/data/schemas/` (Room sinh ra, phải commit) và `app/src/release/generated/`.
- Giữ ổn định testTag `call_log_list` và testTag `blocklistFragment` của tab Danh sách chặn; `:baselineprofile` tìm UI bằng chúng.
- Không thêm hay nâng dependency khi chưa hỏi; phiên bản ghim trong `gradle/libs.versions.toml`.
- Trả lời người dùng, viết docs, comment và commit bằng tiếng Việt. Commit theo dạng `<loại> phạm vi: mô tả`, loại đặt trong ngoặc nhọn (`feat`, `fix`, `refactor`, `test`, `perf`, `docs`, `build`, `chore`), ví dụ `<fix> calllog: sửa so khớp đầu số khi số có mã quốc gia`. Commit sửa bug Lab 2 ghi thêm mã bug: `<fix> BUG-2 domain: ...`.

## Commands

Git Bash dùng `./gradlew`, PowerShell dùng `.\gradlew.bat`.

```bash
./gradlew assembleDebug                                    # build
./gradlew installDebug                                     # cài lên máy (minSdk 26, JDK 17)
./scripts/seed-call-log.sh                                 # thêm nhật ký cuộc gọi mẫu qua adb
./gradlew test                                             # toàn bộ unit test
./gradlew lint                                             # Android lint
./gradlew :core:domain:test --tests "*SpamClassifierTest"  # một class test
./gradlew :app:connectedDebugAndroidTest                  # instrumented test (Hilt + dữ liệu giả), cần emulator
./scripts/customize.sh com.acme.notes QuickNotes          # đổi package/tên để dùng repo làm nền app mới
./gradlew :app:generateBaselineProfile                     # cần emulator đã seed; commit kết quả
```

## Repo gotchas

- Toàn bộ test hiện có đều xanh dù đang có 3 bug của Lab 2: test xanh không chứng minh code đúng.
- `:core:domain` và `:core:testing` là module JVM thuần, chạy bằng `test`. Module Android chạy bằng `testDebugUnitTest` (ví dụ `:feature:blocklist:testDebugUnitTest`).
- `uiState` dùng `WhileSubscribed`, nên test ViewModel phải mở collector trước (xem `collectUiState()` trong `BlocklistViewModelTest`).
- Repo không có Turbine. Route lấy ViewModel bằng `hiltViewModel()`; ViewModel cần tham số từ `NavKey` thì dùng assisted injection (xem `CallDetailViewModel.Factory`). ViewModel sống theo entry của back stack (`rememberViewModelStoreNavEntryDecorator`), nên đổi tab sẽ tạo ViewModel mới.
- Instrumented test chạy bằng `HiltTestRunner`; `FakeDataModule` (androidTest của `:app`) thay `DataModule` bằng fake của `:core:testing`. `./gradlew test` của AGP 9 chỉ chạy unit test bản debug.

## Definition of done

- Chạy test của mọi module đã sửa (không chỉ build) và `./gradlew lint`, rồi báo kết quả.
- Nếu đổi màn hình chính, nhắc người dùng sinh lại Baseline Profile.
- Nếu đã dùng skill trong `.claude/skills/` mà skill sai hoặc lệch repo (template không khớp, phải làm khác hướng dẫn, người dùng sửa lại kết quả do skill gây ra): thêm một mục vào cuối `docs/skill-log.md` theo mẫu ở đầu file, `Chạy lại: chưa`, rồi báo người dùng. Không tự sửa `SKILL.md` khi chưa được yêu cầu.

## More docs

- Skills trong `.claude/skills/`: `compose-screen` (màn hình mới; template trong skill có thể còn mẫu Fragment cũ), `android-review` (review diff/MR), `room-migration` (mọi thay đổi schema Room). Nếu template trong skill mâu thuẫn với file này hoặc `docs/ARCHITECTURE.md`, làm theo file này.
- Triệu chứng 3 bug của Lab 2: `BUGS.md`. Cài đặt và bối cảnh khóa học: `README.md`.
