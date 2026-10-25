import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import javax.imageio.ImageIO;

/** CI-only screen helper. Never packaged in the mod. */
class CaptureScreen {
    private static int key(String action) {
        return switch (action) {
            case "confirm" -> java.awt.event.KeyEvent.VK_ENTER;
            case "mode" -> java.awt.event.KeyEvent.VK_C;
            case "inventory" -> java.awt.event.KeyEvent.VK_E;
            case "activate" -> java.awt.event.KeyEvent.VK_R;
            case "cancel" -> java.awt.event.KeyEvent.VK_V;
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
                Thread.sleep(180);
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
        if (args.length > 0 && args[0].equals("--server")) {
            try (var input = new BufferedReader(new InputStreamReader(System.in))) {
                String line;
                while ((line = input.readLine()) != null) {
                    if (line.equals("exit")) return;
                    String[] parts = line.split("\\t", -1);
                    try {
                        if (parts[0].equals("press") && parts.length == 2) {
                            run(robot, bounds, "press", null, parts[1]);
                        } else if (parts[0].equals("capture") && parts.length == 3 && !parts[1].isEmpty()) {
                            run(robot, bounds, "capture", parts[1], parts[2]);
                        } else {
                            throw new IllegalArgumentException("Malformed screen-helper command");
                        }
                        System.out.println("OK");
                    } catch (Throwable error) {
                        System.err.println("Screen helper command failed: " + error);
                        System.out.println("ERROR");
                    }
                    System.out.flush();
                }
            }
            return;
        }
        String action = args.length > 1 ? args[1] : null;
        String path = args.length > 0 ? args[0] : null;
        if ("--press".equals(path)) {
            run(robot, bounds, "press", null, action);
        } else if (path != null) {
            run(robot, bounds, "capture", path, action);
        }
    }
}
