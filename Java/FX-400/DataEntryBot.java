import java.awt.Robot;

public class DataEntryBot extends Robot{

    private int delay = 200; //default 200. Delay time between key presses
    private double DELAY_STRENGTH = 0; //Default 0. Multiply delay by this to determine final delay time

    public DataEntryBot(int delay) throws Exception{
        this.delay = delay;
    }

    //Give key and distance, press and release key distance times, and multiply delay if provided
    public void pressKey(int key, int target_distance, double delay_strength)
    {
        try {

            for(int i = 0; i < target_distance; i++){

                this.keyPress(key); 
                this.keyRelease(key);

                this.delay((int) (delay * delay_strength));
            }
        }catch (Exception e) {
            e.printStackTrace();
        }
    }  

    public void pressKey(int key, int target_distance){
        pressKey(key, target_distance, DELAY_STRENGTH);
    }

    public void pressKey(int key){
        pressKey(key, 1, DELAY_STRENGTH);
    }
}
