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
package org.pushingpixels.matrixrain.auxiliary.graphics.colors.manager;

import java.awt.Color;

import org.pushingpixels.matrixrain.auxiliary.graphics.colors.interpolator.ColorInterpolator;
import org.pushingpixels.matrixrain.auxiliary.graphics.colors.interpolator.ColorInterpolatorRGB;

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
