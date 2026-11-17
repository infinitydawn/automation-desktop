package checker;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import checker.Job.Circuit;
import checker.Job.Kind;
import checker.Roles.Device;
import checker.Roles.Family;
import checker.Roles.Output;
import checker.Roles.Zone;

//Checks from the FlexNet & FX6000 "Device & Zone Cheat Sheet" (FX4000 has the same requirements).
//Each check names the cheat sheet line it comes from in its message.
public final class CheatSheetRules {

    private CheatSheetRules() {}

    //How many example devices to list when many are missing from one output
    static final int EXAMPLES = 6;

    //Device inputs the cheat sheet rules look at: real devices, not panel/telephone/amplifier circuits or spares
    static List<Circuit> devices(Job job) {
        List<Circuit> list = new ArrayList<>();
        for (Circuit c : job.inputs.values()) {
            if (c.kind != Kind.DEVICE || c.isSpare()) continue;
            if (c.loop_type.equals("Telephone") || c.loop_type.equals("Amplifiers") || c.loop_type.equals("Switches")) continue;
            list.add(c);
        }
        return list;
    }

    static Map<Zone, List<Circuit>> zonesByRole(Job job) {
        Map<Zone, List<Circuit>> map = new EnumMap<>(Zone.class);
        for (Circuit z : job.inputZones()) map.computeIfAbsent(Roles.zone(z), k -> new ArrayList<>()).add(z);
        return map;
    }

    static boolean inAny(Job job, Circuit device, List<Circuit> zones) {
        if (zones == null) return false;
        for (Circuit z : job.zonesOf(device)) if (zones.contains(z)) return true;
        return false;
    }

    static String list(Iterable<Circuit> circuits) {
        StringBuilder s = new StringBuilder();
        int n = 0, total = 0;
        for (Circuit c : circuits) {
            total++;
            if (n < EXAMPLES) {
                s.append("\n            ").append(c.label());
                n++;
            }
        }
        if (total > n) s.append("\n            ... and ").append(total - n).append(" more");
        return s.toString();
    }

    //---------------------------------------------------------------------------------------------------

    public static class DeviceSetup implements Rule {

        public String id() {
            return "SHEET-DEVICES";
        }

        public String name() {
            return "Device setup";
        }

        public String title() {
            return "Cheat sheet device setup: input type and NS flag for each kind of device";
        }

        public boolean supports(Job.Panel panel) {
            return panel.isMgc();
        }

        public void check(Job job, Report report) {
            int checked = 0;
            for (Circuit c : devices(job)) {
                Device role = Roles.device(c);
                if (role.expected == null) continue;
                checked++;
                Family is = Roles.family(c);
                if (is != role.expected && !(role == Device.SUPERVISORY && is == Family.TROUBLE)) {
                    String want = role.expected == Family.ALARM ? "an alarm type (Alarm Input)"
                        : role.expected == Family.LATCHED ? "Latched Supv" : role.expected == Family.TROUBLE ? "Trouble Input" : "Non-Latch Supv";
                    boolean serious = is == Family.ALARM || role.expected == Family.ALARM;
                    String msg = "Tagged as a " + role.label + " but its type is \"" + c.process_name + "\"; the cheat sheet sets it to " + want + ".";
                    //Combo smoke/CO (sounder base) detectors inside apartments are sometimes set up as supervisory on purpose
                    if (c.device_type.toLowerCase().contains("fire-co") && role == Device.SMOKE) {
                        serious = false;
                        msg += " (Combo smoke/CO: fine if this is an in-unit detector meant to be supervisory.)";
                    }
                    if (serious) report.error(c.label(), msg);
                    else report.warning(c.label(), msg);
                }
                if (role == Device.WATERFLOW && c.silenceable) {
                    report.error(c.label(), "Waterflows should have NS (non-silenceable) set.");
                }
            }
            report.note(checked + " recognised device inputs checked");
        }
    }

    //---------------------------------------------------------------------------------------------------

    public static class DeviceZones implements Rule {

        public String id() {
            return "SHEET-ZONES";
        }

        public String name() {
            return "Standard zones";
        }

        public String title() {
            return "Cheat sheet zones: every device in General Alarm / Automatic Alarm / Supervisory / Waterflow as required";
        }

        public boolean supports(Job.Panel panel) {
            return panel.isMgc();
        }

        public void check(Job job, Report report) {
            Map<Zone, List<Circuit>> zones = zonesByRole(job);
            List<String> found = new ArrayList<>();
            for (Map.Entry<Zone, List<Circuit>> e : zones.entrySet()) {
                if (e.getKey() != Zone.OTHER) found.add(e.getKey().label + " x" + e.getValue().size());
            }
            report.note("Standard zones found: " + (found.isEmpty() ? "none" : String.join(", ", found)));

            //Zone setup
            for (Map.Entry<Zone, List<Circuit>> e : zones.entrySet()) {
                for (Circuit z : e.getValue()) {
                    Family f = Roles.family(z);
                    switch (e.getKey()) {
                        case GENERAL_ALARM: case AUTOMATIC_ALARM: case WATERFLOW: case ELEV_RECALL: case ALT_RECALL:
                            if (f != Family.ALARM) report.error(z.label(), e.getKey().label + " zones should be type Alarm, not \"" + z.process_name + "\".");
                            break;
                        case NON_LATCHED: case LATCHED:
                            if (f != Family.SUPERVISORY && f != Family.LATCHED) {
                                report.error(z.label(), e.getKey().label + " zones should be a supervisory type, not \"" + z.process_name + "\".");
                            }
                            break;
                        default:
                    }
                    if (e.getKey() == Zone.WATERFLOW && z.silenceable) report.error(z.label(), "The Waterflow zone should have F1 NS (non-silenceable).");
                    if (e.getKey() == Zone.LATCHED && Boolean.FALSE.equals(z.co_flag)) report.warning(z.label(), "The Latched Supervisory (CO) zone should have the F4 CO flag.");
                }
            }

            List<Circuit> ga = zones.get(Zone.GENERAL_ALARM), auto = zones.get(Zone.AUTOMATIC_ALARM);
            List<Circuit> nls = zones.get(Zone.NON_LATCHED), latched = zones.get(Zone.LATCHED), wf = zones.get(Zone.WATERFLOW);
            List<Circuit> alarm_zones = new ArrayList<>();
            for (Zone z : new Zone[] { Zone.GENERAL_ALARM, Zone.AUTOMATIC_ALARM, Zone.WATERFLOW, Zone.ELEV_RECALL, Zone.ALT_RECALL }) {
                if (zones.containsKey(z)) alarm_zones.addAll(zones.get(z));
            }

            List<Circuit> devices = devices(job);
            int alarms = 0, supv = 0, latch = 0, waterflows = 0;
            List<Circuit> no_ga = new ArrayList<>(), no_auto = new ArrayList<>(), no_nls = new ArrayList<>(), no_latched = new ArrayList<>(), no_wf = new ArrayList<>();
            for (Circuit c : devices) {
                Family f = Roles.family(c);
                Device role = Roles.device(c);
                if (f == Family.ALARM) {
                    alarms++;
                    if (ga != null && !inAny(job, c, ga)) no_ga.add(c);
                    if (auto != null && role != Device.PULL && !inAny(job, c, auto)) no_auto.add(c);
                    if (role == Device.PULL && inAny(job, c, auto)) {
                        report.error(c.label(), "Pull stations must not be in Automatic Alarm (cheat sheet: Automatic Alarm = all alarm inputs besides pull stations).");
                    }
                    if (role == Device.WATERFLOW) {
                        waterflows++;
                        if (wf != null && !inAny(job, c, wf)) no_wf.add(c);
                    }
                    if (inAny(job, c, nls) || inAny(job, c, latched)) {
                        report.warning(c.label(), "An alarm input is in a supervisory zone.");
                    }
                } else if (f == Family.SUPERVISORY || f == Family.LATCHED) {
                    if (f == Family.SUPERVISORY) {
                        supv++;
                        if (nls != null && !inAny(job, c, nls)) no_nls.add(c);
                    } else if (role == Device.CO) { //Latched Supervisory zone is for CO detectors
                        latch++;
                        if (latched != null && !inAny(job, c, latched)) no_latched.add(c);
                    }
                    for (Circuit z : job.zonesOf(c)) {
                        if (alarm_zones.contains(z)) report.error(c.label(), "A supervisory input is in " + z.label() + "; supervisory devices must not set off alarm zones.");
                    }
                }
            }
            missing(report, "General Alarm", ga, alarms, no_ga, "Correlate to all signals and inputs", true);
            missing(report, "Automatic Alarm", auto, alarms, no_auto, "Correlate to all alarm inputs besides pull station", true);
            missing(report, "Non-Latched Supervisory", nls, supv, no_nls, "Correlate to all non-latched devices", true);
            missing(report, "Latched Supervisory", latched, latch, no_latched, "For CO detectors", true);
            missing(report, "Waterflow", wf, waterflows, no_wf, "Correlate all waterflows to an input zone", true);
        }

        //One finding per device that is missing from a zone it belongs in
        private static void missing(Report report, String zone, List<Circuit> zones, int expected, List<Circuit> devices, String sheet, boolean error) {
            if (zones == null) {
                if (expected > 0) {
                    report.warning("Input zones", "No " + zone + " input zone found (by tag), but " + Report.plural(expected, "device")
                        + " would belong in it. Cheat sheet: " + sheet + ".");
                }
                return;
            }
            for (Circuit c : devices) {
                String msg = "Not in the " + zone + " zone. Cheat sheet: " + sheet + ".";
                if (error) report.error(c.label(), msg);
                else report.warning(c.label(), msg);
            }
        }
    }

    //---------------------------------------------------------------------------------------------------

    public static class Outputs implements Rule {

        public String id() {
            return "SHEET-OUTPUTS";
        }

        public String name() {
            return "Relays & strobes";
        }

        public String title() {
            return "Cheat sheet outputs: strobes, elevator recall, fan/boiler shutdown, door release and other relays";
        }

        public boolean supports(Job.Panel panel) {
            return panel.isMgc();
        }

        public void check(Job job, Report report) {
            List<Circuit> devices = devices(job);
            List<Circuit> alarms = new ArrayList<>(), automatic = new ArrayList<>(), waterflows = new ArrayList<>();
            for (Circuit c : devices) {
                if (Roles.family(c) != Family.ALARM) continue;
                alarms.add(c);
                Device role = Roles.device(c);
                if (role != Device.PULL) automatic.add(c);
                if (role == Device.WATERFLOW) waterflows.add(c);
            }

            Map<Output, Integer> counts = new EnumMap<>(Output.class);
            List<Circuit> recalls = new ArrayList<>(); //main and alt
            List<Circuit> main_recalls = new ArrayList<>(); //main recall and shunt trip
            List<Circuit> shaft_lights = new ArrayList<>();
            for (Circuit o : job.outputs.values()) {
                if (o.isSpare() || o.kind == Kind.OUTPUT_ZONE) continue;
                Output role = Roles.output(o);
                counts.merge(role, 1, Integer::sum);
                Set<Circuit> reach = job.devicesReaching(o);
                switch (role) {
                    case NOTIFICATION:
                        need(report, o, reach, alarms, "Strobes correlate to all alarms");
                        noSupervisory(report, o, reach, true, "Do not correlate any supervisory to strobes");
                        break;
                    case DOOR_RELEASE:
                        need(report, o, reach, automatic, "Door release gets all alarms (General Alarm, or Automatic Alarm without pull stations)");
                        noSupervisory(report, o, reach, false, "Door release is for alarms");
                        break;
                    case MUSIC_SHUTDOWN:
                        need(report, o, reach, alarms, "Music Shutdown: correlate to General Alarm");
                        break;
                    case FAN_SHUTDOWN:
                        need(report, o, reach, automatic, "Fan/AC Shutdowns get all alarms but not pull stations");
                        for (Circuit c : reach) {
                            if (Roles.device(c) == Device.PULL) {
                                report.warning(o.label(), "Activated by pull station " + c.label() + ". Cheat sheet: Fan/AC Shutdowns get all alarms but not pull stations.");
                            }
                        }
                        noSupervisory(report, o, reach, false, "Fan/AC Shutdowns get all alarms");
                        if (!o.aux_reset) report.warning(o.label(), "Fan/AC shutdown relays should have F3 AR (auxiliary reset).");
                        break;
                    case ELEV_RECALL:
                        recalls.add(o);
                        main_recalls.add(o);
                        noSupervisory(report, o, reach, false, "Elevator recall is for elevator smokes/heats and waterflows");
                        break;
                    case ALT_RECALL:
                        recalls.add(o);
                        noSupervisory(report, o, reach, false, "Elevator recall is for elevator smokes/heats and waterflows");
                        break;
                    case BOILER_SHUTDOWN: {
                        boolean co = false;
                        for (Circuit c : reach) if (Roles.device(c) == Device.CO) co = true;
                        if (!co) report.error(o.label(), "No CO detector activates this. Cheat sheet: Boiler/HWH/Laundry Shutdown gets the CO detector(s) in the same room.");
                        break;
                    }
                    case GENERATOR_SHUTDOWN: {
                        boolean heat = false;
                        for (Circuit c : reach) if (Roles.device(c) == Device.HEAT) heat = true;
                        if (!heat) report.warning(o.label(), "No heat detector activates this. Cheat sheet: Generator Shutdown correlates to the heat detector in the same room.");
                        break;
                    }
                    case SHAFT_LIGHT:
                        shaft_lights.add(o);
                        break;
                    default:
                }
            }

            //Waterflows and elevator recall. One recall relay: it gets every waterflow. Several (towers, car lifts):
            //"correlate only to their associated devices", so each waterflow needs at least one recall relay and
            //each recall relay at least one waterflow.
            if (main_recalls.size() == 1) {
                Circuit o = main_recalls.get(0);
                need(report, o, job.devicesReaching(o), waterflows, "Elev recall/shunt trip relays get all waterflows");
            } else if (main_recalls.size() > 1 && !waterflows.isEmpty()) {
                for (Circuit c : waterflows) {
                    boolean any = false;
                    for (Circuit o : job.outputsReachedBy(c)) if (main_recalls.contains(o)) any = true;
                    if (!any) report.error(c.label(), "This waterflow activates none of the " + main_recalls.size()
                        + " elevator recall / shunt trip relays. Cheat sheet: Elev recall/shunt trip relays get all waterflows.");
                }
                for (Circuit o : main_recalls) {
                    boolean any = false;
                    for (Circuit c : job.devicesReaching(o)) if (Roles.device(c) == Device.WATERFLOW) any = true;
                    if (!any) report.warning(o.label(), "No waterflow activates this recall / shunt trip relay. Cheat sheet: Elev recall/shunt trip relays get all waterflows "
                        + "(with several recalls, the waterflows associated with this one).");
                }
            }

            //Elevator lobby / EMR / top of shaft smokes and heats should recall the elevator
            if (!recalls.isEmpty()) {
                List<Circuit> no_recall = new ArrayList<>();
                for (Circuit c : automatic) {
                    Device role = Roles.device(c);
                    if ((role == Device.SMOKE || role == Device.HEAT) && Roles.elevatorRelated(c)) {
                        boolean any = false;
                        for (Circuit o : job.outputsReachedBy(c)) if (recalls.contains(o)) any = true;
                        if (!any) no_recall.add(c);
                    }
                }
                for (Circuit c : no_recall) {
                    report.warning(c.label(), "Looks like an elevator lobby / EMR / shaft detector but activates no elevator recall relay. "
                        + "Cheat sheet: elev recall relays are correlated to elev lobby smokes, top of shaft smokes and smokes in the EMR.");
                }
            }

            //Shaft light relays follow elevator recall (main and alt)
            if (!shaft_lights.isEmpty()) {
                Map<Zone, List<Circuit>> zones = zonesByRole(job);
                List<Circuit> recall_zones = new ArrayList<>();
                if (zones.containsKey(Zone.ELEV_RECALL)) recall_zones.addAll(zones.get(Zone.ELEV_RECALL));
                if (zones.containsKey(Zone.ALT_RECALL)) recall_zones.addAll(zones.get(Zone.ALT_RECALL));
                for (Circuit light : shaft_lights) {
                    List<Circuit> from = job.directInputs(light);
                    for (Circuit z : recall_zones) {
                        if (z.node == light.node && !from.contains(z)) {
                            report.warning(light.label(), "Not correlated to " + z.label() + ". Cheat sheet: FSAE Shaft Lights correlate to elevator recall (including alt).");
                        }
                    }
                }
            }

            List<String> parts = new ArrayList<>();
            for (Map.Entry<Output, Integer> e : counts.entrySet()) {
                if (e.getKey() != Output.OTHER) parts.add(e.getValue() + " " + e.getKey().label);
            }
            report.note("Outputs recognised: " + (parts.isEmpty() ? "none" : String.join(", ", parts)));
        }

        //Report the devices of a required set that do not activate this output, as one finding
        private static void need(Report report, Circuit output, Set<Circuit> reach, List<Circuit> required, String sheet) {
            Set<Circuit> missing = new LinkedHashSet<>();
            for (Circuit c : required) if (!reach.contains(c)) missing.add(c);
            if (missing.isEmpty()) return;
            String what = missing.size() == required.size() ? "None of the " + Report.plural(required.size(), "device") + " it needs activate it"
                : Report.plural(missing.size(), "device") + " of " + required.size() + " do not activate it";
            report.error(output.label(), what + ". Cheat sheet: " + sheet + "." + list(missing));
        }

        private static void noSupervisory(Report report, Circuit output, Set<Circuit> reach, boolean error, String sheet) {
            List<Circuit> supv = new ArrayList<>();
            for (Circuit c : reach) {
                Family f = Roles.family(c);
                if (f == Family.SUPERVISORY || f == Family.LATCHED) supv.add(c);
            }
            if (supv.isEmpty()) return;
            String msg = "Activated by " + Report.plural(supv.size(), "supervisory input") + ". Cheat sheet: " + sheet + "." + list(supv);
            if (error) report.error(output.label(), msg);
            else report.warning(output.label(), msg);
        }
    }

    //---------------------------------------------------------------------------------------------------

    public static class Bypass implements Rule {

        public String id() {
            return "SHEET-BYPASS";
        }

        public String name() {
            return "Zone bypass";
        }

        public String title() {
            return "Cheat sheet zone bypass: every relay, signal and strobe is on the Mixed / Zone Bypass switch";
        }

        public boolean supports(Job.Panel panel) {
            return panel.isMgc();
        }

        public void check(Job job, Report report) {
            List<Job.ControlGroup> groups = job.bypassGroups();
            List<Circuit> outputs = new ArrayList<>();
            for (Circuit o : job.outputs.values()) {
                if (o.isSpare() || o.kind == Kind.OUTPUT_ZONE) continue;
                if (o.process_type == 13 || o.process_type == 14 || o.process_type == 15) outputs.add(o);
            }
            if (groups.isEmpty()) {
                if (!outputs.isEmpty()) {
                    report.error("Base Control", "No Mixed / Zone Bypass switch found. Cheat sheet: correlate all relays, signals and strobes to Zone Bypass.");
                }
                return;
            }
            Set<Circuit> bypassed = new LinkedHashSet<>();
            for (Job.ControlGroup g : groups) {
                bypassed.addAll(g.outputs);
                report.note(g.label() + ": " + Report.plural(g.outputs.size(), "output"));
            }
            for (Circuit o : outputs) {
                if (!bypassed.contains(o)) report.warning(o.label(), "Not on the Zone Bypass switch. Cheat sheet: correlate all relays to Zone Bypass.");
            }
        }
    }
}
