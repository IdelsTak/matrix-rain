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
package org.pushingpixels.matrixrain.font;

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
			if (isEmpty)
				return currRow;
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
						this.addVerticalSegment(minRow, maxRow, prevColumn,
								currClusterWidth);
					break;
				}
				int currColumn = clusterElement.line.x;
				if (currColumn < (prevColumn - 1)) {
					// start of the next cluster. check cluster width
					if (currClusterWidth >= 2)
						this.addVerticalSegment(minRow, maxRow, prevColumn,
								currClusterWidth);
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
			if (isEmpty)
				return currColumn;
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
						this.addHorizontalSegment(minColumn, maxColumn,
								prevRow, currClusterHeight);
					break;
				}
				int currRow = clusterElement.line.y;
				if (currRow < (prevRow - 1)) {
					// start of the next cluster. check cluster height
					if (currClusterHeight >= 2)
						this.addHorizontalSegment(minColumn, maxColumn,
								prevRow, currClusterHeight);
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
