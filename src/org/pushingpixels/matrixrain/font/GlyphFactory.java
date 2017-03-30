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

import java.awt.FontMetrics;
import java.awt.Image;
import java.awt.image.PixelGrabber;

public class GlyphFactory {
	private MemoryGlyph[][][][] glyphs;

	private MemoryGlyph[][] miniGlyphs;

	private int[][][][] weightIndices;

	private int glyphMapWidth, glyphMapHeight;

	private int glyphSize;

	private FontMetrics fontMetrics;

	private int spaceSize;

	private int baselineY;

	private int glyphCount;

	private int[] glyphMapPixels;

	private int[] sizeArray;

	private int maxSize, maxBlur, maxRadiance;

	private int[] miniSizeArray;

	private int maxMiniSize;

	private RadianceFactory radianceFactory;

	public GlyphFactory(Image glyphMapImage, int glyphSize,
			FontMetrics fontMetrics) {

		this.glyphSize = glyphSize;

		this.fontMetrics = fontMetrics;
		if (this.fontMetrics != null) {
			this.spaceSize = fontMetrics.charWidth(' ');
			this.baselineY = fontMetrics.getAscent();
		} else {
			this.spaceSize = 0;
			this.baselineY = 0;
		}
		this.glyphMapWidth = glyphMapImage.getWidth(null);
		this.glyphMapHeight = glyphMapImage.getHeight(null);

		this.glyphMapPixels = new int[glyphMapHeight * glyphMapWidth];
		PixelGrabber pg = new PixelGrabber(glyphMapImage, 0, 0, glyphMapWidth,
				glyphMapHeight, glyphMapPixels, 0, glyphMapWidth);
		try {
			pg.grabPixels();
		} catch (InterruptedException e) {
			System.err.println("Everything not cool");
		}
		if ((pg.getStatus() & java.awt.image.ImageObserver.ABORT) != 0) {
			System.err.println("Everything not cool");
			return;
		}
		pg = null;

		glyphCount = (glyphMapWidth / glyphSize) * (glyphMapHeight / glyphSize);
	}

	public int getGlyphCount() {
		return glyphCount;
	}

	public int getSizeByIndex(int sizeIndex) {
		if (sizeIndex == 0)
			return this.glyphSize;
		return this.sizeArray[sizeIndex - 1];
	}

	public int getMiniSizeByIndex(int miniSizeIndex) {
		return this.miniSizeArray[miniSizeIndex];
	}

	public int getSizeCount() {
		return this.maxSize;
	}

	public int getMiniSizeCount() {
		return this.maxMiniSize;
	}

	public int getBlurCount() {
		return this.maxBlur;
	}

	public int getRadianceCount() {
		return this.maxRadiance;
	}

	private void readOriginalGlyphs(boolean toMirrorGlyphs) {
		int gColumns = glyphMapWidth / glyphSize;
		int gRows = glyphMapHeight / glyphSize;
		for (int gCol = 0; gCol < gColumns; gCol++) {
			for (int gRow = 0; gRow < gRows; gRow++) {
				int glyphIndex = gRow * gColumns + gCol;
				// System.out.println("Reading glyph " + glyphIndex + " from " +
				// gRow + ", " + gCol);
				glyphs[glyphIndex][0][0][0] = new MemoryGlyph(glyphSize);
				// get all pixels of this glyph
				int startX = glyphSize * gCol;
				int endX = glyphSize * (gCol + 1);
				int startY = glyphSize * gRow;
				int endY = glyphSize * (gRow + 1);
				for (int x = startX; x < endX; x++) {
					for (int y = startY; y < endY; y++) {
						if (toMirrorGlyphs) {
							// glyphs[glyphIndex][0][0][0].setPixel(glyphSize-1-(x-startX),
							// y-startY, mf.getPixel(x, y));
							glyphs[glyphIndex][0][0][0].setPixel(glyphSize - 1
									- (x - startX), y - startY,
									this.glyphMapPixels[y * this.glyphMapWidth
											+ x] & 0x000000FF);
						} else {
							// glyphs[glyphIndex][0][0][0].setPixel(x-startX,
							// y-startY, mf.getPixel(x, y));
							glyphs[glyphIndex][0][0][0].setPixel(x - startX, y
									- startY, this.glyphMapPixels[y
									* this.glyphMapWidth + x] & 0x000000FF);
						}
					}
				}
				glyphs[glyphIndex][0][0][0].computeMetrics();
			}
		}
	}

	private MemoryGlyph createScaledVersion(int glyphIndex, int size) {
		MemoryGlyph newGlyph = new MemoryGlyph(size);

		// this variable is the scale-down factor
		float factor = (float) size / (float) this.glyphSize;

		for (int col = 0; col < size; col++) {
			for (int row = 0; row < size; row++) {
				// find all pixels in original glyph that contribute to this
				// scaled-
				// down pixel
				double columnLeftInOriginal = (double) col / factor;
				double columnRightInOriginal = (double) (col + 1) / factor;
				double rowTopInOriginal = (double) row / factor;
				double rowBottomInOriginal = (double) (row + 1) / factor;

				int origLeft = Math.max((int) Math.floor(columnLeftInOriginal),
						0);
				int origRight = Math.min(
						(int) Math.ceil(columnRightInOriginal),
						this.glyphSize - 1);
				int origTop = Math.max((int) Math.floor(rowTopInOriginal), 0);
				int origBottom = Math.min((int) Math.ceil(rowBottomInOriginal),
						this.glyphSize - 1);

				double finalValue = 0.0;
				for (int origCol = origLeft; origCol < origRight; origCol++) {
					// column share
					double columnShare;
					if (origCol < columnLeftInOriginal) {
						columnShare = 1.0 - (columnLeftInOriginal - origCol);
					} else {
						if ((origCol + 1) > columnRightInOriginal) {
							columnShare = columnRightInOriginal - origCol;
						} else
							columnShare = 1.0;
					}
					for (int origRow = origTop; origRow < origBottom; origRow++) {
						// row share
						double rowShare;
						if (origRow < rowTopInOriginal) {
							rowShare = 1.0 - (rowTopInOriginal - origRow);
						} else {
							if ((origRow + 1) > rowBottomInOriginal) {
								rowShare = rowBottomInOriginal - origRow;
							} else
								rowShare = 1.0;
						}

						finalValue += columnShare
								* rowShare
								* glyphs[glyphIndex][0][0][0].getPixel(origCol,
										origRow);
					}
				}
				finalValue *= (factor * factor);
				newGlyph.setPixel(col, row, (int) finalValue);
			}
		}
		// newGlyph.sharpen(0.8);
		newGlyph.computeMetrics();
		return newGlyph;
	}

	private void createScaledGlyphs() {
		for (int glyphIndex = 0; glyphIndex < this.glyphCount; glyphIndex++)
			for (int sizeIndex = 1; sizeIndex <= this.maxSize; sizeIndex++)
				this.glyphs[glyphIndex][sizeIndex][0][0] = this
						.createScaledVersion(glyphIndex,
								this.sizeArray[sizeIndex - 1]);
	}

	private void createMiniGlyphs() {
		for (int glyphIndex = 0; glyphIndex < this.glyphCount; glyphIndex++)
			for (int miniSizeIndex = 0; miniSizeIndex < this.maxMiniSize; miniSizeIndex++)
				this.miniGlyphs[glyphIndex][miniSizeIndex] = this
						.createScaledVersion(glyphIndex,
								this.miniSizeArray[miniSizeIndex]);
	}

	private void createBlurredGlyph(int glyphIndex, int sizeIndex, int blur) {
		int size = this.getSizeByIndex(sizeIndex);

		// take average over a square of pixels
		glyphs[glyphIndex][sizeIndex][blur][0] = new MemoryGlyph(size);

		for (int col = 0; col < size; col++) {
			for (int row = 0; row < size; row++) {
				int origLeft = Math.max(col - blur, 0);
				int origRight = Math.min(col + blur, size - 1);
				int origTop = Math.max(row - blur, 0);
				int origBottom = Math.min(row + blur, size - 1);

				double finalValue = 0.0;
				int pixelCount = 0;
				for (int origCol = origLeft; origCol <= origRight; origCol++) {
					for (int origRow = origTop; origRow <= origBottom; origRow++) {
						int manhattanDistance = Math.abs(origCol - col)
								+ Math.abs(origRow - row);
						if (manhattanDistance <= blur) {
							finalValue += glyphs[glyphIndex][sizeIndex][0][0]
									.getPixel(origCol, origRow);
							pixelCount++;
						}
					}
				}

				finalValue /= pixelCount;
				glyphs[glyphIndex][sizeIndex][blur][0].setPixel(col, row,
						(int) finalValue);
			}
		}
		glyphs[glyphIndex][sizeIndex][blur][0].sharpen(1.0);
		glyphs[glyphIndex][sizeIndex][blur][0].computeMetrics();
	}

	private void createBlurredGlyphs() {
		for (int glyphIndex = 0; glyphIndex < this.glyphCount; glyphIndex++)
			for (int sizeIndex = 0; sizeIndex <= this.maxSize; sizeIndex++)
				for (int blur = 1; blur <= this.maxBlur; blur++)
					this.createBlurredGlyph(glyphIndex, sizeIndex, blur);
	}

	private void createRadiantGlyph(int glyphIndex, int sizeIndex, int blur,
			int radiance) {
		int size = this.getSizeByIndex(sizeIndex);

		// replicate each pixel over a fade-out square
		MemoryGlyph newGlyph = new MemoryGlyph(size);
		for (int col = 0; col < size; col++) {
			for (int row = 0; row < size; row++) {
				// each pixel in original glyph contributes to a square of
				// pixel in radiant glyph

				int origLeft = col - radiance;
				if (origLeft < 0)
					origLeft = 0;
				int origRight = col + radiance;
				if (origRight >= size)
					origRight = size - 1;
				int origTop = row - radiance;
				if (origTop < 0)
					origTop = 0;
				int origBottom = row + radiance;
				if (origBottom >= size)
					origBottom = size - 1;

				int oldValue = glyphs[glyphIndex][sizeIndex][blur][0].getPixel(
						col, row);
				if (oldValue == 0)
					continue;
				newGlyph.addToPixel(col, row, oldValue);
				for (int newCol = origLeft; newCol <= origRight; newCol++) {
					for (int newRow = origTop; newRow <= origBottom; newRow++) {
						newGlyph.addToPixel(newCol, newRow,
								this.radianceFactory.getFactor(oldValue,
										radiance, newCol - col, newRow - row));
					}
				}
			}
		}

		newGlyph.normalize();
		newGlyph.computeMetrics();
		glyphs[glyphIndex][sizeIndex][blur][radiance] = newGlyph;
	}

	private void createRadiantGlyphs() {
		this.radianceFactory = new RadianceFactory();
		for (int glyphIndex = 0; glyphIndex < this.glyphCount; glyphIndex++)
			for (int sizeIndex = 0; sizeIndex <= this.maxSize; sizeIndex++)
				for (int blur = 0; blur <= this.maxBlur; blur++)
					for (int radiance = 1; radiance <= this.maxRadiance; radiance++)
						this.createRadiantGlyph(glyphIndex, sizeIndex, blur,
								radiance);
	}

	public void createGlyphs(int[] sizeArray, int maxBlur, int maxRadiance,
			boolean toMirrorGlyphs) {
		this.sizeArray = sizeArray;
		this.maxBlur = maxBlur;
		this.maxRadiance = maxRadiance;

		this.maxSize = sizeArray.length;
		// allocate glyph array and weight array
		this.glyphs = new MemoryGlyph[glyphCount][maxSize + 1][maxBlur + 1][maxRadiance + 1];
		this.weightIndices = new int[glyphCount][maxSize + 1][maxBlur + 1][maxRadiance + 1];

		// read original glyphs
		this.readOriginalGlyphs(toMirrorGlyphs);
		this.createScaledGlyphs();
		this.createBlurredGlyphs();
		this.createRadiantGlyphs();

		for (int sizeIndex = 0; sizeIndex <= this.maxSize; sizeIndex++)
			for (int blurIndex = 0; blurIndex <= this.maxBlur; blurIndex++)
				for (int radianceIndex = 0; radianceIndex <= this.maxRadiance; radianceIndex++)
					this.computeWeightIndices(sizeIndex, blurIndex,
							radianceIndex);
		// System.out.println("glyph read " + (time1-time0));
		// System.out.println("glyph scale " + (time2-time1));
		// System.out.println("glyph blur " + (time3-time2));
		// System.out.println("glyph radiant " + (time4-time3));
		// System.out.println("glyph weight " + (time5-time4));

		// System.out.println("Glyph total: " + (time5-time0));
	}

	public void createMiniGlyphs(int[] miniSizeArray) {
		this.miniSizeArray = miniSizeArray;

		this.maxMiniSize = miniSizeArray.length;
		// allocate miniglyph array and weight array
		this.miniGlyphs = new MemoryGlyph[glyphCount][this.maxMiniSize];

		// scale mini glyphs
		this.createMiniGlyphs();

		// System.out.println("miniglyph " + (time1-time0));
	}

	/*
	 * public void computeGlyphSegments() { // Compute bitmap part that
	 * corresponds to each glyph. // Work (for the time being) only on original
	 * sized, unblurred, unradiating // glyphs.
	 * 
	 * EdgeDetector edgeDetector = new EdgeDetector(this.applet, this.mf); int[]
	 * edgesValueMap = edgeDetector.getValueMap();
	 * 
	 * int gColumns = glyphMapWidth/glyphSize; int gRows =
	 * glyphMapHeight/glyphSize; for (int gRow=0; gRow<gRows; gRow++) { for
	 * (int gCol=0; gCol<gColumns; gCol++) { int glyphIndex =
	 * gRow*gColumns+gCol; int[][] glyphEdgeBitmap = new
	 * int[glyphSize][glyphSize]; for (int x=0; x<glyphSize; x++) { for (int
	 * y=0; y<glyphSize; y++) { // the row in edgesBitmap is gRow*glyphSize+y //
	 * the col in edgesBitmap is gCol*glyphSize+x int origRow =
	 * gRow*glyphSize+y; int origCol = gCol*glyphSize+x; glyphEdgeBitmap[x][y] =
	 * edgesValueMap[origRow*glyphMapWidth+origCol]; } }
	 * this.glyphs[glyphIndex][0][0][0].computeSegments(); } } }
	 */
	public void computeGlyphSegments() {
		// Compute bitmap part that corresponds to each glyph.
		// Work (for the time being) only on original sized, unblurred,
		// unradiating
		// glyphs.

		for (int glyphIndex = 0; glyphIndex < this.glyphCount; glyphIndex++) {
			for (int sizeIndex = 0; sizeIndex <= this.maxSize; sizeIndex++) {
				for (int blurIndex = 0; blurIndex <= this.maxBlur; blurIndex++) {
					for (int radianceIndex = 0; radianceIndex <= this.maxRadiance; radianceIndex++) {
						this.glyphs[glyphIndex][sizeIndex][blurIndex][radianceIndex]
								.computeSegments();
						// this.glyphs[glyphIndex][sizeIndex][blurIndex][radianceIndex].generateRandomWarpedGlyph();
					}
				}
			}
		}
		// System.out.println("glyph warp " + (time1-time0));
	}

	public MemoryGlyph rewarpGlyph(int glyphIndex, int sizeIndex,
			int blurIndex, int radianceIndex) {
		return this.glyphs[glyphIndex][sizeIndex][blurIndex][radianceIndex]
				.generateRandomWarpedGlyph();
	}

	private void computeWeightIndices(int sizeIndex, int blur, int radiance) {
		int ind1 = sizeIndex, ind2 = blur, ind3 = radiance;
		// implement simple look up. At every iteration compute the smallest
		// weight not taken until now until all the glyphs are sorted

		int[] weights = new int[this.glyphCount];
		for (int i = 0; i < this.glyphCount; i++)
			weights[i] = this.glyphs[i][ind1][ind2][ind3].getWeight();
		// compute maximal weight
		int maxWeight = 0;
		for (int i = 0; i < this.glyphCount; i++)
			maxWeight = Math.max(maxWeight, weights[i]);

		int currIndex = 0;
		int glyphsLeft = this.glyphCount;
		int prevMinimalWeight = -1;
		while (glyphsLeft > 0) {
			// compute minimal weight not taken
			int currMinimalWeight = maxWeight;
			for (int i = 0; i < this.glyphCount; i++)
				if (weights[i] > prevMinimalWeight)
					currMinimalWeight = Math.min(currMinimalWeight, weights[i]);
			// take all glyphs of this weight
			for (int i = 0; i < this.glyphCount; i++) {
				if (weights[i] == currMinimalWeight) {
					this.weightIndices[currIndex++][ind1][ind2][ind3] = i;
					glyphsLeft--;
				}
			}
			prevMinimalWeight = currMinimalWeight;
		}
		weights = null;
	}

	public MemoryGlyph getGlyph(int index, int sizeIndex, int blur, int radiance) {
		return this.glyphs[index][sizeIndex][blur][radiance];
	}

	public MemoryGlyph getMiniGlyph(int index, int miniSizeIndex) {
		return this.miniGlyphs[index][miniSizeIndex];
	}

	public MemoryGlyph getGlyphByWeight(int weightIndex, int sizeIndex,
			int blur, int radiance) {
		int glyphIndex = this.weightIndices[weightIndex][sizeIndex][blur][radiance];
		return glyphs[glyphIndex][sizeIndex][blur][radiance];
	}

	public int getGlyphIndexByWeightGroup(int tenthIndex, int sizeIndex,
			int blur, int radiance) {
		// draw a random index in this tenth and return the corresponding glyph
		// index
		int tenthSize = this.glyphCount / 10;
		int weightIndex = 10 * (tenthIndex - 1)
				+ (int) (Math.floor(Math.random() * tenthSize));
		int glyphIndex = this.weightIndices[weightIndex][sizeIndex][blur][radiance];
		return glyphIndex;
	}

	public MemoryGlyph getGlyphByWeightGroup(int tenthIndex, int sizeIndex,
			int blur, int radiance) {
		// draw a random index in this tenth and return the corresponding glyph
		int tenthSize = this.glyphCount / 10;
		int weightIndex = 10 * (tenthIndex - 1)
				+ (int) (Math.floor(Math.random() * tenthSize));
		int glyphIndex = this.weightIndices[weightIndex][sizeIndex][blur][radiance];
		return glyphs[glyphIndex][sizeIndex][blur][radiance];
	}

	public int getGlyphWidth(int index, int sizeIndex, int blur, int radiance) {
		return glyphs[index][sizeIndex][blur][radiance].getWidth();
	}

	public void computeGlowClouds(int sizeIndex, int blur, int radiance,
			int glowRadius) {
		for (int i = 0; i < this.glyphCount; i++)
			this.glyphs[i][sizeIndex][blur][radiance]
					.computeGlowCloud(glowRadius);
	}

	public int getOriginalSize() {
		return this.glyphSize;
	}

	public int getSpaceSize() {
		return this.spaceSize;
	}

	public int getOrigHeight() {
		return this.fontMetrics.getHeight();
	}

	/*
	 * public int stringWidth(int[] indices, int sizeIndex, int blur, int
	 * radiance) { int result = 0; int len = indices.length; for (int i=0; i<len;
	 * i++) result += glyphs[indices[i]][sizeIndex][blur][radiance].getWidth(); //
	 * add 1-pixel gap between glyphs result += (len-1); return result; }
	 */
	public int stringWidth(String str, int sizeIndex, int blur, int radiance) {
		int result = 0;
		// System.out.println("*** For '" + str + "':");
		int len = str.length();
		for (int i = 0; i < len; i++) {
			char c = str.charAt(i);
			if (c == ' ') {
				result += this.spaceSize;
			} else {
				// System.out.println(" character '" + c + "' - width " +
				// glyphs[(int)c][sizeIndex][blur][radiance].getWidth());
				result += glyphs[(int) c][sizeIndex][blur][radiance].getWidth();
			}
		}
		// add 1-pixel gap between glyphs
		result += (len - 1);

		// System.out.println("Width of '" + str + "' is " + result);
		return result;
	}

	public int charWidth(char c, int sizeIndex, int blur, int radiance) {
		if (c == ' ')
			return this.spaceSize;
		else
			return this.glyphs[(int) c][sizeIndex][blur][radiance].getWidth();
	}

	public int getBaselineY() {
		return this.baselineY;
	}
}
