#!/bin/bash
# compile and print unique errors: ./cc.sh [maxlines]
cd "$(dirname "$0")"
./gradlew.bat compileJava compileGametestJava > "$TEMP/cc.log" 2>&1
grep -E "^[A-Z]:.*error:" "$TEMP/cc.log" | perl -pe "s#.*saintsdragons.##" | sort -u > "$TEMP/cc.err"
echo "errors: $(wc -l < "$TEMP/cc.err")  $(grep -E 'BUILD' "$TEMP/cc.log")"
head -${1:-60} "$TEMP/cc.err" | cut -c1-230
