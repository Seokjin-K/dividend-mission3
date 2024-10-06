package com.dividend.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@Slf4j
@ControllerAdvice
public class CustomExceptionHandler {
    // ControllerAdvice 는 필터와 비슷하게 컨트롤러 코드보다 더 앞쪽에서 동작하는 레이어
    // 필터는 더 앞쪽이라면, ControllerAdvice 는 컨트롤러와 더 가깝다.
    // 서비스에서 지정된 에러가 발생하면 해당 에러를 잡아서 Response 로 던질 수 있다.
    // 해당 메서드에서 에러가 발생했을 때 이 에러를 잡아서 어떻게 던질지 정해준다.
    @ExceptionHandler(AbstractException.class)
    protected ResponseEntity<ErrorResponse> handleCustomException(AbstractException e) {

        ErrorResponse errorResponse = ErrorResponse.builder()
                .code(e.getStatusCode())
                .message(e.getMessage())
                .build();
        return new ResponseEntity<>(errorResponse, HttpStatus.resolve(e.getStatusCode()));
    }
}
