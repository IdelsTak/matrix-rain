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
package org.pushingpixels.matrixrain.connector;

import org.pushingpixels.matrixrain.auxiliary.graphics.IndexBitmapObject;
import org.pushingpixels.matrixrain.auxiliary.math.intersect.Circle1PixelArbitraryIntersectorFactory;
import org.pushingpixels.matrixrain.intro.IntroManager;
import org.pushingpixels.matrixrain.phosphore.*;

public class ConnectorObject {
	private int width, height;

	private int[][] pixels;

	// glow cloud
	private PhosphoreCloud glowCloud;

	private ConnectorObject() {
		this.width = 0;
		this.height = 0;
		this.pixels = null;
		this.glowCloud = null;
	}

	public ConnectorObject(int length, int height) {
		this.width = length;
		this.height = height;
		this.pixels = new int[length][height];
		for (int j = 0; j < height; j++) {
			int distFromCenter = (height - 1) / 2 - j;
			if (distFromCenter < 0)
				distFromCenter = -distFromCenter;
			double coef = 1.0 - 0.6 * (double) distFromCenter
					/ (double) (height - 1);
			for (int i = 0; i < length; i++)
				this.pixels[i][j] = (int) (coef * 255.0);
		}

		// compute glow cloud
		this.glowCloud = new PhosphoreCloud(length, IntroManager.GLOW_RADIUS,
				255);
	}

	public ConnectorObject(IndexBitmapObject indexBitmapObject) {
		this.width = indexBitmapObject.getWidth();
		this.height = indexBitmapObject.getHeight();
		this.pixels = indexBitmapObject.getBitmap();
		// compute glow cloud
		// this.glowCloud = new PhosphoreCloud(length, IntroManager.GLOW_RADIUS,
		// 255);
	}

	public Object clone() {
		ConnectorObject newObject = new ConnectorObject();
		newObject.width = this.width;
		newObject.height = this.height;
		newObject.pixels = new int[newObject.width][newObject.height];
		for (int i = 0; i < newObject.width; i++)
			for (int j = 0; j < newObject.height; j++)
				newObject.pixels[i][j] = this.pixels[i][j];
		if (this.glowCloud != null)
			newObject.glowCloud = (PhosphoreCloud) this.glowCloud.clone();
		return newObject;
	}

	public int getWidth() {
		return this.width;
	}

	public int getHeight() {
		return this.height;
	}

	public int[][] getPixels() {
		return pixels;
	}

	public int getPixel(int i, int j) {
		return pixels[i][j];
	}

	public PhosphoreCloud getGlowCloud() {
		return glowCloud;
	}

	public final ConnectorObject createScaledUpPhosphoreVersion(
			PhosphoreCloudFactory pcFct,
			Circle1PixelArbitraryIntersectorFactory iFct, double factor) {

		ConnectorObject newConnector = new ConnectorObject();
		IndexBitmapObject oldPixels = new IndexBitmapObject(this.pixels,
				this.width, this.height);
		IndexBitmapObject newPixels = Phosphorizer
				.createScaledUpPhosphoreVersion(oldPixels, pcFct, iFct, factor,
						false);

		newConnector.pixels = newPixels.getBitmap();
		newConnector.width = newPixels.getWidth();
		newConnector.height = newPixels.getHeight();

		newConnector.sharpen(50, 255);
		return newConnector;
	}

	public final ConnectorObject createScaledUpPixelVersion(
			Circle1PixelArbitraryIntersectorFactory iFct, double factor) {

		ConnectorObject newConnector = new ConnectorObject();
		IndexBitmapObject oldPixels = new IndexBitmapObject(this.pixels,
				this.width, this.height);
		IndexBitmapObject newPixels = Phosphorizer.createScaledUpPixelVersion(
				oldPixels, iFct, factor);

		newConnector.pixels = newPixels.getBitmap();
		newConnector.width = newPixels.getWidth();
		newConnector.height = newPixels.getHeight();

		// newConnector.sharpen(50, 255);
		return newConnector;
	}

	public void sharpen(int minColorVal, int maxColorVal) {
		// find minimum and maximum value and "stretch" the color map to new
		// range
		int maxVal = 0, minVal = 255;
		for (int i = 0; i < this.width; i++) {
			for (int j = 0; j < this.height; j++) {
				maxVal = Math.max(maxVal, pixels[i][j]);
				if (pixels[i][j] > 0)
					minVal = Math.min(minVal, pixels[i][j]);
			}
		}

		// minVal -> minColorVal
		// maxVal -> maxColorVal
		double coef = (double) (maxColorVal - minColorVal)
				/ (double) (maxVal - minVal);
		for (int i = 0; i < this.width; i++) {
			for (int j = 0; j < this.height; j++) {
				if (pixels[i][j] == 0)
					continue;
				int newVal = (int) (minColorVal + coef
						* (pixels[i][j] - minVal));
				pixels[i][j] = newVal;
			}
		}
	}
}
