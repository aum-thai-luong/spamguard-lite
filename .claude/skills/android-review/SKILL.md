---
name: android-review
description: Review code Android/Kotlin theo checklist của team (lifecycle, coroutines, Compose, kiến trúc, bảo mật & dữ liệu cá nhân, permission, Room, build/R8, test) và xếp mức độ blocker / nên sửa / gợi ý. Dùng khi được yêu cầu review code, review diff, review MR, kiểm tra trước khi tạo merge request, hoặc trong bước code-review của BMAD.
---

# android-review

Review như một senior Android dev khó tính nhưng công bằng: chỉ nêu vấn đề có thật, có bằng chứng, có cách sửa.

## 1. Xác định phạm vi

- Mặc định: thay đổi chưa merge so với nhánh chính — `git diff origin/main...HEAD` (dùng `develop` nếu repo dùng nhánh đó) cộng thay đổi chưa commit (`git diff`).
- Nếu người dùng chỉ định file/MR/commit thì review đúng phạm vi đó.
- Đọc `CLAUDE.md` để biết rules của team. Đọc thêm file xung quanh khi cần hiểu ngữ cảnh, nhưng **chỉ góp ý trên code thay đổi** (trừ khi thay đổi làm lộ lỗi ở code cũ).
- Nếu review trong BMAD: đọc story file và `architecture.md`, kiểm tra từng acceptance criteria có code và test tương ứng.

## 2. Checklist

### Lifecycle & bộ nhớ
- Giữ `Activity`/`Context`/`View` trong object sống lâu hơn (singleton, ViewModel, companion) → leak
- Collect Flow trong UI không theo lifecycle (thiếu `repeatOnLifecycle` / `collectAsStateWithLifecycle`)
- State quan trọng mất khi process death (thiếu `SavedStateHandle`)

### Coroutines
- `GlobalScope`, `runBlocking` trên main thread, scope tự tạo không bị huỷ
- IO/CPU nặng chạy trên main thread (thiếu `withContext(Dispatchers.IO)` hoặc dispatcher inject)
- `catch (e: Exception)` nuốt luôn `CancellationException`
- Flow nóng/lạnh dùng sai; `stateIn` thiếu `SharingStarted` hợp lý

### Compose
- Logic nghiệp vụ hoặc gọi repository trong Composable
- Tạo object/lambda nặng mỗi lần recompose mà không `remember`; tham số không ổn định gây recompose thừa
- `LazyColumn` thiếu `key`; side effect sai chỗ (`LaunchedEffect` key sai)
- Chuỗi hardcode, thiếu `contentDescription`, vùng chạm < 48dp

### Kiến trúc & rules team
- Vi phạm UDF/layer theo `CLAUDE.md`; dùng LiveData/RxJava trong code mới
- Thêm dependency mới chưa được duyệt; trùng lặp code đã có sẵn trong `:core:*`

### Bảo mật & dữ liệu cá nhân (app xử lý số điện thoại, danh bạ, call log)
- Log số điện thoại, tên danh bạ, nội dung cuộc gọi (`Log.*`, `Timber`, crash report)
- API key, token, URL nội bộ hardcode trong source
- Gửi dữ liệu cá nhân lên server không mã hoá hoặc không cần thiết
- `exported=true` không có lý do; `PendingIntent` thiếu `FLAG_IMMUTABLE`

### Permission & hệ thống
- Thiếu kiểm tra runtime permission (READ_CALL_LOG, READ_CONTACTS, POST_NOTIFICATIONS trên Android 13+)
- Background work không dùng WorkManager; foreground service thiếu khai báo type (Android 14+)
- Hành vi có thể vi phạm chính sách Google Play về Call Log/SMS permission

### Room & dữ liệu
- Đổi Entity mà không tăng version / không có migration / không có migration test (→ dùng skill `room-migration`)
- Query trên main thread; thiếu index cho cột lọc thường xuyên

### Build & release
- Class dùng reflection/serialization thiếu R8 keep rule
- Thay đổi `minSdk`/`targetSdk`/version dependency không được nhắc trong mô tả MR

### Test
- Logic mới không có test; test chỉ để đủ coverage mà không kiểm tra hành vi
- Test phụ thuộc thời gian thật, mạng thật, thứ tự chạy

## 3. Mức độ

| Mức | Nghĩa | Ví dụ |
| --- | --- | --- |
| 🔴 Blocker | Phải sửa trước khi merge | Crash, mất dữ liệu, leak dữ liệu cá nhân, lỗ hổng bảo mật, vi phạm chính sách Play |
| 🟡 Nên sửa | Nên sửa trong MR này | Vi phạm rules team, thiếu test cho logic mới, rủi ro hiệu năng rõ ràng |
| 🔵 Gợi ý | Tuỳ chọn | Đặt tên, đơn giản hoá, refactor nhỏ |

Không xếp vấn đề style mà ktlint/detekt đã bắt được — chỉ nhắc chạy lint.

## 4. Kiểm chứng trước khi kết luận

- Chạy `./gradlew <module>:testDebugUnitTest` và `./gradlew <module>:lintDebug` cho các module bị ảnh hưởng (nếu được phép chạy lệnh). Ghi kết quả vào báo cáo.
- Với mỗi blocker, chỉ ra đúng dòng code và giải thích vì sao lỗi xảy ra. Không chắc chắn → ghi là "cần xác nhận", không xếp blocker.
- Không bịa API Android: nếu nghi ngờ một API không tồn tại hoặc đã deprecated, nói rõ là nghi ngờ.

## 5. Định dạng báo cáo

```markdown
## Kết quả review — <phạm vi>

**Kết luận:** ✅ Có thể merge | ⚠️ Merge sau khi sửa mục 🟡 | ⛔ Chưa merge được (có blocker)
**Kiểm chứng:** test <pass/fail/chưa chạy> · lint <pass/fail/chưa chạy>

### 🔴 Blocker
1. `path/File.kt:42` — <vấn đề>. **Vì sao:** <hậu quả>. **Sửa:** <cách sửa, kèm đoạn code ngắn nếu cần>

### 🟡 Nên sửa
...

### 🔵 Gợi ý
...

### Acceptance criteria (nếu có story)
- [x] AC1 — có code + test `XxxTest`
- [ ] AC2 — chưa thấy test
```

Mục nào không có vấn đề thì ghi "Không có". Không sửa code trong lúc review trừ khi người dùng yêu cầu.
