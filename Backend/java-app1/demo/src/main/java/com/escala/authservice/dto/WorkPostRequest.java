package com.escala.authservice.dto;

import java.util.UUID;

public record WorkPostRequest(String name, String description, ProjectRef project) {

    public record ProjectRef(UUID id) {
    }

    public UUID projectId() {
        return project == null ? null : project.id();
    }
}
