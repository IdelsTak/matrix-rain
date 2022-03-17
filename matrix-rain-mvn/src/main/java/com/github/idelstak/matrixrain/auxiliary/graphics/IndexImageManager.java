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
package com.github.idelstak.matrixrain.auxiliary.graphics;

import java.awt.Component;
import java.awt.Image;
import java.awt.image.MemoryImageSource;

import com.github.idelstak.matrixrain.auxiliary.graphics.colors.manager.ColorManager;
import com.github.idelstak.matrixrain.auxiliary.math.GaussValues;
import com.github.idelstak.matrixrain.auxiliary.math.intersect.Circle1PixelArbitraryIntersectorFactory;

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
