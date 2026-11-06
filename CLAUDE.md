# SpamGuard Lite

App Android mẫu của khóa AI-Assisted Android Development: đọc nhật ký cuộc gọi, cho người dùng tạo quy tắc chặn (đúng số hoặc theo đầu số), đánh dấu cuộc gọi khớp quy tắc. Kotlin, Clean Architecture + MVVM, màn hình Jetpack Compose đặt trong Fragment (Navigation Component + bottom navigation), dialog XML, Hilt, Room.

## Architecture

@docs/ARCHITECTURE.md

## BMAD

@AGENTS.md

## Mandatory rules

- IMPORTANT: repo **cố ý chứa 3 bug** cho Lab 2 (`BUGS.md`). Không bao giờ sửa chúng khi đang làm việc khác. Khi được giao sửa: viết test fail trước → giải thích nguyên nhân gốc → sửa tối thiểu → mỗi bug một commit.
- `feature/*` không import `com.spamguard.core.data.*`; `core/domain` không import `android.*` hay `androidx.*`.
- ViewModel phát `StateFlow`; Route collect bằng `collectAsStateWithLifecycle()`. Không dùng LiveData.
- ViewModel chỉ catch lỗi domain đã biết (ví dụ `CallLogUnavailableException`). Không `catch (e: Exception)`, không nuốt lỗi khác.
- Dialog: constructor rỗng, dữ liệu vào qua `arguments`, kết quả trả về qua Fragment Result API. Không giữ callback, state hay ViewBinding trong field của Fragment.
- Luôn inject `Clock`, `ZoneId`, `CoroutineDispatcher`; không gọi trực tiếp `System.currentTimeMillis()`, `LocalDateTime.now()`, `Dispatchers.IO`.
- Chuỗi hiển thị nằm trong `strings.xml`, tiền tố theo module (`call_log_*`, `blocklist_*`).
- Không log số điện thoại hay tên danh bạ. Test và Preview chỉ dùng dữ liệu giả.
- Test dùng fake và `TestData` trong `:core:testing`; không mock repository.
- Chuẩn hóa số bằng `PhoneNumberNormalizer` trước khi so khớp (`+84 900-123-456` → `0900123456`); pattern quy tắc lưu ở dạng đã chuẩn hóa.
- Không sửa tay `core/data/schemas/` (Room sinh ra, phải commit) và `app/src/release/generated/`.
- Giữ ổn định testTag `call_log_list` và menu id `blocklistFragment`; `:baselineprofile` tìm UI bằng chúng.
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
./gradlew :app:generateBaselineProfile                     # cần emulator đã seed; commit kết quả
```

## Repo gotchas

- Toàn bộ test hiện có đều xanh dù đang có 3 bug của Lab 2: test xanh không chứng minh code đúng.
- `:core:domain` và `:core:testing` là module JVM thuần, chạy bằng `test`. Module Android chạy bằng `testDebugUnitTest` (ví dụ `:feature:blocklist:testDebugUnitTest`).
- `uiState` dùng `WhileSubscribed`, nên test ViewModel phải mở collector trước (xem `collectUiState()` trong `BlocklistViewModelTest`).
- Repo không có Turbine và `hilt-navigation-compose`: Fragment lấy ViewModel bằng `by viewModels()` rồi truyền vào Route.

## Definition of done

- Chạy test của mọi module đã sửa (không chỉ build) và `./gradlew lint`, rồi báo kết quả.
- Nếu đổi màn hình chính, nhắc người dùng sinh lại Baseline Profile.
- Nếu đã dùng skill trong `.claude/skills/` mà skill sai hoặc lệch repo (template không khớp, phải làm khác hướng dẫn, người dùng sửa lại kết quả do skill gây ra): thêm một mục vào cuối `docs/skill-log.md` theo mẫu ở đầu file, `Chạy lại: chưa`, rồi báo người dùng. Không tự sửa `SKILL.md` khi chưa được yêu cầu.

## More docs

- Skills trong `.claude/skills/`: `compose-screen` (màn hình mới hoặc chuyển XML → Compose), `android-review` (review diff/MR), `room-migration` (mọi thay đổi schema Room). Nếu template trong skill mâu thuẫn với file này hoặc `docs/ARCHITECTURE.md`, làm theo file này.
- Triệu chứng 3 bug của Lab 2: `BUGS.md`. Cài đặt và bối cảnh khóa học: `README.md`.
