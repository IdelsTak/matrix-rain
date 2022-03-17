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
package com.github.idelstak.matrixrain.auxiliary.graphics.colors.manager;

import java.awt.Color;

import com.github.idelstak.matrixrain.auxiliary.graphics.colors.interpolator.ColorInterpolator;
import com.github.idelstak.matrixrain.auxiliary.graphics.colors.interpolator.ColorInterpolatorRGB;

public class ColorManager1ColorScheme implements ColorManager {
  private final int[] colorBytes;

  private final Color[] colors;

  private final ColorInterpolator colorInterpolatorLight;

  private final ColorInterpolator colorInterpolatorDark;

  private final ColorInterpolator colorInterpolatorMaster;

  private final Color masterColor;

  public ColorManager1ColorScheme(Color masterColor) {
    colorBytes = new int[256];
    colors = new Color[256];
    this.masterColor = masterColor;

    int masterR = masterColor.getRed();
    int masterG = masterColor.getGreen();
    int masterB = masterColor.getBlue();

    // first 240 - black-master
    // last 16 - master-white
    for (int i = 0; i < 240; i++) {
      int redComp = (int) ((double) (i) * masterR / 240);
      int greenComp = (int) ((double) (i) * masterG / 240);
      int blueComp = (int) ((double) (i) * masterB / 240);
      colorBytes[i] = (255 << 24) | (redComp << 16) | (greenComp << 8) | blueComp;
      colors[i] = new Color(colorBytes[i]);
    }
    for (int i = 0; i < 16; i++) {
      int redComp = masterR + (255 - masterR) * i / 15;
      int greenComp = masterG + (255 - masterG) * i / 15;
      int blueComp = masterB + (255 - masterB) * i / 15;
      colorBytes[240 + i] = (255 << 24) | (redComp << 16) | (greenComp << 8) | blueComp;
      colors[240 + i] = new Color(colorBytes[240 + i]);
    }
    this.colorInterpolatorLight = new ColorInterpolatorRGB(colors[240], colors[250], 100);
    this.colorInterpolatorDark = new ColorInterpolatorRGB(colors[0], colors[239], 100);
    this.colorInterpolatorMaster = new ColorInterpolatorRGB(colors[0], colors[250], 200);
  }

  @Override
  public int getColorPresentation(int index) {
    return colorBytes[index];
  }

  @Override
  public Color getColor(int index) {
    return colors[index];
  }

  @Override
  public Color getMasterColor() {
    return this.masterColor;
  }

  @Override
  public Color getDarkColor() {
    return colors[20];
  }

  @Override
  public Color getMidColor() {
    return colors[200];
  }

  @Override
  public Color getLightColor() {
    return colors[250];
  }

  @Override
  public ColorInterpolator getColorInterpolatorLight() {
    return this.colorInterpolatorLight;
  }

  @Override
  public ColorInterpolator getColorInterpolatorDark() {
    return this.colorInterpolatorDark;
  }

  @Override
  public ColorInterpolator getColorInterpolatorMaster() {
    return this.colorInterpolatorMaster;
  }
}
