package ec.planprocons.backend.util;

import ec.planprocons.backend.dto.response.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public class ResponseFactory {

    private ResponseFactory(){
    }

    public static <T>ResponseEntity<ApiResponse<T>> ok(String message, T data){

        ApiResponse<T> response = ApiResponse.<T>builder()
                .success(true)
                .message(message)
                .data(data)
                .build();

        return ResponseEntity.ok(response);

    }

    public static <T> ResponseEntity<ApiResponse<T>> created(String message, T data){

        ApiResponse<T> response = ApiResponse.<T>builder()
                .success(true)
                .message(message)
                .data(data)
                .build();

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(response);
    }
}
