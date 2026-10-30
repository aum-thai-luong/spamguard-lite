# Báo cáo lỗi đang mở

Đây là 3 lỗi QA và người dùng đã báo. Phần mô tả chỉ ghi **triệu chứng**; tìm nguyên nhân là việc của bạn trong Lab 2.

Quy trình với mỗi bug:

1. Tái hiện bằng test (unit test hoặc instrumented test) và chạy để thấy test **fail**.
2. Giải thích nguyên nhân gốc trước khi sửa.
3. Sửa bằng thay đổi nhỏ nhất, chạy lại test cho tới khi xanh.
4. Mỗi bug một commit; ghi nguyên nhân gốc vào mô tả MR.

Trước khi bắt đầu: cài app và chạy `./scripts/seed-call-log.sh` để có dữ liệu.

---

## BUG-1 · App crash khi xóa quy tắc chặn sau khi xoay màn hình

**Mức độ:** Cao, app crash · **Thiết bị:** Pixel 7 emulator, API 34

**Các bước:**
1. Mở tab **Danh sách chặn**, thêm một quy tắc bất kỳ (ví dụ đầu số `0900`).
2. Bấm nút xóa ở quy tắc đó. Hộp thoại "Xóa quy tắc?" hiện ra.
3. Xoay màn hình (ngang hoặc dọc), hộp thoại vẫn còn trên màn hình.
4. Bấm **Xóa**.

**Mong đợi:** Quy tắc bị xóa và hiện thông báo "Đã xóa quy tắc."
**Thực tế:** App crash.

Ghi chú của QA: nếu không xoay màn hình thì xóa bình thường.

---

## BUG-2 · Số hợp lệ bị đánh dấu là spam

**Mức độ:** Cao, chặn nhầm cuộc gọi của người quen · **Người báo:** khách hàng qua email

**Các bước:**
1. Tab **Danh sách chặn**: thêm quy tắc **theo đầu số** `0900`.
2. Mở tab **Cuộc gọi**.

**Mong đợi:** Chỉ các số bắt đầu bằng `0900` (hoặc `+84900`) bị đánh dấu chặn.
**Thực tế:** Cuộc gọi của **Chị Lan (0919 009 001)** cũng bị đánh dấu "Bị chặn theo quy tắc 0900".

Ghi chú: toàn bộ unit test hiện có đều xanh.

---

## BUG-3 · App crash khi từ chối quyền đọc nhật ký cuộc gọi

**Mức độ:** Nghiêm trọng, crash ngay lần mở đầu tiên · **Thiết bị:** Samsung Galaxy A54, Android 14

**Các bước:**
1. Gỡ app (hoặc vào Cài đặt → Ứng dụng → SpamGuard Lite → Quyền → thu hồi quyền Nhật ký cuộc gọi).
2. Mở app.
3. Khi hệ thống hỏi quyền nhật ký cuộc gọi, chọn **Không cho phép**.

**Mong đợi:** Màn hình hiện lời giải thích vì sao cần quyền, kèm nút "Cấp quyền".
**Thực tế:** App crash ngay sau khi bấm "Không cho phép".

Ghi chú: nếu cho phép thì app chạy bình thường.
