import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.io.File;
import javax.imageio.ImageIO;

/** CI-only screenshot helper. Never packaged in the mod. */
class CaptureScreen {
    public static void main(String[] args) throws Exception {
        var bounds = new Rectangle(Toolkit.getDefaultToolkit().getScreenSize());
        var robot = new Robot();
        if (args.length > 1) {
            robot.keyPress(java.awt.event.KeyEvent.VK_F1);
            robot.keyRelease(java.awt.event.KeyEvent.VK_F1);
            Thread.sleep(1000);
        }
        ImageIO.write(robot.createScreenCapture(bounds), "png", new File(args[0]));
    }
}
