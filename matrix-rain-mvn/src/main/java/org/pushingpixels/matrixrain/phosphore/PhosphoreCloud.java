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
package org.pushingpixels.matrixrain.phosphore;

import org.pushingpixels.matrixrain.font.MemoryGlyph;

public final class PhosphoreCloud {

  private double[][] radiationBitmap;

  // private double[] radiationBitmap1d;
  private int refPointX, refPointY;
  private int width, height;

  private double maxValue;

  public PhosphoreCloud() {
    this.radiationBitmap = null;
    // this.radiationBitmap1d = null;
    this.width = 0;
    this.height = 0;
    this.refPointX = 0;
    this.refPointY = 0;
    this.maxValue = 0;
  }

  // constructor from Glyph
  public PhosphoreCloud(MemoryGlyph memoryGlyph, int cloudRadius) {
    this.refPointX = cloudRadius;
    this.refPointY = cloudRadius;

    int glyphSize = memoryGlyph.getSize();

    this.width = 2 * cloudRadius + glyphSize;
    this.height = 2 * cloudRadius + glyphSize;

    this.radiationBitmap = new double[this.width][this.height];
    // initialize the radiation bitmap
    for (int i = 0; i < this.width; i++) {
      for (int j = 0; j < this.height; j++) {
        this.radiationBitmap[i][j] = 0;
      }
    }

    // gloify each pixel of the memoryGlyph
    int cr2 = cloudRadius * cloudRadius;
    for (int x = 0; x < glyphSize; x++) {
      for (int y = 0; y < glyphSize; y++) {
        // check if this pixel has color
        double val = 0.0;
        if (memoryGlyph.getPixel(x, y) == 0) continue;
        val = (double) memoryGlyph.getPixel(x, y) / 255.0;

        for (int dx = -cloudRadius; dx < cloudRadius; dx++) {
          for (int dy = -cloudRadius; dy < cloudRadius; dy++) {
            // int adx = (dx>0)?dx:-dx;
            // int ady = (dy>0)?dy:-dy;
            int rx = x + dx + this.refPointX;
            if ((rx < 0) || (rx >= this.width)) continue;
            int ry = y + dy + this.refPointY;
            if ((ry < 0) || (ry >= this.height)) continue;

            if ((dx * dx + dy * dy) <= cr2) {
              double coef = 1.0 - (double) (dx * dx + dy * dy) / (double) cr2;
              if (this.radiationBitmap[rx][ry] < (val * coef))
                this.radiationBitmap[rx][ry] = val * coef;
            }
          }
        }
      }
    }

    for (int i = 0; i < this.width; i++)
      for (int j = 0; j < this.height; j++)
        this.maxValue = Math.max(this.maxValue, radiationBitmap[i][j]);
  }

  // constructor from connector (horizontal line)
  public PhosphoreCloud(int length, int cloudRadius, int connectorGlow) {
    this.refPointX = cloudRadius;
    this.refPointY = cloudRadius;

    this.width = 2 * cloudRadius + length;
    this.height = 2 * cloudRadius + 1;

    this.radiationBitmap = new double[this.width][this.height];
    // initialize the radiation bitmap
    for (int i = 0; i < this.width; i++) {
      for (int j = 0; j < this.height; j++) {
        this.radiationBitmap[i][j] = 0;
      }
    }

    // gloify each pixel of the connector
    int cr2 = cloudRadius * cloudRadius;
    for (int x = 0; x < length; x++) {
      // y = 0;
      // check if this pixel has color
      for (int dx = -cloudRadius; dx < cloudRadius; dx++) {
        for (int dy = -cloudRadius; dy < cloudRadius; dy++) {
          int adx = (dx > 0) ? dx : -dx;
          int ady = (dy > 0) ? dy : -dy;
          int rx = x + dx + this.refPointX;
          int ry = dy + this.refPointY;

          if ((adx * adx + ady * ady) < cr2) {
            double coef = 1.0 - (double) (adx * adx + ady * ady) / (double) cr2;
            if (coef > this.radiationBitmap[rx][ry]) this.radiationBitmap[rx][ry] = coef;
          }
        }
      }
    }

    for (int i = 0; i < this.width; i++)
      for (int j = 0; j < this.height; j++)
        this.maxValue = Math.max(this.maxValue, radiationBitmap[i][j]);
  }

  public static double getRandom(double minValue, double maxValue) {
    // 0.0 -> min
    // 1.0 -> max
    double rand = Math.random();
    return minValue + (maxValue - minValue) * rand;
  }

  // constructor for random cloud with given half-height and thickness
  public PhosphoreCloud(int halfHeight, int cloudRadius) {
    this.refPointX = 2 * cloudRadius;
    this.refPointY = halfHeight;

    this.width = 4 * cloudRadius + 1;
    this.height = 2 * halfHeight + 2 * cloudRadius + 1;

    this.radiationBitmap = new double[this.width][this.height];
    // this.radiationBitmap1d = new double[this.width*this.height];
    // initialize the radiation bitmap
    for (int i = 0; i < this.width; i++) {
      for (int j = 0; j < this.height; j++) {
        this.radiationBitmap[i][j] = 0;
        // this.radiationBitmap1d[j*this.width+i] = 0;
      }
    }

    // choose cloud Y limits
    double startY = 1.0 * cloudRadius * Math.random();
    // double endY = 2.0*halfHeight-cloudRadius*getRandom(0.0,
    // 2.0*cloudRadius-startY);
    double endY = 2.0 * halfHeight - 1.0 * cloudRadius * Math.random();
    if (startY > endY) {
      startY = halfHeight;
      endY = halfHeight;
    }

    // upper half
    double currY = startY;
    while (currY <= halfHeight) {
      // choose random X
      double maxDeltaX = (startY - currY) * cloudRadius / (startY - halfHeight);
      int currDeltaX = 2 * cloudRadius + (int) getRandom(-maxDeltaX, maxDeltaX);
      // create a 'ball'
      double minRadius =
          (double) cloudRadius / 2.0
              + (startY - currY) * (double) cloudRadius / (2.0 * (startY - halfHeight));
      int currRadius = (int) Math.ceil(getRandom(minRadius, (double) cloudRadius));
      if (currRadius == 0) currRadius = 1;
      int cr2 = currRadius * currRadius;
      for (int dx = -currRadius; dx < currRadius; dx++) {
        for (int dy = -currRadius; dy < currRadius; dy++) {
          int adx = (dx > 0) ? dx : -dx;
          int ady = (dy > 0) ? dy : -dy;
          int rx = dx + currDeltaX;
          int ry = dy + (int) currY + cloudRadius;

          if ((adx * adx + ady * ady) < cr2) {
            this.radiationBitmap[rx][ry] += 255 * (1.0 - (adx * adx + ady * ady) / cr2);
            // this.radiationBitmap1d[ry*this.width+ry] +=
            // 255*(1.0-(adx*adx+ady*ady)/cr2);
          }
        }
      }
      currY += getRandom(0.0, 0.4);
    }
    // lower half
    currY = halfHeight;
    while (currY <= endY) {
      // choose random X
      double maxDeltaX = (endY - currY) * cloudRadius / (endY - halfHeight);
      int currDeltaX = 2 * cloudRadius + (int) getRandom(-maxDeltaX, maxDeltaX);
      double minRadius =
          (double) cloudRadius / 2.0
              + (endY - currY) * (double) cloudRadius / (2.0 * (endY - halfHeight));
      int currRadius = (int) Math.ceil(getRandom(minRadius, (double) cloudRadius));
      if (currRadius == 0) currRadius = 1;
      int cr2 = currRadius * currRadius;
      for (int dx = -currRadius; dx < currRadius; dx++) {
        for (int dy = -currRadius; dy < currRadius; dy++) {
          int adx = (dx > 0) ? dx : -dx;
          int ady = (dy > 0) ? dy : -dy;
          int rx = dx + currDeltaX;
          int ry = dy + (int) currY + cloudRadius;

          if ((adx * adx + ady * ady) < cr2) {
            this.radiationBitmap[rx][ry] += 255 * (1.0 - (adx * adx + ady * ady) / cr2);
            // this.radiationBitmap1d[ry*this.width+ry] +=
            // 255*(1.0-(adx*adx+ady*ady)/cr2);
          }
        }
      }
      currY += getRandom(0.0, 0.4);
    }

    for (int i = 0; i < this.width; i++)
      for (int j = 0; j < this.height; j++)
        this.maxValue = Math.max(this.maxValue, radiationBitmap[i][j]);
  }

  // constructor for random cloud with given half-height and thickness
  public PhosphoreCloud(double halfHeight, double cloudRadius) {
    this.width = (int) Math.ceil(4.0 * cloudRadius + 1.0);
    this.height = (int) Math.ceil(2.0 * halfHeight + 2.0 * cloudRadius + 1.0);

    this.refPointX = this.width / 2;
    this.refPointY = this.height / 2;

    this.radiationBitmap = new double[this.width][this.height];
    // this.radiationBitmap1d = new double[this.width*this.height];
    // initialize the radiation bitmap
    for (int i = 0; i < this.width; i++) {
      for (int j = 0; j < this.height; j++) {
        this.radiationBitmap[i][j] = 0;
        // this.radiationBitmap1d[j*this.width+i] = 0;
      }
    }

    // choose cloud Y limits
    double startY = 1.0 * cloudRadius * Math.random();
    // double endY = 2.0*halfHeight-cloudRadius*getRandom(0.0,
    // 2.0*cloudRadius-startY);
    double endY = 2.0 * halfHeight - 1.0 * cloudRadius * Math.random();
    if (startY > endY) {
      startY = halfHeight;
      endY = halfHeight;
    }

    // upper half
    double currY = startY;
    while (currY <= halfHeight) {
      // choose random X
      double maxDeltaX = (startY - currY) * cloudRadius / (startY - halfHeight);
      int currDeltaX = (int) (2 * cloudRadius) + (int) getRandom(-maxDeltaX, maxDeltaX);
      // create a 'ball'
      double minRadius =
          cloudRadius / 2.0 + (startY - currY) * cloudRadius / (2.0 * (startY - halfHeight));
      int currRadius = (int) Math.ceil(getRandom(minRadius, cloudRadius));
      if (currRadius == 0) currRadius = 1;
      int cr2 = currRadius * currRadius;
      for (int dx = -currRadius; dx < currRadius; dx++) {
        for (int dy = -currRadius; dy < currRadius; dy++) {
          int adx = (dx > 0) ? dx : -dx;
          int ady = (dy > 0) ? dy : -dy;
          int rx = dx + currDeltaX;
          int ry = dy + (int) currY + (int) cloudRadius;

          if ((adx * adx + ady * ady) < cr2) {
            this.radiationBitmap[rx][ry] += (1.0 - (adx * adx + ady * ady) / cr2);
            // this.radiationBitmap1d[ry*this.width+ry] +=
            // 255*(1.0-(adx*adx+ady*ady)/cr2);
          }
        }
      }
      currY += getRandom(0.0, 0.2);
    }
    // lower half
    currY = halfHeight;
    while (currY <= endY) {
      // choose random X
      double maxDeltaX = (endY - currY) * cloudRadius / (endY - halfHeight);
      int currDeltaX = (int) (2 * cloudRadius) + (int) getRandom(-maxDeltaX, maxDeltaX);
      double minRadius =
          cloudRadius / 2.0 + (endY - currY) * cloudRadius / (2.0 * (endY - halfHeight));
      int currRadius = (int) Math.ceil(getRandom(minRadius, cloudRadius));
      if (currRadius == 0) currRadius = 1;
      int cr2 = currRadius * currRadius;
      for (int dx = -currRadius; dx < currRadius; dx++) {
        for (int dy = -currRadius; dy < currRadius; dy++) {
          int adx = (dx > 0) ? dx : -dx;
          int ady = (dy > 0) ? dy : -dy;
          int rx = dx + currDeltaX;
          int ry = dy + (int) currY + (int) cloudRadius;

          if ((adx * adx + ady * ady) < cr2) {
            this.radiationBitmap[rx][ry] += (1.0 - (adx * adx + ady * ady) / cr2);
            // this.radiationBitmap1d[ry*this.width+ry] +=
            // 255*(1.0-(adx*adx+ady*ady)/cr2);
          }
        }
      }
      currY += getRandom(0.0, 0.2);
    }

    for (int i = 0; i < this.width; i++)
      for (int j = 0; j < this.height; j++)
        this.maxValue = Math.max(this.maxValue, radiationBitmap[i][j]);
  }

  public Object clone() {
    PhosphoreCloud newObject = new PhosphoreCloud();
    newObject.width = this.width;
    newObject.height = this.height;
    newObject.refPointX = this.refPointX;
    newObject.refPointY = this.refPointY;
    if (this.radiationBitmap != null) {
      newObject.radiationBitmap = new double[newObject.width][newObject.height];
      for (int i = 0; i < newObject.width; i++)
        for (int j = 0; j < newObject.height; j++)
          newObject.radiationBitmap[i][j] = this.radiationBitmap[i][j];
    } else newObject.radiationBitmap = null;
    newObject.maxValue = this.maxValue;
    return newObject;
  }

  // constructor for cloud around a point
  public PhosphoreCloud(int a, int b, int maxValue, int minValue, int cloudRadius) {

    this.refPointX = cloudRadius + a / 2;
    this.refPointY = cloudRadius + b / 2;

    this.width = 2 * cloudRadius + 2 * a + 1;
    this.height = 2 * cloudRadius + 2 * b + 1;

    int ar = a + cloudRadius;
    int br = b + cloudRadius;

    this.radiationBitmap = new double[this.width][this.height];
    // initialize the radiation bitmap
    for (int i = 0; i < this.width; i++) {
      for (int j = 0; j < this.height; j++) {
        this.radiationBitmap[i][j] = 0;
      }
    }

    for (int i = 0; i < ar; i++) {
      for (int j = 0; j < br; j++) {
        double maxDist = 0.0;
        if (i == 0) maxDist = br;
        else {
          double alpha = Math.atan2(j, i);
          double cosa = Math.cos(alpha);
          double sina = Math.sin(alpha);
          maxDist = Math.sqrt(ar * ar * cosa * cosa + br * br * sina * sina);
        }
        double currDist = Math.sqrt(i * i + j * j);
        if (currDist > maxDist) continue;

        // 0 --> maxValue
        // 1 --> minValue
        double ratio = currDist / maxDist;

        double value = maxValue + ratio * (minValue - maxValue);

        this.radiationBitmap[ar - i][br - j] = value;
        this.radiationBitmap[ar - i][br + j] = value;
        this.radiationBitmap[ar + i][br - j] = value;
        this.radiationBitmap[ar + i][br + j] = value;
      }
    }
  }

  public int getRefPointX() {
    return this.refPointX;
  }

  public int getRefPointY() {
    return this.refPointY;
  }

  public int getWidth() {
    return this.width;
  }

  public int getHeight() {
    return this.height;
  }

  public int getRadiation(int x, int y) {
    return (int) this.radiationBitmap[x][y];
  }

  public double getDoubleRadiation(int x, int y) {
    return this.radiationBitmap[x][y];
  }

  public double[][] getRadiations() {
    return this.radiationBitmap;
  }

  public int getMaxValue() {
    return (int) this.maxValue;
  }

  public void normalize(double cutoff) {
    // all radiations to 0.0-1.0 range
    if (this.getMaxValue() == 0) return;

    // 0..maxValue*cutoff --> 0..255
    // maxValue*cutoff..maxValue --> 255

    double threshold = cutoff * this.getMaxValue();
    double coef = 40.0 / (this.maxValue * cutoff);
    for (int i = 0; i < this.width; i++) {
      for (int j = 0; j < this.height; j++) {
        if (this.radiationBitmap[i][j] <= threshold) this.radiationBitmap[i][j] *= coef;
        else this.radiationBitmap[i][j] = 40.0;
      }
    }
    this.maxValue = 40.0;

    /*
     * double maxLog = Math.log(this.getMaxValue()); double minVal = 128.0;
     * for (int i=0; i<this.width; i++) { for (int j=0; j<this.height;
     * j++) { if (this.radiationBitmap[i][j] > 0) { double newValue =
     * 128.0*Math.log(this.radiationBitmap[i][j])/maxLog; if (newValue <
     * minVal) minVal = newValue; this.radiationBitmap[i][j] = newValue; } } }
     *  // now stretch : min..max --> 0..128 double coef =
     * 128.0/(128.0-minVal); for (int i=0; i<this.width; i++) { for (int
     * j=0; j<this.height; j++) { if (this.radiationBitmap[i][j] > 0) {
     * this.radiationBitmap[i][j] =
     * coef*(this.radiationBitmap[i][j]-minVal); } } }
     */
    // 0 --> 0
    // max/2 --> 90
    // max --> 128
    /*
     * double a = -104.0/(this.maxValue*this.maxValue); double b =
     * 232.0/this.maxValue; for (int i=0; i<this.width; i++) { for (int
     * j=0; j<this.height; j++) { double val = this.radiationBitmap[i][j];
     * if (val > 0) { double newVal = (a*val+b)*val;
     * this.radiationBitmap[i][j] = newVal; } } }
     *
     * this.maxValue = 128.0;
     */
  }
}
