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
package org.pushingpixels.matrixrain.intro.rain;

import java.awt.*;
import java.awt.image.PixelGrabber;

import org.pushingpixels.matrixrain.auxiliary.graphics.IndexBitmapObject;
import org.pushingpixels.matrixrain.auxiliary.graphics.TrueColorBitmapObject;
import org.pushingpixels.matrixrain.auxiliary.graphics.colors.manager.ColorManager;
import org.pushingpixels.matrixrain.auxiliary.graphics.spline.SplineInterpolatorObject;
import org.pushingpixels.matrixrain.auxiliary.graphics.spline.SplineManager;
import org.pushingpixels.matrixrain.auxiliary.math.GaussValues;
import org.pushingpixels.matrixrain.font.GlyphFactory;
import org.pushingpixels.matrixrain.font.MemoryGlyph;

public final class LetterRainManager {
  private static final int N = 8;

  private static final int M = 6;

  private static final int WAVE_COUNT = 10;

  private static final double CUTOFF_THRESHOLD = 0.75;

  private static final int STATE_LETTERRAIN_FIRSTPHASE = 0;

  private static final int STATE_LETTERRAIN_SECONDPHASE = 1;

  private static final int STATE_LETTERRAIN_FINISHED = 10;

  private static final int STATE_WAVE_WAITING = 0;

  private static final int STATE_WAVE_STARTED = 1;

  private static final int STATE_WAVE_ENDED = 2;

  private int width, height;

  private int letterSize;

  private int widthInLetters, heightInLetters;

  private LetterRainPixelInfo[][] pixelInfoArray;

  private LetterRainCellInfo[][] cellInfoArray;

  private LetterRainColumnInfo[] columnInfoArray;

  private LetterRainScreenInfo screenInfo;

  // rain start and delta - first step
  private int[] rain_ys;

  private int[][] rain_delta;

  private int[] rain_curr;

  // for iterations
  private ColorManager colorManager;

  private GlyphFactory glyphFactory;

  private int totalGlyphCount;

  private int currIteration;

  private int maxIterationFirstPhase;

  private int maxIterationSecondPhase;

  private int state;

  private IndexBitmapObject currIndexBitmap;

  private TrueColorBitmapObject currTrueColorBitmap;

  // interpolators for first phase
  private SplineInterpolatorObject fullLetterToMonochromeLetterFadeInterpolator;

  // interpolators for second phase
  private SplineInterpolatorObject monochromeLetterToFullColorLetterFadeInterpolator;

  private SplineInterpolatorObject blackBackgroundToFullColorBackgroundFadeInterpolator;

  private final class LetterRainPixelInfo {
    public int red;

    public int green;

    public int blue;

    public int trueColor;

    public double luminosity;

    public double relativeLuminosity;

    public boolean isExposedByRain;
  }

  private final class LetterRainWaveInfo {
    public int age;

    public int timeUntilStart;

    public int[] deltas;

    public int state;

    public int timeFromStart;

    public int currHeadRow;
  }

  private class LetterRainWaveInfoElement {
    public LetterRainWaveInfo waveInfo;

    public LetterRainWaveInfoElement next;

    public LetterRainWaveInfoElement prev;

    public LetterRainWaveInfoElement(LetterRainWaveInfo waveInfo) {
      this.waveInfo = waveInfo;
      this.next = null;
      this.prev = null;
    }
  }

  private class LetterRainWaveInfoList {
    public LetterRainWaveInfoElement head;

    public LetterRainWaveInfoElement tail;

    public int length;

    public LetterRainWaveInfoList() {
      head = null;
      tail = null;
      length = 0;
    }

    public synchronized void addWaveInfo(LetterRainWaveInfo waveInfo) {
      LetterRainWaveInfoElement newElement = new LetterRainWaveInfoElement(waveInfo);
      newElement.next = this.head;
      if (this.head != null) this.head.prev = newElement;
      if (this.head == null) this.tail = newElement;
      this.head = newElement;
      length++;
    }

    public synchronized void addWaveInfoAtTail(LetterRainWaveInfo waveInfo) {
      LetterRainWaveInfoElement newElement = new LetterRainWaveInfoElement(waveInfo);
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

    public synchronized void removeWaveInfo(LetterRainWaveInfoElement element) {
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
  }

  private final class LetterRainCellInfo {
    public double averageLuminosity;

    public double relativeAverageLuminosity;

    public boolean isExposedByRain;

    public int glyphIndex;

    public int glyphBlur;

    public int glyphRadiance;

    public double glyphFade;

    public double glyphFadeCoef;
    // public MemoryGlyph currGlyph;
  }

  private final class LetterRainColumnInfo {
    public LetterRainWaveInfoList waves;
  }

  private final class LetterRainScreenInfo {
    public double averagePixelLuminosity;

    public double maxPixelLuminosity;

    public double averageCellLuminosity;

    public double maxCellLuminosity;
  }

  public LetterRainManager(
      int width,
      int height,
      Image finalImage,
      int letterSize,
      ColorManager colorManager,
      GlyphFactory glyphFactory) {

    this.width = width;
    this.height = height;
    this.letterSize = letterSize;
    this.colorManager = colorManager;
    this.glyphFactory = glyphFactory;
    this.totalGlyphCount = glyphFactory.getGlyphCount();

    // compute dimension in letters
    this.widthInLetters = (int) (Math.ceil((double) width / (double) letterSize));
    this.heightInLetters = 1 + (int) (Math.ceil((double) height / (double) letterSize));

    // get original pixels
    int[] origPixels = new int[height * width];
    PixelGrabber pg = new PixelGrabber(finalImage, 0, 0, width, height, origPixels, 0, width);
    try {
      pg.grabPixels();
    } catch (InterruptedException e) {
      System.err.println("Everything not cool");
      return;
    }
    if ((pg.getStatus() & java.awt.image.ImageObserver.ABORT) != 0) {
      System.err.println("Everything not cool");
      return;
    }

    this.screenInfo = new LetterRainScreenInfo();
    int[] luminosities = new int[256];
    for (int i = 0; i < luminosities.length; i++) luminosities[i] = 0;

    // create info structures on all pixels
    int pixelCount = 0;
    double pixelSum = 0.0;
    double maxPixelLum = 0.0;
    this.pixelInfoArray = new LetterRainPixelInfo[width][height];
    for (int col = 0; col < this.width; col++) {
      for (int row = 0; row < this.height; row++) {
        int index = row * this.width + col;
        LetterRainPixelInfo currPixelInfo = new LetterRainPixelInfo();
        currPixelInfo.trueColor = origPixels[index];
        int red = currPixelInfo.trueColor & 0x00FF0000;
        red >>= 16;
        currPixelInfo.red = red;
        int green = currPixelInfo.trueColor & 0x0000FF00;
        green >>= 8;
        currPixelInfo.green = green;
        int blue = currPixelInfo.trueColor & 0x000000FF;
        currPixelInfo.blue = blue;

        currPixelInfo.luminosity = (222 * red + 707 * green + 71 * blue) / 1000;
        luminosities[(int) currPixelInfo.luminosity]++;
        pixelSum += currPixelInfo.luminosity;
        if (currPixelInfo.luminosity > maxPixelLum) maxPixelLum = currPixelInfo.luminosity;
        pixelCount++;

        currPixelInfo.isExposedByRain = false;

        this.pixelInfoArray[col][row] = currPixelInfo;
      }
    }
    this.screenInfo.averagePixelLuminosity = pixelSum / pixelCount;
    this.screenInfo.maxPixelLuminosity = maxPixelLum;

    int cutoffLuminosity = 255;
    int cutoffThreshold = (int) (this.width * this.height * LetterRainManager.CUTOFF_THRESHOLD);
    int cutoffSum = 0;
    for (int i = 0; i < luminosities.length; i++) {
      cutoffSum += luminosities[i];
      if (cutoffSum > cutoffThreshold) {
        cutoffLuminosity = i;
        break;
      }
    }
    // System.out.println("max lum: " + maxPixelLum);
    // System.out.println("avg lum: " +
    // this.screenInfo.averagePixelLuminosity);
    // System.out.println("cut lum: " + cutoffLuminosity);
    for (int col = 0; col < this.width; col++) {
      for (int row = 0; row < this.height; row++) {
        double currLum = this.pixelInfoArray[col][row].luminosity;
        if (currLum > cutoffLuminosity) this.pixelInfoArray[col][row].relativeLuminosity = 255.0;
        else
          this.pixelInfoArray[col][row].relativeLuminosity =
              255.0 * this.pixelInfoArray[col][row].luminosity / cutoffLuminosity;
      }
    }

    this.screenInfo.averagePixelLuminosity = pixelSum / pixelCount;

    // create info structures on all cells
    this.cellInfoArray = new LetterRainCellInfo[this.widthInLetters][this.heightInLetters];
    for (int cellCol = 0; cellCol < this.widthInLetters; cellCol++) {
      for (int cellRow = 0; cellRow < this.heightInLetters; cellRow++) {
        LetterRainCellInfo currCellInfo = new LetterRainCellInfo();

        // go over all pixels of this cell
        int startX = cellCol * this.letterSize;
        int endX = (cellCol + 1) * this.letterSize;
        if (endX >= this.width) endX = this.width - 1;
        int startY = cellRow * this.letterSize;
        int endY = (cellRow + 1) * this.letterSize;
        if (endY >= this.height) endY = this.height - 1;
        int count = 0;
        double luminositySum = 0.0;
        for (int x = startX; x < endX; x++) {
          for (int y = startY; y < endY; y++) {
            luminositySum += this.pixelInfoArray[x][y].luminosity;
            count++;
          }
        }

        double avgLuminosity = (count > 0) ? (luminositySum / count) : 0;
        currCellInfo.averageLuminosity = avgLuminosity;

        currCellInfo.isExposedByRain = false;

        this.cellInfoArray[cellCol][cellRow] = currCellInfo;
      }
    }

    // for (int clusterRow=0; clusterRow < this.heightInClusters;
    // clusterRow++) {
    // for (int clusterCol=0; clusterCol < this.widthInClusters;
    // clusterCol++) {
    // System.out.print(this.clusterInfoArray[clusterCol][clusterRow].distanceToClosestTopEdge
    // + ",");
    // }
    // System.out.println();
    // }

    int[][] bitmapPixels = new int[this.width][this.height];
    for (int i = 0; i < this.width; i++)
      for (int j = 0; j < this.height; j++) bitmapPixels[i][j] = 0;
    this.currIndexBitmap = new IndexBitmapObject(bitmapPixels, this.width, this.height);

    // long time0 = System.currentTimeMillis();
    // compute rain start positions
    this.rain_ys = new int[this.widthInLetters];
    this.rain_curr = new int[this.widthInLetters];
    for (int x = 0; x < this.widthInLetters; x++) {
      double currYs = 0.0;
      for (int i = 1; i < N; i++) {
        double k = 2 * Math.PI / (N * this.widthInLetters);
        currYs +=
            ((double) (N + Math.random()) / (double) i)
                * Math.sin(k * i * x + 2 * Math.PI * Math.random());
      }
      this.rain_ys[x] = (int) currYs;
    }
    int maxYs = this.rain_ys[0];
    for (int i = 1; i < this.widthInLetters; i++)
      if (this.rain_ys[i] > maxYs) maxYs = this.rain_ys[i];

    for (int i = 0; i < this.widthInLetters; i++) {
      this.rain_ys[i] -= maxYs;
      this.rain_ys[i]--;
      this.rain_curr[i] = this.rain_ys[i];
      // System.out.print(this.rain_ys[i] + ",");
    }

    // compute rain deltas
    this.maxIterationFirstPhase = maxYs + 2 * this.heightInLetters;
    this.rain_delta = new int[this.widthInLetters][this.maxIterationFirstPhase];
    for (int x = 0; x < this.widthInLetters; x++) {
      for (int t = 0; t < this.maxIterationFirstPhase; t++) {
        double currDelta = 0.0;
        double phase = 2.0 * Math.PI * Math.random();
        for (int i = 1; i < M; i++) currDelta += Math.sin(i * x * (t + 1) + phase);
        if (currDelta < 0.0) currDelta = -currDelta;
        currDelta += 1.0;
        this.rain_delta[x][t] = (int) currDelta;
      }
    }

    // compute the end of the first step
    int maxTf = 0;
    for (int i = 0; i < this.widthInLetters; i++) {
      int currSum = this.rain_ys[i];
      int currT = 0;
      while (currT < this.maxIterationFirstPhase) {
        currSum += this.rain_delta[i][currT];
        if (currSum >= this.heightInLetters) {
          if (currT > maxTf) maxTf = currT;
          break;
        }
        currT++;
      }
    }
    // compute the end of the first step
    this.maxIterationFirstPhase = maxTf + this.heightInLetters;

    // interpolators
    Point[] fullLetterToMonochromeLetterFadePoints = new Point[3];
    fullLetterToMonochromeLetterFadePoints[0] = new Point(0, 0);
    fullLetterToMonochromeLetterFadePoints[1] = new Point(this.maxIterationFirstPhase / 2, 0);
    fullLetterToMonochromeLetterFadePoints[2] = new Point(this.maxIterationFirstPhase, 100);

    this.fullLetterToMonochromeLetterFadeInterpolator =
        SplineManager.getSplineInterpolation(fullLetterToMonochromeLetterFadePoints);

    // this.edgeFadeInterpolator.print();

    // second step
    this.maxIterationSecondPhase = this.heightInLetters * 2;

    // create waves
    int averageIntervalBetweenWaves =
        (this.maxIterationFirstPhase + this.maxIterationSecondPhase) / LetterRainManager.WAVE_COUNT;
    this.columnInfoArray = new LetterRainColumnInfo[this.widthInLetters];
    for (int currCol = 0; currCol < this.widthInLetters; currCol++) {
      LetterRainColumnInfo newColumnInfo = new LetterRainColumnInfo();
      newColumnInfo.waves = new LetterRainWaveInfoList();
      // create 10 waves
      int prevStart = 0;
      for (int currWaveIndex = 0; currWaveIndex < 10; currWaveIndex++) {
        LetterRainWaveInfo newWaveInfo = new LetterRainWaveInfo();
        newWaveInfo.age = currWaveIndex;
        if (currWaveIndex == 0) {
          newWaveInfo.timeUntilStart = -this.rain_ys[currCol];
        } else {
          int interval = averageIntervalBetweenWaves + GaussValues.getRandomGaussian(5);
          newWaveInfo.timeUntilStart = prevStart + interval;
        }
        prevStart = newWaveInfo.timeUntilStart;

        newWaveInfo.deltas = new int[this.heightInLetters];
        for (int currTime = 0; currTime < this.heightInLetters; currTime++)
          newWaveInfo.deltas[currTime] = 1 + GaussValues.getRandomGaussian(currWaveIndex + 2);

        newWaveInfo.state = LetterRainManager.STATE_WAVE_WAITING;

        newColumnInfo.waves.addWaveInfoAtTail(newWaveInfo);
      }

      this.columnInfoArray[currCol] = newColumnInfo;
    }
    /*
     * // print info on all columns for (int currCol=0; currCol<this.widthInLetters;
     * currCol++) { LetterRainColumnInfo currColumnInfo =
     * this.columnInfoArray[currCol]; System.out.println("Column " +
     * currCol); LetterRainWaveInfoList waveList = currColumnInfo.waves;
     * LetterRainWaveInfoElement currWaveElement = waveList.head; while
     * (currWaveElement != null) { LetterRainWaveInfo currWaveInfo =
     * currWaveElement.waveInfo; System.out.print(" wave " +
     * currWaveInfo.age); System.out.print(": start at " +
     * currWaveInfo.timeUntilStart + "; "); for (int i=0; i<currWaveInfo.deltas.length;
     * i++) { System.out.print(currWaveInfo.deltas[i] + ","); }
     * System.out.println(); currWaveElement = currWaveElement.next; } }
     */

    int cellCount = 0;
    double cellSum = 0.0;
    double maxCellLum = 0.0;
    for (int currCol = 0; currCol < this.widthInLetters; currCol++) {
      for (int currRow = 0; currRow < this.heightInLetters; currRow++) {
        double currLum = this.cellInfoArray[currCol][currRow].averageLuminosity;
        cellSum += currLum;
        if (currLum > maxCellLum) maxCellLum = currLum;
        cellCount++;
      }
    }
    this.screenInfo.averageCellLuminosity = cellSum / cellCount;
    this.screenInfo.maxCellLuminosity = maxCellLum;
    for (int currCol = 0; currCol < this.widthInLetters; currCol++) {
      for (int currRow = 0; currRow < this.heightInLetters; currRow++) {
        this.cellInfoArray[currCol][currRow].relativeAverageLuminosity =
            255.0
                * this.cellInfoArray[currCol][currRow].averageLuminosity
                / this.screenInfo.maxCellLuminosity;
      }
    }

    // interpolators
    Point[] monochromeLetterToFullColorLetterFadePoints = new Point[4];
    monochromeLetterToFullColorLetterFadePoints[0] = new Point(0, 0);
    monochromeLetterToFullColorLetterFadePoints[1] =
        new Point(this.maxIterationSecondPhase / 2, 60);
    monochromeLetterToFullColorLetterFadePoints[2] =
        new Point(4 * this.maxIterationSecondPhase / 5, 100);
    monochromeLetterToFullColorLetterFadePoints[3] = new Point(this.maxIterationSecondPhase, 100);

    this.monochromeLetterToFullColorLetterFadeInterpolator =
        SplineManager.getSplineInterpolation(monochromeLetterToFullColorLetterFadePoints);

    Point[] blackBackgroundToFullColorBackgroundFadePoints = new Point[4];
    blackBackgroundToFullColorBackgroundFadePoints[0] = new Point(0, 0);
    blackBackgroundToFullColorBackgroundFadePoints[1] =
        new Point(this.maxIterationSecondPhase / 2, 30);
    blackBackgroundToFullColorBackgroundFadePoints[2] =
        new Point(5 * this.maxIterationSecondPhase / 6, 100);
    blackBackgroundToFullColorBackgroundFadePoints[3] =
        new Point(this.maxIterationSecondPhase, 100);

    this.blackBackgroundToFullColorBackgroundFadeInterpolator =
        SplineManager.getSplineInterpolation(blackBackgroundToFullColorBackgroundFadePoints);

    this.state = LetterRainManager.STATE_LETTERRAIN_FIRSTPHASE;
    // System.out.println("Letter rain creation: " + (time3-time0));
  }

  private void advanceWaves() {
    // go over all letters and advance down waves if necessary
    for (int cellCol = 0; cellCol < this.widthInLetters; cellCol++) {
      LetterRainColumnInfo currColumnInfo = this.columnInfoArray[cellCol];
      LetterRainWaveInfoList waveList = currColumnInfo.waves;
      LetterRainWaveInfoElement currWaveElement = waveList.head;
      while (currWaveElement != null) {
        LetterRainWaveInfo currWaveInfo = currWaveElement.waveInfo;
        if (currWaveInfo.state == LetterRainManager.STATE_WAVE_WAITING) {
          currWaveInfo.timeUntilStart--;
          if (currWaveInfo.timeUntilStart <= 0) {
            currWaveInfo.state = LetterRainManager.STATE_WAVE_STARTED;
            currWaveInfo.timeFromStart = 0;
            currWaveInfo.currHeadRow = 0;
          }
        } else currWaveInfo.timeFromStart++;

        if (currWaveInfo.timeFromStart >= currWaveInfo.deltas.length)
          currWaveInfo.state = LetterRainManager.STATE_WAVE_ENDED;

        if (currWaveInfo.state == LetterRainManager.STATE_WAVE_STARTED) {
          // if here - this wave is under way
          // System.out.println("column " + cellCol + ", wave " +
          // currWaveInfo.age);
          int newHeadRow =
              currWaveInfo.currHeadRow + currWaveInfo.deltas[currWaveInfo.timeFromStart];
          int oldVisibleHeadRow =
              (currWaveInfo.currHeadRow >= this.heightInLetters)
                  ? (this.heightInLetters - 1)
                  : currWaveInfo.currHeadRow;
          // update radiance of trailing glyphs
          for (int currRow = 0; currRow < oldVisibleHeadRow; currRow++) {
            LetterRainCellInfo currCellInfo = cellInfoArray[cellCol][currRow];
            currCellInfo.glyphRadiance--;
            if (currCellInfo.glyphRadiance < 0) currCellInfo.glyphRadiance = 0;
          }
          int newVisibleHeadRow =
              (newHeadRow >= this.heightInLetters) ? (this.heightInLetters - 1) : newHeadRow;
          for (int currRow = oldVisibleHeadRow; currRow <= newVisibleHeadRow; currRow++) {
            LetterRainCellInfo currCellInfo = cellInfoArray[cellCol][currRow];
            currCellInfo.glyphIndex = (int) (Math.random() * this.totalGlyphCount);
            currCellInfo.glyphBlur = 1;
            currCellInfo.glyphRadiance = (currRow == newHeadRow) ? 2 : 1;
            currCellInfo.glyphFade = 1.0;
            currCellInfo.glyphFadeCoef =
                1.0 - (0.07 - 0.07 * currWaveInfo.age / LetterRainManager.WAVE_COUNT);
            currCellInfo.isExposedByRain = true;
          }
          currWaveInfo.currHeadRow = newHeadRow;
        }

        currWaveElement = currWaveElement.next;
      }

      // after all waves have been processed - update fade factor
      for (int currRow = 0; currRow < this.heightInLetters; currRow++) {
        LetterRainCellInfo currCellInfo = cellInfoArray[cellCol][currRow];
        if (currCellInfo.glyphRadiance == 0) currCellInfo.glyphFade *= currCellInfo.glyphFadeCoef;
      }
    }
  }

  private void iterationStep1() {
    this.advanceWaves();

    this.currIndexBitmap.reset(0);
    int[][] bitmap = this.currIndexBitmap.getBitmap();

    double fullLetterToMonochromeLetterCoef =
        this.fullLetterToMonochromeLetterFadeInterpolator.getValue(this.currIteration) / 100.0;
    if (fullLetterToMonochromeLetterCoef < 0.0) fullLetterToMonochromeLetterCoef = 0.0;

    // go over all letters and draw them
    for (int cellCol = 0; cellCol < this.widthInLetters; cellCol++) {
      for (int cellRow = 0; cellRow < this.heightInLetters; cellRow++) {
        LetterRainCellInfo currCellInfo = this.cellInfoArray[cellCol][cellRow];
        if (!currCellInfo.isExposedByRain) continue;
        MemoryGlyph glyph =
            this.glyphFactory.getGlyph(
                currCellInfo.glyphIndex, 1, currCellInfo.glyphBlur, currCellInfo.glyphRadiance);
        double fadeCoef = currCellInfo.glyphFade;

        int xL = glyph.getLeft(), xR = glyph.getRight();
        int yT = glyph.getTop(), yB = glyph.getBottom();
        int x = cellCol * this.letterSize;
        int y = cellRow * this.letterSize;
        for (int i = xL; i <= xR; i++) {
          for (int j = yT; j <= yB; j++) {
            int rainPixelValue = (int) (fadeCoef * glyph.getPixel(i, j));

            int bx = x + i;
            if ((bx < 0) || (bx >= this.width)) continue;
            int by = y + j;
            if ((by < 0) || (by >= this.height)) continue;

            // fade from full rain to rain on pixels
            // 0.0 -> rainPixelValue
            // 1.0 -> rainPixelValue*picture pixel value/255.0
            LetterRainPixelInfo currPixelInfo = this.pixelInfoArray[bx][by];

            int newValue =
                rainPixelValue
                    + (int)
                        (fullLetterToMonochromeLetterCoef
                            * (currPixelInfo.relativeLuminosity - rainPixelValue));

            if (newValue > rainPixelValue) newValue = rainPixelValue;

            if ((newValue > 0) && (newValue > bitmap[bx][by])) bitmap[bx][by] = newValue;
          }
        }
      }
    }
  }

  private void iterationStep2() {
    this.advanceWaves();

    this.currTrueColorBitmap.reset(0);
    int[][] bitmap = this.currTrueColorBitmap.getBitmap();

    double monochromeLetterToFullColorLetterCoef =
        this.monochromeLetterToFullColorLetterFadeInterpolator.getValue(this.currIteration) / 100.0;
    if (monochromeLetterToFullColorLetterCoef < 0.0) monochromeLetterToFullColorLetterCoef = 0.0;

    double blackBackgroundToFullColorBackgroundCoef =
        this.blackBackgroundToFullColorBackgroundFadeInterpolator.getValue(this.currIteration)
            / 100.0;
    if (blackBackgroundToFullColorBackgroundCoef < 0.0)
      blackBackgroundToFullColorBackgroundCoef = 0.0;

    for (int cellCol = 0; cellCol < this.widthInLetters; cellCol++) {
      for (int cellRow = 0; cellRow < this.heightInLetters; cellRow++) {
        LetterRainCellInfo currCellInfo = this.cellInfoArray[cellCol][cellRow];
        MemoryGlyph glyph =
            this.glyphFactory.getGlyph(
                currCellInfo.glyphIndex, 1, currCellInfo.glyphBlur, currCellInfo.glyphRadiance);
        double fadeCoef = currCellInfo.glyphFade;

        int gS = glyph.getSize();
        int x = cellCol * this.letterSize;
        int y = cellRow * this.letterSize;
        for (int i = 0; i < gS; i++) {
          for (int j = 0; j < gS; j++) {
            int glyphPixelValue = glyph.getPixel(i, j);
            int rainPixelValue = (int) (fadeCoef * glyphPixelValue);

            int bx = x + i;
            if ((bx < 0) || (bx >= this.width)) continue;
            int by = y + j;
            if ((by < 0) || (by >= this.height)) continue;

            LetterRainPixelInfo currPixelInfo = this.pixelInfoArray[bx][by];
            int startR, startG, startB;
            double coef;

            if (glyphPixelValue > 0) {
              // pixel on glyph

              // fade from monochrome to full-color rain on pixels
              // 0.0 -> monochrome components
              // 1.0 -> full-color components
              int rainIndexValue = (int) currPixelInfo.relativeLuminosity;
              if (rainIndexValue > rainPixelValue) rainIndexValue = rainPixelValue;

              Color rainColor = this.colorManager.getColor(rainIndexValue);
              startR = rainColor.getRed();
              startG = rainColor.getGreen();
              startB = rainColor.getBlue();

              coef = monochromeLetterToFullColorLetterCoef;
            } else {
              // pixel on background
              startR = 0;
              startG = 0;
              startB = 0;

              coef = blackBackgroundToFullColorBackgroundCoef;
            }

            int finalR = currPixelInfo.red;
            int finalG = currPixelInfo.green;
            int finalB = currPixelInfo.blue;

            int newR = (int) (startR + coef * (finalR - startR));
            int newG = (int) (startG + coef * (finalG - startG));
            int newB = (int) (startB + coef * (finalB - startB));

            // int newR = finalR;
            // int newG = finalG;
            // int newB = finalB;

            // System.out.println("str: " + bx + "," + by + " -> ("
            // + startR + "," + startG + "," + startB + ")");
            // System.out.println("fnl: " + bx + "," + by + " -> ("
            // + finalR + "," + finalG + "," + finalB + ")");
            // System.out.println("new: " + bx + "," + by + " -> ("
            // + newR + "," + newG + "," + newB + ")");

            if (newR > 255) newR = 255;
            if (newR < 0) newR = 0;
            if (newG > 255) newG = 255;
            if (newG < 0) newG = 0;
            if (newB > 255) newB = 255;
            if (newB < 0) newB = 0;

            int newColor = (255 << 24) | (newR << 16) | (newG << 8) | newB;
            // newColor = currPixelInfo.trueColor;
            bitmap[bx][by] = newColor;
          }
        }
      }
    }
  }

  public void iteration() {
    // long time0 = System.currentTimeMillis();
    switch (this.state) {
      case LetterRainManager.STATE_LETTERRAIN_FIRSTPHASE:
        this.iterationStep1();
        this.currIteration++;
        if (this.currIteration >= this.maxIterationFirstPhase) {
          System.out.println("LetterRain first step over");
          this.currTrueColorBitmap =
              new TrueColorBitmapObject(this.currIndexBitmap, this.colorManager);
          this.currIteration = 0;
          this.state = LetterRainManager.STATE_LETTERRAIN_SECONDPHASE;
        }
        break;
      case LetterRainManager.STATE_LETTERRAIN_SECONDPHASE:
        this.iterationStep2();
        this.currIteration++;
        if (this.currIteration >= this.maxIterationSecondPhase) {
          System.out.println("LetterRain second step over");
          this.currIteration = 0;
          this.state = LetterRainManager.STATE_LETTERRAIN_FINISHED;
        }
        break;
      case LetterRainManager.STATE_LETTERRAIN_FINISHED:
        return;
    }
    // long time1 = System.currentTimeMillis();
    // System.out.println("iteration: " + (time1-time0));
  }

  public IndexBitmapObject getCurrentIndexBitmap() {
    return this.currIndexBitmap;
  }

  public TrueColorBitmapObject getCurrentTrueColorBitmap() {
    return this.currTrueColorBitmap;
  }

  public boolean isFinished() {
    return (this.state == LetterRainManager.STATE_LETTERRAIN_FINISHED);
  }

  public boolean toGetIndexBitmap() {
    return (this.state == LetterRainManager.STATE_LETTERRAIN_FIRSTPHASE);
  }
}
