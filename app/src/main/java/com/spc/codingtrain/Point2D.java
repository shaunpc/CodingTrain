package com.spc.codingtrain;

public class Point2D {
    private double x;
    private double y;

    Point2D (double x, double y) {
        this.x = x;
        this.y = y;
    }

    public void add (Point2D p) {
        this.x += p.x;
        this.y += p.y;
    }

    public void add (int xa, int ya) {
        this.x += xa;
        this.y += ya;
    }

    public void clear() {
        this.x = 0;
        this.y = 0;
    }

    public void mult (Point2D p) {
        this.x *= p.x;
        this.y *= p.y;
    }

    public void mult (int xm, int ym) {
        this.x *= xm;
        this.y *= ym;
    }

    public float getX() {
        return (float) this.x;
    }

    public float getY() {
        return (float) this.y;
    }

    public boolean inRangeX (double lower, double upper) {
        return (this.x >= lower && this.x <= upper);
    }
    public boolean inRangeY (double lower, double upper) {
        return (this.y >= lower && this.y <= upper);
    }

    public float angle () {
        return (float) (Math.atan(this.y/this.x));

    }

    public double distance (Point2D p) {
        return Math.sqrt(Math.pow(this.x - p.x, 2)+Math.pow(this.y - p.y, 2));
    }

    public double distance (int xd, int yd) {
        return Math.sqrt(Math.pow(this.x - xd, 2)+Math.pow(this.y - yd, 2));
    }
}