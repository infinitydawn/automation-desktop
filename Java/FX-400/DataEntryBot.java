import java.awt.Robot;
import java.awt.event.KeyEvent;
import com.github.kwhat.jnativehook.keyboard.NativeKeyListener;
import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent;

public class DataEntryBot implements NativeKeyListener {
    Robot robot;
    int delay = 200;
    private int skipCount = 19;

    public DataEntryBot(){
        try {
            Thread.sleep(5000);
            robot = new Robot();
        } catch (Exception e) {
            System.err.println(e);
        }
    }

    public void skipDevices(){
        pressKey(KeyEvent.VK_RIGHT, skipCount, 2);
    }
    

    public void open(){
        try {
            Thread.sleep(delay);
            robot.keyPress(KeyEvent.VK_SHIFT);
            robot.keyPress(KeyEvent.VK_F10);
            robot.keyRelease(KeyEvent.VK_SHIFT);
            robot.keyRelease(KeyEvent.VK_F10);
            robot.delay(delay);

            pressKey(KeyEvent.VK_DOWN);
            pressKey(KeyEvent.VK_ENTER);
            
        } catch (Exception e) {
            System.err.println(e);
        }
    }
    

    public void addPhotoDetector(){
        open();
        pressKey(KeyEvent.VK_TAB, 3);
        skipDevices();
        pressKey(KeyEvent.VK_ENTER);
        pressKey(KeyEvent.VK_ESCAPE);
        pressKey(KeyEvent.VK_END);
    }


    public void addAlarmInputMod(){
        open();
        pressKey(KeyEvent.VK_D, 2);
        pressKey(KeyEvent.VK_TAB, 3);
        skipDevices();
        pressKey(KeyEvent.VK_ENTER);
        pressKey(KeyEvent.VK_ESCAPE);
        pressKey(KeyEvent.VK_END);
    }


    public void addNonLatchedSupv(){
        open();
        pressKey(KeyEvent.VK_D, 2);
        pressKey(KeyEvent.VK_TAB, 2);
        pressKey(KeyEvent.VK_N);
        pressKey(KeyEvent.VK_TAB);
        skipDevices();
        pressKey(KeyEvent.VK_ENTER);
        pressKey(KeyEvent.VK_ESCAPE);
        pressKey(KeyEvent.VK_END);
    }

    public void updateSkipCount(int skipCount){
        this.skipCount = skipCount;
    }



    public void addLatchedSupv(){
        open();
        pressKey(KeyEvent.VK_D,2);
        pressKey(KeyEvent.VK_TAB,2);
        pressKey(KeyEvent.VK_L);
        pressKey(KeyEvent.VK_TAB);
        skipDevices();
        pressKey(KeyEvent.VK_ENTER);
        pressKey(KeyEvent.VK_ESCAPE);
        pressKey(KeyEvent.VK_END);
    }

    public void addHeatDetector(){
        open();
        pressKey(KeyEvent.VK_H,3);
        pressKey(KeyEvent.VK_TAB,3);
        skipDevices();
        pressKey(KeyEvent.VK_ENTER);
        pressKey(KeyEvent.VK_ESCAPE);
        pressKey(KeyEvent.VK_END);
    }



    public void addRelay(){
        open();
        pressKey(KeyEvent.VK_D);
        pressKey(KeyEvent.VK_TAB, 2);
        skipDevices();
        pressKey(KeyEvent.VK_ENTER);
        pressKey(KeyEvent.VK_ESCAPE);
        pressKey(KeyEvent.VK_END);
    }

    private void enterTag(String tag){
        try {
            Thread.sleep(delay);
            for(int i = 0; i < tag.length(); i++){
                char c = tag.charAt(i);
                int keyCode = KeyEvent.getExtendedKeyCodeForChar(c);
                if (keyCode == KeyEvent.VK_UNDEFINED) {
                    System.err.println("Key code not found for character: " + c);
                } else {
                    robot.keyPress(keyCode); robot.keyRelease(keyCode); Thread.sleep(15);
                }
            }
        } catch (Exception e) {
            System.err.println(e);
        }
    }

    private void updateTags(Zone zone){
        try {
            Thread.sleep(delay);
            robot.keyPress(KeyEvent.VK_ENTER); robot.keyRelease(KeyEvent.VK_ENTER); robot.delay(delay);
            enterTag(zone.getTag1());
            robot.keyPress(KeyEvent.VK_ENTER); robot.keyRelease(KeyEvent.VK_ENTER); robot.delay(delay);
            enterTag(zone.getTag2());
            robot.keyPress(KeyEvent.VK_ENTER); robot.keyRelease(KeyEvent.VK_ENTER); robot.delay(delay);
        } catch (Exception e) {
            // TODO: handle exception
        }
    }

    private void updateType(Zone zone){
        try {
            Thread.sleep(delay);
            switch(zone.getType()){
                case "Photo Detector":
                    pressKey(KeyEvent.VK_A);
                    break;
                case "Alarm Input":
                    pressKey(KeyEvent.VK_M);
                    pressKey(KeyEvent.VK_A, 3);
                    break;
                case "Non-latched Supervisory":
                    pressKey(KeyEvent.VK_N);
                    break;
                case "Latched Supervisory":
                    pressKey(KeyEvent.VK_L);
                    break;
                case "Heat Detector":
                    pressKey(KeyEvent.VK_M);
                    pressKey(KeyEvent.VK_A, 3);
                    break;
                case "Blank Device":
                    pressKey(KeyEvent.VK_N);
                    pressKey(KeyEvent.VK_B, 2);
                    break;
                case "Relay":
                    pressKey(KeyEvent.VK_R);
                    break;
            }
            robot.keyPress(KeyEvent.VK_ENTER); robot.keyRelease(KeyEvent.VK_ENTER); robot.delay(delay);
        } catch (Exception e) {
            // TODO: handle exception
        }
    }

    private void updateRow(Zone zone){
        updateTags(zone);
        updateType(zone);
        
        if(zone.isNS()){
            pressKey(KeyEvent.VK_N);
        }

        pressKey(KeyEvent.VK_ENTER);
        pressKey(KeyEvent.VK_ESCAPE);
        pressKey(KeyEvent.VK_DOWN);
    }


    private void updateZone(Zone zone){
        try {
            updateRow(zone);

            if (zone.getSubAddress() != null){
                updateRow(zone.getSubAddress());
            } else if(zone.isDualInput() || zone.getType().equals("Relay")) {
                // add empty
                updateRow(new subZone(zone.getAddress()+0.1, "    Spare", zone.getTag2()));
            }
        } catch (Exception e) {
            // TODO: handle exception
            System.out.println(e);
        }
        
    }

    public void enterZoneList(ZoneList zoneList){
        try {
            pressKey(KeyEvent.VK_PAGE_UP);
            for(Zone zone : zoneList.zones){
                if(!zone.getType().equals("Blank Device")){
                    updateZone(zone);
                }
            }
            
        } catch (Exception e) {
            // TODO: handle exception
        }
    }

    //Give key and distance, press and release key distance times, and delay if provided
    public void pressKey(int key, int targetDistance, int threadSleep)
    {
        try {
            Thread.sleep(delay);

            for(int i = 0; i < targetDistance; i++){
            
                robot.keyPress(key); 
                robot.keyRelease(key);

                robot.delay(delay);

                if (threadSleep > 0)
                {
                    Thread.sleep(15);
                }
            }
        }catch (Exception e) {
            // TODO: handle exception
        }
    }  

    public void pressKey(int key, int targetDistance)
    {
        pressKey(key, targetDistance, 0);
    }

    public void pressKey(int key)
    {
        pressKey(key, 1, 0);
    }
}
