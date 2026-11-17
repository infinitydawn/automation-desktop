package checker;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

//Collects what the rules find. ERROR = the panel will not do what the correlation is meant to do.
//WARNING = unusual, worth a look, may be intentional. INFO = context for the reader, not a problem.
public class Report {

    public enum Severity { ERROR, WARNING, INFO }

    public static class Finding {
        public final String rule;
        public final Severity severity;
        public final String where;
        public final String message;

        Finding(String rule, Severity severity, String where, String message) {
            this.rule = rule;
            this.severity = severity;
            this.where = where;
            this.message = message;
        }
    }

    private final Map<String, Rule> rules = new LinkedHashMap<>();
    private final Map<String, List<String>> notes = new LinkedHashMap<>();
    private final List<Finding> findings = new ArrayList<>();
    private Rule current;

    void begin(Rule rule) {
        current = rule;
        rules.put(rule.id(), rule);
        notes.putIfAbsent(rule.id(), new ArrayList<>());
    }

    public void error(String where, String message) {
        add(Severity.ERROR, where, message);
    }

    public void warning(String where, String message) {
        add(Severity.WARNING, where, message);
    }

    public void info(String where, String message) {
        add(Severity.INFO, where, message);
    }

    //A summary line for the rule's section, e.g. "73 floor groups checked"
    public void note(String text) {
        notes.get(current.id()).add(text);
    }

    private void add(Severity severity, String where, String message) {
        findings.add(new Finding(current.id(), severity, where, message));
    }

    public List<Finding> findings() {
        return findings;
    }

    public int rulesRun() {
        return rules.size();
    }

    //Rules that ran, in report order
    public List<Rule> rules() {
        return new ArrayList<>(rules.values());
    }

    //Summary lines a rule wrote, e.g. "73 floor groups found"
    public List<String> notes(String rule_id) {
        return notes.getOrDefault(rule_id, List.of());
    }

    //Ids of rules that do not support this job's panel, so did not run
    public final List<String> skipped = new ArrayList<>();

    public int count(String rule_id, Severity severity) {
        int n = 0;
        for (Finding f : findings) if (f.severity == severity && f.rule.equals(rule_id)) n++;
        return n;
    }

    public int count(Severity severity) {
        int n = 0;
        for (Finding f : findings) if (f.severity == severity) n++;
        return n;
    }

    public void print(PrintStream out, boolean show_info) {
        for (Rule rule : rules.values()) {
            List<Finding> mine = new ArrayList<>();
            int errors = 0, warnings = 0;
            for (Finding f : findings) {
                if (!f.rule.equals(rule.id())) continue;
                if (f.severity == Severity.ERROR) errors++;
                if (f.severity == Severity.WARNING) warnings++;
                if (f.severity != Severity.INFO || show_info) mine.add(f);
            }
            String status = errors + warnings == 0 ? "OK" : plural(errors, "error") + ", " + plural(warnings, "warning");
            out.println();
            out.println("[" + rule.id() + "] " + rule.title() + " - " + status);
            for (String n : notes.get(rule.id())) out.println("    " + n);
            mine.sort((a, b) -> a.severity.compareTo(b.severity));
            for (Finding f : mine) {
                out.println(String.format("  %-7s %s", f.severity, f.where));
                out.println("          " + f.message);
            }
        }
        out.println();
        out.println("Total: " + plural(count(Severity.ERROR), "error") + ", " + plural(count(Severity.WARNING), "warning"));
    }

    public void writeCsv(File file) throws IOException {
        try (PrintWriter w = new PrintWriter(file, StandardCharsets.UTF_8)) {
            w.print('﻿'); //so Excel opens it as UTF-8
            w.println("Rule,Severity,Where,Message");
            for (Finding f : findings) w.println(csv(f.rule) + "," + f.severity + "," + csv(f.where) + "," + csv(f.message));
        }
    }

    private static String csv(String s) {
        String one_line = s.replaceAll(":\\s*\\n\\s*", ": ").replaceAll("\\s*\\n\\s*", "; ");
        return "\"" + one_line.replace("\"", "\"\"") + "\"";
    }

    static String plural(int n, String word) {
        return n + " " + word + (n == 1 ? "" : "s");
    }
}
