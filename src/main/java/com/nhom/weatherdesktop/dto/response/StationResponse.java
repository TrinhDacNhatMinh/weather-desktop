package com.nhom.weatherdesktop.dto.response;

public record StationResponse(
        Long id,
        String name,
        String location,
        String status,
        Double latitude,
        Double longitude,
        String apiKey,
        String createdAt,
        Boolean active,
        Boolean isPublic,
        Long ownerId,
        String ownerName
) {
}
