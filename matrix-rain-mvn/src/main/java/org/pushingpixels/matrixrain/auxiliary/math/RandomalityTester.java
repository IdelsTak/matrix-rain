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
package org.pushingpixels.matrixrain.auxiliary.math;

import org.pushingpixels.matrixrain.connector.ConnectorMap;

public final class RandomalityTester {
  private static double gamma(double x) {
    double dt = 0.1;
    double result = 0.0;
    double t = 0;
    while (true) {
      double delta = Math.pow(t, x - 1) * Math.exp(-t);
      // step out when current delta is too small
      if (delta < 1.0e-03) return result;
      result += (delta * dt);
      t += dt;
    }
  }

  private static double chi2integral(double chi2, double d) {
    double d2 = d / 2.0;

    double dt = 0.1;
    double result = 0.0;
    double t = chi2;
    while (true) {
      double delta = Math.pow(t, d2 - 1) * Math.exp(-t / 2);
      // step out when current delta is too small
      if (delta < 1.0e-03) return result;
      result += (delta * dt);
      t += dt;
    }
  }

  private static double getChi2Probability(double chi2, double d) {
    return chi2integral(chi2, d) / (Math.pow(2.0, d / 2.0) * gamma(d / 2.0));
  }

  private static double getChi2(int tries, int[] outcomes, double[] probabilities) {
    int k = outcomes.length;
    double result = 0.0;
    for (int i = 0; i < k; i++) {
      double delta = outcomes[i] - tries * probabilities[i];
      result += ((delta * delta) / (tries * probabilities[i]));
    }
    return result;
  }

  private static double getChi2(String connectorString) {
    double[] probabilities = ConnectorMap.getProbabilities();
    int[] outcomes = new int[3];
    for (int i = 0; i < 3; i++) outcomes[i] = 0;
    for (int i = 0; i < connectorString.length(); i++) {
      int connector = Integer.parseInt("" + connectorString.charAt(i));
      outcomes[connector - 1]++;
    }
    return getChi2(connectorString.length(), outcomes, probabilities);
  }

  public static double getChi2Probability(String connectorString) {
    double chi2 = getChi2(connectorString);
    return getChi2Probability(chi2, 2.0);
  }

  public static double getChi2Probability(String[] connectorStrings) {
    String combinedStr = "";
    for (String connectorString : connectorStrings) {
      if (connectorString != null) {
        combinedStr += connectorString;
      }
    }
    return getChi2Probability(combinedStr);
  }

  public static double getChi2Probability(String connectorString, String[] connectorStrings) {
    String combinedStr = "";
    if (connectorStrings != null)
      for (String connectorString1 : connectorStrings) {
        if (connectorString1 != null) {
          combinedStr += connectorString1;
        }
      }
    combinedStr += connectorString;
    return getChi2Probability(combinedStr);
  }

  private static double getMedianProbability(String valueString, double median) {
    int length = valueString.length();
    if (length == 0) return 0.0;

    int[] values = new int[length];
    for (int i = 0; i < length; i++) values[i] = Integer.parseInt("" + valueString.charAt(i));

    int u = 0, n1 = 0, n2 = 0;
    boolean isPrevA = (values[0] > median);
    for (int i = 0; i < length; i++) {
      boolean isCurrA = (values[i] > median);
      if (isCurrA) n1++;
      else n2++;
      if (isPrevA == isCurrA) {
        // current run continues;
      } else {
        // new run
        u++;
      }
      isPrevA = isCurrA;
    }
    // last run
    u++;

    if ((n1 == 0) || (n2 == 0)) return 0.0;

    double n1u = n1;
    double n2u = n2;
    double mu = (2.0 * n1u * n2u) / (n1u + n2u) + 1.0;
    double sigma =
        Math.sqrt(
            (2.0 * n1u * n2u * (2.0 * n1u * n2u - n1u - n2u))
                / ((n1u + n2u) * (n1u + n2u) * (n1u + n2u - 1.0)));

    double z = Math.abs((u - mu) / sigma);

    if (z >= 3.0) return 0.0;

    // go to table of gauss integrals
    double gaussValue = GaussValues.getValue(z);
    return 1.0 - 2.0 * (gaussValue - 0.5);
  }

  public static double getMedianProbability(String valueString) {
    return getMedianProbability(valueString, ConnectorMap.getMedian());
  }

  public static double getMedianProbability(String[] valueStrings) {
    String combinedStr = "";
    for (String valueString : valueStrings) {
      if (valueString != null) {
        combinedStr += valueString;
      }
    }
    return getMedianProbability(combinedStr);
  }

  public static double getMedianProbability(String valueString, String[] valueStrings) {
    String combinedStr = "";
    if (valueStrings != null)
      for (String valueString1 : valueStrings) {
        if (valueString1 != null) {
          combinedStr += valueString1;
        }
      }
    combinedStr += valueString;
    return getMedianProbability(combinedStr);
  }
}
