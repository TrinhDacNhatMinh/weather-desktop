package com.nhom.weatherdesktop.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Wrapper for API responses that contain a "data" field
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record ApiResponse<T>(
        T data
) {
}
