---
title: 'Chặn đúng số khi nhấn giữ cuộc gọi ở tab Cuộc gọi'
type: 'feature'
created: '2026-11-06'
status: 'done'
baseline_commit: 'c52b8dbaf6d22d99485d5fd95c67797d0b1036e9'
route: 'dispatch'
review_loop_iteration: 0
context: []
---

<frozen-after-approval reason="human-owned intent — do not modify unless human renegotiates">

## Intent

**Problem:** Muốn chặn một số từ tab Cuộc gọi, người dùng phải mở màn chi tiết rồi bấm "Chặn số này"; chưa có cách nhanh ngay trên danh sách.

**Approach:** Nhấn giữ một dòng cuộc gọi có số và chưa bị chặn → hộp thoại xác nhận → `AddBlockRuleUseCase(number, MatchType.EXACT)` → snackbar báo kết quả. Số ẩn và cuộc gọi đã bị chặn không phản hồi nhấn giữ (giống điều kiện `canBlock` của màn chi tiết).

## Boundaries & Constraints

**Always:** Dialog constructor rỗng, số đi qua `arguments`, kết quả về qua Fragment Result API (mẫu `ConfirmUnblockDialogFragment`). Thông báo một lần đặt trong UI state (`userMessageRes`) + `onUserMessageShown()`. Chuẩn hóa/kiểm tra trùng để `AddBlockRuleUseCase` lo. Giữ testTag `call_log_list` và nhấn thường vẫn mở chi tiết. Chuỗi mới tiền tố `call_log_*`.

**Never:** Không sửa 3 bug Lab 2 (BUGS.md: matcher đầu số, crash từ chối quyền, crash dialog xoá ở blocklist). Không thêm dependency. Không log số. Không `catch (e: Exception)`. Không thêm kiểu chặn đầu số ở luồng này.

## I/O & Edge-Case Matrix

| Scenario | Input / State | Expected Output / Behavior | Error Handling |
|----------|--------------|---------------------------|----------------|
| Chặn mới | Xác nhận chặn `0987654321`, chưa có quy tắc | Thêm quy tắc EXACT; snackbar "Đã chặn số này."; dòng hiện "Bị chặn theo quy tắc …" | N/A |
| Trùng | Đã có quy tắc EXACT cùng số (dạng chuẩn hóa) | Không thêm; snackbar "Số này đã có trong danh sách chặn." | N/A |
| Không hợp lệ | Số quá ngắn sau chuẩn hóa (vd `123`) | Không thêm; snackbar "Không chặn được số này." | N/A |
| Số ẩn | `number` rỗng | Nhấn giữ không làm gì; ViewModel bỏ qua số rỗng | Không gọi use case |
| Huỷ | Bấm "Hủy" trong dialog | Không có kết quả, không đổi gì | N/A |
| Danh sách phát lại | Quy tắc mới khiến danh sách phát lại `Content` | Snackbar vẫn hiện đúng một lần | N/A |

</frozen-after-approval>

## Code Map

- `feature/calllog/.../CallLogViewModel.kt` -- `_uiState` gán thẳng từ `collect`; cần luồng `userMessage` riêng để không mất khi danh sách phát lại.
- `feature/calllog/.../CallLogUiState.kt` -- `Content` thêm `userMessageRes`.
- `feature/calllog/.../model/CallUiModel.kt` + `CallUiMapper.kt` -- thêm `number: String?` (null với số ẩn) và `canBlock`.
- `feature/calllog/.../CallLogScreen.kt` -- `CallItem` dùng `clickable`; đổi sang `combinedClickable`; thêm `Scaffold`/`SnackbarHost`.
- `feature/calllog/.../CallLogFragment.kt` -- mở dialog, lắng nghe kết quả trên `childFragmentManager`.
- `feature/calllog/.../detail/CallDetailViewModel.kt#onBlockNumber` -- mẫu map `AddBlockRuleResult` → chuỗi; tái dùng `call_log_detail_message_{blocked,duplicate,invalid}`.
- `feature/calllog/.../detail/CallDetailScreen.kt#UserMessageEffect` -- mẫu hiện snackbar một lần (private; chép hoặc chuyển thành `internal` dùng chung).
- `feature/calllog/.../detail/ConfirmUnblockDialogFragment.kt` + test -- mẫu dialog và test Robolectric.
- `core/testing/.../TestData.kt`, `FakeBlockRuleRepository` -- dùng cho test, không mock.

## Tasks & Acceptance

**Execution:**
- [x] `feature/calllog/src/main/res/values/strings.xml` -- thêm `call_log_block_title`, `call_log_block_message` (%1$s = số), `call_log_action_block` -- chuỗi dialog.
- [x] `feature/calllog/.../model/CallUiModel.kt`, `CallUiMapper.kt` -- thêm `number`, `canBlock = number != null && !isBlocked` -- Screen quyết định có gắn nhấn giữ.
- [x] `feature/calllog/.../CallLogUiState.kt`, `CallLogViewModel.kt` -- `userMessageRes`; `onBlockConfirmed(number)`, `onUserMessageShown()`; kết hợp `userMessage` vào `uiState` (`stateIn` Eagerly để test cũ đọc `.value` vẫn đúng).
- [x] `feature/calllog/.../ConfirmBlockNumberDialogFragment.kt` (mới) -- `newInstance(number)`, `REQUEST_KEY`, `parseResult()`.
- [x] `feature/calllog/.../CallLogScreen.kt` -- `onCallLongClick: (CallUiModel) -> Unit`, `onUserMessageShown`; `onLongClick` chỉ khi `canBlock`, có `onLongClickLabel`; snackbar; Preview vẫn chạy.
- [x] `feature/calllog/.../CallLogFragment.kt` -- hiện dialog, nhận kết quả → `viewModel.onBlockConfirmed`.
- [x] Test: `CallLogViewModelTest` (chặn mới, trùng, không hợp lệ, số rỗng, xoá message, message còn sau khi danh sách phát lại); `CallUiMapperTest` (`number`/`canBlock` cho số ẩn, đã chặn); `ConfirmBlockNumberDialogFragmentTest` (xác nhận trả số, sống qua recreate, huỷ không gửi); `CallLogScreenTest` (nhấn giữ gọi callback, số ẩn không gọi).

**Acceptance Criteria:**
- Given tab Cuộc gọi có danh sách, when nhấn thường một dòng, then vẫn mở màn chi tiết như cũ.
- Given dialog chặn đang mở, when xoay màn hình rồi bấm "Chặn", then quy tắc vẫn được thêm cho đúng số.
- Given đã chặn xong, when quay lại, then số mới chặn hiện nhãn bị chặn mà không cần làm mới.

## Design Notes

`uiState = combine(_listState, userMessage) { s, m -> if (s is Content) s.copy(userMessageRes = m) else s }.stateIn(viewModelScope, Eagerly, PermissionRequired)` — giống `CallDetailViewModel` nhưng Eagerly để giữ hành vi hiện tại (test đọc `.value` không cần collector). Dialog nhận số thay vì `callId`: không phụ thuộc danh sách đã tải lại sau process death.

## Verification

**Commands:**
- `./gradlew :feature:calllog:testDebugUnitTest` -- expected: xanh
- `./gradlew lint` -- expected: không lỗi mới

**Manual checks (if no CLI):**
- Nhắc người dùng sinh lại Baseline Profile (đổi màn hình chính).

## Implementation Notes

- Triển khai trực tiếp trong phiên, không qua subagent.
- `onCallLongClick` nhận `number: String` thay vì `CallUiModel`: Screen chỉ gắn nhấn giữ khi `canBlock`, nên Fragment không cần biết model hiển thị.
- `UserMessageEffect` trong `detail/CallDetailScreen.kt` đổi từ `private` sang `internal` để tab Cuộc gọi dùng lại, không chép code.
- `CallLogScreen` bọc `Scaffold` (`contentWindowInsets = WindowInsets(0)`) để có `SnackbarHost`; nội dung cũ tách sang `CallLogContent` (private). testTag `call_log_list` và `testTagsAsResourceId` giữ nguyên.
- `combinedClickable` vẫn là experimental ở Compose BOM 2024.12.01, nên cần `@OptIn(ExperimentalFoundationApi::class)` trên `CallItem`.
- Điều bất ngờ: vì `stateIn` chạy ngay trong constructor, `CallLogViewModelTest` phải tạo ViewModel `by lazy` (field initializer chạy trước `MainDispatcherRule.starting`).
- Kết quả: `:feature:calllog:testDebugUnitTest` có 71 test, xanh hết; `lint` không lỗi; module calllog không có cảnh báo; app vẫn 77 cảnh báo như trước khi sửa.
- Sau review (bmad-build bước 4): `CallItem` luôn truyền `onLongClick`, còn khi `!canBlock` thì không làm gì và không gắn `onLongClickLabel`, để thả tay sau khi nhấn giữ không mở màn chi tiết. Đã có 72 test, xanh hết.
- Quyết định của người dùng sau `android-review` (2026-11-06): chọn phương án (b), giữ hành vi trên. Dòng số ẩn hoặc đã bị chặn: nhấn giữ không làm gì, đúng như ma trận I/O. Đánh đổi được chấp nhận: TalkBack vẫn có thể đọc hành động nhấn giữ (không có nhãn) ở các dòng này. Phương án (a) bị loại là truyền `onLongClick = null`: semantics sạch nhưng giữ lâu rồi thả sẽ mở chi tiết. Hành vi TalkBack chưa kiểm trên máy thật.

## Spec Change Log

## Review Triage Log

| # | Nguồn | Phát hiện | Verdict | Bằng chứng | Hướng |
|---|-------|-----------|---------|-----------|-------|
| 1 | blind | Nhấn giữ dòng không chặn được, thả tay thì mở màn chi tiết | medium | Chạy `CallLogScreenTest` hai lần, thêm `assertNull(clickedCallId)`: với dòng đã bị chặn, `onClick` vẫn chạy ổn định khi `onLongClick = null`. Trái với ma trận ("Nhấn giữ không làm gì") | patch: luôn truyền `onLongClick`, chỉ chặn khi `canBlock`; nhãn TalkBack chỉ gắn khi `canBlock`; thêm assert ở cả 2 test |
| 2 | blind + edge | Thông báo nằm chờ khi state không phải `Content` (Loading/Error/PermissionRequired) | low | Có xảy ra, nhưng trường hợp thường gặp duy nhất là sau khi process bị dừng: kết quả dialog đến lúc STARTED, trước `onResume`, rồi thông báo hiện khi danh sách tải xong. Đó đúng là hành vi mong muốn. Trường hợp Error rồi Thử lại rất hiếm, và sửa thì phải thêm nhánh | reject |
| 3 | blind + edge | Hai dialog chồng nhau khi nhấn giữ liên tiếp | low | Dialog modal hiện ngay sau lần nhấn giữ khoảng 500 ms; `ConfirmUnblockDialogFragment` cũng làm như vậy. Sửa thì phải thêm guard | reject |
| 4 | blind | `UserMessageEffect` của danh sách import từ gói `detail` | low | Màn cha phụ thuộc màn con: nếu sau này refactor `detail` sẽ làm vỡ tab Cuộc gọi | patch: chuyển sang `calllog/UserMessageEffect.kt` |
| 5 | blind | Tab Cuộc gọi mượn chuỗi `call_log_detail_*` | false | Cùng một kết quả (đã chặn/trùng/không hợp lệ) nên cố ý dùng chung một câu; đổi câu ở một chỗ thì đổi cả hai là đúng. Tiền tố `call_log_*` vẫn đúng quy ước | reject |
| 6 | blind + vgap | Không có test nối `CallLogFragment` (`childFragmentManager`) với `onBlockConfirmed` | medium | Đã được verification-gap kiểm: không có harness cho Fragment Hilt; 2 dialog có sẵn cũng chưa có test này | defer |
| 7 | blind | Test "snackbar hiện một lần" chưa thử trường hợp danh sách phát lại | low | `LaunchedEffect(messageRes)` không chạy lại khi giá trị giống nhau, nhưng chưa có test chứng minh | patch: thêm `replayed list with the same message does not show snackbar again` |
| 8 | blind + edge | Spec lệch với code (chữ ký `onCallLongClick`, chỗ đặt `canBlock`), AC và Verification thiếu | low | Cách sửa là sửa spec của build này | reject |
| 9 | blind | Người dùng khó biết là có thể nhấn giữ | low | Ý định đã chọn nhấn giữ; thêm gợi ý là ngoài phạm vi | reject |
| 10 | blind | Thiếu Preview cho trạng thái có thông báo | false | Preview tĩnh không chạy `LaunchedEffect`, nên không thấy snackbar; `previewCalls` đã có dòng số ẩn và dòng đã chặn | reject |
| 11 | blind | `Eagerly` lệch quy ước `WhileSubscribed`; mẹo `by lazy` chưa ghi vào gotchas | low | `CallLogViewModel` trước đây là `MutableStateFlow`, không dùng `WhileSubscribed`; `Eagerly` giữ nguyên hành vi đó. Cách sửa là sửa CLAUDE.md | defer |
