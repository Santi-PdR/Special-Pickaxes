import javax.imageio.ImageIO;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
/** Image-only processing of genuine captures; never controls a window or starts Minecraft. */
class ResizePortrait {
    public static void main(String[] args) throws Exception {
        var source=ImageIO.read(new File(args[0]));
        if(source==null)throw new IllegalArgumentException("Unreadable capture");
        double ratio=Math.min(1,Math.min(640D/source.getWidth(),360D/source.getHeight()));
        var target=new BufferedImage((int)(source.getWidth()*ratio),(int)(source.getHeight()*ratio),BufferedImage.TYPE_INT_RGB);
        var graphics=target.createGraphics();
        graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION,RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        graphics.drawImage(source,0,0,target.getWidth(),target.getHeight(),null);graphics.dispose();
        if(!ImageIO.write(target,"jpg",new File(args[1])))throw new IllegalStateException("Missing JPEG writer");
    }
}
