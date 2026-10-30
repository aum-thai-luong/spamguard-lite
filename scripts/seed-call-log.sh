#!/usr/bin/env bash
# Thêm nhật ký cuộc gọi mẫu vào emulator/thiết bị đang kết nối qua adb.
# Dùng cho các lab: chạy một lần sau khi tạo emulator mới.
#
#   ./scripts/seed-call-log.sh            # thiết bị mặc định
#   ANDROID_SERIAL=emulator-5556 ./scripts/seed-call-log.sh
set -euo pipefail

readonly INCOMING=1
readonly OUTGOING=2
readonly MISSED=3
readonly REJECTED=5

now_millis=$(( $(date +%s) * 1000 ))

# insert_call <số> <loại> <số phút trước> <thời lượng giây> [tên danh bạ]
insert_call() {
  local number=$1 type=$2 minutes_ago=$3 duration=$4 name=${5:-}
  local date_millis=$(( now_millis - minutes_ago * 60 * 1000 ))
  local bindings="--bind number:s:${number} --bind type:i:${type} --bind date:l:${date_millis} --bind duration:i:${duration} --bind new:i:0"
  if [[ -n "$name" ]]; then
    bindings+=" --bind name:s:'${name}'"
  fi
  adb shell "content insert --uri content://call_log/calls ${bindings}"
  echo "  + ${number} ${name}"
}

echo "Đang thêm cuộc gọi mẫu..."
insert_call "0900123456"   "$MISSED"   5    0
insert_call "+84900555777" "$INCOMING" 30   12
insert_call "0919009001"   "$INCOMING" 90   245 "Chị Lan"
insert_call "0987654321"   "$OUTGOING" 180  63  "Anh Minh"
insert_call "02873001234"  "$INCOMING" 300  128
insert_call "0912345678"   "$REJECTED" 1440 0
echo "Xong. Mở SpamGuard Lite và kéo để xem tab Cuộc gọi."
