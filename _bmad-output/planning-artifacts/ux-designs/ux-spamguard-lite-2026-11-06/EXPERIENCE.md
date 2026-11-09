---
name: SpamGuard Lite — Giờ yên tĩnh
status: final
created: '2026-11-06'
updated: '2026-11-09'
sources:
  - ../../prds/prd-spamguard-lite-2026-11-06/prd.md
  - ../../briefs/brief-spamguard-lite-2026-11-06/brief.md
---

# Giờ yên tĩnh — Experience Spine

## Foundation

App Android có sẵn, một bề mặt duy nhất là điện thoại, đứng dọc. Hệ thống UI là **Jetpack Compose + Material 3** đặt trong Fragment, dùng Navigation Component và bottom navigation. Theme là `SpamGuardTheme`. Dialog dùng XML `DialogFragment`, trả kết quả qua Fragment Result API, theo quy định của repo. `DESIGN.md` mô tả giao diện trông thế nào; tài liệu này chỉ ghi phần hành vi khác với mặc định của Material 3. Thuật ngữ dùng đúng như PRD §3.

## Information Architecture

| Bề mặt | Mở từ | Mục đích | FR |
|---|---|---|---|
| **Tab Giờ yên tĩnh** | Bottom navigation, tab thứ 3, nhãn "Yên tĩnh", icon mặt trăng | Trạng thái hiện tại, nút 1 giờ, danh sách khung giờ, danh sách quan trọng và gợi ý | FR-1, 3, 4, 7, 14 |
| **Sửa khung giờ** | Bấm "Thêm khung giờ" hoặc bấm một dòng khung giờ | Đặt giờ bắt đầu và kết thúc, chọn ngày, lưu hoặc xóa | FR-4, 5 |
| Bộ chọn giờ | Bấm ô giờ trong Sửa khung giờ | `MaterialTimePicker` 24 giờ | FR-4 |
| Dialog thêm số quan trọng | Bấm "Thêm số" | Nhập số bằng tay (XML, giống `dialog_add_block_rule`) | FR-1 |
| Dialog cảnh báo chặn số quan trọng | Tab Danh sách chặn, khi lưu quy tắc khớp số quan trọng | Hỏi xác nhận | FR-2 |
| **Tab Cuộc gọi** (có sẵn) | Bottom navigation | Thêm dòng tóm tắt và nhãn "Đã im lặng" | FR-12, 13 |

Nhãn tab rút gọn thành "Yên tĩnh"; tên tính năng vẫn là Giờ yên tĩnh.

Tab Giờ yên tĩnh là **một màn cuộn**, đi từ trên xuống theo thứ tự: thẻ trạng thái, mục **Khung giờ**, mục **Số quan trọng**, mục **Gợi ý**. Sửa khung giờ là một destination riêng (Fragment + Compose), mở chồng lên tab và quay lại bằng nút Back. Không có modal lồng hai tầng.

Mock tham khảo bố cục: [`mockups/quiet-tab.html`](mockups/quiet-tab.html) (đang yên tĩnh, sáng/tối, chưa có vai trò) và [`mockups/edit-window.html`](mockups/edit-window.html) (hợp lệ, lỗi chưa chọn ngày). Hai mock chỉ để minh họa; nếu mâu thuẫn với tài liệu này hoặc `DESIGN.md` thì làm theo tài liệu. Các bề mặt còn lại (bộ chọn giờ, dialog thêm số, dialog cảnh báo chặn, phần thêm vào tab Cuộc gọi) không vẽ mock và được xây từ các bảng bên dưới.

## Voice and Tone

Câu chữ ngắn, bình thản, không dấu chấm than. Giọng khiêm tốn giống brief. Mọi chuỗi đặt trong `strings.xml` với tiền tố `quiet_hours_*` (riêng chuỗi thêm vào tab Cuộc gọi dùng `call_log_*`).

| Nên | Không nên |
|---|---|
| "Đang yên tĩnh đến 07:00" | "Chế độ Không làm phiền ĐANG BẬT!" |
| "Sẽ bật lúc 21:00" | "Còn 3 giờ 12 phút nữa" (đếm ngược) |
| "Chỉ số quan trọng mới đổ chuông." | "Chặn tất cả cuộc gọi" (sai: cuộc gọi không bị chặn) |
| "Đã im lặng" | "Bị bỏ qua", "Bị lọc" |
| "Giờ yên tĩnh chưa hoạt động. Cho phép SpamGuard sàng lọc cuộc gọi." | "Lỗi quyền" |
| "Số này đang bị chặn." | "Không thể thêm do xung đột quy tắc" |

## Component Patterns

Phần này chỉ ghi hành vi; phần trông thế nào nằm ở `DESIGN.md.Components`.

| Thành phần | Dùng ở | Quy tắc hành vi |
|---|---|---|
| Thẻ trạng thái | Đầu tab | Hiện một trong ba câu theo FR-14: "Đang yên tĩnh đến HH:mm" / "Sẽ bật lúc HH:mm" / "Đang tắt". Dòng phụ: "Chỉ số quan trọng mới đổ chuông." khi đang yên tĩnh. Nội dung cập nhật khi tới giờ bắt đầu hoặc kết thúc mà không cần mở lại tab. |
| Nút yên tĩnh 1 giờ | Trong thẻ trạng thái | Khi không có yên tĩnh tạm thời: "Yên tĩnh trong 1 giờ". Khi đang có: hiện "Thêm 1 giờ" (gia hạn từ lúc bấm, FR-7) và "Hủy". Bấm xong thì snackbar xác nhận "Yên tĩnh đến 15:00". |
| Dòng khung giờ | Mục Khung giờ | Bấm vào dòng thì mở Sửa khung giờ. Bấm Switch thì bật hoặc tắt ngay, không cần xác nhận. Danh sách sắp theo giờ bắt đầu. |
| Nút "Thêm khung giờ" | Cuối mục Khung giờ | Mở Sửa khung giờ với giá trị mặc định 21:00–07:00, Hằng ngày. |
| Sửa khung giờ | Màn riêng | Hai ô giờ, hai lối tắt chọn ngày, 7 chip ngày, nút "Lưu" và (khi sửa) "Xóa khung giờ". Lỗi kiểm tra (giờ trùng nhau, không chọn ngày) hiện ngay dưới trường liên quan, và nút Lưu bị vô hiệu cho tới khi hợp lệ. Có thay đổi chưa lưu mà bấm Back thì bỏ thay đổi, không hỏi lại. |
| Chip ngày | Sửa khung giờ | Bấm để bật hoặc tắt từng ngày. Lối tắt "Hằng ngày" / "Thứ 2–6" ghi đè toàn bộ tập ngày. Nếu tập ngày đang chọn khớp lối tắt nào thì lối tắt đó hiện ở trạng thái đã chọn. |
| Dòng số quan trọng | Mục Số quan trọng | Hiện số đã chuẩn hóa. Nút xóa thì xóa ngay, kèm snackbar "Đã xóa" có nút "Hoàn tác". |
| Nút "Thêm số" | Cuối mục Số quan trọng | Mở dialog nhập số. Lỗi (trùng số, không hợp lệ, đang bị chặn) báo bằng snackbar sau khi đóng dialog, giống cách thêm quy tắc chặn hiện nay. |
| Dòng gợi ý | Mục Gợi ý | Hiện tối đa 10 số (FR-3). Nút thêm đưa số vào danh sách quan trọng ngay; dòng biến khỏi Gợi ý và xuất hiện ở Số quan trọng. Mục Gợi ý ẩn hẳn khi không có gợi ý nào. |
| Dòng tóm tắt im lặng | Tab Cuộc gọi, ngay dưới dòng "N cuộc gọi bị chặn" | Chỉ hiện khi N ≥ 1 (FR-13). Bấm vào thì không làm gì. |
| Nhãn "Đã im lặng" | Dòng cuộc gọi ở tab Cuộc gọi | Cùng chỗ với nhãn "Bị chặn…". Nếu một dòng đủ điều kiện cả hai thì chỉ hiện "Bị chặn…" (FR-12). |
| Dialog cảnh báo chặn số quan trọng | Tab Danh sách chặn | Tiêu đề "Chặn cả số quan trọng?". Nội dung "Quy tắc này sẽ chặn cả số quan trọng 0912345678." (nhiều số thì liệt kê, tối đa 3 số rồi "và N số khác"). Hai nút "Vẫn chặn" và "Hủy". |

## State Patterns

| Trạng thái | Bề mặt | Cách hiển thị |
|---|---|---|
| Thiết bị dưới Android 10 | Tab Giờ yên tĩnh | Banner "Thiết bị này không hỗ trợ Giờ yên tĩnh (cần Android 10 trở lên)." Các mục bên dưới vẫn xem được nhưng mọi Switch và nút đều vô hiệu. |
| Chưa có vai trò sàng lọc | Tab Giờ yên tĩnh | Banner "Giờ yên tĩnh chưa hoạt động. Cho phép SpamGuard sàng lọc cuộc gọi." với nút "Cho phép", mở hộp thoại hệ thống xin vai trò. Vẫn cấu hình được bình thường. Khi quay lại tab, banner tự biến mất nếu đã được cấp. |
| Chưa có khung giờ | Mục Khung giờ | "Chưa có khung giờ nào." và nút "Thêm khung giờ". |
| Danh sách quan trọng rỗng | Mục Số quan trọng | "Chưa có số quan trọng. Khi đang yên tĩnh, mọi cuộc gọi sẽ im lặng." Câu này cảnh báo hệ quả để người dùng không tưởng là vẫn có số được reo. |
| Không có quyền đọc nhật ký | Mục Gợi ý | Ẩn mục Gợi ý; không xin quyền từ tab này (FR-3). |
| Đang yên tĩnh | Thẻ trạng thái | `status-card` màu `quiet-active-container`. |
| Lỗi đọc dữ liệu | Tab Giờ yên tĩnh | "Không đọc được cài đặt Giờ yên tĩnh." và nút "Thử lại". Việc xử lý cuộc gọi vẫn theo FR-11 (đổ chuông). |
| Đang tải | Tab Giờ yên tĩnh | `CircularProgressIndicator` ở giữa, như tab Cuộc gọi. |

## Interaction Primitives

- Bấm để thao tác. Switch bật hoặc tắt ngay. Sửa và tạo khung giờ làm ở màn riêng, xong bấm Lưu.
- Thông báo một lần dùng snackbar qua `userMessageRes` (quy ước của repo). Xóa số quan trọng có nút Hoàn tác.
- Không dùng vuốt để xóa, không nhấn giữ. Tab Cuộc gọi đã dùng nhấn giữ để chặn số, nên tránh để một cử chỉ mang hai nghĩa.
- **Cấm:** đếm ngược theo giây, huy hiệu trên tab, thông báo đẩy kiểu "Giờ yên tĩnh đã bật", hoạt ảnh trang trí.

## Accessibility Floor

Phần này chỉ ghi hành vi; độ tương phản màu nằm ở `DESIGN.md`.

- TalkBack: Switch của khung giờ đọc cả nội dung khung, ví dụ "Khung giờ 21:00 đến 07:00, hằng ngày, đang bật". Chip ngày đọc được tên đầy đủ ("Thứ Hai, đã chọn").
- Thẻ trạng thái là vùng live region (`polite`), nên khi trạng thái đổi thì TalkBack đọc câu mới.
- Nút xóa và nút thêm có nhãn chứa số điện thoại, ví dụ "Xóa số quan trọng 0912345678".
- Vùng chạm tối thiểu `{spacing.touch-min}` (NFR-6). Chữ theo cỡ chữ hệ thống; ở cỡ lớn nhất, dòng khung giờ được xuống dòng, không cắt chữ.
- Thứ tự focus đi theo thứ tự đọc từ trên xuống.

## Inspiration & Anti-patterns

- **Lấy từ app Đồng hồ (báo thức của Android):** mỗi khung giờ là một dòng có Switch, chip ngày T2…CN, và bộ chọn giờ của hệ thống. Người dùng đã quen cách này.
- **Lấy từ DND của hệ thống:** câu trạng thái "Đến 07:00" thay cho đếm ngược.
- **Tránh:** mô hình "lịch" dạng lưới tuần, kiểu Google Calendar. Quá nặng cho vài khung giờ lặp lại.
- **Tránh:** tách mỗi khối ra một màn riêng, vì người dùng phải chạm thêm nhiều lần để thấy toàn cảnh.

## Key Flows

Các flow giữ nguyên tên UJ của PRD.

### Flow UJ-1: Minh đặt giờ yên tĩnh lần đầu (buổi trưa, rảnh 1 phút)

1. Minh bấm tab "Yên tĩnh".
2. Banner báo chưa hoạt động, Minh bấm "Cho phép" rồi chọn SpamGuard trong hộp thoại hệ thống. Banner biến mất.
3. Ở mục Gợi ý, Minh bấm + cạnh số của mẹ và số của sếp. Hai số chuyển lên Số quan trọng.
4. Minh bấm "Thêm khung giờ". Màn mở sẵn 21:00–07:00, Hằng ngày. Minh bấm Lưu.
5. **Cao trào:** thẻ trạng thái hiện "Sẽ bật lúc 21:00 · Chỉ số quan trọng mới đổ chuông." Minh biết tối nay đã có app lo.

Lỗi: Minh từ chối vai trò thì banner vẫn còn và cấu hình vẫn được lưu.

### Flow UJ-2: Buổi tối, chỉ mẹ làm máy reo

Flow này không có giao diện trong app; hành vi nằm ở FR-8 đến FR-10. Thứ duy nhất người dùng thấy là màn hình cuộc gọi của hệ thống, không chuông và không rung với số không quan trọng. **Cao trào:** máy reo đúng lúc mẹ gọi.

### Flow UJ-3: Sáng ra, Minh xem ai đã gọi

1. Minh mở app ở tab Cuộc gọi.
2. Dưới dòng "N cuộc gọi bị chặn" là dòng "2 cuộc gọi bị im lặng trong giờ yên tĩnh".
3. **Cao trào:** hai dòng có nhãn "Đã im lặng". Minh nhận ra lần gọi đầu của anh giao hàng (số chưa lưu) và bấm vào để mở chi tiết, rồi gọi lại từ đó.

### Flow UJ-4: Họp đột xuất

1. Minh mở tab Yên tĩnh, bấm "Yên tĩnh trong 1 giờ".
2. Snackbar báo "Yên tĩnh đến 15:00", thẻ chuyển sang màu đang yên tĩnh.
3. **Cao trào:** 15:00 thẻ tự về "Đang tắt" hoặc "Sẽ bật lúc 21:00". Minh không phải nhớ bật lại chuông.

Lỗi: họp kéo dài thì Minh bấm "Thêm 1 giờ", hạn mới tính từ lúc bấm.

## Tác động lên Baseline Profile và điều hướng

- Thêm item thứ 3 vào `bottom_navigation.xml`. Giữ nguyên menu id `blocklistFragment` và testTag `call_log_list`.
- Thay đổi tab Cuộc gọi (dòng tóm tắt, nhãn) và việc thêm tab đều phải sinh lại Baseline Profile.
