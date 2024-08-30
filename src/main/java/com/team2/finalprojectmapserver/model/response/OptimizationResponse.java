package com.team2.finalprojectmapserver.model.response;

import com.team2.finalprojectmapserver.model.request.OptimizationRequest.Stopover;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Getter
@NoArgsConstructor
public class OptimizationResponse {

    private double totalDistance;
    private long totalTime;
    private LocalDateTime startTime; //상차 시작 시간
    private Stopover startStopover;
    private LocalTime breakStartTime;
    private LocalTime breakEndTime;
    private int restingPosition; // 휴식위치 해당 경유지 도착전
    private List<ResultStopover> resultStopoverList;
    private List<Map<String, Double>> coordinates;

    // 생성자 추가
    private OptimizationResponse(double totalDistance, long totalTime , LocalDateTime startTime,Stopover startStopover, List<ResultStopover> resultStopoverList, List<Map<String, Double>> coordinates, LocalTime breakStartTime, LocalTime breakEndTime, int restingPosition) {
        this.totalDistance = totalDistance;
        this.totalTime = totalTime;
        this.startTime = startTime;
        this.startStopover = startStopover;
        this.resultStopoverList = resultStopoverList;
        this.coordinates = coordinates;
        this.breakStartTime = breakStartTime;
        this.breakEndTime = breakEndTime;
        this.restingPosition = restingPosition;
    }

    public static OptimizationResponse of(double totalDistance, long totalTime, LocalDateTime startTime,Stopover startStopover, List<ResultStopover> resultStopoverList, List<Map<String, Double>> coordinates, LocalTime breakStartTime, LocalTime breakEndTime, int restingPosition) {
        return new OptimizationResponse(totalDistance,totalTime, startTime,startStopover,resultStopoverList,coordinates,breakStartTime,breakEndTime,restingPosition);
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
        @Setter
        private LocalDateTime startTime; // 출발시간 //상차완료  //경유지1시작
        @Setter
        private LocalDateTime endTime;  // 도착 시간 //경유지1도착

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
