import java.awt.Robot;

public class DataEntryBot extends Robot{

    private int delay;

    public DataEntryBot(int delay) throws Exception{
        this.delay = delay;
    }

    public DataEntryBot() throws Exception{
        this(200);
    }

    //Give key and distance, press and release key distance times, and delay if provided
    public void pressKey(int key, int targetDistance, int threadSleep)
    {
        try {
            Thread.sleep(delay);

            for(int i = 0; i < targetDistance; i++){
            
                this.keyPress(key); 
                this.keyRelease(key);

                this.delay(delay);

                if (threadSleep > 0)
                {
                    Thread.sleep(15);
                }
            }
        }catch (Exception e) {
            // TODO: handle exception
        }
    }  

    public void pressKey(int key, int targetDistance)
    {
        pressKey(key, targetDistance, 0);
    }

    public void pressKey(int key)
    {
        pressKey(key, 1, 0);
    }
}
