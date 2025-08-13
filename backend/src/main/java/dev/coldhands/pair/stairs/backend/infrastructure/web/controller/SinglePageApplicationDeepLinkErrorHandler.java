package dev.coldhands.pair.stairs.backend.infrastructure.web.controller;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@ControllerAdvice
public class SinglePageApplicationDeepLinkErrorHandler {

    @ExceptionHandler(Exception.class)
    public String handleError(Exception e) throws Exception {
        if (e instanceof NoResourceFoundException) {
            return "forward:/index.html";
        }
        throw e;
    }
}
