import java.util.Scanner;
import java.awt.RenderingHints.Key;
import java.awt.event.KeyEvent;
import java.io.File;
import java.util.ArrayList;

//For Input Zone updating: create input zones in advance (preferably type Monitor) then select the first zone to be updated
//To update logic: Get the first input zone address (IZ-##) then write it in fsae_zones inside [brackets]. Press the key inside the Advanced Logic window for each zone to be updated.

public class FSAEInputZoneBot extends FSAEBot{

    private String EQUATION = "NOT ANY 1 OF (  %n" +
                    " 01-00-**-IZ-%s:A ,  %n" +
                    " 01-00-**-IZ-%s:A ,  %n" +
                    "  %n" +
                    "  %n" +
                    " 01-00-**-IZ-%s:F ,  %n" +
                    " 01-00-**-IZ-%s:F ) ";
    private String EQUATION_NAME = "NORMAL %s";
    private String EQUATION_COMMENT = "NORMAL %s - Dual Heat Not In Alarm Or Trouble";

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
                        updateZone("Normal " + floor , KeyEvent.VK_M);
                        updateZone("Low Heat " + floor, KeyEvent.VK_S);
                        updateZone("High Heat " + floor, KeyEvent.VK_A);
                        updateZone("Smoke Det " + floor, KeyEvent.VK_A);
                    }

                    setIsRunning(false);
                    System.out.println("Data Entry Complete");
                    System.exit(MAX_PRIORITY);
                }
            } else {
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

                //Insert 0s to reach 3 digits
                //+1 since it intends to use the Low Heat
                String final_input_zone1 = CURRENT_ZONE_ADDRESS + current_zone_index + 1 + ""; 
                if(CURRENT_ZONE_ADDRESS + current_zone_index < 99) {
                    final_input_zone1 = "0" + final_input_zone1;

                    if(CURRENT_ZONE_ADDRESS + current_zone_index < 9) {
                        final_input_zone1 = "0" + final_input_zone1;
                    }
                }
                
                //+2 for High Heat
                String final_input_zone2 = CURRENT_ZONE_ADDRESS + current_zone_index + 2 + "";
                if(CURRENT_ZONE_ADDRESS + current_zone_index + 1 < 99) {
                    final_input_zone2 = "0" + final_input_zone2;
                    if(CURRENT_ZONE_ADDRESS + current_zone_index < 9) {
                        final_input_zone2 = "0" + final_input_zone2;
                    }
                }

                bot.clearText();
                bot.pasteText(String.format(EQUATION, 
                    final_input_zone1, final_input_zone2,
                    final_input_zone1, final_input_zone2));
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

    public void setIsDataEntryMode(boolean status) {
        is_data_entry_mode = status;
    }
}
