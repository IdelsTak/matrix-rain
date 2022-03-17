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
