import java.util.Scanner;
import java.awt.RenderingHints.Key;
import java.awt.event.KeyEvent;
import java.io.File;
import java.util.ArrayList;

//There are 2 modes to this bot - Full mode and Tag Only mode.
//Full mode:
//Select the first row of the graphic adder, then press F4. All zones must be UNASSIGNED. The correct type will be entered. The drawback is that this mode will be slow.
//Tag Only mode:
//Set all the zones of the graphic adder to Ipt Zone before hand then run the bot. Only the tags will be updated. Although this mode is MUCH faster,
//  the drawback is that the user will have to update the Types afterwards, which may crash the configurator. 

public class FSAEGraphicAdderBot extends FSAEBot{

    private boolean TAG_ONLY_MODE = true; //Default True. 

    //Default 3. This is the position of the Tag column in the configurator. If you want to speed things up, move Tag to the first column and change this to 1. 
    private int TAG_COLUMN_POSITION = 3; 

    //Override the default settings since it is not as heavy of a task
    private int DELAY = 200;
    private double SKIP_DELAY_STRENGTH = .4; // Default .4. For Full mode. The delay time for scrolling through zones.

    private int graphic_index; //the index of the current graphic adder that will be updated
    private int floor_index;
    private int overall_index;

    public void run() {
        try {
            if (floors == null) {
                readFloors();
                bot = new DataEntryBot(DELAY);  
                System.out.println("Reading floors: " + floors);
            }

            System.out.println("Starting Graphic Adder Entry");

            setIsRunning(true);
            setIsPaused(false);

            floor_index = 0;
            graphic_index = 0;
            overall_index = 0;
            String floor;  

            while(floor_index < floors.size()) {
                floor = floors.get(floor_index);
                System.out.println("Updating: " + floor);

                if(!TAG_ONLY_MODE) {
                updateZone("Normal " + floor , KeyEvent.VK_M);
                updateZone("Low Heat " + floor, KeyEvent.VK_S);
                updateZone("High Heat " + floor, KeyEvent.VK_A);
                updateZone("Smoke Det " + floor, KeyEvent.VK_A);
                }
                else {
                    updateZoneTag("Normal " + floor);
                    updateZoneTag("Low Heat " + floor);
                    updateZoneTag("High Heat " + floor);
                    updateZoneTag("Smoke Det " + floor);
                }

                floor_index++;
            }

            setIsRunning(false);
            System.out.println("Graphic Adder Entry Complete - Add the other zones afterwards");
            System.exit(MAX_PRIORITY);
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }

    public void updateZone(String name, int type_key) {
        try {

            Thread.sleep(DELAY);
            
            while(is_paused) {
                Thread.sleep(100);
            }

            //Type Ipt Zone
            bot.pressKey(KeyEvent.VK_ENTER, 1);
            bot.pressKey(KeyEvent.VK_I, 1);
            bot.pressKey(KeyEvent.VK_ENTER, 1, DEVICE_UPDATE_DELAY_STRENGTH);

            //Assignment
            bot.pressKey(type_key, 1);
            bot.pressKey(KeyEvent.VK_ENTER, 1, DEVICE_UPDATE_DELAY_STRENGTH);

            //Tag
            bot.pasteText(name);

            //Selection restarts from the top if a Type or Assignment is changed, need to get back to next available zone
            bot.pressKey(KeyEvent.VK_ENTER, 1, DEVICE_UPDATE_DELAY_STRENGTH);

            if(graphic_index != 0 && graphic_index % 47 == 0) {
                graphic_index = 0;
                setIsPaused(true);
                System.out.println("Entry paused. Move to next graphic adder, then press F4 to continue.");
            }
            else {
                if(overall_index < (floors.size() * 4) - 1) {
                    bot.pressKey(KeyEvent.VK_DOWN, 2 + graphic_index, SKIP_DELAY_STRENGTH);
                }
                graphic_index++;
            }

            overall_index++;

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void updateZoneTag(String name) {

        try {
            Thread.sleep(DELAY);
            
            while(is_paused) {
                Thread.sleep(100);
            }

            bot.pressKey(KeyEvent.VK_ENTER, TAG_COLUMN_POSITION);
            bot.pasteText(name);

            //Selection restarts from the top if a Type or Assignment is changed, need to get back to next available zone
            bot.pressKey(KeyEvent.VK_ENTER, 1, DEVICE_UPDATE_DELAY_STRENGTH);
            bot.pressKey(KeyEvent.VK_DOWN);

            if(graphic_index != 0 && graphic_index % 47 == 0) {
                graphic_index = 0;
                setIsPaused(true);
                System.out.println("Entry paused. Move to next graphic adder, then press F4 to continue.");
            } 
            else {
                graphic_index++;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
