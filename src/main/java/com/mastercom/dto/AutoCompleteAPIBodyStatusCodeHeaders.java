package com.mastercom.dto;

import org.springframework.http.HttpHeaders;

public class AutoCompleteAPIBodyStatusCodeHeaders {
    public Object apiBody;
    public int httpStatusCode;
    public HttpHeaders httpHeaders;

    public Object getApiBody() {
        return apiBody;
    }

    public void setApiBody(Object apiBody) {
        this.apiBody = apiBody;
    }

    public int getHttpStatusCode() {
        return httpStatusCode;
    }

    public void setHttpStatusCode(int httpStatusCode) {
        this.httpStatusCode = httpStatusCode;
    }

    public HttpHeaders getHttpHeaders() {
        return httpHeaders;
    }

    public void setHttpHeaders(HttpHeaders httpHeaders) {
        this.httpHeaders = httpHeaders;
    }
}
