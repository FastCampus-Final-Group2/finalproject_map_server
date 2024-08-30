package com.team2.finalprojectmapserver.model.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

public record OptimizationRequest (
    @NotNull LocalDateTime startTime,
    @Valid Stopover startStopover,
    @Valid List<Stopover> stopoverList,
    LocalTime restStartTime,
    LocalTime restDuration
){

    public record Stopover(
        @NotBlank String address,
        @NotNull Double lat,
        @NotNull Double lon,
        @NotNull LocalTime delayTime
    ){

    }
}
