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
