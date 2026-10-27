import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.io.File;
import javax.imageio.ImageIO;

/** CI-only screenshot helper. Never packaged in the mod. */
class CaptureScreen {
    private static int key(String action) {
        return switch (action) {
            case "confirm" -> java.awt.event.KeyEvent.VK_ENTER;
            case "mode" -> java.awt.event.KeyEvent.VK_C;
            case "inventory" -> java.awt.event.KeyEvent.VK_E;
            case "activate" -> java.awt.event.KeyEvent.VK_R;
            case "cancel" -> java.awt.event.KeyEvent.VK_X;
            case "shift" -> java.awt.event.KeyEvent.VK_SHIFT;
            case "third" -> java.awt.event.KeyEvent.VK_F5;
            case "clean" -> java.awt.event.KeyEvent.VK_F1;
            default -> java.awt.event.KeyEvent.VK_F1;
        };
    }

    private static void run(Robot robot, Rectangle bounds, String operation,
            String path, String action) throws Exception {
        if (action != null && !action.isEmpty()) {
            int key = key(action);
            robot.keyPress(key);
            Thread.sleep(120);
            if (!action.equals("shift")) robot.keyRelease(key);
            if (operation.equals("press")) {
                Thread.sleep(1000);
                return;
            }
            Thread.sleep(500);
        }
        var image = robot.createScreenCapture(bounds);
        ImageIO.write(image, "png", new File(path));
        ImageIO.write(image, "jpg", new File(path.substring(0, path.length() - 4) + ".jpg"));
        if ("shift".equals(action)) robot.keyRelease(java.awt.event.KeyEvent.VK_SHIFT);
    }

    public static void main(String[] args) throws Exception {
        var bounds = new Rectangle(Toolkit.getDefaultToolkit().getScreenSize());
        var robot = new Robot();
        String action = args.length > 1 ? args[1] : null;
        String path = args.length > 0 ? args[0] : null;
        if ("--press".equals(path)) {
            run(robot, bounds, "press", null, action);
            System.exit(0);
        } else if (path != null) {
            run(robot, bounds, "capture", path, action);
            System.exit(0);
        }
    }
}
