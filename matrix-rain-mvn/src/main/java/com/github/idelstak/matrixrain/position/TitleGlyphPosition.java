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
package com.github.idelstak.matrixrain.position;

import java.awt.Point;

public class TitleGlyphPosition {

  public static final int STATE_WAITTODROP = 0;

  public static final int STATE_DROPPING = 1;

  public static final int STATE_FINISHEDDROPPING = 2;

  private Point currPosition;

  private Point finalPosition;

  private int state;

  private int x;

  private int startY;

  private int endY;

  private int deltaY;

  private int timeToStartDropping;

  public TitleGlyphPosition() {
    this.currPosition = new Point(0, 0);
    this.finalPosition = new Point(0, 0);
    this.state = TitleGlyphPosition.STATE_WAITTODROP;
  }

  public Object clone() {
    TitleGlyphPosition tgp = new TitleGlyphPosition();
    tgp.currPosition = new Point(this.currPosition);
    tgp.finalPosition = new Point(this.finalPosition);
    tgp.state = this.state;
    tgp.x = this.x;
    tgp.startY = this.startY;
    tgp.endY = this.endY;
    tgp.deltaY = this.deltaY;
    tgp.timeToStartDropping = this.timeToStartDropping;
    return tgp;
  }

  public void setX(int value) {
    x = value;
    this.currPosition.x = value;
    this.finalPosition.x = value;
  }

  public int getX() {
    return x;
  }

  public void setStartY(int value) {
    startY = value;
    this.currPosition.y = value;
  }

  public void setEndY(int value) {
    endY = value;
    this.finalPosition.y = value;
  }

  public int getEndY() {
    return endY;
  }

  public void setDeltaY(int value) {
    deltaY = value;
  }

  public int getDeltaY() {
    return deltaY;
  }

  public void setTimeToStartDropping(int value) {
    timeToStartDropping = value;
  }

  public int getTimeToStartDropping() {
    return timeToStartDropping;
  }

  public Point getPosition() {
    return this.currPosition;
  }

  public Point getFinalPosition() {
    return this.finalPosition;
  }

  public synchronized void iteration(long delta) {
    switch (this.state) {
      case TitleGlyphPosition.STATE_WAITTODROP:
        this.timeToStartDropping -= delta;
        if (this.timeToStartDropping <= 0) {
          this.state = TitleGlyphPosition.STATE_DROPPING;
          currPosition.y = startY;
        }
        break;
      case TitleGlyphPosition.STATE_DROPPING:
        int newY = currPosition.y + deltaY;
        if (newY >= endY) {
          newY = endY;
          this.state = TitleGlyphPosition.STATE_FINISHEDDROPPING;
        }
        currPosition.y = newY;
        break;
      case TitleGlyphPosition.STATE_FINISHEDDROPPING:
        break;
    }
  }

  public synchronized boolean toShow() {
    return (this.state != TitleGlyphPosition.STATE_WAITTODROP);
  }

  public synchronized int getState() {
    return this.state;
  }
}
