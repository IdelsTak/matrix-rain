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
package org.pushingpixels.matrixrain.auxiliary.graphics.geom.voronoi;

import org.pushingpixels.matrixrain.auxiliary.graphics.TrueColorBitmapObject;
import org.pushingpixels.matrixrain.auxiliary.math.coord.Point2D;

public final class VoronoiManager {
	public static TrueColorBitmapObject getVoronoiDiagram(int width,
			int height, int averageDistanceBetweenCenters) {

		long time0 = System.currentTimeMillis();

		// allocate and initialize z-buffer
		int pixelsLeft = width * height;
		int[][] zBuffer = new int[width][height];
		for (int i = 0; i < width; i++)
			for (int j = 0; j < height; j++)
				zBuffer[i][j] = -1;

		// choose center points at random in each cell
		int widthInCells = (int) (Math.ceil(width
				/ averageDistanceBetweenCenters)) + 1;
		int heightInCells = (int) (Math.ceil(height
				/ averageDistanceBetweenCenters)) + 1;

		Point2D[] centers = new Point2D[widthInCells * heightInCells];
		int centersCount = 0;
		for (int i = 0; i < widthInCells; i++) {
			for (int j = 0; j < heightInCells; j++) {
				int cellLeftPixel = i * averageDistanceBetweenCenters;
				int cellTopPixel = j * averageDistanceBetweenCenters;
				int offsetX = (int) (Math.random() * averageDistanceBetweenCenters);
				int offsetY = (int) (Math.random() * averageDistanceBetweenCenters);
				int pointX = cellLeftPixel + offsetX;
				int pointY = cellTopPixel + offsetY;
				if ((pointX < width) && (pointY < height)) {
					centers[centersCount] = new Point2D(pointX, pointY);
					zBuffer[pointX][pointY] = centersCount;
					centersCount++;
					pixelsLeft--;
				}
			}
		}

		// allocate colors
		int[] colors = new int[centersCount];
		int minColor = 16;
		int colorDelta = (255 - minColor)
				/ ((int) Math.floor(Math.pow(centersCount, 1.0 / 3.0)));
		int currR = 255, currG = 255, currB = 255;
		for (int i = 0; i < centersCount; i++) {
			// System.out.println("color: " + currR + ", " + currG + ", " +
			// currB);
			colors[i] = (255 << 24) | (currR << 16) | (currG << 8) | currB;
			currB -= colorDelta;
			if (currB < minColor) {
				currB = 255;
				currG -= colorDelta;
				if (currG < minColor) {
					currG = 255;
					currR -= colorDelta;
				}
			}
		}

		// one iteration of voronoi
		// System.out.println(pixelsLeft + " unmarked left");
		int prevRad = 0;
		int currRad = 1;
		while (pixelsLeft > 0) {
			int currRad2 = currRad * currRad;
			int prevRad2 = prevRad * prevRad;
			for (int currCenter = 0; currCenter < centersCount; currCenter++) {
				int centerX = (int) centers[currCenter].getX();
				int centerY = (int) centers[currCenter].getY();
				for (int dx = -currRad; dx <= currRad; dx++) {
					for (int dy = -currRad; dy <= currRad; dy++) {
						int d2 = dx * dx + dy * dy;
						if (d2 <= prevRad2) {
							// marked during previous iteration
							continue;
						}
						if (d2 > currRad2) {
							// no need to mark
							continue;
						}
						// check x and y
						int x = centerX + dx;
						if ((x < 0) || (x >= width))
							continue;
						int y = centerY + dy;
						if ((y < 0) || (y >= height))
							continue;
						if (zBuffer[x][y] >= 0) {
							// already marked by another color
							continue;
						}
						// mark
						zBuffer[x][y] = currCenter;
						pixelsLeft--;
					}
				}
			}
			// System.out.println("after " + currRad + " we have " + pixelsLeft
			// + " unmarked left");
			prevRad++;
			currRad++;
		}

		int[][] trueColorBitmap = new int[width][height];
		for (int i = 0; i < width; i++) {
			for (int j = 0; j < height; j++) {
				if (zBuffer[i][j] < 0)
					trueColorBitmap[i][j] = 0xFF000000;
				else {
					trueColorBitmap[i][j] = colors[zBuffer[i][j]];
				}
			}
		}
		for (int i = 0; i < centersCount; i++) {
			int centerX = (int) centers[i].getX();
			int centerY = (int) centers[i].getY();
			if ((centerX < width) && (centerY < height))
				trueColorBitmap[centerX][centerY] = 0xFF000000;
		}

		TrueColorBitmapObject resultBitmap = new TrueColorBitmapObject(
				trueColorBitmap, width, height);

		long time1 = System.currentTimeMillis();
		System.out.println("Voronoi: " + (time1 - time0) + " (" + currRad
				+ " iterations)");

		return resultBitmap;
	}

	public static VoronoiIndexDiagramInfo getVoronoiIndexDiagram(int width,
			int height, int averageDistanceBetweenCenters) {

		long time0 = System.currentTimeMillis();

		// allocate and initialize z-buffer
		int pixelsLeft = width * height;
		int[][] zBuffer = new int[width][height];
		for (int i = 0; i < width; i++)
			for (int j = 0; j < height; j++)
				zBuffer[i][j] = -1;

		// choose center points at random in each cell
		int widthInCells = (int) (Math.ceil(width
				/ averageDistanceBetweenCenters)) + 1;
		int heightInCells = (int) (Math.ceil(height
				/ averageDistanceBetweenCenters)) + 1;

		Point2D[] centers = new Point2D[widthInCells * heightInCells];
		int centersCount = 0;
		for (int i = 0; i < widthInCells; i++) {
			for (int j = 0; j < heightInCells; j++) {
				int cellLeftPixel = i * averageDistanceBetweenCenters;
				int cellTopPixel = j * averageDistanceBetweenCenters;
				int offsetX = (int) (Math.random() * averageDistanceBetweenCenters);
				int offsetY = (int) (Math.random() * averageDistanceBetweenCenters);
				int pointX = cellLeftPixel + offsetX;
				int pointY = cellTopPixel + offsetY;
				if ((pointX < width) && (pointY < height)) {
					centers[centersCount] = new Point2D(pointX, pointY);
					zBuffer[pointX][pointY] = centersCount;
					centersCount++;
					pixelsLeft--;
				}
			}
		}

		// one iteration of voronoi
		// System.out.println(pixelsLeft + " unmarked left");
		int prevRad = 0;
		int currRad = 1;
		while (pixelsLeft > 0) {
			int currRad2 = currRad * currRad;
			int prevRad2 = prevRad * prevRad;
			for (int currCenter = 0; currCenter < centersCount; currCenter++) {
				int centerX = (int) centers[currCenter].getX();
				int centerY = (int) centers[currCenter].getY();
				for (int dx = -currRad; dx <= currRad; dx++) {
					for (int dy = -currRad; dy <= currRad; dy++) {
						int d2 = dx * dx + dy * dy;
						if (d2 <= prevRad2) {
							// marked during previous iteration
							continue;
						}
						if (d2 > currRad2) {
							// no need to mark
							continue;
						}
						// check x and y
						int x = centerX + dx;
						if ((x < 0) || (x >= width))
							continue;
						int y = centerY + dy;
						if ((y < 0) || (y >= height))
							continue;
						if (zBuffer[x][y] >= 0) {
							// already marked by another color
							continue;
						}
						// mark
						zBuffer[x][y] = currCenter;
						pixelsLeft--;
					}
				}
			}
			// System.out.println("after " + currRad + " we have " + pixelsLeft
			// + " unmarked left");
			prevRad++;
			currRad++;
		}

		// sanity check
		for (int i = 0; i < width; i++)
			for (int j = 0; j < height; j++)
				if (zBuffer[i][j] == -1)
					System.err.println("unexpected error in voronoi");

		long time1 = System.currentTimeMillis();
		System.out.println("Voronoi: " + (time1 - time0) + " (" + currRad
				+ " iterations)");

		VoronoiIndexDiagramInfo resultStruct = new VoronoiIndexDiagramInfo(
				width, height, zBuffer, centers, currRad, centersCount);
		return resultStruct;
	}
}
