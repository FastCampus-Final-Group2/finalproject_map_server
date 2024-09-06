package com.team2.finalprojectmapserver.service;

import com.graphhopper.GHRequest;
import com.graphhopper.GHResponse;
import com.graphhopper.GraphHopper;
import com.graphhopper.ResponsePath;
import com.graphhopper.jsprit.core.algorithm.VehicleRoutingAlgorithm;
import com.graphhopper.jsprit.core.algorithm.box.Jsprit;
import com.graphhopper.jsprit.core.problem.Location;
import com.graphhopper.jsprit.core.problem.VehicleRoutingProblem;
import com.graphhopper.jsprit.core.problem.job.Service;
import com.graphhopper.jsprit.core.problem.solution.VehicleRoutingProblemSolution;
import com.graphhopper.jsprit.core.problem.solution.route.VehicleRoute;
import com.graphhopper.jsprit.core.problem.solution.route.activity.TourActivity;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleImpl;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleTypeImpl;
import com.graphhopper.jsprit.core.util.Coordinate;
import com.graphhopper.jsprit.core.util.Solutions;
import com.graphhopper.jsprit.core.util.VehicleRoutingTransportCostsMatrix;
import com.graphhopper.util.PointList;
import com.graphhopper.util.shapes.GHPoint;
import com.team2.finalprojectmapserver.model.MatrixResult;
import com.team2.finalprojectmapserver.model.request.OptimizationRequest;
import com.team2.finalprojectmapserver.model.request.OptimizationRequest.Stopover;
import com.team2.finalprojectmapserver.model.response.OptimizationResponse;
import com.team2.finalprojectmapserver.model.response.OptimizationResponse.ResultStopover;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OptimizationService {

    private final GraphHopper graphHopper;

    public List<OptimizationResponse> optimization(List<OptimizationRequest> optimizationListRequest){
        List<OptimizationResponse> optimizationResponseList = new ArrayList<>();
        for (OptimizationRequest request : optimizationListRequest){

            Stopover startStopover = request.startStopover();
            GHPoint startPoint = new GHPoint(startStopover.lat(), startStopover.lon());
            List<GHPoint> viaPoints = new ArrayList<>();
            viaPoints.add(startPoint);

            Map<GHPoint, LinkedList<Stopover>> addressListMap = new HashMap<>();
            addressListMap.put(startPoint, new LinkedList<>(List.of(startStopover)));

            for (Stopover stopover : request.stopoverList()) {
                GHPoint point = new GHPoint(stopover.lat(), stopover.lon());
                viaPoints.add(point);

                // 동일한 키가 없다면 map에 추가  동일한 키가 있다면 해당 리스트에 추가후 map에 저장
                addressListMap.computeIfAbsent(point, k -> new LinkedList<>()).add(stopover);
            }

            VehicleRoutingProblemSolution bestSolution = vrp(viaPoints);

            List<GHPoint> resultPoints = new ArrayList<>();
            resultPoints.add(viaPoints.get(0));
            for (VehicleRoute route : bestSolution.getRoutes()) {
                for (TourActivity activity : route.getActivities()) {
                    resultPoints.add(new GHPoint(activity.getLocation().getCoordinate().getY(), activity.getLocation().getCoordinate().getX()));
                }
            }

            OptimizationResponse optimumPath = operationOptimalPath(addressListMap, resultPoints, request.startTime(),request.startStopover().delayTime(),request.restStartTime(),request.restDuration());

            optimizationResponseList.add(optimumPath);
        }
        return optimizationResponseList;
    }

    private VehicleRoutingProblemSolution vrp(List<GHPoint> viaPoints){
        MatrixResult matrixResult = calculateMatrices(viaPoints);

        VehicleTypeImpl vehicleType = VehicleTypeImpl.Builder.newInstance("vehicleType")
            .addCapacityDimension(0, 1).setCostPerDistance(1).build();


        VehicleImpl vehicle = VehicleImpl.Builder.newInstance("vehicle")
            .setStartLocation(Location.newInstance(viaPoints.get(0).lon, viaPoints.get(0).lat))
            .setType(vehicleType).build();

        VehicleRoutingProblem.Builder vrpBuilder = VehicleRoutingProblem.Builder.newInstance()
            .addVehicle(vehicle);

        for (int i = 1; i < viaPoints.size(); i++) {
            Service service = Service.Builder.newInstance(String.valueOf(i))
                .setLocation(Location.newInstance(viaPoints.get(i).lon,viaPoints.get(i).lat))
                .build();
            vrpBuilder.addJob(service);
        }

        VehicleRoutingTransportCostsMatrix.Builder costMatrixBuilder = VehicleRoutingTransportCostsMatrix.Builder.newInstance(true);

        for (int i = 0; i < viaPoints.size(); i++) {
            for (int j = i; j < viaPoints.size(); j++) {
                GHPoint from = viaPoints.get(i);
                GHPoint to = viaPoints.get(j);
                String fromKey = Coordinate.newInstance(from.getLon(),from.getLat()).toString();
                String toKey = Coordinate.newInstance(to.getLon(),to.getLat()).toString();
                costMatrixBuilder.addTransportDistance(fromKey, toKey, matrixResult.distanceMatrix[i][j]);
                costMatrixBuilder.addTransportTime(fromKey, toKey, matrixResult.timeMatrix[i][j]);
            }
        }

        VehicleRoutingTransportCostsMatrix costMatrix = costMatrixBuilder.build();
        vrpBuilder.setRoutingCost(costMatrix);
        VehicleRoutingProblem vrp = vrpBuilder.build();
        VehicleRoutingAlgorithm algorithm = Jsprit.createAlgorithm(vrp);
        Collection<VehicleRoutingProblemSolution> solutions = algorithm.searchSolutions();
        VehicleRoutingProblemSolution bestSolution = Solutions.bestOf(solutions);
        return bestSolution;
    }

    private MatrixResult calculateMatrices(List<GHPoint> points) {
        int size = points.size();
        double[][] distanceMatrix = new double[size][size];
        double[][] timeMatrix = new double[size][size];
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                if (i == j) {
                    distanceMatrix[i][j] = 0;
                    timeMatrix[i][j] = 0;
                } else {
                    GHRequest request = new GHRequest(points.get(i), points.get(j)).setProfile("truck").setSnapPreventions(List.of("motorway", "ferry", "tunnel"));
                    GHResponse response = graphHopper.route(request);
                    if (response.hasErrors()) {
                        throw new RuntimeException("Error calculating route from " + points.get(i) + " to " + points.get(j) + ": " + response.getErrors());
                    }
                    distanceMatrix[i][j] = response.getBest().getDistance();
                    timeMatrix[i][j] = response.getBest().getTime() / 1000.0; // convert to seconds
                }
            }
        }
        return new MatrixResult(distanceMatrix, timeMatrix);
    }

    public OptimizationResponse getOptimumPath(OptimizationRequest request) {
        Stopover startStopover = request.startStopover();
        GHPoint startPoint = new GHPoint(startStopover.lat(), startStopover.lon());
        List<GHPoint> viaPoints = new ArrayList<>();
        viaPoints.add(startPoint);

        Map<GHPoint, LinkedList<Stopover>> addressListMap = new HashMap<>();
        addressListMap.put(startPoint, new LinkedList<>(List.of(startStopover)));

        for (Stopover stopover : request.stopoverList()) {
            GHPoint point = new GHPoint(stopover.lat(), stopover.lon());
            viaPoints.add(point);

            // 동일한 키가 없다면 map에 추가  동일한 키가 있다면 해당 리스트에 추가후 map에 저장
            addressListMap.computeIfAbsent(point, k -> new LinkedList<>()).add(stopover);
        }

        OptimizationResponse optimumPath = operationOptimalPath(addressListMap, viaPoints, request.startTime(),request.startStopover().delayTime(),request.restStartTime(),request.restDuration());
        return optimumPath;
    }

    private OptimizationResponse operationOptimalPath(Map<GHPoint, LinkedList<Stopover>> addressListMap,List<GHPoint> viaPoints ,LocalDateTime startTime, LocalTime startStopoverDelayTime,LocalTime breakStartTime, // 추가된 매개변수: 휴식 시작 시간
        LocalTime breakDuration){
        List<ResultStopover> resultStopoverList = new ArrayList<>();
        Stopover startStopover = addressListMap.get(viaPoints.get(0)).getFirst();

        if (breakStartTime == null){
            breakStartTime = LocalTime.of(0, 0, 0);
        }
        if (breakDuration == null){
            breakDuration = LocalTime.of(0, 0, 0);
        }


        LocalDateTime currentTime = startTime.plusHours(startStopoverDelayTime.getHour())
            .plusMinutes(startStopoverDelayTime.getMinute())
            .plusSeconds(startStopoverDelayTime.getSecond());  // 경유지마다의 도착 시간을 추적하는 변수

        for (int i = 0; i < viaPoints.size() - 1; i++) {
            GHRequest request = new GHRequest()
                .setProfile("truck") // 트럭 프로필 사용
                .setPoints(List.of(viaPoints.get(i), viaPoints.get(i + 1)))
                .setSnapPreventions(List.of("motorway", "ferry", "tunnel"))
                .setLocale(Locale.KOREA);

            GHResponse response = graphHopper.route(request);
            ResponsePath path = response.getBest();

            if (response.hasErrors()) {
                throw new RuntimeException("GraphHopper route request failed: " + response.getErrors());
            }


            double segmentDistance = path.getDistance();
            long segmentTime = path.getTime(); // 각 구간의 시간 (밀리초)

            LocalDateTime StopoverStartTime = currentTime;  // 출발시간
            currentTime = currentTime.plusSeconds(segmentTime / 1000); // 도착시간

            LinkedList<Stopover> StopoverList = addressListMap.get(viaPoints.get(i + 1));

            // 같은 좌표의 경유지중 맨처음 데이터를 가져오고 삭제
            Stopover toStopover = StopoverList.peekFirst();
            StopoverList.pollFirst();

            ResultStopover updatedToStopovers = ResultStopover.of(
                toStopover.address(),
                toStopover.lat(),
                toStopover.lon(),
                toStopover.delayTime(),
                segmentDistance,
                segmentTime,
                StopoverStartTime,
                currentTime
            );

            resultStopoverList.add(updatedToStopovers);


            // 이후 딜레이 추가
            // 딜레이 타임이 있는 경우 추가
            if (toStopover.delayTime() != null) {
                currentTime = currentTime.plusSeconds(toStopover.delayTime().toSecondOfDay());
            }
        }

        boolean plusTrue = false;
        int restingPosition = 0;
        LocalTime resultBreakStartTime = breakStartTime;
        for (int i = 0; i < resultStopoverList.size()-1; i++) {
            LocalDateTime stopoverDepartureTime = resultStopoverList.get(i).getStartTime();
            LocalDateTime stopoverEndTime = resultStopoverList.get(i).getEndTime();
            LocalDateTime nextStopoverDepartureTime = resultStopoverList.get(i+1).getStartTime();
            LocalDateTime nextStopoverEndTime = resultStopoverList.get(i+1).getEndTime();

            if (plusTrue){
                resultStopoverList.get(i+1).setStartTime(nextStopoverDepartureTime.plusSeconds(breakDuration.toSecondOfDay()));
                resultStopoverList.get(i+1).setEndTime(nextStopoverEndTime.plusSeconds(breakDuration.toSecondOfDay()));
            }
            // 출발시간과 도착시간사이
            if (stopoverDepartureTime.toLocalTime().isBefore(breakStartTime) && stopoverEndTime.toLocalTime().isAfter(breakStartTime)){
                plusTrue = true;
                restingPosition =  i+1;
                resultStopoverList.get(i).setEndTime(stopoverEndTime.plusSeconds(breakDuration.toSecondOfDay()));
                resultStopoverList.get(i+1).setStartTime(nextStopoverDepartureTime.plusSeconds(breakDuration.toSecondOfDay()));
                resultStopoverList.get(i+1).setEndTime(nextStopoverEndTime.plusSeconds(breakDuration.toSecondOfDay()));
            }

                // 작업 도중인경우
            if (stopoverEndTime.toLocalTime().isBefore(breakStartTime) && resultStopoverList.get(i+1).getStartTime().toLocalTime().isAfter(breakStartTime)){
                plusTrue = true;
                restingPosition =  i+2;
                resultStopoverList.get(i+1).setEndTime(nextStopoverEndTime.plusSeconds(breakDuration.toSecondOfDay()));
                resultBreakStartTime = resultStopoverList.get(i+1).getStartTime().toLocalTime();
            }
        }

        // 마지막경유지 시간 추가
        LocalDateTime beforeStopoverDepartureTime = resultStopoverList.get(resultStopoverList.size()-2).getStartTime();
        LocalDateTime beforeStopoverEndTime = resultStopoverList.get(resultStopoverList.size()-2).getEndTime();
        resultStopoverList.get(resultStopoverList.size()-1).setStartTime(beforeStopoverDepartureTime.plusSeconds(breakDuration.toSecondOfDay()));
        resultStopoverList.get(resultStopoverList.size()-1).setEndTime(beforeStopoverEndTime.plusSeconds(breakDuration.toSecondOfDay()));




        // 전체 경로 요청
        GHRequest finalRequest = new GHRequest()
            .setProfile("truck") // 트럭 프로필 사용
            .setPoints(viaPoints)
            .setSnapPreventions(List.of("motorway", "ferry", "tunnel"))
            .setLocale(Locale.KOREA);

        GHResponse finalResponse = graphHopper.route(finalRequest);
        ResponsePath path = finalResponse.getBest();

        if (finalResponse.hasErrors()) {
            throw new RuntimeException("GraphHopper route request failed: " + finalResponse.getErrors());
        }

        double totalDistance = path.getDistance(); // 총 거리 (미터)
        long totalTime = path.getTime(); // 총 시간 (밀리초)
        PointList pointList = path.getPoints();

        List<Map<String, Double>> coordinates = new ArrayList<>();
        for (int i = 0; i < pointList.size(); i++) {
            Map<String, Double> coordinate = new HashMap<>();
            coordinate.put("lat", pointList.getLat(i));
            coordinate.put("lon", pointList.getLon(i));
            coordinates.add(coordinate);
        }

        return OptimizationResponse.of(totalDistance, totalTime,startTime,startStopover, resultStopoverList, coordinates,resultBreakStartTime,resultBreakStartTime.plusSeconds(breakDuration.toSecondOfDay()),restingPosition);
    }

}
