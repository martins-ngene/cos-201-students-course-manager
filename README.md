# Student Course Management System

**COS 201 — Programming I | Mid-Semester Lab Assessment | MIVA Open University**
Author: Martins Ngene

A Java application that lets a student record their semester courses, view them, search by
course code, compute total credit units, and save/load the list to a file. It has a Swing GUI
and a console (text-menu) mode that share the same logic.

## How to run

Requires a JDK, version 11 or newer.

```bash
./compile_and_run.sh           # GUI
./compile_and_run.sh console   # console menu
./compile_and_run.sh test      # automated tests (32 checks)
```

Manual commands:

```bash
mkdir -p bin
javac -d bin src/*.java test/*.java
java -cp bin CourseManagementGUI              # GUI
java -cp bin CourseManagementGUI --console    # or: java -cp bin CourseManagementConsole
java -cp bin CourseManagerTest
```

Courses are saved to `data/courses.txt`, one `CODE,Title,Units` line per course.

## Operations

| # | Operation | GUI | Console |
|---|-----------|-----|---------|
| 1 | Add Course | Add form / Operations › 1 | option 1 |
| 2 | View All Courses | *View All* button / table | option 2 |
| 3 | Search Course by Code | Search box (Enter) | option 3 |
| 4 | Compute Total Units | *Total Units* button + totals bar | option 4 |
| 5 | Save to File | *Save* button / Ctrl(⌘)+S | option 5 |
| 6 | Load from File | *Load* button / Ctrl(⌘)+L | option 6 |
| 7 | Exit Program | Operations › 7 / window close | option 7 |

Extra: *Delete Selected* removes a course from the list.

## Project structure

```
cos-201/
├── src/
│   ├── Course.java                   model + validation + file-line conversion
│   ├── CourseManager.java            ArrayList store, recursive search/total, save/load
│   ├── CourseManagementGUI.java      Swing interface
│   └── CourseManagementConsole.java  text interface with a recursive menu
├── test/CourseManagerTest.java       plain-Java tests (no libraries)
├── screenshots/                      screenshots used in the report
├── tools/                            screenshot and report generators
├── COS201_Project_Submission_Report.pdf
└── compile_and_run.sh
```

## Requirement mapping

| Requirement | Where |
|-------------|-------|
| Classes and objects | `Course`, `CourseManager`, `CourseManagementGUI`, `CourseManagementConsole` |
| Methods for operations | one method per operation in each interface; logic in `CourseManager` |
| Decision-making for menu selection | `switch` in `CourseManagementConsole.showMenu()`; GUI menu/button listeners |
| Invalid input handling | validating setters, `Course.parseUnits`, dialogs / re-ask loops |
| String processing | trim, upper-case, remove inner spaces, collapse spaces, regex code check |
| Iteration | for-each over courses, `while` file reading, re-ask `while` loop |
| Recursion | `searchCourseByCode(code, index)`, `computeTotalUnits(index)`, `showMenu()` |
| Exceptions | `IllegalArgumentException`, `NumberFormatException`, `FileNotFoundException`, `IOException` |
| File I/O | `BufferedWriter` / `BufferedReader` with try-with-resources |
| GUI / console | Swing window and organised console menu |
