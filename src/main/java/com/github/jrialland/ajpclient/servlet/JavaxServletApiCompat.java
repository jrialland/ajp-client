package com.github.jrialland.ajpclient.servlet;

import com.github.jrialland.ajpclient.util.ApiCompat;

import java.io.IOException;

/**
 * Implements Api compatibility from javax.servlet to jakarta.servlet.
 * The {@link ApiCompat#makeProxy} method allows to create proxies of objects from javax.servlet to jakarta.servlet.
 *
 * <code>
 * javax.servlet.http.HttpServletRequest request = new MockHttpServletRequest();
 * jakarta.servlet.http.HttpServletRequest jakartaRequest = JavaxServletApiCompat.INSTANCE.makeProxy(request);
 * </code>
 */
public class JavaxServletApiCompat extends ApiCompat {

    public static final JavaxServletApiCompat INSTANCE = new JavaxServletApiCompat();

    private JavaxServletApiCompat() {
        super("javax", "jakarta");

        addConverter(javax.servlet.ServletInputStream.class, (in) -> new jakarta.servlet.ServletInputStream() {
            @Override
            public int read() throws IOException {
                return in.read();
            }

            @Override
            public boolean isReady() {
                return in.isReady();
            }

            @Override
            public void setReadListener(jakarta.servlet.ReadListener readListener) {
                in.setReadListener(new javax.servlet.ReadListener() {
                    @Override
                    public void onDataAvailable() throws IOException {
                        readListener.onDataAvailable();
                    }

                    @Override
                    public void onAllDataRead() throws IOException {
                        readListener.onAllDataRead();
                    }

                    @Override
                    public void onError(Throwable t) {
                        readListener.onError(t);
                    }
                });
            }

            @Override
            public boolean isFinished() {
                return in.isFinished();
            }
        });

        addConverter(javax.servlet.ServletOutputStream.class, (out) -> new jakarta.servlet.ServletOutputStream() {
            @Override
            public void write(int b) throws IOException {
                out.write(b);
            }

            @Override
            public boolean isReady() {
                return out.isReady();
            }

            @Override
            public void setWriteListener(jakarta.servlet.WriteListener writeListener) {
                out.setWriteListener(new javax.servlet.WriteListener() {
                    @Override
                    public void onWritePossible() throws IOException {
                        writeListener.onWritePossible();
                    }

                    @Override
                    public void onError(Throwable t) {
                        writeListener.onError(t);
                    }
                });
            }
        });
    }
}
