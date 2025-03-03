package com.example.iot_project.Exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

@Getter
public enum ErrorCode {
    USER_NOT_EXISTED(1001, "User is not existed", HttpStatus.NOT_FOUND),
    UNAUTHENTICATED_EXCEPTION(1002, "Unauthenticated exception", HttpStatus.UNAUTHORIZED),
    USER_EXISTED(1003,"User đã tồn tại",HttpStatus.BAD_REQUEST),
    ;
    private int code;
    private String message;
    private HttpStatus httpStatusCode;

    ErrorCode(int code, String message, HttpStatusCode httpStatusCode) {
        this.code = code;
        this.message = message;
        this.httpStatusCode = (HttpStatus) httpStatusCode;
    }
}
