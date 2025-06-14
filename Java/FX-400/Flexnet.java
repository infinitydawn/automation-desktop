import java.util.ArrayList;
import java.util.Collections;
import java.awt.event.KeyEvent;

public class Flexnet extends FX2000{

    private ArrayList<Zone> phones = new ArrayList<>();
    private int AP_START = 1;

    public void run() {
        System.out.println("Starting Flexnet Data Entry");
        try{
            is_running = true;
            is_paused = false;

            ZoneList zone_list = new ZoneList();
            zone_list.readFile();
            zone_list.displayZoneList();
            AP_START = zone_list.AP_START;

            if(zone_list.CONTAINS_AR || AP_START > 1 || zone_list.CONTAINS_DUAL_HEAT) {
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

                        if (zone_list.CONTAINS_DUAL_HEAT) {
                            System.out.println("100F Dual Heat detector enabled");
                        }

                        if(zone_list.CONTAINS_AR) {
                            System.out.println("Auxiliary Reset in Base Control/Annun. Idx 3");
                        }

                        if (zone_list.AP_START > 1) {
                            System.out.println("AP Start to " + AP_START);
                        }

                        System.out.println("Please make necessary changes and press F4 to continue.");
                        System.out.println("----------------------------------------------------------------");
                    }
                }

                while(is_paused) {
                    Thread.sleep(DELAY); //Wait until start button pressed again
                }

                Zone zone;

                if(!phones.isEmpty()) {
                    zone = phones.get(0);
                    skip_count = (int) zone.getAddress() - 100 - 1;

                    for (int current_zone = 0; current_zone < phones.size() && is_running; current_zone++) {
                        if(current_zone > 0) {
                            skip_count += (int) zone.getAddress() - (int) phones.get(current_zone - 1).getAddress() - 1;
                        }
                        System.out.println("Inserting: " + zone.getZoneinfo());
                        addTelephoneModule(); 
                    }
                }

                ArrayList<Zone> zones = zone_list.zones;
                zone = zones.get(0);
                skip_count = (int) zone.getAddress() - AP_START;

                //Reduce skip count if first address in zone list is ap mod
                if(!zone.isSensor()) {
                    skip_count -= 100;
                }

                for(int current_zone = 0; current_zone < zones.size() && is_running; current_zone++) {

                    zone = zones.get(current_zone);

                    //Get difference of current and previous address, then -1. Skip telephone mods
                    if(current_zone > 0 && !zone.getType().equals("Telephone Module")) {
                        if(zone.isSensor()) {
                            skip_count += (int) zone.getAddress() - (int) zones.get(current_zone - 1).getAddress() - 1;
                        }
                        else {  
                            //Assuming zone list is sorted, reset skip count once ap devices are reached
                            if(zones.get(current_zone - 1).isSensor() && !zone.isSensor()) {
                                skip_count = (int) zone.getAddress() - AP_START - 100;
                            } 
                            else {
                                skip_count += ((int) zone.getAddress() - 100) - ((int) zones.get(current_zone - 1).getAddress() - 100) - 1;
                            }
                        }

                    }

                    System.out.println("Inserting: " + zone.getZoneinfo());

                    switch (zone.getType()) {
                        case "Photo Detector":
                            if (zone.isDualInput()) {
                                zone.setTag1("Smoke Detector");
                                addSmokeCODetector();
                            }
                            else {
                                addPhotoDetector();
                            }
                            break;
                        case "Alarm Input":
                            if(zone.getSubAddress() != null) {
                                addDualAlarmInputMod();
                            }
                            else {
                                addAlarmInputMod(); 
                            }                          
                            break;
                        case "Alarm Input Class A":
                            addAlarmInputMiniMod();                         
                            break;
                        case "Non-latched Supervisory":
                        //Check for radio, single monitor and dual monitor
                            if(Zone.checkTags(zone.getTag1(), new String[] { "radio" })) {
                                addNonLatchedSupvMini();
                            } else {
                                if(zone.getSubAddress() != null || Zone.checkTags(zone.getTag1(), new String[] { "generator", "dry sys" })) {
                                    addDualNonLatchedSupv();
                                } 
                                else {
                                    addNonLatchedSupv();
                                }    
                            }
                            break;
                        case "Latched Supervisory":
                            addLatchedSupv();
                            break;
                        case "Heat Detector": 
                            if(zone.isDualInput()) {
                                addDualHeatSmokeDetector();
                                zone.setTag1("Smoke Detector"); //Rename Dual Heat to Smoke Detector
                            }
                            else {
                                addHeatDetector();
                            }
                            break;
                        case "Relay":
                            addRelay();
                            break;
                        case "Telephone Module":
                            //addTelephoneModule();
                            break;
                        case "Speakers":
                            addSpeakers();
                            break;
                    }
                }

                if(is_running) {
                    enterZoneList(zone_list);
                }

                System.out.println("Flexnet Entry Complete");
                is_running = false;
            }
            else {
                System.out.println("Flexnet entry did not run");
            }
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }
    
    protected void addPhotoDetector() {
        open();
        bot.pressKey(KeyEvent.VK_P);
        bot.pressKey(KeyEvent.VK_TAB, 3);
        bot.pressKey(KeyEvent.VK_N);
        bot.pressKey(KeyEvent.VK_TAB, 2);
        skipDevices();
        bot.pressKey(KeyEvent.VK_ENTER, 1 , ENTER_DELAY_STRENGTH);
        bot.pressKey(KeyEvent.VK_ESCAPE);
        bot.pressKey(KeyEvent.VK_END);
    }

    protected void addAlarmInputMod() {
        open();
        bot.pressKey(KeyEvent.VK_M);
        bot.pressKey(KeyEvent.VK_TAB, 2);
        bot.pressKey(KeyEvent.VK_N);
        bot.pressKey(KeyEvent.VK_TAB, 2);
        skipDevices();
        bot.pressKey(KeyEvent.VK_ENTER, 1 , ENTER_DELAY_STRENGTH);
        bot.pressKey(KeyEvent.VK_ESCAPE);
        bot.pressKey(KeyEvent.VK_END);
    }

    //Meant for Monitor only waterflow/valve
    protected void addDualAlarmInputMod() {
        open();
        if(AP_START > 1) {
            bot.pressKey(KeyEvent.VK_D, 5);
        } 
        else {
            bot.pressKey(KeyEvent.VK_D, 3);
        }

        bot.pressKey(KeyEvent.VK_TAB, 2);
        bot.pressKey(KeyEvent.VK_N);
        bot.pressKey(KeyEvent.VK_TAB, 2);
        skipDevices();
        bot.pressKey(KeyEvent.VK_ENTER, 1 , Math.max(ENTER_DELAY_STRENGTH, 1.5));
        bot.pressKey(KeyEvent.VK_ESCAPE);
        bot.pressKey(KeyEvent.VK_END);
    }

    protected void addAlarmInputMiniMod() {
        open();
        bot.pressKey(KeyEvent.VK_M, 2);
        bot.pressKey(KeyEvent.VK_TAB, 2);
        bot.pressKey(KeyEvent.VK_N);
        bot.pressKey(KeyEvent.VK_TAB, 2);
        skipDevices();
        bot.pressKey(KeyEvent.VK_ENTER, 1 , ENTER_DELAY_STRENGTH);
        bot.pressKey(KeyEvent.VK_ESCAPE);
        bot.pressKey(KeyEvent.VK_END);
    }

    protected void addNonLatchedSupv() {
        open();
        bot.pressKey(KeyEvent.VK_M);
        bot.pressKey(KeyEvent.VK_TAB);
        bot.pressKey(KeyEvent.VK_N);
        bot.pressKey(KeyEvent.VK_TAB);
        bot.pressKey(KeyEvent.VK_N);
        bot.pressKey(KeyEvent.VK_TAB, 2);
        skipDevices();
        bot.pressKey(KeyEvent.VK_ENTER, 1 , ENTER_DELAY_STRENGTH);
        bot.pressKey(KeyEvent.VK_ESCAPE);
        bot.pressKey(KeyEvent.VK_END);
    }

    protected void addNonLatchedSupvMini() {
        open();
        bot.pressKey(KeyEvent.VK_M, 2);
        bot.pressKey(KeyEvent.VK_TAB);
        bot.pressKey(KeyEvent.VK_N);
        bot.pressKey(KeyEvent.VK_TAB);
        bot.pressKey(KeyEvent.VK_N);
        bot.pressKey(KeyEvent.VK_TAB, 2);
        skipDevices();
        bot.pressKey(KeyEvent.VK_ENTER, 1 , ENTER_DELAY_STRENGTH);
        bot.pressKey(KeyEvent.VK_ESCAPE);
        bot.pressKey(KeyEvent.VK_END);
    }

    protected void addDualNonLatchedSupv() {
        open();
        if(AP_START > 1) {
            bot.pressKey(KeyEvent.VK_D, 5);
        }
        else {
            bot.pressKey(KeyEvent.VK_D, 3);
        }
        
        bot.pressKey(KeyEvent.VK_TAB);
        bot.pressKey(KeyEvent.VK_N);
        bot.pressKey(KeyEvent.VK_TAB);
        bot.pressKey(KeyEvent.VK_N);
        bot.pressKey(KeyEvent.VK_TAB, 2);
        skipDevices();
        bot.pressKey(KeyEvent.VK_ENTER, 1 , Math.max(ENTER_DELAY_STRENGTH, 1.5));
        bot.pressKey(KeyEvent.VK_ESCAPE);
        bot.pressKey(KeyEvent.VK_END);
    }

    protected void addLatchedSupv() {
        open();
        bot.pressKey(KeyEvent.VK_M, 2);
        bot.pressKey(KeyEvent.VK_TAB);
        bot.pressKey(KeyEvent.VK_L);
        bot.pressKey(KeyEvent.VK_TAB);
        bot.pressKey(KeyEvent.VK_N);
        bot.pressKey(KeyEvent.VK_TAB, 2);
        skipDevices();
        bot.pressKey(KeyEvent.VK_ENTER, 1 , ENTER_DELAY_STRENGTH);
        bot.pressKey(KeyEvent.VK_ESCAPE);
        bot.pressKey(KeyEvent.VK_END);
    }

    protected void addHeatDetector() {
        open();
        if(AP_START > 1) {
            bot.pressKey(KeyEvent.VK_H, 2);
        }
        else {
            bot.pressKey(KeyEvent.VK_H);
        }
        
        bot.pressKey(KeyEvent.VK_TAB, 5);
        skipDevices();
        bot.pressKey(KeyEvent.VK_ENTER, 1 , ENTER_DELAY_STRENGTH);
        bot.pressKey(KeyEvent.VK_ESCAPE);
        bot.pressKey(KeyEvent.VK_END);
    }

    protected void addDualHeatSmokeDetector() {
        open();
        if(AP_START > 1) {
            bot.pressKey(KeyEvent.VK_D, 6);
        }
        else {
            bot.pressKey(KeyEvent.VK_D, 4);
        }
        bot.pressKey(KeyEvent.VK_TAB, 5);
        skipDevices();
        bot.pressKey(KeyEvent.VK_ENTER, 1 , Math.max(ENTER_DELAY_STRENGTH, 2)); //needs extra time for three address devices
        bot.pressKey(KeyEvent.VK_ESCAPE);
        bot.pressKey(KeyEvent.VK_END);
    }

    protected void addRelay() {
        open();
        if(AP_START > 1) {
            bot.pressKey(KeyEvent.VK_R, 2);
        }
        else {
            bot.pressKey(KeyEvent.VK_R);
        }
        bot.pressKey(KeyEvent.VK_TAB, 4);
        skipDevices();
        bot.pressKey(KeyEvent.VK_ENTER, 1 , Math.max(ENTER_DELAY_STRENGTH, 1.5));
        bot.pressKey(KeyEvent.VK_ESCAPE);
        bot.pressKey(KeyEvent.VK_END);
    }

    public void addTelephoneModule() {
        open();
        bot.pressKey(KeyEvent.VK_F);
        bot.pressKey(KeyEvent.VK_TAB, 3);
        skipDevices();
        bot.pressKey(KeyEvent.VK_ENTER, 1, ENTER_DELAY_STRENGTH);
        bot.pressKey(KeyEvent.VK_ESCAPE);
        bot.pressKey(KeyEvent.VK_END);
    }

    public void addSpeakers() {
        open();
        bot.pressKey(KeyEvent.VK_C, 2);
        bot.pressKey(KeyEvent.VK_TAB, 4);
        skipDevices();
        bot.pressKey(KeyEvent.VK_ENTER, 1, ENTER_DELAY_STRENGTH);
        bot.pressKey(KeyEvent.VK_ESCAPE);
        bot.pressKey(KeyEvent.VK_END);
    }

    protected void addSmokeCODetector() {
        open();
        bot.pressKey(KeyEvent.VK_F, 2);
        bot.pressKey(KeyEvent.VK_TAB);
        bot.pressKey(KeyEvent.VK_A);
        bot.pressKey(KeyEvent.VK_TAB, 4);
        skipDevices();
        bot.pressKey(KeyEvent.VK_ENTER, 1, Math.max(ENTER_DELAY_STRENGTH, 2));
        bot.pressKey(KeyEvent.VK_ESCAPE);
        bot.pressKey(KeyEvent.VK_END);
    }

    protected void enterZoneList(ZoneList zone_list) {
        try {
            bot.pressKey(KeyEvent.VK_HOME, 1, 1); 

            //Update phones first since they go at the very top
            for(Zone zone : phones) {
                System.out.println("Updating: " + zone.getZoneinfo());
                updateZone(zone);
            }

            for(Zone zone : zone_list.zones) {
                if(!zone.getType().equals("Blank Device") && !zone.getType().equals("Telephone Module")) {
                    System.out.println("Updating: " + zone.getZoneinfo());
                    updateZone(zone);
                }
            }
            
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
                    bot.pressKey(KeyEvent.VK_A, 8);
                    break;
                case "Non-latched Supervisory":
                    bot.pressKey(KeyEvent.VK_N);
                    break;
                case "Latched Supervisory":
                    bot.pressKey(KeyEvent.VK_L);
                    break;
                case "Heat Detector":
                    bot.pressKey(KeyEvent.VK_A);
                    break;
                case "Blank Device":
                    bot.pressKey(KeyEvent.VK_N);
                    bot.pressKey(KeyEvent.VK_B, 2);
                    break;
                case "Relay":
                    bot.pressKey(KeyEvent.VK_R);
                    break;
                case "Telephone Module":
                case "Speakers":
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
            if(zone.isDualInput()) { 
                //Dual heat/smoke
                if(zone.getType().equals("Heat Detector")) {
                    updateRow(new subZone(zone.getAddress()+0.1, "Low Heat Detector", zone.getTag2()));
                    updateRow(new subZone(zone.getAddress()+0.2, "Heat Detector 135°F", zone.getTag2()));
                }

                //Smoke/co combo + sounder
                if(zone.getType().equals("Photo Detector")) {
                    updateRow(new subZone(zone.getAddress()+0.1, "CO Detector", zone.getTag2()));
                    updateRow(new subZone(zone.getAddress()+0.2, "Sounder Base", zone.getTag2()));
                }
                else {
                    if(zone.getSubAddress() != null) {
                        updateRow(zone.getSubAddress());
                    }   
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }    
    }

    //Check each zone to see if it meets the panel's requirements. Returns True if one incorrect device found
    protected boolean validateZones(ZoneList zone_list) {
        boolean invalid_found = false;
        boolean current_zone_valid;
        String zone_errors;
        ArrayList<Integer> used_Phone_Zones = new ArrayList<>(); //phone addresses
        ArrayList<Integer> usedZones = new ArrayList<>(); //smoke/heat addresses
        ArrayList<Integer> used100Zones = new ArrayList<>(); //ap mod addresses
        
        //Add to respective arrays for organized inserting and duplication checking
        for(Zone zone :zone_list.zones) {     
            if(zone.isSensor()) {
                usedZones.add((int) zone.getAddress());
            } 
            else if(zone.getType().equals("Telephone Module")) {
                used_Phone_Zones.add((int) zone.getAddress());
                phones.add(zone);
            }
            else {
                used100Zones.add((int) zone.getAddress());
            }
        }

        for(Zone zone : zone_list.zones) {
            current_zone_valid = true;
            zone_errors = zone.getAddress() + " " + zone.getTag1() + " errors: ";

            //Check zone type if it is unknown or blank
            //Check address in valid range
            //Smoke, heat: AP_START - 159
            //AP mod: 100 + AP_START - 259
            if(Zone.checkTags(zone.getType(), new String[] { "unknown", "blank"})) {
                current_zone_valid = false;
                zone_errors += "unknown zone type, ";
            }
            else if(zone.isSensor()) {
                if((zone.getAddress() < AP_START || zone.getAddress() > 159 )) {
                    current_zone_valid = false;
                    zone_errors += "address out of range for smoke/heat, ";
                }

                //Check for duplicate addresses 
                if(Collections.frequency(usedZones, (int) zone.getAddress()) > 1) {
                    current_zone_valid = false;
                    zone_errors += "duplicate smoke/heat address, ";
                }
            } 
            else if(zone.getType().equals("Telephone Module")) {
                if ((int) zone.getAddress() < 101 && (int) zone.getAddress() > 99 + AP_START) {
                    current_zone_valid = false;
                    zone_errors += "address out of range for telephone mod, ";
                }

                if(Collections.frequency(used_Phone_Zones, (int) zone.getAddress()) > 1) {
                    current_zone_valid = false;
                    zone_errors += "duplicate telephone address, ";
                }
            }
            else {
                if((int) zone.getAddress() < 100 + AP_START || (int) zone.getAddress() > 259) {
                    current_zone_valid = false;
                    zone_errors += "address out of range for ap module, ";
                }

                //Check for duplicate addresses 
                if(Collections.frequency(used100Zones, (int) zone.getAddress()) > 1) {
                    current_zone_valid = false;
                    zone_errors += "duplicate ap module address, ";
                }
            }

            //Check tag lengths
            if(zone.getTag1().length() > 20 && !IGNORE_TAG_LENGTH) {
                current_zone_valid = false;
                zone_errors += "tag 1 length > 20, ";
            }

            if(zone.getTag2().length() > 20 && !IGNORE_TAG_LENGTH) {
                current_zone_valid = false;
                zone_errors += "tag 2 length > 20, ";
            }

            if(zone.getSubAddress() != null) {

                /*
                //Check if subzone is valve or waterflow only -- needs to be able to check for high/low air dry sys
                if(!Zone.checkTags(zone.getSubAddress().getTag1(), new String[] { "valve", "waterfl", "valve", "tamper", "stat", "pump", "intake", "discharge",
                "jockey", "jocky", "bypass", "recall"})) {
                    current_zone_valid = false;
                    zone_errors += "invalid tag 1 name for subzone, ";
                }
                */

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
            }

            if (!current_zone_valid) {
                System.out.println(zone_errors);
                invalid_found = true;
            }
        }
        return invalid_found;
    }
}
