import com.github.kwhat.jnativehook.GlobalScreen;
import com.github.kwhat.jnativehook.NativeHookException;
import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent;
import com.github.kwhat.jnativehook.keyboard.NativeKeyListener;

public class Automation implements NativeKeyListener{

    private int START_DELAY = 0;

    private FX400 fx400 = new FX400();
    private FX2000 fx2000;

    public static void main(String[] args){
        //Register key presses
        try {
			GlobalScreen.registerNativeHook();
		}
		catch (NativeHookException ex) {
			System.err.println("There was a problem registering the native hook.");
			System.err.println(ex.getMessage());

			System.exit(1);
		}

        GlobalScreen.addNativeKeyListener(new Automation());

        System.out.println("Program ready. Press F2 to start, ` to Exit.");
    }

    public void nativeKeyPressed(NativeKeyEvent e) {
        
		if (e.getKeyCode() == NativeKeyEvent.VC_BACKQUOTE) {

            //Closes the program completely - will have to restart it
            try {
                System.out.println("Exiting Program");
                GlobalScreen.unregisterNativeHook();
                System.exit(0);
            } catch (NativeHookException nativeHookException) {
                nativeHookException.printStackTrace();
            }

            /*
            //Stops the data entry bot WITHOUT requiring a restart  - this will throw errors, may want to catch them properly
            try {
                System.out.println("Exiting Program");
                fx400.interrupt();
                fx400.setIsRunning(false);
            } catch (Exception nativeHookException) {
                nativeHookException.printStackTrace();
            }
            */
        }

        
        if (e.getKeyCode() == NativeKeyEvent.VC_F2) {
            try {
                //Start a new thread only if it doesn't exist or is no longer alive
                if(fx400 == null || (fx400 != null && !fx400.isAlive())){
                    fx400 = new FX400();
                }

                if(!fx400.isAlive()){
                    Thread.sleep(START_DELAY);

                    System.out.println("Starting FX400 Data Entry");      
                    fx400.start();
                }else if(fx400.isAlive() && fx400.getIsPaused()){
                    fx400.setIsPaused(false);
                }
            } catch (Exception except) {
                except.printStackTrace();
            }
        }
        
        
         if (e.getKeyCode() == NativeKeyEvent.VC_F3) {
            try {
                //Start a new thread only if it doesn't exist or is no longer alive
                if(fx2000 == null || (fx2000 != null && !fx2000.isAlive())){
                    fx2000 = new FX2000();
                }

                if(!fx2000.isAlive()){
                    Thread.sleep(START_DELAY);

                    System.out.println("Starting FX2000 Data Entry");      
                    fx2000.start();
                }else if(fx2000.isAlive() && fx2000.getIsPaused()){
                    fx2000.setIsPaused(false);
                }
            } catch (Exception except) {
                except.printStackTrace();
            }
        }
        
	}
}
