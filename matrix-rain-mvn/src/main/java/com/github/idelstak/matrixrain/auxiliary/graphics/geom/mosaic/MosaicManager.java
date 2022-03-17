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
package com.github.idelstak.matrixrain.auxiliary.graphics.geom.mosaic;

import java.awt.Color;
import java.awt.Image;

import com.github.idelstak.matrixrain.auxiliary.graphics.TrueColorBitmapObject;
import com.github.idelstak.matrixrain.auxiliary.graphics.colors.interpolator.ColorInterpolator;
import com.github.idelstak.matrixrain.auxiliary.graphics.geom.voronoi.VoronoiIndexDiagramInfo;
import com.github.idelstak.matrixrain.auxiliary.math.coord.Point2D;

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
