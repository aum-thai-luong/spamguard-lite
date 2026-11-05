#!/usr/bin/env bash
# Chặn agent sửa file sinh tự động hoặc nhạy cảm.
# Đường dẫn có thể dùng "/" hoặc "\" (Windows, trong JSON thành "\\"), nên so cả hai.
payload=$(cat)
if printf '%s' "$payload" | grep -Eq \
  '"file_path" *: *"[^"]*(local\.properties|\.jks|[/\\]+schemas[/\\]+[^"]*\.json|[/\\]+src[/\\]+release[/\\]+generated[/\\]+)'; then
  echo "File này được bảo vệ, không sửa bằng agent." >&2
  exit 2
fi
exit 0
