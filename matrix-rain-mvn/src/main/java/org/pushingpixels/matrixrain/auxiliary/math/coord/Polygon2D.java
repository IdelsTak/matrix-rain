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
package org.pushingpixels.matrixrain.auxiliary.math.coord;

public final class Polygon2D {
  private static int currID = 0;
  public int id;
  private final Point2D[] points;

  public Polygon2D(Point2D[] points) {
    this.id = Polygon2D.currID++;
    this.points = points;
  }

  public Polygon2D(Point2DList pointList) {
    this.id = Polygon2D.currID++;
    this.points = new Point2D[pointList.length];
    int curr = 0;
    Point2DList.Point2DElement currElem = pointList.head;
    while (currElem != null) {
      this.points[curr++] = currElem.point;
      currElem = currElem.next;
    }
  }

  public Point2D[] getPoints() {
    return this.points;
  }

  public int getPointsCount() {
    if (this.points == null) return 0;
    return this.points.length;
  }

  public void dump() {
    if (this.points == null) return;
    for (Point2D point : this.points) {
      System.out.print(point.toString() + ", ");
    }
    System.out.println();
  }
}
