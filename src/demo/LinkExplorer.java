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
import java.text.DecimalFormat;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JPanel;

import org.pushingpixels.matrixrain.MatrixPanel;
import org.pushingpixels.matrixrain.auxiliary.graphics.TrueColorImageManager;
import org.pushingpixels.matrixrain.auxiliary.graphics.colors.interpolator.ColorInterpolator;
import org.pushingpixels.matrixrain.auxiliary.graphics.colors.interpolator.ColorInterpolatorRGB;
import org.pushingpixels.matrixrain.connector.ConnectorManager;
import org.pushingpixels.matrixrain.connector.ConnectorManager.ConnectorChainElement;
import org.pushingpixels.matrixrain.connector.ConnectorManager.ConnectorChainList;
import org.pushingpixels.matrixrain.connector.ConnectorObject;
import org.pushingpixels.matrixrain.font.GlyphFactory;
import org.pushingpixels.matrixrain.font.MemoryGlyph;

public class LinkExplorer {
    static int FRAME_WIDTH = 1200;
    static int FRAME_HEIGHT = 850;
    static int COLUMNS = 3;//3;
    static int STRIDE = 300;//500;
    static String STRING = "matrix";

    public static class DemoPanel extends JPanel {
        private TrueColorImageManager tcim = new TrueColorImageManager(this, FRAME_WIDTH,
                FRAME_HEIGHT);
        private GlyphFactory abcGlyphFactory;
        private ConnectorChainList ccSortedList;

        public DemoPanel() {
            ConnectorManager connectorManager = new ConnectorManager();
            ConnectorChainList ccList = connectorManager.computeAllConnectorChains(STRING, null);
            ccSortedList = connectorManager.getSortedConnectorChains(ccList);
            ConnectorChainElement element = ccSortedList.head;
            int index = 0;
            while (element != null) {
                System.out.println(
                        index++ + " " + element.connectorChain + " : " + element.coefficient);
                element = element.next;
            }

            Image abcGlyphImg = null;
            try {
                abcGlyphImg = ImageIO.read(MatrixPanel.class.getResource("/images/common/abc.jpg"));
            } catch (IOException ioe) {
            }

            JFrame frame = new JFrame();
            frame.setSize(new Dimension(FRAME_WIDTH, FRAME_HEIGHT));
            frame.setLocationRelativeTo(null);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

            abcGlyphFactory = new GlyphFactory(abcGlyphImg, 40, null);
            abcGlyphFactory.createGlyphs(new int[] {}, 2, 1, false);
            abcGlyphFactory.computeGlyphSegments();

            ColorInterpolator interpolator = new ColorInterpolatorRGB(Color.white, Color.green,
                    256);

            int chainSize = Math.min(39, ccSortedList.length);
            ConnectorChainElement chain = ccSortedList.head;
            int chainIndex = 0;
            while (chainIndex < chainSize) {
                int column = chainIndex % COLUMNS;
                int row = chainIndex / COLUMNS;

                paintOneChain(STRING, interpolator, chain, 20 + column * STRIDE, 40 + row * 60);

                chainIndex++;
                chain = chain.next;
            }

            tcim.recomputeImage();
        }

        private void paintOneChain(String string, ColorInterpolator interpolator,
                ConnectorChainElement head, int dx, int dy) {
            int startX = 0;
            for (int i = 0; i < string.length(); i++) {
                int charIndex = string.charAt(i) - 'a';
                MemoryGlyph glyph = abcGlyphFactory.getGlyph(charIndex, 0, 1, 1);
                int glyphWidth = glyph.getWidth();
                tcim.paintGlyph(glyph, 0, dx + startX, dy, interpolator, false);

                if (i == string.length() - 1) {
                    continue;
                }

                int connectorType = Integer.parseInt(head.connectorChain.substring(i, i + 1));
                int connectorStartX, connectorEndX, connectorY = 0;

                int leftCharIndex = string.charAt(i) - 'a';
                int rightCharIndex = string.charAt(i + 1) - 'a';
                MemoryGlyph glyphLeft = abcGlyphFactory.getGlyph(leftCharIndex, 0, 1, 1);
                MemoryGlyph glyphRight = abcGlyphFactory.getGlyph(rightCharIndex, 0, 1, 1);
                int connectorGlow = 100;

                switch (connectorType) {
                case 1:
                    // UP
                    int topLeft = glyphLeft.getHighestRightSidePixel(connectorGlow);
                    int topRight = glyphRight.getHighestLeftSidePixel(connectorGlow);
                    connectorY = Math.max(topLeft, topRight);
                    break;
                case 2:
                    // CENTER
                    int centerLeft = (glyphLeft.getTop() + glyphLeft.getBottom()) / 2;
                    int centerRight = (glyphRight.getTop() + glyphRight.getBottom()) / 2;
                    connectorY = (centerLeft + centerRight) / 2;
                    break;
                case 3:
                    // DOWN
                    int bottomLeft = glyphLeft.getLowestRightSidePixel(connectorGlow);
                    int bottomRight = glyphRight.getLowestLeftSidePixel(connectorGlow);
                    connectorY = Math.min(bottomLeft, bottomRight);
                    break;
                }

                connectorStartX = startX + +glyphLeft.getRightConnectorX(connectorY, connectorGlow)
                        - glyphLeft.getLeft();
                connectorEndX = startX + glyphWidth + 10
                        + glyphRight.getLeftConnectorX(connectorY, connectorGlow)
                        - glyphRight.getLeft();

                ConnectorObject connectorObject = new ConnectorObject(
                        connectorEndX - connectorStartX + 1, 3);

                int connectorWidth = connectorObject.getWidth();
                int connectorHeight = connectorObject.getHeight();
                int[][] connectorPixels = connectorObject.getPixels();
                double coef = (double) connectorGlow / 255.0;

                int xL = connectorStartX, xR = connectorStartX + connectorWidth;
                int yT = connectorY, yB = connectorY + connectorHeight;

                for (int cX = xL; cX < xR; cX++) {
                    for (int cY = yT; cY < yB; cY++) {
                        if (connectorPixels[cX - xL][cY - yT] > 0) {
                            double alpha = coef * (double) connectorPixels[cX - xL][cY - yT]
                                    / 255.0;
                            Color color = interpolator.getInterpolatedColor(1.0 - alpha);
                            tcim.blendPixel(dx + cX, dy + cY, color.getRGB(), alpha);
                        }
                    }
                }

                startX += (glyphWidth + 10);
            }
        }

        @Override
        public void paint(Graphics g) {
            g.setColor(Color.black);
            g.fillRect(0, 0, getWidth(), getHeight());
            g.drawImage(tcim.getImage(), 0, 0, null);

            Graphics2D g2d = (Graphics2D) g.create();
            g2d.setColor(new Color(64, 255, 96));
            g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                    RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g2d.setFont(new Font("Menlo", Font.PLAIN, 14));
            //
            // int baseGlyphSize = abcGlyphFactory.getSizeByIndex(0);
            // for (int i = 0; i < 26; i++) {
            // int row = i / 9;
            // int col = i % 9;
            // int baseX = 20 + col * 65;
            // int baseY = 40 + row * 80;
            //
            // for (int connector = 1; connector <= 3; connector++) {
            // int leftRating = ConnectorMap.getRatingLeft((char) ('a' + i),
            // connector);
            // int rightRating = ConnectorMap.getRatingRight((char) ('a' + i),
            // connector);
            //
            // int y = baseY + (connector - 1) * 15 + 6;
            // g2d.drawString("" + leftRating, baseX - 6, y);
            // g2d.drawString("" + rightRating, baseX + baseGlyphSize, y);
            // }
            // }
            //
            int chainSize = Math.min(39, ccSortedList.length);
            ConnectorChainElement chain = ccSortedList.head;
            int chainIndex = 0;
            DecimalFormat df = new DecimalFormat(".###");
            while (chainIndex < chainSize) {
                int column = chainIndex % COLUMNS;
                int row = chainIndex / COLUMNS;

                g2d.drawString(String.format("%.3f", chain.coefficient), 20 + column * STRIDE, 40 + row * 60);

                chainIndex++;
                chain = chain.next;
            }
            g2d.dispose();
        }

    }

    public static void main(String[] args) {
        JFrame frame = new JFrame();
        frame.setSize(new Dimension(FRAME_WIDTH, FRAME_HEIGHT));
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        frame.setLayout(new BorderLayout());
        DemoPanel demoPanel = new DemoPanel();
        frame.add(demoPanel, BorderLayout.CENTER);

        frame.setVisible(true);
    }
}
