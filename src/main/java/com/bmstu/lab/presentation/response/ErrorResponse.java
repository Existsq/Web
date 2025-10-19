package com.bmstu.lab.presentation.response;

import java.sql.Timestamp;
import lombok.Builder;

@Builder
public record ErrorResponse(
    Timestamp timestamp, String status, String error, String message, String path) {}
