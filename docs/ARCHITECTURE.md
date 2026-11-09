# Kiến trúc SpamGuard Lite

## Tóm tắt

Có 3 tầng, và phụ thuộc chỉ đi **từ ngoài vào trong**:

- **Presentation**: `feature/*`, `app`
- **Domain**: `core/domain`, viết bằng Kotlin thuần, không biết gì về Android
- **Data**: `core/data`

Tầng domain định nghĩa interface, tầng data cài đặt interface đó, và Hilt nối hai bên lại với nhau.

```mermaid
flowchart LR
    subgraph Presentation
        app[":app"]
        calllog[":feature:calllog"]
        blocklist[":feature:blocklist"]
    end
    domain[":core:domain<br/>(Kotlin thuần)"]
    data[":core:data<br/>(Room, ContentResolver)"]
    testing[":core:testing<br/>(fake, test rule)"]
    ui[":core:ui<br/>(theme Compose)"]

    app --> calllog
    app --> blocklist
    calllog --> calllognav[":feature:calllog-navigation<br/>(NavKey)"]
    blocklist --> blocklistnav[":feature:blocklist-navigation<br/>(NavKey)"]
    app --> calllognav
    app --> blocklistnav
    app --> data
    calllog --> domain
    blocklist --> domain
    data --> domain
    testing --> domain
    app --> ui
    calllog --> ui
    blocklist --> ui
```

Feature module **không** phụ thuộc `:core:data`. Feature chỉ biết use case và interface repository trong domain.

Mỗi feature có một module `*-navigation` chỉ chứa `NavKey` của các màn hình trong feature đó. Feature muốn mở màn hình của feature khác chỉ phụ thuộc module navigation của feature đích, không phụ thuộc chính feature đó.

`:baselineprofile` là module test (`com.android.test`) nằm ngoài sơ đồ trên. Nó chạy `:app` trên thiết bị bằng UiAutomator để sinh Baseline Profile và đo hiệu năng. Module này chỉ biết package name và các testTag lộ thành resource-id (`call_log_list`, `blocklistFragment`), không phụ thuộc code của module nào.

`:core:ui` chỉ chứa theme dùng chung (`SpamGuardTheme` cho Compose; `Theme.SpamGuard` chỉ là theme XML của cửa sổ trước khi Compose vẽ), không chứa logic và không phụ thuộc domain.

## Luồng dữ liệu (UDF)

Lấy ví dụ màn hình **Cuộc gọi**:

```
CallLogRoute ────onPermissionResult()──▶ CallLogViewModel
      ▲                                        │ GetClassifiedCallsUseCase(limit)
      │ uiState: StateFlow<CallLogUiState>     ▼
      └── CallLogScreen(state) ◀── CallUiMapper ◀── Flow<List<ClassifiedCall>>
                                                     │
                       CallLogRepository ◀───────────┤ (đọc 1 lần)
                       BlockRuleRepository.observeRules() (phát lại khi quy tắc đổi)
```

- **App shell** (`SpamGuardNavigation` trong `:app`): `Scaffold` có thanh trên, thanh điều hướng dưới và `NavDisplay` của Navigation 3. Back stack là danh sách `NavKey` được lưu lại khi xoay màn hình hoặc hệ thống dừng process.
- **Route/Screen**: `CallLogRoute` lấy ViewModel bằng `hiltViewModel()`, collect `uiState` bằng `collectAsStateWithLifecycle()` rồi truyền cho `CallLogScreen`. Route cũng xử lý việc cần Android API (xin quyền, mở Cài đặt, RoleManager, clipboard). Screen là composable không giữ state, chỉ vẽ `uiState` (kể cả dialog) và gọi callback sự kiện.
- **ViewModel** giữ trạng thái màn hình trong `StateFlow`. Trạng thái của mỗi màn hình được mô tả bằng một kiểu duy nhất (`CallLogUiState`, `BlocklistUiState`).
- **Mapper** (`CallUiMapper`, `BlockRuleUiMapper`) chuyển model domain thành model hiển thị: định dạng thời gian, chọn icon, chọn chuỗi. Nhờ vậy ViewModel gọn và mapper test được riêng.
- **Use case** chỉ làm một việc, gọi qua `operator fun invoke`.
- **Repository impl** đổi dữ liệu thô (Cursor, Entity) thành model domain, và đổi lỗi hạ tầng thành lỗi domain (`CallLogUnavailableException`).

## SOLID trong repo

| Nguyên tắc | Ở đâu |
| --- | --- |
| **S**: một lý do để thay đổi | `PhoneNumberNormalizer` chỉ chuẩn hóa số; `SpamClassifier` chỉ phân loại; `AddBlockRuleUseCase` chỉ thêm quy tắc; mapper chỉ định dạng hiển thị |
| **O**: mở rộng không sửa | Kiểu so khớp là chiến lược `BlockRuleMatcher`. Thêm kiểu mới thì thêm `MatchType` và một matcher, không phải sửa `SpamClassifier` |
| **L**: thay thế được | `FakeCallLogRepository` và `FakeBlockRuleRepository` thay hoàn toàn cho bản thật trong test |
| **I**: interface nhỏ | `CallLogRepository` (đọc) tách khỏi `BlockRuleRepository` (đọc/ghi); `CallLogDataSource` chỉ có 2 hàm đọc (danh sách gần nhất, một cuộc gọi theo id) |
| **D**: phụ thuộc vào trừu tượng | Domain định nghĩa interface; `DataModule` dùng `@Binds` nối impl. `Clock`, `ZoneId`, `CoroutineDispatcher` đều được inject thay vì gọi trực tiếp |

## Quy ước

- **Trạng thái UI**: ViewModel phát `StateFlow`; Route collect bằng `collectAsStateWithLifecycle()`. Không dùng LiveData.
- **Compose**: Screen không giữ state và không gọi repository; item `LazyColumn` có `key` ổn định; mỗi trạng thái màn hình có `@Preview`.
- **Thông báo một lần** (ví dụ snackbar): đặt trong UI state (`userMessageRes`). UI hiển thị xong thì gọi `onUserMessageShown()`.
- **Dialog**: composable vẽ theo `uiState`. ViewModel giữ trạng thái mở/đóng và dữ liệu cần xác nhận (ví dụ id quy tắc), nên dialog còn nguyên khi xoay màn hình và id không đi vòng qua UI.
- **Điều hướng**: Navigation 3. Feature đăng ký màn hình bằng `EntryProviderScope<NavKey>.xxxEntries(...)`; tham số màn hình nằm trong `NavKey` và tới ViewModel qua assisted injection.
- **Lỗi**: ViewModel chỉ bắt các lỗi domain đã biết. Lỗi lập trình phải để lộ ra, không nuốt.
- **Chuỗi hiển thị**: luôn đặt trong `strings.xml`, tên có tiền tố theo module (`call_log_*`, `blocklist_*`).
- **Dữ liệu cá nhân**: không log số điện thoại hay tên danh bạ. Test dùng dữ liệu giả.
- **Visibility**: implementation trong `core/data` để `internal`, chỉ interface domain là public.

## Thêm một màn hình mới

1. Tạo module `feature/<tên>` (sao chép `build.gradle.kts` của `feature/blocklist`) và `feature/<tên>-navigation` (sao chép của `feature/blocklist-navigation`), rồi thêm cả hai vào `settings.gradle.kts`.
2. Domain: thêm use case (và interface repository nếu cần).
3. Data: cài đặt repository rồi `@Binds` trong `DataModule`.
4. Feature: viết `NavKey` trong module navigation; `UiState`, `UiMapper`, `ViewModel`, `XxxRoute`/`XxxScreen` (kèm Preview), hàm `xxxEntries()`, `strings.xml`; test cho ViewModel và mapper.
5. App: gọi `xxxEntries()` trong `entryProvider` của `SpamGuardNavigation`; nếu là tab, thêm vào `TopLevelDestination`. Thêm trường hợp vào instrumented test `NavigationTest` nếu là luồng chính.
