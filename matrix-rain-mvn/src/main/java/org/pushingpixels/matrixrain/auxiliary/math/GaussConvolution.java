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
package org.pushingpixels.matrixrain.auxiliary.math;

public class GaussConvolution {

  private double[][] kernel;

  private int[][] kernel_shift;

  private double[] kernel_1d;

  private int size;

  public GaussConvolution(double sigma, int size) {
    this.size = size;
    // compute the kernel
    kernel = new double[2 * size + 1][2 * size + 1];
    kernel_shift = new int[2 * size + 1][2 * size + 1];
    int width = 2 * size + 1;
    kernel_1d = new double[width * width];
    double sigsig = sigma * sigma;
    for (int x = -size; x <= size; x++) {
      for (int y = -size; y <= size; y++) {
        // approximate integral over a pixel*pixel area
        int count = 0;
        double startX = x - 0.5;
        double endX = x + 0.51;
        double startY = y - 0.5;
        double endY = y + 0.51;
        double val = 0.0;
        for (double subX = startX; subX < endX; subX += 0.1) {
          for (double subY = startY; subY < endY; subY += 0.1) {
            val += Math.pow(Math.E, -(subX * subX + subY * subY) / (2.0 * sigsig));
            count++;
          }
        }
        val /= (2 * Math.PI * sigsig);
        val /= count;
        kernel[x + size][y + size] = val;
        kernel_shift[x + size][y + size] = (int) (Math.log(1.0 / val) / Math.log(2.0));
        kernel_1d[(y + size) * width + x + size] = val;
      }
    }
  }

  private short pixelConvolution(short[][] bitmap, int width, int height, int x, int y) {
    int startX = x - this.size;
    if (startX < 0) startX = 0;
    int endX = x + this.size;
    if (endX >= width) endX = width - 1;
    int startY = Math.max(0, y - this.size);
    if (startY < 0) startY = 0;
    int endY = y + this.size;
    if (endY >= height) endY = height - 1;
    short val = 0;
    int kernelIndex = 0;
    for (int i = startX; i <= endX; i++) {
      for (int j = startY; j <= endY; j++) {
        int shift = kernel_shift[size + i - x][size + j - y];
        if ((shift >= 0) && (shift < 8)) val += (bitmap[i][j] >>> shift);
        // vald += (bitmap[i][j]*kernel_1d[kernelIndex]);
        kernelIndex++;
      }
    }
    return (short) val;
  }

  public short[][] getSmoothedBitmap(short[][] bitmap, int width, int height) {
    short[][] result = new short[width][height];
    for (int x = 0; x < width; x++) {
      for (int y = 0; y < height; y++) {
        result[x][y] = pixelConvolution(bitmap, width, height, x, y);
      }
    }
    return result;
  }
}
