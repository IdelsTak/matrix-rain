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
package com.github.idelstak.matrixrain.drop;

import com.github.idelstak.matrixrain.font.GlyphFactory;

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
