#!/bin/bash
# ./fixat.sh "<error-message regex>" '<perl substitution>'  -- applies the substitution on each erroring line
cd "$(dirname "$0")"
grep -E "^[A-Z]:.*error:" "$TEMP/cc.log" | grep -E "$1" | sed -E 's/^([A-Z]:[^:]*):([0-9]+): error.*/\1\t\2/' | sort -u | while IFS=$'\t' read -r file line; do
  f=$(cygpath -u "$file"); perl -pi -e "if (\$. == $line) { $2 }" "$f"
done
