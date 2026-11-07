/**
 * Course.java
 *
 * Model class representing a single course in the Student Course Management System.
 * Each course has a course code, a course title and a number of credit units.
 *
 * This class demonstrates:
 * - Encapsulation (private fields, public getters, validating setters)
 * - String processing (trimming, upper-casing, removing inner spaces, validation)
 * - Error handling by throwing exceptions for invalid data
 * - Conversion to/from a single line of text for file storage
 *
 * @author Martins Ngene
 * @version 2.0
 */
public final class Course {

    // ─── Validation rules ────────────────────────────────────────────────────
    /** Smallest number of credit units a course may carry. */
    public static final int MIN_UNITS = 1;
    /** Largest number of credit units a course may carry. */
    public static final int MAX_UNITS = 6;
    /** Longest course title accepted. */
    public static final int MAX_TITLE_LENGTH = 60;

    /** Course codes are letters followed by digits, e.g. COS201, MTH101, GST2011. */
    private static final String CODE_PATTERN = "[A-Z]{2,4}[0-9]{3,4}";

    /** Delimiter used when saving/loading courses to/from a file. */
    private static final String FILE_DELIMITER = ",";

    // ─── Fields ──────────────────────────────────────────────────────────────
    private String courseCode;   // e.g. "COS201"
    private String courseTitle;  // e.g. "Programming I"
    private int creditUnits;     // e.g. 3

    // ─── Constructor ─────────────────────────────────────────────────────────

    /**
     * Creates a new Course. Every value is checked by the matching setter.
     *
     * @throws IllegalArgumentException if any value is invalid
     */
    public Course(String courseCode, String courseTitle, int creditUnits) {
        setCourseCode(courseCode);
        setCourseTitle(courseTitle);
        setCreditUnits(creditUnits);
    }

    // ─── Getters ─────────────────────────────────────────────────────────────

    public String getCourseCode()  { return courseCode; }
    public String getCourseTitle() { return courseTitle; }
    public int getCreditUnits()    { return creditUnits; }

    // ─── Static helpers ──────────────────────────────────────────────────────

    /**
     * Normalises a course code typed by the user: trims it, removes inner
     * spaces and converts it to upper case. " cos 201 " becomes "COS201".
     */
    public static String normalizeCode(String rawCode) {
        if (rawCode == null) {
            return "";
        }
        return rawCode.trim().replaceAll("\\s+", "").toUpperCase();
    }

    /**
     * Normalises a course title: trims it and collapses repeated spaces.
     * "  Data   Structures " becomes "Data Structures".
     */
    public static String normalizeTitle(String rawTitle) {
        if (rawTitle == null) {
            return "";
        }
        return rawTitle.trim().replaceAll("\\s+", " ");
    }

    /**
     * Parses credit units typed by the user.
     *
     * @throws NumberFormatException if the text is not a whole number
     */
    public static int parseUnits(String rawUnits) {
        if (rawUnits == null || rawUnits.trim().isEmpty()) {
            throw new NumberFormatException("Credit units cannot be empty.");
        }
        try {
            return Integer.parseInt(rawUnits.trim());
        } catch (NumberFormatException e) {
            throw new NumberFormatException(
                "Credit units must be a whole number, but got '" + rawUnits.trim() + "'.");
        }
    }

    // ─── Setters with validation ─────────────────────────────────────────────

    /**
     * Sets the course code after normalising and validating it.
     *
     * @throws IllegalArgumentException if the code is empty or badly formed
     */
    public void setCourseCode(String rawCode) {
        String code = normalizeCode(rawCode);
        if (code.isEmpty()) {
            throw new IllegalArgumentException("Course code cannot be empty.");
        }
        if (!code.matches(CODE_PATTERN)) {
            throw new IllegalArgumentException("Invalid course code '" + code
                + "'. Use 2-4 letters followed by 3-4 digits, e.g. COS201.");
        }
        this.courseCode = code;
    }

    /**
     * Sets the course title after normalising and validating it.
     *
     * @throws IllegalArgumentException if the title is empty or too long
     */
    public void setCourseTitle(String rawTitle) {
        String title = normalizeTitle(rawTitle);
        if (title.isEmpty()) {
            throw new IllegalArgumentException("Course title cannot be empty.");
        }
        if (title.length() > MAX_TITLE_LENGTH) {
            throw new IllegalArgumentException(
                "Course title is too long (maximum " + MAX_TITLE_LENGTH + " characters).");
        }
        this.courseTitle = title;
    }

    /**
     * Sets the credit units after checking the allowed range.
     *
     * @throws IllegalArgumentException if units are outside MIN_UNITS..MAX_UNITS
     */
    public void setCreditUnits(int creditUnits) {
        if (creditUnits < MIN_UNITS || creditUnits > MAX_UNITS) {
            throw new IllegalArgumentException("Credit units must be between "
                + MIN_UNITS + " and " + MAX_UNITS + ", but got " + creditUnits + ".");
        }
        this.creditUnits = creditUnits;
    }

    // ─── File I/O helpers ────────────────────────────────────────────────────

    /**
     * Converts this course to one line of text: code,title,units
     */
    public String toFileString() {
        return courseCode + FILE_DELIMITER + courseTitle + FILE_DELIMITER + creditUnits;
    }

    /**
     * Rebuilds a Course from a line written by {@link #toFileString()}.
     * The code is everything before the FIRST comma and the units are
     * everything after the LAST comma, so titles that contain commas
     * (e.g. "Peace, Conflict and Society") are read back correctly.
     *
     * @throws IllegalArgumentException if the line is malformed
     *         (NumberFormatException, a subclass, if the units are not a number)
     */
    public static Course fromFileString(String line) {
        if (line == null || line.trim().isEmpty()) {
            throw new IllegalArgumentException("Cannot read a course from an empty line.");
        }
        int first = line.indexOf(FILE_DELIMITER);
        int last = line.lastIndexOf(FILE_DELIMITER);
        if (first < 0 || first == last) {
            throw new IllegalArgumentException(
                "Expected 3 comma-separated fields (code,title,units): '" + line.trim() + "'");
        }
        String code = line.substring(0, first);
        String title = line.substring(first + 1, last);
        int units = parseUnits(line.substring(last + 1));
        return new Course(code, title, units);
    }

    // ─── toString ────────────────────────────────────────────────────────────

    @Override
    public String toString() {
        return String.format("%-8s %-40s %2d unit%s",
            courseCode, courseTitle, creditUnits, creditUnits == 1 ? "" : "s");
    }
}
