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
