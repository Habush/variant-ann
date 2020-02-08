package org.mozi.varann.web;

import org.mozi.varann.util.AnnotationNotFoundException;
import org.mozi.varann.util.MultipleValuesException;
import org.mozi.varann.web.models.CustomErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.mozi.varann.util.AnnotationException;

import java.time.LocalDateTime;

@ControllerAdvice
public class AnnotationAdvisor {

    @ResponseBody
    @ExceptionHandler(AnnotationException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    String annotationException(AnnotationException ex) {
        return ex.getMessage();
    }

    @ExceptionHandler(AnnotationNotFoundException.class)
    public ResponseEntity<CustomErrorResponse> annotationNotFoundException(AnnotationNotFoundException ex){
        CustomErrorResponse error = new CustomErrorResponse();
        error.setTimestamp(LocalDateTime.now());
        error.setError(ex.getMessage());
        error.setStatus(HttpStatus.NOT_FOUND.value());

        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(MultipleValuesException.class)
    public ResponseEntity<CustomErrorResponse> multiValsException(MultipleValuesException ex) {
        CustomErrorResponse error = new CustomErrorResponse();
        error.setTimestamp(LocalDateTime.now());
        error.setError(ex.getMessage());
        error.setStatus(HttpStatus.BAD_REQUEST.value());

        return new ResponseEntity<>(error, HttpStatus.BAD_GATEWAY);
    }
}
