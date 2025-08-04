package dev.coldhands.pair.stairs.backend.infrastructure.web.controller;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class SinglePageApplicationDeepLinkErrorHandler {

    @ExceptionHandler(Exception.class)
    public String handleError() {
        return "forward:/index.html";
    }
}
