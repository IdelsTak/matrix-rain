/*
 * The MIT License
 * Copyright © 2022 Hiram K
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.github.idelstak.matrixrain.auxiliary.math.intersect;

public final class Intersector {
  public static long delta = 0;

  public static double S(double x, double r) {
    double r2 = r * r;
    if (x >= 0)
      return Math.PI * r2 / 4.0 - r2 * Math.asin(x / r) / 2.0 - x * Math.sqrt(r2 - x * x) / 2.0;
    else return 0.0;
  }

  public static double SR(double x, double r) {
    double r2 = r * r;
    if (x < -r) return Math.PI * r2;
    if (x < 0) return Math.PI * r2 - 2.0 * S(-x, r);
    if (x <= r) return 2 * S(x, r);
    return 0;
  }

  public static double SXY(double x, double y, double r) {
    double r2 = r * r;
    if ((x >= 0) && (y >= 0) && ((x * x + y * y) <= r2)) {
      double x2 = Math.sqrt(r2 - y * y);
      return S(x, r) - (x2 - x) * y - S(x2, r);
    } else return 0.0;
  }

  public static double SRXY(double x, double y, double r) {
    double r2 = r * r;
    boolean flag = ((x * x + y * y) < r2);
    if (flag) {
      if (x >= 0)
        if (y >= 0) return SXY(x, y, r);
        else return SR(x, r) - SXY(x, -y, r);
      else if (y >= 0) return SR(y, r) - SXY(-x, y, r);
      else return Math.PI * r2 - SR(-x, r) - SR(-y, r) + SXY(-x, -y, r);
    } else {
      if (x >= 0)
        if (y >= 0) return 0;
        else return SR(x, r);
      else if (y >= 0) return SR(y, r);
      else return Math.PI * r2 - SR(-x, r) - SR(-y, r);
    }
  }

  public static double intersectionArea(double r, double x1, double y1, double x2, double y2) {
    double time0 = System.currentTimeMillis();
    double result =
        Math.PI * r * r
            - SR(-x1, r)
            - SR(x2, r)
            - SR(-y1, r)
            - SR(y2, r)
            + SRXY(x2, y2, r)
            + SRXY(x2, -y1, r)
            + SRXY(-x1, y2, r)
            + SRXY(-x1, -y1, r);
    delta += (System.currentTimeMillis() - time0);
    return result;
  }
}
