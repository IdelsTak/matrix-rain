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
