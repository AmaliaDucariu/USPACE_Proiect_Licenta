package com.data;

import com.app.Node;
import com.app.NodePath;
import com.data.Drones.Drone;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class CentralProcessingUnit {
    private final List<Drone> drones;
    private final Map<Integer, Node> idToNode;
    private Set<Integer> visitedNodes;
    private Map<Integer, List<Drone>> nodesToListOfVisitors;


    public CentralProcessingUnit(List<Drone> drones, Map<Integer, Node> idToNode) {
        this.drones = drones;
        this.idToNode = idToNode;
    }

    public void resolveConflicts() {
        while (true) {
            updateNodesToListOfVisitors();
            boolean existsTimeConflict = false;
            for (Map.Entry<Integer, List<Drone>> entry : nodesToListOfVisitors.entrySet()) {
                final Drone droneToBeRerouted = getReroutedDrone(entry);
                if (droneToBeRerouted != null && droneToBeRerouted.getOrderOfNodes().indexOf(entry.getKey()) > 0) {
                    final Integer lastValidNode = droneToBeRerouted.getOrderOfNodes().get(droneToBeRerouted.getOrderOfNodes().indexOf(entry.getKey()) - 1);
                    droneToBeRerouted.setNewPath(lastValidNode);
                    existsTimeConflict = true;
                    break;
                }
            }
            if (!existsTimeConflict) {
                break;
            }
        }
    }
    public void resolveConflictsForOneDrone(Drone newDrone){
        while (true) {
            updateNodesToListOfVisitors();
            boolean existsTimeConflict = false;
            for (Map.Entry<Integer, List<Drone>> entry : nodesToListOfVisitors.entrySet()) {
                final Drone droneToBeRerouted = getReroutedDrone(entry);
                if (droneToBeRerouted != null && newDrone.getUid() == droneToBeRerouted.getUid() && droneToBeRerouted.getOrderOfNodes().indexOf(entry.getKey()) > 0) {
                    final Integer lastValidNode = droneToBeRerouted.getOrderOfNodes().get(droneToBeRerouted.getOrderOfNodes().indexOf(entry.getKey()) - 1);
                    droneToBeRerouted.setNewPath(lastValidNode);
                    existsTimeConflict = true;
                    break;
                }
            }
            if (!existsTimeConflict) {
                break;
            }
        }
    }


    public void getAllVisitedNodes() {
        this.visitedNodes = drones
                .stream()
                .flatMap(drone -> drone.getTimeStampForEachNodeInPath()
                        .keySet()
                        .stream())
                .collect(Collectors.toSet());

    }

    public void updateNodesToListOfVisitors() {
        getAllVisitedNodes();
        this.nodesToListOfVisitors = visitedNodes
                .stream()
                .collect(Collectors
                        .toMap(nodeID -> nodeID, nodeID -> drones
                                .stream()
                                .filter(drone -> drone.getTimeStampForEachNodeInPath()
                                        .containsKey(nodeID))
                                .sorted((o1, o2) -> {
                                    if (o1.getTimeStampForEachNodeInPath().get(nodeID).equals(o2.getTimeStampForEachNodeInPath().get(nodeID)))
                                        return 0;
                                    else if (o1.getTimeStampForEachNodeInPath().get(nodeID) > o2.getTimeStampForEachNodeInPath().get(nodeID))
                                        return -1;
                                    else return 1;
                                })
                                .collect(Collectors.toList())));
    }

    private Drone getReroutedDrone(Map.Entry<Integer, List<Drone>> nodeIDToDroneVisitors) {
        final List<Drone> drones = nodeIDToDroneVisitors.getValue();
        for (int i = 1; i < drones.size(); i++) {
            final double timeDifference = drones.get(i-1).getNodeToEstimatedTime().get(nodeIDToDroneVisitors.getKey()) - drones.get(i).getNodeToEstimatedTime().get(nodeIDToDroneVisitors.getKey());
            if (Math.abs(timeDifference) < Math.max(drones.get(i).getSafetyNodeOccupancyTime(), drones.get(i-1).getSafetyNodeOccupancyTime())) {
                return drones.get(i);
            }
        }
        return null;
    }


    public List<Drone> getDrones() {
        return drones;
    }
}
