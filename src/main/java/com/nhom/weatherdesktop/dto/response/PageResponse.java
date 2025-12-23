package com.nhom.weatherdesktop.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record PageResponse<T>(
    @JsonProperty("data") List<T> content,
    @JsonProperty("page") int pageNumber,
    int size,
    long totalElements,
    int totalPages,
    boolean last
) {
}
