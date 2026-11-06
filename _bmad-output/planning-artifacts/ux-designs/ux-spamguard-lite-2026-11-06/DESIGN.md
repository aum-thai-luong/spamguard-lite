---
name: SpamGuard Lite — Giờ yên tĩnh
description: Phần bổ sung cho bộ nhận diện SpamGuard Lite (Material 3) dành cho tab Giờ yên tĩnh và nhãn cuộc gọi bị im lặng. Điềm tĩnh, ít màu, đọc nhanh.
status: final
created: '2026-11-06'
updated: '2026-11-06'
sources:
  - ../../prds/prd-spamguard-lite-2026-11-06/prd.md
  - ../../briefs/brief-spamguard-lite-2026-11-06/brief.md
colors:
  brand-primary: '#006A60'
  brand-on-primary: '#FFFFFF'
  brand-primary-dark: '#53DBC9'
  brand-on-primary-dark: '#003731'
  quiet-active-container:
    note: 'M3 role secondaryContainer (Dynamic Color trên Android 12+, sinh từ brand-primary trên máy cũ)'
  quiet-active-on-container:
    note: 'M3 role onSecondaryContainer'
  silenced-label:
    note: 'M3 role tertiary'
  blocked-label:
    note: 'M3 role error — giữ nguyên như tab Cuộc gọi hiện nay'
  muted-text:
    note: 'M3 role onSurfaceVariant'
typography:
  status-title:
    note: 'M3 titleLarge'
  status-detail:
    note: 'M3 bodyMedium'
  section-header:
    note: 'M3 titleSmall, màu primary — cùng kiểu dòng tóm tắt "N cuộc gọi bị chặn" ở tab Cuộc gọi'
  row-title:
    note: 'M3 titleMedium'
  row-meta:
    note: 'M3 bodyMedium, màu muted-text'
  label:
    note: 'M3 labelMedium'
rounded:
  card:
    note: 'M3 shape medium (12dp)'
  chip:
    note: 'M3 shape small (8dp)'
spacing:
  '1': 4dp
  '2': 8dp
  '3': 12dp
  '4': 16dp
  '5': 24dp
  row-min-height: 72dp
  touch-min: 48dp
components:
  status-card:
    container: '{colors.quiet-active-container}'
    content: '{colors.quiet-active-on-container}'
    shape: '{rounded.card}'
    padding: '{spacing.4}'
  status-card-idle:
    container: 'M3 surfaceContainerHigh'
    content: 'M3 onSurface'
  quick-quiet-button:
    style: 'M3 FilledTonalButton khi idle; OutlinedButton "Hủy" khi đang yên tĩnh tạm thời'
  window-row:
    min-height: '{spacing.row-min-height}'
    trailing: 'M3 Switch'
  day-chip:
    style: 'M3 FilterChip'
    shape: '{rounded.chip}'
  important-row:
    min-height: '{spacing.row-min-height}'
    trailing: 'IconButton xóa (icon ic_delete)'
  suggestion-row:
    trailing: 'IconButton thêm (ic_add)'
  silenced-label:
    color: '{colors.silenced-label}'
    typography: '{typography.label}'
  feature-banner:
    style: 'M3 Card màu surfaceContainerHighest, có một TextButton hành động'
---

## Brand & Style

Giờ yên tĩnh không có nhận diện riêng. Nó **kế thừa** nguyên `SpamGuardTheme`: Material 3, Dynamic Color trên Android 12+, màu `brand_*` (#006A60 / #53DBC9) trên máy cũ, có chế độ tối. Tính năng này nói về sự yên lặng, nên giao diện cũng phải yên: ít màu, không minh họa, không hoạt ảnh trang trí. Người dùng mở tab để biết một điều, "bây giờ máy có reo không", và câu trả lời phải đọc được trong một cái liếc.

## Colors

- **Màu chính của app (`{colors.brand-primary}`)** giữ nguyên vai trò như mọi nơi khác trong app: tiêu đề mục, nút chính.
- **Nền thẻ trạng thái khi đang yên tĩnh (`{colors.quiet-active-container}`)** chỉ dùng cho thẻ trạng thái lúc **đang yên tĩnh**. Khi không yên tĩnh, thẻ dùng nền trung tính (`status-card-idle`). Nhờ vậy người dùng biết trạng thái mà không cần đọc chữ.
- **Màu nhãn "Đã im lặng" (`{colors.silenced-label}`)** dùng riêng cho nhãn "Đã im lặng" ở tab Cuộc gọi. Nó cố ý khác màu `error` của nhãn "Bị chặn", vì cuộc gọi bị im lặng không phải là lỗi hay mối nguy.
- **Tránh:** dùng màu đỏ hay `error` cho bất cứ thứ gì thuộc Giờ yên tĩnh, kể cả lúc tính năng chưa hoạt động. Banner báo chưa hoạt động là thông tin, không phải cảnh báo.

## Typography

Dùng thang chữ Material 3 qua token, không đặt cỡ chữ riêng. Thẻ trạng thái dùng `status-title` cho câu chính ("Đang yên tĩnh đến 07:00") và `status-detail` cho dòng phụ. Tiêu đề mục ("Khung giờ", "Số quan trọng", "Gợi ý") dùng `section-header`, giống dòng tóm tắt hiện có ở tab Cuộc gọi. Giờ luôn viết theo kiểu 24 giờ `HH:mm`.

## Layout & Spacing

Một cột, lề ngang `{spacing.4}`, giống tab Cuộc gọi. Khoảng giữa các mục là `{spacing.5}`, giữa các dòng trong một mục là 0 (dòng tự có padding). Mỗi dòng cao tối thiểu `{spacing.row-min-height}`, giống `CallItem`.

## Elevation & Depth

Không dùng bóng đổ. Thẻ trạng thái và banner tách khỏi nền bằng màu (tonal), không bằng bóng.

## Shapes

Thẻ trạng thái và banner bo `{rounded.card}`. Chip chọn ngày bo `{rounded.chip}` theo FilterChip mặc định.

## Components

- **Thẻ trạng thái:** gồm icon mặt trăng (bedtime), một dòng `status-title`, một dòng `status-detail` và `quick-quiet-button`. Khi đang yên tĩnh dùng `status-card`, khi không thì dùng `status-card-idle`.
- **Dòng khung giờ:** dòng chính là "21:00 – 07:00" (`row-title`), dòng phụ là "Hằng ngày" / "Thứ 2–6" / "T2, T4, T6" (`row-meta`). Cuối dòng có Switch.
- **Chip chọn ngày:** 7 FilterChip "T2 … CN" xếp một hàng (xuống dòng nếu màn hẹp). Hai lối tắt "Hằng ngày" và "Thứ 2–6" là FilterChip đặt ngay phía trên.
- **Dòng số quan trọng / dòng gợi ý:** cùng anatomy với dòng của tab Cuộc gọi. Số quan trọng có nút xóa ở cuối, gợi ý có nút thêm ở cuối. Gợi ý có dòng phụ "N cuộc gọi trong 30 ngày".
- **Nhãn "Đã im lặng":** icon mặt trăng 16dp và chữ `label` màu `{colors.silenced-label}`, nằm cùng vị trí với nhãn "Bị chặn theo quy tắc…".
- **Banner tính năng:** chỉ hiện khi tính năng chưa hoạt động (FR-14). Nằm thay chỗ thẻ trạng thái.

## Do's and Don'ts

- **Nên:** để một cái liếc là biết trạng thái. Thẻ trạng thái luôn là thứ đầu tiên trên màn.
- **Nên:** dùng lại icon và anatomy dòng sẵn có của app.
- **Không nên:** dùng đồng hồ đếm ngược chạy từng giây, hoạt ảnh, hay huy hiệu đếm số trên tab.
- **Không nên:** dùng màu đỏ cho bất cứ thứ gì thuộc Giờ yên tĩnh.
