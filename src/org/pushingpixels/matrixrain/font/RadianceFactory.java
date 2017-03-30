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

public class RadianceFactory {

	private RadianceMap[][] radiances;

	private class RadianceMap {
		public int cValue;

		public int radiance;

		public int size;

		public int[][] rMap;

		public RadianceMap(int cValue, int radiance) {
			this.cValue = cValue;
			this.radiance = radiance;
			this.size = radiance + 1;

			this.rMap = new int[size][size];
			for (int i = 0; i < size; i++)
				for (int j = 0; j < size; j++)
					rMap[i][j] = 0;

			rMap[0][0] = cValue;

			if (radiance == 0) {
				// no radiance - take only this pixel
				return;
			}

			if (cValue == 0) {
				// no color
				return;
			}

			double delta = (double) (255 - cValue) / 255.0;
			double delta_rf = delta;
			for (int i = 1; i < radiance; i++)
				delta_rf *= delta;

			for (int dx = 0; dx <= radiance; dx++) {
				for (int dy = 0; dy <= radiance; dy++) {
					double distance = Math.sqrt(dx * dx + dy * dy);
					if (distance > radiance) {
						rMap[dx][dy] = 0;
					} else {
						double factor1 = 1.0 - (distance / (radiance + 0.1));
						double factor2 = 255.0 * delta * (1.0 - delta_rf);
						rMap[dx][dy] = (int) (factor1 * factor2);
					}
				}
			}
		}

		public int getFactor(int dx, int dy) {
			if (dx < 0)
				dx = -dx;
			if (dy < 0)
				dy = -dy;
			return rMap[dx][dy];
		}
	}

	public RadianceFactory() {
		radiances = new RadianceMap[256][2];
		for (int cValue = 0; cValue < 256; cValue++) {
			for (int radiance = 1; radiance <= 2; radiance++) {
				radiances[cValue][radiance - 1] = new RadianceMap(cValue,
						radiance);
			}
		}
	}

	public int getFactor(int cValue, int radiance, int dx, int dy) {
		return radiances[cValue][radiance - 1].getFactor(dx, dy);
	}
}
