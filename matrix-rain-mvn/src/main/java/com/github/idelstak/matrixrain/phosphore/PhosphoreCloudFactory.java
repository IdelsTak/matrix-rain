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
package com.github.idelstak.matrixrain.phosphore;

public final class PhosphoreCloudFactory {
  private static final int N = 10;

  private final class PhosphoreCloudElement {
    public int sizeN;
    public int maxWidth;
    public int maxHeight;
    public PhosphoreCloud[] clouds;
    public PhosphoreBitCloud[] bitClouds00;
    public PhosphoreBitCloud[] bitClouds01;
    public PhosphoreBitCloud[] bitClouds10;
    public PhosphoreBitCloud[] bitClouds11;
    public PhosphoreCloudElement next;

    public PhosphoreCloudElement(double size, int cloudCount) {
      this.maxWidth = 0;
      this.maxHeight = 0;
      this.sizeN = (int) (N * size);
      this.clouds = new PhosphoreCloud[cloudCount];
      this.bitClouds00 = new PhosphoreBitCloud[cloudCount];
      this.bitClouds01 = new PhosphoreBitCloud[cloudCount];
      this.bitClouds10 = new PhosphoreBitCloud[cloudCount];
      this.bitClouds11 = new PhosphoreBitCloud[cloudCount];
      for (int i = 0; i < cloudCount; i++) {
        PhosphoreCloud pCloud = new PhosphoreCloud(size, 1 + size / 4);
        // System.out.println("cloud of " + size + " - max value " +
        // pCloud.getMaxValue());
        pCloud.normalize(1.0);
        this.maxWidth = Math.max(this.maxWidth, pCloud.getWidth());
        this.maxHeight = Math.max(this.maxHeight, pCloud.getHeight());
        this.clouds[i] = pCloud;
        this.bitClouds00[i] = new PhosphoreBitCloud(pCloud, false, false);
        this.bitClouds01[i] = new PhosphoreBitCloud(pCloud, false, true);
        this.bitClouds10[i] = new PhosphoreBitCloud(pCloud, true, false);
        this.bitClouds11[i] = new PhosphoreBitCloud(pCloud, true, true);
      }
      this.next = null;
    }
  }

  private final class PhosphoreCloudList {
    public PhosphoreCloudElement head;
    public PhosphoreCloudElement curr;
    public int length;
    private final int cloudCount;
    private int prevIndex = 0;

    public PhosphoreCloudList(int cloudCount) {
      this.head = null;
      this.curr = null;
      this.length = 0;
      this.cloudCount = cloudCount;
    }

    private void addClouds(double size) {
      PhosphoreCloudElement newElement = new PhosphoreCloudElement(size, cloudCount);
      newElement.next = this.head;
      this.head = newElement;
      this.curr = newElement;
      length++;
    }

    private int getNextCloudIndex() {
      prevIndex++;
      if (prevIndex >= this.cloudCount) prevIndex = 0;
      return prevIndex;
      // double val = Math.random();
      // return (int)(Math.floor(val*maxValue));
    }

    public PhosphoreCloud getRandomCurrCloud() {
      // choose random cloud
      PhosphoreCloudElement currElem = this.curr;
      int index = this.getNextCloudIndex();
      return currElem.clouds[index];
    }

    public PhosphoreBitCloud getRandomCurrBitCloud(boolean shiftX, boolean shiftY) {
      // choose random cloud
      PhosphoreCloudElement currElem = this.curr;
      int index = this.getNextCloudIndex();
      // int index = getRandom(currElem.clouds.length);
      if (shiftX)
        if (shiftY) return currElem.bitClouds11[index];
        else return currElem.bitClouds10[index];
      else if (shiftY) return currElem.bitClouds01[index];
      else return currElem.bitClouds00[index];
    }

    public int getCurrMaxWidth() {
      return this.curr.maxWidth;
    }

    public int getCurrMaxHeight() {
      return this.curr.maxHeight;
    }

    public void setCurrSize(double size) {
      PhosphoreCloudElement currElem = this.head;
      int sizeN = (int) (size * N);
      while (currElem != null) {
        if (currElem.sizeN == sizeN) {
          this.curr = currElem;
          return;
        }
        currElem = currElem.next;
      }
      // if here - does not exist. create
      this.addClouds(size);
    }
  }

  private final PhosphoreCloudList cloudList;
  public static long delta = 0;

  public PhosphoreCloudFactory(int cloudCount) {
    this.cloudList = new PhosphoreCloudList(cloudCount);
  }

  public void addSize(double size) {
    cloudList.addClouds(size);
  }

  public PhosphoreCloud getRandomCurrCloud() {
    PhosphoreCloud result = cloudList.getRandomCurrCloud();
    return result;
  }

  public PhosphoreBitCloud getRandomCurrBitCloud(boolean shiftX, boolean shiftY) {
    return cloudList.getRandomCurrBitCloud(shiftX, shiftY);
  }

  public int getCurrMaxWidth() {
    return cloudList.getCurrMaxWidth();
  }

  public int getCurrMaxHeight() {
    return cloudList.getCurrMaxHeight();
  }

  public void setCurrFactor(double factor) {
    cloudList.setCurrSize(4.0 * factor - 1.5);
  }
}
