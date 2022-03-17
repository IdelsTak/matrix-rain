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
package org.pushingpixels.matrixrain.auxiliary.math.coord;

public final class Point2DList {

  public class Point2DElement {
    public Point2D point;
    public Point2DElement next;
    public Point2DElement prev;

    public Point2DElement(Point2D point) {
      this.point = point;
      this.next = null;
      this.prev = null;
    }
  }

  public Point2DElement head;

  public Point2DElement tail;

  public int length;

  public Point2DList() {
    head = null;
    tail = null;
    length = 0;
  }

  public synchronized void addPoint(Point2D point) {
    Point2DElement newElement = new Point2DElement(point);
    newElement.next = this.head;
    if (this.head != null) this.head.prev = newElement;
    if (this.head == null) this.tail = newElement;
    this.head = newElement;
    length++;
  }

  public synchronized void addPointAtTail(Point2D point) {
    Point2DElement newElement = new Point2DElement(point);
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

  public synchronized void removePoint(Point2DElement element) {
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

  public synchronized void removePoint(long id) {
    // search for it
    Point2DElement curr = this.head;
    while (curr != null) {
      if (curr.point.id == id) break;
      curr = curr.next;
    }
    if (curr != null) this.removePoint(curr);
  }

  public boolean exists(long id) {
    // search for it
    Point2DElement curr = this.head;
    while (curr != null) {
      if (curr.point.id == id) return true;
      curr = curr.next;
    }
    return false;
  }

  public Point2D getPointWithID(long id) {
    // search for it
    Point2DElement curr = this.head;
    while (curr != null) {
      if (curr.point.id == id) return curr.point;
      curr = curr.next;
    }
    return null;
  }

  private Point2D[] getAsArray() {
    if (this.head == null) return null;
    int count = this.length;
    if (count == 0) return null;
    Point2D[] arr = new Point2D[count];
    int curr = 0;
    Point2DList.Point2DElement currElem = this.head;
    while (currElem != null) {
      arr[curr++] = currElem.point;
      currElem = currElem.next;
    }
    return arr;
  }

  public Point2D[] getAsSortedXArray() {
    Point2D[] arr = this.getAsArray();
    if (arr == null) return null;
    int count = arr.length;
    if (count <= 1) return arr;

    // bubble sort
    for (int i = 0; i < count; i++) {
      for (int j = i + 1; j < count; j++) {
        if (arr[i].getX() > arr[j].getX()) {
          // swap
          Point2D tmp = arr[i];
          arr[i] = arr[j];
          arr[j] = tmp;
        }
      }
    }
    return arr;
  }

  public Point2D[] getAsSortedYArray() {
    Point2D[] arr = this.getAsArray();
    if (arr == null) return null;
    int count = arr.length;
    if (count <= 1) return arr;

    // bubble sort
    for (int i = 0; i < count; i++) {
      for (int j = i + 1; j < count; j++) {
        if (arr[i].getY() > arr[j].getY()) {
          // swap
          Point2D tmp = arr[i];
          arr[i] = arr[j];
          arr[j] = tmp;
        }
      }
    }
    return arr;
  }
}
