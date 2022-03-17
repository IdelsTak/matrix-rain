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
package com.github.idelstak.matrixrain.font;

public class SegmentScanner {

  private int[][] valueMap;

  private int size;

  private GlyphSegment segments;

  private class VerticalLine {
    public int x;

    public int y1, y2;
  }

  private class HorizontalLine {
    public int y;

    public int x1, x2;
  }

  private class VerticalLineElement {
    public VerticalLine line;

    public VerticalLineElement next;
  }

  private class HorizontalLineElement {
    public HorizontalLine line;

    public HorizontalLineElement next;
  }

  public SegmentScanner(int[][] valueMap, int size) {
    this.size = size;
    this.valueMap = valueMap;
    this.segments = null;
  }

  private void addHorizontalSegment(int x1, int x2, int y, int height) {
    GlyphSegment gs = new GlyphSegment();
    gs.startX = x1;
    gs.endX = x2;
    gs.startY = y;
    gs.endY = y + height;
    gs.width = x2 - x1;
    gs.height = height;
    gs.isVertical = false;
    gs.next = this.segments;
    this.segments = gs;
  }

  private void addVerticalSegment(int y1, int y2, int x, int width) {
    GlyphSegment gs = new GlyphSegment();
    gs.startX = x;
    gs.endX = x + width;
    gs.startY = y1;
    gs.endY = y2;
    gs.width = width;
    gs.height = y2 - y1;
    gs.isVertical = true;
    gs.next = this.segments;
    this.segments = gs;
  }

  private int nextEmptyRow(int column, int startRow) {
    // Go over a column and try to find the next empty row.
    int currRow = startRow + 1;
    while (currRow < this.size) {
      int value = this.valueMap[column][currRow];

      boolean isEmpty = (value == 0);
      if (isEmpty) return currRow;
      currRow++;
    }
    // if here - we are at the last row
    return size - 1;
  }

  private VerticalLineElement computeAllVerticalLines() {
    // Go over all columns. A line is defined as a sequence of vertical
    // pixels that is longer than three fifths of the original image
    int minLength = 3 * this.size / 5;

    VerticalLineElement result = null;

    for (int currColumn = 0; currColumn < (this.size - 1); currColumn++) {
      // start checking this column
      int currStartRow = 0, currEndRow = 0;
      while (currStartRow < (this.size - 1)) {
        currEndRow = this.nextEmptyRow(currColumn, currStartRow);
        // check that the last sequence is long enough
        if ((currEndRow - currStartRow) >= minLength) {
          VerticalLineElement vle = new VerticalLineElement();
          vle.line = new VerticalLine();
          vle.line.x = currColumn;
          vle.line.y1 = currStartRow;
          vle.line.y2 = currEndRow;
          vle.next = result;
          result = vle;
        }
        currStartRow = currEndRow;
      }
    }
    return result;
  }

  private void scanForVertical() {
    // get all vertical lines
    VerticalLineElement vLines = this.computeAllVerticalLines();

    // compute line "clusters" : a cluster is defined as a sequence of
    // at least 3 adjacent vertical lines with at least 3 lines
    VerticalLineElement clusterElement = vLines;
    while (clusterElement != null) {
      // start cluster at this column
      int currClusterWidth = 1;
      int minRow = clusterElement.line.y1, maxRow = clusterElement.line.y2;
      int prevColumn = clusterElement.line.x;
      while (true) {
        clusterElement = clusterElement.next;
        if (clusterElement == null) {
          // last vertical line. check cluster width
          if (currClusterWidth >= 2)
            this.addVerticalSegment(minRow, maxRow, prevColumn, currClusterWidth);
          break;
        }
        int currColumn = clusterElement.line.x;
        if (currColumn < (prevColumn - 1)) {
          // start of the next cluster. check cluster width
          if (currClusterWidth >= 2)
            this.addVerticalSegment(minRow, maxRow, prevColumn, currClusterWidth);
          break;
        } else {
          currClusterWidth++;
          minRow = Math.min(minRow, clusterElement.line.y1);
          maxRow = Math.max(maxRow, clusterElement.line.y2);
        }
        prevColumn = currColumn;
      }
    }
  }

  private int nextEmptyColumn(int row, int startColumn) {
    // Go over a row and try to find the next empty column.
    int currColumn = startColumn + 1;
    while (currColumn < this.size) {
      int value = this.valueMap[currColumn][row];

      boolean isEmpty = (value == 0);
      if (isEmpty) return currColumn;
      currColumn++;
    }
    // if here - we are at the last column
    return size - 1;
  }

  private HorizontalLineElement computeAllHorizontalLines() {
    // Go over all rows. A line is defined as a sequence of horizontal
    // pixels that is longer than a half of the original image
    int minLength = this.size / 2;

    HorizontalLineElement result = null;

    for (int currRow = 0; currRow < (this.size - 1); currRow++) {
      // start checking this row
      int currStartColumn = 0, currEndColumn = 0;
      while (currStartColumn < (this.size - 1)) {
        currEndColumn = this.nextEmptyColumn(currRow, currStartColumn);
        // check that the last sequence is long enough
        if ((currEndColumn - currStartColumn) >= minLength) {
          HorizontalLineElement hle = new HorizontalLineElement();
          hle.line = new HorizontalLine();
          hle.line.y = currRow;
          hle.line.x1 = currStartColumn;
          hle.line.x2 = currEndColumn;
          hle.next = result;
          result = hle;
        }
        currStartColumn = currEndColumn;
      }
    }
    return result;
  }

  private void scanForHorizontal() {
    // get all horizontal lines
    HorizontalLineElement hLines = this.computeAllHorizontalLines();

    // compute line "clusters" : a cluster is defined as a sequence of
    // at least 3 adjacent horizontal lines with at least 3 lines
    HorizontalLineElement clusterElement = hLines;
    while (clusterElement != null) {
      // start cluster at this row
      int currClusterHeight = 1;
      int minColumn = clusterElement.line.x1, maxColumn = clusterElement.line.x2;
      int prevRow = clusterElement.line.y;
      while (true) {
        clusterElement = clusterElement.next;
        if (clusterElement == null) {
          // last horizontal line. check cluster heightdth
          if (currClusterHeight >= 2)
            this.addHorizontalSegment(minColumn, maxColumn, prevRow, currClusterHeight);
          break;
        }
        int currRow = clusterElement.line.y;
        if (currRow < (prevRow - 1)) {
          // start of the next cluster. check cluster height
          if (currClusterHeight >= 2)
            this.addHorizontalSegment(minColumn, maxColumn, prevRow, currClusterHeight);
          break;
        } else {
          currClusterHeight++;
          minColumn = Math.min(minColumn, clusterElement.line.x1);
          maxColumn = Math.max(maxColumn, clusterElement.line.x2);
        }
        prevRow = currRow;
      }
    }
  }

  public void computeSegments() {
    scanForVertical();
    scanForHorizontal();
  }

  public GlyphSegment getSegments() {
    return this.segments;
  }
}
