import com.github.kwhat.jnativehook.GlobalScreen;
import com.github.kwhat.jnativehook.NativeHookException;
import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent;
import com.github.kwhat.jnativehook.keyboard.NativeKeyListener;

public class Automation implements NativeKeyListener{

    private FX400 fx400;
    private int START_DELAY;

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

        if (e.getKeyCode() == NativeKeyEvent.VC_ALT) {
            try {
                if(fx400 == null){
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
