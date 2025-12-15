import java.util.Scanner;
import java.awt.RenderingHints.Key;
import java.awt.event.KeyEvent;
import java.io.File;
import java.util.ArrayList;

//For Input Zone updating: create input zones in advance (preferably type Monitor) then select the first zone to be updated
//To update logic: Get the first input zone address (IZ-##) then write it in fsae_zones inside [brackets]. Press the key inside the Advanced Logic window for each zone to be updated.

public class FSAEInputZoneBot extends FSAEBot{

    /////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //Settings

    private int NODE = 1; // The CPU that the input zones belong to

    //Tag names for each zone. Change these if they need to be shorter
    private String NORMAL_STRING = "Normal "; //Default "Normal "
    private String LOW_STRING = "Low Heat "; //Default "Low Heat "
    private String HIGH_STRING = "High Heat "; //Default "High Heat "
    private String SMOKE_STRING = "Smoke Det "; //Default "Smoke Det "

    private String EQUATION = "NOT ANY 1 OF (  %n" +
                    " 0%s-00-**-IZ-%s:A ,  %n" +
                    " 0%s-00-**-IZ-%s:A ,  %n" +
                    "  %n" +
                    "  %n" +
                    " 0%s-00-**-IZ-%s:F ,  %n" +
                    " 0%s-00-**-IZ-%s:F ) ";
    private String EQUATION_NAME = "NORMAL %s";
    private String EQUATION_COMMENT = "NORMAL %s - Dual Heat Not In Alarm Or Trouble";

    /////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private int current_zone_index = 0;
    private boolean is_data_entry_mode = false;

    public FSAEInputZoneBot() {
        try {

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public FSAEInputZoneBot(boolean data_entry_status) {
        super();
        setIsDataEntryMode(data_entry_status);
    }

    public void run() {
        try {
            if (floors == null) {
                readFloors();
                bot = new DataEntryBot(DELAY);  
                System.out.println("Reading floors: " + floors);
            }
            
            if(!is_data_entry_mode) {
                if(!is_running) {
                    setIsRunning(true);
                    System.out.println("Starting Data Entry");

                    for (String floor : floors) {
                        System.out.println("Updating: " + floor);
                        updateZone(NORMAL_STRING + floor , KeyEvent.VK_M);
                        updateZone(LOW_STRING + floor, KeyEvent.VK_S);
                        updateZone(HIGH_STRING + floor, KeyEvent.VK_A);
                        updateZone(SMOKE_STRING + floor, KeyEvent.VK_A);
                    }

                    setIsRunning(false);
                    System.out.println("Data Entry Complete");
                    System.exit(MAX_PRIORITY);
                }
            } else {
                //Print out the zone addresses for the FSAE Dual Heat zone logic (contains all dual heats involved)
                String lowheat;
                String highheat;
                System.out.println("Input Zone addresses for the FSAE Dual Heat Input Zone logic:");
                String zone_string = "0%s-00-**-IZ-%s:F";
                int current_index = 0;

                for(String floor : floors) {
                    lowheat = calcLowHeat(current_index);
                    highheat = calcHighHeat(current_index);

                    System.out.print(String.format(zone_string, NODE, lowheat) + ", " +
                    String.format(zone_string, NODE, highheat));                   

                    if(!floor.equals(floors.getLast())) {
                        System.out.println(",");
                    }
                    current_index++;
                }

                System.out.println("");

                //Press the key to go to next floor for logic
                System.out.println("Begin logic updates. Use Win + V to go through paste history if an entry was skipped.");

                setIsRunning(true);
                setIsPaused(false);

                while(is_running) {
                    Thread.sleep(Math.max(100,DELAY));

                    if(!is_paused) {
                        updateLogic();
                    }
                }

            }
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }

    public void updateZone(String name, int type_key) {
        try {
            Thread.sleep(DELAY);

            //Tag 1
            bot.pressKey(KeyEvent.VK_ENTER, 1);
            bot.pasteText(name);
            bot.pressKey(KeyEvent.VK_ENTER, 1, DEVICE_UPDATE_DELAY_STRENGTH);
            bot.pressKey(KeyEvent.VK_ENTER, 1, DEVICE_UPDATE_DELAY_STRENGTH);
            //Type
            bot.pressKey(type_key, 1);
            bot.pressKey(KeyEvent.VK_ENTER, 1, DEVICE_UPDATE_DELAY_STRENGTH * 3);
            bot.pressKey(KeyEvent.VK_ESCAPE, 1);
            bot.pressKey(KeyEvent.VK_DOWN);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void updateLogic() {
        if(floors != null && current_zone_index < floors.size()) {
            if(!is_paused) {
                setIsPaused(true);

                System.out.println("Setting logic for " + floors.get(current_zone_index));

                String lowheat = calcLowHeat(current_zone_index);
                String highheat = calcHighHeat(current_zone_index);
                
                bot.clearText();
                bot.pasteText(String.format(EQUATION, 
                    "" + NODE, lowheat,
                    "" + NODE, highheat,
                    "" + NODE, lowheat,
                    "" + NODE, highheat));
                bot.pressKey(KeyEvent.VK_TAB);

                bot.clearText();
                bot.pasteText(String.format(EQUATION_NAME, floors.get(current_zone_index)));
                bot.pressKey(KeyEvent.VK_TAB);

                bot.clearText();
                bot.pasteText(String.format(EQUATION_COMMENT, floors.get(current_zone_index)));

                System.out.println("Logic set for " + floors.get(current_zone_index));
                
                current_zone_index++;
                CURRENT_ZONE_ADDRESS += 3;

                if(current_zone_index >= floors.size()) {
                    System.out.println("Finished logic updates.");
                    setIsRunning(false);
                    setIsPaused(false);
                    System.exit(MAX_PRIORITY);
                }
                else {
                    System.out.println("Next floor: " + floors.get(current_zone_index));
                }
            }
        } else {
            setIsRunning(false);
        }
    }

    public String calcLowHeat(int current_index) {
        //Insert 0s to reach 3 digits
        //+1 since it intends to use the Low Heat
        String lowheat = CURRENT_ZONE_ADDRESS + current_index + 1 + ""; 
        if(CURRENT_ZONE_ADDRESS + current_index < 99) {
            lowheat = "0" + lowheat;

            if(CURRENT_ZONE_ADDRESS + current_index < 9) {
                lowheat = "0" + lowheat;
            }
        }

        return lowheat;
    }

    public String calcHighHeat(int current_index) {
        //+2 for High Heat
        String highheat = CURRENT_ZONE_ADDRESS + current_index + 2 + "";
        if(CURRENT_ZONE_ADDRESS + current_index + 1 < 99) {
            highheat = "0" + highheat;
            if(CURRENT_ZONE_ADDRESS + current_index < 9) {
                highheat = "0" + highheat;
            }
        }

        return highheat;
    }

    public void setIsDataEntryMode(boolean status) {
        is_data_entry_mode = status;
    }
}
