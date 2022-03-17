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
package org.pushingpixels.matrixrain.auxiliary.graphics.colors.interpolator;

import java.awt.Color;

import org.pushingpixels.matrixrain.auxiliary.graphics.colors.ColorConverter;

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
