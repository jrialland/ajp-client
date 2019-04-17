/* Copyright (c) 2014-2018 Julien Rialland <julien.rialland@gmail.com>
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AttributeFactory {

  public static Attribute createContextAttribute(final String context) {
    return new Attribute(AttributeType.CONTEXT, Collections.singletonList(context));
  }

  public static Attribute createServletPathAttribute(final String servletPath) {
    return new Attribute(AttributeType.SERVLET_PATH, Collections.singletonList(servletPath));
  }

  public static Attribute createRemoteUserAttribute(final String remoteUser) {
    return new Attribute(AttributeType.REMOTE_USER, Collections.singletonList(remoteUser));
  }

  public static Attribute createAuthTypeAttribute(final String authType) {
    return new Attribute(AttributeType.AUTH_TYPE, Collections.singletonList(authType));
  }

  public static Attribute createQueryStringAttribute(final String queryString) {
    return new Attribute(AttributeType.QUERY_STRING, Collections.singletonList(queryString));
  }

  public static Attribute createRouteAttribute(final String route) {
    return new Attribute(AttributeType.ROUTE, Collections.singletonList(route));
  }

  public static Attribute createSslCertificateAttribute(final String sslCertificate) {
    return new Attribute(AttributeType.SSL_CERT, Collections.singletonList(sslCertificate));
  }

  public static Attribute createSslCipherAttribute(final String sslCipher) {
    return new Attribute(AttributeType.SSL_CIPHER, Collections.singletonList(sslCipher));
  }

  public static Attribute createSslSessionAttribute(final String sslSession) {
    return new Attribute(AttributeType.SSL_SESSION, Collections.singletonList(sslSession));
  }

  public static Attribute createReqAttributeAttribute(final String requestAttributeName, final String requestAttributeValue) {
    final List<String> requestAttribute = new ArrayList<>();
    requestAttribute.add(requestAttributeName);
    requestAttribute.add(requestAttributeValue);
    return new Attribute(AttributeType.REQ_ATTRIBUTE, requestAttribute);
  }

  public static Attribute createSslKeySizeAttribute(final String sslKeySize) {
    return new Attribute(AttributeType.SSL_KEY_SIZE, Collections.singletonList(sslKeySize));
  }

  public static Attribute createSecretAttribute(final String secret) {
    return new Attribute(AttributeType.SECRET, Collections.singletonList(secret));
  }

  public static Attribute createStoredMethodAttribute(final String storedMethod) {
    return new Attribute(AttributeType.STORED_METHOD, Collections.singletonList(storedMethod));
  }
}
