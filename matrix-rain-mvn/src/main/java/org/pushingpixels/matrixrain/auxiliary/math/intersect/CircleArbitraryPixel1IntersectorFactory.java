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

public class CircleArbitraryPixel1IntersectorFactory {

  private class IntersectorElement {
    public int N;

    public double radius;

    public double[] _SR;

    public double[][] _SRXY;

    public double[][] _unitIntersection;

    public IntersectorElement(double radius) {
      this.radius = radius;
      this.N = (int) (Math.ceil(radius)) + 1;
      this._SR = new double[2 * N + 1];
      for (int x = -N; x <= N; x++) {
        this._SR[x + N] = Intersector.SR(x, radius);
      }

      this._SRXY = new double[2 * N + 1][2 * N + 1];
      for (int x = -N; x <= N; x++) {
        for (int y = -N; y <= N; y++) {
          this._SRXY[x + N][y + N] = Intersector.SRXY(x, y, radius);
        }
      }

      this._unitIntersection = new double[2 * N + 1][2 * N + 1];
      for (int x = -N; x <= N; x++) {
        for (int y = -N; y <= N; y++) {
          this._unitIntersection[x + N][y + N] =
              Intersector.intersectionArea(radius, x, y, x + 1, y + 1);
        }
      }
    }

    public double SR(int x) {
      if ((x < -N) || (x > N)) return 0.0;
      return _SR[x + N];
    }

    public double SRXY(int x, int y) {
      if ((x < -N) || (x > N)) return 0.0;
      if ((y < -N) || (y > N)) return 0.0;
      return _SRXY[x + N][y + N];
    }

    public double intersectionArea(int x, int y) {
      if ((x < -N) || (x > N)) return 0.0;
      if ((y < -N) || (y > N)) return 0.0;
      return _unitIntersection[x + N][y + N];
    }
  }

  private IntersectorElement intersectorElement;

  public CircleArbitraryPixel1IntersectorFactory(double radius) {
    this.intersectorElement = new IntersectorElement(radius);
  }

  public double intersectionArea(int x, int y) {
    return this.intersectorElement.intersectionArea(x, y);
  }
}
