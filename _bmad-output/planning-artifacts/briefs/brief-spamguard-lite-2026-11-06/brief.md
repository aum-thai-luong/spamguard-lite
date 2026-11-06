---
title: 'Product Brief: Giờ yên tĩnh cho SpamGuard Lite (capstone)'
status: final
created: '2026-11-06'
updated: '2026-11-06'
---

# Product Brief: Giờ yên tĩnh

## Tóm tắt

"Giờ yên tĩnh" là một tính năng mới của SpamGuard Lite. Trong những khung giờ người dùng tự đặt, ví dụ 21:00–07:00 hoặc lúc họp, mọi cuộc gọi đến đều im chuông và không rung. Riêng các số người dùng đánh dấu là quan trọng (bố mẹ, sếp) vẫn đổ chuông như thường. Cuộc gọi bị im lặng không mất đi: chúng vẫn ở trong tab Cuộc gọi, có đánh dấu riêng để người dùng xem lại sau.

Android đã có chế độ Không làm phiền (DND); giá trị ở đây là gom vào app quản lý cuộc gọi và thiết lập dễ hơn (xem Khác biệt).

Đây là bài capstone của khóa AI-Assisted Android Development: làm trong 3 buổi, trên app có sẵn, theo BMAD Method. Rủi ro lớn nhất: chưa chắc im chuông được số có trong danh bạ; kiểm ngay đầu buổi 1.

## Vấn đề

Người dùng muốn yên tĩnh vào buổi tối (từ khoảng 21h) và trong các cuộc họp. Hiện họ có hai lựa chọn, cả hai đều tệ:

- **Để chuông:** bị làm phiền bởi cuộc gọi không gấp, quảng cáo, số lạ.
- **Tắt chuông hẳn:** sợ lỡ cuộc gọi của bố mẹ hay sếp, đúng những cuộc gọi không được phép lỡ.

DND của hệ thống giải quyết được phần lớn, nhưng nằm sâu trong Cài đặt. Nó dựa vào việc gắn sao trong app Danh bạ, và không nằm ở nơi người dùng xem cuộc gọi. Nhóm giả định rằng người dùng mục tiêu không biết DND hoặc thấy nó khó dùng (xem mục Rủi ro).

## Giải pháp

1. **Danh sách số quan trọng.** Người dùng tự thêm hoặc xóa số. App gợi ý những số hay liên lạc, lấy từ nhật ký cuộc gọi, nên không cần xin thêm quyền đọc danh bạ.
2. **Nhiều khung giờ yên tĩnh.** Mỗi khung có giờ bắt đầu và kết thúc (được phép qua nửa đêm), chọn các thứ trong tuần (mặc định cả tuần, có lối tắt "Hằng ngày" và "Thứ 2–6"), và có công tắc bật/tắt riêng.
3. **Nút "Yên tĩnh trong 1 giờ"** cho cuộc họp đột xuất. Sau 1 giờ, chế độ yên tĩnh tự tắt.
4. **Trong giờ yên tĩnh:** cuộc gọi từ số quan trọng đổ chuông bình thường. Các cuộc gọi khác bị im chuông và không rung, nhưng không bị chặn: vẫn hiện trên màn hình và vẫn vào nhật ký.
5. **Lưới an toàn:** một số gọi lại lần thứ 2 trong vòng 3 phút thì được đổ chuông, kể cả khi không có trong danh sách.
6. **Xem lại:** tab Cuộc gọi đánh dấu các cuộc gọi đã bị im lặng, kèm dòng tóm tắt "N cuộc gọi bị im lặng trong giờ yên tĩnh".

**Thứ tự ưu tiên:** chặn > quan trọng > im lặng. Số đã có trong danh sách chặn thì vẫn bị chặn, và không thêm được vào danh sách quan trọng.

## Khác biệt

Lợi thế duy nhất là tiện: tính năng nằm ngay trong app quản lý cuộc gọi, app gợi ý sẵn số quan trọng, có nút 1 giờ, và người dùng xem lại được cuộc gọi bị im lặng ở cùng một chỗ. Về kỹ thuật, tính năng này không có lợi thế gì so với DND.

## Người dùng

Người đi làm có lịch họp và muốn tối được yên. Họ cần không bị làm phiền, nhưng tuyệt đối không được lỡ cuộc gọi từ một nhóm nhỏ người thân và cấp trên. Thành công với họ là: tối đặt một lần rồi quên đi, sáng ra biết ngay ai đã gọi.

## Tiêu chí thành công

- **Đúng:** trên emulator, trong giờ yên tĩnh, 100% cuộc gọi từ số quan trọng đổ chuông và 100% cuộc gọi khác bị im chuông, theo kịch bản demo có sẵn.
- **Lưới an toàn:** số lạ gọi lại lần thứ 2 trong vòng 3 phút thì đổ chuông.
- **Dễ dùng:** thiết lập một khung giờ và 2 số quan trọng mất dưới 1 phút, tính từ lúc mở app.
- **Chất lượng:** mỗi use case mới có unit test, có migration test cho bảng Room mới, và `./gradlew test lint` xanh.
- **Quy trình:** có brief, PRD, architecture và stories trong `_bmad-output/`, mỗi story đi qua `bmad-build`.

## Phạm vi

**Có trong bản này:** toàn bộ §Giải pháp (mục 1 gồm cả gợi ý số quan trọng), tức 7 phần việc, đều bắt buộc chạy khi demo.

**Thứ tự làm**, để lúc nào cũng có bản demo chạy được:

1. Làm thử phần im chuông với số trong danh bạ
2. Im chuông và danh sách quan trọng
3. Khung giờ
4. Nút 1 giờ
5. Gọi lại lần 2
6. Gợi ý số quan trọng
7. Đánh dấu cuộc gọi bị im lặng

**Không có:**

- Đọc lịch họp từ Google Calendar
- Xử lý khi người quen đổi số
- Im lặng thông báo SMS hay app khác
- Đồng bộ nhiều thiết bị
- Hỗ trợ Android 8–9 (API im chuông chỉ có từ Android 10)

## Rủi ro và điều chưa biết

- **Rủi ro cao, chưa kiểm:** có thể hệ thống không cho app sàng lọc cuộc gọi từ số **có trong danh bạ** nếu app không phải ứng dụng gọi điện mặc định. Khi đó bạn bè đã lưu số vẫn đổ chuông trong giờ yên tĩnh. Bước 1 trong Thứ tự làm (§Phạm vi) kiểm điều này. Nếu không im chuông được số trong danh bạ, nhóm phải bàn lại đề tài trước khi viết PRD. Chi tiết ở `addendum.md`.
- **Phạm vi so với thời gian:** 7 mục bắt buộc, cần bảng Room mới và migration, trong 3 buổi. Nhóm đã chấp nhận rủi ro này; thứ tự làm ở trên dùng để giảm thiệt hại nếu bị trễ.
- **Giả định chưa kiểm chứng:** vấn đề và nhận định "người dùng không biết DND hoặc thấy DND khó dùng" đều dựa trên trải nghiệm của chính nhóm. Nhóm chưa hỏi người dùng nào khác, và chấp nhận điều này trong phạm vi capstone.

## Hướng phát triển

Nếu làm tốt, "Giờ yên tĩnh" có thể thành một bộ hồ sơ theo ngữ cảnh: đi ngủ, đi họp, lái xe. Có thể tự bật theo lịch họp, và có thể học từ việc người dùng hay nghe máy của ai để tự đề xuất số quan trọng. Đây chỉ là định hướng, không thuộc phạm vi capstone.
