#!/bin/bash
# ──────────────────────────────────────────────────────────
# compile_and_run.sh — Student Course Management System (COS 201)
#
#   ./compile_and_run.sh           compile and open the GUI
#   ./compile_and_run.sh console   compile and run the text menu
#   ./compile_and_run.sh test      compile and run the automated tests
# ──────────────────────────────────────────────────────────
cd "$(dirname "$0")" || exit 1

# Homebrew OpenJDK support (macOS)
for jdk in /opt/homebrew/opt/openjdk/bin /usr/local/opt/openjdk/bin; do
    [ -d "$jdk" ] && export PATH="$jdk:$PATH"
done

if ! command -v javac >/dev/null 2>&1; then
    echo "javac was not found. Install a JDK (version 11 or newer) and try again."
    exit 1
fi

mkdir -p bin data
echo "Compiling..."
if ! javac -d bin src/*.java test/*.java; then
    echo "Compilation failed. Check the errors above."
    exit 1
fi

case "$1" in
    console) java -cp bin CourseManagementConsole ;;
    test)    java -cp bin CourseManagerTest ;;
    *)       java -cp bin CourseManagementGUI ;;
esac
