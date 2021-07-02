package com.data.Drones;

import com.app.Node;
import com.app.NodePath;
import com.displayUtils.Utils;
import com.esri.arcgisruntime.geometry.Point;
import com.network.RestrictedPoints;
import com.sun.scenario.effect.impl.sw.java.JSWBlend_SRC_OUTPeer;

import java.awt.geom.Line2D;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Drone {
    private final double speed; // m/s
    private final Integer startNode;
    private final Integer endNode;
    private final NodePath shortestPath;
    private double safetyRadius = 5; //m
    private double safetyNodeOccupancyTime;
    private final double timeStart;
    private Map<Integer, Double> nodeToEstimatedTime;
    private List<Integer> orderOfNodes;
    private int color = 0xFF9E3D64;
    private int uid;
    private Set<Integer> nodesToBeIgnored;

    public Drone(int uid, double speed, Integer startNode, Integer endNode, double timeStart, NodePath shortestPath, Set<Integer> nodesToBeIgnored, int color) {
        this(uid, speed, startNode, endNode, timeStart, shortestPath, nodesToBeIgnored);
        this.color = color;
    }

    public Drone(int uid, double speed, Integer startNode, Integer endNode, double timeStart, NodePath shortestPath, Set<Integer> nodesToBeIgnored) {
        this.uid = uid;
        this.speed = speed;
        this.startNode = startNode;
        this.endNode = endNode;
        this.timeStart = timeStart;
        this.shortestPath = shortestPath;
        this.safetyNodeOccupancyTime = this.safetyRadius / this.speed;
        this.nodesToBeIgnored = nodesToBeIgnored;
        this.orderOfNodes = shortestPath.findPath(startNode, endNode, 0, null, this.nodesToBeIgnored);
    }

    public Drone(int uid, double speed, Integer startNode, Integer endNode, double timeStart, NodePath shortestPath, Set<Integer> nodesToBeIgnored, double safetyRadius) {
        this(uid, speed, startNode, endNode, timeStart, shortestPath, nodesToBeIgnored);
        this.safetyRadius = safetyRadius;
        this.safetyNodeOccupancyTime = this.speed / this.safetyRadius;
    }


    public Map<Integer, Double> getTimeStampForEachNodeInPath() {
        final Map<Integer, Double> idToVisitedTime = new HashMap<>();
        idToVisitedTime.put(orderOfNodes.get(0), timeStart);
        for (int i = 1; i < orderOfNodes.size(); i++) {
            idToVisitedTime.put(orderOfNodes.get(i), idToVisitedTime.get(orderOfNodes.get(i - 1)) + shortestPath.getIdToNeighbourDistance().get(orderOfNodes.get(i)).get(orderOfNodes.get(i - 1)) / speed);
        }
        return idToVisitedTime;
    }


    public List<Integer> getNewPath(Integer lastValidNode) {
        final Integer indexOfLastValidNode = orderOfNodes.indexOf(lastValidNode);

        final Integer conflictNode = orderOfNodes.get(indexOfLastValidNode + 1);
        if (shortestPath.getNodeIdToMultiplicationFactor().containsKey(conflictNode)) {
            shortestPath.getIdToNeighbourDistance().get(lastValidNode).put(conflictNode, shortestPath.getIdToNeighbourDistance().get(lastValidNode).get(conflictNode) * shortestPath.getNodeIdToMultiplicationFactor().get(conflictNode));
        }

        return shortestPath.findPath(startNode, endNode, 0, conflictNode, this.nodesToBeIgnored);
    }

    public void setNewPath(Integer lastValidNode) {
        this.orderOfNodes = getNewPath(lastValidNode);
    }

    public double getSafetyRadius() {
        return safetyRadius;
    }

    public double getTimeStart() {
        return timeStart;
    }

    public double getSafetyNodeOccupancyTime() {
        return safetyNodeOccupancyTime;
    }

    public void setOrderOfNodes(final List<Integer> orderOfNodes) {
        this.orderOfNodes = orderOfNodes;
    }

    public Map<Integer, Double> getNodeToEstimatedTime() {
        return getTimeStampForEachNodeInPath();
    }

    public List<Integer> getOrderOfNodes() {
        return orderOfNodes;
    }

    public int getColor() {
        return color;
    }

    public double getSpeed() {
        return speed;
    }

    public NodePath getShortestPath() {
        return shortestPath;
    }


    public int getUid() {
        return uid;
    }

    public Integer getEndNode() {
        return endNode;
    }

    public Drone getDroneWithSameSpecificationsDifferentStart(Point startPoint, Set<Integer> nodesToBeIgnored) {

        Map.Entry<Integer, Node> startEntry = shortestPath
                .getIdToNode()
                .entrySet()
                .stream()
                .filter(entry -> entry.getValue().getAlt() == startPoint.getZ()
                        && entry.getValue().getLat() == startPoint.getY()
                        && entry.getValue().getLng() == startPoint.getX())
                .findFirst().orElse(null);

        if (startEntry != null) {
            return new Drone(this.uid, this.speed, startEntry.getKey(), this.endNode, getTimeStampForEachNodeInPath().get(startEntry.getKey()), new NodePath(Utils.idToNode, Utils.idToNeighbourDistance),nodesToBeIgnored, this.color);
        } else {
            return null;
        }

    }
}
