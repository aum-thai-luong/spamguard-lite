---
title: 'PRD: Giờ yên tĩnh (capstone SpamGuard Lite)'
status: final
created: '2026-11-06'
updated: '2026-11-06'
---

# PRD: Giờ yên tĩnh

## 0. Mục đích tài liệu

PRD này dành cho nhóm capstone, người chấm, và các bước BMAD tiếp theo (UX, architecture, epics và stories). Tài liệu xây trên [product brief](../../briefs/brief-spamguard-lite-2026-11-06/brief.md) và không chép lại phần lý do vì sao làm. Thuật ngữ dùng đúng như §3, không dùng từ đồng nghĩa. Yêu cầu chức năng (FR) được đánh số toàn cục. Mỗi FR có **tiêu chí chấp nhận** viết theo Given/When/Then, kiểm được bằng unit test hoặc Robolectric test, không cần thiết bị thật trừ khi ghi rõ. Chi tiết kỹ thuật nằm ở [addendum.md](addendum.md).

**Ràng buộc chung:** tính năng làm trên app SpamGuard Lite hiện có (Android, Kotlin, Clean Architecture + MVVM, Room), không có backend, và chia tối đa 2 epic.

## 1. Tầm nhìn

Giờ yên tĩnh cho người dùng một buổi tối và những cuộc họp không bị cuộc gọi làm phiền, mà không phải đánh đổi bằng nỗi lo lỡ cuộc gọi của bố mẹ hay sếp. Người dùng đặt khung giờ một lần. Trong khung giờ đó, chỉ số quan trọng mới đổ chuông. Cuộc gọi khác vẫn vào máy nhưng im lặng, và sáng hôm sau người dùng biết ngay ai đã gọi.

Tính năng nằm trong app quản lý cuộc gọi người dùng đang dùng, nên ba việc diễn ra ở cùng một chỗ: chọn số quan trọng, đặt giờ, và xem lại.

## 2. Người dùng mục tiêu

### 2.1 Việc người dùng cần làm (JTBD)

- **Chức năng:** im chuông mọi cuộc gọi không quan trọng trong khung giờ cố định (buổi tối) hoặc đột xuất (cuộc họp).
- **Cảm xúc:** yên tâm rằng mình không lỡ cuộc gọi của người thân hay cấp trên, kể cả khi họ gọi lúc khẩn cấp.
- **Bối cảnh:** sáng hôm sau, biết được ai đã gọi trong lúc mình yên tĩnh để gọi lại nếu cần.

### 2.2 Không phục vụ trong bản đầu

- Máy Android 8–9 (API 26–28): hệ thống không cho app im chuông cuộc gọi.
- Người cần tắt tiếng cả SMS và thông báo của app khác: dùng DND của hệ thống.

### 2.3 Hành trình người dùng chính

Persona chung: **Minh**, trưởng nhóm kỹ thuật, có bố mẹ ở quê, hay bị gọi quảng cáo buổi tối và có nhiều cuộc họp không báo trước.

- **UJ-1. Minh đặt giờ yên tĩnh lần đầu trong chưa đầy một phút.**
  Minh mở SpamGuard Lite, vào màn Giờ yên tĩnh. App gợi ý vài số Minh hay liên lạc, lấy từ nhật ký cuộc gọi. Minh chọn số của mẹ và của sếp vào **danh sách quan trọng**. Minh tạo khung 21:00–07:00, để nguyên "Hằng ngày", rồi bật công tắc. Màn hình báo "Đang tắt, sẽ bật lúc 21:00". **Edge case:** app chưa có vai trò sàng lọc cuộc gọi. Màn hình báo tính năng chưa hoạt động và có nút cấp vai trò.

- **UJ-2. Tối đó, chỉ cuộc gọi của mẹ làm máy reo.**
  22:15, một số quảng cáo gọi tới. Máy hiện cuộc gọi nhưng không chuông, không rung. 22:40, mẹ gọi và máy reo như thường. 23:05, bạn thân của Minh (không có trong danh sách) gọi; máy im. Bạn gọi lại lúc 23:06 và lần này máy reo vì đó là **gọi lại khẩn**. **Edge case:** số quảng cáo đã nằm trong danh sách chặn thì vẫn bị chặn như trước, không chỉ im lặng.

- **UJ-3. Sáng ra, Minh biết ngay tối qua ai đã gọi.**
  07:30, Minh mở tab Cuộc gọi. Đầu danh sách có dòng "2 cuộc gọi bị im lặng trong giờ yên tĩnh". Hai cuộc gọi đó có nhãn "Đã im lặng", nên Minh nhận ra ngay số quảng cáo và lần gọi đầu của bạn thân.

- **UJ-4. Họp đột xuất, Minh bấm một nút.**
  14:00, sếp triệu tập họp gấp. Minh bấm **Yên tĩnh trong 1 giờ**. Màn hình báo "Yên tĩnh đến 15:00" và có nút Hủy. 15:00 chế độ tự tắt. Minh không phải nhớ bật lại chuông.

## 3. Thuật ngữ

- **Giờ yên tĩnh** — tên tính năng, đồng thời là tên màn hình cấu hình.
- **Khung giờ yên tĩnh** — một khoảng giờ lặp lại do người dùng tạo, gồm giờ bắt đầu, giờ kết thúc, tập ngày trong tuần và trạng thái bật/tắt. Một người dùng có 0..N khung giờ.
- **Yên tĩnh tạm thời** — chế độ yên tĩnh bật bằng nút "Yên tĩnh trong 1 giờ", tự hết hạn sau 60 phút. Mỗi lúc có tối đa 1.
- **Đang yên tĩnh** — trạng thái tại một thời điểm, đúng khi thời điểm đó nằm trong ít nhất một khung giờ yên tĩnh đang bật, hoặc trong thời gian yên tĩnh tạm thời còn hiệu lực.
- **Số quan trọng** — số điện thoại (đã chuẩn hóa) người dùng chọn để luôn đổ chuông khi đang yên tĩnh. Tập hợp các số này là **danh sách quan trọng**.
- **Gợi ý số quan trọng** — các số app đề xuất từ nhật ký cuộc gọi để người dùng thêm nhanh vào danh sách quan trọng.
- **Cuộc gọi bị im lặng** — cuộc gọi đến mà app đã im chuông vì đang yên tĩnh.
- **Gọi lại khẩn** — cuộc gọi đến từ một số đã có cuộc gọi bị im lặng trong vòng 3 phút trước đó. Cuộc gọi này được đổ chuông.
- **Quy tắc chặn** — khái niệm có sẵn của SpamGuard Lite: số khớp quy tắc chặn bị từ chối.
- **Vai trò sàng lọc cuộc gọi** — quyền hệ thống mà app phải được người dùng cấp thì mới xử lý được cuộc gọi đến.

## 4. Tính năng

**Vị trí trong app:** Giờ yên tĩnh là **tab thứ 3** ở bottom navigation, ngang hàng với Cuộc gọi và Danh sách chặn. Màn này chứa danh sách quan trọng (kèm gợi ý), các khung giờ yên tĩnh, nút "Yên tĩnh trong 1 giờ" và trạng thái hiện tại. Phần xem lại cuộc gọi bị im lặng (§4.5) nằm ở tab Cuộc gọi.

### 4.1 Danh sách quan trọng

**Mô tả:** người dùng quản lý danh sách quan trọng: xem, thêm, xóa. App gợi ý số để thêm nhanh. Số luôn được chuẩn hóa trước khi lưu và trước khi so khớp, nên `+84 912 345 678` và `0912345678` là cùng một số quan trọng. Phần này thực hiện UJ-1.

#### FR-1: Thêm và xóa số quan trọng

Người dùng thêm được một số quan trọng bằng cách nhập số hoặc chọn một gợi ý, và xóa được một số quan trọng.

**Tiêu chí chấp nhận:**
- Given danh sách quan trọng rỗng, when thêm `+84 912 345 678`, then danh sách chứa đúng một số quan trọng `0912345678`.
- Given đã có `0912345678`, when thêm `0912 345 678`, then không có bản ghi mới và người dùng thấy thông báo "đã có trong danh sách quan trọng".
- Given số sau chuẩn hóa không hợp lệ (ngắn hơn 8 hoặc dài hơn 15 chữ số), when thêm, then không lưu và báo "số không hợp lệ" (cùng ràng buộc độ dài với quy tắc chặn đúng số).
- Given danh sách có `0912345678`, when xóa số đó, then danh sách không còn số này.

#### FR-2: Số quan trọng và quy tắc chặn

Hệ thống từ chối thêm một số vào danh sách quan trọng nếu số đó khớp một quy tắc chặn.

**Tiêu chí chấp nhận:**
- Given có quy tắc chặn đầu số `0900`, when thêm `0900123456` làm số quan trọng, then không lưu và báo "số này đang bị chặn".
- Given `0912345678` là số quan trọng, when người dùng tạo quy tắc chặn khớp số này (đúng số hoặc đầu số, ví dụ `0912`), then màn Danh sách chặn cảnh báo "Quy tắc này sẽ chặn cả số quan trọng 0912345678" và hỏi xác nhận.
- Given cảnh báo đó, when người dùng xác nhận, then quy tắc chặn được tạo và cuộc gọi từ số đó bị chặn (thứ tự ưu tiên ở FR-9). Số vẫn nằm trong danh sách quan trọng.
- Given cảnh báo đó, when người dùng hủy, then không tạo quy tắc chặn.
- Given quy tắc chặn mới không khớp số quan trọng nào, then không có cảnh báo, giữ nguyên hành vi hiện nay.

> **[NOTE FOR PM]** Cảnh báo theo đầu số dùng chung matcher đầu số, mà matcher này đang dính BUG-2 (`BUGS.md`): quy tắc `0900` khớp nhầm `0919009001`. Tiêu chí chấp nhận của FR-2 chỉ đúng sau khi BUG-2 được sửa theo quy trình Lab 2 (test fail trước, một commit riêng `<fix> BUG-2 ...`). Nếu đến lúc demo BUG-2 vẫn chưa sửa, ghi đây là rủi ro đã biết: có thể hiện cảnh báo thừa.

#### FR-3: Gợi ý số quan trọng từ nhật ký cuộc gọi

Hệ thống gợi ý tối đa 10 số có nhiều cuộc gọi nhất trong nhật ký cuộc gọi 30 ngày gần nhất. Chỉ tính cuộc gọi đi và cuộc gọi đến đã nghe máy.

**Tiêu chí chấp nhận:**
- Given nhật ký có A (6 cuộc), B (4 cuộc), C (1 cuộc) trong 30 ngày qua, when mở gợi ý, then thứ tự gợi ý là A, B, C.
- Given một số là số ẩn, đã là số quan trọng, hoặc khớp quy tắc chặn, then số đó không xuất hiện trong gợi ý.
- Given cuộc gọi cũ hơn 30 ngày hoặc là cuộc gọi nhỡ, then không được tính.
- Given có 12 số đủ điều kiện, then chỉ hiện 10 số có nhiều cuộc gọi nhất.
- Given app chưa có quyền đọc nhật ký cuộc gọi, then gợi ý trống. Màn hình vẫn cho nhập số bằng tay và **không** xin thêm quyền đọc danh bạ.

### 4.2 Khung giờ yên tĩnh

**Mô tả:** người dùng tạo nhiều khung giờ yên tĩnh. Mỗi khung có giờ bắt đầu, giờ kết thúc và các ngày trong tuần. Mặc định là cả 7 ngày, có lối tắt "Hằng ngày" và "Thứ 2–6". Mỗi khung có công tắc bật/tắt riêng. Khung được phép kéo qua nửa đêm. Phần này thực hiện UJ-1 và UJ-2.

#### FR-4: Tạo, sửa, xóa và bật/tắt khung giờ

Người dùng tạo, sửa, xóa khung giờ yên tĩnh và bật hoặc tắt từng khung.

**Tiêu chí chấp nhận:**
- Given chưa có khung nào, when tạo khung 21:00–07:00 mà không đổi ngày, then khung được lưu với cả 7 ngày và ở trạng thái bật.
- Given giờ bắt đầu bằng giờ kết thúc, when lưu, then bị từ chối với thông báo "giờ bắt đầu và kết thúc phải khác nhau".
- Given không chọn ngày nào, when lưu, then bị từ chối với thông báo "chọn ít nhất một ngày".
- Given một khung đang bật, when tắt công tắc, then khung không còn ảnh hưởng tới trạng thái đang yên tĩnh (FR-6), và cấu hình giờ của khung được giữ nguyên.

#### FR-5: Lối tắt chọn ngày

Người dùng chọn nhanh "Hằng ngày" (7 ngày) hoặc "Thứ 2–6" (5 ngày làm việc), hoặc chọn từng ngày.

**Tiêu chí chấp nhận:**
- When chọn "Thứ 2–6", then tập ngày là {T2, T3, T4, T5, T6}.
- When chọn "Hằng ngày", then tập ngày là cả 7 ngày.

#### FR-6: Xác định trạng thái đang yên tĩnh

Tại mọi thời điểm, hệ thống xác định được có đang yên tĩnh hay không, từ các khung giờ đang bật và yên tĩnh tạm thời. Giờ tính theo múi giờ của thiết bị.

**Tiêu chí chấp nhận** (test với đồng hồ giả):
- Given khung 21:00–07:00 cho Thứ 2, when là Thứ 2 lúc 22:00, then đang yên tĩnh.
- Given khung 21:00–07:00 cho Thứ 2, when là Thứ 3 lúc 06:59, then đang yên tĩnh (phần sau nửa đêm thuộc về ngày bắt đầu).
- Given cùng khung đó, when là Thứ 3 lúc 07:00, then không yên tĩnh (giờ kết thúc không tính vào khung).
- Given khung 21:00–07:00 chỉ cho Thứ 2, when là Thứ 3 lúc 22:00, then không yên tĩnh.
- Given hai khung chồng nhau, then đang yên tĩnh khi thời điểm nằm trong ít nhất một khung.
- Given mọi khung đều tắt và không có yên tĩnh tạm thời, then không yên tĩnh.

### 4.3 Yên tĩnh tạm thời

**Mô tả:** nút "Yên tĩnh trong 1 giờ" dành cho cuộc họp đột xuất. Phần này thực hiện UJ-4.

#### FR-7: Bật, hủy và tự hết hạn

Người dùng bật yên tĩnh tạm thời trong 60 phút và hủy được trước hạn.

**Tiêu chí chấp nhận** (đồng hồ giả):
- Given 14:00, when bật, then đang yên tĩnh đến 15:00 và màn hình hiện "Yên tĩnh đến 15:00".
- Given đã bật lúc 14:00, when là 15:00, then không còn yên tĩnh tạm thời, mà không cần người dùng thao tác gì.
- Given đang yên tĩnh tạm thời, when hủy lúc 14:20, then không còn yên tĩnh tạm thời ngay lúc đó.
- Given đang yên tĩnh tạm thời đến 15:00, when bấm lại lúc 14:30, then hạn được đặt lại thành 15:30 (60 phút tính từ lúc bấm).
- Given app bị hệ thống dừng rồi mở lại lúc 14:30, then vẫn đang yên tĩnh đến 15:00 (trạng thái được lưu lại).

### 4.4 Xử lý cuộc gọi đến khi đang yên tĩnh

**Mô tả:** với mỗi cuộc gọi đến, hệ thống quyết định chặn, đổ chuông hay im lặng. Cuộc gọi bị im lặng vẫn hiện trên màn hình và vẫn vào nhật ký cuộc gọi; chỉ không chuông và không rung. Phần này thực hiện UJ-2.

#### FR-8: Im chuông cuộc gọi không quan trọng

Khi đang yên tĩnh, cuộc gọi đến từ số không quan trọng bị im chuông, không bị chặn, và được ghi nhận là cuộc gọi bị im lặng.

**Tiêu chí chấp nhận:**
- Given đang yên tĩnh và `0987654321` không phải số quan trọng, when số đó gọi đến, then quyết định là **im lặng** và có một bản ghi cuộc gọi bị im lặng (số, thời điểm).
- Given **không** yên tĩnh, then quyết định là **đổ chuông** với mọi số không bị chặn.
- Given đang yên tĩnh và cuộc gọi từ số ẩn, then quyết định là im lặng. Số ẩn không bao giờ là số quan trọng và không được tính là gọi lại khẩn.
- *(Thiết bị thật hoặc emulator, kiểm tay)* Given đang yên tĩnh trên Android 10 trở lên và app có vai trò sàng lọc cuộc gọi, when một số không quan trọng gọi tới, then máy không chuông, không rung, cuộc gọi vẫn hiện và vẫn có trong nhật ký. Tài liệu `setSilenceCall` chỉ nói tới nhạc chuông, nên việc "không rung" phải được xác nhận trong phần làm thử (§9, câu 1); trước khi xác nhận, tiêu chí này chưa phải cam kết.

#### FR-9: Thứ tự ưu tiên

Quyết định cho mỗi cuộc gọi đến theo thứ tự: **chặn > quan trọng > gọi lại khẩn > im lặng > đổ chuông**.

**Tiêu chí chấp nhận:**
- Given số khớp quy tắc chặn, then quyết định là **chặn**, dù có đang yên tĩnh hay không.
- Given đang yên tĩnh và số là số quan trọng (so khớp sau khi chuẩn hóa: `+84912345678` khớp `0912345678`), then quyết định là **đổ chuông**.
- Given đang yên tĩnh, số không quan trọng và không phải gọi lại khẩn, then quyết định là **im lặng**.

#### FR-10: Gọi lại khẩn

Khi đang yên tĩnh, cuộc gọi đến từ một số đã có cuộc gọi bị im lặng trong vòng 3 phút trước đó được đổ chuông.

**Tiêu chí chấp nhận** (đồng hồ giả):
- Given `0987654321` bị im lặng lúc 23:05:00, when gọi lại lúc 23:07:59, then **đổ chuông**.
- Given cùng cuộc gọi bị im lặng đó, when gọi lại lúc 23:08:01 (quá 3 phút), then **im lặng**.
- Given số khác (`0911111111`) bị im lặng lúc 23:05, when `0987654321` gọi lúc 23:06 mà trước đó chưa bị im lặng, then **im lặng**.
- Given số đó bị im lặng ở dạng `+84987654321`, when gọi lại ở dạng `0987654321`, then được tính là cùng một số.

#### FR-11: An toàn khi có lỗi

Nếu không xác định được quyết định kịp thời hoặc đọc dữ liệu bị lỗi, hệ thống cho cuộc gọi **đổ chuông**. Đổ chuông nhầm vẫn tốt hơn là im lặng nhầm một số quan trọng.

**Tiêu chí chấp nhận:**
- Given không đọc được danh sách quan trọng hoặc khung giờ (lỗi dữ liệu domain đã biết), then quyết định là **đổ chuông**.
- Given việc tra cứu vượt giới hạn thời gian, then quyết định là **đổ chuông**. Hành vi chặn đang có giữ nguyên: lỗi khi đọc quy tắc chặn thì cho qua.

### 4.5 Xem lại cuộc gọi bị im lặng

**Mô tả:** tab Cuộc gọi có sẵn cho người dùng biết những cuộc gọi nào đã bị im lặng. Phần này thực hiện UJ-3.

#### FR-12: Nhãn "Đã im lặng" trong tab Cuộc gọi

Mỗi cuộc gọi trong tab Cuộc gọi tương ứng với một cuộc gọi bị im lặng được hiện nhãn "Đã im lặng".

**Tiêu chí chấp nhận:**
- Given có một bản ghi cuộc gọi bị im lặng cho `0987654321` lúc 22:15, và nhật ký cuộc gọi có cuộc gọi đến từ số đó lúc 22:15, then dòng đó có nhãn "Đã im lặng". Bản ghi được ghép với nhật ký theo số đã chuẩn hóa và thời điểm lệch không quá 60 giây.
- Given bản ghi lúc 22:15:00 và cuộc gọi trong nhật ký lúc 22:16:30 (lệch 90 giây), then không có nhãn.
- Given cuộc gọi không có bản ghi tương ứng, then không có nhãn.
- Given cuộc gọi bị chặn, then hiện nhãn "Bị chặn theo quy tắc…" như hiện nay, không hiện nhãn "Đã im lặng".

#### FR-13: Dòng tóm tắt

Đầu tab Cuộc gọi hiện "N cuộc gọi bị im lặng trong giờ yên tĩnh" khi có ít nhất một cuộc gọi bị im lặng trong 24 giờ qua.

**Tiêu chí chấp nhận:**
- Given có 2 bản ghi cuộc gọi bị im lặng trong 24 giờ qua, then dòng tóm tắt hiện N = 2.
- Given không có bản ghi nào trong 24 giờ qua, then không hiện dòng tóm tắt.
- Chuỗi dùng plurals trong `strings.xml`.

### 4.6 Trạng thái và điều kiện hoạt động

#### FR-14: Báo khi tính năng chưa hoạt động được

Màn Giờ yên tĩnh cho biết tính năng có đang hoạt động không, và lý do nếu không.

**Tiêu chí chấp nhận:**
- Given thiết bị dưới Android 10, then màn hình báo "Thiết bị không hỗ trợ Giờ yên tĩnh" và không cho bật khung giờ hay yên tĩnh tạm thời.
- Given Android 10 trở lên nhưng app chưa có vai trò sàng lọc cuộc gọi, then màn hình báo tính năng chưa hoạt động và có nút xin vai trò.
- Given điều kiện đã đủ, then màn hình hiện trạng thái hiện tại: "Đang yên tĩnh đến HH:mm", hoặc "Sẽ bật lúc HH:mm", hoặc "Đang tắt".

## 5. Yêu cầu phi chức năng

- **NFR-1 Quyền riêng tư:** số quan trọng, khung giờ và bản ghi cuộc gọi bị im lặng chỉ lưu trên máy. Không gửi đi đâu, vì app không có backend. Không log số điện thoại hay tên danh bạ. Test và Preview chỉ dùng dữ liệu giả. Bản ghi cuộc gọi bị im lặng chỉ giữ tối đa 7 ngày. Tiêu chí test (đồng hồ giả): given một bản ghi lúc ngày D 22:00, when là D+7 lúc 22:01, then bản ghi đã bị xóa; còn bản ghi lúc D+1 vẫn giữ.
- **NFR-2 Thời gian quyết định:** hệ thống chỉ chờ app sàng lọc trong vài giây. Quyết định cho mỗi cuộc gọi phải nằm trong giới hạn thời gian app đang dùng (3 giây); quá hạn thì xử lý theo FR-11.
- **NFR-3 Lưu bền:** khung giờ, danh sách quan trọng và yên tĩnh tạm thời vẫn còn sau khi khởi động lại app hoặc máy. Thay đổi schema Room có migration và migration test.
- **NFR-4 Kiểm được:** mọi logic thời gian dùng `Clock` và `ZoneId` được inject, không đọc giờ hệ thống trực tiếp, để FR-6, FR-7, FR-10 và FR-13 test được với đồng hồ giả.
- **NFR-5 Nhất quán với repo:** tuân thủ `CLAUDE.md` và `docs/ARCHITECTURE.md` (Clean Architecture + MVVM, `StateFlow`, chuỗi `strings.xml` có tiền tố module, fake thay vì mock). Không thêm dependency khi chưa hỏi.
- **NFR-6 Trợ năng:** công tắc và nút có nhãn đọc được bằng TalkBack. Vùng chạm tối thiểu 48dp.

## 6. Ngoài phạm vi

- Không đọc lịch họp (Google Calendar) để tự bật.
- Không tắt tiếng SMS hay thông báo của app khác.
- Không xử lý trường hợp người quen đổi số.
- Không đồng bộ giữa nhiều thiết bị, không sao lưu lên mạng.
- Không thay thế hay điều khiển DND của hệ thống.
- Không hỗ trợ Android 8–9.

## 7. Phạm vi MVP và chia epic

Toàn bộ FR-1 đến FR-14 đều trong phạm vi và bắt buộc chạy khi demo (theo brief). Chia **2 epic**:

- **Epic 1: Im chuông theo giờ yên tĩnh**, phần lõi để có bản demo chạy được sớm nhất: FR-1, FR-2, FR-4, FR-5, FR-6, FR-7, FR-8, FR-9, FR-10, FR-11, FR-14.
- **Epic 2: Gợi ý và xem lại**, phần hỗ trợ dùng dễ hơn: FR-3, FR-12, FR-13.

Thứ tự làm theo brief. Bước 0 là phần làm thử im chuông số trong danh bạ (§9, câu hỏi 1), làm trước Epic 1.

## 8. Chỉ số thành công

**Chính**
- **SM-1:** chạy kịch bản demo trên emulator (2 số quan trọng, 3 số thường, 1 lần gọi lại khẩn) trong một khung giờ đang bật. 100% cuộc gọi đúng quyết định đổ chuông hoặc im lặng. Kiểm FR-8, FR-9, FR-10.
- **SM-2:** mọi FR có ít nhất một test tự động cho tiêu chí chấp nhận (trừ tiêu chí ghi "kiểm tay"), và `./gradlew test lint` xanh. Kiểm toàn bộ FR.

**Phụ**
- **SM-3:** thiết lập một khung giờ và 2 số quan trọng mất dưới 1 phút, tính từ lúc mở app. Đo bằng bấm giờ tay khi chạy kịch bản demo UJ-1. Kiểm FR-1, FR-3, FR-4.
- **SM-4:** có đủ brief, PRD, architecture và stories trong `_bmad-output/`, và mỗi story đã đi qua `bmad-build`. Kiểm quy trình capstone (từ brief).

**Chỉ số đối trọng (không tối ưu)**
- **SM-C1:** số cuộc gọi bị im lặng. Im lặng nhiều hơn không có nghĩa là tốt hơn. Đối trọng với SM-1: không nới ngưỡng gọi lại khẩn hay bỏ lưới an toàn để tăng tỉ lệ im lặng.

## 9. Câu hỏi còn mở

1. **Điều kiện chặn trước architecture:** hệ thống có chuyển cuộc gọi từ số **có trong danh bạ** cho app sàng lọc không, khi app không phải app gọi điện mặc định? Nếu không, FR-8 hỏng với bạn bè đã lưu số. Cùng lần làm thử, kiểm luôn `setSilenceCall` có tắt cả rung không (FR-8 hứa "không rung"). Cần làm thử trên emulator (xem addendum của brief).
2. Nếu người dùng bật cả DND của hệ thống, số quan trọng có còn đổ chuông không? Có thể là không, vì DND của hệ thống nằm ngoài tầm kiểm soát của app. PRD đang để ngoài phạm vi; cần kiểm khi làm thử và ghi vào hướng dẫn demo.
