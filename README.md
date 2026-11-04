# SpamGuard Lite

Ứng dụng Android mẫu dùng trong khóa **AI-Assisted Android Development**. App đọc nhật ký cuộc gọi, cho người dùng tạo quy tắc chặn theo số hoặc theo đầu số, rồi đánh dấu những cuộc gọi khớp quy tắc.

Repo được viết theo **Clean Architecture + MVVM**, tuân thủ **SOLID**, giao diện dùng **Jetpack Compose** (các màn hình đã được nâng cấp từ XML + ViewBinding bằng AI agent trong Lab 1–3; dialog vẫn dùng XML).

> ⚠️ Repo **cố ý chứa 3 bug** cho Lab 2. Triệu chứng của từng bug nằm ở [BUGS.md](BUGS.md).

## Tính năng

| Tab | Chức năng |
| --- | --- |
| Cuộc gọi | Xin quyền `READ_CALL_LOG`, liệt kê tối đa 200 cuộc gọi gần nhất, đánh dấu cuộc gọi khớp quy tắc chặn, đếm số cuộc gọi bị chặn |
| Danh sách chặn | Thêm quy tắc (đúng số hoặc theo đầu số), xem và xóa quy tắc; dữ liệu lưu bằng Room |

Mọi số điện thoại đều được chuẩn hóa trước khi so khớp, ví dụ `+84 900-123-456` → `0900123456`.

## Yêu cầu

- Android Studio bản stable mới (JDK đi kèm, Java 17 trở lên)
- Android SDK 35
- Emulator hoặc thiết bị Android 8.0 (API 26) trở lên

## Chạy dự án

```bash
./gradlew assembleDebug            # build
./gradlew installDebug             # cài lên emulator/thiết bị
./scripts/seed-call-log.sh         # thêm nhật ký cuộc gọi mẫu (cần adb)
```

Nếu script không thêm được dữ liệu trên máy của bạn, hãy tạo cuộc gọi giả bằng Extended Controls của emulator (**Phone → Call device**), hoặc dùng lệnh `adb emu gsm call 0919009001` rồi `adb emu gsm cancel 0919009001`.

## Kiểm tra

```bash
./gradlew test                                   # unit test toàn bộ
./gradlew lint                                   # Android lint
```

### Hiệu năng (cần emulator đã chạy `seed-call-log.sh`)

```bash
./gradlew :app:generateBaselineProfile                           # sinh lại Baseline Profile
./gradlew :baselineprofile:connectedBenchmarkReleaseAndroidTest  # đo khởi động lạnh
```

Macrobenchmark mặc định từ chối chạy trên emulator vì số đo không đáng tin. Muốn so tương đối trên emulator thì thêm `-Pandroid.testInstrumentationRunnerArguments.androidx.benchmark.suppressErrors=EMULATOR`.

Profile được ghi vào `app/src/release/generated/baselineProfiles/` và cần commit. Hãy sinh lại sau khi đổi màn hình hoặc hành trình chính. Bản release dùng profile đã commit, nên `assembleRelease` không cần thiết bị.

Lần build đầu tiên Room sẽ sinh schema vào `core/data/schemas/`. Hãy commit thư mục này, vì nó cần cho việc viết migration sau này.

## Cấu trúc module

```
app/                 MainActivity, Navigation, cấu hình Hilt (TimeModule)
core/domain/         Kotlin thuần: model, interface repository, use case, SpamClassifier
core/data/           Room, đọc CallLog provider, implementation repository, DI module
core/testing/        Fake repository, builder dữ liệu test, MainDispatcherRule
core/ui/             Theme dùng chung: Theme.SpamGuard (XML) và SpamGuardTheme (Compose)
feature/calllog/     Màn hình nhật ký cuộc gọi (Fragment + Compose + ViewModel)
feature/blocklist/   Màn hình danh sách chặn (Compose) + 2 dialog (XML) + CallScreeningService
baselineprofile/     Sinh Baseline Profile và macrobenchmark cho :app (chạy trên thiết bị)
scripts/             Script tiện ích cho lab
```

Chi tiết về kiến trúc, luồng dữ liệu và cách áp dụng SOLID nằm ở [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md).

## Công nghệ

Kotlin 2.0 · Coroutines/Flow · Hilt · Room · Navigation Component · Jetpack Compose · Material 3 · JUnit 4 + kotlinx-coroutines-test · Robolectric

Phiên bản thư viện được ghim trong `gradle/libs.versions.toml`. Đây là bộ phiên bản đã ổn định; nếu muốn nâng cấp thì dùng AGP Upgrade Assistant trong Android Studio, hoặc giao cho Claude Code như một bài tập.

## Dùng trong khóa học

| Buổi | Dùng repo này để |
| --- | --- |
| 1 | Chạy `/init` tạo `CLAUDE.md`, sinh một màn hình mới theo mẫu `feature/blocklist` |
| 2 | Lab 2: tái hiện bằng test và sửa 3 bug trong [BUGS.md](BUGS.md) |
| 3 | Lab 3: chuyển một màn hình từ XML sang Jetpack Compose |
| 5 | Lab 6: thêm `CLAUDE.md`, `.claude/settings.json` và 3 skills Android |
| 6–7 | Capstone: làm feature mới theo BMAD Method |

Repo **chưa có** `CLAUDE.md`. Học viên sẽ tự tạo ở buổi 1 bằng lệnh `/init`.
