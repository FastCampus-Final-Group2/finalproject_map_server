package com.team2.finalprojectmapserver.controller;

import com.team2.finalprojectmapserver.model.request.OptimizationRequest;
import com.team2.finalprojectmapserver.model.response.OptimizationResponse;
import com.team2.finalprojectmapserver.service.OptimizationService;
import com.team2.finalprojectmapserver.util.response.ApiResponse;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class OptimizationController {

    private final OptimizationService optimizationService;

    @PostMapping("/Optimization")
    public ResponseEntity<List<OptimizationResponse>> optimize(@RequestBody @Valid List<OptimizationRequest> requestList) {
        List<OptimizationResponse> optimizationResponseList = optimizationService.optimization(requestList);

        return ApiResponse.OK(optimizationResponseList);
    }

    @PostMapping("/OptimumPath")
    public ResponseEntity<OptimizationResponse> getOptimumPath(@RequestBody @Valid OptimizationRequest request) {
        OptimizationResponse optimizationResponse = optimizationService.getOptimumPath(request);

        return  ApiResponse.OK(optimizationResponse);
    }

}
