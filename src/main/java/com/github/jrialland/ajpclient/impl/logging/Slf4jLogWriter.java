/* Copyright (c) 2014 Julien Rialland <julien.rialland@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.jrialland.ajpclient.impl.logging;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;

import org.slf4j.Logger;

public class Slf4jLogWriter extends PrintWriter {

	/**
	 * Internal buffer.
	 */
	private final StringBuffer buffer = new StringBuffer();

	/**
	 * Logger for message output.
	 */
	private Logger log;

	/**
	 * Creates a new <code>LogPrintWriter</code> which is based on a
	 * <code>Writer</code>.
	 *
	 * @param out
	 *            the base <code>Writer</code>.
	 */
	public Slf4jLogWriter(final Writer out) {
		super(out);
	}

	/**
	 * Creates a new <code>LogPrintWriter</code> which is based on a
	 * <code>Logger</code>.
	 *
	 * @param log
	 *            the base <code>Logger</code>.
	 */
	public Slf4jLogWriter(final Logger log) {
		super(new NullWriter());
		this.log = log;
	}

	/**
	 * Sets a new output <code>Writer</code>. Calling this method will flush
	 * this <code>LogPrintWriter</code> before the new <code>Writer</code>
	 * <code>out</code> is set.
	 *
	 * @param out
	 *            the <code>Writer</code> to use for output.
	 */
	public void setWriter(final Writer out) {
		flushBuffer();
		this.out = out;
		log = null;
	}

	/**
	 * Sets a new <code>Logger</code>. Calling this method will flush this
	 * <code>LogPrintWriter</code> before the new <code>Logger</code> is set.
	 *
	 * @param log
	 *            the new <code>Logger</code> to use for output.
	 */
	public void setLogger(final Logger log) {
		flushBuffer();
		out = new NullWriter();
		this.log = log;
	}

	@Override
	public void close() {
		flushBuffer();
		super.close();
	}

	@Override
	public void flush() {
		flushBuffer();
		super.flush();
	}

	@Override
	public void write(final int c) {
		buffer.append(c);
	}

	@Override
	public void write(final char cbuf[], final int off, final int len) {
		buffer.append(cbuf, off, len);
	}

	@Override
	public void write(final String str, final int off, final int len) {
		buffer.append(str.substring(off, off + len));
	}

	@Override
	public void println() {
		if (log == null) {
			// only add newline when operating on a writer
			buffer.append('\n');
		}
		flushBuffer();
	}

	private void flushBuffer() {
		if (buffer.length() == 0) {
			return;
		}
		if (log != null) {
			log.debug(buffer.toString());
		} else {
			try {
				out.write(buffer.toString());
			} catch (final IOException e) {
				setError();
			}
		}
		// reset buffer
		buffer.setLength(0);
	}

	/**
	 * Implements a Writer that simply ignores all calls.
	 */
	private static class NullWriter extends Writer {

		@Override
		public void close() throws IOException {
			// ignore
		}

		@Override
		public void flush() throws IOException {
			// ignore
		}

		@Override
		public void write(final char cbuf[], final int off, final int len) throws IOException {
			// ignore
		}
	}
}
