package com.cymelle.app.common.exception;

import lombok.*;

import java.time.Instant;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ErrorResponse {

    private Instant timestamp;
    private int status;
    private String error;
    private String message;
    private String path;

    // present only for validation errors
    private List<FieldErrorResponse> fieldErrors;
}
