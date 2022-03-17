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

import java.util.StringTokenizer;

import org.pushingpixels.matrixrain.connector.ConnectorManager;
import org.pushingpixels.matrixrain.connector.ConnectorObject;
import org.pushingpixels.matrixrain.font.GlyphFactory;
import org.pushingpixels.matrixrain.font.MemoryGlyph;
import org.pushingpixels.matrixrain.position.TitleConnectorPosition;
import org.pushingpixels.matrixrain.position.TitleGlyphPosition;

public class TitleManager {
  private int windowWidth;

  private int windowHeight;

  private int titleGlyphsCount;

  private MemoryGlyph[] titleGlyphs;

  private TitleGlyphPosition[] titleGlyphsPositions;

  private TitleGlyphPosition[] titleGlyphsPositionsClone;

  private int titleConnectorsCount;

  private ConnectorObject[] titleConnectors;

  private TitleConnectorPosition[] titleConnectorPositions;

  private TitleConnectorPosition[] titleConnectorPositionsClone;

  private int prevEndTime;

  public TitleManager(int windowWidth, int windowHeight) {
    this.windowHeight = windowHeight;
    this.windowWidth = windowWidth;
  }

  private int getRandom(int maxValue) {
    double val = Math.random();
    return (int) (Math.floor(val * maxValue));
  }

  public synchronized void showTitle(
      String title, GlyphFactory abcGlyphFactory, int lineHeight, int connectorGlow) {
    String lcTitle = title.toLowerCase();

    // 1. break title into words
    StringTokenizer t = new StringTokenizer(lcTitle);
    String[] words = new String[t.countTokens()];
    int wordCount = 0;
    for (; t.hasMoreTokens(); ) {
      words[wordCount++] = t.nextToken();
    }

    // 2. break words into lines
    int wordLineIndices[] = new int[wordCount];
    int maxWidth = this.windowWidth - 20;
    int lineCount = 0;
    int currLineLength = 0;
    for (int wordIndex = 0; wordIndex < wordCount; wordIndex++) {
      // !!!
      int currWordLength = 40 * words[wordIndex].length();
      currLineLength += currWordLength;
      if (currLineLength > maxWidth) {
        // put this word on the next line
        lineCount++;
        currLineLength = currWordLength;
        wordLineIndices[wordIndex] = lineCount;
      } else {
        wordLineIndices[wordIndex] = lineCount;
        // add a space
        // !!!
        currLineLength += 40;
        if (currLineLength > maxWidth) {
          lineCount++;
          currLineLength = 0;
        }
      }
    }
    lineCount++;

    // 3. break title into lines
    int totalLetterCount = 0;
    String lines[] = new String[lineCount];
    for (int lineIndex = 0; lineIndex < lineCount; lineIndex++) lines[lineIndex] = new String();
    for (int wordIndex = 0; wordIndex < wordCount; wordIndex++) {
      int lineIndex = wordLineIndices[wordIndex];
      if (lines[lineIndex].length() > 0) {
        totalLetterCount++;
        lines[lineIndex] += " ";
      }
      lines[lineIndex] += words[wordIndex];
      totalLetterCount += words[wordIndex].length();
    }

    // 4. create glyphs (warped) for each letter
    this.titleGlyphs = new MemoryGlyph[totalLetterCount];
    int currLetterIndex = 0;
    for (int lineIndex = 0; lineIndex < lineCount; lineIndex++) {
      String currLine = lines[lineIndex];
      int currLineLetterCount = currLine.length();
      for (int i = 0; i < currLineLetterCount; i++) {
        char currLetter = currLine.charAt(i);
        if (currLetter == ' ') {
          this.titleGlyphs[currLetterIndex] = null;
        } else {
          abcGlyphFactory.rewarpGlyph(currLetter - 'a', 0, 2, 1);
          this.titleGlyphs[currLetterIndex] =
              abcGlyphFactory.getGlyph(currLetter - 'a', 0, 2, 1).generateRandomWarpedGlyph();
        }
        currLetterIndex++;
      }
    }

    // 5. compute start and end positions for each letter
    int firstLineHeight = this.windowHeight / 2 - (int) ((lineCount - 0.5) * lineHeight) / 2;
    // ((lineCount-1)*abcGlyphFactory.getOriginalSize())/2;
    titleGlyphsPositions = new TitleGlyphPosition[totalLetterCount];
    currLetterIndex = 0;
    for (int lineIndex = 0; lineIndex < lineCount; lineIndex++) {
      String currLine = lines[lineIndex];
      // compute total width of this line
      int origGlyphWidth = abcGlyphFactory.getOriginalSize();
      int interGlyphSpace = origGlyphWidth / 4;
      int totalLineWidth = 0;
      int letterCount = currLine.length();
      for (int i = 0; i < letterCount; i++) {
        MemoryGlyph currLetterGlyph = this.titleGlyphs[currLetterIndex + i];
        if (currLetterGlyph == null) totalLineWidth += abcGlyphFactory.getOriginalSize();
        else totalLineWidth += currLetterGlyph.getWidth();
        // add spacing
        totalLineWidth += interGlyphSpace;
      }
      // subtract spacing after the last letter
      totalLineWidth -= interGlyphSpace;
      // System.out.println("line: " + lineIndex + ", total width: " +
      // totalLineWidth);

      // start x position for the first letter of this line
      int startX = (this.windowWidth - totalLineWidth) / 2;
      // end y position for this line
      int endY = firstLineHeight + abcGlyphFactory.getOriginalSize() * lineIndex;

      for (int i = 0; i < letterCount; i++) {
        TitleGlyphPosition tgp = new TitleGlyphPosition();
        tgp.setX(startX);
        tgp.setStartY(0);
        tgp.setEndY(endY);
        this.titleGlyphsPositions[currLetterIndex] = tgp;

        // System.out.print("line: " + lineIndex + ", letter: " + i);
        // System.out.println(" startX : " + startX + ", endY: " +
        // endY);

        // advance to next letter
        MemoryGlyph currLetterGlyph = this.titleGlyphs[currLetterIndex];
        if (currLetterGlyph == null) startX += abcGlyphFactory.getOriginalSize();
        else startX += currLetterGlyph.getWidth();
        // add spacing
        startX += interGlyphSpace;

        currLetterIndex++;
      }
    }

    // 6. Compute drop speed, drop start time and connector parameters
    // Drop letters in this line by pairs. Each pair starts to drop shortly
    // after the previous pair has reached its final positions. The letters
    // in each pair drop "almost" together - there is random delta in start
    // time and random small delta in drop speed

    // The first pair in the next line starts to drop shortly after the last
    // pair of the previous line

    // If the number of letters in a line is odd, the first "pair" is
    // actually
    // only one letter

    // For each falling pair we compute the connector parameters (x, y, when
    // starts to appear)

    String[] connectors = new ConnectorManager().getGaussianConnectorChain(words);
    this.titleConnectorsCount = 0;
    if (connectors != null) {
      for (int currConnector = 0; currConnector < connectors.length; currConnector++)
        if (connectors[currConnector] != null)
          this.titleConnectorsCount += connectors[currConnector].length();
    }
    /*
     * System.out.println(""+this.titleConnectorsCount+ " connectors"); for
     * (int j=0; j<connectors.length; j++) if (connectors[j] == null)
     * System.out.print("[] "); else System.out.print("[" + connectors[j] + "]
     * "); System.out.println();
     */
    this.titleConnectors = new ConnectorObject[this.titleConnectorsCount];
    this.titleConnectorPositions = new TitleConnectorPosition[this.titleConnectorsCount];

    int currConnectorChainIndex = 0;
    int connectorInCurrChain = 0;
    int currConnectorIndex = 0;

    currLetterIndex = 0;
    int currLetterIndexInTitle = 0;

    prevEndTime = 0; // end drop time of the last letter
    for (int lineIndex = 0; lineIndex < lineCount; lineIndex++) {
      String currLine = lines[lineIndex];
      // compute total width of this line
      int letterCount = currLine.length();

      // compute whether the first pair is a single letter
      boolean firstIsPair = (letterCount % 2 == 0);
      int firstLetterIndex;
      if (firstIsPair) firstLetterIndex = letterCount / 2 - 1;
      else firstLetterIndex = (letterCount - 1) / 2 - 1;

      if (!firstIsPair) {
        TitleGlyphPosition tgp = this.titleGlyphsPositions[currLetterIndex + firstLetterIndex + 1];
        tgp.setTimeToStartDropping(prevEndTime);
        int deltaY = 30 + this.getRandom(5);
        tgp.setDeltaY(deltaY);
        prevEndTime += (tgp.getEndY() / deltaY + this.getRandom(3));
      }

      for (int firstPairLetter = firstLetterIndex; firstPairLetter >= 0; firstPairLetter--) {
        TitleGlyphPosition tgpL = this.titleGlyphsPositions[currLetterIndex + firstPairLetter];
        TitleGlyphPosition tgpR =
            this.titleGlyphsPositions[currLetterIndex + (letterCount - 1 - firstPairLetter)];

        int startL = prevEndTime;
        int startR = prevEndTime + this.getRandom(2);
        tgpL.setTimeToStartDropping(startL);
        tgpR.setTimeToStartDropping(startR);
        int deltaYL = 30 + this.getRandom(5);
        int deltaYR = deltaYL + this.getRandom(3) - 1;
        tgpL.setDeltaY(deltaYL);
        tgpR.setDeltaY(deltaYR);
        int dropTimeL = tgpL.getEndY() / deltaYL;
        int dropTimeR = tgpR.getEndY() / deltaYR;
        prevEndTime += (Math.max(dropTimeL, dropTimeR) + this.getRandom(2));
      }

      if (connectors == null) continue;

      int currLetterIndexInLine = 0;
      // System.out.println("we have " + letterCount + " letters");
      while (currLetterIndexInLine < (letterCount - 1)) {
        MemoryGlyph glyphLeft = this.titleGlyphs[currLetterIndexInTitle];
        MemoryGlyph glyphRight = this.titleGlyphs[currLetterIndexInTitle + 1];
        /*
         * if (glyphLeft == null) System.out.println("glyph " +
         * currLetterIndexInTitle + " null"); else
         * System.out.println("glyph " + currLetterIndexInTitle + " not
         * null"); if (glyphRight == null) System.out.println("glyph " +
         * (currLetterIndexInTitle+1) + " null"); else
         * System.out.println("glyph " + (currLetterIndexInTitle+1) + "
         * not null");
         */
        if (glyphRight == null) {
          // space
          // System.out.println("Going to next word");
          currLetterIndexInLine += 2;
          currLetterIndexInTitle += 2;
          currConnectorChainIndex++;
          connectorInCurrChain = 0;
          continue;
        }
        TitleGlyphPosition tgpL = this.titleGlyphsPositions[currLetterIndexInTitle];
        TitleGlyphPosition tgpR = this.titleGlyphsPositions[currLetterIndexInTitle + 1];

        // System.out.println("letters " + currLetterIndexInTitle + "-"
        // + (currLetterIndexInTitle+1));

        int connectorType = connectors[currConnectorChainIndex].charAt(connectorInCurrChain) - '0';

        // System.out.println("connector type " + connectorType);

        int connectorStartX, connectorEndX, connectorY = 0;
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
        connectorStartX =
            tgpL.getX()
                - glyphLeft.getLeft()
                + glyphLeft.getRightConnectorX(connectorY, connectorGlow);
        connectorEndX =
            tgpR.getX()
                - glyphRight.getLeft()
                + glyphRight.getLeftConnectorX(connectorY, connectorGlow);

        // System.out.println("Connector x " + connectorStartX + "-" +
        // connectorEndX + ", y " + connectorY);

        // add connector info
        // System.out.println("Setting info for connector " +
        // currConnectorIndex);
        this.titleConnectors[currConnectorIndex] =
            new ConnectorObject(connectorEndX - connectorStartX + 1, 3);
        TitleConnectorPosition tcp = new TitleConnectorPosition();
        tcp.setX(connectorStartX);
        tcp.setY(tgpL.getEndY() + connectorY - 1);
        tcp.setStartGlowIndex(0);
        tcp.setEndGlowIndex(connectorGlow);
        tcp.setGlowDelta(10);

        int deltaYL = tgpL.getDeltaY();
        int deltaYR = tgpR.getDeltaY();
        int dropTimeL = tgpL.getTimeToStartDropping() + tgpL.getEndY() / deltaYL;
        int dropTimeR = tgpR.getTimeToStartDropping() + tgpR.getEndY() / deltaYR;
        tcp.setTimeToStartAppearing(1 + Math.max(dropTimeL, dropTimeR));

        // System.out.println("Connector starttime " +
        // (1+Math.max(dropTimeL, dropTimeR)));

        this.titleConnectorPositions[currConnectorIndex] = tcp;

        currConnectorIndex++;
        connectorInCurrChain++;
        currLetterIndexInLine++;
        currLetterIndexInTitle++;
      }
      // go to next connector
      // System.out.println("Going to next word");
      currConnectorChainIndex++;
      connectorInCurrChain = 0;
      // take into account that the last letter is not connected from
      // right
      currLetterIndexInTitle++;

      currLetterIndex += letterCount;
      // end of the line
      prevEndTime += (10 + this.getRandom(5));
    }

    this.titleGlyphsCount = totalLetterCount;
  }

  public void makeClone() {
    // store positions
    int count = this.titleGlyphsCount;
    this.titleGlyphsPositionsClone = new TitleGlyphPosition[count];
    for (int i = 0; i < count; i++) {
      if (this.titleGlyphsPositions[i] == null) this.titleGlyphsPositionsClone[i] = null;
      else
        this.titleGlyphsPositionsClone[i] =
            (TitleGlyphPosition) this.titleGlyphsPositions[i].clone();
    }

    count = this.titleConnectorsCount;
    this.titleConnectorPositionsClone = new TitleConnectorPosition[count];
    for (int i = 0; i < count; i++) {
      if (this.titleConnectorPositions[i] == null) this.titleConnectorPositionsClone[i] = null;
      else
        this.titleConnectorPositionsClone[i] =
            (TitleConnectorPosition) this.titleConnectorPositions[i].clone();
    }
  }

  public void restore() {
    this.titleConnectorPositions = this.titleConnectorPositionsClone;
    this.titleGlyphsPositions = this.titleGlyphsPositionsClone;
    this.makeClone();
  }

  public void iteration(int delta) {
    for (int i = 0; i < this.titleGlyphsCount; i++) this.titleGlyphsPositions[i].iteration(delta);
    for (int i = 0; i < this.titleConnectorsCount; i++)
      this.titleConnectorPositions[i].iteration(delta);
  }

  public int getTitleGlyphsCount() {
    return this.titleGlyphsCount;
  }

  public MemoryGlyph getTitleGlyph(int index) {
    return this.titleGlyphs[index];
  }

  public MemoryGlyph[] getTitleGlyphs() {
    return this.titleGlyphs;
  }

  public TitleGlyphPosition getTitleGlyphPosition(int index) {
    return this.titleGlyphsPositions[index];
  }

  public TitleGlyphPosition[] getTitleGlyphPositions() {
    return this.titleGlyphsPositions;
  }

  public int getTitleConnectorsCount() {
    return this.titleConnectorsCount;
  }

  public ConnectorObject getTitleConnector(int index) {
    return this.titleConnectors[index];
  }

  public ConnectorObject[] getTitleConnectors() {
    return this.titleConnectors;
  }

  public TitleConnectorPosition getTitleConnectorPosition(int index) {
    return this.titleConnectorPositions[index];
  }

  public TitleConnectorPosition[] getTitleConnectorPositions() {
    return this.titleConnectorPositions;
  }

  public int getTitleAnimationEndTime() {
    int maxTime = this.prevEndTime;
    for (int i = 0; i < this.titleConnectorsCount; i++)
      maxTime = Math.max(maxTime, this.titleConnectorPositions[i].getStabilizationTime());
    return maxTime;
  }
}
