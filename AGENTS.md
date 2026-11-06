<!-- bmad:context -->
<!-- Verified 2026-11-06 against f10a710. Managed by bmad-project-context; edits inside this block are replaced on refresh. Keep anything you want preserved outside the markers. -->

## spamguard-lite: BMAD

Phần bổ sung cho BMAD Method v6.12; quy tắc chung của repo nằm trong `CLAUDE.md` và `docs/ARCHITECTURE.md`. BMAD ghi tài liệu kế hoạch vào `_bmad-output/planning-artifacts/`, story và kết quả triển khai vào `_bmad-output/implementation-artifacts/`, đọc tài liệu dự án từ `docs/`.

## Policy

- Commit cả `_bmad/` và `_bmad-output/`; tài liệu kế hoạch là sản phẩm nộp bài.
- Commit do `bmad-build` tạo theo định dạng commit trong `CLAUDE.md` (`<feat> phạm vi: mô tả`), không theo conventional commits (`feat(scope): ...`) mà skill mặc định.
- Không merge vào `develop` hay `main`; người dùng tự merge.
- `bmad-build` hoặc `bmad-code-review` xong thì hỏi người dùng có chạy thêm `android-review` không; không tự chạy.
- Agent làm sai trong phiên và bị người dùng sửa: đề xuất ghi lại bằng `bmad-project-context record`.

## Running and verifying

- Chạy script BMAD bằng `uv run`, ví dụ `uv run _bmad/scripts/resolve_config.py -p <thư mục gốc>`; không gọi `python` trực tiếp, máy không có `python3` và uv tự cấp Python ≥ 3.11.
- Installer `npx bmad-method install` cần Node.js ≥ 20.12.

## Conventions that differ from defaults

- Không sửa `_bmad/config.toml`, `_bmad/config.user.toml`, `_bmad/_config/` hay `.claude/skills/bmad-*/` để đổi hành vi lâu dài, vì installer ghi đè khi cài hoặc update; ghi vào `_bmad/custom/config.toml` hoặc `_bmad/custom/<skill>.toml` (dùng `bmad-customize`).
- Tên agent là AUM BA, AUM PM, AUM UXDesign, AUM SA, AUM Dev, ghi cứng trong `.claude/skills/bmad-agent-*/SKILL.md`, `customize.toml` và `_bmad/_config/skill-manifest.csv`; sau mỗi lần cài lại BMAD phải đổi lại tên ở các file đó.

## Known pitfalls

- `uv: command not found` trong phiên Claude Code mở trước khi cài uv: PATH của phiên chưa cập nhật, mở phiên mới thay vì cài lại uv.

<!-- /bmad:context -->
