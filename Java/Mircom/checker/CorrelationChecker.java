package checker;

import java.awt.FileDialog;
import java.awt.Frame;
import java.awt.GraphicsEnvironment;
import java.io.File;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

//Reads a configurator job (.mdb) and reports correlation mistakes. Read-only: the job is never changed.
//
//  checker.bat [job.mdb] [--job N] [--version V] [--rules FSAE-DISPLAY,LINKS] [--csv report.csv] [--info] [--list]
//
//With no file, a file browser opens. An exported job holds one version and needs no --job. The configurator's own
//configurator.mdb holds every job: give --job (newest version is used unless --version is given).
public class CorrelationChecker {

    //Every rule, in report order. New checks go here.
    public static final List<Rule> RULES = List.of(
        new LinkRules.Integrity(),
        new LinkRules.Coverage(),
        new CheatSheetRules.DeviceSetup(),
        new CheatSheetRules.DeviceZones(),
        new CheatSheetRules.Outputs(),
        new CheatSheetRules.Bypass(),
        new FsaeRules.Zones(),
        new FsaeRules.Devices(),
        new FsaeRules.Logic(),
        new FsaeRules.Display());

    public static void main(String[] args) throws Exception {
        PrintStream out = new PrintStream(System.out, true, StandardCharsets.UTF_8);
        File file = null;
        Integer job_id = null, version = null;
        File csv = null;
        List<String> only = null;
        boolean info = false, list = false;

        for (int i = 0; i < args.length; i++) {
            String a = args[i];
            switch (a.toLowerCase(Locale.ROOT)) {
                case "--job": job_id = Integer.parseInt(args[++i]); break;
                case "--version": version = Integer.parseInt(args[++i]); break;
                case "--csv": csv = new File(args[++i]); break;
                case "--rules": only = Arrays.asList(args[++i].toUpperCase(Locale.ROOT).split(",")); break;
                case "--info": info = true; break;
                case "--list": list = true; break;
                case "--help": case "-h": case "/?": usage(out); return;
                default:
                    if (a.startsWith("--")) {
                        out.println("Unknown option " + a);
                        usage(out);
                        System.exit(2);
                    }
                    file = new File(a);
            }
        }
        if (file == null) file = browse();
        if (file == null) {
            usage(out);
            System.exit(2);
        }
        if (!file.isFile()) {
            out.println("File not found: " + file);
            System.exit(2);
        }

        if (list) {
            for (String v : Job.listVersions(file)) out.println(v);
            return;
        }

        Job job;
        try {
            job = Job.load(file, job_id, version);
        } catch (Exception e) {
            out.println("Could not read " + file.getName() + ": " + e.getMessage());
            out.println("If the configurator has this database open, export the job and check the exported .mdb instead.");
            System.exit(2);
            return;
        }

        out.println("Correlation check: " + file.getName());
        out.println("Job " + job.job_id + "-" + String.format("%02d", job.version) + ": " + job.description
            + (job.version_description.isEmpty() ? "" : " (" + job.version_description + ")") + " - " + job.panel.label);

        Report report = run(job, only);
        if (report.rulesRun() == 0) {
            out.println("Nothing was checked: there are no " + (only == null ? "" : "matching ") + "checks for " + job.panel.label + " yet.");
            System.exit(2);
        }
        report.print(out, info);
        if (csv != null) {
            report.writeCsv(csv);
            out.println("Findings saved to " + csv.getAbsolutePath());
        }
        System.exit(report.count(Report.Severity.ERROR) > 0 ? 1 : 0);
    }

    //Runs the rules that support the job's panel (all of them, or only the ids listed)
    public static Report run(Job job, List<String> only) {
        Report report = new Report();
        List<String> skipped = new ArrayList<>();
        for (Rule rule : RULES) {
            if (only != null && only.stream().noneMatch(id -> rule.id().startsWith(id))) continue;
            if (!rule.supports(job.panel)) {
                skipped.add(rule.id());
                continue;
            }
            report.begin(rule);
            rule.check(job, report);
        }
        if (!skipped.isEmpty()) {
            System.out.println("Not checked on " + job.panel.label + " yet: " + String.join(", ", skipped));
        }
        return report;
    }

    private static File browse() {
        if (GraphicsEnvironment.isHeadless()) return null;
        Frame frame = new Frame();
        try {
            FileDialog dialog = new FileDialog(frame, "Pick a configurator job (.mdb) to check", FileDialog.LOAD);
            dialog.setFile("*.mdb");
            dialog.setVisible(true);
            return dialog.getFile() == null ? null : new File(dialog.getDirectory(), dialog.getFile());
        } finally {
            frame.dispose();
        }
    }

    private static void usage(PrintStream out) {
        out.println("Usage: checker.bat [job.mdb] [--job N] [--version V] [--rules ID,ID] [--csv report.csv] [--info] [--list]");
        out.println("  job.mdb     exported job, or the configurator's configurator.mdb (then --job is needed)");
        out.println("  --list      list the jobs and versions in the file");
        out.println("  --rules     only run these rules (prefix match, e.g. FSAE runs every FSAE rule)");
        out.println("  --csv       also save the findings to a CSV file");
        out.println("  --info      also show informational notes");
        out.println("Rules:");
        for (Rule r : RULES) out.println(String.format("  %-13s %s", r.id(), r.title()));
    }
}
