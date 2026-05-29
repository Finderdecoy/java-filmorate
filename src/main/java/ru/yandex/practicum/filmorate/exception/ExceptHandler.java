package ru.yandex.practicum.filmorate.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ResponseStatus;


public class ExceptionHandler {
    @org.springframework.web.bind.annotation.ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String validExeception(MethodArgumentNotValidException e) {
         throw new ValidationException("Поле: " + e.getFieldError().getField() + " " + e.getFieldError().getDefaultMessage());
    }

    @ExceptionHandler
    public void notFoundException(NotFoundException e){

    }
}

