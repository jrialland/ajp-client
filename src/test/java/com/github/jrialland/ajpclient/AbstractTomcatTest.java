/* Copyright (c) 2014-2022 Julien Rialland <julien.rialland@gmail.com>
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
 *
 */
package com.github.jrialland.ajpclient;

import com.github.jrialland.ajpclient.servlet.JavaxServletApiCompat;
import com.github.jrialland.ajpclient.util.ApiCompat;
import jakarta.servlet.Servlet;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.catalina.Context;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.connector.Connector;
import org.apache.catalina.startup.Tomcat;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mock.web.MockServletConfig;
import org.springframework.mock.web.MockServletContext;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.security.MessageDigest;
import java.util.Base64;
import java.util.Map;
import java.util.TreeMap;

/**
 * To be extended by test cases that need to have a real tomcat running
 *
 * @author Julien Rialland <julien.rialland@gmail.com>
 */
public abstract class AbstractTomcatTest {

    private final Logger logger = LoggerFactory.getLogger(getClass().getName());
    private final Map<String, Servlet> servlets = new TreeMap<String, Servlet>();
    private final Protocol protocol;
    private Tomcat tomcat;
    private final Path tempDir;

    /**
     * @param protocol wether to serve http or ajp
     */
    public AbstractTomcatTest(final Protocol protocol) {
        this.protocol = protocol;
        try {
            tempDir = Files.createTempDirectory("tomcat-test-");
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }

    public AbstractTomcatTest(final Protocol protocol, final int maxThreads) {
        this(protocol);
    }

    protected static String computeMd5(final InputStream is) {
        try {
            final MessageDigest md5 = MessageDigest.getInstance("md5");
            final byte[] buf = new byte[1024];
            int c = 0;
            while ((c = is.read(buf)) > -1) {
                md5.update(buf, 0, c);
            }
            return new String(Base64.getEncoder().encode(md5.digest()));
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }

    protected static void deleteDirectory(final Path path) throws IOException {
        Files.walkFileTree(path, new FileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(final Path file, final BasicFileAttributes attrs) throws IOException {
                Files.delete(file);
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult postVisitDirectory(final Path dir, final IOException exc) throws IOException {
                Files.delete(dir);
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult preVisitDirectory(final Path dir, final BasicFileAttributes attrs) throws IOException {
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFileFailed(final Path file, final IOException exc) throws IOException {
                throw exc;
            }
        });
    }

    public Tomcat getTomcat() {
        return tomcat;
    }

    public Path getTempDir() {
        return tempDir;
    }

    @BeforeEach
    public void before() throws LifecycleException, IOException {

        tomcat = new Tomcat();
        tomcat.setBaseDir(tempDir.toString());
        tomcat.getHost().setAppBase(tempDir.toString());
        tomcat.getHost().setAutoDeploy(true);
        tomcat.getHost().setDeployOnStartup(true);

        final Connector connector = new Connector(protocol.getProto());

        connector.setProperty("address", "localhost");
        if (protocol == Protocol.Ajp) {
            connector.setProperty("secretRequired", "false"); //optional shared secret header, as described at https://httpd.apache.org/docs/trunk/mod/mod_proxy_ajp.html
        }
        connector.setPort(0);

        tomcat.setConnector(connector);
        tomcat.init();
        tomcat.start();

        final Path root = Files.createDirectory(tempDir.resolve("ROOT"));
        final Context rootContext = tomcat.addContext("", root.toString());

        servlets.entrySet().forEach(entry -> {
            Servlet servlet = entry.getValue();
            String path = entry.getKey();
            Tomcat.addServlet(rootContext, servlet.toString(), servlet).addMapping(path);
            logger.info(getUri() + entry.getKey() + " => " + entry.getValue());
        });

    }

    /**
     * binds a servlet on the tomcat instance. the 'mapping' has the same format
     * that the 'servlet-mapping' parameter in web.xml. the path of the servlet
     * will be relative to /
     * <p>
     * {@link Servlet#init(jakarta.servlet.ServletConfig)} is called with a mock
     * object as parameter.
     *
     * @param mapping
     * @param servlet
     */
    protected void addServlet(final String mapping, final Servlet servlet) {
        final MockServletContext servletContext = new MockServletContext("/");
        final MockServletConfig config = new MockServletConfig(servletContext, servlet.toString());
        try {
            servlet.init(JavaxServletApiCompat.INSTANCE.makeProxy(config));
        } catch (final Exception e) {
            throw new IllegalStateException(e);
        }
        servlets.put(mapping, servlet);
    }

    @SuppressWarnings("serial")
    protected void addStaticResource(final String mapping, final Path file) {

        if (!Files.isRegularFile(file)) {
            final FileNotFoundException fnf = new FileNotFoundException(file.toString());
            fnf.fillInStackTrace();
            throw new IllegalArgumentException(fnf);
        }

        String md5;
        try {
            final InputStream is = file.toUri().toURL().openStream();
            md5 = computeMd5(is);
            is.close();
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
        final String fMd5 = md5;

        addServlet(mapping, new HttpServlet() {
            @Override
            protected void service(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
                resp.setContentLength((int) Files.size(file));
                resp.setHeader("Content-MD5", fMd5);
                final String mime = Files.probeContentType(file);
                if (mime != null) {
                    resp.setContentType(mime);
                }
                final OutputStream out = resp.getOutputStream();
                Files.copy(file, out);
                out.flush();
            }
        });
    }

    protected int getPort() {
        return tomcat.getConnector().getLocalPort();
    }

    protected String getUri() {
        final InetAddress addr = (InetAddress) tomcat.getConnector().getProperty("address");

        return protocol.getScheme() + addr.getHostAddress() + ":" + getPort();
    }

    @AfterEach
    public void after() throws IOException {
        try {
            tomcat.stop();
            tomcat.destroy();
        } catch (final Exception e) {
            logger.error("could not stop tomcat", e);
        }

        deleteDirectory(tempDir);
    }

    protected enum Protocol {
        Ajp("AJP/1.3"), Http("HTTP/1.1");

        private final String proto;

        Protocol(final String proto) {
            this.proto = proto;
        }

        public String getScheme() {
            return proto.replaceAll("/.+$", "://").toLowerCase();
        }

        public String getProto() {
            return proto;
        }
    }
}
