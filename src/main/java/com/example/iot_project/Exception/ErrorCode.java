package com.example.iot_project.Exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

@Getter
public enum ErrorCode {
    UNCATEGORIZED_EXCEPTION(9999, "Lỗi không xác định", HttpStatus.INTERNAL_SERVER_ERROR),
    INVALID_KEY(1111, "Lỗi máy chủ", HttpStatus.INTERNAL_SERVER_ERROR),
    ACCESS_DENIED(1111, "Truy cập bị từ chối", HttpStatus.FORBIDDEN),
    USER_NOT_EXISTED(1001, "User không tồn tại", HttpStatus.NOT_FOUND),
    UNAUTHENTICATED_EXCEPTION(1002, "Lỗi xác thực", HttpStatus.UNAUTHORIZED),
    USER_NAME_INVALID(1003,"Username phải từ 6 ký tự trở lên",HttpStatus.BAD_REQUEST),
    PASSWORD_INVALID(1004,"Password phải từ 6 ký tự trở lên",HttpStatus.BAD_REQUEST),
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
