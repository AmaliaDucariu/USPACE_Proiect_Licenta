package com.app;

import java.util.*;
import java.util.stream.Collectors;


public class NodePath {
    private final Map<Integer, Node> idToNode;
    private final List<Integer> orderOfNodes = new ArrayList<>();
    private double multiplicationFactor = 1;
    private final Map<Integer, Map<Integer, Double>> idToNeighbourDistance;
    private final Map<Integer, Integer> nodeIdToMultiplicationFactor = new HashMap<>();

    public NodePath(Map<Integer, Node> idToNode, Map<Integer, Map<Integer, Double>> idToNeighbourDistance) {
        this.idToNode = idToNode;
        this.idToNeighbourDistance = idToNeighbourDistance;
    }

    public List<Integer> findPath(final Integer startID, final Integer endID, final int h, final Integer intermediateNodeId,  Set<Integer> nodesToBeIgnored) {

        updateIdToNeighbourDistance(intermediateNodeId);
        final Node start = idToNode.get(startID);
        final Node end = idToNode.get(endID);
        final LinkedList<Node> openSet = new LinkedList<>();
        final Map<Integer, Integer> cameFrom = new HashMap<>();

        // Data structure for keeping up with the G values for each node
        final Map<Integer, Double> gScore = new HashMap<>();


        // Data structure for keeping up with the F values for each node fScore[n] := gScore[n] + h(n)
        final Map<Integer, Double> fScore = new HashMap<>();
        idToNode.keySet().forEach(key -> {
            gScore.put(key, Double.POSITIVE_INFINITY);
            fScore.put(key, Double.POSITIVE_INFINITY);
        });

        // Initialize Start com.app.Node
        start.setG(0);
        start.setH(h);

        // Keep track of Start com.app.Node
        openSet.add(start);

        // Update scores for Start com.app.Node
        gScore.put(start.getId(), (double) 0);
        fScore.put(start.getId(), start.getH(idToNode.get(endID)));

        while (!openSet.isEmpty()) {
            final Integer minFScoreId = Objects.requireNonNull(fScore.entrySet().stream().min(Map.Entry.comparingByValue()).orElse(null)).getKey();
            Node currentNode = this.idToNode.get(minFScoreId);

            if (!currentNode.getId().equals(startID) && nodesToBeIgnored.contains(currentNode.getId())){
                openSet.remove(currentNode);
                fScore.remove(currentNode.getId());
                continue;
            }

            if (currentNode.isTheSameNode(end)) {
                final List<Integer> finalOrder = new ArrayList<>(this.getOrderOfNodes(cameFrom, end.getId(), start.getId()));
                orderOfNodes.clear();
                return finalOrder;
            }

            openSet.remove(currentNode);
            fScore.remove(currentNode.getId());

            for (Node neighbour : currentNode.getNeighbours()) {
                double tempGScore = gScore.get(currentNode.getId()) + idToNeighbourDistance.get(currentNode.getId()).get(neighbour.getId());


                if (tempGScore < gScore.get(neighbour.getId())) {
                    cameFrom.put(neighbour.getId(), currentNode.getId());
                    gScore.put(neighbour.getId(), tempGScore);
                    fScore.put(neighbour.getId(), gScore.get(neighbour.getId()) + neighbour.getH(idToNode.get(endID)));

                    if (!openSet.contains(neighbour)) {
                        openSet.add(neighbour);
                    }
                }
            }

        }
        return new ArrayList<>();
    }
    private Integer reconstructPath(Map<Integer, Integer> cameFrom, Integer currentNode, Integer startId) {
        this.orderOfNodes.add(currentNode);

        if (currentNode.equals(startId)) {
            return currentNode;
        } else {
            return reconstructPath(cameFrom, cameFrom.get(currentNode), startId);
        }
    }
    private List<Integer> getOrderOfNodes(Map<Integer, Integer> cameFrom, Integer currentNode, Integer startId) {
        this.reconstructPath(cameFrom, currentNode, startId);
        Collections.reverse(this.orderOfNodes);
        return orderOfNodes;
    }

    public Map<Integer, Node> getIdToNode() {
        return idToNode;
    }

    public Map<Integer, Map<Integer, Double>> getIdToNeighbourDistance() {
        return idToNeighbourDistance;
    }

    public double getAndIncrementMultiplicationFactor() {
        final double tempMultiplicationFactor = this.multiplicationFactor;
        this.multiplicationFactor++;
        return tempMultiplicationFactor;
    }
    public double getMultipliedAddedDistance() {
        double addedDistance = 40.0 / 3600 / 3;
        return getAndIncrementMultiplicationFactor() * addedDistance;
    }
    private void updateIdToNeighbourDistance(Integer lastPoint){
        if (nodeIdToMultiplicationFactor.containsKey(lastPoint)){
            nodeIdToMultiplicationFactor.put(lastPoint, nodeIdToMultiplicationFactor.get(lastPoint) + 1);
        }else{
            nodeIdToMultiplicationFactor.put(lastPoint,  1);
        }

    }


    public Map<Integer, Integer> getNodeIdToMultiplicationFactor() {
        return nodeIdToMultiplicationFactor;
    }

    public void adjustIdToNeighbourDistance(List<Node> restrictedNodes) {
        Set<Integer> idsOfRestrictedNodes = restrictedNodes.parallelStream().map(Node::getId).collect(Collectors.toSet());


        Map<Integer, Map<Integer, Double>> adjustedSpecificValues = idToNeighbourDistance
                .entrySet()
                .stream()
                .filter(entry -> entry
                        .getValue()
                        .keySet()
                        .parallelStream()
                        .distinct()
                        .anyMatch(idsOfRestrictedNodes::contains))
                .collect(Collectors
                        .toMap(Map.Entry::getKey,
                                entry -> entry
                                        .getValue()
                                        .entrySet()
                                        .parallelStream()
                                        .filter(ndEntry -> idsOfRestrictedNodes.contains(ndEntry.getKey()))
                                        .collect(Collectors
                                                .toMap(Map.Entry::getKey,
                                                        ndEntry -> ndEntry.getValue() * Double.POSITIVE_INFINITY))));
        adjustedSpecificValues.forEach((key, value) -> value.forEach((keyInner, valueInner) -> idToNeighbourDistance.get(key).put(keyInner, valueInner)));


        idToNeighbourDistance
                .entrySet()
                .stream()
                .filter(entry -> idsOfRestrictedNodes
                        .contains(entry.getKey()))
                .forEach(entry -> idToNeighbourDistance
                        .put(entry.getKey(),
                                entry.getValue()
                                        .entrySet()
                                        .parallelStream()
                                        .collect(Collectors.toMap(Map.Entry::getKey,
                                                el -> el.getValue() *  Double.POSITIVE_INFINITY))));

    }

}
