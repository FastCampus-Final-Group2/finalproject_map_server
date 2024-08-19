package com.team2.finalprojectmapserver.model.response;

import com.team2.finalprojectmapserver.model.request.OptimizationRequest.Stopover;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.NoArgsConstructor;


@Getter
@NoArgsConstructor
public class OptimizationResponse {

    private double totalDistance;
    private long totalTime;
    private LocalDateTime startTime;
    private Stopover startStopover;
    private List<ResultStopover> resultStopoverList;
    private List<Map<String, Double>> coordinates;

    // 생성자 추가
    private OptimizationResponse(double totalDistance, long totalTime , LocalDateTime startTime,Stopover startStopover, List<ResultStopover> resultStopoverList, List<Map<String, Double>> coordinates) {
        this.totalDistance = totalDistance;
        this.totalTime = totalTime;
        this.startTime = startTime;
        this.startStopover = startStopover;
        this.resultStopoverList = resultStopoverList;
        this.coordinates = coordinates;
    }

    public static OptimizationResponse of(double totalDistance, long totalTime, LocalDateTime startTime,Stopover startStopover, List<ResultStopover> resultStopoverList, List<Map<String, Double>> coordinates) {
        return new OptimizationResponse(totalDistance,totalTime, startTime,startStopover,resultStopoverList,coordinates);
    }

    @Getter
    @NoArgsConstructor
    public static class ResultStopover{
        private String address;
        private Double lat;
        private Double lon;
        private LocalTime delayTime;
        private Double distance;  // 이동거리
        private Long timeFromPrevious;  // 소요시간
        private LocalDateTime startTime; // 출발시간
        private LocalDateTime endTime;  // 도착 시간

        private ResultStopover(String address, Double lat, Double lon, LocalTime delayTime, Double distance,Long timeFromPrevious,LocalDateTime startTime, LocalDateTime endTime) {
            this.address = address;
            this.lat = lat;
            this.lon = lon;
            this.delayTime = delayTime;
            this.distance = distance;
            this.timeFromPrevious = timeFromPrevious;
            this.startTime = startTime;
            this.endTime = endTime;
        }

        public static ResultStopover of(String address, Double lat, Double lon,LocalTime delayTime, Double distance, Long timeFromPrevious,LocalDateTime startTime, LocalDateTime endTime) {
            return new ResultStopover(address,lat,lon,delayTime,distance,timeFromPrevious,startTime,endTime);
        }

    }

}
