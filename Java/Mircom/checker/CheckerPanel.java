package checker;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FileDialog;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Window;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.prefs.Preferences;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.RowFilter;
import javax.swing.RowSorter;
import javax.swing.SortOrder;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableRowSorter;

import checker.Report.Finding;
import checker.Report.Severity;

//The correlation checker as a window panel: pick a job file (or the configurator's own database and a job in it),
//run every check, then browse the findings by check, severity or text. Read-only like the command line version.
//Kept as a JPanel so it can sit in its own window (CheckerWindow) or as a tab in the bot window.
@SuppressWarnings("serial")
public class CheckerPanel extends JPanel {

    static final Color ERROR_COLOR = new Color(190, 0, 0);
    static final Color WARNING_COLOR = new Color(170, 105, 0);
    static final Color OK_COLOR = new Color(0, 125, 50);
    static final Color MUTED_COLOR = new Color(110, 110, 110);
    static final Color SELECTED_ERROR = new Color(255, 225, 225);
    static final Color SELECTED_WARNING = new Color(255, 240, 210);

    //The configurators' own databases, offered as shortcuts when they exist on this PC
    static final File FX4000_DB = new File("C:\\ProgramData\\Mircom Group of Companies\\MGC-4000 Configurator V2\\Database\\configurator.mdb");
    static final File FX6000_DB = new File("C:\\ProgramData\\Mircom Group of Companies\\MGC-Config_v14\\Database\\configurator.mdb");

    private final Preferences prefs = Preferences.userNodeForPackage(CheckerPanel.class);

    private File file;
    private Job job;
    private Report report;

    private final JTextField file_field = new JTextField();
    private final JComboBox<Job.Version> version_box = new JComboBox<>();
    private final JButton check_button = new JButton("Check");
    private final JButton export_button = new JButton("Save CSV...");
    private final JLabel status = new JLabel("Pick a job file to check.");
    private final JProgressBar busy = new JProgressBar();

    private final JLabel job_label = new JLabel(" ");
    private final JLabel total_label = new JLabel(" ");

    private final RuleModel rule_model = new RuleModel();
    private final JTable rule_table = new JTable(rule_model);
    private final FindingModel finding_model = new FindingModel();
    private final JTable finding_table = new JTable(finding_model);
    private final TableRowSorter<FindingModel> sorter = new TableRowSorter<>(finding_model);
    private final JCheckBox show_errors = new JCheckBox("Errors", true);
    private final JCheckBox show_warnings = new JCheckBox("Warnings", true);
    private final JTextField search = new JTextField(18);
    private final JLabel shown_label = new JLabel(" ");
    private final JTextArea details = new JTextArea();

    public CheckerPanel() {
        super(new BorderLayout(0, 6));
        setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        add(sourcePanel(), BorderLayout.NORTH);
        add(resultsPanel(), BorderLayout.CENTER);
        setResultsEnabled(false);
    }

    //-------------------------------------------------------------------------------------------------
    //Layout

    private JPanel sourcePanel() {
        JPanel p = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(2, 2, 2, 4);
        c.anchor = GridBagConstraints.WEST;

        file_field.setEditable(false);
        JButton open = new JButton("Open job file...");
        open.addActionListener(e -> pickFile());
        JButton fx4000 = new JButton("FX4000 configurator");
        fx4000.setToolTipText("All jobs in the MGC-4000 Configurator V2 database on this PC");
        fx4000.addActionListener(e -> openFile(FX4000_DB));
        fx4000.setVisible(FX4000_DB.isFile());
        JButton fx6000 = new JButton("FX6000 configurator");
        fx6000.setToolTipText("All jobs in the MGC Configurator v14 (FX6000/FlexNet) database on this PC");
        fx6000.addActionListener(e -> openFile(FX6000_DB));
        fx6000.setVisible(FX6000_DB.isFile());

        c.gridx = 0; c.gridy = 0;
        p.add(new JLabel("Job file:"), c);
        c.gridx = 1; c.weightx = 1; c.fill = GridBagConstraints.HORIZONTAL;
        p.add(file_field, c);
        c.gridx = 2; c.weightx = 0; c.fill = GridBagConstraints.NONE;
        p.add(open, c);
        c.gridx = 3;
        p.add(fx4000, c);
        c.gridx = 4;
        p.add(fx6000, c);

        c.gridx = 0; c.gridy = 1;
        p.add(new JLabel("Job:"), c);
        c.gridx = 1; c.weightx = 1; c.fill = GridBagConstraints.HORIZONTAL;
        version_box.setMaximumRowCount(25);
        version_box.setEnabled(false);
        p.add(version_box, c);
        c.gridx = 2; c.weightx = 0; c.fill = GridBagConstraints.HORIZONTAL;
        check_button.setFont(check_button.getFont().deriveFont(Font.BOLD));
        check_button.setEnabled(false);
        check_button.addActionListener(e -> runCheck());
        p.add(check_button, c);

        busy.setIndeterminate(true);
        busy.setVisible(false);
        busy.setPreferredSize(new Dimension(120, busy.getPreferredSize().height));
        JPanel status_row = new JPanel(new BorderLayout(8, 0));
        status_row.add(status, BorderLayout.CENTER);
        status_row.add(busy, BorderLayout.EAST);
        c.gridx = 0; c.gridy = 2; c.gridwidth = 5; c.fill = GridBagConstraints.HORIZONTAL;
        p.add(status_row, c);

        //Picking another job clears the old results so they are never shown under the wrong job
        version_box.addActionListener(e -> {
            Job.Version v = (Job.Version) version_box.getSelectedItem();
            if (!version_box.isEnabled() || v == null) return;
            if (job != null && (job.job_id != v.job || job.version != v.version)) clearResults();
            if (job == null) status.setText("Press Check to check the selected job.");
        });
        return p;
    }

    private JPanel resultsPanel() {
        //Header: job name and totals
        job_label.setFont(job_label.getFont().deriveFont(Font.BOLD, job_label.getFont().getSize() + 3f));
        total_label.setFont(total_label.getFont().deriveFont(Font.BOLD, total_label.getFont().getSize() + 1f));
        JPanel header = new JPanel(new BorderLayout(12, 0));
        header.setBorder(BorderFactory.createEmptyBorder(6, 2, 4, 2));
        header.add(job_label, BorderLayout.CENTER);
        header.add(total_label, BorderLayout.EAST);

        //Left: checks and how each went
        rule_table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        rule_table.setRowHeight(rule_table.getRowHeight() + 6);
        rule_table.setDefaultRenderer(Object.class, new RuleRenderer());
        rule_table.getColumnModel().getColumn(0).setPreferredWidth(150);
        rule_table.getColumnModel().getColumn(1).setPreferredWidth(140);
        rule_table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                applyFilter();
                showDetails();
            }
        });
        JScrollPane rule_scroll = new JScrollPane(rule_table);
        rule_scroll.setBorder(BorderFactory.createTitledBorder("Checks"));
        rule_scroll.setPreferredSize(new Dimension(320, 300));

        //Right top: filters and findings
        JPanel filters = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        filters.add(new JLabel("Show:"));
        show_errors.setForeground(ERROR_COLOR);
        show_warnings.setForeground(WARNING_COLOR);
        show_errors.addActionListener(e -> applyFilter());
        show_warnings.addActionListener(e -> applyFilter());
        filters.add(show_errors);
        filters.add(show_warnings);
        filters.add(new JLabel("   Search:"));
        search.setToolTipText("Matches the location or the problem text, e.g. 40FL, IZ131, waterflow");
        search.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { applyFilter(); }
            public void removeUpdate(DocumentEvent e) { applyFilter(); }
            public void changedUpdate(DocumentEvent e) { applyFilter(); }
        });
        filters.add(search);
        filters.add(shown_label);
        shown_label.setForeground(MUTED_COLOR);

        finding_table.setRowSorter(sorter);
        finding_table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        finding_table.setRowHeight(finding_table.getRowHeight() + 6);
        finding_table.setDefaultRenderer(Object.class, new FindingRenderer());
        finding_table.getColumnModel().getColumn(0).setPreferredWidth(70);
        finding_table.getColumnModel().getColumn(0).setMaxWidth(90);
        finding_table.getColumnModel().getColumn(1).setPreferredWidth(110);
        finding_table.getColumnModel().getColumn(1).setMaxWidth(150);
        finding_table.getColumnModel().getColumn(2).setPreferredWidth(330);
        finding_table.getColumnModel().getColumn(3).setPreferredWidth(520);
        finding_table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) showDetails();
        });
        JPanel findings = new JPanel(new BorderLayout(0, 4));
        findings.add(filters, BorderLayout.NORTH);
        findings.add(new JScrollPane(finding_table), BorderLayout.CENTER);

        //Right bottom: the selected finding in full
        details.setEditable(false);
        details.setLineWrap(true);
        details.setWrapStyleWord(true);
        details.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 13));
        details.setBorder(BorderFactory.createEmptyBorder(6, 8, 6, 8));
        JScrollPane details_scroll = new JScrollPane(details);
        details_scroll.setBorder(BorderFactory.createTitledBorder("Details"));

        JSplitPane right = new JSplitPane(JSplitPane.VERTICAL_SPLIT, findings, details_scroll);
        right.setResizeWeight(0.7);
        right.setBorder(null);
        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, rule_scroll, right);
        split.setResizeWeight(0.22);
        split.setBorder(null);

        export_button.addActionListener(e -> exportCsv());
        JLabel hint = new JLabel("Read-only: the job is never changed. Click a check to see only its findings.");
        hint.setForeground(MUTED_COLOR);
        JPanel bottom = new JPanel(new BorderLayout());
        bottom.add(hint, BorderLayout.CENTER);
        bottom.add(export_button, BorderLayout.EAST);

        JPanel p = new JPanel(new BorderLayout());
        p.add(header, BorderLayout.NORTH);
        p.add(split, BorderLayout.CENTER);
        p.add(bottom, BorderLayout.SOUTH);
        return p;
    }

    private void clearResults() {
        job = null;
        report = null;
        rule_model.rows.clear();
        rule_model.fireTableDataChanged();
        finding_model.rows.clear();
        finding_model.fireTableDataChanged();
        job_label.setText(" ");
        total_label.setText(" ");
        shown_label.setText(" ");
        details.setText("");
        setResultsEnabled(false);
    }

    private void setResultsEnabled(boolean on) {
        export_button.setEnabled(on && report != null && !report.findings().isEmpty());
        show_errors.setEnabled(on);
        show_warnings.setEnabled(on);
        search.setEnabled(on);
    }

    //-------------------------------------------------------------------------------------------------
    //Picking a file and a job

    private void pickFile() {
        Window owner = SwingUtilities.getWindowAncestor(this);
        FileDialog dialog = new FileDialog(owner instanceof Frame ? (Frame) owner : null, "Pick a configurator job (.mdb)", FileDialog.LOAD);
        String last = prefs.get("lastDir", new File(System.getProperty("user.home"), "Downloads").getPath());
        dialog.setDirectory(last);
        dialog.setFile("*.mdb");
        dialog.setVisible(true);
        if (dialog.getFile() == null) return;
        File picked = new File(dialog.getDirectory(), dialog.getFile());
        prefs.put("lastDir", picked.getParent());
        openFile(picked);
    }

    //Lists the jobs in the file. An exported job (one version) is checked straight away.
    public void openFile(File f) {
        if (f == null || !f.isFile()) {
            status.setText("File not found: " + f);
            return;
        }
        file = f;
        file_field.setText(f.getPath());
        clearResults();
        version_box.removeAllItems();
        version_box.setEnabled(false);
        check_button.setEnabled(false);
        working("Reading the list of jobs in " + f.getName() + "...");
        new SwingWorker<List<Job.Version>, Void>() {
            protected List<Job.Version> doInBackground() throws Exception {
                return Job.versions(f);
            }

            protected void done() {
                idle();
                List<Job.Version> versions;
                try {
                    versions = get();
                } catch (Exception e) {
                    failed("Could not read " + f.getName(), e);
                    return;
                }
                if (versions.isEmpty()) {
                    status.setText(f.getName() + " has no jobs.");
                    return;
                }
                //Newest first, so the job just worked on is at the top
                List<Job.Version> newest_first = new ArrayList<>(versions);
                newest_first.sort((a, b) -> b.created.compareTo(a.created));
                for (Job.Version v : newest_first) version_box.addItem(v);
                version_box.setSelectedIndex(0);
                version_box.setEnabled(versions.size() > 1);
                check_button.setEnabled(true);
                if (versions.size() == 1) {
                    runCheck();
                } else {
                    status.setText(f.getName() + " holds " + versions.size() + " job versions (newest first). Pick one and press Check.");
                }
            }
        }.execute();
    }

    //-------------------------------------------------------------------------------------------------
    //Checking

    private void runCheck() {
        Job.Version v = (Job.Version) version_box.getSelectedItem();
        if (file == null || v == null) return;
        check_button.setEnabled(false);
        working("Checking job " + v.job + "-" + String.format("%02d", v.version) + "...");
        new SwingWorker<Object[], Void>() {
            protected Object[] doInBackground() throws Exception {
                Job j = Job.load(file, v.job, v.version);
                return new Object[] { j, CorrelationChecker.run(j, null) };
            }

            protected void done() {
                idle();
                check_button.setEnabled(true);
                try {
                    Object[] result = get();
                    show((Job) result[0], (Report) result[1]);
                } catch (Exception e) {
                    failed("Could not check the job. If the configurator is saving right now, try again, or export the job and open the export", e);
                }
            }
        }.execute();
    }

    private void show(Job j, Report r) {
        job = j;
        report = r;
        job_label.setText("Job " + j.job_id + "-" + String.format("%02d", j.version) + ":  " + j.description
            + (j.version_description.isBlank() ? "" : "  (" + j.version_description.trim() + ")") + "   -   " + j.panel.label);
        int errors = r.count(Severity.ERROR), warnings = r.count(Severity.WARNING);
        if (r.rulesRun() == 0) {
            total_label.setText("Not checked");
            total_label.setForeground(MUTED_COLOR);
        } else if (errors + warnings == 0) {
            total_label.setText("All checks passed");
            total_label.setForeground(OK_COLOR);
        } else {
            total_label.setText(Report.plural(errors, "error") + "   " + Report.plural(warnings, "warning"));
            total_label.setForeground(errors > 0 ? ERROR_COLOR : WARNING_COLOR);
        }
        rule_model.load(r);
        finding_model.load(r);
        sorter.setSortKeys(List.of(new RowSorter.SortKey(0, SortOrder.ASCENDING))); //errors first, then report order
        setResultsEnabled(true);
        rule_table.getSelectionModel().setSelectionInterval(0, 0);
        applyFilter();
        showDetails();
        if (r.rulesRun() == 0) {
            status.setText("Nothing was checked: there are no checks for " + j.panel.label + " yet.");
        } else {
            status.setText("Checked " + file.getName() + ". " + j.summary() + ".");
        }
    }

    //-------------------------------------------------------------------------------------------------
    //Filtering and details

    private Rule selectedRule() {
        int row = rule_table.getSelectedRow();
        return row < 0 ? null : rule_model.rows.get(row).rule;
    }

    private void applyFilter() {
        Rule rule = selectedRule();
        boolean errors = show_errors.isSelected(), warnings = show_warnings.isSelected();
        String text = search.getText().trim().toLowerCase(Locale.ROOT);
        sorter.setRowFilter(new RowFilter<FindingModel, Integer>() {
            public boolean include(Entry<? extends FindingModel, ? extends Integer> entry) {
                Finding f = finding_model.rows.get(entry.getIdentifier());
                if (rule != null && !f.rule.equals(rule.id())) return false;
                if (f.severity == Severity.ERROR && !errors) return false;
                if (f.severity == Severity.WARNING && !warnings) return false;
                if (f.severity == Severity.INFO) return false;
                return text.isEmpty() || (f.where + " " + f.message).toLowerCase(Locale.ROOT).contains(text);
            }
        });
        if (report != null) shown_label.setText("   " + finding_table.getRowCount() + " of " + (report.count(Severity.ERROR) + report.count(Severity.WARNING)) + " shown");
    }

    private void showDetails() {
        if (report == null) {
            details.setText("");
            return;
        }
        int row = finding_table.getSelectedRow();
        StringBuilder s = new StringBuilder();
        if (row >= 0) {
            Finding f = finding_model.rows.get(finding_table.convertRowIndexToModel(row));
            Rule rule = rule(f.rule);
            s.append(f.severity).append("  -  ").append(rule == null ? f.rule : rule.name() + " (" + f.rule + "): " + rule.title()).append("\n\n");
            s.append(f.where).append("\n\n");
            s.append(f.message.replaceAll("\\n\\s+", "\n    "));
        } else {
            Rule rule = selectedRule();
            List<Rule> rules = rule == null ? report.rules() : List.of(rule);
            if (rule == null) {
                s.append("Select a check on the left to see only its findings, or a finding to see it in full.\n\n");
            }
            for (Rule r : rules) {
                s.append(r.name()).append(" (").append(r.id()).append("): ").append(r.title()).append('\n');
                for (String n : report.notes(r.id())) s.append("    ").append(n).append('\n');
                s.append('\n');
            }
            if (rule == null && !report.skipped.isEmpty()) {
                s.append("Not available for ").append(job.panel.label).append(" yet: ").append(String.join(", ", report.skipped));
            }
        }
        details.setText(s.toString().trim());
        details.setCaretPosition(0);
    }

    private Rule rule(String id) {
        for (Rule r : CorrelationChecker.RULES) if (r.id().equals(id)) return r;
        return null;
    }

    private void exportCsv() {
        if (report == null) return;
        Window owner = SwingUtilities.getWindowAncestor(this);
        FileDialog dialog = new FileDialog(owner instanceof Frame ? (Frame) owner : null, "Save findings as CSV", FileDialog.SAVE);
        dialog.setDirectory(prefs.get("lastDir", System.getProperty("user.home")));
        dialog.setFile("Job " + job.job_id + "-" + String.format("%02d", job.version) + " correlation check.csv");
        dialog.setVisible(true);
        if (dialog.getFile() == null) return;
        String name = dialog.getFile().toLowerCase(Locale.ROOT).endsWith(".csv") ? dialog.getFile() : dialog.getFile() + ".csv";
        File out = new File(dialog.getDirectory(), name);
        try {
            report.writeCsv(out);
            status.setText("Findings saved to " + out.getPath());
        } catch (Exception e) {
            failed("Could not save " + out.getName(), e);
        }
    }

    private void working(String text) {
        status.setText(text);
        busy.setVisible(true);
    }

    private void idle() {
        busy.setVisible(false);
    }

    private void failed(String what, Exception e) {
        Throwable cause = e.getCause() != null ? e.getCause() : e;
        status.setText(what + ".");
        JOptionPane.showMessageDialog(this, what + ":\n" + cause.getMessage(), "Correlation checker", JOptionPane.ERROR_MESSAGE);
    }

    //-------------------------------------------------------------------------------------------------
    //Tables

    private static class RuleRow {
        final Rule rule; //null = every check
        final int errors, warnings;
        final boolean skipped;

        RuleRow(Rule rule, int errors, int warnings, boolean skipped) {
            this.rule = rule;
            this.errors = errors;
            this.warnings = warnings;
            this.skipped = skipped;
        }

        String result() {
            if (skipped) return "not for this panel";
            if (errors + warnings == 0) return "OK";
            List<String> parts = new ArrayList<>();
            if (errors > 0) parts.add(Report.plural(errors, "error"));
            if (warnings > 0) parts.add(Report.plural(warnings, "warning"));
            return String.join(", ", parts);
        }
    }

    private static class RuleModel extends AbstractTableModel {
        final List<RuleRow> rows = new ArrayList<>();

        void load(Report r) {
            rows.clear();
            rows.add(new RuleRow(null, r.count(Severity.ERROR), r.count(Severity.WARNING), false));
            for (Rule rule : r.rules()) rows.add(new RuleRow(rule, r.count(rule.id(), Severity.ERROR), r.count(rule.id(), Severity.WARNING), false));
            for (String id : r.skipped) {
                for (Rule rule : CorrelationChecker.RULES) if (rule.id().equals(id)) rows.add(new RuleRow(rule, 0, 0, true));
            }
            fireTableDataChanged();
        }

        public int getRowCount() { return rows.size(); }
        public int getColumnCount() { return 2; }
        public String getColumnName(int c) { return c == 0 ? "Check" : "Result"; }

        public Object getValueAt(int row, int col) {
            RuleRow r = rows.get(row);
            if (col == 0) return r.rule == null ? "All checks" : r.rule.name();
            return r.result();
        }
    }

    private class RuleRenderer extends DefaultTableCellRenderer {
        public Component getTableCellRendererComponent(JTable table, Object value, boolean selected, boolean focused, int row, int col) {
            super.getTableCellRendererComponent(table, value, selected, false, row, col);
            RuleRow r = rule_model.rows.get(row);
            setToolTipText(r.rule == null ? "Every check" : r.rule.id() + ": " + r.rule.title());
            setFont(getFont().deriveFont(r.rule == null ? Font.BOLD : Font.PLAIN));
            if (!selected) {
                if (col == 0) setForeground(r.skipped ? MUTED_COLOR : Color.BLACK);
                else setForeground(r.skipped ? MUTED_COLOR : r.errors > 0 ? ERROR_COLOR : r.warnings > 0 ? WARNING_COLOR : OK_COLOR);
            }
            return this;
        }
    }

    private static class FindingModel extends AbstractTableModel {
        final List<Finding> rows = new ArrayList<>();

        void load(Report r) {
            rows.clear();
            for (Finding f : r.findings()) if (f.severity != Severity.INFO) rows.add(f);
            fireTableDataChanged();
        }

        public int getRowCount() { return rows.size(); }
        public int getColumnCount() { return 4; }

        public String getColumnName(int c) {
            switch (c) {
                case 0: return "Severity";
                case 1: return "Check";
                case 2: return "Where";
                default: return "Problem";
            }
        }

        public Class<?> getColumnClass(int c) {
            return c == 0 ? Severity.class : String.class;
        }

        public Object getValueAt(int row, int col) {
            Finding f = rows.get(row);
            switch (col) {
                case 0: return f.severity;
                case 1: return ruleName(f.rule);
                case 2: return f.where;
                default:
                    //First line only; the details pane shows the rest (lists of devices)
                    String m = f.message;
                    int nl = m.indexOf('\n');
                    return nl < 0 ? m : m.substring(0, nl).trim() + "  (+ list)";
            }
        }
    }

    private class FindingRenderer extends DefaultTableCellRenderer {
        public Component getTableCellRendererComponent(JTable table, Object value, boolean selected, boolean focused, int row, int col) {
            String text = value instanceof Severity ? (value == Severity.ERROR ? "Error" : value == Severity.WARNING ? "Warning" : "Info") : String.valueOf(value);
            super.getTableCellRendererComponent(table, text, selected, false, row, col);
            Finding f = finding_model.rows.get(table.convertRowIndexToModel(row));
            setToolTipText(col == 3 ? "<html>" + escape(f.message).replace("\n", "<br>") + "</html>" : col == 2 ? f.where : null);
            if (!selected) {
                setBackground(table.getBackground());
                setForeground(col == 0 ? (f.severity == Severity.ERROR ? ERROR_COLOR : WARNING_COLOR) : Color.BLACK);
            } else {
                setBackground(f.severity == Severity.ERROR ? SELECTED_ERROR : SELECTED_WARNING);
                setForeground(col == 0 ? (f.severity == Severity.ERROR ? ERROR_COLOR : WARNING_COLOR) : Color.BLACK);
            }
            setFont(getFont().deriveFont(col == 0 ? Font.BOLD : Font.PLAIN));
            return this;
        }
    }

    private static String ruleName(String id) {
        for (Rule r : CorrelationChecker.RULES) if (r.id().equals(id)) return r.name();
        return id;
    }

    private static String escape(String s) {
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }
}
