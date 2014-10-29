package com.github.jrialland.ajpclient.impl.logging;

import org.junit.Test;
import org.slf4j.LoggerFactory;

import com.github.jrialland.ajpclient.impl.logging.Slf4jLogWriter.Level;

public class Slf4jLogWriterTest {

	@Test
	public void doTest() {

		final Slf4jLogWriter slf4jLogWriter = new Slf4jLogWriter(Level.WARN, LoggerFactory.getLogger("samplelog"));
		for (final Level level : Level.values()) {
			slf4jLogWriter.setLevel(level);
			slf4jLogWriter.write("this is a sample " + level + " log message");
			slf4jLogWriter.println();
		}
		slf4jLogWriter.flush();
		slf4jLogWriter.close();
	}
}
