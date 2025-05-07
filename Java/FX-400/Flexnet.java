import java.util.ArrayList;
import java.awt.event.KeyEvent;

public class Flexnet extends FX400{
    public void run() {
        System.out.println("Starting FX2000 Data Entry");
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

                if(Zone.checkTags(tags1[i], new String[] {"ac shutdown", "ac shut down", "fan shutdown","fan shut down"})){
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

    protected void addAlarmInputMod(){
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

    protected void addRelay(){
        open();
        bot.pressKey(KeyEvent.VK_R, 2);
        bot.pressKey(KeyEvent.VK_TAB, 4);
        skipDevices();
        bot.pressKey(KeyEvent.VK_ENTER, 1 , ENTER_DELAY_STRENGTH);
        bot.pressKey(KeyEvent.VK_ESCAPE);
        bot.pressKey(KeyEvent.VK_END);
    }

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
            /* //can get 2 subzones
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
}

//need to set AP in order to get some types, like Input Mod
//dual heat photos:  3 addresses, but a preset order
//dual smoke/fire