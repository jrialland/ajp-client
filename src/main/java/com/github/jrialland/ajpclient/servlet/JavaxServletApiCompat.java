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
