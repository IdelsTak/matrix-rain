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
package org.pushingpixels.matrixrain.intro.title;

import java.awt.Point;

import org.pushingpixels.matrixrain.auxiliary.graphics.IndexBitmapObject;
import org.pushingpixels.matrixrain.auxiliary.math.intersect.Circle1PixelArbitraryIntersectorFactory;
import org.pushingpixels.matrixrain.phosphore.PhosphoreCloudFactory;
import org.pushingpixels.matrixrain.phosphore.Phosphorizer;
import org.pushingpixels.matrixrain.position.TitleConnectorPosition;
import org.pushingpixels.matrixrain.position.TitleGlyphPosition;

public class TitleZoomManager {
  private class TitleFrameElement {
    public IndexBitmapObject titleBitmap;

    public int iterationsToLive;

    public int currIteration;

    public boolean toAccelerateDrops;

    public TitleFrameElement next;

    public TitleFrameElement prev;

    public TitleFrameElement(
        IndexBitmapObject titleBitmap, int iterationsToLive, boolean toAccelerateDrops) {

      this.titleBitmap = titleBitmap;
      this.iterationsToLive = iterationsToLive;
      this.currIteration = iterationsToLive;
      this.toAccelerateDrops = toAccelerateDrops;
      this.next = null;
      this.prev = null;
    }
  }

  private class TitleFrameList {
    public TitleFrameElement head;

    public TitleFrameElement tail;

    public TitleFrameElement curr;

    public boolean toAccelerateDrops;

    public int length;

    public TitleFrameList() {
      head = null;
      tail = null;
      curr = null;
      length = 0;
      this.toAccelerateDrops = false;
    }

    public synchronized void addTitleFrameAtTail(
        IndexBitmapObject titleBitmap, int iterationsToLive, boolean toAccelerateDrops) {

      TitleFrameElement newElement =
          new TitleFrameElement(titleBitmap, iterationsToLive, toAccelerateDrops);
      if (this.tail == null) {
        // empty list
        this.head = newElement;
        this.tail = newElement;
      } else {
        this.tail.next = newElement;
        newElement.prev = this.tail;
        this.tail = newElement;
      }
      length++;
    }

    public synchronized void removeTitleFrame(TitleFrameElement element) {
      // update tail pointer
      if (element == this.tail) {
        this.tail = element.prev;
      }

      if (element == this.head) {
        this.head = element.next;
        if (this.head != null) this.head.prev = null;
      } else {
        if (element.next != null) element.next.prev = element.prev;
        if (element.prev != null) element.prev.next = element.next;
      }
      length--;
    }

    public synchronized void setCursorAtFirstFrame() {
      this.curr = this.head;
    }

    public synchronized void setCursorAtNextFrame() {
      if (this.curr != null) {
        this.toAccelerateDrops = false;
        this.curr.currIteration--;
        if (this.curr.currIteration <= 0) {
          this.curr = this.curr.next;
          if (this.curr != null) this.toAccelerateDrops = this.curr.toAccelerateDrops;
        }
      }
    }

    public synchronized boolean isCursorAfterLastFrame() {
      return (this.curr == null);
    }
  }

  private int windowWidth;

  private int windowHeight;

  private int glyphCount;

  private Point[] originalGlyphPositions;

  private int connectorCount;

  private Point[] originalConnectorPositions;

  private IndexBitmapObject startBitmapObject;

  private PhosphoreCloudFactory pcFct;

  private Circle1PixelArbitraryIntersectorFactory iFct;

  private TitleFrameList titleFrameList;

  public TitleZoomManager(
      PhosphoreCloudFactory pcFct, Circle1PixelArbitraryIntersectorFactory iFct) {
    this.titleFrameList = new TitleFrameList();
    this.pcFct = pcFct;
    this.iFct = iFct;
  }

  public void setWindowWidth(int value) {
    this.windowWidth = value;
  }

  public void setWindowHeight(int value) {
    this.windowHeight = value;
  }

  public void setGlyphCount(int value) {
    this.glyphCount = value;
  }

  public int getGlyphCount() {
    return this.glyphCount;
  }

  public void setOriginalGlyphPositions(TitleGlyphPosition[] values) {
    if (values == null) {
      this.originalGlyphPositions = null;
      return;
    }

    int length = values.length;
    if (length == 0) {
      this.originalGlyphPositions = null;
      return;
    }

    this.originalGlyphPositions = new Point[length];
    for (int i = 0; i < length; i++)
      this.originalGlyphPositions[i] = new Point(values[i].getX(), values[i].getEndY());
  }

  public void setConnectorCount(int value) {
    this.connectorCount = value;
  }

  public int getConnectorCount() {
    return this.connectorCount;
  }

  public void setOriginalConnectorPositions(TitleConnectorPosition[] values) {
    if (values == null) {
      this.originalConnectorPositions = null;
      return;
    }

    int length = values.length;
    if (length == 0) {
      this.originalConnectorPositions = null;
      return;
    }

    this.originalConnectorPositions = new Point[length];
    for (int i = 0; i < length; i++)
      this.originalConnectorPositions[i] = new Point(values[i].getPosition());
  }

  public void setFirstBitmapObject(IndexBitmapObject startBitmapObject) {
    this.startBitmapObject = startBitmapObject;
  }

  private IndexBitmapObject createNextBitmap(double factor) {
    // System.out.print("Creating bitmap with factor " + factor + "... ");

    this.pcFct.setCurrFactor(factor);

    IndexBitmapObject toPhosphorize = new IndexBitmapObject();
    int tpWidth = (int) (this.startBitmapObject.getWidth() / factor) + 4;
    if (tpWidth > this.windowWidth) tpWidth = this.windowWidth;
    int tpHeight = (int) (this.startBitmapObject.getHeight() / factor) + 4;
    if (tpHeight > this.windowHeight) tpHeight = this.windowHeight;

    int startCol = (this.startBitmapObject.getWidth() - tpWidth) / 2;
    int startRow = (this.startBitmapObject.getHeight() - tpHeight) / 2;

    int[][] startPixels = this.startBitmapObject.getBitmap();
    int[][] tpPixels = new int[tpWidth][tpHeight];
    for (int i = 0; i < tpWidth; i++)
      for (int j = 0; j < tpHeight; j++) tpPixels[i][j] = startPixels[i + startCol][j + startRow];
    toPhosphorize.setWidth(tpWidth);
    toPhosphorize.setHeight(tpHeight);
    toPhosphorize.setBitmap(tpPixels);

    IndexBitmapObject newBitmap =
        Phosphorizer.createScaledUpPhosphoreVersion(
            toPhosphorize, this.pcFct, this.iFct, factor, true);

    // System.out.println("time: " + (time1-time0) + ", create: " + delta);
    return newBitmap;
  }

  public void createAllFrames() {
    IndexBitmapObject firstFrame = this.createNextBitmap(1.0);
    this.titleFrameList.addTitleFrameAtTail(firstFrame, 2, true);

    for (double factor = 1.05; factor < 1.5; factor += 0.05) {
      IndexBitmapObject nextBitmap = this.createNextBitmap(factor);
      this.titleFrameList.addTitleFrameAtTail(nextBitmap, 2, false);
    }

    for (double factor = 1.5; factor <= 10.0; factor += 1.5) {
      IndexBitmapObject nextBitmap = this.createNextBitmap(factor);
      this.titleFrameList.addTitleFrameAtTail(nextBitmap, 1, true);
    }
  }

  public IndexBitmapObject getCurrentBitmap() {
    if (this.titleFrameList.curr == null) return null;

    return this.titleFrameList.curr.titleBitmap;
  }

  public boolean getToAccelerateDrops() {
    if (this.titleFrameList.curr == null) return false;

    return this.titleFrameList.toAccelerateDrops;
  }

  public synchronized void setCurrentAtFirstFrame() {
    this.titleFrameList.setCursorAtFirstFrame();
  }

  public synchronized void setCurrentAtNextFrame() {
    this.titleFrameList.setCursorAtNextFrame();
  }

  public synchronized boolean isCurrentAfterLastFrame() {
    return this.titleFrameList.isCursorAfterLastFrame();
  }
}
