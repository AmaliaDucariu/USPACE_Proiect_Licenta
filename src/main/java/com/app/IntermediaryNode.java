package com.app;

public class IntermediaryNode {
    private double multiplicationFactor = 1;
    private final Integer nodeID;



    public IntermediaryNode(final Integer nodeID){
        this.nodeID = nodeID;
    }

    public double getAndIncrementMultiplicationFactor() {
        final double tempMultiplicationFactor = this.multiplicationFactor;
        this.multiplicationFactor++;
        return tempMultiplicationFactor;
    }

    public double incrementAndGetMultiplicationFactor(){
        this.multiplicationFactor++;
        return multiplicationFactor;
    }

    public double getMultipliedAddedDistance() {
        double addedDistance = 40.0 / 3600 / 3;
        return getAndIncrementMultiplicationFactor() * addedDistance;
    }

    public Integer getNodeID() {
        return nodeID;
    }
}
