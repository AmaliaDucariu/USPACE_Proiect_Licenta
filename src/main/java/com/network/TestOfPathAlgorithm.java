package com.network;

import com.app.Node;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TestOfPathAlgorithm {

    public void testCase(){
        Map<Integer, Node> idToNode = new HashMap<>();


        Map<List<Integer>, Integer> distance = new HashMap<>();


        Node node1 = new Node(1);
        Node node2 = new Node(2);
        Node node3 = new Node(3);
        Node node4 = new Node(4);
        Node node5 = new Node(5);
        Node node6 = new Node(6);
        node1.setNeighbours(Arrays.asList(node6, node3, node2));
        node2.setNeighbours(Arrays.asList(node1, node3, node4));
        node3.setNeighbours(Arrays.asList(node1, node2, node4, node6));
        node4.setNeighbours(Arrays.asList(node5, node2, node3));
        node5.setNeighbours(Arrays.asList(node6, node4));
        node6.setNeighbours(Arrays.asList(node1, node3, node5));


        idToNode.put(1, node1);
        idToNode.put(2, node2);
        idToNode.put(3, node3);
        idToNode.put(4, node4);
        idToNode.put(5, node5);
        idToNode.put(6, node6);


        distance.put(Arrays.asList(1, 3), 9);
        distance.put(Arrays.asList(1, 6), 14);
        distance.put(Arrays.asList(1, 2), 7);
        distance.put(Arrays.asList(2, 4), 15);
        distance.put(Arrays.asList(2, 3), 10);
        distance.put(Arrays.asList(4, 3), 11);
        distance.put(Arrays.asList(4, 5), 6);
        distance.put(Arrays.asList(5, 6), 9);
        distance.put(Arrays.asList(3, 6), 2);
    }
}
