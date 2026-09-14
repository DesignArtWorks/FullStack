package com.escala.authservice.dto;

import java.util.UUID;

/** Minimal ownership descriptor; contains no private profile fields. */
public record AvatarAccessResponse(UUID userId, UUID companyId) {}
