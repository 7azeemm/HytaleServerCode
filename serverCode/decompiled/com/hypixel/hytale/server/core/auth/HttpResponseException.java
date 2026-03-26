/*
 * Decompiled with CFR 0.152.
 */
package com.hypixel.hytale.server.core.auth;

import java.io.IOException;

public class HttpResponseException
extends IOException {
    private final int statusCode;
    private final String responseBody;

    public HttpResponseException(int statusCode, String responseBody) {
        super("HTTP " + statusCode + ": " + HttpResponseException.truncateBody(responseBody));
        this.statusCode = statusCode;
        this.responseBody = responseBody;
    }

    public int getStatusCode() {
        return this.statusCode;
    }

    public String getResponseBody() {
        return this.responseBody;
    }

    private static String truncateBody(String body) {
        if (body == null || body.length() <= 200) {
            return body;
        }
        return body.substring(0, 200) + "...";
    }
}

