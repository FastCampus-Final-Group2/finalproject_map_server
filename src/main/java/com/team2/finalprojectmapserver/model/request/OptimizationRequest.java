package com.team2.finalprojectmapserver.model.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

public record OptimizationRequest (
    @NotNull LocalDateTime startTime,
    Stopover startStopover,
    List<Stopover> stopoverList
){

    public record Stopover(
        @NotBlank String address,
        @NotNull Double lat,
        @NotNull Double lon,
        @NotNull LocalTime delayTime
    ){

    }
}
