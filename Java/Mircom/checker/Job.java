package checker;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.healthmarketscience.jackcess.Database;
import com.healthmarketscience.jackcess.DatabaseBuilder;
import com.healthmarketscience.jackcess.Row;
import com.healthmarketscience.jackcess.Table;

//One job version read from a configurator database: an exported job .mdb, or the configurator's own
//configurator.mdb (which holds every job, so a job number is needed). Read-only; nothing is ever written.
//Only the MGC-4000 (FX4000) layout is loaded for now. FX400/FX2000 keep correlations in one IOCorrs table
//keyed by loop/address and will get their own loader.
public class Job {

    public enum Panel {
        FX4000("FX4000 (MGC-4000 Configurator)"),
        FX400("FX400 (MGC-400 Series Configurator)"),
        FX2000("FX2000 (FX-2000 Configuration Utility)"),
        UNKNOWN("unknown panel");

        public final String label;

        Panel(String label) {
            this.label = label;
        }
    }

    //Loop types the MGC-4000 configurator uses for its virtual input/output zone lists (SYSLoopTypes)
    static final int LOOP_TYPE_INPUT_ZONE = 126;
    static final int LOOP_TYPE_OUTPUT_ZONE = 127;

    public final File file;
    public Panel panel = Panel.UNKNOWN;
    public int job_id;
    public int version;
    public String description = "";
    public String version_description = "";

    public final Map<Integer, Circuit> inputs = new TreeMap<>(); //by CktNo
    public final Map<Integer, Circuit> outputs = new TreeMap<>(); //by CktNo
    public final Map<Integer, Led> leds = new TreeMap<>(); //by LedGrp
    public final Map<Integer, Equation> equations = new TreeMap<>(); //by EquationNo

    //Raw link rows, kept as-is so integrity checks can see dangling or duplicate rows
    public final List<int[]> zone_links = new ArrayList<>(); //IptZoneCorrs: {input ckt, input zone ckt}
    public final List<int[]> io_links = new ArrayList<>(); //IOCorrs: {input ckt, output ckt}
    public final List<int[]> led_input_links = new ArrayList<>(); //LedIpts: {led group, input ckt}
    public final List<int[]> led_output_links = new ArrayList<>(); //LedOpts: {led group, output ckt}

    public final Map<String, String> frame_types = new HashMap<>(); //"node/cpu/frame" -> frame type name

    //Lookups built from the link rows once loading is done
    private final Map<Integer, List<Circuit>> zones_of = new HashMap<>(); //device input ckt -> its input zones
    private final Map<Integer, List<Circuit>> sources_of = new HashMap<>(); //input zone ckt -> device inputs feeding it
    private final Map<Integer, List<Led>> leds_of_input = new HashMap<>();
    private final Map<Integer, List<Equation>> equations_of_subject = new HashMap<>();

    //Rules can park shared work here (e.g. the FSAE floor groups) so it is only worked out once
    public final Map<String, Object> cache = new HashMap<>();

    private Job(File file) {
        this.file = file;
    }

    //-------------------------------------------------------------------------------------------------
    //Model

    public enum Kind { DEVICE, INPUT_ZONE, OUTPUT_ZONE, PANEL }

    public static class Circuit {
        public int ckt;
        public boolean output;
        public Kind kind = Kind.DEVICE;
        public int node, cpu, loop, addr, sub;
        public String loop_type = ""; //SYSLoopTypes name, e.g. S200, Telephone, IptZone
        public String device_type = "";
        public int process_type;
        public String process_name = "";
        public String tag = "";

        //Short, unambiguous name for reports, e.g. N1 IZ61 "Low Heat 13FL SouthT" or N1 L1-109.2 HeatPhoto (2351TAP) "..."
        public String label() {
            String t = tag.trim().replaceAll("\\s{2,}", " ");
            switch (kind) {
                case INPUT_ZONE: return "N" + node + " IZ" + addr + " \"" + t + "\"";
                case OUTPUT_ZONE: return "N" + node + " OZ" + addr + " \"" + t + "\"";
                default:
                    return "N" + node + " C" + cpu + " L" + loop + "-" + addr + "." + sub + " " + device_type
                        + (t.isEmpty() ? "" : " \"" + t + "\"");
            }
        }

        public String deviceKey() {
            return node + "/" + cpu + "/" + loop + "/" + addr;
        }
    }

    public static class Led {
        public int group;
        public int led_type;
        public String led_type_name = "";
        public String tag = "";
        public Map<String, Integer> states = new LinkedHashMap<>(); //AllActive, SomeActive, ... as stored
        public final List<int[]> slots = new ArrayList<>(); //{node, cpu, frame, led}
        public final List<Circuit> inputs = new ArrayList<>();
        public final List<Circuit> outputs = new ArrayList<>();

        public String label() {
            StringBuilder s = new StringBuilder();
            for (int[] p : slots) {
                if (s.length() > 0) s.append(", ");
                s.append("N").append(p[0]).append(" C").append(p[1]).append(" F").append(p[2]).append(" LED").append(p[3]);
            }
            if (s.length() == 0) s.append("LED group ").append(group).append(" (no position)");
            return s + " \"" + tag.trim() + "\"";
        }
    }

    public static class Equation {
        public int number;
        public int node;
        public String tag = "";
        public String text = "";
        public String comments = "";
        public final List<Circuit> subjects = new ArrayList<>(); //input zones this equation drives
        public final TreeMap<Integer, Integer> inputs = new TreeMap<>(); //sequence -> input ckt
        public final List<int[]> sequence = new ArrayList<>(); //compiled form: {sequence, term, value, mask}

        public String label() {
            return "Equation " + number + " \"" + tag.trim() + "\"";
        }

        //The compiled form with the circuit references left out, for comparing equations that share a template
        public String signature() {
            StringBuilder s = new StringBuilder();
            for (int[] r : sequence) s.append(r[1]).append(':').append(r[2]).append(':').append(r[3]).append(' ');
            return s.toString();
        }
    }

    //-------------------------------------------------------------------------------------------------
    //Lookups

    public List<Circuit> zonesOf(Circuit c) {
        return zones_of.getOrDefault(c.ckt, List.of());
    }

    public List<Circuit> sourcesOf(Circuit zone) {
        return sources_of.getOrDefault(zone.ckt, List.of());
    }

    public List<Led> ledsOf(Circuit c) {
        return leds_of_input.getOrDefault(c.ckt, List.of());
    }

    public List<Equation> equationsDriving(Circuit zone) {
        return equations_of_subject.getOrDefault(zone.ckt, List.of());
    }

    public List<Circuit> inputZones() {
        List<Circuit> list = new ArrayList<>();
        for (Circuit c : inputs.values()) if (c.kind == Kind.INPUT_ZONE) list.add(c);
        list.sort((a, b) -> a.node != b.node ? Integer.compare(a.node, b.node) : Integer.compare(a.addr, b.addr));
        return list;
    }

    public Circuit inputZone(int node, int addr) {
        for (Circuit c : inputs.values()) {
            if (c.kind == Kind.INPUT_ZONE && c.node == node && c.addr == addr) return c;
        }
        return null;
    }

    public String frameType(int node, int cpu, int frame) {
        return frame_types.getOrDefault(node + "/" + cpu + "/" + frame, "?");
    }

    public String summary() {
        int zones = 0;
        for (Circuit c : inputs.values()) if (c.kind == Kind.INPUT_ZONE) zones++;
        return inputs.size() + " input circuits (" + zones + " input zones), " + outputs.size() + " output circuits, "
            + zone_links.size() + " device-zone links, " + io_links.size() + " input-output links, "
            + leds.size() + " LEDs, " + equations.size() + " equations";
    }

    //-------------------------------------------------------------------------------------------------
    //Loading

    //Lists the job versions in a database as "job/version  description", newest last
    public static List<String> listVersions(File file) throws IOException {
        List<String> list = new ArrayList<>();
        try (Database db = open(file)) {
            Map<Integer, String> names = new HashMap<>();
            Table jobs = db.getTable("Jobs");
            if (jobs != null) for (Row r : jobs) names.put(num(r, "JobID"), str(r, "Description"));
            for (Row r : db.getTable("JobVersions")) {
                int j = num(r, "JobID");
                list.add(j + "/" + num(r, "VersionNo") + "  " + names.getOrDefault(j, "") + "  (" + str(r, "Description") + ")");
            }
        }
        return list;
    }

    //job/version may be null: an exported job holds one version, which is picked automatically.
    //With a job but no version, the newest version of that job is used.
    public static Job load(File file, Integer job_id, Integer version) throws IOException {
        Job job = new Job(file);
        try (Database db = open(file)) {
            job.panel = detectPanel(db);
            job.pickVersion(db, job_id, version);
            if (job.panel != Panel.FX4000) return job; //only the FX4000 layout is understood so far
            job.loadFx4000(db);
        }
        job.index();
        return job;
    }

    private static Database open(File file) throws IOException {
        return new DatabaseBuilder(file).setReadOnly(true).open();
    }

    private static Panel detectPanel(Database db) throws IOException {
        if (db.getTableNames().contains("DeviceConfig")) return Panel.FX4000;
        Table ipt = db.getTable("InputCircuits");
        if (ipt == null) return Panel.UNKNOWN;
        if (ipt.getColumn("PrimDevAddr") != null) return Panel.FX400;
        if (ipt.getColumn("CrossZnCount") != null) return Panel.FX2000;
        return Panel.UNKNOWN;
    }

    private void pickVersion(Database db, Integer want_job, Integer want_version) throws IOException {
        List<int[]> versions = new ArrayList<>();
        for (Row r : db.getTable("JobVersions")) versions.add(new int[] { num(r, "JobID"), num(r, "VersionNo") });
        if (versions.isEmpty()) throw new IOException("The database has no jobs.");

        int[] pick = null;
        if (want_job == null) {
            if (versions.size() > 1) {
                throw new IOException("The database holds " + versions.size() + " job versions. Pick one with --job (and --version), "
                    + "or check an exported job file instead. Use --list to see them.");
            }
            pick = versions.get(0);
        } else {
            for (int[] v : versions) {
                if (v[0] != want_job) continue;
                if (want_version != null ? v[1] == want_version : (pick == null || v[1] > pick[1])) pick = v;
            }
            if (pick == null) throw new IOException("Job " + want_job + (want_version != null ? " version " + want_version : "") + " is not in this database.");
        }
        job_id = pick[0];
        version = pick[1];

        Table jobs = db.getTable("Jobs");
        if (jobs != null) for (Row r : jobs) if (num(r, "JobID") == job_id) description = str(r, "Description");
        for (Row r : db.getTable("JobVersions")) {
            if (num(r, "JobID") == job_id && num(r, "VersionNo") == version) version_description = str(r, "Description");
        }
    }

    private boolean mine(Row r) {
        return num(r, "JobID") == job_id && num(r, "VersionNo") == version;
    }

    private void loadFx4000(Database db) throws IOException {
        Map<Integer, String> device_types = lookup(db, "SYSDevTypes", "DevType", "Tag");
        Map<Integer, String> process_types = lookup(db, "SYSProcTypes", "ProcessType", "Tag");
        Map<Integer, String> frame_type_names = lookup(db, "SYSFrameTypes", "FrameType", "Tag");
        Map<Integer, String> loop_type_names = lookup(db, "SYSLoopTypes", "LoopType", "Tag");
        Map<Integer, String> led_type_names = new HashMap<>();
        for (Row r : db.getTable("SYSLedTypes")) led_type_names.put(num(r, "LedType"), str(r, "DisplayType") + " / " + str(r, "Description"));

        Map<String, Integer> loop_types = new HashMap<>(); //"node/cpu/loop" -> loop type
        for (Row r : db.getTable("LoopConfig")) {
            if (mine(r)) loop_types.put(num(r, "NodeNo") + "/" + num(r, "CPUNo") + "/" + num(r, "LoopNo"), num(r, "LoopType"));
        }

        Map<Integer, Row> devices = new HashMap<>();
        for (Row r : db.getTable("DeviceConfig")) if (mine(r)) devices.put(num(r, "DevNo"), r);

        for (String table : new String[] { "InputCircuits", "OutputCircuits" }) {
            boolean output = table.startsWith("Output");
            for (Row r : db.getTable(table)) {
                if (!mine(r)) continue;
                Circuit c = new Circuit();
                c.ckt = num(r, "CktNo");
                c.output = output;
                c.sub = num(r, "SubAddr");
                c.process_type = num(r, "ProcessType");
                c.process_name = process_types.getOrDefault(c.process_type, "type " + c.process_type);
                c.tag = str(r, "Tag");
                Row d = devices.get(num(r, "DevNo"));
                if (d != null) {
                    c.node = num(d, "NodeNo");
                    c.cpu = num(d, "CPUNo");
                    c.loop = num(d, "LoopNo");
                    c.addr = num(d, "DevAddr");
                    c.device_type = device_types.getOrDefault(num(d, "DevType"), "type " + num(d, "DevType"));
                    int loop_type = loop_types.getOrDefault(c.node + "/" + c.cpu + "/" + c.loop, -1);
                    c.loop_type = loop_type_names.getOrDefault(loop_type, "");
                    if (loop_type == LOOP_TYPE_INPUT_ZONE) c.kind = Kind.INPUT_ZONE;
                    else if (loop_type == LOOP_TYPE_OUTPUT_ZONE) c.kind = Kind.OUTPUT_ZONE;
                    else if (c.cpu == 0) c.kind = Kind.PANEL;
                }
                (output ? outputs : inputs).put(c.ckt, c);
            }
        }

        for (Row r : db.getTable("IptZoneCorrs")) if (mine(r)) zone_links.add(new int[] { num(r, "IptCkt"), num(r, "IptZone") });
        for (Row r : db.getTable("IOCorrs")) if (mine(r)) io_links.add(new int[] { num(r, "IptCkt"), num(r, "OptCkt") });

        for (Row r : db.getTable("DisplayFrames")) {
            if (mine(r)) {
                frame_types.put(num(r, "NodeNo") + "/" + num(r, "CPUNo") + "/" + num(r, "FrameNo"),
                    frame_type_names.getOrDefault(num(r, "FrameType"), "type " + num(r, "FrameType")));
            }
        }

        String[] state_columns = { "AllActive", "Alert", "SomeActive", "PreAlarm", "Unbypass", "ManCtrl0", "ManCtrl1", "ManCtrl2", "Trouble" };
        for (Row r : db.getTable("LedDefinitions")) {
            if (!mine(r)) continue;
            Led l = new Led();
            l.group = num(r, "LedGrp");
            l.led_type = num(r, "LedType");
            l.led_type_name = led_type_names.getOrDefault(l.led_type, "type " + l.led_type);
            l.tag = str(r, "Tag");
            for (String s : state_columns) l.states.put(s, num(r, s));
            leds.put(l.group, l);
        }
        for (Row r : db.getTable("LedLookup")) {
            if (!mine(r)) continue;
            Led l = leds.get(num(r, "LedGrp"));
            if (l != null) l.slots.add(new int[] { num(r, "NodeNo"), num(r, "CPUNo"), num(r, "FrameNo"), num(r, "LedNo") });
        }
        for (Row r : db.getTable("LedIpts")) if (mine(r)) led_input_links.add(new int[] { num(r, "LedGrp"), num(r, "IptCkt") });
        for (Row r : db.getTable("LedOpts")) if (mine(r)) led_output_links.add(new int[] { num(r, "LedGrp"), num(r, "OptCkt") });

        for (Row r : db.getTable("Equations")) {
            if (!mine(r)) continue;
            Equation e = new Equation();
            e.number = num(r, "EquationNo");
            e.node = num(r, "NodeNo");
            e.tag = str(r, "Tag");
            e.text = str(r, "Equation");
            e.comments = str(r, "Comments");
            equations.put(e.number, e);
        }
        for (Row r : db.getTable("EquationInput")) {
            Equation e = mine(r) ? equations.get(num(r, "EquationNo")) : null;
            if (e != null) e.inputs.put(num(r, "Sequence"), num(r, "IptCkt"));
        }
        for (Row r : db.getTable("EquationSequence")) {
            Equation e = mine(r) ? equations.get(num(r, "EquationNo")) : null;
            if (e != null) e.sequence.add(new int[] { num(r, "Sequence"), num(r, "Term"), num(r, "Value"), num(r, "Mask") });
        }
        for (Equation e : equations.values()) e.sequence.sort((a, b) -> Integer.compare(a[0], b[0]));
        for (Row r : db.getTable("EquationSubjIpt")) {
            Equation e = mine(r) ? equations.get(num(r, "EquationNo")) : null;
            Circuit c = inputs.get(num(r, "IptCkt"));
            if (e != null && c != null) e.subjects.add(c);
        }
    }

    private void index() {
        for (int[] link : zone_links) {
            Circuit src = inputs.get(link[0]);
            Circuit zone = inputs.get(link[1]);
            if (src == null || zone == null) continue;
            zones_of.computeIfAbsent(src.ckt, k -> new ArrayList<>()).add(zone);
            sources_of.computeIfAbsent(zone.ckt, k -> new ArrayList<>()).add(src);
        }
        for (int[] link : led_input_links) {
            Led l = leds.get(link[0]);
            Circuit c = inputs.get(link[1]);
            if (l == null || c == null) continue;
            l.inputs.add(c);
            leds_of_input.computeIfAbsent(c.ckt, k -> new ArrayList<>()).add(l);
        }
        for (int[] link : led_output_links) {
            Led l = leds.get(link[0]);
            Circuit c = outputs.get(link[1]);
            if (l != null && c != null) l.outputs.add(c);
        }
        for (Equation e : equations.values()) {
            for (Circuit c : e.subjects) equations_of_subject.computeIfAbsent(c.ckt, k -> new ArrayList<>()).add(e);
        }
    }

    private static Map<Integer, String> lookup(Database db, String table, String key, String value) throws IOException {
        Map<Integer, String> map = new HashMap<>();
        Table t = db.getTable(table);
        if (t != null) for (Row r : t) map.put(num(r, key), str(r, value));
        return map;
    }

    static int num(Row r, String column) {
        Object o = r.get(column);
        if (o instanceof Number) return ((Number) o).intValue();
        if (o instanceof Boolean) return ((Boolean) o) ? 1 : 0;
        if (o instanceof String) {
            try {
                return Integer.parseInt(((String) o).trim());
            } catch (NumberFormatException e) {
                return -1;
            }
        }
        return -1;
    }

    static String str(Row r, String column) {
        Object o = r.get(column);
        return o == null ? "" : o.toString();
    }
}
