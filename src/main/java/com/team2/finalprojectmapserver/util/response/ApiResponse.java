package com.team2.finalprojectmapserver.util.response;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@Slf4j
public class ApiResponse {

    public static <T> ResponseEntity<T> OK(T data) {
        return ResponseEntity.ok(data);
    }

    public static ResponseEntity<Void> OK() {
        return ResponseEntity.status(HttpStatus.OK).build();
    }

}