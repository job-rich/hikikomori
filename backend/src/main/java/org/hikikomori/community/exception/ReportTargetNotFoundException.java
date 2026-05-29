package org.hikikomori.community.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class ReportTargetNotFoundException extends RuntimeException {
    public ReportTargetNotFoundException(String message) {
        super(message);
    }
}
