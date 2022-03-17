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
package org.pushingpixels.matrixrain.phosphore;

import org.pushingpixels.matrixrain.auxiliary.graphics.IndexBitmapObject;
import org.pushingpixels.matrixrain.auxiliary.math.intersect.Circle1PixelArbitraryIntersectorFactory;
import org.pushingpixels.matrixrain.intro.IntroManager;

public final class Phosphorizer {

  public static final IndexBitmapObject createScaledUpPixelVersion(
      IndexBitmapObject origBitmap, Circle1PixelArbitraryIntersectorFactory iFct, double factor) {

    int origWidth = origBitmap.getWidth();
    int origHeight = origBitmap.getHeight();

    int newWidth = (int) Math.ceil(((double) (origWidth - 1) + 0.5) * factor + 2);
    if ((newWidth % 2) == 1) newWidth++;
    int newHeight = (int) Math.ceil(((double) (origHeight - 1) + 0.5) * factor + 2);
    if ((newHeight % 2) == 1) newHeight++;

    int[][] result = new int[newWidth][newHeight];

    boolean toCenter = (factor > 2) && (Math.abs(factor - (int) factor) < 1.0e-3);

    double centerXInNew = 0.5 * factor - 0.5;
    if (toCenter) centerXInNew = Math.ceil(centerXInNew) - 0.5;
    int iFctN = iFct.N;

    int[][] origPixels = origBitmap.getBitmap();

    for (int col = 0; col < origWidth; col++) {
      int startCol = (int) Math.floor(centerXInNew - 1);
      if (startCol < 0) startCol = 0;
      int endCol = (int) Math.ceil(centerXInNew + 1);

      double centerYInNew = 0.5 * factor - 0.5;
      if (toCenter) centerYInNew = Math.ceil(centerYInNew) - 0.5;
      for (int row = 0; row < origHeight; row++) {
        if (origPixels[col][row] != 0) {
          // find all pixels in the new glyph that are affected by
          // this pixel
          int startRow = (int) Math.floor(centerYInNew - 1);
          if (startRow < 0) startRow = 0;
          int endRow = (int) Math.ceil(centerYInNew + 1);

          int colN = (int) ((startCol - centerXInNew) * iFctN);
          for (int newCol = startCol; newCol <= endCol; newCol++) {
            int rowN = (int) ((startRow - centerYInNew) * iFctN);
            for (int newRow = startRow; newRow <= endRow; newRow++) {
              int shift = iFct.unitShift(colN, rowN);
              if (shift < 8) {
                int delta = (origPixels[col][row] >> shift) << 2;
                result[newCol][newRow] += delta;
              }
              rowN += iFctN;
            }
            colN += iFctN;
          }
        }
        centerYInNew += factor;
      }
      centerXInNew += factor;
    }

    for (int i = 0; i < newWidth; i++)
      for (int j = 0; j < newHeight; j++) if (result[i][j] > 255) result[i][j] = 255;

    IndexBitmapObject iboResult = new IndexBitmapObject(result, newWidth, newHeight);
    return iboResult;
  }

  public static final int getScaledUpPhosphoreVersionWidth(
      int width, PhosphoreCloudFactory pcFct, double factor) {
    int newSize = (int) (factor * width) + pcFct.getCurrMaxWidth();
    if ((newSize % 2) == 1) newSize++;
    return newSize;
  }

  public static final int getScaledUpPhosphoreVersionHeight(
      int height, PhosphoreCloudFactory pcFct, double factor) {
    int newSize = (int) (factor * height) + pcFct.getCurrMaxHeight();
    if ((newSize % 2) == 1) newSize++;
    return newSize;
  }

  public static IndexBitmapObject createScaledUpPhosphoreVersion(
      IndexBitmapObject origBitmap,
      PhosphoreCloudFactory pcFct,
      Circle1PixelArbitraryIntersectorFactory iFct,
      double factor,
      boolean toGlow) {

    int origWidth = origBitmap.getWidth();
    int origHeight = origBitmap.getHeight();

    int newWidth = getScaledUpPhosphoreVersionWidth(origWidth, pcFct, factor);
    int newHeight = getScaledUpPhosphoreVersionHeight(origHeight, pcFct, factor);

    IndexBitmapObject pixelizedVersion = createScaledUpPixelVersion(origBitmap, iFct, factor);
    int pixelizedWidth = pixelizedVersion.getWidth();
    int pixelizedHeight = pixelizedVersion.getHeight();
    int[][] pixelizedPixels = pixelizedVersion.getBitmap();

    int resultBitWidth = (int) Math.ceil((double) newWidth / 2.0);
    int resultBitHeight = (int) Math.ceil((double) newHeight / 2.0);
    int newBitSize = resultBitWidth * resultBitHeight;
    int[] bitPixels = new int[newBitSize];
    for (int i = 0; i < newBitSize; i++) bitPixels[i] = 0;

    int offset =
        ((resultBitHeight - pixelizedHeight / 2) / 2) * resultBitWidth
            + (resultBitWidth - pixelizedWidth / 2) / 2;

    int[] bitGlowPixels = null;
    PhosphoreBitCloud bitGlowCloud = null;
    if (toGlow) {
      int a = pcFct.getCurrMaxWidth() / 2;
      int b = pcFct.getCurrMaxHeight() / 2;

      PhosphoreCloud glowCloud =
          new PhosphoreCloud(
              a,
              b,
              IntroManager.MAX_GLOW_VALUE,
              IntroManager.MIN_GLOW_VALUE,
              IntroManager.GLOW_RADIUS);
      bitGlowPixels = new int[newBitSize];
      for (int i = 0; i < newBitSize; i++) bitGlowPixels[i] = 0;
      bitGlowCloud = new PhosphoreBitCloud(glowCloud, false, false);
    }

    // create cloud around each pixel in this glyph
    boolean shiftX = false;
    for (int col = 0; col < pixelizedWidth; col++) {
      shiftX = !shiftX;
      boolean shiftY = false;
      for (int row = 0; row < pixelizedHeight; row++) {
        shiftY = !shiftY;
        int origValue = pixelizedPixels[col][row];
        if (origValue == 0) continue;

        int shift = 8 - (int) (Math.log(origValue + 1) / Math.log(2.0));

        // get phosphore cloud for this pixel
        PhosphoreBitCloud pBitCloud = pcFct.getRandomCurrBitCloud(shiftX, shiftY);

        int[] bitRadiations = pBitCloud.getRadiations();
        int refX = pBitCloud.getRefPointX(), refY = pBitCloud.getRefPointY();
        int cloudBitSize = pBitCloud.getSize();
        int cloudBitWidth = pBitCloud.getWidth();

        int bitCloudStartPositionInResult =
            offset + (row / 2 - refY) * resultBitWidth + (col / 2 - refX);
        int newPosition = bitCloudStartPositionInResult;

        int deltaRow = 0, deltaCol = 0;
        for (int i = 0; i < cloudBitSize; i++) {
          int val1, val2, val3, val4;
          if ((newPosition >= 0) && (newPosition < newBitSize)) {
            int pre = bitPixels[newPosition];
            int curr = bitRadiations[i];
            if (curr != 0) {
              if (pre == 0) {
                val1 = (((curr & 0xFF000000) >>> 24) >> shift);
                val2 = (((curr & 0x00FF0000) >> 16) >> shift);
                val3 = (((curr & 0x0000FF00) >> 8) >> shift);
                val4 = ((curr & 0x000000FF) >> shift);
              } else {
                val1 = ((pre & 0xFF000000) >>> 24) + (((curr & 0xFF000000) >>> 24) >> shift);
                if (val1 > 255) val1 = 255;
                val2 = ((pre & 0x00FF0000) >> 16) + (((curr & 0x00FF0000) >> 16) >> shift);
                if (val2 > 255) val2 = 255;
                val3 = ((pre & 0x0000FF00) >> 8) + (((curr & 0x0000FF00) >> 8) >> shift);
                if (val3 > 255) val3 = 255;
                val4 = (pre & 0x000000FF) + ((curr & 0x000000FF) >> shift);
                if (val4 > 255) val4 = 255;
              }
              /*
               * System.out.print(" old: " + ((pre & 0xFF000000)
               * >>> 24) + "," + ((pre & 0x00FF0000) >> 16) + "," +
               * ((pre & 0x0000FF00) >> 8) + "," + (pre &
               * 0x000000FF)); System.out.print(" add: " + (((curr &
               * 0xFF000000) >>> 24) >> shift) + "," + (((curr &
               * 0x00FF0000) >> 16) >> shift) + "," + (((curr &
               * 0x0000FF00) >> 8) >> shift) + "," + ((curr &
               * 0x000000FF) >> shift)); System.out.println(" new: " +
               * val1 + "," + val2 + "," + val3 + "," + val4);
               */
              bitPixels[newPosition] = (val1 << 24) | (val2 << 16) | (val3 << 8) | val4;
            }
          }

          deltaCol++;
          newPosition++;
          if (deltaCol >= cloudBitWidth) {
            deltaCol = 0;
            deltaRow++;
            newPosition += (resultBitWidth - cloudBitWidth);
          }
        }

        if (toGlow) {
          // get phosphore glow cloud for this pixel
          int[] bitGlowRadiations = bitGlowCloud.getRadiations();
          refX = bitGlowCloud.getRefPointX();
          refY = bitGlowCloud.getRefPointY();
          int glowCloudBitSize = bitGlowCloud.getSize();
          int glowCloudBitWidth = bitGlowCloud.getWidth();

          int bitGlowCloudStartPositionInResult =
              offset + (row / 2 - refY) * resultBitWidth + (col / 2 - refX);
          newPosition = bitGlowCloudStartPositionInResult;

          deltaRow = 0;
          deltaCol = 0;
          for (int i = 0; i < glowCloudBitSize; i++) {
            int val1, val2, val3, val4, currGlow;
            if ((newPosition >= 0) && (newPosition < newBitSize)) {
              int pre = bitGlowPixels[newPosition];
              int curr = bitGlowRadiations[i];
              if (curr != 0) {
                if (pre == 0) {
                  val1 = (((curr & 0xFF000000) >>> 24) >> shift);
                  val2 = (((curr & 0x00FF0000) >> 16) >> shift);
                  val3 = (((curr & 0x0000FF00) >> 8) >> shift);
                  val4 = ((curr & 0x000000FF) >> shift);
                } else {
                  currGlow = (((curr & 0xFF000000) >>> 24) >> shift);
                  val1 = ((pre & 0xFF000000) >>> 24);
                  if (currGlow > val1) val1 = currGlow;
                  currGlow = (((curr & 0x00FF0000) >> 16) >> shift);
                  val2 = ((pre & 0x00FF0000) >> 16);
                  if (currGlow > val2) val2 = currGlow;
                  currGlow = (((curr & 0x0000FF00) >> 8) >> shift);
                  val3 = ((pre & 0x0000FF00) >> 8);
                  if (currGlow > val3) val3 = currGlow;
                  currGlow = ((curr & 0x000000FF) >> shift);
                  val4 = (pre & 0x000000FF);
                  if (currGlow > val4) val4 = currGlow;
                }
                /*
                 * System.out.print(" old: " + ((pre &
                 * 0xFF000000) >>> 24) + "," + ((pre &
                 * 0x00FF0000) >> 16) + "," + ((pre &
                 * 0x0000FF00) >> 8) + "," + (pre &
                 * 0x000000FF)); System.out.print(" add: " +
                 * (((curr & 0xFF000000) >>> 24) >> shift) + "," +
                 * (((curr & 0x00FF0000) >> 16) >> shift) + "," +
                 * (((curr & 0x0000FF00) >> 8) >> shift) + "," +
                 * ((curr & 0x000000FF) >> shift));
                 * System.out.println(" new: " + val1 + "," +
                 * val2 + "," + val3 + "," + val4);
                 */
                bitGlowPixels[newPosition] = (val1 << 24) | (val2 << 16) | (val3 << 8) | val4;
              }
            }

            deltaCol++;
            newPosition++;
            if (deltaCol >= glowCloudBitWidth) {
              deltaCol = 0;
              deltaRow++;
              newPosition += (resultBitWidth - glowCloudBitWidth);
            }
          }
        }
      }
    }

    int[][] result = new int[newWidth][newHeight];
    for (int i = 0; i < newWidth; i++) for (int j = 0; j < newHeight; j++) result[i][j] = 0;

    int cornerRow = 0;
    int cornerCol = 0;
    for (int i = 0; i < newBitSize; i++) {
      int cloudFactor = bitPixels[i];
      if (cloudFactor != 0) {
        int val1 = (cloudFactor & 0xFF000000) >>> 24;
        int val2 = (cloudFactor & 0x00FF0000) >> 16;
        int val3 = (cloudFactor & 0x0000FF00) >> 8;
        int val4 = (cloudFactor & 0x000000FF);

        result[cornerCol][cornerRow] = val1;
        if ((cornerCol + 1) < newWidth) result[cornerCol + 1][cornerRow] = val2;
        if ((cornerRow + 1) < newHeight) result[cornerCol][cornerRow + 1] = val3;
        if (((cornerCol + 1) < newWidth) && ((cornerRow + 1) < newHeight))
          result[cornerCol + 1][cornerRow + 1] = val4;
      }

      cornerCol += 2;
      if (cornerCol >= newWidth) {
        cornerCol = 0;
        cornerRow += 2;
      }
    }

    if (toGlow) {
      int refX = bitGlowCloud.getRefPointX();
      int refY = bitGlowCloud.getRefPointY();
      cornerRow = -refY;
      cornerCol = -refX;
      for (int i = 0; i < newBitSize; i++) {
        if ((cornerRow >= 0) && (cornerCol >= 0)) {
          int glowFactor = bitGlowPixels[i];
          if (glowFactor != 0) {
            int val1 = (glowFactor & 0xFF000000) >>> 24;
            int val2 = (glowFactor & 0x00FF0000) >> 16;
            int val3 = (glowFactor & 0x0000FF00) >> 8;
            int val4 = (glowFactor & 0x000000FF);

            if (result[cornerCol][cornerRow] < val1) result[cornerCol][cornerRow] = val1;
            if (((cornerCol + 1) < newWidth) && (result[cornerCol + 1][cornerRow] < val2))
              result[cornerCol + 1][cornerRow] = val2;
            if (((cornerRow + 1) < newHeight) && (result[cornerCol][cornerRow + 1] < val3))
              result[cornerCol][cornerRow + 1] = val3;
            if ((((cornerCol + 1) < newWidth))
                && ((cornerRow + 1) < newHeight)
                && (result[cornerCol + 1][cornerRow + 1] < val4))
              result[cornerCol + 1][cornerRow + 1] = val4;
          }
        }

        cornerCol += 2;
        if (cornerCol >= (newWidth - refX)) {
          cornerCol = -refX;
          cornerRow += 2;
        }
      }
    }

    IndexBitmapObject iboResult = new IndexBitmapObject(result, newWidth, newHeight);
    return iboResult;
  }
}
