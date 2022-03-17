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
