# Skill log

Nhật ký review và chỉnh các skill trong `.claude/skills/` (Lab 6, bước 6). Mỗi khi một skill làm sai hoặc lệch so với repo, thêm một mục: skill nào, sai ở đâu, đã sửa gì trong `SKILL.md`, chạy lại có đạt không.

Trạng thái "Chạy lại": `chưa` = đã ghi nhận, chưa chạy lại; `đạt` = chạy lại skill sau khi sửa và kết quả đúng; `chưa đạt` = cần sửa tiếp.

Cách ghi (người hoặc Claude đều theo mẫu này):

- Thêm vào **cuối file**, dưới heading ngày `## YYYY-MM-DD` (tạo heading mới nếu chưa có ngày hôm nay). Không sửa hay xóa mục cũ, chỉ đổi dòng "Chạy lại" khi đã chạy lại.
- Mỗi mục một lỗi, chỉ ghi lỗi có bằng chứng (lệnh, file, đoạn code), không ghi phỏng đoán.
- Lỗi đã có mục `chưa` thì không ghi trùng.

```
- Skill: <tên skill>
  Sai ở đâu: <skill bảo làm gì, vì sao sai với repo, bằng chứng>
  Sửa SKILL.md: <đề xuất sửa gì, ở mục nào>
  Chạy lại: chưa
```

## 2026-11-05

- Skill: compose-screen
  Sai ở đâu: tạo `CallLogRoute` dùng `hiltViewModel()`, nhưng repo không có `hilt-navigation-compose`; màn hình được host trong Fragment bằng `ComposeView`.
  Sửa SKILL.md: thêm mục "Áp dụng cho SpamGuard Lite": host trong Fragment bằng `ComposeView` + `SpamGuardTheme`, Fragment lấy ViewModel bằng `by viewModels()` rồi truyền vào Route; mẫu chuẩn là `feature/blocklist`.
  Chạy lại: chưa

- Skill: compose-screen
  Sai ở đâu: template test ViewModel dùng Turbine (`uiState.test { }`), repo không có Turbine.
  Sửa SKILL.md: đổi template sang `MainDispatcherRule` + mở collector trước (`collectUiState()` như `BlocklistViewModelTest`), fake lấy từ `:core:testing`.
  Chạy lại: chưa

- Skill: compose-screen
  Sai ở đâu: template ViewModel `catch (e: Exception)`, trái luật "chỉ catch lỗi domain đã biết" trong `CLAUDE.md`; Preview dùng `AppTheme`, mẫu trỏ tới `feature/contacts` (không tồn tại).
  Sửa SKILL.md: chỉ catch `CallLogUnavailableException`; đổi sang `SpamGuardTheme`; mẫu là `feature/blocklist`; thêm yêu cầu `XxxUiMapper` và giữ testTag `call_log_list`.
  Chạy lại: chưa

- Skill: android-review
  Sai ở đâu: hướng dẫn chạy `testDebugUnitTest` và `lintDebug` cho mọi module, nhưng `:core:domain` là module JVM thuần (task đúng là `:core:domain:test`, không có lint).
  Sửa SKILL.md: ghi rõ task theo loại module; nhắc test xanh không chứng minh code đúng vì repo có 3 bug Lab 2.
  Chạy lại: chưa

- Skill: android-review
  Sai ở đâu: checklist chung chung, không bắt được vi phạm riêng của repo (feature import `core.data`, impl không `internal`, sửa `SpamClassifier` thay vì thêm `BlockRuleMatcher`); có thể báo 3 bug Lab 2 như lỗi mới.
  Sửa SKILL.md: thêm mục "Riêng SpamGuard Lite" theo `docs/ARCHITECTURE.md`; lỗi trùng `BUGS.md` ghi là "bug đã biết (Lab 2, BUG-n)", không tự sửa.
  Chạy lại: chưa

- Skill: room-migration
  Sai ở đâu: không biết DB thật (ví dụ bảng `blocked_number`); migration test cần `androidx.room:room-testing` nhưng repo chưa có.
  Sửa SKILL.md: thêm mục "Hiện trạng": `SpamGuardDatabase` v1, `BlockRuleEntity`, schema ở `core/data/schemas/` qua KSP `room.schemaLocation`; thêm `room-testing` là dependency mới, phải hỏi trước.
  Chạy lại: chưa
