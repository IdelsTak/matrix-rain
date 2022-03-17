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
