#!/usr/bin/env bash
# Biến repo này thành điểm khởi đầu cho một app mới: đổi package, applicationId và tên app.
# Chạy từ thư mục gốc repo (Git Bash trên Windows, hoặc bash trên macOS/Linux), TRƯỚC khi mở
# bằng Android Studio:
#
#   ./scripts/customize.sh <package gốc> <TênApp> ["Tên hiển thị"]
#   ./scripts/customize.sh com.acme.notes QuickNotes "Quick Notes"
#
# Kết quả:
#   com.spamguard.lite      -> <package gốc>            (module :app, applicationId)
#   com.spamguard.<x>       -> <package gốc>.<x>        (core, feature, baselineprofile)
#   SpamGuard...            -> <TênApp>...              (SpamGuardTheme, Theme.SpamGuard, SpamGuardApp, …)
#   "SpamGuard Lite"        -> "<Tên hiển thị>"         (app_name, tài liệu)
#
# Script chỉ đổi tên, không xóa tính năng mẫu (calllog, blocklist): giữ chúng làm mẫu cho màn hình
# đầu tiên rồi xóa khi không cần. Không đụng .git; sau khi chạy, xem `git status` rồi commit.
set -euo pipefail

usage() {
  sed -n '2,9p' "$0" | sed 's/^# \{0,1\}//'
  exit 1
}

[[ $# -lt 2 || $# -gt 3 ]] && usage

# export: perl ở bước 1 đọc các giá trị này qua %ENV.
export NEW_PACKAGE=$1
export NEW_NAME=$2
export NEW_DISPLAY_NAME=${3:-$2}
NEW_NAME_LOWER=$(printf '%s' "$NEW_NAME" | tr '[:upper:]' '[:lower:]')
export NEW_NAME_LOWER

readonly OLD_ROOT=com.spamguard
readonly OLD_APP_PACKAGE=com.spamguard.lite

if ! [[ $NEW_PACKAGE =~ ^[a-z][a-z0-9_]*(\.[a-z][a-z0-9_]*)+$ ]]; then
  echo "Package không hợp lệ: '$NEW_PACKAGE' (ví dụ hợp lệ: com.acme.notes)" >&2
  exit 1
fi
if ! [[ $NEW_NAME =~ ^[A-Z][A-Za-z0-9]*$ ]]; then
  echo "Tên app phải viết PascalCase, chỉ chữ và số: '$NEW_NAME' (ví dụ: QuickNotes)" >&2
  exit 1
fi
if [[ ! -f settings.gradle.kts || ! -d app/src/main/kotlin/com/spamguard/lite ]]; then
  echo "Chạy script từ thư mục gốc repo SpamGuard Lite chưa được đổi tên." >&2
  exit 1
fi
if git rev-parse --git-dir >/dev/null 2>&1 && [[ -n $(git status --porcelain) ]]; then
  echo "Cây làm việc còn thay đổi chưa commit; commit hoặc stash trước để dễ xem lại kết quả." >&2
  exit 1
fi

readonly NEW_PATH=${NEW_PACKAGE//./\/}

# Thư mục không đổi: lịch sử git, output build, cấu hình IDE, tài liệu của khóa học và BMAD.
prune_args=(
  -name .git -o -name .gradle -o -name build -o -name .idea -o -name .kotlin
  -o -name _bmad -o -name _bmad-output -o -name .claude
)

text_files() {
  find . \( "${prune_args[@]}" \) -prune -o -type f \( \
    -name '*.kt' -o -name '*.kts' -o -name '*.xml' -o -name '*.toml' -o -name '*.pro' \
    -o -name '*.properties' -o -name '*.md' -o -name '*.yml' -o -name '*.sh' -o -name '*.json' \
    -o -name '*.txt' \) -print | grep -v '^./scripts/customize.sh$'
}

echo "1/3 Đổi nội dung file…"
# perl thay cho `sed -i` vì cú pháp `-i` khác nhau giữa GNU sed và BSD sed (macOS).
text_files | while IFS= read -r file; do
  perl -pi -e '
    s/\Qcom.spamguard.lite\E/$ENV{NEW_PACKAGE}/g;
    s/\bcom\.spamguard\./$ENV{NEW_PACKAGE}./g;
    s/SpamGuard Lite/$ENV{NEW_DISPLAY_NAME}/g;
    s/SpamGuardLite/$ENV{NEW_NAME}/g;
    s/SpamGuard/$ENV{NEW_NAME}/g;
    s/spamguard-lite/$ENV{NEW_NAME_LOWER}/g;
    s/\bspamguard\.db\b/$ENV{NEW_NAME_LOWER}.db/g;
  ' "$file"
done

echo "2/3 Chuyển thư mục source theo package mới…"
# Mỗi source set: com/spamguard/lite -> <package>, com/spamguard/<x> -> <package>/<x>.
find . \( "${prune_args[@]}" \) -prune -o -type d -path '*/com/spamguard' -print | while IFS= read -r old_dir; do
  base=${old_dir%/com/spamguard}
  mkdir -p "$base/$NEW_PATH"
  for child in "$old_dir"/*; do
    [[ -e $child ]] || continue
    name=${child##*/}
    if [[ $name == lite ]]; then
      cp -R "$child"/. "$base/$NEW_PATH"/
    else
      mv "$child" "$base/$NEW_PATH/$name"
    fi
  done
  rm -rf "$old_dir"
  rmdir "$base/com" 2>/dev/null || true
done

echo "3/3 Đổi tên file và thư mục có tên cũ…"
# Ví dụ SpamGuardApp.kt, schema Room `com.spamguard.core.data.db.SpamGuardDatabase/`.
# Sắp đường dẫn dài trước để đổi phần tử con trước thư mục cha (`find -depth` làm -prune mất tác dụng).
find . \( "${prune_args[@]}" \) -prune -o \( -name '*SpamGuard*' -o -name 'com.spamguard.*' \) -print |
  awk '{ print length($0) "\t" $0 }' | sort -rn | cut -f2- |
  while IFS= read -r path; do
    dir=${path%/*}
    name=${path##*/}
    new_name=${name//$OLD_APP_PACKAGE/$NEW_PACKAGE}
    new_name=${new_name//$OLD_ROOT./$NEW_PACKAGE.}
    new_name=${new_name//SpamGuard/$NEW_NAME}
    [[ $name == "$new_name" ]] || mv "$path" "$dir/$new_name"
  done

# Baseline Profile liệt kê class của app cũ: không còn đúng với app mới, phải sinh lại.
rm -rf app/src/release/generated

cat <<EOF

Xong. Package: $NEW_PACKAGE, tên app: $NEW_NAME ("$NEW_DISPLAY_NAME").
Tiếp theo:
  1. ./gradlew assembleDebug test        # kiểm tra build sau khi đổi tên
  2. Sửa CLAUDE.md, README.md và docs/ cho đúng app mới; xóa BUGS.md (chỉ dành cho khóa học).
  3. Thêm tính năng theo mục "Thêm một màn hình mới" trong docs/ARCHITECTURE.md;
     xóa :feature:calllog và :feature:blocklist khi không cần mẫu nữa.
  4. Sinh Baseline Profile cho app mới khi đã có màn hình chính: ./gradlew :app:generateBaselineProfile
     (sửa UserJourney.kt trong :baselineprofile cho đúng hành trình của app mới).
  5. git add -A && git commit
EOF
