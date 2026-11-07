import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.List;
import java.util.Scanner;

/**
 * CourseManagementConsole.java
 *
 * Text-based (console) interface for the Student Course Management System.
 * Offers the same seven operations as the GUI through a numbered menu.
 *
 * This class demonstrates:
 * - A RECURSIVE menu: showMenu() calls itself after every operation until
 *   the user chooses Exit (the base case)
 * - Logical decision-making with a switch statement
 * - Loops that keep asking until the input is valid
 * - Handling of invalid input with exceptions and clear messages
 *
 * Run with:  java -cp bin CourseManagementConsole
 *
 * @author Martins Ngene
 * @version 2.0
 */
public class CourseManagementConsole {

    private static final String LINE = "-------------------------------------------------------------";

    private final CourseManager manager = new CourseManager();
    private final Scanner in;
    private final PrintStream out;
    private boolean unsavedChanges = false;

    public CourseManagementConsole(InputStream input, PrintStream output) {
        this.in = new Scanner(input);
        this.out = output;
    }

    // RECURSIVE MENU

    /**
     * Displays the menu, runs the chosen operation, then calls itself again.
     * Base case: the user chooses 7 (Exit) or input ends -> the method returns
     * without calling itself, and the chain of calls unwinds.
     */
    public void showMenu() {
        out.println();
        out.println(LINE);
        out.println("  STUDENT COURSE MANAGEMENT SYSTEM  |  COS 201");
        out.println(LINE);
        out.println("  1. Add Course");
        out.println("  2. View All Courses");
        out.println("  3. Search Course by Code");
        out.println("  4. Compute Total Units");
        out.println("  5. Save to File");
        out.println("  6. Load from File");
        out.println("  7. Exit Program");
        out.println(LINE);

        String choice = prompt("Enter your choice (1-7): ");
        if (choice == null) {                 // input closed (e.g. end of file)
            out.println("\nInput ended. Goodbye!");
            return;                           // base case
        }

        switch (choice) {
            case "1": addCourse(); break;
            case "2": viewAllCourses(); break;
            case "3": searchCourse(); break;
            case "4": computeTotalUnits(); break;
            case "5": saveToFile(); break;
            case "6": loadFromFile(); break;
            case "7":
                if (confirmExit()) {
                    out.println("Goodbye!");
                    return;                   // base case: stop recursing
                }
                break;
            default:
                out.println("[!] '" + choice + "' is not a valid option. Please enter a number from 1 to 7.");
        }

        showMenu();                           // recursive case: show the menu again
    }

    // Operations

    private void addCourse() {
        out.println("\n-- Add Course --");
        try {
            String code = askUntilValid("Course code (e.g. COS201): ", value -> {
                String normalized = Course.normalizeCode(value);
                new Course(normalized, "Placeholder", Course.MIN_UNITS); // validates format
                if (manager.searchCourseByCode(normalized) != null) {
                    throw new IllegalArgumentException("Course " + normalized + " already exists.");
                }
            });
            String title = askUntilValid("Course title: ",
                value -> new Course("AAA100", value, Course.MIN_UNITS));
            String units = askUntilValid("Credit units (" + Course.MIN_UNITS + "-" + Course.MAX_UNITS + "): ",
                value -> new Course("AAA100", "Placeholder", Course.parseUnits(value)));

            Course course = new Course(code, title, Course.parseUnits(units));
            manager.addCourse(course);
            unsavedChanges = true;
            out.println("[OK] Added " + course.getCourseCode() + " - " + course.getCourseTitle()
                + " (" + course.getCreditUnits() + " units).");
        } catch (InputEndedException e) {
            out.println("[!] Add cancelled.");
        }
    }

    private void viewAllCourses() {
        out.println("\n-- All Courses --");
        if (manager.isEmpty()) {
            out.println("No courses recorded yet. Choose 1 to add one.");
            return;
        }
        out.println(String.format("%-3s %-8s %-40s %s", "#", "CODE", "TITLE", "UNITS"));
        List<Course> all = manager.getAllCourses();
        for (int i = 0; i < all.size(); i++) {
            Course c = all.get(i);
            out.println(String.format("%-3d %-8s %-40s %5d",
                i + 1, c.getCourseCode(), c.getCourseTitle(), c.getCreditUnits()));
        }
        out.println(manager.getCourseCount() + " course(s), " + manager.computeTotalUnits() + " total units.");
    }

    private void searchCourse() {
        out.println("\n-- Search Course by Code --");
        String code = prompt("Enter course code: ");
        if (code == null || Course.normalizeCode(code).isEmpty()) {
            out.println("[!] No code entered.");
            return;
        }
        Course found = manager.searchCourseByCode(code);
        if (found == null) {
            out.println("[!] No course found with code '" + Course.normalizeCode(code) + "'.");
        } else {
            out.println("[OK] Found: " + found);
        }
    }

    private void computeTotalUnits() {
        out.println("\n-- Compute Total Units --");
        out.println("Courses registered: " + manager.getCourseCount());
        out.println("Total credit units: " + manager.computeTotalUnits());
    }

    private void saveToFile() {
        out.println("\n-- Save to File --");
        if (manager.isEmpty()) {
            out.println("[!] There are no courses to save.");
            return;
        }
        try {
            manager.saveCourses();
            unsavedChanges = false;
            out.println("[OK] Saved " + manager.getCourseCount() + " course(s) to " + CourseManager.DEFAULT_FILE_PATH);
        } catch (IOException e) {
            out.println("[ERROR] Could not save: " + e.getMessage());
        }
    }

    private void loadFromFile() {
        out.println("\n-- Load from File --");
        if (unsavedChanges) {
            String answer = prompt("Loading replaces your unsaved courses. Continue? (y/n): ");
            if (answer == null || !answer.equalsIgnoreCase("y")) {
                out.println("Load cancelled.");
                return;
            }
        }
        try {
            CourseManager.LoadResult result = manager.loadCourses();
            unsavedChanges = false;
            out.println("[OK] Loaded " + result.loaded + " course(s) from " + CourseManager.DEFAULT_FILE_PATH);
            for (String warning : result.warnings) {
                out.println("  [skipped] " + warning);
            }
        } catch (FileNotFoundException e) {
            out.println("[!] " + e.getMessage() + ". Save some courses first.");
        } catch (IOException e) {
            out.println("[ERROR] Could not load: " + e.getMessage());
        }
    }

    private boolean confirmExit() {
        if (!unsavedChanges) {
            return true;
        }
        String answer = prompt("You have unsaved changes. Exit anyway? (y/n): ");
        return answer == null || answer.equalsIgnoreCase("y");
    }

    // Input helpers

    /** Thrown when the input stream ends while we are waiting for an answer. */
    private static final class InputEndedException extends Exception {
        private static final long serialVersionUID = 1L;
    }

    /** A check that throws IllegalArgumentException when a value is invalid. */
    private interface Validator {
        void check(String value);
    }

    /** Prints a prompt and returns the trimmed answer, or null if input has ended. */
    private String prompt(String message) {
        out.print(message);
        out.flush();
        if (!in.hasNextLine()) {
            return null;
        }
        return in.nextLine().trim();
    }

    /** Keeps asking (a loop) until the validator accepts the answer. */
    private String askUntilValid(String message, Validator validator) throws InputEndedException {
        while (true) {
            String value = prompt(message);
            if (value == null) {
                throw new InputEndedException();
            }
            try {
                validator.check(value);
                return value;
            } catch (IllegalArgumentException e) {
                out.println("  [!] " + e.getMessage() + " Please try again.");
            }
        }
    }

    // Entry point

    public static void main(String[] args) {
        new CourseManagementConsole(System.in, System.out).showMenu();
    }
}
