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
package org.pushingpixels.matrixrain.auxiliary.math.coord;

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

  public String toString() {
    return "(" + x + ", " + y + ")";
  }
}
