package org.mozi.varann.web;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import util.AnnotationException;

@ControllerAdvice
public class AnnotationAdvisor {

    @ResponseBody
    @ExceptionHandler(AnnotationException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    String annotationException(AnnotationException ex) {
        return ex.getMessage();
    }
}
