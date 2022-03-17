/*
 * Copyright (c) 2003-2017 Matrix Rain, Kirill Grouchnikov. All Rights Reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *  o Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 *
 *  o Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 *  o Neither the name of Matrix Rain, Kirill Grouchnikov nor the names of
 *    its contributors may be used to endorse or promote products derived
 *    from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS;
 * OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE
 * OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE,
 * EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.pushingpixels.matrixrain.auxiliary.math.intersect;

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
