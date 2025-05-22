import java.util.ArrayList;
import java.util.Collections;
import java.awt.event.KeyEvent;
import java.io.File;

import org.ini4j.Ini;


public class FX400 extends Thread{

    protected int DELAY = 200; //Default 200. Delay time for everything. Multiply by delay strength to change length
    protected double ENTER_DELAY_STRENGTH = 1; // Default 1. Delay after pressing Enter (Writes to database. Larger databases may want this higher)
    protected boolean BYPASS_PAUSE = false; //Prevents the error prompt from showing
    protected boolean IGNORE_TAG_LENGTH = false; //Omits tag length requirement from errors
    protected String SETTINGS_FILE = "settings.ini";

    protected DataEntryBot bot;
    protected int skip_count = 19;
    protected boolean is_running = false; //used to stop the bot from running without closing process
    protected boolean is_paused = false; //used to prompt user to enable AR related settings -- MAKE USE OF THIS IF PAUSING GUI

    public FX400() {
        try {
            readSettings();
            bot = new DataEntryBot(DELAY);           
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void run() {
        System.out.println("Starting FX400 Data Entry");
        try{
            is_running = true;
            is_paused = false;

            ZoneList zone_list = new ZoneList();
            zone_list.readFile();
            zone_list.displayZoneList();

            if(zone_list.CONTAINS_AR) {
                is_paused = true;
            }

            System.out.println("----------------------------------------------------------------");
            if (validateZones(zone_list)) {
                is_running = false;
                System.out.println("----------------------------------------------------------------");
                System.out.println("Errors found in zone list, please correct them and run again.");
                System.out.println("----------------------------------------------------------------");
            }

            if(is_running) {
                if(is_paused) {
                    if(BYPASS_PAUSE) {
                        is_paused = false;
                    }else{
                        System.out.println("The following settings need to be enabled for data entry:");
                        if(zone_list.CONTAINS_AR) {
                            System.out.println("AR/Buzzer Silence");
                        }
                        System.out.println("Please make necessary changes and press F2 to continue.");
                        System.out.println("----------------------------------------------------------------");
                    }
                }

                while(is_paused) {
                    Thread.sleep(Math.max(100,DELAY)); //Wait until start button pressed again
                }

                ArrayList<Zone> zones = zone_list.zones;
                Zone zone = zones.get(0);
                skip_count = (int) zone.getAddress() - 1;

                for(int current_zone = 0; current_zone < zones.size() && is_running; current_zone++) {

                    zone = zones.get(current_zone);
                    //Get difference of current and previous address, then -1
                    if(current_zone > 0) {
                        skip_count += (int) zone.getAddress() - (int) zones.get(current_zone - 1).getAddress() - 1;
                    }

                    System.out.println("Inserting: " + zone.getZoneinfo());

                    switch (zone.getType()) {
                        case "Photo Detector":
                            addPhotoDetector();
                            break;
                        case "Alarm Input":
                            addAlarmInputMod();
                            break;
                        case "Non-latched Supervisory":
                            addNonLatchedSupv();
                            break;
                        case "Latched Supervisory":
                            addLatchedSupv();
                            break;
                        case "Heat Detector":
                            addHeatDetector();
                            break;
                        case "Alarm Input Class A":
                            addAlarmInputClassA();
                            break;
                        case "Relay":
                            addRelay();
                            break;
                    }
                }

                if(is_running) {
                    enterZoneList(zone_list);
                }

                System.out.println("FX400 Entry Complete");
                is_running = false;
            }
            else {
                System.out.println("FX400 entry did not run");
            }
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }

    protected void skipDevices() {
        bot.pressKey(KeyEvent.VK_RIGHT, skip_count);
    }

    protected void open() {
        try {
            Thread.sleep(DELAY);
            bot.keyPress(KeyEvent.VK_SHIFT);
            bot.keyPress(KeyEvent.VK_F10);
            bot.keyRelease(KeyEvent.VK_SHIFT);
            bot.keyRelease(KeyEvent.VK_F10);
            bot.delay(DELAY);

            bot.pressKey(KeyEvent.VK_DOWN);
            bot.pressKey(KeyEvent.VK_ENTER, 1 , ENTER_DELAY_STRENGTH);
            
        } catch (Exception e) {
            System.err.println(e);
        }
    }
    
    protected void addPhotoDetector() {
        open();
        bot.pressKey(KeyEvent.VK_TAB, 3);
        skipDevices();
        bot.pressKey(KeyEvent.VK_ENTER, 1 , ENTER_DELAY_STRENGTH);
        bot.pressKey(KeyEvent.VK_ESCAPE);
        bot.pressKey(KeyEvent.VK_END);
    }

    protected void addAlarmInputMod() {
        open();
        bot.pressKey(KeyEvent.VK_D, 2);
        bot.pressKey(KeyEvent.VK_TAB, 3);
        skipDevices();
        bot.pressKey(KeyEvent.VK_ENTER, 1 , ENTER_DELAY_STRENGTH);
        bot.pressKey(KeyEvent.VK_ESCAPE);
        bot.pressKey(KeyEvent.VK_END);
    }

    protected void addNonLatchedSupv() {
        open();
        bot.pressKey(KeyEvent.VK_D, 2);
        bot.pressKey(KeyEvent.VK_TAB, 2);
        bot.pressKey(KeyEvent.VK_N);
        bot.pressKey(KeyEvent.VK_TAB);
        skipDevices();
        bot.pressKey(KeyEvent.VK_ENTER, 1 , ENTER_DELAY_STRENGTH);
        bot.pressKey(KeyEvent.VK_ESCAPE);
        bot.pressKey(KeyEvent.VK_END);
    }

    protected void addLatchedSupv() {
        open();
        bot.pressKey(KeyEvent.VK_D,2);
        bot.pressKey(KeyEvent.VK_TAB,2);
        bot.pressKey(KeyEvent.VK_L);
        bot.pressKey(KeyEvent.VK_TAB);
        skipDevices();
        bot.pressKey(KeyEvent.VK_ENTER, 1 , ENTER_DELAY_STRENGTH);
        bot.pressKey(KeyEvent.VK_ESCAPE);
        bot.pressKey(KeyEvent.VK_END);
    }

    protected void addHeatDetector() {
        open();
        bot.pressKey(KeyEvent.VK_H,3);
        bot.pressKey(KeyEvent.VK_TAB,3);
        skipDevices();
        bot.pressKey(KeyEvent.VK_ENTER, 1 , ENTER_DELAY_STRENGTH);
        bot.pressKey(KeyEvent.VK_ESCAPE);
        bot.pressKey(KeyEvent.VK_END);
    }

    protected void addAlarmInputClassA() {
        open();
        bot.pressKey(KeyEvent.VK_D, 2);
        bot.pressKey(KeyEvent.VK_TAB, 1);
        bot.pressKey(KeyEvent.VK_C, 1);
        bot.pressKey(KeyEvent.VK_TAB, 2);
        skipDevices();
        bot.pressKey(KeyEvent.VK_ENTER, 1 , ENTER_DELAY_STRENGTH);
        bot.pressKey(KeyEvent.VK_ESCAPE);
        bot.pressKey(KeyEvent.VK_END);
    }

    protected void addRelay() {
        open();
        bot.pressKey(KeyEvent.VK_D);
        bot.pressKey(KeyEvent.VK_TAB, 2);
        skipDevices();
        bot.pressKey(KeyEvent.VK_ENTER, 1 , ENTER_DELAY_STRENGTH);
        bot.pressKey(KeyEvent.VK_ESCAPE);
        bot.pressKey(KeyEvent.VK_END);
    }

    protected void updateTags(Zone zone) {
        try {
            Thread.sleep(DELAY);
            bot.pressKey(KeyEvent.VK_ENTER, 1, ENTER_DELAY_STRENGTH);
            bot.pasteText(zone.getTag1());
            bot.pressKey(KeyEvent.VK_ENTER, 1, ENTER_DELAY_STRENGTH);
            bot.pasteText(zone.getTag2());
            bot.pressKey(KeyEvent.VK_ENTER, 1, ENTER_DELAY_STRENGTH);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected void updateType(Zone zone) {
        try {
            Thread.sleep(DELAY);
            switch(zone.getType()) {
                case "Photo Detector":
                    bot.pressKey(KeyEvent.VK_A);
                    break;
                case "Alarm Input":
                    bot.pressKey(KeyEvent.VK_M);
                    bot.pressKey(KeyEvent.VK_A, 3);
                    break;
                case "Non-latched Supervisory":
                    bot.pressKey(KeyEvent.VK_N);
                    break;
                case "Latched Supervisory":
                    bot.pressKey(KeyEvent.VK_L);
                    break;
                case "Heat Detector":
                    bot.pressKey(KeyEvent.VK_M);
                    bot.pressKey(KeyEvent.VK_A, 3);
                    break;
                case "Blank Device":
                    bot.pressKey(KeyEvent.VK_N);
                    bot.pressKey(KeyEvent.VK_B, 2);
                    break;
                case "Relay":
                    bot.pressKey(KeyEvent.VK_R);
                    break;
            }
            bot.pressKey(KeyEvent.VK_ENTER, 1, ENTER_DELAY_STRENGTH);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected void updateRow(Zone zone) {
        updateTags(zone);
        updateType(zone);
        
        if(zone.isNS()) {
            bot.pressKey(KeyEvent.VK_N, 1, ENTER_DELAY_STRENGTH);
        }

        bot.pressKey(KeyEvent.VK_ENTER, 1 , ENTER_DELAY_STRENGTH);
        
        if(zone.isAR()) {
            bot.pressKey(KeyEvent.VK_A);
            bot.pressKey(KeyEvent.VK_ENTER, 1 , ENTER_DELAY_STRENGTH);
        }

        bot.pressKey(KeyEvent.VK_ESCAPE);
        bot.pressKey(KeyEvent.VK_DOWN);
    }

    protected void updateZone(Zone zone) {
        try {
            updateRow(zone);

            if (zone.getSubAddress() != null) {
                updateRow(zone.getSubAddress());
            } else if(zone.isDualInput() || zone.getType().equals("Relay")) {
                // add empty
                updateRow(new subZone(zone.getAddress()+0.1, "    Spare", zone.getTag2()));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        
    }

    protected void enterZoneList(ZoneList zone_list) {
        try {
            bot.pressKey(KeyEvent.VK_HOME, 1, 1); 
            for(Zone zone : zone_list.zones) {
                System.out.println("Updating: " + zone.getZoneinfo());
                if(!zone.getType().equals("Blank Device")) {
                    updateZone(zone);
                }
            }
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected void readSettings() {
        try{
            Ini ini;
            File ini_file = new File(SETTINGS_FILE);

            //Create settings file if doesn't exist
            if(!ini_file.exists()) {
                ini_file.createNewFile();
            }

            ini = new Ini(ini_file);

            //Add settings if doesn't exist - only checks if ini section exists, not keys
            if(!ini.containsKey("Key Delay")) {
                ini.add("Key Delay");
                ini.put("Key Delay", "delayTime", DELAY);
                ini.put("Key Delay", "enterDelayStrength", ENTER_DELAY_STRENGTH);
            }

            if(!ini.containsKey("Options")) {
                ini.add("Options");
                ini.put("Options", "bypassPause", BYPASS_PAUSE);
                ini.put("Options", "ignoreTagLength", IGNORE_TAG_LENGTH);
            }

            ini.store(ini_file);

            //Read settings
            DELAY = ini.get("Key Delay", "delayTime", int.class);
            ENTER_DELAY_STRENGTH = ini.get("Key Delay", "enterDelayStrength", double.class);
            BYPASS_PAUSE = ini.get("Options", "bypassPause", boolean.class);
            IGNORE_TAG_LENGTH = ini.get("Options", "ignoreTagLength", boolean.class);
            
        }catch(Exception e) {
            e.printStackTrace();
        }
    }

    //Check each zone to see if it meets the panel's requirements. Returns True if one incorrect device found
    protected boolean validateZones(ZoneList zone_list) {
        boolean invalid_found = false;
        boolean current_zone_valid;
        String zone_errors;
        ArrayList<Integer> usedZones = new ArrayList<>();
        
        //Add all addresses to check for duplicates later
        for(Zone zone :zone_list.zones) {
            usedZones.add((int) zone.getAddress());
        }

        for(Zone zone : zone_list.zones) {
            current_zone_valid = true;
            zone_errors = zone.getAddress() + " " + zone.getTag1() + " errors: ";

            //Check for duplicate addresses
            if(Collections.frequency(usedZones, (int) zone.getAddress()) > 1) {
                current_zone_valid = false;
                zone_errors += "duplicate address, ";
            }

            //Check address in valid range
            if ((int) zone.getAddress() < 1 || (int) zone.getAddress() > 240) {
                current_zone_valid = false;
                zone_errors += "address out of range, ";
            }

            //Check if tag1 is named correctly
            if(Zone.checkTags(zone.getTag1(), new String[] { "spare", "blank", "unknown" })) {
                current_zone_valid = false;
                zone_errors += "invalid tag 1, ";
            }

            //Check zone tag lengths
            if(zone.getTag1().length() > 20 && !IGNORE_TAG_LENGTH) {
                current_zone_valid = false;
                zone_errors += "tag 1 length > 20, ";
            }

            if(zone.getTag2().length() > 20 && !IGNORE_TAG_LENGTH) {
                current_zone_valid = false;
                zone_errors += "tag 2 length > 20, ";
            }
           
            if(zone.getSubAddress() != null) {
                //Check if subzone is spare, valve, or waterflow only
                if(!Zone.checkTags(zone.getSubAddress().getTag1(), new String[] { "spare", "valve", "waterfl", "valve", "tamper", "stat", "pump", "intake", "discharge",
                "jockey", "jocky", "bypass", "recall"})) {
                    current_zone_valid = false;
                    zone_errors += "invalid tag 1 name for subzone, ";
                }
                
                //Subzone tag 2 lengths
                if(zone.getSubAddress().getTag1().length() > 20 && !IGNORE_TAG_LENGTH) {
                    current_zone_valid = false;
                    zone_errors += "subzone tag 1 length > 20, ";
                }

                if(zone.getSubAddress().getTag2().length() > 20 && !IGNORE_TAG_LENGTH) {
                    current_zone_valid = false;
                    zone_errors += "subzone tag 2 length > 20, ";
                }

                //Check zone type if it is unknown or blank
                if(Zone.checkTags(zone.getType(), new String[] { "unknown", "blank"})) {
                    current_zone_valid = false;
                    zone_errors += "subzone unknown zone type, ";
                }

                /*
                //Check if zone address matches with sub address
                if((int) zone.getAddress() != (int) zone.getSubAddress().getAddress()) {
                    current_zone_valid = false;
                    zone_errors += "address and subaddress do not match, ";
                }*/
            }

            if (!current_zone_valid) {
                System.out.println(zone_errors);
                invalid_found = true;
            }
        }
        return invalid_found;
    }

    public void setIsRunning(boolean status) {
        is_running = status;

        //Set bot to null to prevent further inputs
        if(!is_running) {
            bot = null;
        }
    }

    public void setIsPaused(boolean status) {
        is_paused = status;
    }

    public boolean getIsPaused() {
        return is_paused;
    }
}
