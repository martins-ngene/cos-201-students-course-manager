import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;

/**
 * CourseManagerTest.java
 *
 * Plain-Java test runner (no external libraries) that checks every
 * operation of Course and CourseManager, including the error cases.
 *
 * Run with:  ./compile_and_run.sh test
 *
 * @author Martins Ngene
 */
public class CourseManagerTest {

    private static int passed = 0;
    private static int failed = 0;

    public static void main(String[] args) throws Exception {
        testCourseValidation();
        testStringProcessing();
        testAddAndDuplicates();
        testRecursiveSearch();
        testRecursiveTotal();
        testDelete();
        testSaveAndLoad();
        testLoadSkipsBadLines();
        testLoadMissingFile();

        System.out.println();
        System.out.println(passed + " passed, " + failed + " failed");
        if (failed > 0) {
            System.exit(1);
        }
    }

    // Tests

    static void testCourseValidation() {
        expectThrows("empty code rejected", () -> new Course("  ", "Computer Programming I", 3));
        expectThrows("badly formed code rejected", () -> new Course("201COS", "Computer Programming I", 3));
        expectThrows("empty title rejected", () -> new Course("COS201", "   ", 3));
        expectThrows("zero units rejected", () -> new Course("COS201", "Computer Programming I", 0));
        expectThrows("7 units rejected", () -> new Course("COS201", "Computer Programming I", 7));
        expectThrows("text units rejected", () -> Course.parseUnits("three"));
        expectThrows("decimal units rejected", () -> Course.parseUnits("2.5"));
    }

    static void testStringProcessing() {
        Course c = new Course("  cos 201 ", "  Computer   Programming    I  ", 3);
        check("code trimmed, spaces removed, upper-cased", c.getCourseCode().equals("COS201"));
        check("title trimmed and spaces collapsed", c.getCourseTitle().equals("Computer Programming I"));
        check("units parsed with spaces", Course.parseUnits(" 4 ") == 4);
    }

    static void testAddAndDuplicates() {
        CourseManager m = new CourseManager();
        m.addCourse(new Course("COS201", "Computer Programming I", 3));
        check("course added", m.getCourseCount() == 1);
        expectThrows("duplicate code rejected (case-insensitive)",
            () -> m.addCourse(new Course("cos201", "Another", 2)));
        check("duplicate not stored", m.getCourseCount() == 1);
    }

    static void testRecursiveSearch() {
        CourseManager m = sample();
        check("search finds first", m.searchCourseByCode("COS201") != null);
        check("search finds last", m.searchCourseByCode("COS205").getCourseTitle().equals("Computer Architecture"));
        check("search is case/space-insensitive", m.searchCourseByCode(" mth 201 ") != null);
        check("search returns null when missing", m.searchCourseByCode("BIO101") == null);
        check("search on empty list returns null", new CourseManager().searchCourseByCode("COS201") == null);
        check("search with blank input returns null", m.searchCourseByCode("   ") == null);
    }

    static void testRecursiveTotal() {
        check("total of empty list is 0", new CourseManager().computeTotalUnits() == 0);
        check("total of sample is 14", sample().computeTotalUnits() == 14);
        CourseManager big = new CourseManager();
        for (int i = 100; i < 600; i++) {
            big.addCourse(new Course("TST" + i, "Test " + i, 2));
        }
        check("total of 500 courses is 1000 (deep recursion ok)", big.computeTotalUnits() == 1000);
    }

    static void testDelete() {
        CourseManager m = sample();
        check("delete existing returns true", m.removeCourse("gst201"));
        check("course removed", m.getCourseCount() == 4 && m.computeTotalUnits() == 12);
        check("delete missing returns false", !m.removeCourse("GST201"));
    }

    static void testSaveAndLoad() throws IOException {
        File file = File.createTempFile("courses", ".txt");
        file.deleteOnExit();
        CourseManager m = sample();
        m.addCourse(new Course("GST203", "Peace, Conflict and Society", 2));
        m.saveCourses(file.getPath());

        CourseManager loaded = new CourseManager();
        CourseManager.LoadResult r = loaded.loadCourses(file.getPath());
        check("all 6 courses reloaded", r.loaded == 6 && r.skipped() == 0);
        check("title containing a comma survives save/load",
            loaded.searchCourseByCode("GST203").getCourseTitle().equals("Peace, Conflict and Society"));
        check("total units preserved", loaded.computeTotalUnits() == 16);
    }

    static void testLoadSkipsBadLines() throws IOException {
        File file = File.createTempFile("bad", ".txt");
        file.deleteOnExit();
        try (FileWriter w = new FileWriter(file)) {
            w.write("COS201,Computer Programming I,3\n");
            w.write("\n");
            w.write("# a comment line\n");
            w.write("COS203,Discrete Mathematics,three\n");   // bad units
            w.write("just some text\n");                       // missing fields
            w.write("COS201,Computer Programming I,3\n");               // duplicate
            w.write("MTH201,Mathematical Methods I,3\n");
        }
        CourseManager m = new CourseManager();
        CourseManager.LoadResult r = m.loadCourses(file.getPath());
        check("good lines loaded (2)", r.loaded == 2);
        check("bad lines reported (3)", r.skipped() == 3);
    }

    static void testLoadMissingFile() {
        CourseManager m = sample();
        boolean threw = false;
        try {
            m.loadCourses("no/such/folder/courses.txt");
        } catch (FileNotFoundException e) {
            threw = true;
        } catch (IOException e) {
            threw = false;
        }
        check("missing file throws FileNotFoundException", threw);
        check("courses in memory kept after failed load", m.getCourseCount() == 5);
    }

    // Helpers

    static CourseManager sample() {
        CourseManager m = new CourseManager();
        m.addCourse(new Course("COS201", "Computer Programming I", 3));
        m.addCourse(new Course("COS203", "Discrete Mathematics", 3));
        m.addCourse(new Course("MTH201", "Mathematical Methods I", 3));
        m.addCourse(new Course("GST201", "Nigerian Peoples and Culture", 2));
        m.addCourse(new Course("COS205", "Computer Architecture", 3));
        return m;
    }

    static void check(String name, boolean condition) {
        if (condition) {
            passed++;
            System.out.println("  PASS  " + name);
        } else {
            failed++;
            System.out.println("  FAIL  " + name);
        }
    }

    static void expectThrows(String name, Runnable action) {
        try {
            action.run();
            check(name, false);
        } catch (IllegalArgumentException e) {
            check(name, true);
        }
    }
}
