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
package org.pushingpixels.matrixrain.intro.rain;

import java.awt.*;

import org.pushingpixels.matrixrain.auxiliary.graphics.IndexBitmapObject;
import org.pushingpixels.matrixrain.auxiliary.graphics.TrueColorBitmapObject;
import org.pushingpixels.matrixrain.auxiliary.graphics.colors.manager.ColorManager;
import org.pushingpixels.matrixrain.auxiliary.graphics.geom.edgedetection.EdgeDetector;
import org.pushingpixels.matrixrain.auxiliary.graphics.spline.SplineInterpolatorObject;
import org.pushingpixels.matrixrain.auxiliary.graphics.spline.SplineManager;

public final class BellRainManager {
  private static final int N = 8;

  private static final int M = 6;

  private static final double CUTOFF_THRESHOLD = 0.95;

  private static final int STATE_BELLRAIN_FIRSTPHASE = 0;

  private static final int STATE_BELLRAIN_SECONDPHASE = 1;

  private static final int STATE_BELLRAIN_WAIT = 2;

  private static final int STATE_BELLRAIN_DISINTEGRATION = 3;

  private static final int STATE_BELLRAIN_FINISHED = 10;

  private int width, height;

  private int bellCellSize;

  private int bellClusterSize;

  private int widthInCells, heightInCells;
  private int widthInClusters, heightInClusters;

  private BellRainDropInfo[] randomDropArray;

  private BellRainPixelInfo[][] pixelInfoArray;

  private BellRainCellInfo[][] cellInfoArray;

  private BellRainClusterInfo[][] clusterInfoArray;

  private BellRainScreenInfo screenInfo;

  // rain start and delta - first step
  private int[] rain_ys1;

  private int[] rain_ys2;

  private int[][] rain_delta1;

  private int[][] rain_delta2;

  private int[] rain_curr1;

  private int[] rain_curr2;

  // for iterations
  private ColorManager colorManager;

  private int currIteration;

  private int maxIterationFirstPhase;

  private int maxIterationSecondPhase;

  private int maxIterationWait;

  private int maxIterationDisintegration;

  private int state;

  private TrueColorBitmapObject bitmapAfterFirstStep;

  private IndexBitmapObject currIndexBitmap;

  private TrueColorBitmapObject currTrueColorBitmap;

  // interpolators for first phase
  private SplineInterpolatorObject edgeFadeInterpolator;

  private SplineInterpolatorObject edgeFadeInfluenceInterpolator;

  private SplineInterpolatorObject colorIncreaseInterpolator;

  // interpolators for second phase
  private SplineInterpolatorObject monochromeToClusterAverageInterpolator;

  private SplineInterpolatorObject clusterAverageToFullColorInterpolator;

  private SplineInterpolatorObject clusterBorderInterpolator;

  private SplineInterpolatorObject monochromeFadeAroundEdgesInterpolator;

  private SplineInterpolatorObject monochromeFadeAroundEdgesInfluenceInterpolator;

  // interpolators for disintegration phase
  private SplineInterpolatorObject fullColorToClusterMonochromeAverageInterpolator;

  private SplineInterpolatorObject edgeSurvivalInterpolator;

  private SplineInterpolatorObject disintegrationInterpolator;

  // deltas for disintegration
  private int[] disintegrationDeltaX;

  private int[] disintegrationDeltaY;

  // monochrome coefficients
  private double monochromeCoefR;

  private double monochromeCoefG;

  private double monochromeCoefB;

  private final class BellRainDropInfo {
    public int col;

    public int dropRow;

    public int length;

    public int speed;

    public int dropAge;
  }

  private final class BellRainPixelInfo {
    public boolean isOnEdge;

    public int red;

    public int green;

    public int blue;

    public int trueColor;

    public double luminosity;

    public double relativeLuminosity;

    public boolean isExposedByRain;

    public double bellCoefficient;
  }

  private final class BellRainCellInfo {
    public boolean hasPixelOnEdge;

    public boolean inFirstWave;

    public int iterationsAfterLastEdgeInFirstWave;

    public int averageLuminosity;

    public double relativeAverageLuminosity;

    public boolean inRandomDrop;

    public int randomDropAge;
  }

  private final class BellRainClusterInfo {
    public int averageR;

    public int averageG;

    public int averageB;

    public int borderR;

    public int borderG;

    public int borderB;

    public int borderTrueColor;

    public int averageLuminosity;

    public boolean hasPixelOnEdge;

    public int distanceToClosestTopEdge;

    public int regularCenterX;

    public int regularCenterY;

    public int disintegrateCenterX;

    public int disintegrateCenterY;

    public double disintegrationKx;

    public double disintegrationKy;
  }

  private final class BellRainScreenInfo {
    public double averagePixelLuminosity;

    public double maxPixelLuminosity;

    public double averageCellLuminosity;

    public double maxCellLuminosity;
  }

  public BellRainManager(
      int width,
      int height,
      Image finalImage,
      int bellCellSize,
      int bellClusterSize,
      ColorManager colorManager) {

    this.width = width;
    this.height = height;
    this.bellCellSize = bellCellSize;
    this.bellClusterSize = bellClusterSize;
    this.colorManager = colorManager;

    // compute dimension in bell cells
    this.widthInCells = (int) (Math.ceil((double) width / (double) bellCellSize));
    this.heightInCells = (int) (Math.ceil((double) height / (double) bellCellSize));

    // compute dimension in bell clusters
    this.widthInClusters = (int) (Math.ceil((double) width / (double) bellClusterSize));
    this.heightInClusters = (int) (Math.ceil((double) height / (double) bellClusterSize));

    // compute edge image
    EdgeDetector edgeDetector = new EdgeDetector(width, height, finalImage);
    int[] edgeValues = edgeDetector.getValueMap(EdgeDetector.EDGES_SOFT);

    // compute bell coefficients
    Point[] bellPoints = new Point[3];
    bellPoints[0] = new Point(0, 80);
    bellPoints[1] = new Point((bellCellSize - 1) / 2, 60);
    bellPoints[2] = new Point(bellCellSize - 1, 50);

    // bellPoints[0] = new Point(0, 100);
    // bellPoints[1] = new Point((bellCellSize-1)/4, 90);
    // bellPoints[2] = new Point((bellCellSize-1)/2, 70);
    // bellPoints[3] = new Point(bellCellSize-1, 30);

    SplineInterpolatorObject bellInterpolator = SplineManager.getSplineInterpolation(bellPoints);
    // bellInterpolator.print();

    // compute luminosity enhance interpolator
    Point[] luminosityEnhancePoints = new Point[4];
    luminosityEnhancePoints[0] = new Point(0, 0);
    luminosityEnhancePoints[1] = new Point(20, 50);
    luminosityEnhancePoints[2] = new Point(120, 180);
    luminosityEnhancePoints[3] = new Point(255, 255);

    this.screenInfo = new BellRainScreenInfo();
    int[] luminosities = new int[256];
    for (int i = 0; i < luminosities.length; i++) luminosities[i] = 0;

    // get original pixels
    int[] origPixels = edgeDetector.getOriginalPixels();

    // long time1 = System.currentTimeMillis();

    // System.out.println("3: " + System.currentTimeMillis());
    double bellCellSize2 = (this.bellCellSize - 1) / 2.0;
    // create info structures on all pixels
    int pixelCount = 0;
    double pixelSum = 0.0;
    double maxPixelLum = 0.0;
    this.pixelInfoArray = new BellRainPixelInfo[width][height];
    for (int col = 0; col < this.width; col++) {
      int xInBellCell = col % this.bellCellSize;
      for (int row = 0; row < this.height; row++) {
        int index = row * this.width + col;
        BellRainPixelInfo currPixelInfo = new BellRainPixelInfo();
        currPixelInfo.isOnEdge = (edgeValues[index] > 0);

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
        pixelSum += currPixelInfo.luminosity;
        if (currPixelInfo.luminosity > maxPixelLum) maxPixelLum = currPixelInfo.luminosity;
        pixelCount++;

        currPixelInfo.isExposedByRain = false;

        int yInBellCell = row % this.bellCellSize;
        double dx = xInBellCell - bellCellSize2;
        double dy = yInBellCell - bellCellSize2;
        int distToBellCenter2 = (int) (dx * dx + dy * dy);

        double bellCoefficient = bellInterpolator.getValue(distToBellCenter2);
        // tweak randomly
        bellCoefficient += (int) (Math.random() * 20.0);
        if (bellCoefficient > 100) bellCoefficient = 100;
        currPixelInfo.bellCoefficient = bellCoefficient;

        this.pixelInfoArray[col][row] = currPixelInfo;
      }
    }
    this.screenInfo.averagePixelLuminosity = pixelSum / pixelCount;
    this.screenInfo.maxPixelLuminosity = maxPixelLum;

    // System.out.println("4: " + System.currentTimeMillis());

    // create info structures on all cells
    this.cellInfoArray = new BellRainCellInfo[this.widthInCells][this.heightInCells];
    for (int cellCol = 0; cellCol < this.widthInCells; cellCol++) {
      for (int cellRow = 0; cellRow < this.heightInCells; cellRow++) {
        BellRainCellInfo currCellInfo = new BellRainCellInfo();

        // go over all pixels of this cell
        int startX = cellCol * this.bellCellSize;
        int endX = (cellCol + 1) * this.bellCellSize;
        if (endX >= this.width) endX = this.width - 1;
        int startY = cellRow * this.bellCellSize;
        int endY = (cellRow + 1) * this.bellCellSize;
        if (endY >= this.height) endY = this.height - 1;
        int count = 0;
        int luminositySum = 0;
        for (int x = startX; x < endX; x++) {
          for (int y = startY; y < endY; y++) {
            if (this.pixelInfoArray[x][y].isOnEdge) currCellInfo.hasPixelOnEdge = true;
            luminositySum += this.pixelInfoArray[x][y].luminosity;
            count++;
          }
        }

        int avgLuminosity = (count > 0) ? (luminositySum / count) : 0;
        currCellInfo.averageLuminosity = avgLuminosity;
        luminosities[(int) avgLuminosity]++;

        currCellInfo.iterationsAfterLastEdgeInFirstWave = 100;
        currCellInfo.inFirstWave = true;

        this.cellInfoArray[cellCol][cellRow] = currCellInfo;
      }
    }

    int cutoffLuminosity = 255;
    int cutoffThreshold =
        (int) (this.widthInCells * this.heightInCells * BellRainManager.CUTOFF_THRESHOLD);
    int cutoffSum = 0;
    for (int i = 0; i < luminosities.length; i++) {
      cutoffSum += luminosities[i];
      if (cutoffSum > cutoffThreshold) {
        cutoffLuminosity = i;
        break;
      }
    }
    // System.out.println("cut lum: " + cutoffLuminosity);
    for (int col = 0; col < this.widthInCells; col++) {
      for (int row = 0; row < this.heightInCells; row++) {
        double currLum = this.cellInfoArray[col][row].averageLuminosity;
        if (currLum > cutoffLuminosity)
          this.cellInfoArray[col][row].relativeAverageLuminosity = 255.0;
        else
          this.cellInfoArray[col][row].relativeAverageLuminosity =
              255.0 * this.cellInfoArray[col][row].averageLuminosity / cutoffLuminosity;
      }
    }

    int cellCount = 0;
    double cellSum = 0.0;
    double maxCellLum = 0.0;
    for (int currCol = 0; currCol < this.widthInCells; currCol++) {
      for (int currRow = 0; currRow < this.heightInCells; currRow++) {
        double currLum = this.cellInfoArray[currCol][currRow].averageLuminosity;
        cellSum += currLum;
        if (currLum > maxCellLum) maxCellLum = currLum;
        cellCount++;
      }
    }
    this.screenInfo.averageCellLuminosity = cellSum / cellCount;
    this.screenInfo.maxCellLuminosity = maxCellLum;
    for (int col = 0; col < this.widthInCells; col++) {
      for (int row = 0; row < this.heightInCells; row++) {
        double currLum = this.cellInfoArray[col][row].averageLuminosity;
        if (currLum > cutoffLuminosity)
          this.cellInfoArray[col][row].relativeAverageLuminosity = 255.0;
        else
          this.cellInfoArray[col][row].relativeAverageLuminosity =
              255.0 * this.cellInfoArray[col][row].averageLuminosity / cutoffLuminosity;
      }
    }

    // create info structures on all clusters
    this.maxIterationDisintegration = 10;
    this.clusterInfoArray = new BellRainClusterInfo[this.widthInCells][this.heightInCells];
    for (int clusterCol = 0; clusterCol < this.widthInClusters; clusterCol++) {
      for (int clusterRow = 0; clusterRow < this.heightInClusters; clusterRow++) {
        BellRainClusterInfo currClusterInfo = new BellRainClusterInfo();

        // go over all pixels of this cluster
        int startX = clusterCol * this.bellClusterSize;
        int endX = (clusterCol + 1) * this.bellClusterSize;
        if (endX >= this.width) endX = this.width - 1;
        int startY = clusterRow * this.bellClusterSize;
        int endY = (clusterRow + 1) * this.bellClusterSize;
        if (endY >= this.height) endY = this.height - 1;
        int count = 0;
        int rSum = 0, gSum = 0, bSum = 0;
        int luminositySum = 0;
        for (int x = startX; x < endX; x++) {
          for (int y = startY; y < endY; y++) {
            if (this.pixelInfoArray[x][y].isOnEdge) {
              currClusterInfo.hasPixelOnEdge = true;
              currClusterInfo.distanceToClosestTopEdge = 0;
            } else {
              currClusterInfo.distanceToClosestTopEdge = 100;
            }
            rSum += this.pixelInfoArray[x][y].red;
            gSum += this.pixelInfoArray[x][y].green;
            bSum += this.pixelInfoArray[x][y].blue;
            luminositySum += this.pixelInfoArray[x][y].luminosity;
            count++;
          }
        }

        int avgR = (count > 0) ? (rSum / count) : 0;
        int avgG = (count > 0) ? (gSum / count) : 0;
        int avgB = (count > 0) ? (bSum / count) : 0;
        int avgLuminosity = (count > 0) ? (luminositySum / count) : 0;
        currClusterInfo.averageR = avgR;
        currClusterInfo.averageG = avgG;
        currClusterInfo.averageB = avgB;
        currClusterInfo.averageLuminosity = avgLuminosity;

        // border - twice darker
        currClusterInfo.borderR = currClusterInfo.averageR / 2;
        currClusterInfo.borderG = currClusterInfo.averageG / 2;
        currClusterInfo.borderB = currClusterInfo.averageB / 2;

        currClusterInfo.borderTrueColor =
            (255 << 24)
                | (currClusterInfo.borderR << 16)
                | (currClusterInfo.borderG << 8)
                | currClusterInfo.borderB;

        // for disintegration
        currClusterInfo.disintegrationKx =
            (double) (this.width + 2 * clusterCol * this.bellClusterSize)
                / (double) (this.maxIterationDisintegration * this.maxIterationDisintegration);
        // System.out.println("col: " + clusterCol + ", kx: " +
        // currClusterInfo.disintegrationKx);
        currClusterInfo.disintegrationKy =
            (double) (this.height / 2)
                / (double) (this.maxIterationDisintegration * this.maxIterationDisintegration);
        // if (Math.random() < 0.5)
        // currClusterInfo.disintegrationKx *= -1.0;
        currClusterInfo.disintegrateCenterX =
            clusterCol * this.bellClusterSize
                + this.bellClusterSize / 2
                - 50
                - clusterCol * this.bellClusterSize / 10;
        currClusterInfo.disintegrateCenterY =
            clusterRow * this.bellClusterSize
                + this.bellClusterSize / 2
                - 50
                - clusterRow * this.bellClusterSize / 10;

        this.clusterInfoArray[clusterCol][clusterRow] = currClusterInfo;
      }
    }
    // propagate distanceToClosestTopEdge field
    for (int clusterCol = 0; clusterCol < this.widthInClusters; clusterCol++) {
      for (int clusterRow = 0; clusterRow < this.heightInClusters; clusterRow++) {
        BellRainClusterInfo currClusterInfo = this.clusterInfoArray[clusterCol][clusterRow];
        if (currClusterInfo.distanceToClosestTopEdge > 0) continue;

        for (int currClusterCol = 0; currClusterCol < this.widthInClusters; currClusterCol++) {
          int dx = currClusterCol - clusterCol;
          if (dx < 0) dx = -dx;
          for (int currClusterRow = 0; currClusterRow < this.heightInClusters; currClusterRow++) {
            int dy = currClusterRow - clusterRow;
            if (dy < 0) dy = -dy;
            int currDist = dx + dy;
            if (currDist
                < this.clusterInfoArray[currClusterCol][currClusterRow].distanceToClosestTopEdge)
              this.clusterInfoArray[currClusterCol][currClusterRow].distanceToClosestTopEdge =
                  currDist;
          }
        }
      }
    }
    // System.out.println("Propagate - " + (time21-time20));
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
    this.rain_ys1 = new int[this.widthInCells];
    this.rain_curr1 = new int[this.widthInCells];
    this.rain_ys2 = new int[this.widthInCells];
    this.rain_curr2 = new int[this.widthInCells];
    for (int x = 0; x < this.widthInCells; x++) {
      double currYs2 = 0.0;
      for (int i = 1; i < N; i++) {
        double k = 2 * Math.PI / (N * this.widthInCells);
        currYs2 +=
            ((double) (N + Math.random()) / (double) i)
                * Math.sin(k * i * x + 2 * Math.PI * Math.random());
      }
      this.rain_ys2[x] = (int) currYs2;
    }
    int maxYs2 = this.rain_ys2[0];
    for (int i = 1; i < this.widthInCells; i++)
      if (this.rain_ys2[i] > maxYs2) maxYs2 = this.rain_ys2[i];

    for (int i = 0; i < this.widthInCells; i++) {
      this.rain_ys2[i] -= maxYs2;
      this.rain_ys1[i] = this.rain_ys2[i] / 2;
      this.rain_ys2[i]--;
      this.rain_curr1[i] = this.rain_ys1[i];
      this.rain_curr2[i] = this.rain_ys2[i];
      // System.out.print(this.rain_ys[i] + ",");
    }

    // compute rain deltas
    this.maxIterationFirstPhase = maxYs2 + this.heightInCells;
    this.rain_delta1 = new int[this.widthInCells][this.maxIterationFirstPhase];
    this.rain_delta2 = new int[this.widthInCells][this.maxIterationFirstPhase];
    for (int x = 0; x < this.widthInCells; x++) {
      for (int t = 0; t < this.maxIterationFirstPhase; t++) {
        double currDelta2 = 0.0;
        double phase = 2.0 * Math.PI * Math.random();
        for (int i = 1; i < M; i++) currDelta2 += Math.sin(i * x * (t + 1) + phase);
        if (currDelta2 < 0.0) currDelta2 = -currDelta2;
        currDelta2 += 5.0;
        this.rain_delta2[x][t] = (int) currDelta2;
        this.rain_delta1[x][t] = this.rain_delta2[x][t] + (int) (Math.random() * 3.0);
      }
    }

    // compute the end of the first step
    int maxTf = 0;
    for (int i = 0; i < this.widthInCells; i++) {
      int currSum = this.rain_ys2[i];
      int currT = 0;
      while (currT < this.maxIterationFirstPhase) {
        currSum += this.rain_delta2[i][currT];
        if (currSum >= this.heightInCells) {
          if (currT > maxTf) maxTf = currT;
          break;
        }
        currT++;
      }
    }
    this.maxIterationFirstPhase = maxTf + 1;

    /*
     * for (int x=0; x<this.widthInCells; x++) { for (int t=0; t<maxDeltaCount;
     * t++) { System.out.print(this.rain_delta[x][t] + ","); }
     * System.out.println(); }
     */
    // long time2 = System.currentTimeMillis();
    // System.out.println("Ys: " + (time1-time0));
    // System.out.println("D: " + (time2-time1));
    // random drops
    this.randomDropArray = new BellRainDropInfo[20];
    for (int i = 0; i < this.randomDropArray.length; i++) {
      this.randomDropArray[i] = new BellRainDropInfo();
      this.randomizeDrop(this.randomDropArray[i], this.widthInCells, this.heightInCells);
    }

    // interpolators
    Point[] edgeFadePoints = new Point[4];
    edgeFadePoints[0] = new Point(0, 100);
    edgeFadePoints[1] = new Point(4, 50);
    edgeFadePoints[2] = new Point(7, 20);
    edgeFadePoints[3] = new Point(10, 0);

    this.edgeFadeInterpolator = SplineManager.getSplineInterpolation(edgeFadePoints);
    // this.edgeFadeInterpolator.print();

    Point[] edgeFadeInfluencePoints = new Point[3];
    edgeFadeInfluencePoints[0] = new Point(0, 100);
    edgeFadeInfluencePoints[1] = new Point(maxIterationFirstPhase / 2, 60);
    edgeFadeInfluencePoints[2] = new Point(maxIterationFirstPhase, 0);

    this.edgeFadeInfluenceInterpolator =
        SplineManager.getSplineInterpolation(edgeFadeInfluencePoints);

    Point[] colorIncreasePoints = new Point[3];
    colorIncreasePoints[0] = new Point(0, 70);
    colorIncreasePoints[1] = new Point(maxIterationFirstPhase / 3, 90);
    colorIncreasePoints[2] = new Point(maxIterationFirstPhase, 100);

    this.colorIncreaseInterpolator = SplineManager.getSplineInterpolation(colorIncreasePoints);

    // second step
    this.maxIterationSecondPhase = 20;
    Point[] clusterBorderPoints = new Point[7];
    clusterBorderPoints[0] = new Point(0, 0);
    clusterBorderPoints[1] = new Point(this.maxIterationSecondPhase / 6, 50);
    clusterBorderPoints[2] = new Point(this.maxIterationSecondPhase / 3, 80);
    clusterBorderPoints[3] = new Point(this.maxIterationSecondPhase / 2, 70);
    clusterBorderPoints[4] = new Point(2 * this.maxIterationSecondPhase / 3, 40);
    clusterBorderPoints[5] = new Point(3 * this.maxIterationSecondPhase / 4, 0);
    clusterBorderPoints[6] = new Point(this.maxIterationSecondPhase, 0);

    this.clusterBorderInterpolator = SplineManager.getSplineInterpolation(clusterBorderPoints);

    Point[] monochromeToClusterAveragePoints = new Point[4];
    monochromeToClusterAveragePoints[0] = new Point(0, 0);
    monochromeToClusterAveragePoints[1] = new Point(this.maxIterationSecondPhase / 4, 50);
    monochromeToClusterAveragePoints[2] = new Point(this.maxIterationSecondPhase / 2, 100);
    monochromeToClusterAveragePoints[3] = new Point(this.maxIterationSecondPhase, 100);

    this.monochromeToClusterAverageInterpolator =
        SplineManager.getSplineInterpolation(monochromeToClusterAveragePoints);

    Point[] clusterAverageToFullColorPoints = new Point[4];
    clusterAverageToFullColorPoints[0] = new Point(0, 0);
    clusterAverageToFullColorPoints[1] = new Point(this.maxIterationSecondPhase / 2, 0);
    clusterAverageToFullColorPoints[2] = new Point(3 * this.maxIterationSecondPhase / 4, 50);
    clusterAverageToFullColorPoints[3] = new Point(this.maxIterationSecondPhase, 100);

    this.clusterAverageToFullColorInterpolator =
        SplineManager.getSplineInterpolation(clusterAverageToFullColorPoints);

    Point[] monochromeFadeAroundEdgesPoints = new Point[5];
    monochromeFadeAroundEdgesPoints[0] = new Point(0, 100);
    monochromeFadeAroundEdgesPoints[1] = new Point(this.maxIterationSecondPhase / 5, 90);
    monochromeFadeAroundEdgesPoints[2] = new Point(2 * this.maxIterationSecondPhase / 5, 40);
    monochromeFadeAroundEdgesPoints[3] = new Point(this.maxIterationSecondPhase / 2, 0);
    monochromeFadeAroundEdgesPoints[4] = new Point(this.maxIterationSecondPhase, 0);

    this.monochromeFadeAroundEdgesInterpolator =
        SplineManager.getSplineInterpolation(monochromeFadeAroundEdgesPoints);

    Point[] monochromeFadeAroundEdgesInfluencePoints = new Point[6];
    monochromeFadeAroundEdgesInfluencePoints[0] = new Point(0, 100);
    monochromeFadeAroundEdgesInfluencePoints[1] = new Point(1, 90);
    monochromeFadeAroundEdgesInfluencePoints[2] = new Point(3, 50);
    monochromeFadeAroundEdgesInfluencePoints[3] = new Point(6, 40);
    monochromeFadeAroundEdgesInfluencePoints[4] = new Point(8, 20);
    monochromeFadeAroundEdgesInfluencePoints[5] = new Point(10, 0);

    this.monochromeFadeAroundEdgesInfluenceInterpolator =
        SplineManager.getSplineInterpolation(monochromeFadeAroundEdgesInfluencePoints);

    this.bitmapAfterFirstStep = null;

    this.maxIterationWait = 10;

    // disintegration
    this.disintegrationDeltaX = new int[this.maxIterationDisintegration + 1];
    this.disintegrationDeltaY = new int[this.maxIterationDisintegration + 1];
    for (int i = 0; i <= this.maxIterationDisintegration; i++) {
      double angle = 2.0 * Math.PI * (double) i / (double) this.maxIterationDisintegration;
      double radius = 50 + i * 450 / this.maxIterationDisintegration;
      this.disintegrationDeltaX[i] = (int) (radius * Math.cos(angle));
      this.disintegrationDeltaY[i] = (int) (radius * Math.sin(angle));
    }

    Point[] fullColorToClusterMonochromeAveragePoints = new Point[5];
    fullColorToClusterMonochromeAveragePoints[0] = new Point(0, 0);
    fullColorToClusterMonochromeAveragePoints[1] = new Point(1, 60);
    fullColorToClusterMonochromeAveragePoints[2] =
        new Point(this.maxIterationDisintegration / 4, 80);
    fullColorToClusterMonochromeAveragePoints[3] =
        new Point(this.maxIterationDisintegration / 2, 100);
    fullColorToClusterMonochromeAveragePoints[4] = new Point(this.maxIterationDisintegration, 100);

    this.fullColorToClusterMonochromeAverageInterpolator =
        SplineManager.getSplineInterpolation(fullColorToClusterMonochromeAveragePoints);

    Point[] edgeSurvivalPoints = new Point[6];
    edgeSurvivalPoints[0] = new Point(0, 100);
    edgeSurvivalPoints[1] = new Point(this.maxIterationDisintegration / 2, 90);
    edgeSurvivalPoints[2] = new Point(this.maxIterationDisintegration, 80);
    edgeSurvivalPoints[3] = new Point(2 * this.maxIterationDisintegration, 50);
    edgeSurvivalPoints[4] = new Point(3 * this.maxIterationDisintegration, 30);
    edgeSurvivalPoints[5] = new Point(4 * this.maxIterationDisintegration, 0);

    this.edgeSurvivalInterpolator = SplineManager.getSplineInterpolation(edgeSurvivalPoints);

    Point[] disintegrationPoints = new Point[5];
    disintegrationPoints[0] = new Point(0, 100);
    disintegrationPoints[1] = new Point(this.maxIterationDisintegration / 4, 80);
    disintegrationPoints[2] = new Point(this.maxIterationDisintegration / 2, 50);
    disintegrationPoints[3] = new Point(3 * this.maxIterationDisintegration / 4, 20);
    disintegrationPoints[4] = new Point(this.maxIterationDisintegration, 0);

    this.disintegrationInterpolator = SplineManager.getSplineInterpolation(disintegrationPoints);

    // monochrome coefficients
    Color monochromeMasterColor = this.colorManager.getMasterColor();
    this.monochromeCoefR = (double) (monochromeMasterColor.getRed()) / 255.0;
    this.monochromeCoefG = (double) (monochromeMasterColor.getGreen()) / 255.0;
    this.monochromeCoefB = (double) (monochromeMasterColor.getBlue()) / 255.0;

    this.state = BellRainManager.STATE_BELLRAIN_FIRSTPHASE;
    // System.out.println("Bell rain creation: " + (time3-time0));
  }

  private void randomizeDrop(BellRainDropInfo drop, int maxCols, int maxRows) {
    drop.col = (int) (Math.random() * maxCols);
    drop.dropRow = (int) (Math.random() * maxRows);
    drop.dropAge = 0;
    drop.length = 2 + (int) (Math.random() * 5.0);
    drop.speed = 2 + (int) (Math.random() * 4.0);
  }

  private void setIterationsAfterLastEdge(int cellCol, int cellRow, int newValue) {
    if ((cellRow < 0) || (cellRow >= this.heightInCells)) return;
    this.cellInfoArray[cellCol][cellRow].iterationsAfterLastEdgeInFirstWave = newValue;
  }

  private void incrementIterationsAfterLastEdge(int cellCol, int cellRow, int delta) {
    if ((cellRow < 0) || (cellRow >= this.heightInCells)) return;
    this.cellInfoArray[cellCol][cellRow].iterationsAfterLastEdgeInFirstWave += delta;
  }

  private void iterationStep1() {
    // go over all cell columns and advance the two waves for this column
    // - set boolean "isExposedByRain" to true flag of all pixels covered
    // by these cells and "inFirstWave" to false for the cells covered
    // by the second wave
    for (int cellCol = 0; cellCol < this.widthInCells; cellCol++) {
      int rainDelta1 = this.rain_delta1[cellCol][this.currIteration];
      int oldCellRow1 = this.rain_curr1[cellCol];
      int newCellRow1 = oldCellRow1 + rainDelta1;

      int rainDelta2 = this.rain_delta2[cellCol][this.currIteration];
      int oldCellRow2 = this.rain_curr2[cellCol];
      int newCellRow2 = oldCellRow2 + rainDelta2;

      // if drop is still outside - skip
      if (newCellRow1 >= 0) {
        int startVisible1 = (oldCellRow1 < 0) ? 0 : oldCellRow1;
        int endVisible1 = newCellRow1;
        for (int currVisibleCellRow1 = startVisible1;
            currVisibleCellRow1 <= endVisible1;
            currVisibleCellRow1++) {

          // go over all pixels of this cell
          int startX = cellCol * this.bellCellSize;
          int endX = (cellCol + 1) * this.bellCellSize;
          if (endX >= this.width) endX = this.width - 1;
          int startY = currVisibleCellRow1 * this.bellCellSize;
          int endY = (currVisibleCellRow1 + 1) * this.bellCellSize;
          if (endY >= this.height) endY = this.height - 1;
          for (int x = startX; x < endX; x++) {
            for (int y = startY; y < endY; y++) {
              this.pixelInfoArray[x][y].isExposedByRain = true;
            }
          }
        }
      }
      if (newCellRow2 >= 0) {
        int startVisible2 = (oldCellRow2 < 0) ? 0 : oldCellRow2;
        int endVisible2 =
            (newCellRow2 >= this.heightInCells) ? (this.heightInCells - 1) : newCellRow2;
        for (int currVisibleCellRow2 = startVisible2;
            currVisibleCellRow2 <= endVisible2;
            currVisibleCellRow2++) {
          this.cellInfoArray[cellCol][currVisibleCellRow2].inFirstWave = false;
        }
      }
      this.rain_curr1[cellCol] += this.rain_delta1[cellCol][this.currIteration];
      this.rain_curr2[cellCol] += this.rain_delta2[cellCol][this.currIteration];

      if (newCellRow1 < 0) continue;

      // if edge detected between oldCellRow1 and newCellRow1 - all the
      // cells
      // between firstEdgeRow and newCellRow1 0
      int firstEdgeRow = 0;
      if (oldCellRow1 < this.heightInCells) {
        // check oldCelRow1-newCellRow1 range for cell that has pixels
        // on edge
        boolean hasEdge = false;
        for (int currCellRow1 = oldCellRow1 + 1; currCellRow1 <= newCellRow1; currCellRow1++) {
          if (currCellRow1 < 0) continue;
          if (currCellRow1 >= this.heightInCells) break;

          if (this.cellInfoArray[cellCol][currCellRow1].hasPixelOnEdge) {
            if (!hasEdge) firstEdgeRow = currCellRow1;
            hasEdge = true;
            break;
          }
        }

        if (hasEdge) {
          // all in firstEdgeRow..newCellRow1 get 0
          for (int currCellRow1 = firstEdgeRow; currCellRow1 <= newCellRow1; currCellRow1++)
            this.setIterationsAfterLastEdge(cellCol, currCellRow1, 0);

          // all in 0..firstEdgeRow-1 get +=2
          for (int currCellRow1 = 0; currCellRow1 < firstEdgeRow; currCellRow1++)
            this.incrementIterationsAfterLastEdge(cellCol, currCellRow1, 2);
        } else {
          if (oldCellRow1 < 0) {
            // if rainDelta crossed row 0 - all the rows are as were
          } else {
            // all in oldCellRow1+1..newCellRow1 - get
            // oldCellRow1.value+1
            int oldValue =
                this.cellInfoArray[cellCol][oldCellRow1].iterationsAfterLastEdgeInFirstWave;
            for (int currCellRow1 = oldCellRow1 + 1; currCellRow1 <= newCellRow1; currCellRow1++)
              this.setIterationsAfterLastEdge(cellCol, currCellRow1, oldValue + 1);

            for (int currCellRow1 = 0; currCellRow1 <= oldCellRow1; currCellRow1++)
              this.incrementIterationsAfterLastEdge(cellCol, currCellRow1, 2);
          }
        }
      }
      // for (int i=0; i<this.heightInCells; i++) {
      // System.out.print(this.cellInfoArray[cellCol][i].iterationsAfterLastEdgeInFirstWave
      // + ",");
      // }
      // System.out.println();
    }

    this.currIndexBitmap.reset(0);
    int[][] bitmap = this.currIndexBitmap.getBitmap();
    for (int x = 0; x < this.width; x++) {
      int cellX = x / this.bellCellSize;
      for (int y = 0; y < this.height; y++) {
        int cellY = y / this.bellCellSize;
        BellRainPixelInfo currPixelInfo = this.pixelInfoArray[x][y];
        BellRainCellInfo currCellInfo = this.cellInfoArray[cellX][cellY];

        double currPixelValue = 0.0;
        if (!currPixelInfo.isExposedByRain) continue;

        currPixelValue = currPixelInfo.bellCoefficient / 100.0;

        // apply edge fade only for cells in first wave
        if (currCellInfo.inFirstWave) {
          double edgeFadeCoef =
              this.edgeFadeInterpolator.getValue(currCellInfo.iterationsAfterLastEdgeInFirstWave)
                  / 100.0;
          // System.out.println("applying edge fade to: " + cellX + ",
          // " + cellY);
          // System.out.println("itrs: " +
          // currCellInfo.iterationsAfterLastEdgeInFirstWave);
          // apply edge fade influence
          double edgeFadeInfluenceCoef =
              this.edgeFadeInfluenceInterpolator.getValue(this.currIteration) / 100.0;
          // 1.0 -> edgeFadeCoef
          // 0.0 -> 1.0
          edgeFadeCoef = 1.0 + edgeFadeInfluenceCoef * (edgeFadeCoef - 1.0);

          currPixelValue *= edgeFadeCoef;
        }

        bitmap[x][y] = (short) (255.0 * currPixelValue);
      }
    }

    for (int cellX = 0; cellX < this.widthInCells; cellX++) {
      for (int cellY = 0; cellY < this.heightInCells; cellY++) {
        BellRainCellInfo currCellInfo = this.cellInfoArray[cellX][cellY];
        /*
         * if (currCellInfo.hasPixelOnEdge) { // go over all pixels of
         * this cell int startX = cellX*this.bellCellSize; int endX =
         * (cellX+1)*this.bellCellSize; if (endX >= this.width) endX =
         * this.width-1; int startY = cellY*this.bellCellSize; int endY =
         * (cellY+1)*this.bellCellSize; if (endY >= this.height) endY =
         * this.height-1; for (int x=startX; x<endX; x++) { for (int
         * y=startY; y<endY; y++) { bitmap[x][y] = 255; } } }
         */
        // apply color and luminosity factors only for second wave
        if (currCellInfo.inFirstWave) continue;

        // apply color increase
        double colorIncreaseFactor =
            this.colorIncreaseInterpolator.getValue(this.currIteration) / 100.0;
        // 0.0 -> 0
        // 1.0 -> luminosity of pixel
        double colorIntensityFactor =
            colorIncreaseFactor * currCellInfo.relativeAverageLuminosity / 255.0;

        // go over all pixels of this cell
        int startX = cellX * this.bellCellSize;
        int endX = (cellX + 1) * this.bellCellSize;
        if (endX >= this.width) endX = this.width - 1;
        int startY = cellY * this.bellCellSize;
        int endY = (cellY + 1) * this.bellCellSize;
        if (endY >= this.height) endY = this.height - 1;
        for (int x = startX; x < endX; x++) {
          for (int y = startY; y < endY; y++) {
            int oldPixelValue = bitmap[x][y];
            bitmap[x][y] = (short) (oldPixelValue * colorIntensityFactor);
          }
        }
      }
    }

    // overlay random drops
    for (int cellX = 0; cellX < this.widthInCells; cellX++)
      for (int cellY = 0; cellY < this.heightInCells; cellY++)
        this.cellInfoArray[cellX][cellY].inRandomDrop = false;

    for (int currDropIndex = 0; currDropIndex < this.randomDropArray.length; currDropIndex++) {
      BellRainDropInfo currDrop = this.randomDropArray[currDropIndex];
      int startRow = currDrop.dropRow;
      int endRow = startRow - currDrop.length;
      if (startRow >= this.heightInCells) startRow = this.heightInCells - 1;
      if (endRow < 0) endRow = 0;
      int currAge = currDrop.dropAge;
      for (int currRow = startRow; currRow >= endRow; currRow--) {
        this.cellInfoArray[currDrop.col][currRow].inRandomDrop = true;
        this.cellInfoArray[currDrop.col][currRow].randomDropAge = currAge;
        if (Math.random() < 0.5) currAge++;
      }
    }

    for (int cellX = 0; cellX < this.widthInCells; cellX++) {
      for (int cellY = 0; cellY < this.heightInCells; cellY++) {
        BellRainCellInfo currCellInfo = this.cellInfoArray[cellX][cellY];
        if (!currCellInfo.inRandomDrop) continue;

        // go over all pixels of this cell
        int startX = cellX * this.bellCellSize;
        int endX = (cellX + 1) * this.bellCellSize;
        if (endX >= this.width) endX = this.width - 1;
        int startY = cellY * this.bellCellSize;
        int endY = (cellY + 1) * this.bellCellSize;
        if (endY >= this.height) endY = this.height - 1;
        for (int x = startX; x < endX; x++) {
          for (int y = startY; y < endY; y++) {
            double bellValue = this.pixelInfoArray[x][y].bellCoefficient / 100.0;

            double edgeFadeCoef =
                this.edgeFadeInterpolator.getValue(currCellInfo.randomDropAge) / 100.0;

            bellValue *= edgeFadeCoef;
            short newValue = (short) (255.0 * bellValue);
            if (bitmap[x][y] < newValue) bitmap[x][y] = newValue;
          }
        }
      }
    }

    // advance random drops
    for (int currDropIndex = 0; currDropIndex < this.randomDropArray.length; currDropIndex++) {
      BellRainDropInfo currDrop = this.randomDropArray[currDropIndex];
      if (Math.random() < 0.5) currDrop.dropAge++;
      currDrop.dropRow += currDrop.speed;
      // create new drop if this drop is too weak or is outside the screen
      if ((currDrop.dropAge > 6)
          || ((currDrop.dropRow - currDrop.length) >= this.heightInCells)
          || (Math.random() < 0.05))
        this.randomizeDrop(currDrop, this.heightInCells, this.widthInCells);
    }
  }

  private void iterationStep2() {
    this.currTrueColorBitmap.reset(0);
    int[][] firstStepFinalMonochromeTrueColorBitmap = this.bitmapAfterFirstStep.getBitmap();
    int[][] bitmap = this.currTrueColorBitmap.getBitmap();

    double monochromeToClusterAverageCoef =
        this.monochromeToClusterAverageInterpolator.getValue(this.currIteration) / 100.0;
    double clusterAverageToFullColorCoef =
        this.clusterAverageToFullColorInterpolator.getValue(this.currIteration) / 100.0;
    double monochromeFadeAroundEdgesCoef =
        this.monochromeFadeAroundEdgesInterpolator.getValue(this.currIteration) / 100.0;
    double clusterBorderCoef = this.clusterBorderInterpolator.getValue(this.currIteration) / 100.0;

    for (int x = 0; x < this.width; x++) {
      int clusterX = x / this.bellClusterSize;
      for (int y = 0; y < this.height; y++) {
        int clusterY = y / this.bellClusterSize;
        BellRainPixelInfo currPixelInfo = this.pixelInfoArray[x][y];
        BellRainClusterInfo currClusterInfo = this.clusterInfoArray[clusterX][clusterY];

        // apply 'monochrome'->'cluster average' and 'cluster
        // average'->'full color'
        // and adjust according to 'monochrome fade around edges'
        double monochromeFadeAroundEdgesInfluenceCoef =
            this.monochromeFadeAroundEdgesInfluenceInterpolator.getValue(
                    currClusterInfo.distanceToClosestTopEdge)
                / 100.0;

        int startR, startG, startB;
        int finalR, finalG, finalB;
        double coef;

        if (clusterAverageToFullColorCoef > 0.0) {
          startR = currClusterInfo.averageR;
          startG = currClusterInfo.averageG;
          startB = currClusterInfo.averageB;
          finalR = currPixelInfo.red;
          finalG = currPixelInfo.green;
          finalB = currPixelInfo.blue;
          coef = clusterAverageToFullColorCoef;
        } else {
          startR = (firstStepFinalMonochromeTrueColorBitmap[x][y] & 0x00FF0000) >> 16;
          startG = (firstStepFinalMonochromeTrueColorBitmap[x][y] & 0x0000FF00) >> 8;
          startB = (firstStepFinalMonochromeTrueColorBitmap[x][y] & 0x000000FF);

          finalR = currClusterInfo.averageR;
          finalG = currClusterInfo.averageG;
          finalB = currClusterInfo.averageB;

          // monochromeFadeAroundEdgesCoef*monochromeFadeAroundEdgesInfluenceCoef
          // 0.0 -> the color stays the same
          // 1.0 -> currClusterInfo.averageLuminosity in monochrome
          // component
          double monochromeFadeCoef =
              monochromeFadeAroundEdgesCoef * monochromeFadeAroundEdgesInfluenceCoef;
          int luminR = (int) (this.monochromeCoefR * currClusterInfo.averageLuminosity);
          int luminG = (int) (this.monochromeCoefG * currClusterInfo.averageLuminosity);
          int luminB = (int) (this.monochromeCoefB * currClusterInfo.averageLuminosity);
          finalR = (int) (finalR + monochromeFadeCoef * (luminR - finalR));
          finalG = (int) (finalG + monochromeFadeCoef * (luminG - finalG));
          finalB = (int) (finalB + monochromeFadeCoef * (luminB - finalB));

          coef = monochromeToClusterAverageCoef;
        }

        int newR = (int) (startR + coef * (finalR - startR));
        int newG = (int) (startG + coef * (finalG - startG));
        int newB = (int) (startB + coef * (finalB - startB));

        // apply border (for left and top borders)
        boolean isOnClusterBorderTL =
            ((x % this.bellClusterSize) == 0) || ((y % this.bellClusterSize) == 0);
        boolean isOnClusterBorderBR =
            (((x - 1) % this.bellClusterSize) == 0) || (((y - 1) % this.bellClusterSize) == 0);
        if (isOnClusterBorderTL && !isOnClusterBorderBR) {
          int borderR = currClusterInfo.borderR;
          int borderG = currClusterInfo.borderG;
          int borderB = currClusterInfo.borderB;
          newR = (int) (newR + clusterBorderCoef * (borderR - newR));
          newG = (int) (newG + clusterBorderCoef * (borderG - newG));
          newB = (int) (newB + clusterBorderCoef * (borderB - newB));
        }

        if (newR > 255) newR = 255;
        if (newR < 0) newR = 0;
        if (newG > 255) newG = 255;
        if (newG < 0) newG = 0;
        if (newB > 255) newB = 255;
        if (newB < 0) newB = 0;

        int newColor = (255 << 24) | (newR << 16) | (newG << 8) | newB;

        // System.out.println("pix: " + x + "," + y + " -> (" + newR +
        // "," + newG + "," + newB + ")");

        bitmap[x][y] = newColor;
      }
    }
  }

  /*
   * private void iterationStepDisintegration() {
   * this.currTrueColorBitmap.reset(0); int[][] bitmap =
   * this.currTrueColorBitmap.getBitmap();
   *
   * for (int clusterCol=0; clusterCol<this.widthInClusters; clusterCol++) {
   * for (int clusterRow=0; clusterRow<this.heightInClusters; clusterRow++) {
   * BellRainClusterInfo currClusterInfo =
   * this.clusterInfoArray[clusterCol][clusterRow]; int currCenterX =
   * currClusterInfo.disintegrateCenterX +
   * this.disintegrationDeltaX[this.currIteration]; int currCenterY =
   * currClusterInfo.disintegrateCenterY +
   * this.disintegrationDeltaY[this.currIteration];
   *  // go over all pixels of this cluster int startX =
   * clusterCol*this.bellClusterSize; int endX =
   * (clusterCol+1)*this.bellClusterSize; if (endX >= this.width) endX =
   * this.width-1; int startY = clusterRow*this.bellClusterSize; int endY =
   * (clusterRow+1)*this.bellClusterSize; if (endY >= this.height) endY =
   * this.height-1;
   *
   * int dx =
   * (int)(currClusterInfo.disintegrationKx*(currIteration+1)*(currIteration+1) +
   * 3.0*Math.random()*(currIteration+1)); int dy =
   * (int)(//currClusterInfo.disintegrationKy*(currIteration+1)*(currIteration+1) +
   * 2.0*Math.random()*(currIteration+1)); for (int x=startX; x<endX; x++) {
   * for (int y=startY; y<endY; y++) { int compTrueColor =
   * this.pixelInfoArray[x][y].trueColor;
   *
   * boolean isOnClusterBorder = ((x%this.bellClusterSize) == 0) ||
   * ((y%this.bellClusterSize) == 0); if (isOnClusterBorder) { compTrueColor =
   * currClusterInfo.borderTrueColor; }
   *  // int newX = x - currClusterInfo.regularCenterX + currCenterX; // int
   * newY = y - currClusterInfo.regularCenterX + currCenterY; int newX = x +
   * dx; int newY = y + dy;
   *
   * if ((newX >= 0) && (newX < this.width) && (newY >= 0) && (newY <
   * this.height)) bitmap[newX][newY] = compTrueColor; } } } } }
   */

  private void iterationStepDisintegration() {
    this.currTrueColorBitmap.reset(0);
    int[][] bitmap = this.currTrueColorBitmap.getBitmap();

    double fullColorToClusterMonochromeAverageCoef =
        this.fullColorToClusterMonochromeAverageInterpolator.getValue(this.currIteration) / 100.0;
    double disintegrationCoef =
        this.disintegrationInterpolator.getValue(this.currIteration) / 100.0;

    for (int x = 0; x < this.width; x++) {
      int clusterX = x / this.bellClusterSize;
      for (int y = 0; y < this.height; y++) {
        int clusterY = y / this.bellClusterSize;
        BellRainPixelInfo currPixelInfo = this.pixelInfoArray[x][y];
        BellRainClusterInfo currClusterInfo = this.clusterInfoArray[clusterX][clusterY];

        // apply 'full color'->'monochrome average'
        int startR = currPixelInfo.red;
        int startG = currPixelInfo.green;
        int startB = currPixelInfo.blue;
        int finalR = (int) (this.monochromeCoefR * currClusterInfo.averageLuminosity);
        int finalG = (int) (this.monochromeCoefG * currClusterInfo.averageLuminosity);
        int finalB = (int) (this.monochromeCoefB * currClusterInfo.averageLuminosity);

        int newR = (int) (startR + fullColorToClusterMonochromeAverageCoef * (finalR - startR));
        int newG = (int) (startG + fullColorToClusterMonochromeAverageCoef * (finalG - startG));
        int newB = (int) (startB + fullColorToClusterMonochromeAverageCoef * (finalB - startB));

        // apply 'edge survival'
        double edgeSurvivalCoef =
            this.edgeSurvivalInterpolator.getValue(
                    currClusterInfo.distanceToClosestTopEdge * currIteration)
                / 100.0;
        // 1.0 -> color stays
        // 0.0 -> black
        newR = (int) (edgeSurvivalCoef * newR);
        newG = (int) (edgeSurvivalCoef * newG);
        newB = (int) (edgeSurvivalCoef * newB);

        // apply bell coefficient based on value of disintegration
        // coefficient:
        // 1.0 -> color stays
        // 0.5 -> bell coefficient
        // 0.0 -> black
        double bellCoef = currPixelInfo.bellCoefficient / 100.0;
        double coef;
        if (disintegrationCoef >= 0.5)
          coef = bellCoef + (disintegrationCoef - 0.5) * (1.0 - bellCoef) / 0.5;
        else coef = disintegrationCoef * bellCoef / 0.5;
        newR = (int) (coef * newR);
        newG = (int) (coef * newG);
        newB = (int) (coef * newB);

        if (newR > 255) newR = 255;
        if (newR < 0) newR = 0;
        if (newG > 255) newG = 255;
        if (newG < 0) newG = 0;
        if (newB > 255) newB = 255;
        if (newB < 0) newB = 0;

        int newColor = (255 << 24) | (newR << 16) | (newG << 8) | newB;

        // System.out.println("pix: " + x + "," + y + " -> (" + newR +
        // "," + newG + "," + newB + ")");

        bitmap[x][y] = newColor;
      }
    }
  }

  public void iteration() {
    // long time0 = System.currentTimeMillis();
    switch (this.state) {
      case BellRainManager.STATE_BELLRAIN_FIRSTPHASE:
        this.iterationStep1();
        this.currIteration++;
        if (this.currIteration >= this.maxIterationFirstPhase) {
          System.out.println("BellRain first step over");
          this.bitmapAfterFirstStep =
              new TrueColorBitmapObject(this.currIndexBitmap, this.colorManager);
          this.currTrueColorBitmap =
              new TrueColorBitmapObject(this.currIndexBitmap, this.colorManager);
          this.currIteration = 0;
          this.state = BellRainManager.STATE_BELLRAIN_SECONDPHASE;
        }
        break;
      case BellRainManager.STATE_BELLRAIN_SECONDPHASE:
        this.iterationStep2();
        this.currIteration++;
        if (this.currIteration >= this.maxIterationSecondPhase) {
          System.out.println("BellRain second step over");
          this.currIteration = 0;
          this.state = BellRainManager.STATE_BELLRAIN_WAIT;
        }
        break;
      case BellRainManager.STATE_BELLRAIN_WAIT:
        this.currIteration++;
        if (this.currIteration >= this.maxIterationWait) {
          System.out.println("BellRain wait over");
          this.currIteration = 0;
          this.state = BellRainManager.STATE_BELLRAIN_DISINTEGRATION;
        }
        break;
      case BellRainManager.STATE_BELLRAIN_DISINTEGRATION:
        this.iterationStepDisintegration();
        this.currIteration++;
        if (this.currIteration >= this.maxIterationDisintegration) {
          System.out.println("BellRain disintegration over");
          this.currIteration = 0;
          this.state = BellRainManager.STATE_BELLRAIN_FINISHED;
        }
      case BellRainManager.STATE_BELLRAIN_FINISHED:
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
    return (this.state == BellRainManager.STATE_BELLRAIN_FINISHED);
  }

  public boolean toGetIndexBitmap() {
    return (this.state == BellRainManager.STATE_BELLRAIN_FIRSTPHASE);
  }
}
