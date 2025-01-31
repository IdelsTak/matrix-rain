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

public final class GaussValues {
  public static int SAMPLE_RATE;
  private static final double[] GAUSS_VALUES = {
    .5000, .5080, .5160, .5239, .5319, .5398, .5478, .5557, .5636, .5714, .5793, .5871, .5948,
    .6026, .6103, .6179, .6255, .6331, .6406, .6480, .6554, .6628, .6700, .6772, .6844, .6915,
    .6985, .7054, .7123, .7190, .7258, .7324, .7389, .7454, .7518, .7580, .7642, .7704, .7764,
    .7823, .7881, .7939, .7996, .8051, .8106, .8159, .8212, .8264, .8315, .8365, .8413, .8461,
    .8508, .8554, .8599, .8643, .8686, .8729, .8770, .8810, .8849, .8888, .8925, .8962, .8997,
    .9032, .9066, .9029, .9131, .9162, .9192, .9222, .9251, .9279, .9306, .9332, .9357, .9382,
    .9406, .9429, .9452, .9474, .9495, .9515, .9535, .9554, .9573, .9591, .9608, .9625, .9641,
    .9656, .9671, .9686, .9699, .9713, .9726, .9738, .9750, .9761, .9772, .9783, .9793, .9803,
    .9812, .9821, .9830, .9838, .9846, .9854, .9861, .9868, .9875, .9881, .9887, .9893, .9898,
    .9904, .9909, .9913, .9918, .9922, .9927, .9931, .9934, .9938, .9941, .9945, .9948, .9951,
    .9953, .9956, .9959, .9961, .9963, .9965, .9967, .9969, .9971, .9973, .9974, .9976, .9977,
    .9979, .9980, .9981, .9982, .9984, .9985, .9986, .9987
  };
  private static final int COUNT = GAUSS_VALUES.length;
  private static int[] filterValues;
  private static boolean tableComputed;
  private static int valueCount;

  public static void initialize() {
    if (tableComputed) return;
    SAMPLE_RATE = (int) ((GAUSS_VALUES.length - 1) / 1.5);
    valueCount = GAUSS_VALUES.length;
    int iHalfSample = SAMPLE_RATE / 2;
    filterValues = new int[valueCount];
    for (int i = 0; i <= iHalfSample; i++) {
      filterValues[i] =
          (int) (255 * (1.0 - (1.0 - GAUSS_VALUES[50 + i]) - (1.0 - GAUSS_VALUES[50 - i])));
    }
    for (int i = iHalfSample + 1; i < valueCount; i++) {
      filterValues[i] = (int) (255 * (1.0 - GAUSS_VALUES[i - 50]));
    }
    tableComputed = true;
  }

  public static double getValue(double d) {
    int index = (int) (d * 50.0);
    if (index < 0) index = -index;
    if (index >= COUNT) return 1.0;
    return GAUSS_VALUES[index];
  }

  // get [0..maxValue) random number with gaussian distribution
  public static int getRandomGaussian(int maxValue) {
    // get random number from 0..0.5
    double rand = Math.random() / 2.0;
    // get corresponing gaussian random number from 0..150
    int index = 0;
    for (int i = 0; i < GAUSS_VALUES.length; i++) {
      if (rand < (GAUSS_VALUES[i] - 0.5)) {
        index = i;
        break;
      }
    }
    // scale to 0..maxValue
    return index * maxValue / GAUSS_VALUES.length;
  }

  public static int getIntensity(double distance) {
    initialize();
    return filterValues[(int) (distance * (double) SAMPLE_RATE)];
  }
}
