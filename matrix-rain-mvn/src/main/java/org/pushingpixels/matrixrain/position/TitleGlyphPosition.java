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
