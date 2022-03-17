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
package org.pushingpixels.matrixrain.position;

import java.awt.Point;

public class TitleConnectorPosition {

  public static final int STATE_WAITTOSHOW = 0;

  public static final int STATE_APPEARING = 1;

  public static final int STATE_SHOWING = 2;

  private Point currPosition;

  private int state;

  private int x;

  private int y;

  private int startGlowIndex;

  private int endGlowIndex;

  private int glowDelta;

  private int timeToStartAppearing;

  private int currGlowIndex;

  public TitleConnectorPosition() {
    this.currPosition = new Point(0, 0);
    this.state = TitleConnectorPosition.STATE_WAITTOSHOW;
    this.startGlowIndex = 0;
    this.endGlowIndex = 255;
    this.glowDelta = 10;
    this.currGlowIndex = 0;
    this.timeToStartAppearing = 0;
  }

  public Object clone() {
    TitleConnectorPosition tcp = new TitleConnectorPosition();
    tcp.currPosition = new Point(this.currPosition);
    tcp.state = this.state;
    tcp.x = this.x;
    tcp.y = this.y;
    tcp.startGlowIndex = this.startGlowIndex;
    tcp.endGlowIndex = this.endGlowIndex;
    tcp.glowDelta = this.glowDelta;
    tcp.timeToStartAppearing = this.timeToStartAppearing;
    return tcp;
  }

  public void setX(int value) {
    x = value;
    this.currPosition.x = value;
  }

  public void setY(int value) {
    y = value;
    this.currPosition.y = value;
  }

  public void setTimeToStartAppearing(int value) {
    timeToStartAppearing = value;
  }

  public void setStartGlowIndex(int value) {
    startGlowIndex = value;
  }

  public void setEndGlowIndex(int value) {
    endGlowIndex = value;
  }

  public void setGlowDelta(int value) {
    glowDelta = value;
  }

  public Point getPosition() {
    return this.currPosition;
  }

  public int getCurrentGlowIndex() {
    return this.currGlowIndex;
  }

  public int getStabilizationTime() {
    return this.timeToStartAppearing
        + 1
        + (this.endGlowIndex - this.startGlowIndex) / this.glowDelta;
  }

  public synchronized void iteration(long delta) {
    switch (this.state) {
      case TitleConnectorPosition.STATE_WAITTOSHOW:
        this.timeToStartAppearing -= delta;
        if (this.timeToStartAppearing <= 0) {
          this.state = TitleConnectorPosition.STATE_APPEARING;
          this.currGlowIndex = this.startGlowIndex;
        }
        break;
      case TitleConnectorPosition.STATE_APPEARING:
        int newGlowIndex = this.currGlowIndex + this.glowDelta;
        if (newGlowIndex >= this.endGlowIndex) {
          newGlowIndex = this.endGlowIndex;
          this.state = TitleConnectorPosition.STATE_SHOWING;
        }
        this.currGlowIndex = newGlowIndex;
        break;
      case TitleConnectorPosition.STATE_SHOWING:
        break;
    }
  }

  public synchronized boolean toShow() {
    return (this.state != TitleConnectorPosition.STATE_WAITTOSHOW);
  }

  public synchronized int getState() {
    return this.state;
  }
}
