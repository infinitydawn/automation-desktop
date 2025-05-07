import java.awt.Robot;

public class DataEntryBot extends Robot{

    private int delay = 200; //default 200. Delay time between key presses
    private double DELAY_STRENGTH = 0; //Default 0. Multiply delay by this to determine final delay time

    public DataEntryBot(int delay) throws Exception{
        this.delay = delay;
    }

    //Give key and press count, press and release key press count times, and multiply delay if provided
    public void pressKey(int key, int press_count, double delay_strength)
    {
        try {

            for(int i = 0; i < press_count; i++){

                this.keyPress(key); 
                this.keyRelease(key);

                this.delay((int) (delay * delay_strength));
            }
        }catch (Exception e) {
            e.printStackTrace();
        }
    }  

    public void pressKey(int key, int press_count){
        pressKey(key, press_count, DELAY_STRENGTH);
    }

    public void pressKey(int key){
        pressKey(key, 1, DELAY_STRENGTH);
    }
}
