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
package com.github.idelstak.matrixrain.auxiliary.math;

public class GaussConvolution {

  private final double[][] kernel;
  private final int[][] kernel_shift;
  private final double[] kernel_1d;
  private final int size;

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
