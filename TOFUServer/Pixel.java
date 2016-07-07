import java.io.Serializable;

import raja.RGB;

public class Pixel implements Serializable{
    private int x;
    private int y;
    private RGB rgb = null;

    public Pixel(int x, int y, RGB rgb) {
        this.x = x;
        this.y = y;
        this.rgb = rgb;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public RGB getRGB() {
        return rgb;
    }
}
