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
package org.pushingpixels.matrixrain.auxiliary.graphics;

public class IndexBitmapObject {
  private int[][] bitmap;

  private int width, height;

  public IndexBitmapObject() {
    this.width = 0;
    this.height = 0;
    this.bitmap = null;
  }

  public IndexBitmapObject(int[][] bitmap, int width, int height) {
    this.width = width;
    this.height = height;
    this.bitmap = bitmap;
  }

  @Override
  public Object clone() throws CloneNotSupportedException {
    IndexBitmapObject newObject = new IndexBitmapObject();
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
}
