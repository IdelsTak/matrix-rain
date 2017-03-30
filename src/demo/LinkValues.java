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
package demo;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JPanel;

import org.pushingpixels.matrixrain.MatrixPanel;
import org.pushingpixels.matrixrain.auxiliary.graphics.TrueColorImageManager;
import org.pushingpixels.matrixrain.auxiliary.graphics.colors.interpolator.ColorInterpolator;
import org.pushingpixels.matrixrain.auxiliary.graphics.colors.interpolator.ColorInterpolatorRGB;
import org.pushingpixels.matrixrain.connector.ConnectorMap;
import org.pushingpixels.matrixrain.font.GlyphFactory;
import org.pushingpixels.matrixrain.font.MemoryGlyph;

public class LinkValues {
    public static class DemoPanel extends JPanel {
        private TrueColorImageManager tcim = new TrueColorImageManager(this, 600, 400);
        private GlyphFactory abcGlyphFactory;

        public DemoPanel() {
            Image abcGlyphImg = null;
            try {
                abcGlyphImg = ImageIO.read(MatrixPanel.class.getResource("/images/common/abc.jpg"));
            } catch (IOException ioe) {
            }

            JFrame frame = new JFrame();
            frame.setSize(new Dimension(600, 400));
            frame.setLocationRelativeTo(null);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

            abcGlyphFactory = new GlyphFactory(abcGlyphImg, 40, null);
            abcGlyphFactory.createGlyphs(new int[] {}, 2, 1, false);
            abcGlyphFactory.computeGlyphSegments();

            ColorInterpolator interpolator = new ColorInterpolatorRGB(Color.white, Color.green,
                    256);
            int baseGlyphSize = abcGlyphFactory.getSizeByIndex(0);
            for (int i = 0; i < 26; i++) {
                int row = i / 9;
                int col = i % 9;
                MemoryGlyph glyph = abcGlyphFactory.getGlyph(i, 0, 1, 1);
                int glyphWidth = glyph.getWidth();
                int baseX = 20 + col * 65;
                int x = baseX + (baseGlyphSize - glyphWidth) / 2;
                int baseY = 40 + row * 80;
                tcim.paintGlyph(glyph, 0, x, baseY, interpolator, false);

            }
            tcim.recomputeImage();
        }

        @Override
        public void paint(Graphics g) {
            g.setColor(Color.black);
            g.fillRect(0, 0, getWidth(), getHeight());
            g.drawImage(tcim.getImage(), 0, 0, null);
            
            Graphics2D g2d = (Graphics2D) g.create();
            g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                    RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g2d.setFont(new Font("Lucida Grande", Font.PLAIN, 10));
            
            int baseGlyphSize = abcGlyphFactory.getSizeByIndex(0);
            for (int i = 0; i < 26; i++) {
                int row = i / 9;
                int col = i % 9;
                int baseX = 20 + col * 65;
                int baseY = 40 + row * 80;

                for (int connector = 1; connector <= 3; connector++) {
                    char c = (char) ('a' + i);
                    int leftRating = ConnectorMap.getRatingLeft(c, connector);
                    int rightRating = ConnectorMap.getRatingRight(c, connector);
                    boolean relevantLeft = ConnectorMap.isRelevantLeft(c, connector);
                    boolean relevantRight = ConnectorMap.isRelevantRight(c, connector);

                    int y = baseY + (connector - 1) * 15 + 6;
                    g2d.setColor(relevantLeft ? new Color(64, 255, 64) : new Color(255, 64, 64));
                    g2d.drawString("" + leftRating, baseX - 6, y);
                    g2d.setColor(relevantRight ? Color.green : Color.red);
                    g2d.drawString("" + rightRating, baseX + baseGlyphSize, y);
                    
                }
            }

            g2d.dispose();
        }
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame();
        frame.setSize(new Dimension(600, 400));
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        frame.setLayout(new BorderLayout());
        DemoPanel demoPanel = new DemoPanel();
        frame.add(demoPanel, BorderLayout.CENTER);

        frame.setVisible(true);
    }
}
