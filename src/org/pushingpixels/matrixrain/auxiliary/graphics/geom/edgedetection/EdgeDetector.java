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
package org.pushingpixels.matrixrain.auxiliary.graphics.geom.edgedetection;

import java.awt.Image;
import java.awt.Point;
import java.awt.image.PixelGrabber;
import java.util.Stack;

import org.pushingpixels.matrixrain.auxiliary.graphics.TrueColorBitmapObject;
import org.pushingpixels.matrixrain.auxiliary.math.GaussConvolution;


public class EdgeDetector {
	public static final int EDGES_STRONG = 0;

	public static final int EDGES_MEDIUM = 1;

	public static final int EDGES_SOFT = 2;

	private int width, height;

	private int[] imPixels;

	private short[][] bwImPixels;

	public EdgeDetector(int width, int height, Image im) {
		this(new TrueColorBitmapObject(im));

		this.width = width;
		this.height = height;

		this.imPixels = new int[height * width];
		PixelGrabber pg = new PixelGrabber(im, 0, 0, width, height,
				this.imPixels, 0, width);
		try {
			pg.grabPixels();
		} catch (InterruptedException e) {
			System.err.println("Everything not cool");
			return;
		}
		if ((pg.getStatus() & java.awt.image.ImageObserver.ABORT) != 0) {
			System.err.println("Everything not cool");
			return;
		}

		this.bwImPixels = new short[width][height];
		for (int i = 0; i < width; i++) {
			for (int j = 0; j < height; j++) {
				int currRGB = this.imPixels[width * j + i];
				int red = currRGB & 0x00FF0000;
				red >>>= 16;
				int green = currRGB & 0x0000FF00;
				green >>>= 8;
				int blue = currRGB & 0x000000FF;

				int bw = (222 * red + 707 * green + 71 * blue) / 1000;
				this.bwImPixels[i][j] = (short) bw;
			}
		}
	}

	public EdgeDetector(TrueColorBitmapObject bitmapObject) {
		this.width = bitmapObject.getWidth();
		this.height = bitmapObject.getHeight();

		int[][] bits = bitmapObject.getBitmap();
		this.bwImPixels = new short[width][height];
		for (int i = 0; i < width; i++) {
			for (int j = 0; j < height; j++) {
				int currRGB = bits[i][j];
				int red = currRGB & 0x00FF0000;
				red >>>= 16;
				int green = currRGB & 0x0000FF00;
				green >>>= 8;
				int blue = currRGB & 0x000000FF;

				int bw = (222 * red + 707 * green + 71 * blue) / 1000;
				this.bwImPixels[i][j] = (short) bw;
			}
		}
	}

	private int nmsSector(double theta) {
		double thetaD = theta * 180.0 / Math.PI;
		thetaD += 270;
		thetaD = thetaD % 360;
		if ((thetaD >= 337.5) || (thetaD < 22.5)
				|| ((thetaD >= 157.5) && (thetaD < 202.5)))
			return 0;
		if (((thetaD >= 22.5) && (thetaD < 67.5))
				|| ((thetaD >= 202.5) && (thetaD < 247.5)))
			return 1;
		if (((thetaD >= 67.5) && (thetaD < 112.5))
				|| ((thetaD >= 247.5) && (thetaD < 292.5)))
			return 2;
		if (((thetaD >= 112.5) && (thetaD < 157.5))
				|| ((thetaD >= 292.5) && (thetaD < 337.5)))
			return 3;
		return 0;
	}

	private short suppress(short[][] magnitudes, int sector, int i, int j,
			int lowThresh) {
		short curr = magnitudes[i][j];
		if (curr < lowThresh)
			return 0;
		switch (sector) {
		case 0:
			if ((magnitudes[i + 1][j] >= curr) || (magnitudes[i - 1][j] > curr))
				return 0;
			return curr;
		case 1:
			if ((magnitudes[i + 1][j + 1] >= curr)
					|| (magnitudes[i - 1][j - 1] > curr))
				return 0;
			return curr;
		case 2:
			if ((magnitudes[i][j + 1] >= curr) || (magnitudes[i][j - 1] > curr))
				return 0;
			return curr;
		case 3:
			if ((magnitudes[i + 1][j - 1] >= curr)
					|| (magnitudes[i - 1][j + 1] > curr))
				return 0;
			return curr;
		}
		return 0;
	}

	private void nms(short[][] magnitudes, double[][] orientations,
			int lowThresh) {
		// set first/last column
		for (int j = 0; j < this.height; j++) {
			magnitudes[this.width - 1][j] = 0;
			magnitudes[0][j] = 0;
		}
		// set first/last row
		for (int i = 0; i < this.width; i++) {
			magnitudes[i][this.height - 1] = 0;
			magnitudes[i][0] = 0;
		}

		// others
		for (int i = 1; i < (this.width - 1); i++) {
			for (int j = 1; j < (this.height - 1); j++) {
				magnitudes[i][j] = suppress(magnitudes,
						nmsSector(orientations[i][j]), i, j, lowThresh);
			}
		}
	}

	private void trackFromSinglePoint(short[][] magnitudes,
			boolean[][] tracked, int lowThresh, int x, int y) {

		// implement BFS with stack of points
		Stack to_track = new Stack();
		Point curr = new Point(x, y);
		// push the starting point
		to_track.push(curr);
		while (!to_track.empty()) {
			curr = (Point) to_track.pop();
			x = curr.x;
			y = curr.y;

			// check if already tracked
			if (tracked[x][y])
				continue;

			// go over all neighbours
			for (int dx = -1; dx <= 1; dx++) {
				for (int dy = -1; dy <= 1; dy++) {
					// check if the same point
					if ((dx == 0) && (dy == 0))
						continue;
					// check if valid coordinates
					int newX = x + dx;
					if ((newX < 0) || (newX >= this.width))
						continue;
					int newY = y + dy;
					if ((newY < 0) || (newY >= this.height))
						continue;

					if (magnitudes[newX][newY] > lowThresh)
						to_track.push(new Point(newX, newY));
				}
			}
			tracked[x][y] = true;
		}
	}

	private int[] applyCannyAlgorithm(int highThresh, int lowThresh) {
		// resulting value-map
		int[] valueMap = new int[this.width * this.height];
		for (int i = 0; i < this.width * this.height; i++)
			valueMap[i] = 0;

		long time0 = System.currentTimeMillis();
		// 1. Smooth the image with a gaussian filter
		GaussConvolution gaussConvolution = new GaussConvolution(1.0, 2);
		short[][] smoothedPixels = gaussConvolution.getSmoothedBitmap(
				this.bwImPixels, this.width, this.height);

		// long time1 = System.currentTimeMillis();
		// 2. Compute gradient magnitude and orientation
		int maxP = 0, maxQ = 0;
		for (int i = 0; i < (this.width - 1); i++) {
			for (int j = 0; j < (this.height - 1); j++) {
				int p = (smoothedPixels[i][j + 1] - smoothedPixels[i][j]
						+ smoothedPixels[i + 1][j + 1] - smoothedPixels[i + 1][j]);
				if (p < 0)
					p = -p;
				if (maxP < p)
					maxP = p;
				int q = (smoothedPixels[i][j] - smoothedPixels[i + 1][j]
						+ smoothedPixels[i][j + 1] - smoothedPixels[i + 1][j + 1]);
				if (q < 0)
					q = -q;
				if (maxQ < q)
					maxQ = q;
			}
		}

		int N = (maxP > maxQ) ? maxP : maxQ;
		int N2 = 2 * N + 1;
		double[][] atans = new double[N2][N2];
		short[][] sqrs = new short[N2][N2];
		for (int i = 0; i < N2; i++) {
			for (int j = 0; j < N2; j++) {
				double p = (double) (i - N) / 2.0;
				double q = (double) (j - N) / 2.0;
				sqrs[i][j] = (short) Math.sqrt(p * p + q * q);
				atans[i][j] = Math.atan2(q, p);
			}
		}

		short magnitudes[][] = new short[this.width][this.height];
		double theta[][] = new double[this.width][this.height];
		for (int i = 0; i < (this.width - 1); i++) {
			for (int j = 0; j < (this.height - 1); j++) {
				/*
				 * double p = 0.5*(smoothedPixels[i][j+1]-smoothedPixels[i][j]+
				 * smoothedPixels[i+1][j+1]-smoothedPixels[i+1][j]); double q =
				 * 0.5*(smoothedPixels[i][j]-smoothedPixels[i+1][j]+
				 * smoothedPixels[i][j+1]-smoothedPixels[i+1][j+1]);
				 * magnitudes[i][j] = (short)Math.sqrt(p*p+q*q); theta[i][j] =
				 * Math.atan2(q, p);
				 */
				int p2 = (smoothedPixels[i][j + 1] - smoothedPixels[i][j]
						+ smoothedPixels[i + 1][j + 1] - smoothedPixels[i + 1][j]);
				int q2 = (smoothedPixels[i][j] - smoothedPixels[i + 1][j]
						+ smoothedPixels[i][j + 1] - smoothedPixels[i + 1][j + 1]);
				magnitudes[i][j] = sqrs[p2 + N][q2 + N];
				theta[i][j] = atans[p2 + N][q2 + N];
			}
		}

		// long time2 = System.currentTimeMillis();
		// 3. Non maxima suppression
		nms(magnitudes, theta, lowThresh);

		// long time3 = System.currentTimeMillis();
		// 4. Thresholding
		boolean tracked[][] = new boolean[this.width][this.height];
		for (int i = 0; i < this.width; i++) {
			for (int j = 0; j < this.height; j++) {
				tracked[i][j] = false;
				if (magnitudes[i][j] < lowThresh)
					magnitudes[i][j] = 0;
			}
		}
		// track from all points which are above the high threshold
		for (int i = 0; i < this.width; i++) {
			for (int j = 0; j < this.height; j++) {
				if ((magnitudes[i][j] >= highThresh) && (!tracked[i][j]))
					trackFromSinglePoint(magnitudes, tracked, lowThresh, i, j);
			}
		}
		// nullify all unmarked points
		for (int i = 0; i < this.width; i++) {
			for (int j = 0; j < this.height; j++) {
				if (!tracked[i][j])
					magnitudes[i][j] = 0;
			}
		}

		// long time4 = System.currentTimeMillis();
		// resulting value-map
		for (int i = 0; i < this.width - 1; i++) {
			for (int j = 0; j < this.height - 1; j++) {
				int value = magnitudes[i][j];
				if (value > 0)
					value = Math.min(255, (int) (100 + 3 * value));
				valueMap[j * this.width + i] = value;
			}
		}
		long time5 = System.currentTimeMillis();

		// System.out.print("smooth: " + (time1-time0));
		// System.out.print(" gradient: " + (time2-time1));
		// System.out.print(" nms: " + (time3-time2));
		// System.out.print(" track: " + (time4-time3));
		// System.out.println(" result: " + (time5-time4));
		System.out.println("Edge detector: " + (time5 - time0));

		return valueMap;
	}

	public int[] getValueMap(int kind) {
		switch (kind) {
		case EdgeDetector.EDGES_STRONG:
			return applyCannyAlgorithm(60, 30);
		case EdgeDetector.EDGES_MEDIUM:
			return applyCannyAlgorithm(50, 25);
		case EdgeDetector.EDGES_SOFT:
			return applyCannyAlgorithm(40, 20);
		default:
			return null;
		}
	}

	public int[][] getValueMap2D(int kind) {
		int[] values1D = getValueMap(kind);
		int[][] result = new int[this.width][this.height];
		for (int i = 0; i < width; i++)
			for (int j = 0; j < height; j++)
				result[i][j] = values1D[j * width + i];
		return result;
	}

	public int[] getOriginalPixels() {
		return this.imPixels;
	}
}
