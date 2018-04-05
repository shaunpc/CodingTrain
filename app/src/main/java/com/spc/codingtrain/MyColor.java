package com.spc.codingtrain;

import java.util.Random;

/****
 *  MyColor - able to store usual RGB values, but also offers random colour generator, and
 *  a colour-shifter method.
 */

public class MyColor {
    int r,g,b;
    private Random random = new Random();

    MyColor (int r, int g, int b) {
        this.r = r;
        this.g = g;
        this.b = b;
    }

    MyColor () {
        int COLOUR_BASE = 50;    // start random above this
        // give a random colour
        this.r = random.nextInt(255 - COLOUR_BASE) + COLOUR_BASE;
        this.g = random.nextInt(255 - COLOUR_BASE) + COLOUR_BASE;
        this.b = random.nextInt(255 - COLOUR_BASE) + COLOUR_BASE;
    }

    public MyColor shift () {
        int COLOUR_SHIFT= 20;    // max shift colour amount
        int newR = this.r + random.nextInt(COLOUR_SHIFT * 2) - COLOUR_SHIFT;
        int newG = this.g + random.nextInt(COLOUR_SHIFT * 2) - COLOUR_SHIFT;
        int newB = this.b + random.nextInt(COLOUR_SHIFT * 2) - COLOUR_SHIFT;
        if (newR>=0 && newR<=255) {newR = this.r;}
        if (newG>=0 && newG<=255) {newG = this.g;}
        if (newB>=0 && newB<=255) {newB = this.b;}
        return new MyColor(newR, newG, newB);

    }

}
