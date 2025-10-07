package com.bmstu.lab.exception;

import java.sql.Timestamp;
import lombok.Builder;

@Builder
public record ErrorResponse(
    Timestamp timestamp, String status, String error, String message, String path) {}
