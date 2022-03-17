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
package org.pushingpixels.matrixrain.auxiliary.graphics.geom.voronoi;

import org.pushingpixels.matrixrain.auxiliary.math.coord.Point2D;

public final class VoronoiIndexDiagramInfo {
  private int width;

  private int height;

  private int[][] diagramIndex;

  private Point2D[] centers;

  private int maxRadius;

  private int maxIndex;

  public VoronoiIndexDiagramInfo(
      int width, int height, int[][] diagramIndex, Point2D[] centers, int maxRadius, int maxIndex) {

    this.width = width;
    this.height = height;
    this.diagramIndex = diagramIndex;
    this.centers = centers;
    this.maxIndex = maxIndex;
    this.maxRadius = maxRadius;
  }

  public int getWidth() {
    return this.width;
  }

  public int getHeight() {
    return this.height;
  }

  public int[][] getDiagramIndex() {
    return this.diagramIndex;
  }

  public Point2D[] getCenters() {
    return this.centers;
  }

  public int getMaxIndex() {
    return this.maxIndex;
  }

  public int getMaxRadius() {
    return this.maxRadius;
  }
}
