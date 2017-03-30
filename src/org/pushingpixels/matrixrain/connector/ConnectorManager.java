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

import org.pushingpixels.matrixrain.auxiliary.math.GaussValues;
import org.pushingpixels.matrixrain.auxiliary.math.RandomalityTester;

public class ConnectorManager {

    public class ConnectorChainElement {
        public String connectorChain;

        public double coefficient;

        public ConnectorChainElement next;

        public ConnectorChainElement prev;

        public ConnectorChainElement(String connectorChain, double coefficient) {
            this.connectorChain = connectorChain;
            this.coefficient = coefficient;
            this.next = null;
            this.prev = null;
        }
    }

    public class ConnectorChainList {
        public ConnectorChainElement head;

        public ConnectorChainElement tail;

        public int length;

        public ConnectorChainList() {
            head = null;
            tail = null;
            length = 0;
        }

        public synchronized void addConnectorChain(String connectorChain,
                double coefficient) {
            ConnectorChainElement newElement = new ConnectorChainElement(
                    connectorChain, coefficient);
            newElement.next = this.head;
            if (this.head != null)
                this.head.prev = newElement;
            if (this.head == null)
                this.tail = newElement;
            this.head = newElement;
            length++;
        }

        public synchronized void addConnectorChainAtTail(String connectorChain,
                double coefficient) {
            ConnectorChainElement newElement = new ConnectorChainElement(
                    connectorChain, coefficient);
            if (this.tail == null) {
                // empty list
                this.head = newElement;
                this.tail = newElement;
            } else {
                this.tail.next = newElement;
                newElement.prev = this.tail;
                this.tail = newElement;
            }
            length++;
        }

        public synchronized void removeConnectorChain(
                ConnectorChainElement element) {
            // update tail pointer
            if (element == this.tail) {
                this.tail = element.prev;
            }

            if (element == this.head) {
                this.head = element.next;
                if (this.head != null)
                    this.head.prev = null;
            } else {
                if (element.next != null)
                    element.next.prev = element.prev;
                if (element.prev != null)
                    element.prev.next = element.next;
            }
            length--;
        }

        public double getHighestCoefficient() {
            double currHigh = -1.0;
            ConnectorChainElement currElement = this.head;
            while (currElement != null) {
                currHigh = Math.max(currHigh, currElement.coefficient);
                currElement = currElement.next;
            }
            return currHigh;
        }

        public ConnectorChainElement getElementWithHighestCoefficient() {
            double currHigh = -1.0;
            ConnectorChainElement currElement = this.head;
            ConnectorChainElement result = null;
            while (currElement != null) {
                if (currElement.coefficient > currHigh) {
                    currHigh = currElement.coefficient;
                    result = currElement;
                }
                currElement = currElement.next;
            }
            return result;
        }

        public String toString() {
            String result = "";
            ConnectorChainElement currElement = this.head;
            while (currElement != null) {
                result += (currElement.connectorChain + "("
                        + currElement.coefficient + "), ");
                currElement = currElement.next;
            }
            return result;
        }
    }

    public ConnectorManager() {
    }

    private void exploreConnectorChains(ConnectorChainList chainList,
            String inputString, int currPos, int lastConnector,
            String currConnectorString, String[] prevConnectorStrings) {

        // if after last letter - add this connector string
        if (currPos >= inputString.length()) {
            // compute quality-probability coefficient
            double quality = this.getConnectorChainQuality(inputString,
                    currConnectorString);
            double probability = RandomalityTester.getChi2Probability(
                    currConnectorString, prevConnectorStrings)
                    * RandomalityTester.getMedianProbability(
                            currConnectorString, prevConnectorStrings);
            double coefficient = quality * probability;
            chainList.addConnectorChain(currConnectorString, coefficient);
            return;
        }

        // not last letter

        // Go over all connectors (1..3).
        // The connectors that are not good are:
        // - the last one
        // - the one that is rated 0 for one of the letters
        // - the one that is irrelevant for both of the letters
        char prevChar = inputString.charAt(currPos - 1);
        char nextChar = inputString.charAt(currPos);
        for (int connectorIndex = 1; connectorIndex <= 3; connectorIndex++) {
            if (connectorIndex == lastConnector)
                continue;
            if (ConnectorMap.getRatingRight(prevChar, connectorIndex) == 0)
                continue;
            if (ConnectorMap.getRatingLeft(nextChar, connectorIndex) == 0)
                continue;
            if ((!ConnectorMap.isRelevantRight(prevChar, connectorIndex))
                    && (!ConnectorMap.isRelevantLeft(nextChar, connectorIndex)))
                continue;

            String newConnectorString = currConnectorString + connectorIndex;
            this.exploreConnectorChains(chainList, inputString, currPos + 1,
                    connectorIndex, newConnectorString, prevConnectorStrings);
        }
    }

    public ConnectorChainList computeAllConnectorChains(String inputString,
            String[] prevConnectorStrings) {
        if (inputString.length() <= 1)
            return null;
        ConnectorChainList ccList = new ConnectorChainList();
        this.exploreConnectorChains(ccList, inputString, 1, -1, "",
                prevConnectorStrings);

        // System.out.println(ccList.toString());
        if (ccList.length == 0)
            return null;
        return ccList;
    }

    private String[] createStringArrayFromList(ConnectorChainList ccList) {
        if ((ccList == null) || (ccList.length == 0))
            return null;

        String[] result = new String[ccList.length];
        ConnectorChainElement currElem = ccList.head;
        int count = 0;
        while (currElem != null) {
            result[count++] = currElem.connectorChain;
            currElem = currElem.next;
        }
        return result;
    }

    public String[] getConnectorChains(String inputString) {
        ConnectorChainList ccList = this.computeAllConnectorChains(inputString,
                null);
        return this.createStringArrayFromList(ccList);
    }

    public ConnectorChainList getSortedConnectorChains(
            ConnectorChainList ccList) {
        if (ccList == null)
            return null;
        ConnectorChainList ccSortedList = new ConnectorChainList();
        while (ccList.length > 0) {
            ConnectorChainElement currBiggestElem = ccList
                    .getElementWithHighestCoefficient();
            ccSortedList
                    .addConnectorChainAtTail(currBiggestElem.connectorChain,
                            currBiggestElem.coefficient);
            ccList.removeConnectorChain(currBiggestElem);
        }
        return ccSortedList;
    }

    public String[] getSortedConnectorChains(String inputString) {
        ConnectorChainList ccList = this.computeAllConnectorChains(inputString,
                null);
        ConnectorChainList ccSortedList = this.getSortedConnectorChains(ccList);
        return this.createStringArrayFromList(ccSortedList);
    }

    private String getConnectorChainNth(ConnectorChainList ccList, int n) {
        // start removing n-1 biggest values from the list
        int index = n;
        while (true) {
            // if last element left (n >= original count) - return it
            if (ccList.length == 1)
                return ccList.head.connectorChain;

            double highestCoef = ccList.getHighestCoefficient();
            ConnectorChainElement currElem = ccList.head;
            while (currElem != null) {
                if (Math.abs(currElem.coefficient - highestCoef) < 1.e-06) {
                    if (index == 0)
                        return currElem.connectorChain;
                    index--;
                    ccList.removeConnectorChain(currElem);
                    break;
                }
                currElem = currElem.next;
            }
        }
    }

    public String getConnectorChainNth(String inputString, int n) {
        if (inputString.length() <= 1)
            return null;
        ConnectorChainList ccList = new ConnectorChainList();
        this.exploreConnectorChains(ccList, inputString, 1, -1, "", null);

        if (ccList.length == 0)
            return null;
        return getConnectorChainNth(ccList, n);
    }

    private String getGaussianConnectorChain(ConnectorChainList ccSortedList) {
        if ((ccSortedList == null) || (ccSortedList.length == 0))
            return null;

        if (ccSortedList.length == 1)
            return ccSortedList.head.connectorChain;

        // compute the sum of all coefficients
        double sum = 0.0;
        ConnectorChainElement currElem = ccSortedList.head;
        while (currElem != null) {
            sum += currElem.coefficient;
            currElem = currElem.next;
        }
        // get random number in 0..100 range with gaussian distribution
        int index100 = GaussValues.getRandomGaussian(100);
        // For each coefficient we have its "relative percentage". Go over all
        // the possibilities and find the one whose percentage covers this
        // random
        // index
        currElem = ccSortedList.head;
        double currRemainder = index100;
        while (currElem != null) {
            currRemainder -= (100.0 * currElem.coefficient / sum);
            if (currRemainder < 0.0)
                return currElem.connectorChain;
            currElem = currElem.next;
        }
        // if here - take the last connector chain
        return ccSortedList.tail.connectorChain;
        /*
         * int index = RandomalityTester.getRandomGaussian(ccList.length);
         * System.out.print(index+" "); return getConnectorChainNth(ccList,
         * index);
         */
    }

    public String getGaussianConnectorChain(String inputString) {
        ConnectorChainList ccList = this.computeAllConnectorChains(inputString,
                null);
        ConnectorChainList ccSortedList = this.getSortedConnectorChains(ccList);
        return getGaussianConnectorChain(ccSortedList);
    }

    public String[] getGaussianConnectorChain(String[] inputStrings) {
        // For the first word - regular draw.
        // For each consecutive word compute the probability based on all the
        // previous connectors selected
        if ((inputStrings == null) || (inputStrings.length == 0))
            return null;

        int length = inputStrings.length;
        String[] result = new String[length];
        result[0] = this.getGaussianConnectorChain(inputStrings[0]);
        for (int i = 1; i < length; i++) {
            String[] prevConnectors = new String[i];
            // copy previous connector strings
            for (int j = 0; j < i; j++)
                prevConnectors[j] = result[j];

            // Compute all possible connector strings for this word. Coefficient
            // is
            // computed based also on the previously selected connector strings
            ConnectorChainList ccList = this.computeAllConnectorChains(
                    inputStrings[i], prevConnectors);
            ConnectorChainList ccSortedList = this
                    .getSortedConnectorChains(ccList);
            result[i] = getGaussianConnectorChain(ccSortedList);
        }

        return result;
    }

    public double getConnectorChainQuality(String inputString,
            String connectorChainString) {
        int length = inputString.length();
        if (length <= 1)
            return 0;

        int sum = 0;
        for (int i = 0; i < (length - 1); i++) {
            char thisChar = inputString.charAt(i);
            char nextChar = inputString.charAt(i + 1);
            int connector = new Integer("" + connectorChainString.charAt(i))
                    .intValue();
            // from right side
            sum += ConnectorMap.getRatingLeft(nextChar, connector);
            // from left side
            sum += ConnectorMap.getRatingRight(thisChar, connector);
        }

        return (double) sum / (double) (2 * (length - 1));
    }

    public double getConnectorChainsQuality(String[] inputStrings,
            String[] connectorChainStrings) {
        int wordCount = inputStrings.length;
        double sum = 0.0;
        double count = 0;
        for (int i = 0; i < wordCount; i++) {
            double currQuality = this.getConnectorChainQuality(inputStrings[i],
                    connectorChainStrings[i]);
            sum += (currQuality * inputStrings[i].length());
            count += inputStrings[i].length();
        }
        return sum / count;
    }
}