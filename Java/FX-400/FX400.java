import com.github.kwhat.jnativehook.GlobalScreen;
import com.github.kwhat.jnativehook.NativeHookException;
import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent;
import com.github.kwhat.jnativehook.keyboard.NativeKeyListener;


public class FX400 implements NativeKeyListener{

    public static void main(String[] args) throws Exception {

        //Register escape button
        try {
			GlobalScreen.registerNativeHook();
		}
		catch (NativeHookException ex) {
			System.err.println("There was a problem registering the native hook.");
			System.err.println(ex.getMessage());

			System.exit(1);
		}

		GlobalScreen.addNativeKeyListener(new FX400());

        //Data entry stuff
        DataEntryBot bot = new DataEntryBot();
        ReadTempArray reader = new ReadTempArray();
        String[][] zoneParts = reader.readFile();
        ZoneList zoneList = new ZoneList();
        int skipCount = 0;

        String[] addresses = zoneParts[0];
        String[] tags1 = zoneParts[1];
        String[] tags2 = zoneParts[2];

        for (int i = 0; i < addresses.length; i++) {
            System.out.println(" - - - - -  + " + tags1[i]);
            zoneList.addZone(Double.parseDouble(addresses[i]), tags1[i], tags2[i]);
        }

        zoneList.displayZoneList();

        if (zoneList.zones.get(0).getAddress() != 0) {
            skipCount = (int) (zoneList.zones.get(0).getAddress() - 1);
            bot.updateSkipCount(skipCount);
        }

        System.out.println(skipCount);

        for (Zone zone : zoneList.zones) {
            System.out.println("Checking: " + zone.getZoneinfo());

            switch (zone.getType()) {
                case "Photo Detector":
                    bot.addPhotoDetector();
                    break;
                case "Alarm Input":
                    bot.addAlarmInputMod();
                    break;
                case "Non-latched Supervisory":
                    bot.addNonLatchedSupv();
                    break;
                case "Latched Supervisory":
                    bot.addLatchedSupv();
                    break;
                case "Heat Detector":
                    bot.addHeatDetector();
                    break;
                case "Relay":
                    bot.addRelay();
                    break;
                default:
                    skipCount += 1;
                    bot.updateSkipCount(skipCount);
                    System.out.println("  Skipping zone: " + zone.getZoneinfo() + " (skip count: " + skipCount + ")");
            }
        }

       bot.enterZoneList(zoneList);

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
        // robot.keyPress(KeyEvent.VK_H);
        // robot.keyRelease(KeyEvent.VK_H);
        // robot.keyPress(KeyEvent.VK_E);
        // robot.keyRelease(KeyEvent.VK_E);
        // // ... (and so on)
    }

    public void nativeKeyPressed(NativeKeyEvent e) {
		if (e.getKeyCode() == NativeKeyEvent.VC_BACKQUOTE) {
            try {
                GlobalScreen.unregisterNativeHook();
                System.exit(0);
            } catch (NativeHookException nativeHookException) {
                nativeHookException.printStackTrace();
            }
        }
	}
}
