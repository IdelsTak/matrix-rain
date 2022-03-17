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
package com.github.idelstak.matrixrain.phosphore;

// PhosphoreBitCloud - each integer value holds information
// on 2*2 matrix of pixels. 32 bits are enough to hold 4
// separate bytes of radiations

public final class PhosphoreBitCloud {

  private int[] radiationBitValues;

  private int refPointX, refPointY;
  private int width, height;

  private int size;

  public PhosphoreBitCloud() {
    this.radiationBitValues = null;
    this.width = 0;
    this.height = 0;
    this.refPointX = 0;
    this.refPointY = 0;
  }

  // constructor from PhosphoreCloud
  public PhosphoreBitCloud(PhosphoreCloud pCloud, boolean shiftX, boolean shiftY) {
    int origWidth = pCloud.getWidth();
    int origHeight = pCloud.getHeight();

    // System.out.println("NEW CLOUD - CREATE from max value " +
    // pCloud.getMaxValue());

    // go over all 2*2 squares and create value
    int paddedWidth = origWidth + (shiftX ? 1 : 0);
    int paddedHeight = origHeight + (shiftY ? 1 : 0);
    double[][] paddedValues = new double[paddedWidth][paddedHeight];
    for (int i = 0; i < paddedWidth; i++)
      for (int j = 0; j < paddedHeight; j++) paddedValues[i][j] = 0;

    double[][] origValues = pCloud.getRadiations();
    int startCol = (shiftX ? 1 : 0);
    int startRow = (shiftY ? 1 : 0);
    for (int i = 0; i < origWidth; i++)
      for (int j = 0; j < origHeight; j++)
        paddedValues[i + startCol][j + startRow] = origValues[i][j];

    this.refPointX = pCloud.getRefPointX() / 2;
    if (shiftX) this.refPointX++;
    this.refPointY = pCloud.getRefPointY() / 2;
    if (shiftY) this.refPointY++;

    this.width = (int) Math.ceil((double) paddedWidth / 2.0);
    this.height = (int) Math.ceil((double) paddedHeight / 2.0);

    this.size = this.width * this.height;

    this.radiationBitValues = new int[this.size];
    // initialize the radiation values
    for (int i = 0; i < this.size; i++) {
      this.radiationBitValues[i] = 0;
    }

    for (int i = 0; i < this.size; i++) {
      int row = i / this.width;
      int col = i % this.width;

      int byte1 = 0, byte2 = 0, byte3 = 0, byte4 = 0;
      /*
       * if (((2*col) < origWidth) && ((2*row) < origHeight)) byte1 =
       * (int)(32.0*paddedValues[2*col][2*row]); if (((2*col+1) <
       * origWidth) && ((2*row) < origHeight)) byte2 =
       * (int)(32.0*paddedValues[2*col+1][2*row]); if (((2*col) <
       * origWidth) && ((2*row+1) < origHeight)) byte3 =
       * (int)(32.0*paddedValues[2*col][2*row+1]); if (((2*col+1) <
       * origWidth) && ((2*row+1) < origHeight)) byte4 =
       * (int)(32.0*paddedValues[2*col+1][2*row+1]); if (byte2 > 255)
       * byte2 = 255; if (byte3 > 255) byte3 = 255; if (byte4 > 255) byte4 =
       * 255;
       */
      if (((2 * col) < origWidth) && ((2 * row) < origHeight))
        byte1 = (int) (paddedValues[2 * col][2 * row]);
      if (((2 * col + 1) < origWidth) && ((2 * row) < origHeight))
        byte2 = (int) (paddedValues[2 * col + 1][2 * row]);
      if (((2 * col) < origWidth) && ((2 * row + 1) < origHeight))
        byte3 = (int) (paddedValues[2 * col][2 * row + 1]);
      if (((2 * col + 1) < origWidth) && ((2 * row + 1) < origHeight))
        byte4 = (int) (paddedValues[2 * col + 1][2 * row + 1]);

      // System.out.print("cloud at (" + 2*col + ", " + 2*row + ") : ");
      // System.out.print(byte1 + ", " + byte2 + ", " + byte3 + ", " +
      // byte4);

      int newValue = (byte1 << 24) | (byte2 << 16) | (byte3 << 8) | byte4;
      // System.out.println(" = " + newValue);
      this.radiationBitValues[i] = newValue;
    }
  }

  public Object clone() {
    PhosphoreBitCloud newObject = new PhosphoreBitCloud();
    newObject.width = this.width;
    newObject.height = this.height;
    newObject.size = this.size;
    newObject.refPointX = this.refPointX;
    newObject.refPointY = this.refPointY;
    if (this.radiationBitValues != null) {
      newObject.radiationBitValues = new int[newObject.size];
      for (int i = 0; i < newObject.size; i++)
        newObject.radiationBitValues[i] = this.radiationBitValues[i];
    } else newObject.radiationBitValues = null;
    return newObject;
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

  public int getSize() {
    return this.size;
  }

  public int[] getRadiations() {
    return this.radiationBitValues;
  }
}
