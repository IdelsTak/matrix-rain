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

import java.applet.Applet;
import java.awt.Color;
import java.awt.Image;
import java.awt.image.MemoryImageSource;
import java.awt.image.PixelGrabber;

import com.github.idelstak.matrixrain.auxiliary.graphics.colors.manager.ColorManager;

public class TrueColorBitmapObject {
  private int[][] bitmap;

  private int width, height;

  public TrueColorBitmapObject() {
    this.width = 0;
    this.height = 0;
    this.bitmap = null;
  }

  public TrueColorBitmapObject(int[][] bitmap, int width, int height) {
    this.width = width;
    this.height = height;
    this.bitmap = bitmap;
  }

  public TrueColorBitmapObject(IndexBitmapObject indexBitmapObject, ColorManager colorManager) {
    if (indexBitmapObject == null) {
      this.width = 0;
      this.height = 0;
      this.bitmap = null;
    } else {
      this.width = indexBitmapObject.getWidth();
      this.height = indexBitmapObject.getHeight();
      int[][] oldBitmap = indexBitmapObject.getBitmap();
      int[][] newBitmap = new int[this.width][this.height];
      for (int i = 0; i < this.width; i++) {
        for (int j = 0; j < this.height; j++) {
          newBitmap[i][j] = colorManager.getColor(oldBitmap[i][j]).getRGB();
        }
      }
      this.bitmap = newBitmap;
    }
  }

  public TrueColorBitmapObject(Image image, int width, int height) {
    this.width = width;
    this.height = height;
    int[] origPixels = new int[this.height * this.width];
    PixelGrabber pg =
        new PixelGrabber(image, 0, 0, this.width, this.height, origPixels, 0, this.width);
    try {
      pg.grabPixels();
    } catch (InterruptedException e) {
      this.width = 0;
      this.height = 0;
      this.bitmap = null;
      return;
    }
    if ((pg.getStatus() & java.awt.image.ImageObserver.ABORT) != 0) {
      this.width = 0;
      this.height = 0;
      this.bitmap = null;
      return;
    }

    this.bitmap = new int[this.width][this.height];
    for (int x = 0; x < this.width; x++)
      for (int y = 0; y < this.height; y++) this.bitmap[x][y] = origPixels[y * this.width + x];
  }

  public TrueColorBitmapObject(Image image) {
    this.width = image.getWidth(null);
    this.height = image.getHeight(null);
    int[] origPixels = new int[this.height * this.width];
    PixelGrabber pg =
        new PixelGrabber(image, 0, 0, this.width, this.height, origPixels, 0, this.width);
    try {
      pg.grabPixels();
    } catch (InterruptedException e) {
      this.width = 0;
      this.height = 0;
      this.bitmap = null;
      return;
    }
    if ((pg.getStatus() & java.awt.image.ImageObserver.ABORT) != 0) {
      this.width = 0;
      this.height = 0;
      this.bitmap = null;
      return;
    }

    this.bitmap = new int[this.width][this.height];
    for (int x = 0; x < this.width; x++)
      for (int y = 0; y < this.height; y++) this.bitmap[x][y] = origPixels[y * this.width + x];
  }

  @Override
  public Object clone() throws CloneNotSupportedException {
    super.clone();
    TrueColorBitmapObject newObject = new TrueColorBitmapObject();
    if (this.bitmap != null) {
      newObject.width = this.width;
      newObject.height = this.height;
      int[][] newBitmap = new int[this.width][this.height];
      for (int i = 0; i < this.width; i++)
        System.arraycopy(this.bitmap[i], 0, newBitmap[i], 0, this.height);
      newObject.bitmap = newBitmap;
    }
    return newObject;
  }

  public int getWidth() {
    return width;
  }

  public int getHeight() {
    return height;
  }

  public int[][] getBitmap() {
    return bitmap;
  }

  public void setWidth(int width) {
    this.width = width;
  }

  public void setHeight(int height) {
    this.height = height;
  }

  public void setBitmap(int[][] bitmap) {
    this.bitmap = bitmap;
  }

  public void reset(int initValue) {
    for (int i = 0; i < this.width; i++)
      for (int j = 0; j < this.height; j++) this.bitmap[i][j] = initValue;
  }

  public Image getAsImage(Applet applet) {
    int pixels[] = new int[this.width * this.height];

    for (int i = 0; i < this.width; i++)
      for (int j = 0; j < this.height; j++) pixels[j * this.width + i] = this.bitmap[i][j];

    MemoryImageSource blendMIS =
        new MemoryImageSource(this.width, this.height, pixels, 0, this.width);
    Image image = applet.createImage(blendMIS);
    return image;
  }

  public void applyVerticalGradient(Color leftColor, Color rightColor, double swerve) {
    int lcR = leftColor.getRed();
    int lcG = leftColor.getGreen();
    int lcB = leftColor.getBlue();
    int rcR = rightColor.getRed();
    int rcG = rightColor.getGreen();
    int rcB = rightColor.getBlue();
    for (int col = 0; col < this.width; col++) {
      double coef = (double) col / (double) this.width;
      int swerveR = (int) (lcR + coef * (rcR - lcR));
      int swerveG = (int) (lcG + coef * (rcG - lcG));
      int swerveB = (int) (lcB + coef * (rcB - lcB));
      for (int row = 0; row < this.height; row++) {
        int oldR = (this.bitmap[col][row] & 0x00FF0000) >> 16;
        int oldG = (this.bitmap[col][row] & 0x0000FF00) >> 8;
        int oldB = (this.bitmap[col][row] & 0x000000FF);

        int luminance = (int) ((222.0 * oldR + 707.0 * oldG + 71.0 * oldB) / 1000.0);
        oldR = luminance;
        oldG = luminance;
        oldB = luminance;

        int newR = (int) (oldR + swerve * (swerveR - oldR));
        int newG = (int) (oldG + swerve * (swerveG - oldG));
        int newB = (int) (oldB + swerve * (swerveB - oldB));

        this.bitmap[col][row] = (255 << 24) | (newR << 16) | (newG << 8) | newB;
      }
    }
  }
}
