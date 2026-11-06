# Addendum: Giờ yên tĩnh

Những chi tiết không thuộc brief nhưng cần cho PRD và architecture.

## Ràng buộc kỹ thuật (đã kiểm hoặc cần kiểm)

- **Đã kiểm:** muốn im chuông mà không chặn thì dùng `CallScreeningService.CallResponse.Builder#setSilenceCall(true)`, có từ **API 29 (Android 10)**. Theo tài liệu, cuộc gọi vẫn được chuyển tới app gọi điện mặc định, chỉ không đổ chuông. Nguồn: developer.android.com, trang `CallScreeningService.CallResponse.Builder`.
- App có `minSdk 26`, nên trên Android 8–9 không im chuông được qua API này. Cần quyết định với những máy đó thì ẩn tính năng hay báo là không hỗ trợ.
- App đã có `BlockingCallScreeningService` (module `feature/blocklist`). Service này dùng vai trò `ROLE_CALL_SCREENING` và đang trả `REJECT` cho số khớp quy tắc chặn. Giờ yên tĩnh có thể mở rộng chính service này, không cần thêm service mới.
- **Cần xác nhận (rủi ro cao):** hệ thống có gọi service sàng lọc cho cuộc gọi từ số **có trong danh bạ** không, khi app giữ vai trò sàng lọc mà không phải app gọi điện mặc định? Theo trí nhớ của agent thì có thể không, nhưng chưa tìm được câu nào trong tài liệu chính thức. Nếu đúng là không, những người trong danh bạ mà không có trong danh sách quan trọng (ví dụ bạn bè đã lưu số) sẽ **vẫn đổ chuông** trong giờ yên tĩnh. Như vậy tính năng hỏng ở đúng chỗ cốt lõi. Nên làm thử ngay đầu buổi 1 trên emulator: lưu một số vào danh bạ, gọi tới bằng `adb emu gsm call <số>` rồi xem service có được gọi không.
