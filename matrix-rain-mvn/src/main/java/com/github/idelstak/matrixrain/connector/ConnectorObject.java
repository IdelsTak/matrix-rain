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
package com.github.idelstak.matrixrain.connector;

import com.github.idelstak.matrixrain.auxiliary.graphics.IndexBitmapObject;
import com.github.idelstak.matrixrain.auxiliary.math.intersect.Circle1PixelArbitraryIntersectorFactory;
import com.github.idelstak.matrixrain.intro.IntroManager;
import com.github.idelstak.matrixrain.phosphore.PhosphoreCloud;
import com.github.idelstak.matrixrain.phosphore.PhosphoreCloudFactory;
import com.github.idelstak.matrixrain.phosphore.Phosphorizer;

public class ConnectorObject {
  private int width, height;

  private int[][] pixels;

  // glow cloud
  private PhosphoreCloud glowCloud;

  private ConnectorObject() {
    this.width = 0;
    this.height = 0;
    this.pixels = null;
    this.glowCloud = null;
  }

  public ConnectorObject(int length, int height) {
    this.width = length;
    this.height = height;
    this.pixels = new int[length][height];
    for (int j = 0; j < height; j++) {
      int distFromCenter = (height - 1) / 2 - j;
      if (distFromCenter < 0) distFromCenter = -distFromCenter;
      double coef = 1.0 - 0.6 * (double) distFromCenter / (double) (height - 1);
      for (int i = 0; i < length; i++) this.pixels[i][j] = (int) (coef * 255.0);
    }

    // compute glow cloud
    this.glowCloud = new PhosphoreCloud(length, IntroManager.GLOW_RADIUS, 255);
  }

  public ConnectorObject(IndexBitmapObject indexBitmapObject) {
    this.width = indexBitmapObject.getWidth();
    this.height = indexBitmapObject.getHeight();
    this.pixels = indexBitmapObject.getBitmap();
    // compute glow cloud
    // this.glowCloud = new PhosphoreCloud(length, IntroManager.GLOW_RADIUS,
    // 255);
  }

  public Object clone() {
    ConnectorObject newObject = new ConnectorObject();
    newObject.width = this.width;
    newObject.height = this.height;
    newObject.pixels = new int[newObject.width][newObject.height];
    for (int i = 0; i < newObject.width; i++)
      for (int j = 0; j < newObject.height; j++) newObject.pixels[i][j] = this.pixels[i][j];
    if (this.glowCloud != null) newObject.glowCloud = (PhosphoreCloud) this.glowCloud.clone();
    return newObject;
  }

  public int getWidth() {
    return this.width;
  }

  public int getHeight() {
    return this.height;
  }

  public int[][] getPixels() {
    return pixels;
  }

  public int getPixel(int i, int j) {
    return pixels[i][j];
  }

  public PhosphoreCloud getGlowCloud() {
    return glowCloud;
  }

  public final ConnectorObject createScaledUpPhosphoreVersion(
      PhosphoreCloudFactory pcFct, Circle1PixelArbitraryIntersectorFactory iFct, double factor) {

    ConnectorObject newConnector = new ConnectorObject();
    IndexBitmapObject oldPixels = new IndexBitmapObject(this.pixels, this.width, this.height);
    IndexBitmapObject newPixels =
        Phosphorizer.createScaledUpPhosphoreVersion(oldPixels, pcFct, iFct, factor, false);

    newConnector.pixels = newPixels.getBitmap();
    newConnector.width = newPixels.getWidth();
    newConnector.height = newPixels.getHeight();

    newConnector.sharpen(50, 255);
    return newConnector;
  }

  public final ConnectorObject createScaledUpPixelVersion(
      Circle1PixelArbitraryIntersectorFactory iFct, double factor) {

    ConnectorObject newConnector = new ConnectorObject();
    IndexBitmapObject oldPixels = new IndexBitmapObject(this.pixels, this.width, this.height);
    IndexBitmapObject newPixels = Phosphorizer.createScaledUpPixelVersion(oldPixels, iFct, factor);

    newConnector.pixels = newPixels.getBitmap();
    newConnector.width = newPixels.getWidth();
    newConnector.height = newPixels.getHeight();

    // newConnector.sharpen(50, 255);
    return newConnector;
  }

  public void sharpen(int minColorVal, int maxColorVal) {
    // find minimum and maximum value and "stretch" the color map to new
    // range
    int maxVal = 0, minVal = 255;
    for (int i = 0; i < this.width; i++) {
      for (int j = 0; j < this.height; j++) {
        maxVal = Math.max(maxVal, pixels[i][j]);
        if (pixels[i][j] > 0) minVal = Math.min(minVal, pixels[i][j]);
      }
    }

    // minVal -> minColorVal
    // maxVal -> maxColorVal
    double coef = (double) (maxColorVal - minColorVal) / (double) (maxVal - minVal);
    for (int i = 0; i < this.width; i++) {
      for (int j = 0; j < this.height; j++) {
        if (pixels[i][j] == 0) continue;
        int newVal = (int) (minColorVal + coef * (pixels[i][j] - minVal));
        pixels[i][j] = newVal;
      }
    }
  }
}
