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

import java.awt.FlowLayout;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.KeyStroke;

public class MatrixFrame extends JFrame {
  public MatrixFrame() {
    this.setSize(500, 300);
    this.setLayout(new FlowLayout());

    //    Component panel = new MatrixPanel();
    //
    //    this.setLayout(new BorderLayout());
    //    this.add(panel, BorderLayout.CENTER);

    this.add(new JButton("button"));
    this.add(new JCheckBox("check"));

    this.getRootPane()
        .getInputMap()
        .put(
            KeyStroke.getKeyStroke(
                KeyEvent.VK_M, InputEvent.ALT_DOWN_MASK | InputEvent.CTRL_DOWN_MASK),
            "runMatrix");
    this.getRootPane().getActionMap().put("runMatrix", new MatrixAction(this.getRootPane()));

    this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
  }

  public static void main(String[] args) {
    MatrixFrame mf = new MatrixFrame();
    mf.setLocationRelativeTo(null);
    mf.setVisible(true);
  }
}
