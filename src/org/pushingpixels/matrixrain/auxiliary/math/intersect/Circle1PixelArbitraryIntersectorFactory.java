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
package org.pushingpixels.matrixrain.auxiliary.math.intersect;

public class Circle1PixelArbitraryIntersectorFactory {

	public static final int N = 20;

	private class IntersectorElement {
		public int radiusN;

		public double[] _SR;

		public double[][] _SRXY;

		public double[][] _unitIntersection;

		public double[][] _doubleIntersection;

		public int[][] _unitShift;

		public IntersectorElement next;

		public IntersectorElement(double radius) {
			this.radiusN = (int) (radius * N);
			this._SR = new double[2 * N + 1];
			for (int i = -N; i <= N; i++) {
				double x = (double) i / (double) N;
				this._SR[i + N] = Intersector.SR(x, radius);
			}

			this._SRXY = new double[2 * N + 1][2 * N + 1];
			for (int i = -N; i <= N; i++) {
				double x = (double) i / (double) N;
				for (int j = -N; j <= N; j++) {
					double y = (double) j / (double) N;
					this._SRXY[i + N][j + N] = Intersector.SRXY(x, y, radius);
				}
			}

			this._unitIntersection = new double[2 * N + 1][2 * N + 1];
			this._unitShift = new int[2 * N + 1][2 * N + 1];
			for (int i = -N; i <= N; i++) {
				double x = (double) i / (double) N;
				for (int j = -N; j <= N; j++) {
					double y = (double) j / (double) N;
					this._unitIntersection[i + N][j + N] = Intersector
							.intersectionArea(radius, x, y, x + 1, y + 1);
					double normUnitIntersection = 255.0
							* this._unitIntersection[i + N][j + N]
							/ (Math.PI * radius * radius);
					this._unitShift[i + N][j + N] = 8 - (int) (Math
							.log(normUnitIntersection + 1) / Math.log(2.0));
				}
			}

			this._doubleIntersection = new double[4 * N + 1][4 * N + 1];
			for (int i = -2 * N; i <= 2 * N; i++) {
				double x = (double) i / (double) 2 * N;
				for (int j = -2 * N; j <= 2 * N; j++) {
					double y = (double) j / (double) 2 * N;
					this._doubleIntersection[i + 2 * N][j + 2 * N] = Intersector
							.intersectionArea(2.0 * radius, x, y, x + 1, y + 1);
				}
			}

			this.next = null;
		}

		public double SR(int index) {
			if ((index < -N) || (index > N))
				return 0.0;
			return _SR[index + N];
		}

		public double SRXY(int indexX, int indexY) {
			if ((indexX < -N) || (indexX > N))
				return 0.0;
			if ((indexY < -N) || (indexY > N))
				return 0.0;
			return _SRXY[indexX + N][indexY + N];
		}

		public double unitIntersection(int indexX, int indexY) {
			if ((indexX < -N) || (indexX > N))
				return 0.0;
			if ((indexY < -N) || (indexY > N))
				return 0.0;
			return _unitIntersection[indexX + N][indexY + N];
		}

		public double doubleIntersection(int indexX, int indexY) {
			if ((indexX < -2 * N) || (indexX > 2 * N))
				return 0.0;
			if ((indexY < -2 * N) || (indexY > 2 * N))
				return 0.0;
			return _doubleIntersection[indexX + 2 * N][indexY + 2 * N];
		}

		public int unitShift(int indexX, int indexY) {
			if ((indexX < -N) || (indexX > N))
				return 8;
			if ((indexY < -N) || (indexY > N))
				return 8;
			return _unitShift[indexX + N][indexY + N];
		}
	}

	private IntersectorElement intersectorElement;

	public static long delta = 0;

	public Circle1PixelArbitraryIntersectorFactory() {
		this.intersectorElement = new IntersectorElement(0.5);
	}

	public double unitIntersectionArea(int x1N, int y1N) {
		return this.intersectorElement.unitIntersection(x1N, y1N);
	}

	public double doubleIntersectionArea(int x1N, int y1N) {
		return this.intersectorElement.doubleIntersection(x1N, y1N);
	}

	public int unitShift(int x1N, int y1N) {
		return this.intersectorElement.unitShift(x1N, y1N);
	}
}
