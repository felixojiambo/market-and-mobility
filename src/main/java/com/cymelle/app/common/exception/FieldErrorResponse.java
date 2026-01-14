package com.cymelle.app.common.exception;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FieldErrorResponse {

    private String field;
    private String message;
}
