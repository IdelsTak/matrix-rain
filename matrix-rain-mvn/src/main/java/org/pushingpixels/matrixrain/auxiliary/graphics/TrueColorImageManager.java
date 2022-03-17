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
package org.pushingpixels.matrixrain.auxiliary.graphics;

import java.awt.Component;
import java.awt.Image;
import java.awt.image.MemoryImageSource;

public final class TrueColorImageManager extends TrueColorBitmapManager {
  // off-screen image
  private MemoryImageSource baseMImage;

  private Image baseImage;

  public TrueColorImageManager(Component comp) {
    super(comp.getWidth(), comp.getHeight());

    baseMImage = new MemoryImageSource(width, height, bitmap, 0, width);
    baseMImage.setAnimated(true);
    baseImage = comp.createImage(baseMImage);
  }

  public TrueColorImageManager(Component comp, int width, int height) {
    super(width, height);

    baseMImage = new MemoryImageSource(width, height, bitmap, 0, width);
    baseMImage.setAnimated(true);
    baseImage = comp.createImage(baseMImage);
  }

  public synchronized void recomputeImage() {
    // compute actual colors
    baseMImage.newPixels();
  }

  public synchronized Image getImage() {
    return baseImage;
  }
}
