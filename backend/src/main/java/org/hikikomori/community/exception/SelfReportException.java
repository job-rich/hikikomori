package org.hikikomori.community.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class SelfReportException extends RuntimeException {
    public SelfReportException(String message) {
        super(message);
    }
}
