import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.io.File;
import javax.imageio.ImageIO;

/** CI-only screenshot helper. Never packaged in the mod. */
class CaptureScreen {
    public static void main(String[] args) throws Exception {
        var bounds = new Rectangle(Toolkit.getDefaultToolkit().getScreenSize());
        ImageIO.write(new Robot().createScreenCapture(bounds), "png", new File(args[0]));
    }
}
