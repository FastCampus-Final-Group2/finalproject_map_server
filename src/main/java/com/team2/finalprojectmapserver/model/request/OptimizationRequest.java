package com.team2.finalprojectmapserver.model.request;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

public record OptimizationRequest (
    LocalDateTime startTime,
    Stopover startStopover,
    List<Stopover> stopoverList
){

    public record Stopover(
        String address,
        Double lat,
        Double lon,
        LocalTime delayTime
    ){

    }
}
