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
package org.pushingpixels.matrixrain.connector;

public class ConnectorMap {
    private static boolean isInitialized = false;

    private static class SingleLetterRating {
        public int[] leftSideRating;

        public int[] rightSideRating;

        public SingleLetterRating(int[] leftSide, int[] rightSide) {
            this.leftSideRating = leftSide;
            this.rightSideRating = rightSide;
        }
    }

    private static class SingleLetterRelevancy {
        public boolean[] leftSideRelevancy;

        public boolean[] rightSideRelevancy;

        public SingleLetterRelevancy(boolean[] leftSide, boolean[] rightSide) {
            this.leftSideRelevancy = leftSide;
            this.rightSideRelevancy = rightSide;
        }
    }

    private static SingleLetterRating[] letterRatings;

    private static SingleLetterRelevancy[] letterRelevances;

    private static double median;

    private static double[] probabilities;

    private static void initialize() {
        letterRatings = new SingleLetterRating[26];
        letterRelevances = new SingleLetterRelevancy[26];
        // A
        letterRatings[0] = new SingleLetterRating(new int[] { 2, 3, 5 }, new int[] { 2, 3, 5 });
        letterRelevances[0] = new SingleLetterRelevancy(new boolean[] { true, true, true },
                new boolean[] { true, true, true });
        // B
        letterRatings[1] = new SingleLetterRating(new int[] { 5, 4, 5 }, new int[] { 2, 0, 2 });
        letterRelevances[1] = new SingleLetterRelevancy(new boolean[] { true, true, true },
                new boolean[] { true, true, true });
        // C
        letterRatings[2] = new SingleLetterRating(new int[] { 2, 5, 2 }, new int[] { 2, 0, 2 });
        letterRelevances[2] = new SingleLetterRelevancy(new boolean[] { true, false, true },
                new boolean[] { true, false, true });
        // D
        letterRatings[3] = new SingleLetterRating(new int[] { 5, 4, 5 }, new int[] { 2, 5, 2 });
        letterRelevances[3] = new SingleLetterRelevancy(new boolean[] { true, false, true },
                new boolean[] { true, false, true });
        // E
        letterRatings[4] = new SingleLetterRating(new int[] { 5, 4, 5 }, new int[] { 2, 1, 2 });
        letterRelevances[4] = new SingleLetterRelevancy(new boolean[] { true, true, true },
                new boolean[] { true, true, true });
        // F
        letterRatings[5] = new SingleLetterRating(new int[] { 5, 4, 5 }, new int[] { 2, 1, 0 });
        letterRelevances[5] = new SingleLetterRelevancy(new boolean[] { true, true, true },
                new boolean[] { true, true, true });
        // G
        letterRatings[6] = new SingleLetterRating(new int[] { 2, 5, 2 }, new int[] { 2, 5, 2 });
        letterRelevances[6] = new SingleLetterRelevancy(new boolean[] { true, false, true },
                new boolean[] { true, true, true });
        // H
        letterRatings[7] = new SingleLetterRating(new int[] { 5, 4, 5 }, new int[] { 5, 4, 5 });
        letterRelevances[7] = new SingleLetterRelevancy(new boolean[] { true, true, true },
                new boolean[] { true, true, true });
        // I
        letterRatings[8] = new SingleLetterRating(new int[] { 5, 4, 5 }, new int[] { 5, 4, 5 });
        letterRelevances[8] = new SingleLetterRelevancy(new boolean[] { true, false, true },
                new boolean[] { true, false, true });
        // J
        letterRatings[9] = new SingleLetterRating(new int[] { 1, 0, 2 }, new int[] { 5, 4, 2 });
        letterRelevances[9] = new SingleLetterRelevancy(new boolean[] { true, false, true },
                new boolean[] { true, false, true });
        // K
        letterRatings[10] = new SingleLetterRating(new int[] { 5, 4, 5 }, new int[] { 5, 0, 5 });
        letterRelevances[10] = new SingleLetterRelevancy(new boolean[] { true, false, true },
                new boolean[] { true, false, true });
        // L
        letterRatings[11] = new SingleLetterRating(new int[] { 5, 4, 5 }, new int[] { 1, 0, 5 });
        letterRelevances[11] = new SingleLetterRelevancy(new boolean[] { true, false, true },
                new boolean[] { true, false, true });
        // M
        letterRatings[12] = new SingleLetterRating(new int[] { 5, 4, 5 }, new int[] { 5, 4, 5 });
        letterRelevances[12] = new SingleLetterRelevancy(new boolean[] { true, false, true },
                new boolean[] { true, false, true });
        // N
        letterRatings[13] = new SingleLetterRating(new int[] { 5, 4, 5 }, new int[] { 5, 4, 5 });
        letterRelevances[13] = new SingleLetterRelevancy(new boolean[] { true, false, true },
                new boolean[] { true, false, true });
        // O
        letterRatings[14] = new SingleLetterRating(new int[] { 2, 5, 2 }, new int[] { 2, 5, 2 });
        letterRelevances[14] = new SingleLetterRelevancy(new boolean[] { true, false, true },
                new boolean[] { true, false, true });
        // P
        letterRatings[15] = new SingleLetterRating(new int[] { 5, 4, 5 }, new int[] { 2, 1, 5 });
        letterRelevances[15] = new SingleLetterRelevancy(new boolean[] { true, true, true },
                new boolean[] { true, true, true });
        // Q
        letterRatings[16] = new SingleLetterRating(new int[] { 2, 5, 1 }, new int[] { 2, 5, 1 });
        letterRelevances[16] = new SingleLetterRelevancy(new boolean[] { true, false, true },
                new boolean[] { true, false, true });
        // R
        letterRatings[17] = new SingleLetterRating(new int[] { 5, 4, 5 }, new int[] { 2, 0, 5 });
        letterRelevances[17] = new SingleLetterRelevancy(new boolean[] { true, true, true },
                new boolean[] { true, true, true });
        // S
        letterRatings[18] = new SingleLetterRating(new int[] { 2, 0, 2 }, new int[] { 2, 0, 2 });
        letterRelevances[18] = new SingleLetterRelevancy(new boolean[] { true, false, true },
                new boolean[] { true, false, true });
        // T
        letterRatings[19] = new SingleLetterRating(new int[] { 5, 1, 2 }, new int[] { 5, 1, 2 });
        letterRelevances[19] = new SingleLetterRelevancy(new boolean[] { true, false, true },
                new boolean[] { true, false, true });
        // U
        letterRatings[20] = new SingleLetterRating(new int[] { 5, 4, 2 }, new int[] { 5, 4, 2 });
        letterRelevances[20] = new SingleLetterRelevancy(new boolean[] { true, false, true },
                new boolean[] { true, false, true });
        // V
        letterRatings[21] = new SingleLetterRating(new int[] { 5, 3, 1 }, new int[] { 5, 3, 1 });
        letterRelevances[21] = new SingleLetterRelevancy(new boolean[] { true, false, true },
                new boolean[] { true, false, true });
        // W
        letterRatings[22] = new SingleLetterRating(new int[] { 5, 3, 1 }, new int[] { 5, 3, 1 });
        letterRelevances[22] = new SingleLetterRelevancy(new boolean[] { true, false, true },
                new boolean[] { true, false, true });
        // X
        letterRatings[23] = new SingleLetterRating(new int[] { 5, 0, 5 }, new int[] { 5, 0, 5 });
        letterRelevances[23] = new SingleLetterRelevancy(new boolean[] { true, false, true },
                new boolean[] { true, false, true });
        // Y
        letterRatings[24] = new SingleLetterRating(new int[] { 5, 1, 2 }, new int[] { 5, 1, 2 });
        letterRelevances[24] = new SingleLetterRelevancy(new boolean[] { true, false, true },
                new boolean[] { true, false, true });
        // Z
        letterRatings[25] = new SingleLetterRating(new int[] { 5, 0, 5 }, new int[] { 5, 0, 5 });
        letterRelevances[25] = new SingleLetterRelevancy(new boolean[] { true, false, true },
                new boolean[] { true, false, true });

        int sum = 0;
        probabilities = new double[3];
        int nonZeroCount = 0;
        for (int i = 0; i < 3; i++)
            probabilities[i] = 0.0;
        for (int i = 0; i < 26; i++) {
            for (int j = 0; j < 3; j++) {
                if (letterRatings[i].leftSideRating[j] > 0) {
                    sum += j;
                    nonZeroCount++;
                    probabilities[j] += 1.0;
                }
                if (letterRatings[i].rightSideRating[j] > 0) {
                    sum += j;
                    nonZeroCount++;
                    probabilities[j] += 1.0;
                }
            }
        }
        median = 1.0 + (double) sum / (double) (nonZeroCount);
        for (int i = 0; i < 3; i++)
            probabilities[i] /= (double) (nonZeroCount);

        isInitialized = true;
    }

    public static boolean isRelevantLeft(char c, int connector) {
        if (!isInitialized)
            initialize();
        return letterRelevances[c - 'a'].leftSideRelevancy[connector - 1];
    }

    public static boolean isRelevantRight(char c, int connector) {
        if (!isInitialized)
            initialize();
        return letterRelevances[c - 'a'].rightSideRelevancy[connector - 1];
    }

    public static int getRatingLeft(char c, int connector) {
        if (!isInitialized)
            initialize();
        return letterRatings[c - 'a'].leftSideRating[connector - 1];
    }

    public static int getRatingRight(char c, int connector) {
        if (!isInitialized)
            initialize();
        return letterRatings[c - 'a'].rightSideRating[connector - 1];
    }

    public static double getMedian() {
        if (!isInitialized)
            initialize();
        return median;
    }

    public static double[] getProbabilities() {
        if (!isInitialized)
            initialize();
        return probabilities;
    }
}
