import java.util.ArrayList;
import java.util.Collections;
import java.awt.event.KeyEvent;

public class FX2000 extends FX400{

    public void run() {
        System.out.println("Starting FX2000 Data Entry");
        try{
            is_running = true;
            is_paused = false;

            ZoneList zoneList = new ZoneList();
            zoneList.readFile();
            zoneList.displayZoneList();

            if(zoneList.CONTAINS_AR) {
                is_paused = true;
            }

            System.out.println("----------------------------------------------------------------");
            if (validateZones(zoneList)) {
                is_running = false;
                System.out.println("----------------------------------------------------------------");
                System.out.println("Errors found in zone list, please correct them and run again.");
                System.out.println("----------------------------------------------------------------");
            }

            if(is_running) {

                if(is_paused){
                    if(BYPASS_PAUSE){
                        is_paused = false;
                    }else{
                        System.out.println("The following settings need to be enabled for data entry:");
                        if(zoneList.CONTAINS_AR) {
                            System.out.println("Auxiliary Reset in Base Control/Annun. Idx 3");
                        }
                        System.out.println("Please make necessary changes and press F2 to continue.");
                        System.out.println("----------------------------------------------------------------");
                    }
                }

                while(is_paused){
                    Thread.sleep(DELAY); //Wait until start button pressed again
                }

                ArrayList<Zone> zones = zoneList.zones;
                Zone zone = zones.get(0);
                skip_count = (int) zone.getAddress() - 1;

                //Reduce skip count if first address in zone list is ipt/relay
                if(!isSmokeHeat(zone)) {
                    skip_count -= 100;
                }

                for(int current_zone = 0; current_zone < zones.size() && is_running; current_zone++) {

                    zone = zones.get(current_zone);
                    
                    //Get difference of current and previous address, then -1
                    if(current_zone > 0){
                        if(isSmokeHeat(zone)) {
                            skip_count += (int) zone.getAddress() - (int) zones.get(current_zone - 1).getAddress() - 1;
                        }
                        else {  
                            //Assuming zone list is sorted, reset skip count once ipt/relay devices are reached
                            if(isSmokeHeat(zones.get(current_zone - 1)) && !isSmokeHeat(zone)) {
                                skip_count = 0;
                            } 
                            else {                         
                                skip_count += ((int) zone.getAddress() - 100) - ((int) zones.get(current_zone - 1).getAddress() - 100) - 1;
                            }
                        }
                    }

                    System.out.println("Inserting: " + zone.getZoneinfo());

                    switch (zone.getType()) {
                        case "Photo Detector":
                            addPhotoDetector();
                            break;
                        case "Alarm Input":
                        case "Alarm Input Class A":
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
                        case "Relay":
                            addRelay();
                            break;
                    }
                }

                if(is_running){
                    enterZoneList(zoneList);
                }

                System.out.println("FX2000 Entry Complete");
                is_running = false;
            }
            else {
                System.out.println("FX2000 Entry did not run");
            }
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }
    
    protected void addPhotoDetector(){
        open();
        bot.pressKey(KeyEvent.VK_TAB, 2);
        skipDevices();
        bot.pressKey(KeyEvent.VK_ENTER, 1 , ENTER_DELAY_STRENGTH);
        bot.pressKey(KeyEvent.VK_ESCAPE);
        bot.pressKey(KeyEvent.VK_END);
    }

    protected void addAlarmInputMod(){
        open();
        bot.pressKey(KeyEvent.VK_I);
        bot.pressKey(KeyEvent.VK_TAB, 2);
        skipDevices();
        bot.pressKey(KeyEvent.VK_ENTER, 1 , ENTER_DELAY_STRENGTH);
        bot.pressKey(KeyEvent.VK_ESCAPE);
        bot.pressKey(KeyEvent.VK_END);
    }

    protected void addNonLatchedSupv(){
        open();
        bot.pressKey(KeyEvent.VK_I);
        bot.pressKey(KeyEvent.VK_TAB);
        bot.pressKey(KeyEvent.VK_N, 2);
        bot.pressKey(KeyEvent.VK_TAB);
        skipDevices();
        bot.pressKey(KeyEvent.VK_ENTER, 1 , ENTER_DELAY_STRENGTH);
        bot.pressKey(KeyEvent.VK_ESCAPE);
        bot.pressKey(KeyEvent.VK_END);
    }

    protected void addLatchedSupv(){
        open();
        bot.pressKey(KeyEvent.VK_I);
        bot.pressKey(KeyEvent.VK_TAB);
        bot.pressKey(KeyEvent.VK_L);
        bot.pressKey(KeyEvent.VK_TAB);
        skipDevices();
        bot.pressKey(KeyEvent.VK_ENTER, 1 , ENTER_DELAY_STRENGTH);
        bot.pressKey(KeyEvent.VK_ESCAPE);
        bot.pressKey(KeyEvent.VK_END);
    }

    protected void addHeatDetector(){
        open();
        bot.pressKey(KeyEvent.VK_H);
        bot.pressKey(KeyEvent.VK_TAB,2);
        skipDevices();
        bot.pressKey(KeyEvent.VK_ENTER, 1 , ENTER_DELAY_STRENGTH);
        bot.pressKey(KeyEvent.VK_ESCAPE);
        bot.pressKey(KeyEvent.VK_END);
    }

    protected void addRelay(){
        open();
        bot.pressKey(KeyEvent.VK_R);
        bot.pressKey(KeyEvent.VK_TAB, 2);
        skipDevices();
        bot.pressKey(KeyEvent.VK_ENTER, 1 , ENTER_DELAY_STRENGTH);
        bot.pressKey(KeyEvent.VK_ESCAPE);
        bot.pressKey(KeyEvent.VK_END);
    }

    protected void updateRow(Zone zone){
        updateTags(zone);

        bot.pressKey(KeyEvent.VK_ENTER, 1 , ENTER_DELAY_STRENGTH); //make up for not updating Type
        
        if(zone.isNS()){
            bot.pressKey(KeyEvent.VK_N, 1, ENTER_DELAY_STRENGTH);
        }

        bot.pressKey(KeyEvent.VK_ENTER, 1 , ENTER_DELAY_STRENGTH);

        if(zone.isAR()){
            bot.pressKey(KeyEvent.VK_A);
            bot.pressKey(KeyEvent.VK_ENTER, 1 , ENTER_DELAY_STRENGTH);
        }
        bot.pressKey(KeyEvent.VK_ESCAPE);
        bot.pressKey(KeyEvent.VK_DOWN);
    }

    protected void updateZone(Zone zone){
        try {
            updateRow(zone);
        } catch (Exception e) {
            e.printStackTrace();
        }    
    }

    protected boolean validateZones(ZoneList zoneList) {
        boolean invalid_found = false;
        boolean current_zone_valid;
        String zone_errors;
        ArrayList<Integer> usedZones = new ArrayList<>(); //smoke/heat addresses
        ArrayList<Integer> used100Zones = new ArrayList<>(); //ipt/relay addresses
        
        //Add all addresses to check for duplicates later
        for(Zone zone :zoneList.zones) {     
            if(isSmokeHeat(zone)) {
                usedZones.add((int) zone.getAddress());
            } 
            else {
                used100Zones.add((int) zone.getAddress());
            }
        }

        for(Zone zone : zoneList.zones) {
            current_zone_valid = true;
            zone_errors = zone.getAddress() + " " + zone.getTag1() + " errors: ";

            //Check zone type if it is unknown or blank
            //Check address in valid range
            //Smoke, heat: 1 - 99
            //Ipt, Relay: 101 - 199
            if(Zone.checkTags(zone.getType(), new String[] { "unknown", "blank"})) {
                current_zone_valid = false;
                zone_errors += "unknown zone type, ";
            }
            else if(isSmokeHeat(zone)) {
                if((zone.getAddress() < 0 || zone.getAddress() > 99)) {
                    current_zone_valid = false;
                    zone_errors += "address out of range for smoke/heat, ";
                }

                //Check for duplicate addresses 
                if(Collections.frequency(usedZones, (int) zone.getAddress()) > 1) {
                    current_zone_valid = false;
                    zone_errors += "duplicate smoke/heat address, ";
                }

            } 
            else {
                if((int) zone.getAddress() < 101 || (int) zone.getAddress() > 199) {
                    current_zone_valid = false;
                    zone_errors += "address out of range for ipt/relay, ";
                }

                //Check for duplicate addresses 
                if(Collections.frequency(used100Zones, (int) zone.getAddress()) > 1) {
                    current_zone_valid = false;
                    zone_errors += "duplicate ipt/relay address, ";
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

            if (!current_zone_valid) {
                System.out.println(zone_errors);
                invalid_found = true;
            }
        }
        return invalid_found;
    }

    private boolean isSmokeHeat(Zone zone) {
        return Zone.checkTags(zone.getType(), new String[] { "photo", "heat"});
    }
}
