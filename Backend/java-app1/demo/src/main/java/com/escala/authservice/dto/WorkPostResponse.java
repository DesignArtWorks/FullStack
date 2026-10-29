package com.escala.authservice.dto;

import com.escala.authservice.entity.WorkPost;

import java.util.UUID;

public record WorkPostResponse(UUID id, String name, String description, ProjectRef project) {

    public record ProjectRef(UUID id, String name) {
    }

    public static WorkPostResponse from(WorkPost workPost) {
        ProjectRef project = workPost.getProject() == null
                ? null
                : new ProjectRef(workPost.getProject().getId(), workPost.getProject().getName());
        return new WorkPostResponse(workPost.getId(), workPost.getName(), workPost.getDescription(), project);
    }
}
