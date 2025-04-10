import java.awt.Robot;
import java.awt.event.KeyEvent;

public class FSAE {
    

    public static void main(String[] args) throws Exception{
        Robot robot = new Robot();
        int delay = 1000;
        Thread.sleep(1000);
        Thread.sleep(1000);
        robot.keyPress(KeyEvent.VK_SHIFT);Thread.sleep(1000);
        robot.keyPress(KeyEvent.VK_F10);Thread.sleep(1000);
        robot.keyRelease(KeyEvent.VK_SHIFT);Thread.sleep(1000);
        robot.keyRelease(KeyEvent.VK_F10);Thread.sleep(1000);
    }

    // public FSAE(){
    //     try {
    //         robot = new Robot();
    //     } catch (Exception e) {
    //         System.err.println(e);
    //     }
    // }
    

    // public void open(){
    //     try {
    //         Thread.sleep(5000);
    //         robot.keyPress(KeyEvent.VK_SHIFT);
    //         robot.keyPress(KeyEvent.VK_F10);
    //         robot.keyRelease(KeyEvent.VK_SHIFT);
    //         robot.keyRelease(KeyEvent.VK_F10);
    //         robot.delay(delay);
    //         robot.keyPress(KeyEvent.VK_DOWN); robot.keyRelease(KeyEvent.VK_DOWN);
    //         robot.delay(delay);
    //         robot.keyPress(KeyEvent.VK_ENTER); robot.keyRelease(KeyEvent.VK_ENTER);
    //         robot.delay(delay);
            
    //     } catch (Exception e) {
    //         System.err.println(e);
    //     }
    // }
    

    // public void addPhotoDetector(){
    //     try {
    //         open();
    //         robot.keyPress(KeyEvent.VK_ENTER); robot.keyRelease(KeyEvent.VK_ENTER);
    //         robot.delay(delay);
    //         robot.keyPress(KeyEvent.VK_ESCAPE); robot.keyRelease(KeyEvent.VK_ESCAPE);
    //         robot.delay(delay);
    //         robot.keyPress(KeyEvent.VK_END); robot.keyRelease(KeyEvent.VK_END);
            
    //     } catch (Exception e) {
    //         System.err.println(e);
    //     }
    // }


    // public void addAlarmInputMod(){
    //     try {
    //         open();
    //         robot.keyPress(KeyEvent.VK_D); robot.keyRelease(KeyEvent.VK_D); robot.delay(delay);
    //         robot.keyPress(KeyEvent.VK_D); robot.keyRelease(KeyEvent.VK_D); robot.delay(delay);

    //         robot.keyPress(KeyEvent.VK_ENTER); robot.keyRelease(KeyEvent.VK_ENTER); robot.delay(delay);
    //         robot.keyPress(KeyEvent.VK_ESCAPE); robot.keyRelease(KeyEvent.VK_ESCAPE); robot.delay(delay);

    //         robot.keyPress(KeyEvent.VK_END); robot.keyRelease(KeyEvent.VK_END); robot.delay(delay);
            
    //     } catch (Exception e) {
    //         System.err.println(e);
    //     }
    // }


    // public void addNonLatchedSupv(){
    //     try {
    //         open();
    //         robot.keyPress(KeyEvent.VK_D); robot.keyRelease(KeyEvent.VK_D); robot.delay(delay);
    //         robot.keyPress(KeyEvent.VK_D); robot.keyRelease(KeyEvent.VK_D); robot.delay(delay);

    //         robot.keyPress(KeyEvent.VK_TAB); robot.keyRelease(KeyEvent.VK_TAB); robot.delay(delay);
    //         robot.keyPress(KeyEvent.VK_TAB); robot.keyRelease(KeyEvent.VK_TAB); robot.delay(delay);

    //         robot.keyPress(KeyEvent.VK_N); robot.keyRelease(KeyEvent.VK_N); robot.delay(delay);

    //         robot.keyPress(KeyEvent.VK_ENTER); robot.keyRelease(KeyEvent.VK_ENTER); robot.delay(delay);
    //         robot.keyPress(KeyEvent.VK_ESCAPE); robot.keyRelease(KeyEvent.VK_ESCAPE); robot.delay(delay);
    //         robot.keyPress(KeyEvent.VK_END); robot.keyRelease(KeyEvent.VK_END); robot.delay(delay);
    //     } catch (Exception e) {
    //         System.err.println(e);
    //     }
    // }



    // public void addLatchedSupv(){
    //     try {
    //         open();
    //         robot.keyPress(KeyEvent.VK_D); robot.keyRelease(KeyEvent.VK_D); robot.delay(delay);
    //         robot.keyPress(KeyEvent.VK_D); robot.keyRelease(KeyEvent.VK_D); robot.delay(delay);

    //         robot.keyPress(KeyEvent.VK_TAB); robot.keyRelease(KeyEvent.VK_TAB); robot.delay(delay);
    //         robot.keyPress(KeyEvent.VK_TAB); robot.keyRelease(KeyEvent.VK_TAB); robot.delay(delay);

    //         robot.keyPress(KeyEvent.VK_L); robot.keyRelease(KeyEvent.VK_L); robot.delay(delay);

    //         robot.keyPress(KeyEvent.VK_ENTER); robot.keyRelease(KeyEvent.VK_ENTER); robot.delay(delay);
    //         robot.keyPress(KeyEvent.VK_ESCAPE); robot.keyRelease(KeyEvent.VK_ESCAPE); robot.delay(delay);
    //         robot.keyPress(KeyEvent.VK_END); robot.keyRelease(KeyEvent.VK_END); robot.delay(delay);
    //     } catch (Exception e) {
    //         System.err.println(e);
    //     }
    // }
}
