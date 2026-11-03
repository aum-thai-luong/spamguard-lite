---
name: compose-screen
description: Tạo màn hình Jetpack Compose mới theo kiến trúc của team (Route + Screen stateless + ViewModel + UiState + Preview + unit test). Dùng khi được yêu cầu tạo, scaffold màn hình/screen/feature UI mới, hoặc chuyển một màn hình XML sang Compose.
---

# compose-screen

Tạo một màn hình Compose hoàn chỉnh, build được, có test, đúng pattern đang có trong repo.

## 1. Thu thập đầu vào

Cần đủ các thông tin sau trước khi viết code. Thiếu cái nào thì hỏi lại (tối đa 3 câu, gộp trong 1 lượt):

- Tên màn hình và module đích (ví dụ `CallLog` trong `:feature:calllog`)
- Dữ liệu hiển thị và nguồn dữ liệu (repository/use case nào)
- Hành động của người dùng (click, refresh, xoá, điều hướng...)
- Các trạng thái: loading, content, empty, error (và trạng thái riêng nếu có)
- Thiết kế: ảnh chụp, link Figma hoặc mô tả bố cục

## 2. Đọc mẫu có sẵn — bắt buộc

1. Đọc `CLAUDE.md` để lấy màn hình mẫu chuẩn của team (ví dụ `@feature/contacts/`).
2. Nếu `CLAUDE.md` không chỉ định, tìm mẫu gần nhất: tìm `@HiltViewModel` trong các file `.kt`, chọn một màn hình có đủ Route + Screen + ViewModel + test.
3. Làm theo đúng mẫu đó: package, cách đặt tên, cách inject, cách đăng ký navigation, cách viết test. **Mẫu trong repo luôn thắng template bên dưới.**

## 3. Lên kế hoạch

Liệt kê các file sẽ tạo/sửa. Nếu chạm hơn 5 file hoặc phải sửa module khác (navigation, DI, `build.gradle.kts`), dừng lại để người dùng duyệt kế hoạch.

## 4. Viết code

Template tham khảo (điều chỉnh theo mẫu của repo):

```kotlin
// CallLogUiState.kt
sealed interface CallLogUiState {
    data object Loading : CallLogUiState
    data object Empty : CallLogUiState
    data class Content(val items: List<CallLogItemUi>) : CallLogUiState
    data class Error(@StringRes val messageRes: Int) : CallLogUiState
}

// CallLogViewModel.kt
@HiltViewModel
class CallLogViewModel @Inject constructor(
    private val getCallLog: GetCallLogUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow<CallLogUiState>(CallLogUiState.Loading)
    val uiState: StateFlow<CallLogUiState> = _uiState.asStateFlow()

    init { load() }

    fun onRefresh() = load()

    private fun load() {
        viewModelScope.launch {
            _uiState.value = CallLogUiState.Loading
            _uiState.value = try {
                val list = getCallLog()
                if (list.isEmpty()) CallLogUiState.Empty
                else CallLogUiState.Content(list.map { it.toUi() })
            } catch (e: CancellationException) {
                throw e // không nuốt cancellation
            } catch (e: Exception) {
                CallLogUiState.Error(R.string.call_log_error)
            }
        }
    }
}

// CallLogScreen.kt
@Composable
fun CallLogRoute(
    onOpenDetail: (String) -> Unit,
    viewModel: CallLogViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    CallLogScreen(
        uiState = uiState,
        onRefresh = viewModel::onRefresh,
        onItemClick = onOpenDetail,
    )
}

@Composable
fun CallLogScreen(
    uiState: CallLogUiState,
    onRefresh: () -> Unit,
    onItemClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    when (uiState) {
        CallLogUiState.Loading -> LoadingContent(modifier)
        CallLogUiState.Empty -> EmptyContent(modifier)
        is CallLogUiState.Error -> ErrorContent(uiState.messageRes, onRetry = onRefresh, modifier = modifier)
        is CallLogUiState.Content -> CallLogList(uiState.items, onItemClick, modifier)
    }
}

@Preview(showBackground = true)
@Composable
private fun CallLogScreenContentPreview() {
    AppTheme {
        CallLogScreen(
            uiState = CallLogUiState.Content(PreviewData.callLogItems),
            onRefresh = {},
            onItemClick = {},
        )
    }
}
// Thêm Preview cho Loading, Empty, Error và dark mode.
```

Test ViewModel (Turbine + fake + MainDispatcherRule, theo mẫu test của repo):

```kotlin
class CallLogViewModelTest {
    @get:Rule val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `empty list emits Empty`() = runTest {
        val vm = CallLogViewModel(FakeGetCallLogUseCase(result = emptyList()))
        vm.uiState.test {
            assertEquals(CallLogUiState.Empty, awaitItem())
        }
    }
    // Viết thêm: Content, Error, refresh chuyển về Loading rồi Content.
}
```

## 5. Luật bắt buộc

- Screen là stateless: nhận `uiState` + lambda, không gọi ViewModel, không chứa logic nghiệp vụ.
- Collect bằng `collectAsStateWithLifecycle()`; không dùng LiveData.
- Mọi chuỗi hiển thị nằm trong `strings.xml`; icon có `contentDescription` (hoặc `null` nếu chỉ trang trí).
- `modifier: Modifier = Modifier` là tham số tuỳ chọn đầu tiên của Composable.
- `LazyColumn` dùng `key = { it.id }`.
- Không dùng dữ liệu thật (số điện thoại, tên trong danh bạ) trong Preview/test — dùng dữ liệu giả; không log dữ liệu cá nhân.
- Không thêm dependency mới nếu chưa được duyệt.

## 6. Kiểm chứng

Chạy và sửa đến khi xanh (thay `<module>` bằng module đích, ví dụ `:feature:calllog`):

```bash
./gradlew <module>:compileDebugKotlin
./gradlew <module>:testDebugUnitTest
./gradlew <module>:lintDebug
```

Nếu có thiết bị (`adb devices`), cài app và mở màn hình để người dùng xem thử.

## 7. Báo cáo

Kết thúc bằng: danh sách file đã tạo/sửa, kết quả các lệnh kiểm chứng, cách mở màn hình để test tay, và những gì còn để TODO.
