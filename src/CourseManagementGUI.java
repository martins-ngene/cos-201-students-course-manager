import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.GridLayout;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

/**
 * CourseManagementGUI.java
 *
 * Graphical (Swing) interface for the Student Course Management System.
 * Every one of the seven required operations is available from the
 * "Operations" menu (numbered 1-7) and from buttons on the window.
 *
 * This class demonstrates:
 * - GUI development with Java Swing and event handling (ActionListeners)
 * - Decision-making on which operation the user selected
 * - Handling invalid input with exceptions and friendly dialog messages
 * - A clean layout built from small helper methods
 *
 * Run with:  java -cp bin CourseManagementGUI            (window)
 *            java -cp bin CourseManagementGUI --console  (text menu)
 *
 * @author Martins Ngene
 * @version 2.0
 */
public class CourseManagementGUI extends JFrame {

    private static final long serialVersionUID = 1L;

    // ─── Colours ─────────────────────────────────────────────────────────────
    private static final Color PRIMARY    = new Color(30, 64, 175);
    private static final Color SECONDARY  = new Color(2, 132, 199);
    private static final Color SUCCESS    = new Color(5, 150, 105);
    private static final Color DANGER     = new Color(220, 38, 38);
    private static final Color NEUTRAL    = new Color(71, 85, 105);
    private static final Color BACKGROUND = new Color(241, 245, 249);
    private static final Color CARD       = Color.WHITE;
    private static final Color TEXT       = new Color(15, 23, 42);
    private static final Color MUTED      = new Color(100, 116, 139);
    private static final Color BORDER     = new Color(203, 213, 225);
    private static final Color ROW_ALT    = new Color(248, 250, 252);
    private static final Color SELECTED   = new Color(219, 234, 254);

    // ─── Fonts ───────────────────────────────────────────────────────────────
    private static final Font FONT_TITLE  = new Font(Font.SANS_SERIF, Font.BOLD, 21);
    private static final Font FONT_SUB    = new Font(Font.SANS_SERIF, Font.PLAIN, 13);
    private static final Font FONT_HEAD   = new Font(Font.SANS_SERIF, Font.BOLD, 14);
    private static final Font FONT_LABEL  = new Font(Font.SANS_SERIF, Font.BOLD, 12);
    private static final Font FONT_BODY   = new Font(Font.SANS_SERIF, Font.PLAIN, 13);
    private static final Font FONT_BUTTON = new Font(Font.SANS_SERIF, Font.BOLD, 12);

    // ─── State and components ────────────────────────────────────────────────
    private final transient CourseManager manager = new CourseManager();
    private boolean unsavedChanges = false;

    private DefaultTableModel tableModel;
    private JTable table;
    private HintTextField codeField;
    private HintTextField titleField;
    private HintTextField unitsField;
    private HintTextField searchField;
    private JLabel countLabel;
    private JLabel unitsLabel;
    private JLabel statusLabel;

    // ─── Constructor ─────────────────────────────────────────────────────────

    public CourseManagementGUI() {
        super("Student Course Management System - COS 201");
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                exitProgram();
            }
        });

        setJMenuBar(createMenuBar());
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(BACKGROUND);
        root.add(createHeader(), BorderLayout.NORTH);
        root.add(createBody(), BorderLayout.CENTER);
        root.add(createStatusBar(), BorderLayout.SOUTH);
        setContentPane(root);

        setSize(1040, 680);
        setMinimumSize(new Dimension(920, 600));
        setLocationRelativeTo(null);
        refreshTable();
        setStatus("Ready. Add a course or load your saved list (Operations > 6).");
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  MENU BAR — numbered operations 1-7
    // ═════════════════════════════════════════════════════════════════════════

    private JMenuBar createMenuBar() {
        int shortcut = Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx();
        JMenuBar bar = new JMenuBar();

        JMenu ops = new JMenu("Operations");
        ops.setMnemonic(KeyEvent.VK_O);
        ops.add(menuItem("1. Add Course", KeyEvent.VK_1, shortcut, e -> addCourse()));
        ops.add(menuItem("2. View All Courses", KeyEvent.VK_2, shortcut, e -> viewAllCourses()));
        ops.add(menuItem("3. Search Course by Code", KeyEvent.VK_3, shortcut, e -> focusSearch()));
        ops.add(menuItem("4. Compute Total Units", KeyEvent.VK_4, shortcut, e -> computeTotalUnits()));
        ops.add(menuItem("5. Save to File", KeyEvent.VK_S, shortcut, e -> saveToFile()));
        ops.add(menuItem("6. Load from File", KeyEvent.VK_L, shortcut, e -> loadFromFile()));
        ops.addSeparator();
        ops.add(menuItem("7. Exit Program", KeyEvent.VK_Q, shortcut, e -> exitProgram()));
        bar.add(ops);

        JMenu help = new JMenu("Help");
        help.add(menuItem("About", 0, 0, e -> JOptionPane.showMessageDialog(this,
            "Student Course Management System\nCOS 201 - Programming I, MIVA Open University\n"
                + "Author: Martins Ngene\n\nCourses are saved to " + CourseManager.DEFAULT_FILE_PATH,
            "About", JOptionPane.INFORMATION_MESSAGE)));
        bar.add(help);
        return bar;
    }

    private JMenuItem menuItem(String text, int key, int modifiers, ActionListener action) {
        JMenuItem item = new JMenuItem(text);
        if (key != 0) {
            item.setAccelerator(KeyStroke.getKeyStroke(key, modifiers));
        }
        item.addActionListener(action);
        return item;
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  LAYOUT
    // ═════════════════════════════════════════════════════════════════════════

    private JPanel createHeader() {
        JPanel header = new JPanel();
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.setBackground(PRIMARY);
        header.setBorder(new EmptyBorder(16, 24, 16, 24));

        JLabel title = new JLabel("Student Course Management System");
        title.setFont(FONT_TITLE);
        title.setForeground(Color.WHITE);

        JLabel subtitle = new JLabel("COS 201 - Programming I  |  Record, search and total your semester courses");
        subtitle.setFont(FONT_SUB);
        subtitle.setForeground(new Color(191, 219, 254));

        header.add(title);
        header.add(Box.createVerticalStrut(4));
        header.add(subtitle);
        return header;
    }

    private JPanel createBody() {
        JPanel body = new JPanel(new BorderLayout(16, 0));
        body.setBackground(BACKGROUND);
        body.setBorder(new EmptyBorder(16, 16, 12, 16));

        JPanel left = new JPanel();
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
        left.setOpaque(false);
        left.setPreferredSize(new Dimension(300, 10));
        left.add(createAddCard());
        left.add(Box.createVerticalStrut(14));
        left.add(createSearchCard());
        left.add(Box.createVerticalGlue());

        body.add(left, BorderLayout.WEST);
        body.add(createTableCard(), BorderLayout.CENTER);
        return body;
    }

    /** Card with the "Add New Course" form (operation 1). */
    private JPanel createAddCard() {
        codeField = new HintTextField("e.g. COS201");
        titleField = new HintTextField("e.g. Programming I");
        unitsField = new HintTextField(Course.MIN_UNITS + " to " + Course.MAX_UNITS);
        unitsField.addActionListener(e -> addCourse());   // Enter key adds the course

        JPanel form = new JPanel(new GridLayout(0, 1, 0, 4));
        form.setOpaque(false);
        form.add(fieldLabel("Course Code"));
        form.add(codeField);
        form.add(fieldLabel("Course Title"));
        form.add(titleField);
        form.add(fieldLabel("Credit Units"));
        form.add(unitsField);

        JButton add = button("1. Add Course", SUCCESS, e -> addCourse());
        JPanel buttonRow = new JPanel(new GridLayout(1, 1));
        buttonRow.setOpaque(false);
        buttonRow.setBorder(new EmptyBorder(10, 0, 0, 0));
        buttonRow.add(add);

        JPanel content = new JPanel(new BorderLayout());
        content.setOpaque(false);
        content.add(form, BorderLayout.CENTER);
        content.add(buttonRow, BorderLayout.SOUTH);
        return card("Add New Course", content);
    }

    /** Card with the search box (operation 3). */
    private JPanel createSearchCard() {
        searchField = new HintTextField("Course code, e.g. cos201");
        searchField.addActionListener(e -> searchCourse());

        JPanel content = new JPanel(new GridLayout(0, 1, 0, 8));
        content.setOpaque(false);
        content.add(searchField);
        content.add(button("3. Search by Code", PRIMARY, e -> searchCourse()));
        return card("Search Course", content);
    }

    /** Card with the course table, toolbar and totals (operations 2, 4, 5, 6). */
    private JPanel createTableCard() {
        tableModel = new DefaultTableModel(new String[]{"#", "Course Code", "Course Title", "Units"}, 0) {
            private static final long serialVersionUID = 1L;
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        table = new JTable(tableModel);
        table.setFont(FONT_BODY);
        table.setRowHeight(30);
        table.setShowVerticalLines(false);
        table.setGridColor(new Color(226, 232, 240));
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setSelectionBackground(SELECTED);
        table.setSelectionForeground(TEXT);
        table.setFillsViewportHeight(true);
        table.setDefaultRenderer(Object.class, new StripedRenderer());

        JTableHeader header = table.getTableHeader();
        header.setFont(FONT_LABEL);
        header.setReorderingAllowed(false);
        header.setPreferredSize(new Dimension(10, 30));

        table.getColumnModel().getColumn(0).setMaxWidth(44);
        table.getColumnModel().getColumn(1).setPreferredWidth(110);
        table.getColumnModel().getColumn(1).setMaxWidth(140);
        table.getColumnModel().getColumn(2).setPreferredWidth(340);
        table.getColumnModel().getColumn(3).setMaxWidth(70);

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createLineBorder(BORDER));
        scroll.getViewport().setBackground(CARD);

        JPanel toolbar = new JPanel(new GridLayout(1, 0, 8, 0));
        toolbar.setOpaque(false);
        toolbar.setBorder(new EmptyBorder(0, 0, 10, 0));
        toolbar.add(button("2. View All", NEUTRAL, e -> viewAllCourses()));
        toolbar.add(button("4. Total Units", SUCCESS, e -> computeTotalUnits()));
        toolbar.add(button("5. Save", SECONDARY, e -> saveToFile()));
        toolbar.add(button("6. Load", SECONDARY, e -> loadFromFile()));
        toolbar.add(button("Delete Selected", DANGER, e -> deleteSelected()));

        countLabel = new JLabel();
        countLabel.setFont(FONT_LABEL);
        countLabel.setForeground(PRIMARY);
        unitsLabel = new JLabel();
        unitsLabel.setFont(FONT_LABEL);
        unitsLabel.setForeground(SUCCESS);
        JPanel stats = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        stats.setOpaque(false);
        stats.setBorder(new EmptyBorder(10, 2, 0, 0));
        stats.add(countLabel);
        stats.add(Box.createHorizontalStrut(24));
        stats.add(unitsLabel);

        JPanel content = new JPanel(new BorderLayout());
        content.setOpaque(false);
        content.add(toolbar, BorderLayout.NORTH);
        content.add(scroll, BorderLayout.CENTER);
        content.add(stats, BorderLayout.SOUTH);
        return card("Registered Courses", content);
    }

    private JPanel createStatusBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(CARD);
        bar.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(1, 0, 0, 0, BORDER), new EmptyBorder(6, 16, 6, 16)));
        statusLabel = new JLabel(" ");
        statusLabel.setFont(FONT_BODY);
        statusLabel.setForeground(NEUTRAL);
        JLabel file = new JLabel("Data file: " + CourseManager.DEFAULT_FILE_PATH);
        file.setFont(FONT_BODY);
        file.setForeground(MUTED);
        bar.add(statusLabel, BorderLayout.WEST);
        bar.add(file, BorderLayout.EAST);
        return bar;
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  OPERATIONS (event handlers)
    // ═════════════════════════════════════════════════════════════════════════

    /** Operation 1: validate the form and add a course. */
    private void addCourse() {
        String code = codeField.getText();
        String title = titleField.getText();
        String units = unitsField.getText();

        if (code.trim().isEmpty() || title.trim().isEmpty() || units.trim().isEmpty()) {
            showWarning("Missing information", "Please fill in Course Code, Course Title and Credit Units.");
            setStatus("Add failed: all three fields are required.");
            (code.trim().isEmpty() ? codeField : title.trim().isEmpty() ? titleField : unitsField).requestFocusInWindow();
            return;
        }
        try {
            Course course = new Course(code, title, Course.parseUnits(units));  // may throw
            manager.addCourse(course);                                         // may throw (duplicate)
            unsavedChanges = true;
            refreshTable();
            selectRow(course.getCourseCode());
            codeField.setText("");
            titleField.setText("");
            unitsField.setText("");
            codeField.requestFocusInWindow();
            setStatus("Added " + course.getCourseCode() + " - " + course.getCourseTitle() + ".");
        } catch (NumberFormatException ex) {           // checked first: it is a subclass
            showError("Invalid credit units", ex.getMessage());
            setStatus("Add failed: " + ex.getMessage());
            unitsField.requestFocusInWindow();
        } catch (IllegalArgumentException ex) {
            showError("Cannot add course", ex.getMessage());
            setStatus("Add failed: " + ex.getMessage());
        }
    }

    /** Operation 2: show every course and clear any search highlight. */
    private void viewAllCourses() {
        refreshTable();
        table.clearSelection();
        setStatus(manager.isEmpty() ? "No courses recorded yet."
            : "Showing all " + manager.getCourseCount() + " course(s).");
    }

    private void focusSearch() {
        searchField.requestFocusInWindow();
        if (!searchField.getText().trim().isEmpty()) {
            searchCourse();
        } else {
            setStatus("Type a course code in the Search box and press Enter.");
        }
    }

    /** Operation 3: recursive search by course code. */
    private void searchCourse() {
        String query = searchField.getText();
        if (Course.normalizeCode(query).isEmpty()) {
            showWarning("Search", "Please type a course code to search for.");
            return;
        }
        Course found = manager.searchCourseByCode(query);   // recursive search
        if (found == null) {
            table.clearSelection();
            showWarning("Not found", "No course with code '" + Course.normalizeCode(query) + "' is recorded.");
            setStatus("Search: " + Course.normalizeCode(query) + " not found.");
        } else {
            selectRow(found.getCourseCode());
            setStatus("Search: found " + found.getCourseCode() + " - " + found.getCourseTitle()
                + " (" + found.getCreditUnits() + " units).");
        }
    }

    /** Operation 4: recursive total of credit units, with a breakdown. */
    private void computeTotalUnits() {
        int total = manager.computeTotalUnits();             // recursive sum
        StringBuilder breakdown = new StringBuilder();
        for (Course c : manager.getAllCourses()) {
            breakdown.append(String.format("%-8s %-30s %2d%n", c.getCourseCode(), c.getCourseTitle(), c.getCreditUnits()));
        }
        breakdown.append(String.format("%-39s %2d", "TOTAL CREDIT UNITS", total));
        Object message = "No courses recorded yet, so the total is 0 units.";
        if (!manager.isEmpty()) {
            javax.swing.JTextArea area = new javax.swing.JTextArea(breakdown.toString());
            area.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
            area.setEditable(false);
            area.setOpaque(false);
            message = area;
        }
        JOptionPane.showMessageDialog(this, message, "Total Units", JOptionPane.INFORMATION_MESSAGE);
        setStatus("Total credit units: " + total + ".");
    }

    /** Operation 5: write all courses to data/courses.txt. */
    private void saveToFile() {
        if (manager.isEmpty()) {
            showWarning("Nothing to save", "Add at least one course before saving.");
            return;
        }
        try {
            manager.saveCourses();
            unsavedChanges = false;
            setStatus("Saved " + manager.getCourseCount() + " course(s) to " + CourseManager.DEFAULT_FILE_PATH + ".");
        } catch (IOException ex) {
            showError("Save failed", "The courses could not be saved:\n" + ex.getMessage());
            setStatus("Save failed: " + ex.getMessage());
        }
    }

    /** Operation 6: read courses back from data/courses.txt. */
    private void loadFromFile() {
        if (unsavedChanges && !confirm("Load from file",
                "Loading replaces the courses you have not saved. Continue?")) {
            return;
        }
        try {
            CourseManager.LoadResult result = manager.loadCourses();
            unsavedChanges = false;
            refreshTable();
            setStatus("Loaded " + result.loaded + " course(s) from " + CourseManager.DEFAULT_FILE_PATH
                + (result.skipped() > 0 ? " (" + result.skipped() + " line(s) skipped)." : "."));
            if (result.skipped() > 0) {
                showWarning("Some lines were skipped", String.join("\n", result.warnings));
            }
        } catch (FileNotFoundException ex) {
            showWarning("No saved file", ex.getMessage() + ".\nSave your courses first.");
            setStatus("Load failed: no saved file yet.");
        } catch (IOException ex) {
            showError("Load failed", "The file could not be read:\n" + ex.getMessage());
            setStatus("Load failed: " + ex.getMessage());
        }
    }

    /** Extra: remove the selected course. */
    private void deleteSelected() {
        int row = table.getSelectedRow();
        if (row < 0) {
            showWarning("Delete", "Select a course in the table first.");
            return;
        }
        String code = tableModel.getValueAt(row, 1).toString();
        if (confirm("Delete course", "Delete " + code + " from your list?") && manager.removeCourse(code)) {
            unsavedChanges = true;
            refreshTable();
            setStatus("Deleted " + code + ".");
        }
    }

    /** Operation 7: exit, asking first if there are unsaved changes. */
    private void exitProgram() {
        if (!unsavedChanges || confirm("Exit", "You have unsaved changes. Exit without saving?")) {
            dispose();
            System.exit(0);
        }
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  HELPERS
    // ═════════════════════════════════════════════════════════════════════════

    /** Rebuilds the table rows from the manager (iteration) and updates totals. */
    private void refreshTable() {
        tableModel.setRowCount(0);
        int number = 1;
        for (Course c : manager.getAllCourses()) {
            tableModel.addRow(new Object[]{number++, c.getCourseCode(), c.getCourseTitle(), c.getCreditUnits()});
        }
        countLabel.setText("Courses: " + manager.getCourseCount());
        unitsLabel.setText("Total Units: " + manager.computeTotalUnits());
    }

    private void selectRow(String code) {
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            if (tableModel.getValueAt(i, 1).equals(code)) {
                table.setRowSelectionInterval(i, i);
                table.scrollRectToVisible(table.getCellRect(i, 0, true));
                return;
            }
        }
    }

    private void setStatus(String message) {
        statusLabel.setText(message);
    }

    private void showWarning(String title, String message) {
        JOptionPane.showMessageDialog(this, message, title, JOptionPane.WARNING_MESSAGE);
    }

    private void showError(String title, String message) {
        JOptionPane.showMessageDialog(this, message, title, JOptionPane.ERROR_MESSAGE);
    }

    private boolean confirm(String title, String message) {
        return JOptionPane.showConfirmDialog(this, message, title,
            JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION;
    }

    private JPanel card(String title, JComponent content) {
        JPanel card = new JPanel(new BorderLayout(0, 10));
        card.setBackground(CARD);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER), new EmptyBorder(14, 16, 14, 16)));
        JLabel heading = new JLabel(title);
        heading.setFont(FONT_HEAD);
        heading.setForeground(TEXT);
        card.add(heading, BorderLayout.NORTH);
        card.add(content, BorderLayout.CENTER);
        card.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, card.getPreferredSize().height));
        return card;
    }

    private JLabel fieldLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(FONT_LABEL);
        label.setForeground(NEUTRAL);
        return label;
    }

    /** A flat, rounded, coloured button. */
    private JButton button(String text, Color colour, ActionListener action) {
        JButton b = new JButton(text) {
            private static final long serialVersionUID = 1L;
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isPressed() ? colour.darker()
                    : getModel().isRollover() ? colour.brighter() : colour);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        b.setFont(FONT_BUTTON);
        b.setForeground(Color.WHITE);
        b.setContentAreaFilled(false);
        b.setBorderPainted(false);
        b.setFocusPainted(false);
        b.setOpaque(false);
        b.setBorder(new EmptyBorder(9, 10, 9, 10));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.addActionListener(action);
        return b;
    }

    /** Text field that shows a grey hint while it is empty (the hint is never part of the value). */
    private static final class HintTextField extends JTextField {
        private static final long serialVersionUID = 1L;
        private final String hint;

        HintTextField(String hint) {
            this.hint = hint;
            setFont(FONT_BODY);
            setForeground(TEXT);
            setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER), new EmptyBorder(6, 8, 6, 8)));
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (getText().isEmpty()) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                g2.setColor(MUTED);
                g2.setFont(getFont().deriveFont(Font.ITALIC));
                int y = (getHeight() + g2.getFontMetrics().getAscent() - g2.getFontMetrics().getDescent()) / 2;
                g2.drawString(hint, getInsets().left, y);
                g2.dispose();
            }
        }
    }

    /** Zebra-striped rows; # and Units centred. */
    private static final class StripedRenderer extends DefaultTableCellRenderer {
        private static final long serialVersionUID = 1L;
        @Override
        public Component getTableCellRendererComponent(JTable t, Object value, boolean selected,
                                                       boolean focus, int row, int column) {
            super.getTableCellRendererComponent(t, value, selected, false, row, column);
            if (!selected) {
                setBackground(row % 2 == 0 ? CARD : ROW_ALT);
            }
            setForeground(TEXT);
            setHorizontalAlignment(column == 0 || column == 3 ? SwingConstants.CENTER : SwingConstants.LEFT);
            setBorder(new EmptyBorder(0, 10, 0, 10));
            return this;
        }
    }

    // ─── Entry point ─────────────────────────────────────────────────────────

    public static void main(String[] args) {
        boolean consoleRequested = args.length > 0 && args[0].trim().equalsIgnoreCase("--console");
        if (consoleRequested || GraphicsEnvironment.isHeadless()) {
            CourseManagementConsole.main(args);
            return;
        }
        try {
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        } catch (Exception e) {
            System.err.println("Using default look and feel: " + e.getMessage());
        }
        SwingUtilities.invokeLater(() -> new CourseManagementGUI().setVisible(true));
    }
}
