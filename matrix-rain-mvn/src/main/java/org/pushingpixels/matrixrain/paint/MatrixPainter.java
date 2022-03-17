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
package org.pushingpixels.matrixrain.paint;

import java.awt.*;
import java.awt.image.BufferedImage;

import org.pushingpixels.matrixrain.MatrixPanel;
import org.pushingpixels.matrixrain.auxiliary.graphics.TrueColorBitmapObject;
import org.pushingpixels.matrixrain.auxiliary.graphics.TrueColorImageManager;
import org.pushingpixels.matrixrain.auxiliary.graphics.colors.manager.ColorManager;
import org.pushingpixels.matrixrain.font.GlyphFactory;

public final class MatrixPainter {
  public static final int CONTROL_NONE = -1;

  public static final int CONTROL_REPLAYINTRO = 20;

  public static final int CONTROL_MATRIXRAIN = 21;

  public static final int CONTROL_SCREENSAVER = 22;

  public static final int CONTROL_COLORS = 23;

  public static final int CONTROL_NPR = 24;

  public MatrixPanel panel;

  public int appWidth, appHeight;

  // memory glyphs
  private GlyphFactory[][] glyphFactoryArray;

  private GlyphFactory messageGlyphFactory;

  public TrueColorBitmapObject backgroundBitmap;

  public ColorManager introColorManager;

  // public NPRManager nprManager;

  private final TrueColorImageManager trueColorImageManager;

  public Image debugImage;

  public MatrixPainter(MatrixPanel panel, ColorManager introColorManager) {

    this.panel = panel;
    this.appWidth = panel.getWidth();
    this.appHeight = panel.getHeight();

    this.introColorManager = introColorManager;

    this.trueColorImageManager = new TrueColorImageManager(panel);
  }

  private GlyphFactory createSingleGlyphFactory(int weight, int kind) {
    // create image big enough to "hold" 16*8 glyphs
    int imWidth = 16 * (2 * weight);
    int imHeight = 8 * (2 * weight);
    Image glyphImage = new BufferedImage(imWidth, imHeight, BufferedImage.TYPE_INT_ARGB);
    this.debugImage = glyphImage;
    Graphics glyphGraphics = glyphImage.getGraphics();
    glyphGraphics.setFont(new java.awt.Font("Arial", kind, weight));

    glyphGraphics.setColor(Color.black);
    glyphGraphics.fillRect(0, 0, imWidth, imHeight);
    glyphGraphics.setColor(Color.white);
    for (int j = 0; j < 8; j++) {
      for (int i = 0; i < 16; i++) {
        int index = j * 16 + i;
        char c = (char) (index);
        glyphGraphics.drawString("" + c, i * 2 * weight + 2, (j + 1) * 2 * weight - 5);
      }
    }
    GlyphFactory glyphFactory =
        new GlyphFactory(
            glyphImage, // image
            // with
            // first
            // half of
            // ASCII
            2 * weight, // cell size
            glyphGraphics.getFontMetrics()); // font metrics
    glyphFactory.createGlyphs(new int[0], 0, 0, false);
    glyphFactory.computeGlowClouds(0, 0, 0, Math.max(3, weight / 4));

    return glyphFactory;
  }

  public void createMessageGlyphFactory() {
    long time0 = System.currentTimeMillis();
    // create glyphs
    this.messageGlyphFactory = this.createSingleGlyphFactory(14, Font.PLAIN);
    long time1 = System.currentTimeMillis();
    System.out.println("Created message glyphs in " + (time1 - time0));
  }

  public void createGlyphFactories() {
    long time0 = System.currentTimeMillis();
    // create glyphs
    this.glyphFactoryArray = new GlyphFactory[21][3];
    for (int weight = 10; weight <= 18; weight++) {
      this.glyphFactoryArray[weight][Font.PLAIN] =
          this.createSingleGlyphFactory(weight, Font.PLAIN);
    }
    this.glyphFactoryArray[12][Font.BOLD] = this.createSingleGlyphFactory(12, Font.BOLD);
    long time1 = System.currentTimeMillis();
    System.out.println("Created glyphs in " + (time1 - time0));
  }

  private int stringWidth(GlyphFactory glyphFactory, String str) {
    // create index array
    int len = str.length();
    int result = 0;
    for (int i = 0; i < len; i++) result += (glyphFactory.charWidth(str.charAt(i), 0, 0, 0) + 1);
    return result;
  }

  public void paintMessage(String message) {
    this.trueColorImageManager.resetImage();
    GlyphFactory glyphFactory = this.messageGlyphFactory;
    int messWidth = this.stringWidth(glyphFactory, message);
    this.trueColorImageManager.paintString(
        glyphFactory,
        message,
        (this.appWidth - messWidth) / 2,
        this.appHeight / 2,
        Color.red,
        null,
        true);
  }

  public void paintIntro(int[] introFrameBitmap, String optionalMessage) {
    this.trueColorImageManager.overwriteBitmap1D(introFrameBitmap);

    if (optionalMessage == null) return;

    if (optionalMessage.length() == 0) return;

    GlyphFactory glyphFactory = this.glyphFactoryArray[10][0];
    int messWidth = this.stringWidth(glyphFactory, optionalMessage);
    int messHeight = glyphFactory.getOrigHeight() + 1;

    int x = (this.appWidth - messWidth) / 2;
    int y = (this.appHeight - messHeight) / 2;
    this.trueColorImageManager.fillRect(
        x, y - messHeight, messWidth + 4, messHeight, Color.black.getRGB());
    this.trueColorImageManager.drawRect(
        x,
        y - messHeight,
        messWidth + 4,
        messHeight,
        this.introColorManager.getMasterColor().getRGB());

    this.trueColorImageManager.paintHollowRawLine(
        glyphFactory, optionalMessage, x + 2, y - 9, this.introColorManager, false);
  }

  public void redrawBackgroundImage(int x, int y, int width, int height) {
    this.trueColorImageManager.paintTrueColorObject(
        this.backgroundBitmap, x, y, x + width, y + height, x, y);
  }

  public synchronized void recomputeImage() {
    this.trueColorImageManager.recomputeImage();
  }
  //
  public synchronized Image getImage() {
    Image image = this.trueColorImageManager.getImage();
    // the following line is VERY important for preventing flickering
    // in rendering this image in IE environment
    //		image.flush();
    return image;
  }
}
