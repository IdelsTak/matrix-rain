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
package org.pushingpixels.matrixrain.intro.title;

import java.awt.Point;

import org.pushingpixels.matrixrain.connector.ConnectorObject;
import org.pushingpixels.matrixrain.font.MemoryGlyph;

public class TitleFrame {
  // if glyph is completely outside the screen, the corresponding
  // entry in the following two arrays is null
  private MemoryGlyph[] glyphs;

  private Point[] glyphPositions;

  // if connector is completely outside the screen, the corresponding
  // entry in the following two arrays is null
  private ConnectorObject[] connectors;

  private Point[] connectorPositions;

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
