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
package org.pushingpixels.matrixrain;

public class StateStack {
	public class StackEmpty extends Throwable {
		public StackEmpty() {
		}
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
		if (this.count <= 1)
			throw new StackEmpty();

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
