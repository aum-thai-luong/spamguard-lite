# PRD Quality Review: Giờ yên tĩnh

## Nhận định chung

PRD có luận điểm rõ: không lỡ số quan trọng là ưu tiên số một, nên khi lỗi thì cho đổ chuông và có chỉ số đối trọng SM-C1. FR được viết rất cụ thể, mỗi FR có tiêu chí Given/When/Then test được với đồng hồ giả. Có ba lỗ hổng làm giảm độ an toàn khi chuyển sang UX và architecture. Thứ nhất, chưa biết `setSilenceCall` có tắt cả rung không, trong khi FR-8 hứa "không rung". Thứ hai, cảnh báo ở FR-2 dựa vào matcher đầu số đang dính BUG-2. Thứ ba, PRD chưa nói màn Giờ yên tĩnh nằm ở đâu trong app.

## 1. Sẵn sàng để ra quyết định: tốt

Thứ tự ưu tiên (FR-9), hướng xử lý khi lỗi (FR-11) và việc bỏ Android 8–9 (§2.2, §6) đều là quyết định rõ ràng, có nêu cái giá phải trả. §9 có hai câu hỏi mở thật sự, không phải câu hỏi tu từ.

### Phát hiện
- **high** Lời hứa "không rung" chưa được kiểm (FR-8, §4.4) — tài liệu `setSilenceCall` chỉ nói "will not play a ringtone", không nhắc tới rung. Brief và FR-8 đều hứa "không chuông, không rung". *Fix:* đưa câu hỏi "máy có còn rung không" vào phần làm thử ở §9 câu 1, và ghi tiêu chí kiểm tay của FR-8 là phải xác nhận sau khi làm thử.

## 2. Thực chất hay hình thức: tốt

Persona Minh dẫn dắt được các quyết định (nút 1 giờ, gọi lại khẩn). NFR có ngưỡng cụ thể (3 giây, 48dp). Không thấy phần nào chỉ để cho đủ khuôn.

## 3. Mạch chiến lược: tốt

Các tính năng phục vụ cùng một mạch: bật yên tĩnh, đổ chuông đúng số, rồi xem lại. Chỉ số SM-1 kiểm đúng luận điểm, có SM-C1 làm đối trọng.

## 4. Định nghĩa "xong": khá

### Phát hiện
- **high** Cảnh báo FR-2 phụ thuộc BUG-2 (FR-2) — quy tắc chặn đầu số `0900` hiện khớp nhầm với `0919009001` (BUGS.md, BUG-2). Nếu dùng lại matcher này, cảnh báo sẽ báo sai. Repo cấm sửa BUG-2 khi đang làm việc khác. *Fix:* thêm `[NOTE FOR PM]` ở FR-2: tiêu chí chấp nhận chỉ đúng sau khi BUG-2 được sửa (Lab 2). Nếu chưa sửa thì ghi rõ là rủi ro đã biết khi demo.
- **medium** Chưa có giới hạn lưu trữ cho bản ghi cuộc gọi bị im lặng (NFR-1, FR-8) — bản ghi chứa số điện thoại nhưng PRD không nói giữ bao lâu, chỉ addendum có nhắc 7 ngày. *Fix:* thêm vào NFR-1: "bản ghi cuộc gọi bị im lặng cũ hơn 7 ngày bị xóa", kèm tiêu chí test.
- **low** SM-3 chưa nói cách đo (§8) — *Fix:* "bấm giờ tay khi chạy kịch bản demo UJ-1".

## 5. Trung thực về phạm vi: tốt

§6 nêu rõ những gì không làm. Toàn bộ `[ASSUMPTION]` đã được người dùng chốt và ghi trong memlog.

## 6. Dùng được cho bước sau: khá

### Phát hiện
- **medium** Thiếu chỗ đặt màn hình trong app (§4, chưa có mục Information Architecture) — UX và architecture cần biết màn Giờ yên tĩnh là tab thứ 3 ở bottom navigation, một mục trong tab Danh sách chặn, hay mở từ tab Cuộc gọi. Việc này đụng tới `nav_graph.xml`, `bottom_navigation.xml` và Baseline Profile. *Fix:* hỏi người dùng rồi ghi vào §4.6.
- **low** Epic 1 có 11/14 FR (§7) — nặng so với Epic 2, nhưng hợp lý với giới hạn 2 epic và thứ tự làm. Chỉ ghi nhận, không cần sửa.

## 7. Hình dạng phù hợp: tốt

Tính năng có nhiều UX nên có UJ đặt tên nhân vật là đúng. PRD này là brownfield: các chỗ tham chiếu code hiện có (`CallScreeningRole`, `BlockingCallScreeningService`) đã nằm trong addendum.

## Ghi chú kỹ thuật

- Số ID liên tục: FR-1 đến FR-14, UJ-1 đến UJ-4, SM-1 đến SM-3 và SM-C1, NFR-1 đến NFR-6.
- Thuật ngữ được dùng nhất quán. Còn một chỗ cần xem: §2.2 "im lặng SMS" dùng "im lặng" theo nghĩa thường, không phải thuật ngữ "cuộc gọi bị im lặng". Không gây nhầm.
- Không còn `[ASSUMPTION]` nào; không có danh sách giả định là đúng.
