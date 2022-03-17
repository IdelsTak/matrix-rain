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
package org.pushingpixels.matrixrain.auxiliary.graphics;

import java.awt.Component;
import java.awt.Image;
import java.awt.image.MemoryImageSource;

import org.pushingpixels.matrixrain.auxiliary.graphics.colors.manager.ColorManager;
import org.pushingpixels.matrixrain.auxiliary.math.GaussValues;
import org.pushingpixels.matrixrain.auxiliary.math.intersect.Circle1PixelArbitraryIntersectorFactory;

public final class IndexImageManager {
  private int windowWidth, windowHeight;

  private Circle1PixelArbitraryIntersectorFactory iFct;

  // off-screen image
  private ColorManager colorManager;

  private short[] ciBitmap;

  private int[] bitmap;

  private MemoryImageSource baseMImage;

  private Image baseImage;

  public IndexImageManager(
      Component comp, Circle1PixelArbitraryIntersectorFactory iFct, ColorManager colorManager) {

    this.windowWidth = comp.getWidth();
    this.windowHeight = comp.getHeight();
    this.bitmap = new int[windowWidth * windowHeight];
    this.ciBitmap = new short[windowWidth * windowHeight];

    for (int i = 0; i < windowWidth * windowHeight; i++) {
      this.ciBitmap[i] = 0;
      this.bitmap[i] = 0xFF000000;
    }

    this.iFct = iFct;
    this.colorManager = colorManager;

    baseMImage = new MemoryImageSource(windowWidth, windowHeight, bitmap, 0, windowWidth);
    baseMImage.setAnimated(true);
    baseImage = comp.createImage(baseMImage);
  }

  public void resetImage() {
    for (int i = 0; i < windowWidth * windowHeight; i++) {
      this.ciBitmap[i] = 0;
      this.bitmap[i] = 0xFF000000;
    }
  }

  private void paintPixel(int index, int value) {
    this.ciBitmap[index] = (short) (Math.max(value, this.ciBitmap[index]));
  }

  public void paintPixel(int x, int y, int value) {
    if ((x < 0) || (x >= this.windowWidth)) return;
    if ((y < 0) || (y >= this.windowHeight)) return;
    this.paintPixel(y * this.windowWidth + x, value);
  }

  public void paintAAPixel(double x, double y, int value) {
    int iFctN = iFct.N;

    int w = 2;
    int startCol = (int) Math.floor(x - w);
    if (startCol < 0) startCol = 0;
    int endCol = (int) Math.ceil(x + w);
    if (endCol >= this.windowWidth) endCol = this.windowWidth - 1;
    int colN = (int) ((startCol - x) * iFctN);

    int startRow = (int) Math.floor(y - w);
    if (startRow < 0) startRow = 0;
    int endRow = (int) Math.ceil(y + w);
    if (endRow >= this.windowHeight) endRow = this.windowHeight - 1;
    int rowN = (int) ((startRow - y) * iFctN);

    // System.out.println("new pixel");
    for (int newCol = startCol; newCol <= endCol; newCol++) {
      for (int newRow = startRow; newRow <= endRow; newRow++) {
        double dx = newCol + 0.5 - x;
        double dy = newRow + 0.5 - y;
        double dist = Math.sqrt(dx * dx + dy * dy) / 1.25;
        // gauss: 0.5 --> 1, 1.0 --> 0
        double coef = 2 * (1 - GaussValues.getValue(dist));
        // System.out.println("dist: " + dist + ", coef: " + coef + ",
        // val: " + (int)(value*coef));
        this.paintPixel(newRow * this.windowWidth + newCol, (int) (value * coef));
        rowN += iFctN;
      }
      colN += iFctN;
    }
  }

  public synchronized void recomputeImage() {
    // compute actual colors
    int totalSize = this.windowHeight * this.windowWidth;
    for (int i = 0; i < totalSize; i++)
      this.bitmap[i] = colorManager.getColorPresentation((int) (this.ciBitmap[i]));
    baseMImage.newPixels();
  }

  public synchronized int[] getBitmap1D() {
    return this.bitmap;
  }

  public synchronized Image getImage() {
    return baseImage;
  }

  public int[][] getAsBitmap() {
    int[][] finalBitmap = new int[windowWidth][windowHeight];
    for (int i = 0; i < windowWidth; i++)
      for (int j = 0; j < windowHeight; j++) finalBitmap[i][j] = this.ciBitmap[j * windowWidth + i];
    return finalBitmap;
  }

  public int getWindowHeight() {
    return windowHeight;
  }

  public int getWindowWidth() {
    return windowWidth;
  }
}
