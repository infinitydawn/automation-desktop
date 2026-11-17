package checker;

import java.util.regex.Pattern;

import checker.Job.Circuit;

//Works out what a device, input zone or output is from its tag (and device/process type), using the names
//from the Device & Zone Cheat Sheet. Tags are free text, so this is keyword matching; anything not
//recognised is OTHER and the cheat sheet rules leave it alone.
public final class Roles {

    private Roles() {}

    //--------------------------------------------------------------------------------------------------
    //Process types (SYSProcTypes) grouped the way the cheat sheet talks about them

    public enum Family { ALARM, SUPERVISORY, LATCHED, TROUBLE, MONITOR, OUTPUT, NONE }

    public static Family family(Circuit c) {
        switch (c.process_type) {
            case 0: case 1: case 2: case 5: case 10: return Family.ALARM; //Verified, Alarm Input, Priority, Waterflow, Manual Pull
            case 3: return Family.LATCHED;
            case 4: return Family.SUPERVISORY; //Non-Latch Supv
            case 6: return Family.TROUBLE;
            case 7: return Family.MONITOR;
            case 13: case 14: case 15: return Family.OUTPUT; //Signal, Strobe, Relay
            default: return Family.NONE;
        }
    }

    //--------------------------------------------------------------------------------------------------
    //Device inputs

    public enum Device {
        WATERFLOW("Waterflow", Family.ALARM),
        PULL("Pull Station", Family.ALARM),
        ANSUL("Ansul System", Family.ALARM),
        CO("CO Detector", Family.LATCHED),
        LOW_HEAT("Low Heat Detector", Family.SUPERVISORY),
        SMOKE("Smoke Detector", Family.ALARM),
        HEAT("Heat Detector", Family.ALARM),
        SUPERVISORY("supervisory device", Family.SUPERVISORY),
        PSU_TROUBLE("power supply trouble", Family.TROUBLE),
        OTHER("device", null);

        public final String label;
        public final Family expected; //what the cheat sheet sets this device to, null = no rule

        Device(String label, Family expected) {
            this.label = label;
            this.expected = expected;
        }
    }

    private static final Pattern WATERFLOW = Pattern.compile("water\\s*flow|waterf|\\bps\\s*-?10\\b|\\bwf\\b");
    private static final Pattern PULL = Pattern.compile("pull");
    private static final Pattern ANSUL = Pattern.compile("ansul");
    private static final Pattern LOW_HEAT = Pattern.compile("lo(w)?\\s*heat");
    private static final Pattern CO = Pattern.compile("\\bco\\b|carbon|co\\s*det");
    private static final Pattern SMOKE = Pattern.compile("smoke|\\bduct|photo");
    private static final Pattern HEAT = Pattern.compile("heat|135");
    private static final Pattern PSU = Pattern.compile("inx|psu|power supply|\\bpsp\\b");
    private static final Pattern SUPERVISORY = Pattern.compile(
        "valve|tamper|pump|jock|generator|gen\\b|radio|door open|damp|fan|power|pwr|fault|key|dry sys|air|rcv|intake|discharge|bypass|"
        + "status|running|fail|trouble|monitor|elev|phase|low temp|hoistway|meter|riser");

    public static Device device(Circuit c) {
        String t = c.tag1().toLowerCase();
        String type = c.device_type.toLowerCase();
        //FX6000 combo smoke/CO: the "B" input is the CO
        if (type.contains("fire-co") && c.sub == 2) return Device.CO;
        if (type.contains("fire-co")) return Device.SMOKE;
        if (WATERFLOW.matcher(t).find()) return Device.WATERFLOW;
        if (PULL.matcher(t).find()) return Device.PULL;
        if (ANSUL.matcher(t).find()) return Device.ANSUL;
        if (LOW_HEAT.matcher(t).find()) return Device.LOW_HEAT;
        if (CO.matcher(t).find() && !t.contains("smoke")) return Device.CO;
        if (PSU.matcher(t).find() && (t.contains("trouble") || t.contains("fault") || t.contains("fail"))) return Device.PSU_TROUBLE;
        boolean monitor_module = type.contains("monit") || type.contains("input mod");
        if (monitor_module && SUPERVISORY.matcher(t).find()) return Device.SUPERVISORY;
        if (SMOKE.matcher(t).find()) return Device.SMOKE;
        if (HEAT.matcher(t).find()) return Device.HEAT;
        if (SUPERVISORY.matcher(t).find()) return Device.SUPERVISORY;
        return Device.OTHER;
    }

    //Smokes/heats the cheat sheet ties to elevator recall: elevator lobby, EMR, top of shaft
    private static final Pattern ELEVATOR = Pattern.compile("elev|\\bemr\\b|machine\\s*r|shaft|hoistway");

    public static boolean elevatorRelated(Circuit c) {
        return ELEVATOR.matcher(c.cleanTag().toLowerCase()).find();
    }

    //--------------------------------------------------------------------------------------------------
    //Input zones

    public enum Zone {
        GENERAL_ALARM("General Alarm"),
        AUTOMATIC_ALARM("Automatic Alarm"),
        NON_LATCHED("Non-Latched Supervisory"),
        LATCHED("Latched Supervisory"),
        WATERFLOW("Waterflow"),
        ELEV_RECALL("Elevator Recall"),
        ALT_RECALL("Alt Elevator Recall"),
        OTHER("input zone");

        public final String label;

        Zone(String label) {
            this.label = label;
        }
    }

    public static Zone zone(Circuit c) {
        String t = Text.norm(c.tag1());
        if (t.contains("generalalarm")) return Zone.GENERAL_ALARM;
        if (t.contains("automaticalarm") || t.startsWith("autoalarm")) return Zone.AUTOMATIC_ALARM;
        if (t.contains("nonlatch")) return Zone.NON_LATCHED;
        if (t.startsWith("latched") || t.equals("cosupv") || t.equals("latchedsupv")) return Zone.LATCHED;
        if (t.startsWith("waterflow") || t.equals("allwaterflows")) return Zone.WATERFLOW;
        if (t.contains("recal") && (t.startsWith("alt") || t.contains("alternate"))) return Zone.ALT_RECALL;
        if (t.contains("elevrecal") || t.contains("elevatorrecal") || t.startsWith("recal")) return Zone.ELEV_RECALL;
        return Zone.OTHER;
    }

    //--------------------------------------------------------------------------------------------------
    //Outputs

    public enum Output {
        NOTIFICATION("strobe / horn circuit"),
        SPEAKER("speaker"),
        SOUNDER("sounder base"),
        ELEV_RECALL("elevator recall / shunt trip relay"),
        ALT_RECALL("alt elevator recall relay"),
        FAN_SHUTDOWN("fan / AC shutdown relay"),
        BOILER_SHUTDOWN("boiler / HWH / laundry shutdown relay"),
        DOOR_RELEASE("door release relay"),
        MUSIC_SHUTDOWN("music shutdown relay"),
        SHAFT_LIGHT("FSAE shaft light relay"),
        GENERATOR_SHUTDOWN("generator shutdown relay"),
        DAMPER("damper / fan relay"),
        SIGNAL_SILENCE("signal silence relay"),
        OTHER("output");

        public final String label;

        Output(String label) {
            this.label = label;
        }
    }

    private static final Pattern NOTIFICATION = Pattern.compile("strobe|horn|bell|chime|\\bnac\\b|audible|visual|signal circuit");
    private static final Pattern NOT_NOTIFICATION = Pattern.compile("sync|paging|page");

    //The first tag line says what the output is, the second where it is ("Fan Shutdown South" / "27FL Boiler Plant"),
    //so the first line decides and the whole tag is only used when the first line says nothing
    public static Output output(Circuit c) {
        Output role = output(c, c.tag1().toLowerCase());
        return role != Output.OTHER ? role : output(c, c.cleanTag().toLowerCase());
    }

    private static Output output(Circuit c, String t) {
        String type = c.device_type.toLowerCase();
        if (type.contains("sounder") || t.contains("sounder")) return Output.SOUNDER;
        if (t.contains("speaker") || type.contains("watt") || type.startsWith("control")) return Output.SPEAKER;
        if (t.contains("signal silence") || t.contains("sig sil")) return Output.SIGNAL_SILENCE;
        if (t.contains("shaft light")) return Output.SHAFT_LIGHT;
        if (t.contains("recal") || t.contains("shunt")) { //"recal": tags get cut short, e.g. "ElevRecalSouthTowr"
            return t.startsWith("alt") || t.contains("alternate") ? Output.ALT_RECALL : Output.ELEV_RECALL;
        }
        if (t.contains("music")) return Output.MUSIC_SHUTDOWN;
        if (t.contains("door") && (t.contains("release") || t.contains("holder") || t.contains("mag"))) return Output.DOOR_RELEASE;
        if (t.contains("generator") && t.contains("shut")) return Output.GENERATOR_SHUTDOWN;
        if ((t.contains("boiler") || t.contains("hwh") || t.contains("water heater") || t.contains("laundry")) && t.contains("shut")) {
            return Output.BOILER_SHUTDOWN;
        }
        if ((t.contains("fan") || t.contains("ac ") || t.contains("a/c") || t.contains("hvac") || t.contains("ahu") || t.contains("rtu"))
            && t.contains("shut")) {
            return Output.FAN_SHUTDOWN;
        }
        if (t.contains("damp") || t.contains("purge") || t.contains("fresh air") || t.contains("spf") || t.contains("epf")
            || t.contains("fsd") || t.contains("exhaust") || t.contains("fan")) {
            return Output.DAMPER;
        }
        //Signal circuits are also used for sync, paging-active and other non-notification jobs, so a Signal
        //only counts as a strobe/horn circuit when its tag says so. Strobe-type circuits always count.
        if (NOT_NOTIFICATION.matcher(t).find()) return Output.OTHER;
        if (c.process_type == 14) return Output.NOTIFICATION;
        if (c.process_type == 13 && NOTIFICATION.matcher(t).find()) return Output.NOTIFICATION;
        return Output.OTHER;
    }
}
