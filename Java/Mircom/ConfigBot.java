import java.io.File;
import java.awt.event.KeyEvent;
import org.ini4j.Ini;

public abstract class ConfigBot extends Thread {
    protected int DELAY = 200; //Default 200. Delay time for everything. Multiply by delay strength to change length
    protected double DEVICE_INSERT_DELAY_STRENGTH = 2; // Default 2. Multiplied to DELAY. The Delay time after inserting a device.
    protected double DEVICE_UPDATE_DELAY_STRENGTH = 1.5; // Default 1.5. Multiplied to DELAY. The Delay time after updating a device (tag name, type, etc).
    protected boolean BYPASS_PAUSE = false; //Prevents the error prompt from showing
    protected boolean IGNORE_TAG_LENGTH = false; //Omits tag length requirement from errors
    protected boolean SKIP_INSERT_DEVICES = false; //Set the bot to update devices only instead of inserting first. Meant for when the devices are entered and other details (type, f1 tags,etc) are untouched.
    protected String SETTINGS_FILE = "settings.ini";

    protected DataEntryBot bot;
    protected int skip_count = 19;
    protected boolean is_running = false; //used to stop the bot from running without closing process
    protected boolean is_paused = false; //used to prompt user to enable AR related settings -- MAKE USE OF THIS IF PAUSING GUI

    public ConfigBot() {
        try {
            readSettings();
            bot = new DataEntryBot(DELAY);           
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public abstract void run();

    public abstract void addPhotoDetector();

    public abstract void addNonLatchedSupv();

    public abstract void addLatchedSupv();

    public abstract void addHeatDetector();

    public abstract void addRelay();

    public void skipDevices() {
        bot.pressKey(KeyEvent.VK_RIGHT, skip_count);
    }

    public void open() {
        try {
            Thread.sleep(DELAY);
            bot.keyPress(KeyEvent.VK_SHIFT);
            bot.keyPress(KeyEvent.VK_F10);
            bot.keyRelease(KeyEvent.VK_SHIFT);
            bot.keyRelease(KeyEvent.VK_F10);
            bot.delay(DELAY);

            bot.pressKey(KeyEvent.VK_DOWN);
            bot.pressKey(KeyEvent.VK_ENTER, 1 , DEVICE_INSERT_DELAY_STRENGTH);
            
        } catch (Exception e) {
            System.err.println(e);
        }
    }

    public void updateTags(Zone zone) {
        try {
            Thread.sleep(DELAY);
            bot.pressKey(KeyEvent.VK_ENTER, 1, DEVICE_UPDATE_DELAY_STRENGTH);
            bot.pasteText(zone.getTag1());
            bot.pressKey(KeyEvent.VK_ENTER, 1, DEVICE_UPDATE_DELAY_STRENGTH);
            bot.pasteText(zone.getTag2());
            bot.pressKey(KeyEvent.VK_ENTER, 1, DEVICE_UPDATE_DELAY_STRENGTH);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public abstract void updateType(Zone zone);

    protected void updateRow(Zone zone) {
        updateTags(zone);
        updateType(zone);
        
        if(zone.isNS()) {
            bot.pressKey(KeyEvent.VK_N, 1, DEVICE_UPDATE_DELAY_STRENGTH);
        }

        bot.pressKey(KeyEvent.VK_ENTER, 1 , DEVICE_UPDATE_DELAY_STRENGTH);
        
        if(zone.isAR()) {
            bot.pressKey(KeyEvent.VK_A);
            bot.pressKey(KeyEvent.VK_ENTER, 1 , DEVICE_UPDATE_DELAY_STRENGTH);
        }

        bot.pressKey(KeyEvent.VK_ESCAPE);
        bot.pressKey(KeyEvent.VK_DOWN);
    }

    protected abstract void updateZone(Zone zone);

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

    protected abstract boolean validateZones(ZoneList zone_list);

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
                ini.putComment("Key Delay", 
                    String.format("delayTime - Default %s. Delay time for everything. Multiply by delay strength to change duration. \n" +
                    "#deviceInsertDelayStrength - Default %s. Multiplied to delayTime. The Delay time after inserting a device. \n" +
                    "#deviceUpdateDelayStrength - Default %s. Multiplied to delayTime. The Delay time after updating a device (tag name, type, etc).",
                    DELAY, DEVICE_INSERT_DELAY_STRENGTH, DEVICE_UPDATE_DELAY_STRENGTH)
                );
                ini.put("Key Delay", "delayTime", DELAY);
                ini.put("Key Delay", "deviceInsertDelayStrength", DEVICE_INSERT_DELAY_STRENGTH);
                ini.put("Key Delay", "deviceUpdateDelayStrength", DEVICE_UPDATE_DELAY_STRENGTH);
            }

            if(!ini.containsKey("Options")) {
                ini.add("Options");
                ini.putComment("Options", 
                    "bypassPause - Prevents the error prompt from showing. \n" +
                    "#ignoreTagLength - Omits tag length requirement from errors. \n" +
                    "#skipInsertDevices - Set the bot to update devices only instead of inserting first. Meant for when the devices are entered and other details (type, f1 tags,etc) are untouched."
                );
                ini.put("Options", "bypassPause", BYPASS_PAUSE);
                ini.put("Options", "ignoreTagLength", IGNORE_TAG_LENGTH);
                ini.put("Options", "skipInsertDevices", SKIP_INSERT_DEVICES);   
            }

            ini.store(ini_file);

            //Read settings
            DELAY = ini.get("Key Delay", "delayTime", int.class);
            DEVICE_INSERT_DELAY_STRENGTH = ini.get("Key Delay", "deviceInsertDelayStrength", double.class);
            DEVICE_UPDATE_DELAY_STRENGTH = ini.get("Key Delay", "deviceUpdateDelayStrength", double.class);
            BYPASS_PAUSE = ini.get("Options", "bypassPause", boolean.class);
            IGNORE_TAG_LENGTH = ini.get("Options", "ignoreTagLength", boolean.class);
            
        }catch(Exception e) {
            e.printStackTrace();
        }
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
