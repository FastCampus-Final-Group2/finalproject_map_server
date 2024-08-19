package com.team2.finalprojectmapserver.model;

public class MatrixResult {

    public double[][] distanceMatrix;
    public double[][] timeMatrix;

    public MatrixResult(double[][] distanceMatrix, double[][] timeMatrix) {
        this.distanceMatrix = distanceMatrix;
        this.timeMatrix = timeMatrix;
    }

}
