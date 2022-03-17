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
package org.pushingpixels.matrixrain;

import java.awt.Graphics;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.image.BufferedImage;

import javax.swing.AbstractAction;
import javax.swing.JRootPane;

public class MatrixAction extends AbstractAction {
  private final JRootPane rootPane;

  public MatrixAction(JRootPane parentFrame) {
    super();
    this.rootPane = parentFrame;
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    int w = this.rootPane.getWidth();
    int h = this.rootPane.getHeight();

    Insets ins = this.rootPane.getInsets();
    w -= (ins.left + ins.right);
    h -= (ins.top + ins.bottom);

    BufferedImage snap = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
    Graphics g = snap.getGraphics();
    g.translate(-ins.left, -ins.top);
    this.rootPane.paint(g);

    final MatrixPanel panel = new MatrixPanel();
    panel.setSize(w, h);
    panel.setMainImage(snap);
    panel.setTitleLine("matrix");

    panel.addKeyListener(new KeyAdapter() {});
    panel.addMouseMotionListener(new MouseMotionAdapter() {});
    panel.addMouseListener(
        new MouseAdapter() {
          @Override
          public void mouseClicked(MouseEvent e) {
            panel.setVisible(false);
            panel.requestStop();
          }
        });

    this.rootPane.setGlassPane(panel);
    panel.init();
    panel.start();
    panel.setVisible(true);
  }
}
