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
package com.github.idelstak.matrixrain.intro.title;

import java.awt.Point;

import com.github.idelstak.matrixrain.connector.ConnectorObject;
import com.github.idelstak.matrixrain.font.MemoryGlyph;

public class TitleFrame {
  /**
   * if glyph is completely outside the screen, the corresponding entry in the following two arrays
   * is null
   */
  private final MemoryGlyph[] glyphs;

  private final Point[] glyphPositions;
  /**
   * if connector is completely outside the screen, the corresponding entry in the following two
   * arrays is null
   */
  private final ConnectorObject[] connectors;

  private final Point[] connectorPositions;

  public TitleFrame(int glyphCount, int connectorCount) {
    this.glyphs = new MemoryGlyph[glyphCount];
    this.glyphPositions = new Point[glyphCount];
    for (int i = 0; i < glyphCount; i++) {
      this.glyphs[i] = null;
      this.glyphPositions[i] = null;
    }
    this.connectors = new ConnectorObject[connectorCount];
    this.connectorPositions = new Point[connectorCount];
    for (int i = 0; i < connectorCount; i++) {
      this.connectors[i] = null;
      this.connectorPositions[i] = null;
    }
  }

  // glyphs
  public void setGlyph(int index, MemoryGlyph glyph) {
    this.glyphs[index] = glyph;
  }

  public void setGlyphPosition(int index, Point position) {
    this.glyphPositions[index] = position;
  }

  public MemoryGlyph[] getGlyphs() {
    return this.glyphs;
  }

  public Point[] getGlyphPositions() {
    return this.glyphPositions;
  }

  public boolean isGlyphNull(int index) {
    return (this.glyphs[index] == null);
  }

  // connectors
  public void setConnector(int index, ConnectorObject connector) {
    this.connectors[index] = connector;
  }

  public void setConnectorPosition(int index, Point position) {
    this.connectorPositions[index] = position;
  }

  public ConnectorObject[] getConnectors() {
    return this.connectors;
  }

  public Point[] getConnectorPositions() {
    return this.connectorPositions;
  }

  public boolean isConnectorNull(int index) {
    return (this.connectors[index] == null);
  }
}
