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
package org.pushingpixels.matrixrain.intro;

import java.awt.*;

import org.pushingpixels.matrixrain.auxiliary.graphics.*;
import org.pushingpixels.matrixrain.auxiliary.graphics.colors.manager.ColorManager;
import org.pushingpixels.matrixrain.auxiliary.math.intersect.Circle1PixelArbitraryIntersectorFactory;
import org.pushingpixels.matrixrain.connector.ConnectorObject;
import org.pushingpixels.matrixrain.drop.DropManager;
import org.pushingpixels.matrixrain.font.GlyphFactory;
import org.pushingpixels.matrixrain.font.MemoryGlyph;
import org.pushingpixels.matrixrain.intro.rain.BellRainManager;
import org.pushingpixels.matrixrain.intro.rain.LetterRainManager;
import org.pushingpixels.matrixrain.intro.title.TitleManager;
import org.pushingpixels.matrixrain.intro.title.TitleZoomManager;
import org.pushingpixels.matrixrain.phosphore.PhosphoreCloud;
import org.pushingpixels.matrixrain.phosphore.PhosphoreCloudFactory;
import org.pushingpixels.matrixrain.position.TitleConnectorPosition;
import org.pushingpixels.matrixrain.position.TitleGlyphPosition;

public final class IntroManager {
  public static final int STATE_RAINREGULAR = 0;

  public static final int STATE_RAINWITHLOGO = 1;

  public static final int STATE_SHOWTITLE = 10;

  public static final int STATE_ZOOMTITLE = 11;

  public static final int STATE_WAITFORRAINTTOSTOP = 12;

  public static final int STATE_RAINEDGES = 20;

  public static final int STATE_RAINLETTERS = 21;

  public static final int STATE_MOSAICIZE = 30;

  public static final int STATE_INTROENDED = 100;

  public static final int EXPIRE_TIME_RAINREGULAR = 20;

  public static final int EXPIRE_TIME_SHOWTITLE = 10;

  public static final int TIME_BLENDTOMOSAIC = 10;

  public static final int GLOW_RADIUS = 5;

  public static final int MAX_GLOW_VALUE = 150;

  public static final int MIN_GLOW_VALUE = 60;

  public static final int CONNECTOR_GLOW = 200;

  private int windowWidth, windowHeight;

  private DropManager dropManager;

  private boolean paused;

  private int state;

  private int delayBetweenSuccessiveIterations;

  private int delayUntilNextIteration;

  private int currIteration;

  // for showing the title
  private TitleManager titleManager;

  private IndexBitmapObject startIndexBitmapObject;

  private TitleZoomManager titleZoomManager;

  private ColorManager colorManager;

  private GlyphFactory glyphFactory;

  // rains
  Image mainImage;

  private BellRainManager bellRainManager;

  private LetterRainManager letterRainManager;

  // for how long to stay in this state
  private int timeInThisState;

  // whether to check for how long to stay in this state
  private boolean startCountingToNextState;

  // off-screen image
  boolean takeTrueColor;

  private IndexImageManager indexImageManager;

  private TrueColorImageManager trueColorImageManager;

  // mosaic
  private TrueColorBitmapObject startMosaicImage;

  public IntroManager(
      Component comp,
      DropManager dropManager,
      Image bellRainImage,
      ColorManager colorManager,
      GlyphFactory glyphFactory) {

    this.dropManager = dropManager;
    this.colorManager = colorManager;
    this.glyphFactory = glyphFactory;
    this.state = IntroManager.STATE_RAINREGULAR;
    this.startCountingToNextState = false;

    this.windowWidth = comp.getWidth();
    this.windowHeight = comp.getHeight();

    this.takeTrueColor = false;

    this.indexImageManager = new IndexImageManager(comp, null, colorManager);
    this.trueColorImageManager = new TrueColorImageManager(comp);
    this.mainImage = bellRainImage;
  }

  private void paintGlyph(MemoryGlyph glyph, int x, int y) {
    int xL = glyph.getLeft(), xR = glyph.getRight();
    int yT = glyph.getTop(), yB = glyph.getBottom();
    for (int i = xL; i <= xR; i++) {
      for (int j = yT; j <= yB; j++) {
        int value = glyph.getPixel(i, j);
        if (value > 0) this.indexImageManager.paintPixel(x + i - xL, y + j, value);
      }
    }
  }

  public void paintGlyphTrail(MemoryGlyph glyph, int x, int y, int trailY, double fadeOut) {
    // Replicate each pixel vertically over 'trailY' span.
    int xL = glyph.getLeft(), xR = glyph.getRight();
    int yT = glyph.getTop(), yB = glyph.getBottom();
    double minFadeOut = 0.2, maxFadeOut = 0.7;
    for (int dy = 1; dy <= trailY; dy++) {
      double trailFadeOut = maxFadeOut - dy * (maxFadeOut - minFadeOut) / trailY;
      trailFadeOut *= fadeOut;
      for (int i = xL; i <= xR; i++) {
        for (int j = yT; j <= yB; j++) {
          int value = (int) (trailFadeOut * glyph.getPixel(i, j));
          if (value > 0) this.indexImageManager.paintPixel(x + i - xL, y + j - dy, value);
        }
      }
    }
  }

  private void gloifyGlyph(MemoryGlyph glyph, int x, int y) {
    int xL = glyph.getLeft();
    PhosphoreCloud glyphGlowCloud = glyph.getGlowCloud();
    int refPointX = glyphGlowCloud.getRefPointX();
    int refPointY = glyphGlowCloud.getRefPointY();
    int cloudWidth = glyphGlowCloud.getWidth();
    int cloudHeight = glyphGlowCloud.getHeight();
    int cloudMaxValue = glyphGlowCloud.getMaxValue();
    double coef =
        (double) (IntroManager.MAX_GLOW_VALUE - IntroManager.MIN_GLOW_VALUE)
            / (double) cloudMaxValue;
    for (int i = 0; i < cloudWidth; i++) {
      for (int j = 0; j < cloudHeight; j++) {
        double radiationValue = glyphGlowCloud.getDoubleRadiation(i, j);
        if (radiationValue > 0) {
          radiationValue = IntroManager.MIN_GLOW_VALUE + (int) (coef * radiationValue);
          this.indexImageManager.paintPixel(
              x + i - refPointX - xL, y + j - refPointY, (int) radiationValue);
        }
      }
    }
  }

  private void paintConnector(ConnectorObject connector, int colorIndex, int x, int y) {
    int connectorWidth = connector.getWidth();
    int connectorHeight = connector.getHeight();
    int[][] connectorPixels = connector.getPixels();
    double coef = (double) colorIndex / 255.0;

    int xL = x, xR = x + connectorWidth;
    int yT = y, yB = y + connectorHeight;

    for (int i = xL; i < xR; i++) {
      for (int j = yT; j < yB; j++) {
        this.indexImageManager.paintPixel(
            x + i - xL, y + j - yT, (int) (coef * connectorPixels[i - xL][j - yT]));
      }
    }
  }

  private void gloifyConnector(ConnectorObject connector, int colorIndex, int x, int y) {
    PhosphoreCloud glyphGlowCloud = connector.getGlowCloud();
    int refPointX = glyphGlowCloud.getRefPointX();
    int refPointY = glyphGlowCloud.getRefPointY();
    int cloudWidth = glyphGlowCloud.getWidth();
    int cloudHeight = glyphGlowCloud.getHeight();
    int cloudMaxValue = glyphGlowCloud.getMaxValue();
    double coef =
        (double) (IntroManager.MAX_GLOW_VALUE - IntroManager.MIN_GLOW_VALUE)
            / (double) cloudMaxValue;
    for (int i = 0; i < cloudWidth; i++) {
      for (int j = 0; j < cloudHeight; j++) {
        double radiationValue = colorIndex * glyphGlowCloud.getDoubleRadiation(i, j) / 255.0;
        if (radiationValue > 0) {
          radiationValue = IntroManager.MIN_GLOW_VALUE + (int) (coef * radiationValue);
          this.indexImageManager.paintPixel(
              x + i - refPointX, y + j - refPointY, (int) radiationValue);
        }
      }
    }
  }

  private void paintTitle() {
    int titleGlyphsCount = this.titleManager.getTitleGlyphsCount();
    for (int i = 0; i < titleGlyphsCount; i++) {
      TitleGlyphPosition tgp = this.titleManager.getTitleGlyphPosition(i);
      if (tgp.toShow()) {
        Point position = tgp.getPosition();
        MemoryGlyph tp = this.titleManager.getTitleGlyph(i);
        if (tp != null) {
          this.paintGlyph(tp, position.x, position.y);
          // if title glyph is dropping - show trail to simulate
          // motion
          if (tgp.getState() == TitleGlyphPosition.STATE_DROPPING) {
            this.paintGlyphTrail(tp, position.x, position.y, tgp.getDeltaY(), 1.0);
          }
          // for all title glyphs show "halo" - glow cloud
          this.gloifyGlyph(tp, position.x, position.y);
        }
      }
    }
    // connectors
    int titleConnectorsCount = this.titleManager.getTitleConnectorsCount();
    for (int i = 0; i < titleConnectorsCount; i++) {
      TitleConnectorPosition tcp = this.titleManager.getTitleConnectorPosition(i);
      if (tcp.toShow()) {
        Point position = tcp.getPosition();
        ConnectorObject tc = this.titleManager.getTitleConnector(i);
        this.paintConnector(tc, tcp.getCurrentGlowIndex(), position.x, position.y);
        // for all title connectors show "halo" - glow cloud
        this.gloifyConnector(tc, tcp.getCurrentGlowIndex(), position.x, position.y);
      }
    }
  }

  private void paintFinalTitle() {
    int titleGlyphsCount = this.titleManager.getTitleGlyphsCount();
    for (int i = 0; i < titleGlyphsCount; i++) {
      TitleGlyphPosition tgp = this.titleManager.getTitleGlyphPosition(i);
      Point position = tgp.getFinalPosition();
      MemoryGlyph tp = this.titleManager.getTitleGlyph(i);
      if (tp != null) {
        this.paintGlyph(tp, position.x, position.y);
      }
    }
    // connectors
    int titleConnectorsCount = this.titleManager.getTitleConnectorsCount();
    for (int i = 0; i < titleConnectorsCount; i++) {
      TitleConnectorPosition tcp = this.titleManager.getTitleConnectorPosition(i);
      Point position = tcp.getPosition();
      ConnectorObject tc = this.titleManager.getTitleConnector(i);
      this.paintConnector(tc, IntroManager.CONNECTOR_GLOW, position.x, position.y);
    }
  }

  private void paintIndexBitmap(IndexBitmapObject indexBitmapObject) {
    if (indexBitmapObject == null) return;

    int currWidth = indexBitmapObject.getWidth();
    int currHeight = indexBitmapObject.getHeight();

    int[][] currPixels = indexBitmapObject.getBitmap();

    int xL = (currWidth - this.windowWidth) / 2;
    int xR = xL + this.windowWidth;
    int yT = (currHeight - this.windowHeight) / 2;
    int yB = yT + this.windowHeight;

    for (int i = xL; i < xR; i++) {
      for (int j = yT; j < yB; j++) {
        this.indexImageManager.paintPixel(i - xL, j - yT, currPixels[i][j]);
      }
    }
  }

  private void paintTrueColorBitmap(TrueColorBitmapObject trueColorBitmapObject) {

    if (trueColorBitmapObject == null) return;

    int currWidth = trueColorBitmapObject.getWidth();
    int currHeight = trueColorBitmapObject.getHeight();

    int[][] currPixels = trueColorBitmapObject.getBitmap();

    int xL = (currWidth - this.windowWidth) / 2;
    int xR = xL + this.windowWidth;
    int yT = (currHeight - this.windowHeight) / 2;
    int yB = yT + this.windowHeight;

    for (int i = xL; i < xR; i++) {
      for (int j = yT; j < yB; j++) {
        this.trueColorImageManager.overwritePixel(i - xL, j - yT, currPixels[i][j]);
      }
    }
  }

  private void paintMosaic() {
    if (this.startMosaicImage == null) return;
    this.trueColorImageManager.overwriteTrueColorObject(this.startMosaicImage);
  }

  public synchronized void recomputeImage() {
    // reset pixel maps
    this.indexImageManager.resetImage();
    // ask drop manager to draw the drops
    this.dropManager.fillColorIndexMap(this.indexImageManager);

    switch (this.state) {
      case IntroManager.STATE_SHOWTITLE:
        this.paintTitle();
        break;
      case IntroManager.STATE_ZOOMTITLE:
        this.paintIndexBitmap(this.titleZoomManager.getCurrentBitmap());
        break;
      case IntroManager.STATE_WAITFORRAINTTOSTOP:
        break;
      case IntroManager.STATE_RAINEDGES:
        if (this.bellRainManager.toGetIndexBitmap())
          this.paintIndexBitmap(this.bellRainManager.getCurrentIndexBitmap());
        else {
          this.trueColorImageManager.resetImage();
          this.paintTrueColorBitmap(this.bellRainManager.getCurrentTrueColorBitmap());
        }
        break;
      case IntroManager.STATE_RAINLETTERS:
        if (this.letterRainManager.toGetIndexBitmap())
          this.paintIndexBitmap(this.letterRainManager.getCurrentIndexBitmap());
        else {
          this.trueColorImageManager.resetImage();
          this.paintTrueColorBitmap(this.letterRainManager.getCurrentTrueColorBitmap());
        }
        break;
      case IntroManager.STATE_MOSAICIZE:
        this.currIteration++;
        this.paintMosaic();
    }
    //
    if (this.takeTrueColor) this.trueColorImageManager.recomputeImage();
    else this.indexImageManager.recomputeImage();
  }

  public int getState() {
    return this.state;
  }

  private void createTitleBitmap() {
    this.paintFinalTitle();

    int[][] finalBitmap = this.indexImageManager.getAsBitmap();

    this.startIndexBitmapObject =
        new IndexBitmapObject(finalBitmap, this.windowWidth, this.windowHeight);
  }

  public synchronized void showTitle(
      String title,
      GlyphFactory abcGlyphFactory,
      int lineHeight,
      PhosphoreCloudFactory pcFct,
      Circle1PixelArbitraryIntersectorFactory iFct) {

    long time0 = System.currentTimeMillis();
    this.titleManager = new TitleManager(this.windowWidth, this.windowHeight);
    this.titleManager.showTitle(title, abcGlyphFactory, lineHeight, IntroManager.CONNECTOR_GLOW);
    this.titleManager.makeClone();
    long time1 = System.currentTimeMillis();

    this.titleZoomManager = new TitleZoomManager(pcFct, iFct);
    this.titleZoomManager.setWindowWidth(this.windowWidth);
    this.titleZoomManager.setWindowHeight(this.windowHeight);

    this.titleZoomManager.setGlyphCount(this.titleManager.getTitleGlyphsCount());
    this.titleZoomManager.setOriginalGlyphPositions(this.titleManager.getTitleGlyphPositions());

    this.titleZoomManager.setConnectorCount(this.titleManager.getTitleConnectorsCount());
    this.titleZoomManager.setOriginalConnectorPositions(
        this.titleManager.getTitleConnectorPositions());

    this.createTitleBitmap();
    this.titleZoomManager.setFirstBitmapObject(this.startIndexBitmapObject);
    this.titleZoomManager.createAllFrames();
    long time2 = System.currentTimeMillis();

    System.out.println("Title manager init: " + (time1 - time0));
    System.out.println("Title zoom manager init: " + (time2 - time1));

    this.startCountingToNextState = true;
    this.paused = false;
    this.delayBetweenSuccessiveIterations = 1;
    this.delayUntilNextIteration = 1;
    this.timeInThisState = IntroManager.EXPIRE_TIME_RAINREGULAR;
  }

  public synchronized void replay(boolean stayForeverInMatrixRain) {
    System.out.println("Replaying intro...");
    this.startCountingToNextState = !stayForeverInMatrixRain;
    // start from indexed image
    this.takeTrueColor = false;
    this.indexImageManager.resetImage();
    this.indexImageManager.recomputeImage();
    // restore title letters and connectors
    this.titleManager.restore();
    // start in PAUSE
    this.paused = false;
    // default speed
    this.delayBetweenSuccessiveIterations = 1;
    this.delayUntilNextIteration = 1;
    // back to regular rain
    this.dropManager.createDrops();
    this.dropManager.adjustDropCount(this.windowWidth / 5);
    this.timeInThisState = IntroManager.EXPIRE_TIME_RAINREGULAR;
    this.state = IntroManager.STATE_RAINREGULAR;
  }

  public synchronized void slowdown() {
    if (this.delayBetweenSuccessiveIterations < 10) {
      this.delayBetweenSuccessiveIterations++;
      return;
    }
    if (this.delayBetweenSuccessiveIterations < 20) {
      this.delayBetweenSuccessiveIterations += 2;
      return;
    }
    if (this.delayBetweenSuccessiveIterations < 100) {
      this.delayBetweenSuccessiveIterations += 10;
      return;
    }
  }

  public synchronized void speedup() {
    if (this.delayBetweenSuccessiveIterations > 20) {
      this.delayBetweenSuccessiveIterations -= 10;
      return;
    }
    if (this.delayBetweenSuccessiveIterations > 10) {
      this.delayBetweenSuccessiveIterations -= 2;
      return;
    }
    if (this.delayBetweenSuccessiveIterations > 1) {
      this.delayBetweenSuccessiveIterations--;
      return;
    }
  }

  public synchronized void resume() {
    this.paused = false;
  }

  public synchronized void pause() {
    this.paused = true;
  }

  public synchronized void stop() {
    this.dropManager.removeAllDrops();
    this.state = IntroManager.STATE_INTROENDED;
  }

  public synchronized void removeDrops() {
    if (this.canRemoveDrops()) this.dropManager.adjustDropCount(-10);
  }

  public synchronized void addDrops() {
    if (this.canAddDrops()) this.dropManager.adjustDropCount(10);
  }

  public synchronized boolean isPaused() {
    return (this.paused == true);
  }

  public synchronized boolean isAtMaxSpeed() {
    return (this.delayBetweenSuccessiveIterations == 1);
  }

  public synchronized boolean isAtMinSpeed() {
    return (this.delayBetweenSuccessiveIterations >= 100);
  }

  public synchronized boolean canAddDrops() {
    return ((this.state == IntroManager.STATE_RAINREGULAR)
        && (this.startCountingToNextState == false));
  }

  public synchronized boolean canRemoveDrops() {
    return ((this.state == IntroManager.STATE_RAINREGULAR)
        && (this.startCountingToNextState == false)
        && this.dropManager.hasRegeneratingDrops());
  }

  public synchronized int[] getBitmap1D() {
    if (this.takeTrueColor) return this.trueColorImageManager.getBitmap1D();
    else return this.indexImageManager.getBitmap1D();
  }

  public synchronized Image getBitmap() {
    if (this.takeTrueColor) return this.trueColorImageManager.getImage();
    else return this.indexImageManager.getImage();
  }

  public DropManager getDropManager() {
    return this.dropManager;
  }

  public synchronized void iteration(boolean iterateAlways) {
    if (!iterateAlways) {
      if (this.paused) return;

      this.delayUntilNextIteration--;
      if (this.delayUntilNextIteration > 0) return;

      this.delayUntilNextIteration = this.delayBetweenSuccessiveIterations;
    }

    // System.out.println(this.state);
    switch (this.state) {
      case IntroManager.STATE_RAINREGULAR:
        dropManager.iteration(1);
        this.recomputeImage();
        if (this.startCountingToNextState) {
          this.timeInThisState--;
          if (this.timeInThisState <= 0) {
            this.dropManager.setGlobalSpeedupFactor(2);
            this.state = IntroManager.STATE_SHOWTITLE;
            this.currIteration = 0;
            //					this.timeInThisState = IntroManager.EXPIRE_TIME_SHOWTITLE;
            this.dropManager.removeAllDrops();
            this.titleZoomManager.setCurrentAtFirstFrame();
            this.takeTrueColor = true;
            this.timeInThisState =
                this.titleManager.getTitleAnimationEndTime() + IntroManager.EXPIRE_TIME_SHOWTITLE;
            // this.state = IntroManager.STATE_WAITFORRAINTTOSTOP;
            // this.dropManager.removeAllDrops();

            // this.state = IntroManager.STATE_MOSAICIZE;
            // this.timeInThisState = IntroManager.TIME_BLENDTOMOSAIC;
            this.currIteration = 0;
          }
        }
        break;

        // case IntroManager.STATE_RAINWITHLOGO:
        // this.takeTrueColor = true;
        // dropManager.iteration(1);
        // this.recomputeImage();
        // if (this.startCountingToNextState) {
        // this.timeInThisState--;
        // if (this.timeInThisState <= 0) {
        // this.state = IntroManager.STATE_SHOWTITLE;
        // this.timeInThisState = this.titleManager
        // .getTitleAnimationEndTime()
        // + IntroManager.EXPIRE_TIME_SHOWTITLE;
        // this.titleZoomManager.setCurrentAtFirstFrame();
        // }
        // }
        // break;
        //
      case IntroManager.STATE_SHOWTITLE:
        this.takeTrueColor = false;
        dropManager.iteration(1);
        this.recomputeImage();
        this.timeInThisState--;
        if (this.timeInThisState <= 0) {
          this.state = IntroManager.STATE_ZOOMTITLE;
          this.titleZoomManager.setCurrentAtFirstFrame();
          break;
        }
        this.titleManager.iteration(1);
        break;

      case IntroManager.STATE_ZOOMTITLE:
        dropManager.iteration(1);
        this.recomputeImage();
        this.titleZoomManager.setCurrentAtNextFrame();
        if (this.titleZoomManager.getToAccelerateDrops())
          this.dropManager.incrementGlobalSpeedupFactor();
        if (this.titleZoomManager.isCurrentAfterLastFrame()) {
          this.state = IntroManager.STATE_WAITFORRAINTTOSTOP;
          this.dropManager.removeAllDrops();
        }
        break;

      case IntroManager.STATE_WAITFORRAINTTOSTOP:
        dropManager.iteration(1);
        this.recomputeImage();
        if (dropManager.getDropCount() == 0) {
          long time0 = System.currentTimeMillis();
          this.bellRainManager =
              new BellRainManager(
                  this.windowWidth, this.windowHeight, this.mainImage, 3, 6, this.colorManager);
          long time1 = System.currentTimeMillis();
          System.out.println("Created bell rain manager in " + (time1 - time0));
          this.state = IntroManager.STATE_RAINEDGES;
        }
        break;

      case IntroManager.STATE_RAINEDGES:
        this.bellRainManager.iteration();
        this.takeTrueColor = !this.bellRainManager.toGetIndexBitmap();
        this.recomputeImage();
        if (this.bellRainManager.isFinished()) {
          this.bellRainManager = null;
          long time0 = System.currentTimeMillis();
          this.letterRainManager =
              new LetterRainManager(
                  this.windowWidth,
                  this.windowHeight,
                  this.mainImage,
                  6,
                  this.colorManager,
                  this.glyphFactory);
          long time1 = System.currentTimeMillis();
          System.out.println("Created letter rain manager in " + (time1 - time0));
          this.state = IntroManager.STATE_RAINLETTERS;
        }
        break;

      case IntroManager.STATE_RAINLETTERS:
        this.letterRainManager.iteration();
        this.takeTrueColor = !this.letterRainManager.toGetIndexBitmap();
        this.recomputeImage();
        if (this.letterRainManager.isFinished()) {
          this.letterRainManager = null;
          this.startMosaicImage =
              new TrueColorBitmapObject(this.mainImage, this.windowWidth, this.windowHeight);

          this.state = IntroManager.STATE_MOSAICIZE;
          this.timeInThisState = IntroManager.TIME_BLENDTOMOSAIC;
          this.currIteration = 0;
        }
        break;

      case IntroManager.STATE_MOSAICIZE:
        this.takeTrueColor = true;
        this.recomputeImage();
        if (this.startCountingToNextState) {
          this.timeInThisState--;
          if (this.timeInThisState <= 0) {
            System.out.println("Intro ended...");
            this.state = IntroManager.STATE_INTROENDED;
          }
        }
        break;

      case IntroManager.STATE_INTROENDED:
        break;
    }
  }

  public TrueColorBitmapObject getMosaicBitmap() {
    return this.startMosaicImage;
  }

  public void next() {
    /*
     * this.titleZoomManager.setCurrentAtNextFrame(); if
     * (this.titleZoomManager.getToAccelerateDrops())
     * this.dropManager.incrementGlobalSpeedupFactor(); if
     * (this.titleZoomManager.isCurrentAfterLastFrame()) { this.state =
     * IntroManager.STATE_SHOWALLLETTERS; this.dropManager.removeAllDrops(); }
     */
    switch (this.state) {
      case IntroManager.STATE_RAINEDGES:
        this.bellRainManager.iteration();
        break;
      case IntroManager.STATE_RAINLETTERS:
        this.letterRainManager.iteration();
        break;
    }
  }

  public synchronized boolean isEnded() {
    return (this.state == IntroManager.STATE_INTROENDED);
  }
}
