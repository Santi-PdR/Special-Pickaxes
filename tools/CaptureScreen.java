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
            int key=args[1].equals("third")?java.awt.event.KeyEvent.VK_F5:java.awt.event.KeyEvent.VK_F1;
            robot.keyPress(key);
            robot.keyRelease(key);
            Thread.sleep(1000);
        }
        ImageIO.write(robot.createScreenCapture(bounds), "png", new File(args[0]));
        System.exit(0); // AWT/X11 event threads must not keep this one-shot helper alive.
    }
}
