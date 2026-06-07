package org.hikikomori.community.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.FORBIDDEN)
public class BannedUserException extends RuntimeException {
    public BannedUserException(String message) {
        super(message);
    }
}
