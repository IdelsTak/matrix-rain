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
package com.github.idelstak.matrixrain.font;

public class RadianceFactory {

  private RadianceMap[][] radiances;

  private class RadianceMap {
    public int cValue;

    public int radiance;

    public int size;

    public int[][] rMap;

    public RadianceMap(int cValue, int radiance) {
      this.cValue = cValue;
      this.radiance = radiance;
      this.size = radiance + 1;

      this.rMap = new int[size][size];
      for (int i = 0; i < size; i++) for (int j = 0; j < size; j++) rMap[i][j] = 0;

      rMap[0][0] = cValue;

      if (radiance == 0) {
        // no radiance - take only this pixel
        return;
      }

      if (cValue == 0) {
        // no color
        return;
      }

      double delta = (double) (255 - cValue) / 255.0;
      double delta_rf = delta;
      for (int i = 1; i < radiance; i++) delta_rf *= delta;

      for (int dx = 0; dx <= radiance; dx++) {
        for (int dy = 0; dy <= radiance; dy++) {
          double distance = Math.sqrt(dx * dx + dy * dy);
          if (distance > radiance) {
            rMap[dx][dy] = 0;
          } else {
            double factor1 = 1.0 - (distance / (radiance + 0.1));
            double factor2 = 255.0 * delta * (1.0 - delta_rf);
            rMap[dx][dy] = (int) (factor1 * factor2);
          }
        }
      }
    }

    public int getFactor(int dx, int dy) {
      if (dx < 0) dx = -dx;
      if (dy < 0) dy = -dy;
      return rMap[dx][dy];
    }
  }

  public RadianceFactory() {
    radiances = new RadianceMap[256][2];
    for (int cValue = 0; cValue < 256; cValue++) {
      for (int radiance = 1; radiance <= 2; radiance++) {
        radiances[cValue][radiance - 1] = new RadianceMap(cValue, radiance);
      }
    }
  }

  public int getFactor(int cValue, int radiance, int dx, int dy) {
    return radiances[cValue][radiance - 1].getFactor(dx, dy);
  }
}
