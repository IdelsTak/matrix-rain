/*
 * The MIT License
 * Copyright © 2022 Hiram K
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.pushingpixels.matrixrain.drop;

import org.pushingpixels.matrixrain.font.GlyphFactory;
import org.pushingpixels.matrixrain.font.MemoryGlyph;

public class Drop {

  private int fontSizeIndex;

  private int blurFactor;

  private boolean toChangeLetters;

  private boolean drying;

  private int tailLength;

  private int headRadiance;

  private int headSpeed;

  private int headLength;

  private MemoryGlyph[] glyphs;

  private int[] glyphIndices;

  private final GlyphFactory glyphFactory;

  private int originalLength;

  public Drop(GlyphFactory glyphFactory) {
    fontSizeIndex = 2;
    blurFactor = 0;
    toChangeLetters = false;
    tailLength = 10;
    headRadiance = 0;
    headSpeed = 2;
    headLength = 2;
    drying = false;
    originalLength = 12;
    this.glyphFactory = glyphFactory;
  }

  private static int getRandom(int maxValue) {
    double val = Math.random();
    return (int) (Math.floor(val * maxValue));
  }

  // get / set

  public void setFontSize(int value) {
    fontSizeIndex = value;
  }

  public int getFontSizeIndex() {
    return fontSizeIndex;
  }

  public int getFontSize() {
    return this.glyphFactory.getSizeByIndex(fontSizeIndex);
  }

  public void setBlurFactor(int value) {
    blurFactor = value;
  }

  public int getBlurFactor() {
    return blurFactor;
  }

  public void setToChangeLetters(boolean value) {
    toChangeLetters = value;
  }

  public boolean getToChangeLetters() {
    return toChangeLetters;
  }

  public void setDrying(boolean value) {
    drying = value;
  }

  public boolean getDrying() {
    return drying;
  }

  public void setTailLength(int value) {
    tailLength = value;
  }

  public int getTailLength() {
    return tailLength;
  }

  public void setHeadRadiance(int value) {
    headRadiance = value;
  }

  public int getHeadRadiance() {
    return headRadiance;
  }

  public void setHeadSpeed(int value) {
    headSpeed = value;
  }

  public int getHeadSpeed() {
    return headSpeed;
  }

  public void setHeadLength(int value) {
    headLength = value;
  }

  public int getHeadLength() {
    return headLength;
  }

  public void setGlyphs(MemoryGlyph[] value) {
    glyphs = value;
  }

  public MemoryGlyph[] getGlyphs() {
    return glyphs;
  }

  public int getLength() {
    return headLength + tailLength;
  }

  // populate the drop with glyphs by indices
  private void populateGlyphs(int[] glyphIndices) {
    for (int i = 0; i < glyphIndices.length; i++) {
      int radianceFactor = 0;
      if (i < this.headLength) radianceFactor = this.headRadiance;
      glyphs[i] = glyphFactory.getGlyph(glyphIndices[i], fontSizeIndex, blurFactor, radianceFactor);
    }
  }

  public void populateGlyphs() {
    int length = getLength();
    glyphs = new MemoryGlyph[length];
    glyphIndices = new int[length];
    int glyphCount = glyphFactory.getGlyphCount();
    // choose glyphs
    for (int i = 0; i < length; i++) glyphIndices[i] = getRandom(glyphCount);
    this.populateGlyphs(glyphIndices);
    this.originalLength = length;
  }

  public void addGlyphAndShift() {
    // shift all existing glyphs one down
    int length = getLength();
    for (int i = length - 1; i > 0; i--) glyphIndices[i] = glyphIndices[i - 1];
    // randomly choose new glyph
    int glyphCount = glyphFactory.getGlyphCount();
    glyphIndices[0] = getRandom(glyphCount);

    this.populateGlyphs(glyphIndices);
  }

  public void dryIteration(int speedupFactor) {
    for (int count = 0; count < speedupFactor; count++) {
      // make all glyphs smaller (by weight group)
      int length = getLength() - 1;
      if (length <= 0) {
        this.tailLength = 0;
        this.headLength = 0;
        this.glyphs = null;
        return;
      }
      // in any case - no head
      this.headLength = 0;
      this.tailLength = length;
      this.glyphIndices = new int[length];
      this.glyphs = new MemoryGlyph[length];
      for (int i = 0; i < length; i++) {
        int tenthIndex = 1 + (int) Math.floor(10.0 * ((double) i / (double) this.originalLength));
        glyphIndices[i] =
            glyphFactory.getGlyphIndexByWeightGroup(
                tenthIndex, this.fontSizeIndex, this.blurFactor, 0);
      }
      // create new glyphs
      this.populateGlyphs(glyphIndices);
    }
  }

  public double getFadeoutFactor() {
    return (double) getLength() / (double) this.originalLength;
  }
}
