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
package org.pushingpixels.matrixrain.font;

public class GlyphSegment {

  public int startX, endX;
  public int startY, endY;
  public int width, height;

  public boolean isVertical;

  public GlyphSegment next;

  public GlyphSegment() {
    this.next = null;
  }

  public int middleX() {
    return startX + (int) (Math.ceil(width / 2.0));
  }

  public int middleY() {
    return startY + (int) (Math.ceil(height / 2.0));
  }

  public int count() {
    if (this.next == null) return 1;
    return 1 + this.next.count();
  }

  public GlyphSegment getNth(int n) {
    if (n == 0) return this;
    if (this.next == null) return this;
    return this.next.getNth(n - 1);
  }
}
