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

import java.applet.Applet;
import java.awt.Color;
import java.awt.Image;
import java.awt.image.MemoryImageSource;
import java.awt.image.PixelGrabber;

import org.pushingpixels.matrixrain.auxiliary.graphics.colors.manager.ColorManager;

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
