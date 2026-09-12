package com.escala.authservice.controller;

public record ApiError(String code, String message, int status, String errorId, String correlationId) {
}
