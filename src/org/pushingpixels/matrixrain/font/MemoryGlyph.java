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

import org.pushingpixels.matrixrain.auxiliary.graphics.IndexBitmapObject;
import org.pushingpixels.matrixrain.auxiliary.math.intersect.Circle1PixelArbitraryIntersectorFactory;
import org.pushingpixels.matrixrain.intro.IntroManager;
import org.pushingpixels.matrixrain.phosphore.*;

public final class MemoryGlyph {
	private int[][] pixels;

	private int size;

	private int weight;

	private int left, right, top, bottom;

	private GlyphSegment segments;

	// glow cloud
	private PhosphoreCloud glowCloud;

	public static long delta1 = 0, delta2 = 0;

	public static long count1 = 0;

	public MemoryGlyph() {
		this.size = 0;
		this.pixels = null;
		this.segments = null;
		this.glowCloud = null;
	}

	public MemoryGlyph(int size) {
		this.size = size;
		this.pixels = new int[size][size];
		for (int i = 0; i < size; i++) {
			for (int j = 0; j < size; j++) {
				this.pixels[i][j] = 0;
			}
		}
		this.segments = null;
		this.glowCloud = null;
	}

	public Object clone() {
		if (this.size == 0)
			return new MemoryGlyph();

		MemoryGlyph glyph = new MemoryGlyph(this.size);
		for (int i = 0; i < this.size; i++) {
			for (int j = 0; j < this.size; j++) {
				glyph.pixels[i][j] = this.pixels[i][j];
			}
		}
		glyph.computeMetrics();
		glyph.computeSegments();
		if (this.glowCloud != null)
			glyph.glowCloud = (PhosphoreCloud) this.glowCloud.clone();
		else
			glyph.glowCloud = null;
		return glyph;
	}

	public void setPixel(int x, int y, int value) {
		pixels[x][y] = value;
	}

	public void addToPixel(int x, int y, int value) {
		pixels[x][y] += value;
	}

	public int getPixel(int x, int y) {
		return pixels[x][y];
	}

	public int getSize() {
		return size;
	}

	public PhosphoreCloud getGlowCloud() {
		return this.glowCloud;
	}

	public void sharpen(double coef) {
		// find maximum value and "stretch" the color map to 255
		int maxVal = 0;
		for (int i = 0; i < size; i++)
			for (int j = 0; j < size; j++)
				maxVal = Math.max(maxVal, pixels[i][j]);
		if (maxVal == 0)
			return;
		double factor = 255.0 / maxVal;
		double cutoffMax = coef * 255.0;
		for (int i = 0; i < size; i++) {
			for (int j = 0; j < size; j++) {
				int newVal = (int) (factor * (double) pixels[i][j]);
				if (newVal > cutoffMax)
					newVal = 255;
				pixels[i][j] = newVal;
			}
		}
	}

	public void cutoff() {
		for (int i = 0; i < size; i++)
			for (int j = 0; j < size; j++)
				pixels[i][j] = Math.min(255, pixels[i][j]);
	}

	public void brighten(double factor, int maxValue) {
		for (int i = 0; i < size; i++)
			for (int j = 0; j < size; j++)
				if (pixels[i][j] < maxValue)
					pixels[i][j] = Math
							.max(
									0,
									(int) (maxValue + (double) (pixels[i][j] - maxValue)
											/ factor));
	}

	public void normalize() {
		for (int i = 0; i < size; i++)
			for (int j = 0; j < size; j++)
				if (pixels[i][j] > 255)
					pixels[i][j] = 255;
	}

	public void scaleValues() {
		int maxValue = 0;
		for (int i = 0; i < size; i++)
			for (int j = 0; j < size; j++)
				maxValue = Math.max(maxValue, pixels[i][j]);
		if (maxValue == 0)
			return;
		double coef = 2.0 * 255.0 / (double) maxValue;
		for (int i = 0; i < size; i++)
			for (int j = 0; j < size; j++)
				pixels[i][j] = Math.min(255,
						(int) (coef * (double) pixels[i][j]));
	}

	public void computeMetrics() {
		// compute sum of pixels
		int sum = 0;
		for (int i = 0; i < size; i++)
			for (int j = 0; j < size; j++)
				sum += pixels[i][j];
		// compute area
		int minX = size, minY = size, maxX = 0, maxY = 0;
		for (int i = 0; i < size; i++) {
			for (int j = 0; j < size; j++) {
				if (pixels[i][j] > 0) {
					minX = Math.min(minX, i);
					maxX = Math.max(maxX, i);
					minY = Math.min(minY, j);
					maxY = Math.max(maxY, j);
				}
			}
		}

		double coef = (double) ((maxX - minX + 1) * (maxY - minY + 1))
				/ (double) (size * size);
		// this.weight = (int)(coef*sum);
		this.weight = (int) (coef * sum / (size * size));

		// set bounding rectangle
		this.left = minX;
		this.right = maxX;
		this.top = minY;
		this.bottom = maxY;
	}

	public int getWeight() {
		return weight;
	}

	public int getLeft() {
		return left;
	}

	public int getRight() {
		return right;
	}

	public int getTop() {
		return top;
	}

	public int getBottom() {
		return bottom;
	}

	public int getHeight() {
		return bottom - top + 1;
	}

	public int getWidth() {
		return right - left + 1;
	}

	public void computeSegments() {
		SegmentScanner segmScanner = new SegmentScanner(this.pixels, this.size);
		segmScanner.computeSegments();
		this.segments = segmScanner.getSegments();
		segmScanner = null;
	}

	public GlyphSegment getSegments() {
		return this.segments;
	}

	// shift a part of glyph vertically
	private void shiftGlyphVertically(int shiftColumn, int yOffset,
			boolean toShiftRightPart) {
		int startRowInSource = Math.max(0, yOffset);
		int endRowInSource = Math.min(this.size - 1, this.size - 1 + yOffset);

		int startColumn, endColumn;
		if (toShiftRightPart) {
			startColumn = shiftColumn;
			endColumn = this.size - 1;
		} else {
			startColumn = 0;
			endColumn = shiftColumn - 1;
		}
		for (int column = startColumn; column <= endColumn; column++) {
			for (int row = 0; row < startRowInSource; row++)
				this.pixels[column][row] = 0;
			for (int row = startRowInSource; row <= endRowInSource; row++)
				this.pixels[column][row - yOffset] = this.pixels[column][row];
			for (int row = endRowInSource + 1; row < size; row++)
				this.pixels[column][row - yOffset] = 0;
		}
	}

	// shift a part of glyph horizontally
	private void shiftGlyphHorizontally(int shiftRow, int xOffset,
			boolean toShiftUpperPart) {
		int startColumnInSource = Math.max(0, xOffset);
		int endColumnInSource = Math
				.min(this.size - 1, this.size - 1 + xOffset);

		int startRow, endRow;
		if (toShiftUpperPart) {
			startRow = 0;
			endRow = shiftRow - 1;
		} else {
			startRow = shiftRow;
			endRow = this.size - 1;
		}
		for (int row = startRow; row <= endRow; row++) {
			for (int column = 0; column < startColumnInSource; column++)
				this.pixels[column][row] = 0;
			for (int column = startColumnInSource; column <= endColumnInSource; column++)
				this.pixels[column - xOffset][row] = this.pixels[column][row];
			for (int column = endColumnInSource + 1; column < this.size; column++)
				this.pixels[column][row] = 0;
		}
	}

	// shift a part of glyph by given segment
	private void shiftGlyphBySegment(GlyphSegment glyphSegment, int offset) {
		boolean randomBool = (Math.random() < 0.5) ? true : false;
		if (glyphSegment.isVertical)
			this.shiftGlyphVertically(glyphSegment.middleX(), offset,
					randomBool);
		else
			this.shiftGlyphHorizontally(glyphSegment.middleY(), offset,
					randomBool);
	}

	// remove a part of glyph by given segment
	private void removeGlyphBySegment(GlyphSegment glyphSegment) {
		if (glyphSegment.isVertical) {
			// decide whether to remove left part or right part
			int midX = glyphSegment.middleX();
			int toLeft = midX;
			int toRight = this.size - 1 - midX;
			if (toLeft < toRight) {
				for (int col = 0; col < midX; col++)
					for (int row = 0; row < this.size; row++)
						this.pixels[col][row] = 0;
			} else {
				for (int col = midX + 1; col < this.size; col++)
					for (int row = 0; row < this.size; row++)
						this.pixels[col][row] = 0;
			}
		} else {
			// decide whether to remove upper part or lower part
			int midY = glyphSegment.middleY();
			int toTop = midY;
			int toBottom = this.size - 1 - midY;
			if (toTop < toBottom) {
				for (int row = 0; row < midY; row++)
					for (int col = 0; col < this.size; col++)
						this.pixels[col][row] = 0;
			} else {
				for (int row = midY + 1; row < this.size; row++)
					for (int col = 0; col < this.size; col++)
						this.pixels[col][row] = 0;
			}
		}
	}

	private boolean segmentNearCenter(GlyphSegment glyphSegment) {
		// is near center if its middle line (horizontal or vertical)
		// lies within a quarter of glyph's center
		if (glyphSegment.isVertical) {
			int midX = glyphSegment.middleX();
			return (Math.abs(midX - this.size / 2) < this.size / 4);
		} else {
			int midY = glyphSegment.middleY();
			return (Math.abs(midY - this.size / 2) < this.size / 4);
		}
	}

	private int getRandom(int maxValue) {
		double val = Math.random();
		return (int) (Math.floor(val * maxValue));
	}

	private void shiftGlyphRandomly() {
		if (Math.random() < 0.5) {
			// choose vertical line
			int column = this.size / 4 + this.getRandom(this.size / 2);
			// choose offset 1..2
			int shiftAmount = 1 + this.getRandom(2);
			// choose side
			boolean toRight = (Math.random() < 0.5);
			this.shiftGlyphVertically(column, shiftAmount, toRight);
		} else {
			// choose horizontal line
			int row = this.size / 4 + this.getRandom(this.size / 2);
			// choose offset 1..2
			int shiftAmount = 1 + this.getRandom(2);
			// choose side
			boolean toUp = (Math.random() < 0.5);
			this.shiftGlyphHorizontally(row, shiftAmount, toUp);
		}
	}

	public MemoryGlyph generateRandomWarpedGlyph() {
		// If the glyph has no segments - warp by randomly choosing a vertical
		// or horizontal line in the vicinity of glyph's center and moving one
		// part
		// of the glyph by 1 or 2 pixels up-down or left-right

		// If the glyph has segments - the above is performed with 0.5
		// probability.
		// Otherwise - we choose randomly one of the segments. If this segment
		// lies
		// in the vicinity of glyph's center - the above is performed. Otherwise
		// -
		// the above is performed with 0.5 probability, while the other option
		// is
		// "throw away" the "outer" part of the glyph

		MemoryGlyph newGlyph = (MemoryGlyph) this.clone();

		if (this.segments == null) {
			newGlyph.shiftGlyphRandomly();
		} else {
			int segmentCount = newGlyph.segments.count();
			if (Math.random() < 0.5) {
				newGlyph.shiftGlyphRandomly();
			} else {
				// get index of random segment
				int segmIndex = this.getRandom(segmentCount);
				// get the segment itself
				GlyphSegment shiftSegment = newGlyph.segments.getNth(segmIndex);
				// check if near center
				if (this.segmentNearCenter(shiftSegment)) {
					newGlyph
							.shiftGlyphBySegment(shiftSegment, 1 + getRandom(2));
				} else {
					newGlyph.removeGlyphBySegment(shiftSegment);
				}
			}
		}

		newGlyph.computeMetrics();
		// compute glow cloud
		newGlyph.glowCloud = new PhosphoreCloud(newGlyph,
				IntroManager.GLOW_RADIUS);

		return newGlyph;
	}

	public void computeGlowCloud(int radius) {
		this.glowCloud = new PhosphoreCloud(this, radius);
	}

	private int getHighestPixel(int x, int toleranceLevel) {
		for (int y = this.top; y <= this.bottom; y++)
			if (pixels[x][y] >= toleranceLevel)
				return y;
		return -1;
	}

	private int getLowestPixel(int x, int toleranceLevel) {
		for (int y = this.bottom; y >= this.top; y--)
			if (pixels[x][y] >= toleranceLevel)
				return y;
		return -1;
	}

	public int getHighestRightSidePixel(int toleranceLevel) {
		int startX = this.right, endX = (this.left + this.right) / 2;
		for (int y = this.top; y <= this.bottom; y++) {
			for (int x = startX; x >= endX; x--) {
				if (this.pixels[x][y] >= toleranceLevel)
					return this.getHighestPixel(x, toleranceLevel);
			}
			endX = Math.max(endX - 1, this.left);
		}

		// if here - very not good
		return -1;
	}

	public int getHighestLeftSidePixel(int toleranceLevel) {
		int startX = this.left, endX = (this.left + this.right) / 2;
		for (int y = this.top; y <= this.bottom; y++) {
			for (int x = startX; x <= endX; x++) {
				if (this.pixels[x][y] >= toleranceLevel)
					return this.getHighestPixel(x, toleranceLevel);
			}
			endX = Math.min(endX + 1, this.right);
		}

		// if here - very not good
		return -1;
	}

	public int getLowestRightSidePixel(int toleranceLevel) {
		int startX = this.right, endX = (this.left + this.right) / 2;
		for (int y = this.bottom; y >= this.top; y--) {
			for (int x = startX; x >= endX; x--) {
				if (this.pixels[x][y] >= toleranceLevel)
					return this.getLowestPixel(x, toleranceLevel);
			}
			endX = Math.max(endX - 1, this.left);
		}

		// if here - very not good
		return -1;
	}

	public int getLowestLeftSidePixel(int toleranceLevel) {
		int startX = this.left, endX = (this.left + this.right) / 2;
		for (int y = this.bottom; y >= this.top; y--) {
			for (int x = startX; x <= endX; x++) {
				if (this.pixels[x][y] >= toleranceLevel) {
					return this.getLowestPixel(x, toleranceLevel);
				}
			}
			endX = Math.min(endX + 1, this.right);
		}

		// if here - very not good
		return -1;
	}

	public int getRightConnectorX(int y, int toleranceLevel) {
		// find first pixel from right above the tolerance level
		int firstAbove = -1;
		for (int x = this.right; x >= this.left; x--) {
			if (this.pixels[x][y] >= toleranceLevel) {
				firstAbove = x;
				break;
			}
		}

		if (firstAbove == -1)
			return -1;

		// go left until above the tolerance level
		for (int x = firstAbove - 1; x >= this.left; x--)
			if (this.pixels[x][y] < toleranceLevel)
				return (x + 1);

		// if here - the leftmost pixel is ours
		return this.left;
	}

	public int getLeftConnectorX(int y, int toleranceLevel) {
		// find first pixel from left above the tolerance level
		int firstAbove = -1;
		for (int x = this.left; x <= this.right; x++) {
			if (this.pixels[x][y] >= toleranceLevel) {
				firstAbove = x;
				break;
			}
		}

		if (firstAbove == -1)
			return -1;

		// go right until above the tolerance level
		for (int x = firstAbove + 1; x <= this.right; x++)
			if (this.pixels[x][y] < toleranceLevel)
				return (x - 1);

		// if here - the rightmost pixel is ours
		return this.right;
	}

	public MemoryGlyph createScaledUpVersion(double factor) {
		int newSize = (int) (Math.ceil(factor * (double) this.size));
		MemoryGlyph newGlyph = new MemoryGlyph(newSize);

		for (int col = 0; col < size; col++) {
			for (int row = 0; row < size; row++) {
				if (this.pixels[col][row] == 0)
					continue;

				// find all pixels in the new glyph that are affected by this
				// pixel
				double columnLeftInNew = (double) col * factor;
				double columnRightInNew = (double) (col + 1) * factor;
				double rowTopInNew = (double) row * factor;
				double rowBottomInNew = (double) (row + 1) * factor;

				int startCol = (int) Math.floor(columnLeftInNew);
				int endCol = (int) Math.ceil(columnRightInNew);
				int startRow = (int) Math.floor(rowTopInNew);
				int endRow = (int) Math.ceil(rowBottomInNew);

				for (int newCol = startCol; newCol < endCol; newCol++) {
					// column share
					double columnShare;
					if (newCol < (int) Math.ceil(columnLeftInNew)) {
						columnShare = (int) Math.ceil(columnLeftInNew)
								- columnLeftInNew;
					} else {
						if ((newCol + 1) > (int) Math.floor(columnRightInNew)) {
							columnShare = columnRightInNew
									- (int) Math.floor(columnRightInNew);
						} else
							columnShare = 1.0;
					}

					for (int newRow = startRow; newRow < endRow; newRow++) {
						// row share
						double rowShare;
						if (newRow < (int) Math.ceil(rowTopInNew)) {
							rowShare = (int) Math.ceil(rowTopInNew)
									- rowTopInNew;
						} else {
							if ((newRow + 1) > (int) Math.floor(rowBottomInNew)) {
								rowShare = rowBottomInNew
										- (int) Math.floor(rowBottomInNew);
							} else
								rowShare = 1.0;
						}

						newGlyph.pixels[newCol][newRow] += columnShare
								* rowShare * this.pixels[col][row];
					}
				}
			}
		}
		newGlyph.sharpen(1.0);
		newGlyph.computeMetrics();
		return newGlyph;
	}

	public final MemoryGlyph createScaledUpVersion2(
			Circle1PixelArbitraryIntersectorFactory iFct, double factor) {
		long time0 = System.currentTimeMillis();

		MemoryGlyph newGlyph = new MemoryGlyph();
		IndexBitmapObject oldPixels = new IndexBitmapObject(this.pixels,
				this.size, this.size);
		IndexBitmapObject newPixels = Phosphorizer.createScaledUpPixelVersion(
				oldPixels, iFct, factor);
		oldPixels = null;

		newGlyph.pixels = newPixels.getBitmap();
		newGlyph.size = newPixels.getWidth();

		newGlyph.sharpen(1.0);
		newGlyph.computeMetrics();
		long time4 = System.currentTimeMillis();

		delta2 += (time4 - time0);

		return newGlyph;
	}

	public final MemoryGlyph createScaledUpPhosphoreVersion(
			PhosphoreCloudFactory pcFct,
			Circle1PixelArbitraryIntersectorFactory iFct, double factor) {
		long time0 = System.currentTimeMillis();

		MemoryGlyph newGlyph = new MemoryGlyph();
		IndexBitmapObject oldPixels = new IndexBitmapObject(this.pixels,
				this.size, this.size);
		IndexBitmapObject newPixels = Phosphorizer
				.createScaledUpPhosphoreVersion(oldPixels, pcFct, iFct, factor,
						true);
		oldPixels = null;

		newGlyph.pixels = newPixels.getBitmap();
		newGlyph.size = newPixels.getWidth();

		newGlyph.sharpen(1.0);
		newGlyph.computeMetrics();
		long time4 = System.currentTimeMillis();

		delta2 += (time4 - time0);

		return newGlyph;
	}

}
