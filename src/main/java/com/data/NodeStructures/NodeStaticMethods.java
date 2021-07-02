package com.data.NodeStructures;

import com.app.Node;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class NodeStaticMethods {
    final static Path POINTS_PATH = Paths.get("src/main/java/com/data/Points.txt");
    final static Path NEIGHBOURS_PATH = Paths.get("src/main/java/com/data/Neighbours.txt");

    public static List<Node> getListOfNodes() throws IOException {
        return Files.readAllLines(POINTS_PATH).stream().map(line -> {
            final String[] nodeValues = line.split("[,= \\[\\]]+");
            final Node nodeToBeAdded = new Node(Integer.valueOf(nodeValues[0]));
            nodeToBeAdded.setLat(Double.parseDouble(nodeValues[1]));
            nodeToBeAdded.setLng(Double.parseDouble(nodeValues[2]));
            nodeToBeAdded.setAlt(Double.parseDouble(nodeValues[3]));
            return nodeToBeAdded;
        }).collect(Collectors.toList());
    }

    public static Map<Integer, Node> getMapIdToNode() throws IOException {
        List<Node> allNodes = getListOfNodes();
        Map<Integer, List<Node>> allNeighbours = Files.readAllLines(NEIGHBOURS_PATH)
                .stream()
                .map(line -> Arrays.asList(line.split("[,= \\[\\]]+")))
                .collect(Collectors.toMap(line -> Integer.valueOf(line.get(0)),
                        line -> line
                                .stream()
                                .skip(1)
                                .map(element -> allNodes
                                        .stream()
                                        .filter(node -> node.getId().equals(Integer.valueOf(element)))
                                        .findFirst()
                                        .orElse(null))
                                .filter(Objects::nonNull)
                                .collect(Collectors.toList())));

        allNodes.parallelStream().forEach(node -> node.setNeighbours(allNeighbours.get(node.getId())));
        return allNodes.parallelStream().collect(Collectors.toMap(Node::getId, node -> node));
    }
    public static Map<Integer, Map<Integer, Double>> getMapOfDistances(Map<Integer, Node> idToNode){
        return idToNode.values().parallelStream().collect(Collectors.toMap(Node::getId, node -> node.getNeighbours().stream().collect(Collectors.toMap(Node::getId, node::getDistance))));
    }

}
