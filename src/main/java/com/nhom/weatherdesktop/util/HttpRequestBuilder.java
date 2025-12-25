package com.nhom.weatherdesktop.util;

import com.nhom.weatherdesktop.api.ApiClient;
import com.nhom.weatherdesktop.session.SessionContext;

import java.net.URI;
import java.net.http.HttpRequest;

public class HttpRequestBuilder {
    
    private final HttpRequest.Builder builder;
    
    private HttpRequestBuilder(String url) {
        this.builder = HttpRequest.newBuilder().uri(URI.create(url));
        // Always set Content-Type by default
        this.builder.header("Content-Type", "application/json");
    }
    
    /**
     * Create a new request builder with endpoint path
     * @param endpoint API endpoint path (e.g., "/stations" or "/stations/1")
     */
    public static HttpRequestBuilder create(String endpoint) {
        String url = ApiClient.baseUrl() + endpoint;
        return new HttpRequestBuilder(url);
    }
    
    /**
     * Create a new request builder with full URL
     */
    public static HttpRequestBuilder createWithFullUrl(String fullUrl) {
        return new HttpRequestBuilder(fullUrl);
    }
    
    /**
     * Add Authorization header with current session token
     */
    public HttpRequestBuilder withAuth() {
        String token = SessionContext.accessToken();
        if (token != null) {
            builder.header("Authorization", "Bearer " + token);
        }
        return this;
    }
    
    /**
     * Add custom header
     */
    public HttpRequestBuilder header(String name, String value) {
        builder.header(name, value);
        return this;
    }
    
    /**
     * Set GET method
     */
    public HttpRequestBuilder get() {
        builder.GET();
        return this;
    }
    
    /**
     * Set POST method with JSON body
     */
    public HttpRequestBuilder post(String json) {
        builder.POST(HttpRequest.BodyPublishers.ofString(json));
        return this;
    }
    
    /**
     * Set POST method with no body
     */
    public HttpRequestBuilder post() {
        builder.POST(HttpRequest.BodyPublishers.noBody());
        return this;
    }
    
    /**
     * Set PUT method with JSON body
     */
    public HttpRequestBuilder put(String json) {
        builder.PUT(HttpRequest.BodyPublishers.ofString(json));
        return this;
    }
    
    /**
     * Set PUT method with no body
     */
    public HttpRequestBuilder put() {
        builder.PUT(HttpRequest.BodyPublishers.noBody());
        return this;
    }
    
    /**
     * Set DELETE method
     */
    public HttpRequestBuilder delete() {
        builder.DELETE();
        return this;
    }
    
    /**
     * Set PATCH method with JSON body
     */
    public HttpRequestBuilder patch(String json) {
        builder.method("PATCH", HttpRequest.BodyPublishers.ofString(json));
        return this;
    }
    
    /**
     * Set PATCH method with no body
     */
    public HttpRequestBuilder patch() {
        builder.method("PATCH", HttpRequest.BodyPublishers.noBody());
        return this;
    }
    
    /**
     * Build the final HttpRequest
     */
    public HttpRequest build() {
        return builder.build();
    }
    
    /**
     * Send an HTTP request with automatic token refresh on 401
     * @param request The HTTP request to send
     * @return The HTTP response
     * @throws Exception if request fails
     */
    public static java.net.http.HttpResponse<String> sendWithRefresh(HttpRequest request) throws Exception {
        return TokenRefreshInterceptor.sendWithRefresh(request);
    }
}
