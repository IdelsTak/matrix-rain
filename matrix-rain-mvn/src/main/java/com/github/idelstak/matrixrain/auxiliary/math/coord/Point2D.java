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
package com.github.idelstak.matrixrain.auxiliary.math.coord;

public final class Point2D {
  private static int currID = 0;

  public int id;

  private double x;

  private double y;

  public Point2D(int x, int y) {
    this.id = Point2D.currID++;
    this.x = x;
    this.y = y;
  }

  public Point2D(double x, double y) {
    this.id = Point2D.currID++;
    this.x = x;
    this.y = y;
  }

  public Point2D(Point2D p2) {
    this.id = p2.id;
    this.x = p2.x;
    this.y = p2.y;
  }

  public void setID(int newID) {
    this.id = newID;
  }

  public double getX() {
    return x;
  }

  public double getY() {
    return y;
  }

  public void set(double x, double y) {
    this.x = x;
    this.y = y;
  }

  public void set(Point2D p2) {
    this.id = p2.id;
    this.x = p2.x;
    this.y = p2.y;
  }

  public double getDistance(Point2D p) {
    double dx = this.x - p.x;
    double dy = this.y - p.y;
    if (dx == 0.0) return (dy > 0) ? dy : -dy;
    if (dy == 0.0) return (dx > 0) ? dx : -dx;
    return Math.sqrt(dx * dx + dy * dy);
  }

  public boolean isTheSame(Point2D p2, double manhattan) {
    if (this.id == p2.id) return true;
    return this.isTheSame(p2.x, p2.y, manhattan);
  }

  public boolean isTheSame(double x, double y, double manhattan) {
    return (this.hasSameX(x, manhattan) && this.hasSameY(y, manhattan));
  }

  public boolean hasSameX(double x, double manhattan) {
    double dx = this.x - x;
    double ddx = (dx > 0) ? dx : -dx;
    return (ddx < manhattan);
  }

  public boolean hasSameY(double y, double manhattan) {
    double dy = this.y - y;
    double ddy = (dy > 0) ? dy : -dy;
    return (ddy < manhattan);
  }

  public Point2D getPointByOffsets(double offsetX, double offsetY) {
    return new Point2D(this.x + offsetX, this.y + offsetY);
  }

  public void offset(double offsetX, double offsetY) {
    this.x += offsetX;
    this.y += offsetY;
  }

  @Override
  public String toString() {
    return "(" + x + ", " + y + ")";
  }
}
