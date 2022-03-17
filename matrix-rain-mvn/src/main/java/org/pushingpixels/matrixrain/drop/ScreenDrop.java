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
package org.pushingpixels.matrixrain.drop;

import org.pushingpixels.matrixrain.font.GlyphFactory;

public class ScreenDrop extends Drop {

  private int headX;

  private int headY;

  private int hopsToDrip;

  private int hopsToDry;

  private boolean toDry;

  public ScreenDrop(GlyphFactory glyphFactory) {
    super(glyphFactory);
    headX = 100;
    headY = 100;
    hopsToDrip = 1;
  }

  // set / get
  public void setHeadX(int value) {
    headX = value;
  }

  public int getHeadX() {
    return headX;
  }

  public void setHeadY(int value) {
    headY = value;
  }

  public int getHeadY() {
    return headY;
  }

  public void setHopsToDrip(int value) {
    hopsToDrip = value;
  }

  public int getHopsToDrip() {
    return hopsToDrip;
  }

  public void setToDry(boolean value) {
    toDry = value;
  }

  public boolean getToDry() {
    return toDry;
  }

  public void setHopsToDry(int value) {
    hopsToDry = value;
  }

  public int getHopsToDry() {
    return hopsToDry;
  }

  public void iteration(int hopsDelta, int speedupFactor) {
    hopsToDrip -= hopsDelta;

    if (this.getDrying() == false) {
      // drop is still very much alive
      // check if need to move it down
      if (hopsToDrip <= 0) {
        headY += speedupFactor * this.getFontSize();
        for (int i = 0; i < speedupFactor; i++) this.addGlyphAndShift();
        hopsToDrip = this.getHeadSpeed();
      }
      // check if need to dry it
      if (toDry) {
        hopsToDrip -= hopsDelta;
        if (hopsToDry <= 0) this.setDrying(true);
      }
    } else {
      if (hopsToDrip <= 0) {
        this.dryIteration(speedupFactor);
        hopsToDrip = this.getHeadSpeed();
      }
    }
  }
}
