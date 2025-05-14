import java.util.ArrayList;
import java.awt.event.KeyEvent;

public class Flexnet extends FX400{
    public void run() {
        System.out.println("Starting Flexnet Data Entry");
        try{
            is_running = true;
            is_paused = false;

            ZoneList zoneList = new ZoneList();
            zoneList.readFile();
            zoneList.displayZoneList();

            if(zoneList.CONTAINS_AR || zoneList.AP_START > 0) {
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

                        if (zoneList.AP_START > 0) {
                            System.out.println("AP Start to " + zoneList.AP_START);
                        }

                        System.out.println("Please make necessary changes and press F4 to continue.");
                        System.out.println("----------------------------------------------------------------");
                    }
                }

                while(is_paused){
                    Thread.sleep(DELAY); //Wait until start button pressed again
                }

                ArrayList<Zone> zones = zoneList.zones;
                Zone zone = zones.get(0);
                skip_count = (int) zone.getAddress() - 1;

                for(int current_zone = 0; current_zone < zones.size() && is_running; current_zone++) {

                    zone = zones.get(current_zone);

                    //Get difference of current and previous address, then -1
                    if(current_zone > 0){
                        if(isSmokeHeat(zone)) {
                            skip_count += (int) zone.getAddress() - (int) zones.get(current_zone - 1).getAddress() - 1 - zoneList.AP_START;
                        }
                        else {  
                            //Assuming zone list is sorted, reset skip count once ipt/relay devices are reached
                            if(isSmokeHeat(zones.get(current_zone - 1)) && !isSmokeHeat(zone)) {
                                skip_count = 0;
                            } 
                            else {                         
                                skip_count += ((int) zone.getAddress() - 100) - zoneList.AP_START - ((int) zones.get(current_zone - 1).getAddress() - 100) - zoneList.AP_START - 1;
                            }
                        }
                    }

                    System.out.println("Inserting: " + zone.getZoneinfo());

                    switch (zone.getType()) {
                        case "Photo Detector":
                            addPhotoDetector();
                            break;
                        case "Alarm Input":
                            addAlarmInputMod(); 
                            break;
                        case "Alarm Input Class A":
                            addAlarmInputMiniMod();                         
                            break;
                        case "Non-latched Supervisory":
                            if(Zone.checkTags(zone.getTag1(), new String[] { "radio"})) {
                                addNonLatchedSupvMini();
                            } else {
                                addNonLatchedSupv();
                            }
                            break;
                        case "Latched Supervisory":
                            addLatchedSupv();
                            break;
                        case "Heat": 
                            if(zone.isDualInput()) {
                                addDualHeatSmokeDetector();
                            }
                            else {
                                addHeatDetector();
                            }
                            break;
                        case "Relay":
                            addRelay();
                            break;
                    }
                }

                if(is_running){
                    enterZoneList(zoneList);
                }

                System.out.println("Flexnet Entry Complete");
                is_running = false;
            }
            else {
                System.out.println("Flexnet entry did not run");
            }
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }
    
    protected void addPhotoDetector(){
        open();
        bot.pressKey(KeyEvent.VK_TAB, 4);
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
        bot.pressKey(KeyEvent.VK_D, 5);
        bot.pressKey(KeyEvent.VK_TAB, 2);
        bot.pressKey(KeyEvent.VK_N);
        bot.pressKey(KeyEvent.VK_TAB, 2);
        skipDevices();
        bot.pressKey(KeyEvent.VK_ENTER, 1 , ENTER_DELAY_STRENGTH);
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

    protected void addNonLatchedSupv(){
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

        protected void addNonLatchedSupvMini(){
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

    protected void addLatchedSupv(){
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

    protected void addHeatDetector(){
        open();
        bot.pressKey(KeyEvent.VK_H, 2);
        bot.pressKey(KeyEvent.VK_TAB, 5);
        skipDevices();
        bot.pressKey(KeyEvent.VK_ENTER, 1 , ENTER_DELAY_STRENGTH);
        bot.pressKey(KeyEvent.VK_ESCAPE);
        bot.pressKey(KeyEvent.VK_END);
    }

    protected void addDualHeatSmokeDetector(){
        open();
        bot.pressKey(KeyEvent.VK_D, 6);
        bot.pressKey(KeyEvent.VK_TAB, 5);
        skipDevices();
        bot.pressKey(KeyEvent.VK_ENTER, 1 , ENTER_DELAY_STRENGTH);
        bot.pressKey(KeyEvent.VK_ESCAPE);
        bot.pressKey(KeyEvent.VK_END);
    }

    protected void addRelay(){
        open();
        bot.pressKey(KeyEvent.VK_R, 2);
        bot.pressKey(KeyEvent.VK_TAB, 4);
        skipDevices();
        bot.pressKey(KeyEvent.VK_ENTER, 1 , ENTER_DELAY_STRENGTH);
        bot.pressKey(KeyEvent.VK_ESCAPE);
        bot.pressKey(KeyEvent.VK_END);
    }

    /*
    protected void addDualSmokeFireCODetector(){
        open();

    }
     
    protected void addFirephone(){
        open();

    }
     
    
    protected void addSounder(){

    }
     */

    protected void updateType(Zone zone){
        try {
            Thread.sleep(DELAY);
            switch(zone.getType()){
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
                    bot.pressKey(KeyEvent.VK_A);
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

    protected void updateRow(Zone zone){
        updateTags(zone);
        updateType(zone);
        
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
            if(zone.isDualInput()) { 
                if(zone.getType().equals("Heat")) {
                    updateRow(zone.getSubAddress());
                    updateRow(zone.getSubAddress());
                }
                else {
                    updateRow(zone.getSubAddress());
                }
            }

            //can have up to 3 sub zones, but only waterflow/valves are tracked
            //has the 1-159 smokeheat and (100 + 1-159) convention from fx2000
            //also has to consider ap start but it doesnt matter for entry
            
            /*
             if (zone.getSubAddress() != null){
                updateRow(zone.getSubAddress());
            } else if(zone.isDualInput() || zone.getType().equals("Relay")) {
                // add empty
                updateRow(new subZone(zone.getAddress()+0.1, "    Spare", zone.getTag2()));
                
            }
             */
        } catch (Exception e) {
            e.printStackTrace();
        }    
    }

    //Check each zone to see if it meets the panel's requirements. Returns True if one incorrect device found
    protected boolean validateZones(ZoneList zoneList) {
        boolean invalid_found = false;
        boolean current_zone_valid;
        String zone_errors;
        ArrayList<Integer> usedZones = new ArrayList<>();
        
        //Add all addresses to check for duplicates later
        for(Zone zone :zoneList.zones) {
            usedZones.add((int) zone.getAddress());
        }

        /*
        for(Zone zone : zoneList.zones) {
            
            if (!current_zone_valid) {
                System.out.println(zone_errors);
                invalid_found = true;
            }
        }
        */
        return invalid_found;
    }

    private boolean isSmokeHeat(Zone zone) {
        return Zone.checkTags(zone.getType(), new String[] { "photo", "heat"});
    }
}