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

public class StateStack {
  public class StackEmpty extends Throwable {
    public StackEmpty() {}
  }

  private class StateStackFrame {
    public int state;

    private StateStackFrame prev;
  }

  private StateStackFrame last;

  private int count;

  public StateStack(int state) {
    StateStackFrame ssf = new StateStackFrame();
    ssf.state = state;
    ssf.prev = null;
    this.last = ssf;
    this.count = 1;
  }

  public int peek() {
    return this.last.state;
  }

  public int pop() throws StackEmpty {
    if (this.count <= 1) throw new StackEmpty();

    int result = this.last.state;
    this.count--;
    if (this.count == 0) {
      this.last = null;
    } else {
      this.last = this.last.prev;
    }
    return result;
  }

  public void push(int state) {
    StateStackFrame ssf = new StateStackFrame();
    ssf.state = state;
    ssf.prev = null;

    if (this.count == 0) {
      this.last = ssf;
      this.count = 1;
    } else {
      ssf.prev = this.last;
      this.last = ssf;
      this.count++;
    }
  }

  public int peekAndReplace(int state) {
    int result = this.last.state;
    this.last.state = state;
    return result;
  }
}
