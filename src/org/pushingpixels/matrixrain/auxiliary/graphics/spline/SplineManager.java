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
package org.pushingpixels.matrixrain.auxiliary.graphics.spline;

import java.awt.Point;

public class SplineManager {
	protected static double[][] bSplineMatrix = {
			{ -1.0 / 6.0, +3.0 / 6.0, -3.0 / 6.0, +1.0 / 6.0 },
			{ +3.0 / 6.0, -6.0 / 6.0, +3.0 / 6.0, +0.0 / 6.0 },
			{ -3.0 / 6.0, +0.0 / 6.0, +3.0 / 6.0, +0.0 / 6.0 },
			{ +1.0 / 6.0, +4.0 / 6.0, +1.0 / 6.0, +0.0 / 6.0 } };

	protected static double[][] catmullRomMatrix = {
			{ -1.0 / 2.0, +3.0 / 2.0, -3.0 / 2.0, +1.0 / 2.0 },
			{ +2.0 / 2.0, -5.0 / 2.0, +4.0 / 2.0, -1.0 / 2.0 },
			{ -1.0 / 2.0, +0.0 / 2.0, +1.0 / 2.0, +0.0 / 2.0 },
			{ +0.0 / 2.0, +2.0 / 2.0, +0.0 / 2.0, +0.0 / 2.0 } };

	public SplineManager() {
	}

	private static void createSingleInterpolation(double[] values, int minKey,
			int maxKey, double[][] coefs, Point[] points, int index) {
		int ind1, ind2, ind3, ind4;
		ind4 = index;
		ind3 = index - 1;
		ind2 = index - 2;
		ind1 = index - 3;
		if (ind1 < 0)
			ind1 = 0;
		if (ind2 < 0)
			ind2 = 0;
		if (ind3 < 0)
			ind3 = 0;
		if (ind1 >= points.length)
			ind1 = points.length - 1;
		if (ind2 >= points.length)
			ind2 = points.length - 1;
		if (ind3 >= points.length)
			ind3 = points.length - 1;
		if (ind4 >= points.length)
			ind4 = points.length - 1;

		Point p1 = points[ind1];
		Point p2 = points[ind2];
		Point p3 = points[ind3];
		Point p4 = points[ind4];

		double ax = coefs[0][0] * p1.x + coefs[0][1] * p2.x + coefs[0][2]
				* p3.x + coefs[0][3] * p4.x;
		double bx = coefs[1][0] * p1.x + coefs[1][1] * p2.x + coefs[1][2]
				* p3.x + coefs[1][3] * p4.x;
		double cx = coefs[2][0] * p1.x + coefs[2][1] * p2.x + coefs[2][2]
				* p3.x + coefs[2][3] * p4.x;
		double dx = coefs[3][0] * p1.x + coefs[3][1] * p2.x + coefs[3][2]
				* p3.x + coefs[3][3] * p4.x;

		double ay = coefs[0][0] * p1.y + coefs[0][1] * p2.y + coefs[0][2]
				* p3.y + coefs[0][3] * p4.y;
		double by = coefs[1][0] * p1.y + coefs[1][1] * p2.y + coefs[1][2]
				* p3.y + coefs[1][3] * p4.y;
		double cy = coefs[2][0] * p1.y + coefs[2][1] * p2.y + coefs[2][2]
				* p3.y + coefs[2][3] * p4.y;
		double dy = coefs[3][0] * p1.y + coefs[3][1] * p2.y + coefs[3][2]
				* p3.y + coefs[3][3] * p4.y;

		/*
		 * for (double t=0.0; t<=1.0; t+=0.01) { double xt =
		 * ax*t*t*t+bx*t*t+cx*t+dx; double yt = ay*t*t*t+by*t*t+cy*t+dy;
		 * this.imageManager.paintPixel((int)xt, (int)yt, 250); }
		 */

		// 0 <= t <= 1
		double delta = 0.005;
		double x0 = dx;
		double dx0 = ax * delta * delta * delta + bx * delta * delta + cx
				* delta;
		double d2x0 = 6.0 * ax * delta * delta * delta + 2.0 * bx * delta
				* delta;
		double d3x0 = 6.0 * ax * delta * delta * delta;

		double y0 = dy;
		double dy0 = ay * delta * delta * delta + by * delta * delta + cy
				* delta;
		double d2y0 = 6.0 * ay * delta * delta * delta + 2.0 * by * delta
				* delta;
		double d3y0 = 6.0 * ay * delta * delta * delta;

		double currT = 0.0;
		double xt = x0, dxt = dx0, d2xt = d2x0;
		double yt = y0, dyt = dy0, d2yt = d2y0;
		int prevKey = (int) (xt - 1);
		while (currT <= 1.0) {
			int currKey = (int) xt;
			if ((currKey >= minKey) && (currKey <= maxKey)) {
				if (currKey != prevKey) {
					// System.out.println("Setting at " + (currKey-minKey) + "
					// value of " + yt);
					values[currKey - minKey] = yt;
					prevKey = currKey;
				}
			}

			xt += dxt;
			dxt += d2xt;
			d2xt += d3x0;

			yt += dyt;
			dyt += d2yt;
			d2yt += d3y0;

			currT += delta;
		}
	}

	public static SplineInterpolatorObject getSplineInterpolation(Point[] points) {
		if (points == null)
			return null;

		int count = points.length;
		if (count <= 1)
			return null;

		// compute min and max key (x)
		int minX = points[0].x;
		int maxX = points[0].x;
		for (int i = 1; i < count; i++) {
			int currX = points[i].x;
			if (currX < minX)
				minX = currX;
			if (currX > maxX)
				maxX = currX;
		}

		// allocate values array
		double[] values = new double[maxX - minX + 1];

		// create interpolations
		for (int i = 1; i < count + 2; i++)
			createSingleInterpolation(values, minX, maxX, catmullRomMatrix,
					points, i);

		return new SplineInterpolatorObject(minX, maxX, values);
	}

}
