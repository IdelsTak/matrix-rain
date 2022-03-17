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
package org.pushingpixels.matrixrain.drop;

import org.pushingpixels.matrixrain.auxiliary.graphics.IndexImageManager;
import org.pushingpixels.matrixrain.font.GlyphFactory;
import org.pushingpixels.matrixrain.font.MemoryGlyph;

public class DropManager {

  private class DropListElement {
    public ScreenDrop drop;

    public DropListElement next;

    public DropListElement prev;

    public DropListElement(ScreenDrop drop) {
      this.drop = drop;
      this.next = null;
      this.prev = null;
    }
  }

  private class DropList {
    public DropListElement head;

    public int length;

    public DropList() {
      head = null;
      length = 0;
    }

    public synchronized void addDrop(ScreenDrop drop) {
      DropListElement newElement = new DropListElement(drop);
      newElement.next = this.head;
      if (this.head != null) this.head.prev = newElement;
      this.head = newElement;
      length++;
    }

    public synchronized void removeDrop(DropListElement element) {
      if (element == this.head) {
        this.head = element.next;
        if (this.head != null) this.head.prev = null;
      } else {
        if (element.next != null) element.next.prev = element.prev;
        if (element.prev != null) element.prev.next = element.next;
      }
      length--;
    }
  }

  private DropList dropList;

  private GlyphFactory glyphFactory;

  private int windowWidth, windowHeight;
  private long startTime, endTime;

  private int dropDelta;

  private boolean isGenerating;

  private int globalSpeedupFactor;

  public DropManager(GlyphFactory glyphFactory, int windowWidth, int windowHeight) {
    this.glyphFactory = glyphFactory;
    this.dropList = new DropList();
    this.windowWidth = windowWidth;
    this.windowHeight = windowHeight;
    this.dropDelta = 0;
    this.isGenerating = true;
    this.globalSpeedupFactor = 1;
  }

  private int getRandomBlur() {
    int blurCount = this.glyphFactory.getBlurCount();
    int ran = getRandom(blurCount * 2);
    if (ran < blurCount) return 0;
    else return ran - blurCount + 1;
  }

  private int getRandomFontSize() {
    return 1 + getRandom(this.glyphFactory.getSizeCount());
  }

  private int getRandom(int maxValue) {
    double val = Math.random();
    return (int) (Math.floor(val * maxValue));
  }

  private int getRandomHeadLength() {
    return 1 + getRandom(2);
  }

  private int getRandomHeadRadiance() {
    return 1 + getRandom(this.glyphFactory.getRadianceCount() - 1);
  }

  private int getRandomHeadSpeed() {
    int ran = getRandom(7);
    if (ran < 4) return 1;
    else return 1 + 1 * (ran - 3);
  }

  private int getRandomHeadX() {
    return getRandom(this.windowWidth);
  }

  private int getRandomHeadY(int maxY) {
    return getRandom(maxY);
  }

  private int getRandomTailLength() {
    return 10 + getRandom(10);
  }

  private void addRandomDrop(int maxY) {
    ScreenDrop drop = new ScreenDrop(glyphFactory);
    drop.setBlurFactor(this.getRandomBlur());
    drop.setFontSize(this.getRandomFontSize());
    drop.setHeadLength(this.getRandomHeadLength());
    drop.setHeadRadiance(this.getRandomHeadRadiance());
    drop.setHeadSpeed(this.getRandomHeadSpeed());
    drop.setHeadX(this.getRandomHeadX());
    drop.setHeadY(this.getRandomHeadY(maxY));
    drop.setTailLength(this.getRandomTailLength());
    drop.setHopsToDrip(drop.getHeadSpeed());
    drop.setToChangeLetters(false);

    if (this.getRandom(5) == 0) {
      drop.setToDry(true);
      drop.setHopsToDry((5 + this.getRandom(5)) * drop.getHeadSpeed());
    } else drop.setToDry(false);

    drop.populateGlyphs();
    this.dropList.addDrop(drop);
  }

  public synchronized void createDrops() {
    /*
     * // disappearance 1,4,3,5,2 this.createTestDrop(100, 200);
     * this.createTestDrop(150, 140); this.createTestDrop(200, 160);
     * this.createTestDrop(250, 120); this.createTestDrop(300, 180);
     */
    this.isGenerating = true;
    this.globalSpeedupFactor = 1;
    for (int i = 0; i < 50; i++) {
      this.addRandomDrop(this.windowHeight);
    }
  }

  private void paintGlyph(
      IndexImageManager indexImageManager, MemoryGlyph glyph, int x, int y, double fadeOut) {
    int gSize = glyph.getSize();
    for (int i = 0; i < gSize; i++) {
      for (int j = 0; j < gSize; j++) {
        int value = (int) (fadeOut * glyph.getPixel(i, j));
        if (value > 0) indexImageManager.paintPixel(x + i, y + j, value);
      }
    }
  }

  public synchronized void fillColorIndexMap(IndexImageManager indexImageManager) {
    DropListElement currElem = this.dropList.head;
    while (currElem != null) {
      ScreenDrop drop = currElem.drop;
      MemoryGlyph[] glyphs = drop.getGlyphs();

      int headLength = drop.getHeadLength();
      int totalLength = drop.getLength();

      int x = drop.getHeadX();
      for (int headIndex = 0; headIndex < headLength; headIndex++) {
        // y coordinate of symbol (bottom-up)
        int y = drop.getHeadY() - headIndex * drop.getFontSize();
        double fadeOut = 1.0;

        paintGlyph(indexImageManager, glyphs[headIndex], x, y, fadeOut);
      }

      for (int tailIndex = headLength; tailIndex < totalLength; tailIndex++) {
        // y coordinate of symbol (bottom-up)
        int y = drop.getHeadY() - tailIndex * drop.getFontSize();
        // fade-out for tail
        // first tail letter - 0.85
        // last tail letter - 0.4
        double fade1 = 0.85 * drop.getFadeoutFactor();
        double fade2 = 0.4 * drop.getFadeoutFactor();
        double fadeOut =
            fade1
                - (fade1 - fade2)
                    * (double) (tailIndex - headLength)
                    / (double) (totalLength - 1 - headLength);

        paintGlyph(indexImageManager, glyphs[tailIndex], x, y, fadeOut);
      }

      currElem = currElem.next;
    }
  }

  public synchronized void iteration(int hopDelta) {
    startTime = System.currentTimeMillis();
    DropListElement currElem = this.dropList.head;
    int removedCount = 0;
    while (currElem != null) {
      ScreenDrop drop = currElem.drop;
      drop.iteration(hopDelta, this.globalSpeedupFactor);

      // check that is visible and not empty
      boolean isNotVisible =
          ((drop.getHeadY() - drop.getLength() * drop.getFontSize()) > this.windowHeight);
      boolean isEmpty = (drop.getLength() == 0);

      if (isNotVisible || isEmpty) {
        DropListElement nextElem = currElem.next;
        this.dropList.removeDrop(currElem);
        currElem = nextElem;
        removedCount++;
      } else {
        currElem = currElem.next;
      }
    }

    if (this.isGenerating == false) return;

    // check if need to create new drops at all
    int dropsToAdd = 0;
    if (this.dropDelta == 0) {
      dropsToAdd = removedCount;
    } else {
      if (this.dropDelta < 0) {
        if (removedCount <= (-this.dropDelta)) {
          // no need to add new drops
          this.dropDelta += removedCount;
        } else {
          // need to add a few
          dropsToAdd = removedCount + this.dropDelta;
          this.dropDelta = 0;
        }
      } else {
        // need to add a lot
        dropsToAdd = removedCount + this.dropDelta;
        this.dropDelta = 0;
      }
    }
    for (int i = 0; i < dropsToAdd; i++) this.addRandomDrop(1);
  }

  public int getDropCount() {
    return this.dropList.length;
  }

  public synchronized boolean hasRegeneratingDrops() {
    if (this.dropDelta > 0) return true;

    if (this.dropDelta == 0) return (this.isGenerating && (this.dropList.length > 0));

    return (this.isGenerating && ((this.dropList.length + this.dropDelta) > 0));
  }

  public long getTimeDelta() {
    return endTime - startTime;
  }

  public synchronized void adjustDropCount(int delta) {
    if (delta < 0) {
      int toRemove = Math.min(this.getDropCount(), -delta);
      this.dropDelta -= toRemove;
      DropListElement currElem = this.dropList.head;
      while ((currElem != null) && (toRemove > 0)) {
        ScreenDrop drop = currElem.drop;
        if (!drop.getDrying()) {
          drop.setDrying(true);
          toRemove--;
        }
        currElem = currElem.next;
      }
    } else {
      // add new at next iteration
      this.dropDelta += delta;
    }
  }

  public synchronized void removeAllDrops() {
    // System.out.println("removing all drops");
    this.isGenerating = false;
    DropListElement currElem = this.dropList.head;
    while (currElem != null) {
      currElem.drop.setDrying(true);
      currElem = currElem.next;
    }
    this.dropDelta = 0;
  }

  public synchronized void eliminateAllDrops() {
    // System.out.println("removing all drops");
    this.dropList.head = null;
    this.dropList.length = 0;
    this.dropDelta = 0;
    this.isGenerating = false;
  }

  public synchronized void setGlobalSpeedupFactor(int newFactor) {
    if (newFactor <= 0) return;
    this.globalSpeedupFactor = newFactor;
  }

  public synchronized void incrementGlobalSpeedupFactor() {
    this.globalSpeedupFactor++;
  }
}
