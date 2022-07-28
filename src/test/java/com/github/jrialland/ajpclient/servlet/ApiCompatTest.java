package com.github.jrialland.ajpclient.servlet;

import com.github.jrialland.ajpclient.util.ApiCompat;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

public class ApiCompatTest {

    @Test
    public void test() {
        javax.servlet.http.HttpServletRequest request = new MockHttpServletRequest();
        jakarta.servlet.http.HttpServletRequest jakartaRequest = JavaxServletApiCompat.INSTANCE.makeProxy(request);
        jakartaRequest.setAttribute("test", true);
    }
}
