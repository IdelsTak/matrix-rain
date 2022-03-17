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
package org.pushingpixels.matrixrain.auxiliary.graphics.colors;

public final class ColorConverter {
  public static final class TripletHSV {
    public double h;

    public boolean h_defined;

    public double s;

    public double v;

    public TripletHSV(double h, double s, double v) {
      this.h = h;
      this.s = s;
      this.v = v;
      this.h_defined = true;
    }

    public TripletHSV(double s, double v) {
      this.s = s;
      this.v = v;
      this.h_defined = false;
    }
  }

  public static final class TripletRGB {
    public double r;

    public double g;

    public double b;

    public TripletRGB(double r, double g, double b) {
      this.r = r;
      this.g = g;
      this.b = b;
    }
  }

  // input: h in [0, 360) or UNDEFINED, s and v in [0,1]
  // output : r, g, b each in [0,1]
  public static TripletRGB HSV_To_RGB(TripletHSV tripletHSV) {
    double v = tripletHSV.v;

    if (!tripletHSV.h_defined) return new TripletRGB(v, v, v);

    double s = tripletHSV.s;
    if (s == 0.0) return null;

    double h = tripletHSV.h;

    if (h == 360.0) h = 0.0;
    h /= 60.0;
    int i = (int) Math.floor(h);
    double f = h - i;
    double p = v * (1.0 - s);
    double q = v * (1 - (s * f));
    double t = v * (1 - s * (1 - f));
    switch (i) {
      case 0 -> {
        return new TripletRGB(v, t, p);
      }
      case 1 -> {
        return new TripletRGB(q, v, p);
      }
      case 2 -> {
        return new TripletRGB(p, v, t);
      }
      case 3 -> {
        return new TripletRGB(p, q, v);
      }
      case 4 -> {
        return new TripletRGB(t, p, v);
      }
      case 5 -> {
        return new TripletRGB(v, p, q);
      }
    }
    return null;
  }

  // input : r, g, b each in [0,1]
  // output: h in [0, 360), s and v in [0,1] except if s=0 then h=UNDEFINED
  public static TripletHSV RGB_To_HSV(TripletRGB tripletRGB) {
    double r = tripletRGB.r;
    double g = tripletRGB.g;
    double b = tripletRGB.b;

    double maxVal = r;
    double minVal = r;
    if (g > maxVal) maxVal = g;
    if (g < minVal) minVal = g;
    if (b > maxVal) maxVal = b;
    if (b < minVal) minVal = b;

    double v = maxVal;
    double s;
    if (maxVal != 0) s = (maxVal - minVal) / maxVal;
    else s = 0.0;

    if (s == 0.0) return new TripletHSV(s, v);

    // chromatic case
    double delta = maxVal - minVal;
    double h;
    if (r == maxVal) {
      h = (g - b) / delta;
    } else {
      if (g == maxVal) {
        h = 2.0 + (b - r) / delta;
      } else {
        h = 4.0 + (r - g) / delta;
      }
    }
    h *= 60.0;
    if (h < 0.0) h += 360.0;
    return new TripletHSV(h, s, v);
  }
}
