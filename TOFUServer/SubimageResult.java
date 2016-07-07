

import java.awt.Color;
import java.io.Serializable;
import raja.RGB;
import raja.renderer.Resolution;

/**
 * @author Niklas Therning
 * @version $Id: SubimageResult.java,v 1.1 2005/11/07 14:47:16 i041184 Exp $
 */
public class SubimageResult implements Serializable {
    private byte[] colors = null;
    private int width = 0;
    private int height = 0;
    private int current = 0;
    private int x1 = 0;
    private int y1 = 0;
    private int x2 = 0;
    private int y2 = 0;

    public SubimageResult(int xStart,int xWidth,int yStart,int yWidth) {
        x1 = xStart;
        y1 = yStart;
        x2 = xStart + xWidth -1;
        y2 = yStart + yWidth -1;
        this.width = xWidth;
        this.height = yWidth;
        colors = new byte[3*width*height];
    }
    
    public void setColor(int x, int y, RGB color) {
        int base = 3*(y*width+x);
        colors[base++] = (byte)color.getColor().getRed();
        colors[base++] = (byte)color.getColor().getGreen();
        colors[base++] = (byte)color.getColor().getBlue();
    }

    public Pixel getNextPixel() {
        int x = x1+(current/3)%width;
        int y = y1+(current/3)/width;
        int r = (int)colors[current++];
        int g = (int)colors[current++];
        int b = (int)colors[current++];
        if (r<0) {
            r &= 0xff;
        }
        if (g<0) {
            g &= 0xff;
        }
        if (b<0) {
            b &= 0xff;
        }
        RGB color = new RGB(new Color(r, g, b));
        return new Pixel(x, y, color);
    }

    public boolean hasNextPixel() {
        return current < colors.length;
    }

    public void reset() {
        current = 0;
    }
}
