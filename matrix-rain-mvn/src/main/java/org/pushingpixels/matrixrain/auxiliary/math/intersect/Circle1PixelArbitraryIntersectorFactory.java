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
package org.pushingpixels.matrixrain.auxiliary.math.intersect;

public class Circle1PixelArbitraryIntersectorFactory {

  public static final int N = 20;

  private class IntersectorElement {
    public int radiusN;

    public double[] _SR;

    public double[][] _SRXY;

    public double[][] _unitIntersection;

    public double[][] _doubleIntersection;

    public int[][] _unitShift;

    public IntersectorElement next;

    public IntersectorElement(double radius) {
      this.radiusN = (int) (radius * N);
      this._SR = new double[2 * N + 1];
      for (int i = -N; i <= N; i++) {
        double x = (double) i / (double) N;
        this._SR[i + N] = Intersector.SR(x, radius);
      }

      this._SRXY = new double[2 * N + 1][2 * N + 1];
      for (int i = -N; i <= N; i++) {
        double x = (double) i / (double) N;
        for (int j = -N; j <= N; j++) {
          double y = (double) j / (double) N;
          this._SRXY[i + N][j + N] = Intersector.SRXY(x, y, radius);
        }
      }

      this._unitIntersection = new double[2 * N + 1][2 * N + 1];
      this._unitShift = new int[2 * N + 1][2 * N + 1];
      for (int i = -N; i <= N; i++) {
        double x = (double) i / (double) N;
        for (int j = -N; j <= N; j++) {
          double y = (double) j / (double) N;
          this._unitIntersection[i + N][j + N] =
              Intersector.intersectionArea(radius, x, y, x + 1, y + 1);
          double normUnitIntersection =
              255.0 * this._unitIntersection[i + N][j + N] / (Math.PI * radius * radius);
          this._unitShift[i + N][j + N] =
              8 - (int) (Math.log(normUnitIntersection + 1) / Math.log(2.0));
        }
      }

      this._doubleIntersection = new double[4 * N + 1][4 * N + 1];
      for (int i = -2 * N; i <= 2 * N; i++) {
        double x = (double) i / (double) 2 * N;
        for (int j = -2 * N; j <= 2 * N; j++) {
          double y = (double) j / (double) 2 * N;
          this._doubleIntersection[i + 2 * N][j + 2 * N] =
              Intersector.intersectionArea(2.0 * radius, x, y, x + 1, y + 1);
        }
      }

      this.next = null;
    }

    public double SR(int index) {
      if ((index < -N) || (index > N)) return 0.0;
      return _SR[index + N];
    }

    public double SRXY(int indexX, int indexY) {
      if ((indexX < -N) || (indexX > N)) return 0.0;
      if ((indexY < -N) || (indexY > N)) return 0.0;
      return _SRXY[indexX + N][indexY + N];
    }

    public double unitIntersection(int indexX, int indexY) {
      if ((indexX < -N) || (indexX > N)) return 0.0;
      if ((indexY < -N) || (indexY > N)) return 0.0;
      return _unitIntersection[indexX + N][indexY + N];
    }

    public double doubleIntersection(int indexX, int indexY) {
      if ((indexX < -2 * N) || (indexX > 2 * N)) return 0.0;
      if ((indexY < -2 * N) || (indexY > 2 * N)) return 0.0;
      return _doubleIntersection[indexX + 2 * N][indexY + 2 * N];
    }

    public int unitShift(int indexX, int indexY) {
      if ((indexX < -N) || (indexX > N)) return 8;
      if ((indexY < -N) || (indexY > N)) return 8;
      return _unitShift[indexX + N][indexY + N];
    }
  }

  private IntersectorElement intersectorElement;

  public static long delta = 0;

  public Circle1PixelArbitraryIntersectorFactory() {
    this.intersectorElement = new IntersectorElement(0.5);
  }

  public double unitIntersectionArea(int x1N, int y1N) {
    return this.intersectorElement.unitIntersection(x1N, y1N);
  }

  public double doubleIntersectionArea(int x1N, int y1N) {
    return this.intersectorElement.doubleIntersection(x1N, y1N);
  }

  public int unitShift(int x1N, int y1N) {
    return this.intersectorElement.unitShift(x1N, y1N);
  }
}
