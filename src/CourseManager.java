import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * CourseManager.java
 *
 * Business-logic class for the Student Course Management System.
 * It owns the list of courses and performs every operation on it:
 * add, view, search, total units, delete, save and load.
 *
 * Both user interfaces (CourseManagementGUI and CourseManagementConsole)
 * call this class, so the rules live in one place.
 *
 * This class demonstrates:
 * - An ArrayList as the internal data structure
 * - RECURSIVE search and RECURSIVE total-units computation
 * - Iteration with for-each and while loops
 * - File reading/writing with try-with-resources
 * - Error handling through exceptions
 *
 * @author Martins Ngene
 * @version 2.0
 */
public class CourseManager {

    /** Default file used by Save and Load. */
    public static final String DEFAULT_FILE_PATH = "data/courses.txt";

    /** Internal data structure holding every registered course. */
    private final ArrayList<Course> courses = new ArrayList<>();

    /** Result of a load operation: how many lines were loaded and skipped. */
    public static final class LoadResult {
        public final int loaded;
        public final List<String> warnings;

        LoadResult(int loaded, List<String> warnings) {
            this.loaded = loaded;
            this.warnings = Collections.unmodifiableList(warnings);
        }

        public int skipped() {
            return warnings.size();
        }
    }

    // ─── Add ─────────────────────────────────────────────────────────────────

    /**
     * Adds a course, refusing duplicates.
     *
     * @throws IllegalArgumentException if the course is null or its code already exists
     */
    public void addCourse(Course course) {
        if (course == null) {
            throw new IllegalArgumentException("Cannot add an empty course.");
        }
        if (searchCourseByCode(course.getCourseCode()) != null) {
            throw new IllegalArgumentException(
                "A course with code '" + course.getCourseCode() + "' already exists.");
        }
        courses.add(course);
    }

    // ─── View ────────────────────────────────────────────────────────────────

    /** Returns a copy of all courses so callers cannot change the internal list. */
    public List<Course> getAllCourses() {
        return new ArrayList<>(courses);
    }

    public int getCourseCount() {
        return courses.size();
    }

    public boolean isEmpty() {
        return courses.isEmpty();
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  RECURSION 1 — SEARCH BY COURSE CODE
    // ═════════════════════════════════════════════════════════════════════════

    /**
     * Finds a course by its code. The search term is normalised first
     * (trimmed, inner spaces removed, upper-cased) so "cos 201" finds COS201.
     *
     * @return the matching course, or null if there is none
     */
    public Course searchCourseByCode(String code) {
        String target = Course.normalizeCode(code);
        if (target.isEmpty()) {
            return null;
        }
        return searchCourseByCode(target, 0);   // start the recursion at index 0
    }

    /**
     * Recursive helper.
     * Base case 1: index has passed the end of the list -> not found (null).
     * Base case 2: the course at index matches         -> return it.
     * Recursive case: look at the rest of the list     -> call with index + 1.
     */
    private Course searchCourseByCode(String target, int index) {
        if (index >= courses.size()) {
            return null;
        }
        Course current = courses.get(index);
        if (current.getCourseCode().equals(target)) {
            return current;
        }
        return searchCourseByCode(target, index + 1);
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  RECURSION 2 — TOTAL CREDIT UNITS
    // ═════════════════════════════════════════════════════════════════════════

    /** Returns the sum of credit units of all courses. */
    public int computeTotalUnits() {
        return computeTotalUnits(0);
    }

    /**
     * Recursive helper.
     * Base case: index has passed the end of the list -> 0.
     * Recursive case: units at index + total of the rest of the list.
     *
     * Example for [3, 3, 2]:
     *   total(0) = 3 + total(1) = 3 + (3 + total(2)) = 3 + (3 + (2 + total(3)))
     *            = 3 + (3 + (2 + 0)) = 8
     */
    private int computeTotalUnits(int index) {
        if (index >= courses.size()) {
            return 0;
        }
        return courses.get(index).getCreditUnits() + computeTotalUnits(index + 1);
    }

    // ─── Delete ──────────────────────────────────────────────────────────────

    /** Removes the course with the given code. Returns true if one was removed. */
    public boolean removeCourse(String code) {
        Course found = searchCourseByCode(code);
        return found != null && courses.remove(found);
    }

    // ─── Save ────────────────────────────────────────────────────────────────

    public void saveCourses() throws IOException {
        saveCourses(DEFAULT_FILE_PATH);
    }

    /**
     * Writes every course to the file, one "code,title,units" line each.
     * The parent folder is created if it does not exist.
     *
     * @throws IOException if the file cannot be written
     */
    public void saveCourses(String filePath) throws IOException {
        File file = new File(filePath);
        File parent = file.getAbsoluteFile().getParentFile();
        if (parent != null && !parent.exists() && !parent.mkdirs()) {
            throw new IOException("Could not create folder: " + parent.getPath());
        }
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            for (Course course : courses) {
                writer.write(course.toFileString());
                writer.newLine();
            }
        }
    }

    // ─── Load ────────────────────────────────────────────────────────────────

    public LoadResult loadCourses() throws IOException {
        return loadCourses(DEFAULT_FILE_PATH);
    }

    /**
     * Reads courses from the file and replaces the current list.
     * Lines that are blank, start with '#', are malformed or are duplicates
     * are skipped and reported as warnings instead of stopping the load.
     * The file is read into a temporary list first, so if reading fails
     * part-way the courses already in memory are not lost.
     *
     * @throws FileNotFoundException if the file does not exist
     * @throws IOException           if the file cannot be read
     */
    public LoadResult loadCourses(String filePath) throws IOException {
        File file = new File(filePath);
        if (!file.exists()) {
            throw new FileNotFoundException("No saved file found at " + filePath);
        }

        ArrayList<Course> loaded = new ArrayList<>();
        List<String> warnings = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            int lineNumber = 0;
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                if (line.trim().isEmpty() || line.trim().startsWith("#")) {
                    continue;
                }
                try {
                    Course course = Course.fromFileString(line);
                    boolean duplicate = false;
                    for (Course existing : loaded) {
                        if (existing.getCourseCode().equals(course.getCourseCode())) {
                            duplicate = true;
                            break;
                        }
                    }
                    if (duplicate) {
                        warnings.add("Line " + lineNumber + ": duplicate code "
                            + course.getCourseCode() + " skipped.");
                    } else {
                        loaded.add(course);
                    }
                } catch (IllegalArgumentException e) {
                    // Also catches NumberFormatException (a subclass).
                    warnings.add("Line " + lineNumber + ": " + e.getMessage());
                }
            }
        }

        courses.clear();
        courses.addAll(loaded);
        return new LoadResult(loaded.size(), warnings);
    }
}
