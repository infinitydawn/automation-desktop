import java.util.ArrayList;
import java.awt.event.KeyEvent;

public class FX2000 extends FX400{

    public FX2000(){
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
                Thread.sleep(DELAY); //Wait until start button pressed again
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
        catch(Exception e){
            e.printStackTrace();
        }
    }
    
    public void addPhotoDetector(){
        open();
        bot.pressKey(KeyEvent.VK_TAB, 2);
        skipDevices();
        bot.pressKey(KeyEvent.VK_ENTER, 1 , ENTER_DELAY_STRENGTH);
        bot.pressKey(KeyEvent.VK_ESCAPE);
        bot.pressKey(KeyEvent.VK_END);
    }

    public void addAlarmInputMod(){
        open();
        bot.pressKey(KeyEvent.VK_I);
        bot.pressKey(KeyEvent.VK_TAB, 2);
        skipDevices();
        bot.pressKey(KeyEvent.VK_ENTER, 1 , ENTER_DELAY_STRENGTH);
        bot.pressKey(KeyEvent.VK_ESCAPE);
        bot.pressKey(KeyEvent.VK_END);
    }

    public void addNonLatchedSupv(){
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

    public void addLatchedSupv(){
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

    public void addHeatDetector(){
        open();
        bot.pressKey(KeyEvent.VK_H);
        bot.pressKey(KeyEvent.VK_TAB,2);
        skipDevices();
        bot.pressKey(KeyEvent.VK_ENTER, 1 , ENTER_DELAY_STRENGTH);
        bot.pressKey(KeyEvent.VK_ESCAPE);
        bot.pressKey(KeyEvent.VK_END);
    }

    public void addRelay(){
        open();
        bot.pressKey(KeyEvent.VK_R);
        bot.pressKey(KeyEvent.VK_TAB, 2);
        skipDevices();
        bot.pressKey(KeyEvent.VK_ENTER, 1 , ENTER_DELAY_STRENGTH);
        bot.pressKey(KeyEvent.VK_ESCAPE);
        bot.pressKey(KeyEvent.VK_END);
    }

    public void updateRow(Zone zone){
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

    public void updateZone(Zone zone){
        try {
            updateRow(zone);
        } catch (Exception e) {
            e.printStackTrace();
        }    
    }
}
