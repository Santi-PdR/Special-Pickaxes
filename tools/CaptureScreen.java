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
            int key=args[1].equals("mode")?java.awt.event.KeyEvent.VK_C:args[1].equals("inventory")?java.awt.event.KeyEvent.VK_E:args[1].equals("activate")?java.awt.event.KeyEvent.VK_R:args[1].equals("cancel")?java.awt.event.KeyEvent.VK_V:args[1].equals("shift")?java.awt.event.KeyEvent.VK_SHIFT:args[1].equals("third")?java.awt.event.KeyEvent.VK_F5:java.awt.event.KeyEvent.VK_F1;
            robot.keyPress(key);Thread.sleep(120);
            if(!args[1].equals("shift"))robot.keyRelease(key);
            Thread.sleep(1000);
        }
        ImageIO.write(robot.createScreenCapture(bounds), "png", new File(args[0]));
        if(args.length>1&&args[1].equals("shift"))robot.keyRelease(java.awt.event.KeyEvent.VK_SHIFT);
        System.exit(0); // AWT/X11 event threads must not keep this one-shot helper alive.
    }
}
