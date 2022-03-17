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
package com.github.idelstak.matrixrain.auxiliary.graphics.colors.interpolator;

import java.awt.Color;

import com.github.idelstak.matrixrain.auxiliary.graphics.colors.ColorConverter;

public class ColorInterpolatorHSV implements ColorInterpolator {
  private Color[] colors;

  private Color color1;

  private Color color2;

  private int count;

  public ColorInterpolatorHSV(Color color1, Color color2, int count) {
    this.color1 = color1;
    this.color2 = color2;
    this.count = count;

    this.colors = new Color[this.count + 1];
    double r1 = this.color1.getRed();
    double g1 = this.color1.getGreen();
    double b1 = this.color1.getBlue();
    ColorConverter.TripletHSV tripletHSV1 =
        ColorConverter.RGB_To_HSV(
            new ColorConverter.TripletRGB(r1 / 256.0, g1 / 256.0, b1 / 256.0));
    double h1 = tripletHSV1.h;
    double s1 = tripletHSV1.s;
    double v1 = tripletHSV1.v;
    double r2 = this.color2.getRed();
    double g2 = this.color2.getGreen();
    double b2 = this.color2.getBlue();
    ColorConverter.TripletHSV tripletHSV2 =
        ColorConverter.RGB_To_HSV(
            new ColorConverter.TripletRGB(r2 / 256.0, g2 / 256.0, b2 / 256.0));
    double h2 = tripletHSV2.h;
    double s2 = tripletHSV2.s;
    double v2 = tripletHSV2.v;
    for (int i = 0; i <= this.count; i++) {
      double h = h1 + (double) i * (h2 - h1) / (double) this.count;
      double s = s1 + (double) i * (s2 - s1) / (double) this.count;
      double v = v1 + (double) i * (v2 - v1) / (double) this.count;

      ColorConverter.TripletRGB tripletRGB =
          ColorConverter.HSV_To_RGB(new ColorConverter.TripletHSV(h, s, v));

      this.colors[i] =
          new Color(
              (int) (256.0 * tripletRGB.r),
              (int) (256.0 * tripletRGB.g),
              (int) (256.0 * tripletRGB.b));
    }
  }

  // input: coef in [0, 1]
  public Color getInterpolatedColor(double coef) {
    int index = (int) (coef * this.count);
    if (index < 0) index = 0;
    if (index > this.count) index = this.count;

    return this.colors[index];
  }
}
