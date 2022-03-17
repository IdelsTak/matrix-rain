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
package com.github.idelstak.matrixrain.auxiliary.graphics.spline;

public final class SplineInterpolatorObject {
  private int minKey;

  private int maxKey;

  private double[] values;

  public SplineInterpolatorObject(int minKey, int maxKey, double[] values) {
    this.minKey = minKey;
    this.maxKey = maxKey;
    this.values = values;
  }

  public double getValue(int key) {
    if (key < minKey) return values[0];
    if (key > maxKey) return values[values.length - 1];
    int index = key - this.minKey;
    return values[index];
  }

  public int getMinKey() {
    return this.minKey;
  }

  public int getMaxKey() {
    return this.maxKey;
  }

  public void print() {
    for (int i = this.minKey; i <= this.maxKey; i++) {
      System.out.println(i + ": " + this.values[i - this.minKey]);
    }
  }
}
