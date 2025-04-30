import java.awt.Robot;

public class DataEntryBot extends Robot{

    private int delay; //default 200
    private int DELAY_STRENGTH = 0; //multiply delay by this to determine final delay time

    public DataEntryBot(int delay) throws Exception{
        this.delay = delay;
    }

    public DataEntryBot() throws Exception{
        this(200);
    }

    //Give key and distance, press and release key distance times, and multiply delay if provided
    public void pressKey(int key, int target_distance, int delay_strength)
    {
        try {

            for(int i = 0; i < target_distance; i++){

                this.keyPress(key); 
                this.keyRelease(key);

                this.delay(delay * delay_strength);
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
