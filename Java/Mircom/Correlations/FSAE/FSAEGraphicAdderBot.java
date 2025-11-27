import java.util.Scanner;
import java.awt.RenderingHints.Key;
import java.awt.event.KeyEvent;
import java.io.File;
import java.util.ArrayList;

//Select the first row of the graphic adder, then press F4. All zones must be UNASSIGNED.

public class FSAEGraphicAdderBot extends FSAEBot{

    //Override the default settings since it is not as heavy of a task
    private int DELAY = 200;
    private double SKIP_DELAY_STRENGTH = .4; //The delay time for scrolling through zones.

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

                updateZone("Normal " + floor , KeyEvent.VK_M);
                updateZone("Low Heat " + floor, KeyEvent.VK_S);
                updateZone("High Heat " + floor, KeyEvent.VK_A);
                updateZone("Smoke Det " + floor, KeyEvent.VK_A);

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
}
