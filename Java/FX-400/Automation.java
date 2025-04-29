import com.github.kwhat.jnativehook.GlobalScreen;
import com.github.kwhat.jnativehook.NativeHookException;
import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent;
import com.github.kwhat.jnativehook.keyboard.NativeKeyListener;

public class Automation implements NativeKeyListener{

    private final int START_DELAY = 0;

    private FX400 fx400;

    public static void main(String[] args){
        //Register escape button
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
            try {
                System.out.println("Exiting Program");
                GlobalScreen.unregisterNativeHook();
                System.exit(0);
            } catch (NativeHookException nativeHookException) {
                nativeHookException.printStackTrace();
            }
        }

        if (e.getKeyCode() == NativeKeyEvent.VC_F2) {
            try {
                if(fx400 == null){
                    Thread.sleep(START_DELAY);

                    System.out.println("Starting FX400");

                    fx400 = new FX400();
                    if(!fx400.isAlive()){
                        fx400.start();
                    }
                }
            } catch (Exception except) {
                except.printStackTrace();
            }
        }
	}
}
