package cz.tefek.ymd2.util;

public class CubicBezier
{
    private final double a;
    private final double b;
    private final double c;
    private final double d;
    private final int iterations = 16;

    public CubicBezier(double cpx1, double cpy1, double cpx2, double cpy2)
    {
        if (cpx1 < 0 || cpx1 > 1 || cpx2 < 0 || cpx2 > 1)
        {
            throw new RuntimeException("Parameter out of range, only 0..1 is supported (only one Y value for each X).");
        }

        this.a = cpx1;
        this.b = cpy1;
        this.c = cpx2;
        this.d = cpy2;
    }

    public double forX(double xIn)
    {
        double t = 0.5;

        double x;
        double y = 3 * (1 - t) * (1 - t) * t * this.b + 3 * (1 - t) * t * t * this.d + t * t * t;

        double delta = 0.25;
        boolean uh;

        for (int i = 0; i < this.iterations; i++)
        {
            x = 3 * (1 - t) * (1 - t) * t * this.a + 3 * (1 - t) * t * t * this.c + t * t * t;
            y = 3 * (1 - t) * (1 - t) * t * this.b + 3 * (1 - t) * t * t * this.d + t * t * t;

            uh = x > xIn;

            t += uh ? -delta : delta;

            delta /= 2;
        }

        return y;
    }
}
