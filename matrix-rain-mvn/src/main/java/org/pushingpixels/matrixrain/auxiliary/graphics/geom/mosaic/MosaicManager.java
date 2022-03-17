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
package org.pushingpixels.matrixrain.auxiliary.graphics.geom.mosaic;

import java.awt.Color;
import java.awt.Image;

import org.pushingpixels.matrixrain.auxiliary.graphics.TrueColorBitmapObject;
import org.pushingpixels.matrixrain.auxiliary.graphics.colors.interpolator.ColorInterpolator;
import org.pushingpixels.matrixrain.auxiliary.graphics.geom.voronoi.VoronoiIndexDiagramInfo;
import org.pushingpixels.matrixrain.auxiliary.math.coord.Point2D;

public final class MosaicManager {
  /*
   * public static TrueColorBitmapObject createGradientMosaic( Image image,
   * VoronoiIndexDiagramInfo voronoiIndexDiagramInfo, ColorInterpolator
   * colorInterpolator) {
   *
   * long time0 = System.currentTimeMillis();
   *  // get voronoi diagram info int width =
   * voronoiIndexDiagramInfo.getWidth(); int height =
   * voronoiIndexDiagramInfo.getHeight(); int[][] diagramIndex =
   * voronoiIndexDiagramInfo.getDiagramIndex(); Point2D[] centers =
   * voronoiIndexDiagramInfo.getCenters(); int maxIndex =
   * voronoiIndexDiagramInfo.getMaxIndex(); int maxRadius =
   * voronoiIndexDiagramInfo.getMaxRadius();
   *  // get original pixels from image int[] origPixels = new
   * int[height*width]; PixelGrabber pg = new PixelGrabber(image, 0, 0, width,
   * height, origPixels, 0, width); try { pg.grabPixels(); } catch
   * (InterruptedException e) { return null; } if ((pg.getStatus() &
   * java.awt.image.ImageObserver.ABORT) != 0) { return null; }
   *
   * int[][] trueColorBitmap = new int[width][height];
   *
   * for (int x=0; x<width; x++) { for (int y=0; y<height; y++) { Point2D
   * centerPoint = centers[diagramIndex[x][y]]; double xa = centerPoint.getX() -
   * maxRadius; double ya = centerPoint.getY(); double adjustCoef =
   * ((x-xa)*Math.sqrt(3.0) + (y-ya))/maxRadius;
   *
   * double colorCoef = 0.6; colorCoef += ((double)adjustCoef)/12.0;
   *
   * double coef = 1.0-(double)x/(double)width; Color midColor =
   * colorInterpolator.getInterpolatedColor(coef); int midR =
   * midColor.getRed(); int midG = midColor.getGreen(); int midB =
   * midColor.getBlue();
   *
   * int red = (origPixels[y*width+x] & 0x00FF0000) >> 16; int green =
   * (origPixels[y*width+x] & 0x0000FF00) >> 8; int blue =
   * (origPixels[y*width+x] & 0x000000FF);
   *
   * int newR = red + (int)((double)(midR - red) * colorCoef); int newG =
   * green + (int)((double)(midG - green) * colorCoef); int newB = blue +
   * (int)((double)(midB - blue) * colorCoef); if (newR < 0) newR = 0; if
   * (newR > 255) newR = 255; if (newG < 0) newG = 0; if (newG > 255) newG =
   * 255; if (newB < 0) newB = 0; if (newB > 255) newB = 255;
   *
   * trueColorBitmap[x][y] = (255 << 24) | (newR << 16) | (newG << 8) |
   * newB; } }
   *
   * TrueColorBitmapObject resultBitmap = new TrueColorBitmapObject(
   * trueColorBitmap, width, height);
   *
   * long time1 = System.currentTimeMillis(); System.out.println("Mosaic: " +
   * (time1-time0));
   *
   * return resultBitmap; }
   */

  public static TrueColorBitmapObject createGradientMosaic(
      Image image,
      VoronoiIndexDiagramInfo voronoiIndexDiagramInfo,
      ColorInterpolator colorInterpolator) {

    return MosaicManager.createGradientMosaic(
        new TrueColorBitmapObject(image), voronoiIndexDiagramInfo, colorInterpolator);
  }

  public static TrueColorBitmapObject createGradientMosaic(
      TrueColorBitmapObject bitmapObject,
      VoronoiIndexDiagramInfo voronoiIndexDiagramInfo,
      ColorInterpolator colorInterpolator) {

    long time0 = System.currentTimeMillis();

    // get voronoi diagram info
    int width = voronoiIndexDiagramInfo.getWidth();
    int height = voronoiIndexDiagramInfo.getHeight();
    int[][] diagramIndex = voronoiIndexDiagramInfo.getDiagramIndex();
    Point2D[] centers = voronoiIndexDiagramInfo.getCenters();
    int maxRadius = voronoiIndexDiagramInfo.getMaxRadius();

    int[][] origPixels = bitmapObject.getBitmap();

    int[][] trueColorBitmap = new int[width][height];

    for (int x = 0; x < width; x++) {
      for (int y = 0; y < height; y++) {
        Point2D centerPoint = centers[diagramIndex[x][y]];
        double xa = centerPoint.getX() - maxRadius;
        double ya = centerPoint.getY();
        double adjustCoef = ((x - xa) * Math.sqrt(3.0) + (y - ya)) / maxRadius;

        double colorCoef = 0.6;
        colorCoef += ((double) adjustCoef) / 12.0;

        double coef = 1.0 - (double) x / (double) width;
        Color midColor = colorInterpolator.getInterpolatedColor(coef);
        int midR = midColor.getRed();
        int midG = midColor.getGreen();
        int midB = midColor.getBlue();

        int red = (origPixels[x][y] & 0x00FF0000) >> 16;
        int green = (origPixels[x][y] & 0x0000FF00) >> 8;
        int blue = (origPixels[x][y] & 0x000000FF);

        int newR = red + (int) ((double) (midR - red) * colorCoef);
        int newG = green + (int) ((double) (midG - green) * colorCoef);
        int newB = blue + (int) ((double) (midB - blue) * colorCoef);
        if (newR < 0) newR = 0;
        if (newR > 255) newR = 255;
        if (newG < 0) newG = 0;
        if (newG > 255) newG = 255;
        if (newB < 0) newB = 0;
        if (newB > 255) newB = 255;

        trueColorBitmap[x][y] = (255 << 24) | (newR << 16) | (newG << 8) | newB;
      }
    }

    TrueColorBitmapObject resultBitmap = new TrueColorBitmapObject(trueColorBitmap, width, height);

    long time1 = System.currentTimeMillis();
    System.out.println("Mosaic: " + (time1 - time0));

    return resultBitmap;
  }
}
