import com.github.kwhat.jnativehook.GlobalScreen;
import com.github.kwhat.jnativehook.NativeHookException;
import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent;
import com.github.kwhat.jnativehook.keyboard.NativeKeyListener;

public class Automation implements NativeKeyListener{

    private int START_DELAY = 0;

    private FX400 fx400;
    private FX400 fx2000;
    private FX400 flexnet;

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

        System.out.println("Program ready. Press F2 to start FX400, F3 to start FX2000, F4 to start Flexnet, ` to Exit anytime.");
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

        //NEED TO PREVENT PRESSING OTHER PROGRAMS WHILE RUNNING
 
        if (e.getKeyCode() == NativeKeyEvent.VC_F2) {
            try {
                //Start a new thread only if it doesn't exist or is no longer alive
                if(isRunning(fx400)) {
                    fx400 = new FX400();
                }

                if(!fx400.isAlive()) {
                    Thread.sleep(START_DELAY);  
                    fx400.start();
                }else if(fx400.isAlive() && fx400.getIsPaused()) {
                    fx400.setIsPaused(false);
                }
            } catch (Exception except) {
                except.printStackTrace();
            }
        }
        
         if (e.getKeyCode() == NativeKeyEvent.VC_F3) {
            try {
                //Start a new thread only if it doesn't exist or is no longer alive
                if(isRunning(fx2000)) {
                    fx2000 = new FX2000();
                }

                if(!fx2000.isAlive()) {
                    Thread.sleep(START_DELAY);   
                    fx2000.start();
                }else if(fx2000.isAlive() && fx2000.getIsPaused()) {
                    fx2000.setIsPaused(false);
                }
            } catch (Exception except) {
                except.printStackTrace();
            }
        }

        if (e.getKeyCode() == NativeKeyEvent.VC_F4) {
            try {
                //Start a new thread only if it doesn't exist or is no longer alive
                if(isRunning(flexnet)) {
                    flexnet = new Flexnet();
                }

                if(!flexnet.isAlive()) {
                    Thread.sleep(START_DELAY);   
                    flexnet.start();
                }else if(flexnet.isAlive() && flexnet.getIsPaused()) {
                    flexnet.setIsPaused(false);
                }
            } catch (Exception except) {
                except.printStackTrace();
            }
        }
        
	}

    public boolean isRunning(FX400 panel) {
        return ((panel == null || (panel != null && !panel.isAlive())));
    }
}
