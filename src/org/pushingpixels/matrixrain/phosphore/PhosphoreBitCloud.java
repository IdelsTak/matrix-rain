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
package org.pushingpixels.matrixrain.phosphore;

// PhosphoreBitCloud - each integer value holds information
// on 2*2 matrix of pixels. 32 bits are enough to hold 4
// separate bytes of radiations

public final class PhosphoreBitCloud {

	private int[] radiationBitValues;

	private int refPointX, refPointY;

	private int width, height;

	private int size;

	public PhosphoreBitCloud() {
		this.radiationBitValues = null;
		this.width = 0;
		this.height = 0;
		this.refPointX = 0;
		this.refPointY = 0;
	}

	// constructor from PhosphoreCloud
	public PhosphoreBitCloud(PhosphoreCloud pCloud, boolean shiftX,
			boolean shiftY) {
		int origWidth = pCloud.getWidth();
		int origHeight = pCloud.getHeight();

		// System.out.println("NEW CLOUD - CREATE from max value " +
		// pCloud.getMaxValue());

		// go over all 2*2 squares and create value
		int paddedWidth = origWidth + (shiftX ? 1 : 0);
		int paddedHeight = origHeight + (shiftY ? 1 : 0);
		double[][] paddedValues = new double[paddedWidth][paddedHeight];
		for (int i = 0; i < paddedWidth; i++)
			for (int j = 0; j < paddedHeight; j++)
				paddedValues[i][j] = 0;

		double[][] origValues = pCloud.getRadiations();
		int startCol = (shiftX ? 1 : 0);
		int startRow = (shiftY ? 1 : 0);
		for (int i = 0; i < origWidth; i++)
			for (int j = 0; j < origHeight; j++)
				paddedValues[i + startCol][j + startRow] = origValues[i][j];

		this.refPointX = pCloud.getRefPointX() / 2;
		if (shiftX)
			this.refPointX++;
		this.refPointY = pCloud.getRefPointY() / 2;
		if (shiftY)
			this.refPointY++;

		this.width = (int) Math.ceil((double) paddedWidth / 2.0);
		this.height = (int) Math.ceil((double) paddedHeight / 2.0);

		this.size = this.width * this.height;

		this.radiationBitValues = new int[this.size];
		// initialize the radiation values
		for (int i = 0; i < this.size; i++) {
			this.radiationBitValues[i] = 0;
		}

		for (int i = 0; i < this.size; i++) {
			int row = i / this.width;
			int col = i % this.width;

			int byte1 = 0, byte2 = 0, byte3 = 0, byte4 = 0;
			/*
			 * if (((2*col) < origWidth) && ((2*row) < origHeight)) byte1 =
			 * (int)(32.0*paddedValues[2*col][2*row]); if (((2*col+1) <
			 * origWidth) && ((2*row) < origHeight)) byte2 =
			 * (int)(32.0*paddedValues[2*col+1][2*row]); if (((2*col) <
			 * origWidth) && ((2*row+1) < origHeight)) byte3 =
			 * (int)(32.0*paddedValues[2*col][2*row+1]); if (((2*col+1) <
			 * origWidth) && ((2*row+1) < origHeight)) byte4 =
			 * (int)(32.0*paddedValues[2*col+1][2*row+1]); if (byte2 > 255)
			 * byte2 = 255; if (byte3 > 255) byte3 = 255; if (byte4 > 255) byte4 =
			 * 255;
			 */
			if (((2 * col) < origWidth) && ((2 * row) < origHeight))
				byte1 = (int) (paddedValues[2 * col][2 * row]);
			if (((2 * col + 1) < origWidth) && ((2 * row) < origHeight))
				byte2 = (int) (paddedValues[2 * col + 1][2 * row]);
			if (((2 * col) < origWidth) && ((2 * row + 1) < origHeight))
				byte3 = (int) (paddedValues[2 * col][2 * row + 1]);
			if (((2 * col + 1) < origWidth) && ((2 * row + 1) < origHeight))
				byte4 = (int) (paddedValues[2 * col + 1][2 * row + 1]);

			// System.out.print("cloud at (" + 2*col + ", " + 2*row + ") : ");
			// System.out.print(byte1 + ", " + byte2 + ", " + byte3 + ", " +
			// byte4);

			int newValue = (byte1 << 24) | (byte2 << 16) | (byte3 << 8) | byte4;
			// System.out.println(" = " + newValue);
			this.radiationBitValues[i] = newValue;
		}
	}

	public Object clone() {
		PhosphoreBitCloud newObject = new PhosphoreBitCloud();
		newObject.width = this.width;
		newObject.height = this.height;
		newObject.size = this.size;
		newObject.refPointX = this.refPointX;
		newObject.refPointY = this.refPointY;
		if (this.radiationBitValues != null) {
			newObject.radiationBitValues = new int[newObject.size];
			for (int i = 0; i < newObject.size; i++)
				newObject.radiationBitValues[i] = this.radiationBitValues[i];
		} else
			newObject.radiationBitValues = null;
		return newObject;
	}

	public int getRefPointX() {
		return this.refPointX;
	}

	public int getRefPointY() {
		return this.refPointY;
	}

	public int getWidth() {
		return this.width;
	}

	public int getHeight() {
		return this.height;
	}

	public int getSize() {
		return this.size;
	}

	public int[] getRadiations() {
		return this.radiationBitValues;
	}
}
