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
package com.github.idelstak.matrixrain;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.io.IOException;
import java.util.Calendar;
import javax.imageio.ImageIO;
import javax.swing.JPanel;
import com.github.idelstak.matrixrain.auxiliary.graphics.TrueColorBitmapObject;
import com.github.idelstak.matrixrain.auxiliary.graphics.colors.manager.ColorManager1ColorScheme;
import com.github.idelstak.matrixrain.auxiliary.math.intersect.Circle1PixelArbitraryIntersectorFactory;
import com.github.idelstak.matrixrain.drop.DropManager;
import com.github.idelstak.matrixrain.font.GlyphFactory;
import com.github.idelstak.matrixrain.intro.IntroManager;
import com.github.idelstak.matrixrain.paint.MatrixPainter;
import com.github.idelstak.matrixrain.phosphore.PhosphoreCloudFactory;

public final class MatrixPanel extends JPanel implements Runnable {

  public static final int STATE_INITIALIZING = 0;
  public static final int STATE_LOADFILES = 1;
  public static final int STATE_INTRO = 2;
  public static final int STATE_SHOWHISTORY = 20;
  public static final int STATE_REPLAYINTRO = 31;
  public static final int STATE_REPLAYRAIN = 32;
  public static final int TIME_DELTA = 100;
  private String message = "";
  private StateStack stateStack;
  private int appWidth, appHeight;
  private Thread delayManager = null; // The thread that handles time delays
  private MatrixPainter quizPainter;
  private Image mainImage;
  private String titleLine;
  private IntroManager introManager;
  private GlyphFactory glyphFactory;
  private GlyphFactory abcGlyphFactory;
  private PhosphoreCloudFactory pcFct;
  private Circle1PixelArbitraryIntersectorFactory iFct;
  /** Indication whether a stop request has been issued on <code>this</code> thread. */
  private boolean stopRequested = false;

  /** Initializing the applet. */
  public void init() {
    this.stateStack = new StateStack(STATE_INITIALIZING);
    this.appWidth = (int) this.getSize().width;
    this.appHeight = (int) this.getSize().height;

    System.out.println("dimensions: " + appWidth + "*" + appHeight);

    this.quizPainter = new MatrixPainter(this, new ColorManager1ColorScheme(Color.green));
    this.quizPainter.createMessageGlyphFactory();

    // this.timer = QATimer.getHandle(this);

    this.message = "Initializing...";
    this.repaint();
  }

  /** Creates and runs this thread. */
  public void start() {
    this.delayManager = new Thread(this);
    this.delayManager.setPriority(Thread.MAX_PRIORITY);
    this.delayManager.start();
  }

  private boolean initInternal() {
    this.quizPainter.createGlyphFactories();

    this.stateStack.peekAndReplace(STATE_LOADFILES);
    this.message = "Loading files...";
    repaint();

    Image katakanaGlyphImg = null, abcGlyphImg = null;
    try {
      katakanaGlyphImg =
          ImageIO.read(MatrixPanel.class.getResource("/images/common/katakana_b.gif"));
      abcGlyphImg = ImageIO.read(MatrixPanel.class.getResource("/images/common/abc.gif"));
    } catch (IOException ioe) {
    }

    long startTime = System.currentTimeMillis();
    this.glyphFactory = new GlyphFactory(katakanaGlyphImg, 30, null);
    // create mirrored glyphs with 3 blurs and 2 radiances
    this.glyphFactory.createGlyphs(new int[] {6, 8, 10, 12, 14, 16}, 3, 2, true);
    // create glyphs of small sizes
    this.glyphFactory.createMiniGlyphs(new int[] {1, 2, 3, 4, 5, 6, 7, 8});
    long endTime = System.currentTimeMillis();
    System.out.println("Katakana glyphs: " + (endTime - startTime));

    startTime = System.currentTimeMillis();
    this.abcGlyphFactory = new GlyphFactory(abcGlyphImg, 40, null);
    // create unmirrored glyphs with 2 blurs and 1 radiances
    this.abcGlyphFactory.createGlyphs(new int[] {}, 2, 1, false);
    // compute segments
    this.abcGlyphFactory.computeGlyphSegments();
    endTime = System.currentTimeMillis();
    System.out.println("English glyphs: " + (endTime - startTime));

    DropManager dropManager = new DropManager(this.glyphFactory, this.appWidth, this.appHeight);
    dropManager.createDrops();

    this.introManager =
        new IntroManager(
            this,
            dropManager,
            this.mainImage,
            this.quizPainter.introColorManager,
            this.glyphFactory);

    Calendar rightNow = Calendar.getInstance();
    System.out.println("Starting animation ... (" + rightNow.getTime().toString() + ")");
    this.message = "Creating animation sequence...";

    repaint();
    return true;
  }

  private boolean downloadImages() {
    this.pcFct = new PhosphoreCloudFactory(5);
    this.iFct = new Circle1PixelArbitraryIntersectorFactory();

    this.quizPainter.backgroundBitmap =
        new TrueColorBitmapObject(
            this.mainImage, this.mainImage.getWidth(null), this.mainImage.getHeight(null));

    repaint();
    return true;
  }

  /** Destroys the thread. */
  public void stop() {
    delayManager = null;
  }

  /** @return the applet info. */
  public String getAppletInfo() {
    return "Applet Information: first written by Kirill Grouchnikov , 2000-2003";
  }

  @Override
  public void repaint() {
    if (this.quizPainter == null) return;
    if (this.getGraphics() == null) return;
    // long time0 = System.currentTimeMillis();
    this.paint();
    // long time1 = System.currentTimeMillis();
    this.quizPainter.recomputeImage();
    // long time2 = System.currentTimeMillis();
    Image im = this.quizPainter.getImage();
    this.getGraphics().drawImage(im, 0, 0, null);
    // long time3 = System.currentTimeMillis();
    // System.out.println("repaint: " + (time3-time0));
    // System.out.println("repaint: " + (time3-time0) + " (" + (time1-time0)
    // + " + " +
    // (time2-time1) + " + " + (time3-time2) + ")");
  }

  @Override
  public void update(Graphics g) {
    if (this.quizPainter == null) return;
    this.paint();
    this.quizPainter.recomputeImage();
    g.drawImage(this.quizPainter.getImage(), 0, 0, this);
  }

  @Override
  public void paint(Graphics g) {
    // very important - for drawing when window is not focused or just
    // gained focus from Windows
    this.update(g);
  }

  public void flush(Graphics g) {
    if (this.quizPainter == null) return;
    // long time0 = System.currentTimeMillis();
    this.quizPainter.recomputeImage();
    // long time1 = System.currentTimeMillis();
    g.drawImage(this.quizPainter.getImage(), 0, 0, null);
    // long time2 = System.currentTimeMillis();
    // System.out.println("flush: " + (time2-time0) + " (" + (time1-time0) +
    // " + " +
    // (time2-time1) + ")");
  }

  private void paint() {
    if (this.quizPainter == null) return;

    // System.out.println("paint in " + this.stateStack.peek());

    // long t0 = System.currentTimeMillis();
    // synchronize to prevent paints from separate threads
    // synchronized (this.quizPainter) {
    switch (this.stateStack.peek()) {
      case STATE_INITIALIZING, STATE_LOADFILES -> {
        this.quizPainter.paintMessage(this.message);
        return;
      }
      case STATE_INTRO -> {
        if (this.introManager.getState() == IntroManager.STATE_RAINREGULAR)
          this.quizPainter.paintIntro(this.introManager.getBitmap1D(), this.message);
        else this.quizPainter.paintIntro(this.introManager.getBitmap1D(), null);
        return;
      }

      case STATE_REPLAYINTRO -> this.quizPainter.paintIntro(
          this.introManager.getBitmap1D(), this.message);
    }
    // long t1 = System.currentTimeMillis();
  }

  private synchronized void iteration() {
    switch (this.stateStack.peek()) {
      case STATE_INTRO, STATE_REPLAYINTRO -> {
        this.introManager.iteration(false);
        if (this.introManager.isEnded()) {
          System.out.println(
              "Ending animation... (" + Calendar.getInstance().getTime().toString() + ")");
          try {
            this.stateStack.pop();
          } catch (StateStack.StackEmpty see) {
            see.printStackTrace();
            return;
          }
          // this.timer.start();
          this.quizPainter.backgroundBitmap = this.introManager.getMosaicBitmap();
          this.message = "";

          this.stateStack.push(STATE_REPLAYINTRO);
          this.introManager.replay(true);
        }
        repaint();
      }
    }
  }

  /**
   * - Initializes various graphics components
   *
   * <p>- Downloads all images
   *
   * <p>- Shows title in the Matrix rain
   */
  @Override
  public void run() {
    // initialize various graphics components
    if (!initInternal()) {
      repaint();
      return;
    }
    // download all images
    boolean imagesOK = this.downloadImages();
    if (!imagesOK) {
      repaint();
      return;
    }

    this.stateStack.push(STATE_INTRO);
    repaint();

    // show title in Matrix rain
    this.introManager.showTitle(
        this.titleLine,
        this.abcGlyphFactory,
        Math.min(40, this.appHeight / 20),
        this.pcFct,
        this.iFct);
    repaint();

    Thread me = Thread.currentThread();
    while ((delayManager == me) && (!this.stopRequested)) {
      long time1 = System.currentTimeMillis();
      iteration();
      long time2 = System.currentTimeMillis();
      try {
        long timeToSleep = Math.max(0, TIME_DELTA - (time2 - time1));
        Thread.sleep(timeToSleep);
      } catch (InterruptedException e) {
      }
    }
  }

  public void requestStop() {
    this.stopRequested = true;
  }

  public void setMainImage(Image mainImage) {
    this.mainImage = mainImage;
  }

  public void setTitleLine(String titleLine) {
    this.titleLine = titleLine;
  }
}
