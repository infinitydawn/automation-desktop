import java.util.ArrayList;
import java.awt.event.KeyEvent;
import java.io.File;

import org.ini4j.Ini;


public class FX400 extends Thread{

    protected int DELAY = 200; //Default 200. Delay time for everything. Multiply by delay strength to change length
    protected double TAG_DELAY_STRENGTH = 0; //Default 0. Delay when entering tag letters
    protected double ENTER_DELAY_STRENGTH = 1; // Default 1. Delay after pressing Enter (Writes to database. Larger databases may want this higher)
    //protected double DROPDOWN_DELAY_STRENGTH = 0; // Default 0. Delay when scrolling through dropdown menus. UNUSED
    protected boolean BYPASS_PAUSE = false; //Prevents the AR prompt from showing
    private String SETTINGS_FILE = "settings.ini";

    protected DataEntryBot bot;
    protected int skip_count = 19;
    protected boolean is_running = false; //used to stop the bot from running without closing process
    protected boolean is_paused = false; //used to prompt user to enable AR related settings -- MAKE USE OF THIS IF PAUSING GUI

    public FX400(){
        try {
            readSettings();
            bot = new DataEntryBot(DELAY);           
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void run() {
        try{
            is_running = true;

            ReadTempArray reader = new ReadTempArray();
            String[][] zoneParts = reader.readFile(); 

            ZoneList zoneList = new ZoneList();

            String[] addresses = zoneParts[0];
            String[] tags1 = zoneParts[1];
            String[] tags2 = zoneParts[2];

            is_paused = false;
            for (int i = 0; i < addresses.length; i++) {
                System.out.println(" - - - - -  + " + tags1[i]);
                zoneList.addZone(Double.parseDouble(addresses[i]), tags1[i], tags2[i]);

                if(Zone.checkTags(tags1[i], new String[] {"shutdown", "shut down"})){
                    is_paused = true;
                }
            }
            
            zoneList.displayZoneList();

            if(is_paused){
                if(BYPASS_PAUSE){
                    is_paused = false;
                }else{
                    System.out.println("AR related device discovered, please enable then press F2 to continue.");
                }
            }

            while(is_paused){
                Thread.sleep(Math.max(100,DELAY)); //Wait until start button pressed again
            }

            ArrayList<Zone> zones = zoneList.zones;
            Zone zone = zones.get(0);
            skip_count = (int) zone.getAddress() - 1;

            for(int current_zone = 0; current_zone < zones.size() && is_running; current_zone++) {

                zone = zones.get(current_zone);
                //Get difference of current and previous address, then -1
                if(current_zone > 0){
                    skip_count += (int) zone.getAddress() - (int) zones.get(current_zone - 1).getAddress() - 1;
                }

                System.out.println("Checking: " + zone.getZoneinfo());

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

            if(is_running){
                enterZoneList(zoneList);
            }
            // zoneList.addZone(1.1, "Waterflow ");
            // zoneList.addZone(2.1, "valve ");
            // zoneList.addZone(2.2, "discharge ");
            // zoneList.addZone(3.1, "Damper ");
            // zoneList.addZone(4.1, "jocky ");
            // zoneList.addZone(5.1, "duct ");
            // zoneList.addZone(5.2, "bypass ");
            // zoneList.addZone(20, "waterflow");
            // zoneList.addZone(21, "smoke");

            // if (Zone.classify("smoke").equals("Photo Detector")) {
            // bot.addPhotoDetector();
            // }

            // System.out.println(Zone.classify("Waterflow "));
            // System.out.println(Zone.classify("valve "));
            // System.out.println(Zone.classify("smoke "));
            // System.out.println(Zone.classify("Damper "));
            // System.out.println(Zone.classify("jocky "));
            // System.out.println(Zone.classify("duct "));
            // System.out.println(Zone.classify("bypass "));

            // //Simulate typing "Hello, World!"
            // bot.keyPress(KeyEvent.VK_H);
            // bot.keyRelease(KeyEvent.VK_H);
            // bot.keyPress(KeyEvent.VK_E);
            // bot.keyRelease(KeyEvent.VK_E);
            // // ... (and so on)
            System.out.println("FX400 Entry Complete");
            is_running = false;
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }

    public void skipDevices(){
        bot.pressKey(KeyEvent.VK_RIGHT, skip_count);
    }

    public void open(){
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
    
    public void addPhotoDetector(){
        open();
        bot.pressKey(KeyEvent.VK_TAB, 3);
        skipDevices();
        bot.pressKey(KeyEvent.VK_ENTER, 1 , ENTER_DELAY_STRENGTH);
        bot.pressKey(KeyEvent.VK_ESCAPE);
        bot.pressKey(KeyEvent.VK_END);
    }

    public void addAlarmInputMod(){
        open();
        bot.pressKey(KeyEvent.VK_D, 2);
        bot.pressKey(KeyEvent.VK_TAB, 3);
        skipDevices();
        bot.pressKey(KeyEvent.VK_ENTER, 1 , ENTER_DELAY_STRENGTH);
        bot.pressKey(KeyEvent.VK_ESCAPE);
        bot.pressKey(KeyEvent.VK_END);
    }

    public void addNonLatchedSupv(){
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

    public void addLatchedSupv(){
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

    public void addHeatDetector(){
        open();
        bot.pressKey(KeyEvent.VK_H,3);
        bot.pressKey(KeyEvent.VK_TAB,3);
        skipDevices();
        bot.pressKey(KeyEvent.VK_ENTER, 1 , ENTER_DELAY_STRENGTH);
        bot.pressKey(KeyEvent.VK_ESCAPE);
        bot.pressKey(KeyEvent.VK_END);
    }

    public void addAlarmInputClassA(){
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

    public void addRelay(){
        open();
        bot.pressKey(KeyEvent.VK_D);
        bot.pressKey(KeyEvent.VK_TAB, 2);
        skipDevices();
        bot.pressKey(KeyEvent.VK_ENTER, 1 , ENTER_DELAY_STRENGTH);
        bot.pressKey(KeyEvent.VK_ESCAPE);
        bot.pressKey(KeyEvent.VK_END);
    }

    private void enterTag(String tag){
        try {
            Thread.sleep(DELAY);
            for(int i = 0; i < tag.length(); i++){
                char c = tag.charAt(i);
                int keyCode = KeyEvent.getExtendedKeyCodeForChar(c);
                if (keyCode == KeyEvent.VK_UNDEFINED) {
                    System.err.println("Key code not found for character: " + c);
                } else {
                    bot.pressKey(keyCode, 1, TAG_DELAY_STRENGTH);
                }
            }
        } catch (Exception e) {
            System.err.println(e);
        }
    }

    public void updateTags(Zone zone){
        try {
            Thread.sleep(DELAY);
            bot.pressKey(KeyEvent.VK_ENTER, 1, ENTER_DELAY_STRENGTH);
            enterTag(zone.getTag1());
            bot.pressKey(KeyEvent.VK_ENTER, 1, ENTER_DELAY_STRENGTH);
            enterTag(zone.getTag2());
            bot.pressKey(KeyEvent.VK_ENTER, 1, ENTER_DELAY_STRENGTH);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void updateType(Zone zone){
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

    public void updateRow(Zone zone){
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

    public void updateZone(Zone zone){
        try {
            updateRow(zone);

            if (zone.getSubAddress() != null){
                updateRow(zone.getSubAddress());
            } else if(zone.isDualInput() || zone.getType().equals("Relay")) {
                // add empty
                updateRow(new subZone(zone.getAddress()+0.1, "    Spare", zone.getTag2()));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        
    }

    public void enterZoneList(ZoneList zoneList){
        try {
            bot.pressKey(KeyEvent.VK_HOME, 1, 1); 
            for(Zone zone : zoneList.zones){
                if(!zone.getType().equals("Blank Device")){
                    updateZone(zone);
                }
            }
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void readSettings() {
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
                ini.put("Key Delay", "tagDelayStrength", TAG_DELAY_STRENGTH);
                ini.put("Key Delay", "enterDelayStrength", ENTER_DELAY_STRENGTH);
            }

            if(!ini.containsKey("Options")) {
                ini.add("Options");
                ini.put("Options", "bypassPause", BYPASS_PAUSE);
            }

            ini.store(ini_file);

            //Read settings
            DELAY = ini.get("Key Delay", "delayTime", int.class);
            TAG_DELAY_STRENGTH = ini.get("Key Delay", "tagDelayStrength", double.class);
            ENTER_DELAY_STRENGTH = ini.get("Key Delay", "enterDelayStrength", double.class);
            BYPASS_PAUSE = ini.get("Options", "bypassPause", boolean.class);
            
        }catch(Exception e) {
            e.printStackTrace();
        }
    }

    public void setIsRunning(boolean status){
        is_running = status;

        //Set bot to null to prevent further inputs
        if(!is_running){
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
