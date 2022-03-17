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
