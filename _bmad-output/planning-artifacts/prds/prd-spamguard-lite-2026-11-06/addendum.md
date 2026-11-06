# Addendum: PRD Giờ yên tĩnh

Ghi chú kỹ thuật cho bước architecture. Phần này nói cách làm, nên không đưa vào PRD.

## Cơ chế im chuông

- `CallScreeningService.CallResponse.Builder#setSilenceCall(true)` có từ API 29. Không đặt `setDisallowCall`, nên cuộc gọi vẫn được chuyển tới app gọi điện mặc định. Xem addendum của brief.
- Nên mở rộng `feature/blocklist/.../screening/BlockingCallScreeningService` thay vì thêm service thứ hai, vì hệ thống chỉ gắn **một** app với vai trò sàng lọc và app đó chỉ có một service. Service hiện đã có timeout 3 giây, và khi lỗi thì cho cuộc gọi qua (`BlockRulesUnavailableException` → ALLOW). Hành vi này khớp với FR-11.
- Kiểm vai trò sàng lọc bằng `CallScreeningRole.status()` có sẵn (`Unsupported` / `Enabled` / …). Dùng nó cho FR-14.

## Gợi ý cách tổ chức domain (để SA quyết định)

- Một use case thuần Kotlin quyết định cho mỗi cuộc gọi, ví dụ `DecideIncomingCallUseCase` trả `Block | Ring | Silence`, gom thứ tự ưu tiên FR-9 và gọi lại khẩn FR-10 vào một chỗ để test bằng fake.
- Một hàm thuần cho FR-6, ví dụ `QuietSchedule.isQuietAt(instant, zone, windows, temporaryUntil)`, test với `Clock` giả.
- So khớp số qua `PhoneNumberNormalizer`. Lưu ý BUG-2 trong `BUGS.md` nằm ở matcher đầu số; số quan trọng so khớp đúng số nên không phụ thuộc lỗi đó. Theo quy định của repo, không sửa bug này khi làm việc khác.

## Dữ liệu (Room)

- Ba bảng mới gợi ý: `important_numbers`, `quiet_windows` (giờ bắt đầu và kết thúc tính bằng phút trong ngày, ngày lưu dạng bitmask, enabled), `silenced_calls` (số đã chuẩn hóa, thời điểm). Yên tĩnh tạm thời có thể lưu bằng một dòng cấu hình hoặc DataStore. DataStore là dependency mới, phải hỏi trước.
- Tăng version database, viết `Migration` và migration test theo skill `room-migration`. Không sửa tay `core/data/schemas/`.
- `silenced_calls` cần dọn định kỳ (ví dụ xóa bản ghi cũ hơn 7 ngày), nếu không bảng sẽ lớn dần.

## Rủi ro kỹ thuật đã biết

- Có thể không sàng lọc được cuộc gọi từ số trong danh bạ (PRD §9, câu hỏi 1).
- DND của hệ thống có thể ghi đè lên việc đổ chuông của số quan trọng (PRD §9, câu hỏi 2).
- Cuộc gọi đến trong lúc app đang ghi bản ghi im lặng: nếu ghi không kịp trước khi số đó gọi lại thì có thể không nhận ra gọi lại khẩn. Cửa sổ 3 phút làm rủi ro này rất nhỏ.

## Cảnh báo khi chặn số quan trọng (FR-2)

- Phải sửa `feature/blocklist`: trước khi lưu quy tắc, `AddBlockRuleDialogFragment`/`BlocklistViewModel` kiểm xem quy tắc mới có khớp số quan trọng nào không, rồi hỏi xác nhận. Kết quả trả qua Fragment Result API, dialog có constructor rỗng (quy định của repo).
- `feature/blocklist` không được import `core.data`. Phép kiểm này phải nằm trong một use case ở domain (ví dụ `FindImportantNumbersMatchedByRuleUseCase`).
- BUG-1 (crash khi xóa quy tắc sau khi xoay màn hình) nằm ở dialog xóa của cùng module. Không sửa lỗi này khi làm FR-2.
- Matcher đầu số đang dính BUG-2. Cảnh báo theo đầu số ở FR-2 chỉ đúng sau khi BUG-2 được sửa (xem `[NOTE FOR PM]` ở FR-2).

## Tab Giờ yên tĩnh

- Tạo module mới `feature/quiethours` theo mục "Thêm một màn hình mới" trong `docs/ARCHITECTURE.md`, rồi thêm destination vào `nav_graph.xml` và item vào `bottom_navigation.xml`.
- Giữ nguyên testTag `call_log_list` và menu id `blocklistFragment`; `:baselineprofile` tìm UI bằng hai id này. Thêm tab thì phải sinh lại Baseline Profile.
