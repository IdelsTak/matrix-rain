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
package org.pushingpixels.matrixrain.auxiliary.graphics;

import java.awt.Color;

import org.pushingpixels.matrixrain.auxiliary.graphics.colors.interpolator.ColorInterpolator;
import org.pushingpixels.matrixrain.auxiliary.graphics.colors.manager.ColorManager;
import org.pushingpixels.matrixrain.auxiliary.math.GaussValues;
import org.pushingpixels.matrixrain.auxiliary.math.coord.Point2D;
import org.pushingpixels.matrixrain.auxiliary.math.coord.Polygon2D;
import org.pushingpixels.matrixrain.auxiliary.math.intersect.ClippingManager;
import org.pushingpixels.matrixrain.font.GlyphFactory;
import org.pushingpixels.matrixrain.font.MemoryGlyph;
import org.pushingpixels.matrixrain.phosphore.PhosphoreCloud;

public class TrueColorBitmapManager {
	protected int width, height;

	protected int totalCount;

	// off-screen image
	protected int[] bitmap;

	// for lines
	int[][] xTmp;

	int[][] yTmp;

	int[][] iTmp;

	public TrueColorBitmapManager(int width, int height) {
		this.width = width;
		this.height = height;
		this.totalCount = this.height * this.width;
		this.bitmap = new int[width * height];

		this.xTmp = new int[this.width + 2][3];
		this.yTmp = new int[this.width + 2][3];
		this.iTmp = new int[this.width + 2][3];
	}

	public void resetImage() {
		for (int i = 0; i < this.totalCount; i++) {
			this.bitmap[i] = 0xFF000000;
		}
	}

	public void resetImage(int rgba) {
		for (int i = 0; i < this.totalCount; i++) {
			this.bitmap[i] = rgba;
		}
	}

	public void resetImageToTransparent() {
		for (int i = 0; i < this.totalCount; i++) {
			this.bitmap[i] = 0x00000000;
		}
	}

	public void fillRect(int x, int y, int width, int height, int rgb) {
		for (int i = x; i < (x + width); i++) {
			for (int j = y; j < (y + height); j++) {
				this.overwritePixel(i, j, rgb);
			}
		}
	}

	public void drawRect(int x, int y, int width, int height, int rgb) {
		for (int i = x; i < (x + width); i++) {
			this.overwritePixel(i, y, rgb);
			this.overwritePixel(i, y + height - 1, rgb);
		}
		for (int j = y; j < (y + height); j++) {
			this.overwritePixel(x, j, rgb);
			this.overwritePixel(x + width - 1, j, rgb);
		}
	}

	public void drawLineVertical(int x, int y1, int y2, int rgb) {
		for (int j = y1; j <= y2; j++) {
			this.overwritePixel(x, j, rgb);
		}
	}

	protected void paintPixel(int index, int RGBvalue) {
		int oldValue = this.bitmap[index];

		int oldR = (oldValue & 0x00FF0000) >> 16;
		int oldG = (oldValue & 0x0000FF00) >> 8;
		int oldB = (oldValue & 0x000000FF);

		int newR = (RGBvalue & 0x00FF0000) >> 16;
		int newG = (RGBvalue & 0x0000FF00) >> 8;
		int newB = (RGBvalue & 0x000000FF);

		int resR = (newR > oldR) ? newR : oldR;
		int resG = (newG > oldG) ? newG : oldG;
		int resB = (newB > oldB) ? newB : oldB;

		int newValue = (255 << 24) | (resR << 16) | (resG << 8) | resB;

		this.bitmap[index] = newValue;
	}

	protected void paintPixel(int index, int RGBvalue, double intensity) {
		int oldValue = this.bitmap[index];

		int oldR = (oldValue & 0x00FF0000) >> 16;
		int oldG = (oldValue & 0x0000FF00) >> 8;
		int oldB = (oldValue & 0x000000FF);

		int newR = (RGBvalue & 0x00FF0000) >> 16;
		int newG = (RGBvalue & 0x0000FF00) >> 8;
		int newB = (RGBvalue & 0x000000FF);

		newR = (int) (intensity * newR);
		newG = (int) (intensity * newG);
		newB = (int) (intensity * newB);

		int resR = (newR > oldR) ? newR : oldR;
		int resG = (newG > oldG) ? newG : oldG;
		int resB = (newB > oldB) ? newB : oldB;

		int newValue = (255 << 24) | (resR << 16) | (resG << 8) | resB;

		this.bitmap[index] = newValue;
	}

	// intensity in 0..255
	protected void overlayPixel(int index, int newR, int newG, int newB,
			int intensity) {
		if (intensity == 0)
			return;

		int oldValue = this.bitmap[index];

		// System.out.print("at (" + (index%this.width) + ", " +
		// (index/this.width) + ") ");

		int oldR = (oldValue & 0x00FF0000) >> 16;
		int oldG = (oldValue & 0x0000FF00) >> 8;
		int oldB = (oldValue & 0x000000FF);
		double oldIntensity = ((oldValue & 0xFF000000) >>> 24) / 255.0;

		// System.out.print(", intens was " + oldIntensity);

		// System.out.print(", new intens " + newIntensityAbs);

		double newIntensity = intensity / 255.0;

		double totalIntensity = oldIntensity + newIntensity;
		if (totalIntensity > 1.0) {
			oldIntensity = 1.0 - newIntensity;
			totalIntensity = 1.0;
		}

		int resR = (int) ((oldR * oldIntensity + newR * newIntensity) / totalIntensity);
		int resG = (int) ((oldG * oldIntensity + newG * newIntensity) / totalIntensity);
		int resB = (int) ((oldB * oldIntensity + newB * newIntensity) / totalIntensity);

		int totalIntensityAbs = (int) (256.0 * totalIntensity);
		if (totalIntensityAbs > 255)
			totalIntensityAbs = 255;

		int newValue = (totalIntensityAbs << 24) | (resR << 16) | (resG << 8)
				| resB;

		this.bitmap[index] = newValue;
	}

	protected void overlayPixel(int index, int RGBAvalue) {
		int newIntensity = (RGBAvalue & 0xFF000000) >>> 24;
		int newR = (RGBAvalue & 0x00FF0000) >> 16;
		int newG = (RGBAvalue & 0x0000FF00) >> 8;
		int newB = (RGBAvalue & 0x000000FF);

		this.overlayPixel(index, newR, newG, newB, newIntensity);
	}

	protected void overlayPixel(int x, int y, int r, int g, int b, int intensity) {
		if ((x < 0) || (x >= this.width))
			return;
		if ((y < 0) || (y >= this.height))
			return;
		this.overlayPixel(y * this.width + x, r, g, b, intensity);
	}

	public void overlayPixel(int x, int y, int RGBAvalue) {
		if ((x < 0) || (x >= this.width))
			return;
		if ((y < 0) || (y >= this.height))
			return;
		this.overlayPixel(y * this.width + x, RGBAvalue);
	}

	protected void blendPixel(int index, int RGBAvalue, double alphaCoef) {
		int oldValue = this.bitmap[index];

		// System.out.print("at (" + (index%this.width) + ", " +
		// (index/this.width) + ") ");

		int oldR = (oldValue & 0x00FF0000) >> 16;
		int oldG = (oldValue & 0x0000FF00) >> 8;
		int oldB = (oldValue & 0x000000FF);

		double oldIntensity = ((oldValue & 0xFF000000) >>> 24) / 255.0;

		// System.out.print(", intens was " + oldIntensity);

		int newIntensityAbs = (RGBAvalue & 0xFF000000) >>> 24;

		if (newIntensityAbs == 0) {
			// System.out.println();
			return;
		}

		double newIntensity = newIntensityAbs / 255.0;
		// System.out.print(", new intens " + newIntensity);

		int newR = (RGBAvalue & 0x00FF0000) >> 16;
		int newG = (RGBAvalue & 0x0000FF00) >> 8;
		int newB = (RGBAvalue & 0x000000FF);

		// alphaCoef:
		// oldIntensity - (0.0..0.5)->oldIntensity, 1.0->0.0
		// newIntensity - 0.0->0.0, (0.5..1.0)->newIntensity
		if (alphaCoef > 0.5)
			oldIntensity *= (2.0 - 2.0 * alphaCoef);
		if (alphaCoef < 0.5)
			newIntensity *= (alphaCoef / 0.5);
		// System.out.print("old: " + oldIntensity);
		// System.out.print(", new " + newIntensity);

		double totalIntensity = oldIntensity + newIntensity;

		int resR = (int) ((oldR * oldIntensity + newR * newIntensity) / totalIntensity);
		int resG = (int) ((oldG * oldIntensity + newG * newIntensity) / totalIntensity);
		int resB = (int) ((oldB * oldIntensity + newB * newIntensity) / totalIntensity);

		int totalIntensityAbs = (int) (256.0 * totalIntensity);
		if (totalIntensityAbs > 255)
			totalIntensityAbs = 255;

		// System.out.println("IRGB: (" + totalIntensityAbs + ", " + resR + ", "
		// +
		// resG + ", " + resB + ")");
		int newValue = (totalIntensityAbs << 24) | (resR << 16) | (resG << 8)
				| resB;

		this.bitmap[index] = newValue;
	}

	// alpha 0.0-1.0
	public void blendPixel(int x, int y, int RGBAvalue, double alphaCoef) {
		if ((x < 0) || (x >= this.width))
			return;
		if ((y < 0) || (y >= this.height))
			return;
		this.blendPixel(y * this.width + x, RGBAvalue, alphaCoef);
	}

	protected void overwritePixel(int index, int RGBAvalue) {
		if ((index < 0) || (index >= this.totalCount))
			return;
		this.bitmap[index] = RGBAvalue;
	}

	public void paintPixel(int x, int y, int RGBvalue) {
		if ((x < 0) || (x >= this.width))
			return;
		if ((y < 0) || (y >= this.height))
			return;
		this.paintPixel(y * this.width + x, RGBvalue);
	}

	public void paintPixel(int x, int y, int RGBvalue, double intensity) {
		if ((x < 0) || (x >= this.width))
			return;
		if ((y < 0) || (y >= this.height))
			return;
		this.paintPixel(y * this.width + x, RGBvalue, intensity);
	}

	public void overwritePixel(int x, int y, int RGBvalue) {
		if ((x < 0) || (x >= this.width))
			return;
		if ((y < 0) || (y >= this.height))
			return;
		this.overwritePixel(y * this.width + x, RGBvalue);
	}

	public void blendTrueColorObject(TrueColorBitmapObject bitmapObject,
			double alphaCoef) {
		int[][] pixels = bitmapObject.getBitmap();
		for (int i = 0; i < this.width; i++)
			for (int j = 0; j < this.height; j++)
				this.blendPixel(j * this.width + i, pixels[i][j], alphaCoef);
	}

	public void blendTrueColorObject(TrueColorBitmapObject bitmap,
			int destinationLeft, int destinationTop, double alphaCoef) {

		int[][] pixels = bitmap.getBitmap();
		int width = bitmap.getWidth();
		int height = bitmap.getHeight();
		for (int i = 0; i < width; i++) {
			for (int j = 0; j < height; j++) {
				this.blendPixel(destinationLeft + i, destinationTop + j,
						pixels[i][j], alphaCoef);
			}
		}
	}

	public void overwriteTrueColorObject(TrueColorBitmapObject bitmapObject) {
		int[][] pixels = bitmapObject.getBitmap();
		for (int i = 0; i < this.width; i++)
			for (int j = 0; j < this.height; j++)
				this.overwritePixel(j * this.width + i, pixels[i][j]);
	}

	public void overwriteTrueColorObject(TrueColorBitmapObject bitmap,
			int destinationLeft, int destinationTop) {

		int[][] pixels = bitmap.getBitmap();
		int width = bitmap.getWidth();
		int height = bitmap.getHeight();
		for (int i = 0; i < width; i++) {
			for (int j = 0; j < height; j++) {
				this.overwritePixel(destinationLeft + i, destinationTop + j,
						pixels[i][j]);
			}
		}
	}

	public void overlayTrueColorObject(TrueColorBitmapObject bitmapObject) {
		int[][] pixels = bitmapObject.getBitmap();
		for (int i = 0; i < this.width; i++)
			for (int j = 0; j < this.height; j++)
				this.overlayPixel(j * this.width + i, pixels[i][j]);
	}

	public void overlayTrueColorObject(TrueColorBitmapObject bitmap,
			int destinationLeft, int destinationTop) {

		int[][] pixels = bitmap.getBitmap();
		int width = bitmap.getWidth();
		int height = bitmap.getHeight();
		for (int i = 0; i < width; i++) {
			for (int j = 0; j < height; j++) {
				this.overlayPixel(destinationLeft + i, destinationTop + j,
						pixels[i][j]);
			}
		}
	}

	public void paintTrueColorObject(TrueColorBitmapObject bitmapObject) {
		int[][] pixels = bitmapObject.getBitmap();
		for (int i = 0; i < this.width; i++)
			for (int j = 0; j < this.height; j++)
				this.paintPixel(j * this.width + i, pixels[i][j]);
	}

	public void paintTrueColorObject(TrueColorBitmapObject bitmap,
			int destinationLeft, int destinationTop) {

		int[][] pixels = bitmap.getBitmap();
		int width = bitmap.getWidth();
		int height = bitmap.getHeight();
		for (int i = 0; i < width; i++) {
			for (int j = 0; j < height; j++) {
				this.paintPixel(destinationLeft + i, destinationTop + j,
						pixels[i][j]);
			}
		}
	}

	public void paintTrueColorObjectExact(TrueColorBitmapObject bitmap) {
		int[][] pixels = bitmap.getBitmap();
		int width = bitmap.getWidth();
		int height = bitmap.getHeight();
		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				this.overwritePixel(y * this.width + x, pixels[x][y]);
			}
		}
	}

	public void paintTrueColorObject(TrueColorBitmapObject bitmap,
			int sourceLeft, int sourceTop, int sourceRight, int sourceBottom,
			int destinationLeft, int destinationTop) {
		/*
		 * System.out.println("Painting bitmap (" + bitmap.getWidth() + "*" +
		 * bitmap.getHeight() + ")'s ( " + sourceLeft + "," + sourceTop + ")-(" +
		 * sourceRight + "," + sourceBottom + ") at (" + destinationLeft + "," +
		 * destinationTop + ")");
		 */
		int[][] pixels = bitmap.getBitmap();
		int cx = sourceRight - sourceLeft;
		int cy = sourceBottom - sourceTop;
		for (int i = 0; i < cx; i++) {
			for (int j = 0; j < cy; j++) {
				this.overwritePixel(destinationLeft + i, destinationTop + j,
						pixels[sourceLeft + i][sourceTop + j]);
			}
		}
	}

	public void overwriteBitmap1D(int[] newBitmap) {
		int len = newBitmap.length;
		for (int i = 0; i < len; i++)
			this.bitmap[i] = newBitmap[i];
	}

	public synchronized int[] getBitmap1D() {
		return this.bitmap;
	}

	public synchronized int[][] getBitmap2D() {
		int[][] result = new int[this.width][this.height];
		for (int i = 0; i < this.width; i++)
			for (int j = 0; j < this.height; j++)
				result[i][j] = this.bitmap[j * this.width + i];
		return result;
	}

	public synchronized TrueColorBitmapObject getBitmapObject() {
		return new TrueColorBitmapObject(this.getBitmap2D(), this.width,
				this.height);
	}

	protected void paintGlyph(MemoryGlyph glyph, int glyphBaselineY,
			int destinationLeft, int destinationTop, Color color) {

		int dx = glyph.getLeft();
		int dy = glyphBaselineY;

		for (int i = glyph.getLeft(); i <= glyph.getRight(); i++) {
			for (int j = 0; j < glyph.getSize(); j++) {
				if (glyph.getPixel(i, j) > 0) {
					this.overlayPixel(destinationLeft + i - dx, destinationTop
							+ j - dy, color.getRGB());
				}
			}
		}
	}

	public void paintGlyph(MemoryGlyph glyph, int glyphBaselineY,
			int destinationLeft, int destinationTop,
			ColorInterpolator glyphInterpolator, boolean toInvertInterpolation) {

		int dx = glyph.getLeft();
		int dy = glyphBaselineY;

		for (int i = glyph.getLeft(); i <= glyph.getRight(); i++) {
			for (int j = 0; j < glyph.getSize(); j++) {
				if (glyph.getPixel(i, j) > 0) {
					double alpha = (double) glyph.getPixel(i, j) / 255.0;
					Color color = toInvertInterpolation ? glyphInterpolator
							.getInterpolatedColor(alpha) : glyphInterpolator
							.getInterpolatedColor(1.0 - alpha);
					this.blendPixel(destinationLeft + i - dx, destinationTop
							+ j - dy, color.getRGB(), alpha);
				}
			}
		}
	}

	public void paintGlyphCloud(MemoryGlyph glyph, int glyphBaselineY,
			int destinationLeft, int destinationTop,
			ColorInterpolator cloudInterpolator, boolean toInvertInterpolation) {

		int dx = glyph.getLeft();
		int dy = glyphBaselineY;

		// glow cloud
		if (cloudInterpolator != null) {
			PhosphoreCloud glowCloud = glyph.getGlowCloud();
			if (glowCloud != null) {
				int ddx = glowCloud.getRefPointX();
				int ddy = glowCloud.getRefPointY();
				for (int i = 0; i < glowCloud.getWidth(); i++) {
					for (int j = 0; j < glowCloud.getHeight(); j++) {
						double radiation = glowCloud.getDoubleRadiation(i, j);
						if (radiation < 0.1)
							continue;
						Color glowColor = toInvertInterpolation ? cloudInterpolator
								.getInterpolatedColor(radiation)
								: cloudInterpolator
										.getInterpolatedColor(1.0 - radiation);
						double alpha = toInvertInterpolation ? radiation
								: (0.5 * radiation);
						this.blendPixel(destinationLeft + i - dx - ddx,
								destinationTop + j - dy - ddy, glowColor
										.getRGB(), alpha);
					}
				}
			}
		}
	}

	public void paintHollowRawLine(GlyphFactory glyphFactory, char[] letters,
			int[] xPositions, int bottomY, ColorManager colorManager,
			boolean mainIsDark) {

		int baselineY = glyphFactory.getBaselineY();
		if (!mainIsDark) {
			for (int j = 0; j < letters.length; j++) {
				MemoryGlyph glyph = glyphFactory.getGlyph((int) letters[j], 0,
						0, 0);
				this.paintGlyphCloud(glyph, baselineY, xPositions[j], bottomY,
						colorManager.getColorInterpolatorDark(), false);
			}
			for (int j = 0; j < letters.length; j++) {
				MemoryGlyph glyph = glyphFactory.getGlyph((int) letters[j], 0,
						0, 0);
				this.paintGlyph(glyph, baselineY, xPositions[j], bottomY,
						colorManager.getColorInterpolatorLight(), true);
			}
		} else {
			for (int j = 0; j < letters.length; j++) {
				MemoryGlyph glyph = glyphFactory.getGlyph((int) letters[j], 0,
						0, 0);
				this.paintGlyphCloud(glyph, baselineY, xPositions[j], bottomY,
						colorManager.getColorInterpolatorLight(), true);
			}
			for (int j = 0; j < letters.length; j++) {
				MemoryGlyph glyph = glyphFactory.getGlyph((int) letters[j], 0,
						0, 0);
				this.paintGlyph(glyph, baselineY, xPositions[j], bottomY,
						colorManager.getColorInterpolatorDark(), false);
			}
		}
	}

	public void paintHollowRawLine(GlyphFactory glyphFactory, String letters,
			int startX, int bottomY, ColorManager colorManager,
			boolean mainIsDark) {

		char[] chars = new char[letters.length()];
		int[] xPos = new int[letters.length()];
		int currX = startX;
		for (int i = 0; i < letters.length(); i++) {
			chars[i] = letters.charAt(i);
			xPos[i] = currX;
			int glyphWidth = glyphFactory.charWidth(chars[i], 0, 0, 0);
			// System.out.println("Glyph '" + chars[i] + "' width is " +
			// glyphWidth);
			currX += (glyphWidth + 1);
		}

		this.paintHollowRawLine(glyphFactory, chars, xPos, bottomY,
				colorManager, mainIsDark);
		chars = null;
		xPos = null;
	}

	public void paintString(GlyphFactory glyphFactory, String string,
			int destinationLeft, int destinationTop, Color color,
			ColorInterpolator glowCloudInterpolator,
			boolean toInvertInterpolation) {

		int currX = destinationLeft;
		if (glowCloudInterpolator != null) {
			for (int j = 0; j < string.length(); j++) {
				char c = string.charAt(j);
				MemoryGlyph glyph = glyphFactory.getGlyph((int) c, 0, 0, 0);
				this.paintGlyphCloud(glyph, glyphFactory.getBaselineY(), currX,
						destinationTop, glowCloudInterpolator,
						toInvertInterpolation);
				currX += (glyphFactory.charWidth(c, 0, 0, 0) + 1);
			}
		}
		currX = destinationLeft;
		for (int j = 0; j < string.length(); j++) {
			char c = string.charAt(j);
			MemoryGlyph glyph = glyphFactory.getGlyph((int) c, 0, 0, 0);
			this.paintGlyph(glyph, glyphFactory.getBaselineY(), currX,
					destinationTop, color);
			currX += (glyphFactory.charWidth(c, 0, 0, 0) + 1);
		}
	}

	public void paintChar(GlyphFactory glyphFactory, char c,
			int destinationLeft, int destinationTop, Color color,
			ColorInterpolator glowCloudInterpolator,
			boolean toInvertInterpolation) {

		int currX = destinationLeft;
		MemoryGlyph glyph = glyphFactory.getGlyph((int) c, 0, 0, 0);
		this.paintGlyphCloud(glyph, glyphFactory.getBaselineY(),
				destinationLeft, destinationTop, glowCloudInterpolator,
				toInvertInterpolation);
		this.paintGlyph(glyph, glyphFactory.getBaselineY(), destinationLeft,
				destinationTop, color);
	}

	public void paintString(GlyphFactory glyphFactory, String string,
			int destinationLeft, int destinationTop,
			ColorInterpolator glyphInterpolator,
			ColorInterpolator glowCloudInterpolator, boolean mainIsDark) {

		int currX = destinationLeft;
		for (int j = 0; j < string.length(); j++) {
			char c = string.charAt(j);
			MemoryGlyph glyph = glyphFactory.getGlyph((int) c, 0, 0, 0);
			this.paintGlyphCloud(glyph, glyphFactory.getBaselineY(), currX,
					destinationTop, glowCloudInterpolator, !mainIsDark);
			currX += (glyphFactory.charWidth(c, 0, 0, 0) + 1);
		}
		currX = destinationLeft;
		for (int j = 0; j < string.length(); j++) {
			char c = string.charAt(j);
			MemoryGlyph glyph = glyphFactory.getGlyph((int) c, 0, 0, 0);
			currX += (glyphFactory.charWidth(c, 0, 0, 0) + 1);
			this.paintGlyph(glyph, glyphFactory.getBaselineY(), currX,
					destinationTop, glyphInterpolator, mainIsDark);
			currX += (glyphFactory.charWidth(c, 0, 0, 0) + 1);
		}
	}

	public void paintChar(GlyphFactory glyphFactory, char c,
			int destinationLeft, int destinationTop,
			ColorInterpolator glyphInterpolator,
			ColorInterpolator glowCloudInterpolator, boolean mainIsDark) {

		int currX = destinationLeft;
		MemoryGlyph glyph = glyphFactory.getGlyph((int) c, 0, 0, 0);
		this.paintGlyphCloud(glyph, glyphFactory.getBaselineY(),
				destinationLeft, destinationTop, glowCloudInterpolator,
				!mainIsDark);
		this.paintGlyph(glyph, glyphFactory.getBaselineY(), destinationLeft,
				destinationTop, glyphInterpolator, mainIsDark);
	}

	protected void basicAntialiasedLineMidpoint(int x1, int y1, int x2, int y2,
			int[][] yArray, int[][] intensityArray) {
		int dx, dy, incr_e, incr_ne, d, x, y, two_v_dx;
		double inv_denom, two_dx_inv_denom;
		dx = x2 - x1;
		dy = y2 - y1;
		if ((dx >= 0) && (dy >= 0) && (dx >= dy)) {
			d = 2 * dy - dx;
			incr_e = 2 * dy;
			incr_ne = 2 * (dy - dx);
			two_v_dx = 0;
			inv_denom = 1 / (2.0 * Math.sqrt(dx * dx + dy * dy));
			two_dx_inv_denom = 2 * dx * inv_denom;
			x = x1;
			y = y1;
			yArray[0][0] = y;
			yArray[0][1] = y - 1;
			yArray[0][2] = y + 1;
			intensityArray[0][0] = GaussValues.getIntensity(0);
			intensityArray[0][1] = GaussValues.getIntensity(Math
					.abs(two_dx_inv_denom));
			intensityArray[0][2] = GaussValues.getIntensity(Math
					.abs(two_dx_inv_denom));

			while (x <= x2) {
				if (d < 0) {
					two_v_dx = d + dx;
					d = d + incr_e;
					x = x + 1;
				} else {
					two_v_dx = d - dx;
					d = d + incr_ne;
					x = x + 1;
					y = y + 1;
				}

				yArray[x - x1][0] = y;
				yArray[x - x1][1] = y + 1;
				yArray[x - x1][2] = y - 1;
				intensityArray[x - x1][0] = GaussValues.getIntensity(Math
						.abs(two_v_dx * inv_denom));
				intensityArray[x - x1][1] = GaussValues.getIntensity(Math
						.abs(two_dx_inv_denom - two_v_dx * inv_denom));
				intensityArray[x - x1][2] = GaussValues.getIntensity(Math
						.abs(two_dx_inv_denom + two_v_dx * inv_denom));
			}
		}
	}

	protected int computeAntialiasedLineMidpoint(int x1, int y1, int x2,
			int y2, int[][] xArray, int[][] yArray, int[][] intensityArray) {
		// 8 cases, which differ by line slope and direction
		int dx = x2 - x1;
		int dy = y2 - y1;
		int tx, ty, count;
		if (dx >= 0) {
			if (dy >= 0) {
				if (dx >= dy) {
					this.basicAntialiasedLineMidpoint(0, 0, dx, dy, yArray,
							intensityArray);
					for (tx = 0; tx <= dx; tx++) {
						xArray[tx][0] = xArray[tx][1] = xArray[tx][2] = x1 + tx;
						yArray[tx][0] += y1;
						yArray[tx][1] += y1;
						yArray[tx][2] += y1;
					}
					return dx + 1;
				} else {
					this.basicAntialiasedLineMidpoint(0, 0, dy, dx, yArray,
							intensityArray);
					// swap x and y values
					for (ty = 0; ty <= dy; ty++) {
						xArray[ty][0] = x1 + yArray[ty][0];
						xArray[ty][1] = x1 + yArray[ty][1];
						xArray[ty][2] = x1 + yArray[ty][2];
						yArray[ty][0] = yArray[ty][1] = yArray[ty][2] = y1 + ty;
					}
					return dy + 1;
				}
			} else {
				// mirror line around x-parallel line through first point
				count = this.computeAntialiasedLineMidpoint(x1, y1, x2, 2 * y1
						- y2, xArray, yArray, intensityArray);
				for (tx = 0; tx < count; tx++) {
					yArray[tx][0] -= 2 * (yArray[tx][0] - y1);
					yArray[tx][1] -= 2 * (yArray[tx][1] - y1);
					yArray[tx][2] -= 2 * (yArray[tx][2] - y1);
				}
				return count;
			}
		} else {
			// compute line in the other direction
			count = this.computeAntialiasedLineMidpoint(x2, y2, x1, y1, xArray,
					yArray, intensityArray);
			return count;
		}
	}

	public void drawLine(int x1, int y1, int x2, int y2, int rgb) {
		int count = this.computeAntialiasedLineMidpoint(x1, y1, x2, y2,
				this.xTmp, this.yTmp, this.iTmp);
		int r = (rgb & 0x00FF0000) >> 16;
		int g = (rgb & 0x0000FF00) >> 8;
		int b = (rgb & 0x000000FF);
		for (int i = 0; i < count; i++) {
			this.overlayPixel(this.xTmp[i][0], this.yTmp[i][0], r, g, b,
					this.iTmp[i][0]);
			this.overlayPixel(this.xTmp[i][1], this.yTmp[i][1], r, g, b,
					this.iTmp[i][1]);
			this.overlayPixel(this.xTmp[i][2], this.yTmp[i][2], r, g, b,
					this.iTmp[i][2]);
		}
	}

	public void blendLine(int x1, int y1, int x2, int y2, int rgba,
			double alphaCoef) {
		int count = this.computeAntialiasedLineMidpoint(x1, y1, x2, y2,
				this.xTmp, this.yTmp, this.iTmp);
		for (int i = 0; i < count; i++) {
			this.blendPixel(this.xTmp[i][0], this.yTmp[i][0], rgba, alphaCoef
					* this.iTmp[i][0] / 255.0);// this.iTmp[i][0]*alphaCoef);
			this.blendPixel(this.xTmp[i][1], this.yTmp[i][1], rgba, alphaCoef
					* this.iTmp[i][1] / 255.0);// this.iTmp[i][1]*alphaCoef);
			this.blendPixel(this.xTmp[i][2], this.yTmp[i][2], rgba, alphaCoef
					* this.iTmp[i][2] / 255.0);// this.iTmp[i][2]*alphaCoef);
		}
	}

	public void blendPolygon(Polygon2D polygon, int offsetX, int offsetY,
			int rgba, double alphaCoef) {

		if (polygon == null)
			return;

		Point2D[] points = polygon.getPoints();
		int count = polygon.getPointsCount();
		for (int i = 0; i < (count - 1); i++)
			this.blendLine((int) points[i].getX() + offsetX, (int) points[i]
					.getY()
					+ offsetY, (int) points[i + 1].getX() + offsetX,
					(int) points[i + 1].getY() + offsetY, rgba, alphaCoef);
		if (count > 2)
			this.blendLine((int) points[count - 1].getX() + offsetX,
					(int) points[count - 1].getY() + offsetY, (int) points[0]
							.getX()
							+ offsetX, (int) points[0].getY() + offsetY, rgba,
					alphaCoef);
	}

	public void blendFillPolygon(Polygon2D polygon, int offsetX, int offsetY,
			int rgba, double alphaCoef) {

		if (polygon == null)
			return;
		int count = polygon.getPointsCount();
		if (count < 2)
			return;

		Point2D[] points = polygon.getPoints();

		double minX = points[0].getX();
		double maxX = points[0].getX();
		double minY = points[0].getY();
		double maxY = points[0].getY();
		for (int i = 1; i < count; i++) {
			double x = points[i].getX();
			double y = points[i].getY();
			if (x < minX)
				minX = x;
			if (x > maxX)
				maxX = x;
			if (y < minY)
				minY = y;
			if (y > maxY)
				maxY = y;
		}

		int width = (int) maxX + 1 - (int) minX + 1;
		int height = (int) maxY + 1 - (int) minY + 1;
		int dx = (int) minX - 1;
		int dy = (int) minY - 1;
		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				double area = ClippingManager.intersectionArea(points,
						new Point2D(x + dx, y + dy), new Point2D(x + dx + 1, y
								+ dy + 1));
				if (area > 0.0) {
					// System.out.println("Blending " + (x+dx) + "*" + (y+dy) +
					// " with " + alphaCoef*area);
					this.blendPixel(x + dx + offsetX, y + dy + offsetY, rgba,
							alphaCoef * area);
				}
			}
		}
	}
}
