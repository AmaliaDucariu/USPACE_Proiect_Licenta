package com.network;

import com.app.Node;
import com.data.Drones.Drone;
import com.data.NodeStructures.NodeStaticMethods;
import com.esri.arcgisruntime.geometry.Polygon;

import java.awt.geom.Line2D;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class RestrictedPoints {
    private static List<Node> listOfNodes = new ArrayList<>();

    static {
        try {
            listOfNodes = NodeStaticMethods.getListOfNodes();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static boolean onSegment(Node p, Node q, Node r) {
        return q.getLat() <= Math.max(p.getLat(), r.getLat()) &&
                q.getLat() >= Math.min(p.getLat(), r.getLat()) &&
                q.getLng() <= Math.max(p.getLng(), r.getLng()) &&
                q.getLng() >= Math.min(p.getLng(), r.getLng());
    }

    private static int orientation(Node p, Node q, Node r) {
        double val = (q.getLng() - p.getLng()) * (r.getLat() - q.getLat())
                - (q.getLat() - p.getLat()) * (r.getLng() - q.getLng());

        if (val == 0.0) {
            return 0;
        }
        return (val > 0) ? 1 : 2;
    }

    public static boolean doIntersect(Node p1, Node q1,
                                      Node p2, Node q2) {
        int o1 = orientation(p1, q1, p2);
        int o2 = orientation(p1, q1, q2);
        int o3 = orientation(p2, q2, p1);
        int o4 = orientation(p2, q2, q1);
        if (o1 != o2 && o3 != o4) {
            return true;
        }
        if (o1 == 0 && onSegment(p1, p2, q1)) {
            return true;
        }


        if (o2 == 0 && onSegment(p1, q2, q1)) {
            return true;
        }


        if (o3 == 0 && onSegment(p2, p1, q2)) {
            return true;
        }
        if (o4 == 0 && onSegment(p2, q1, q2)) {
            return true;
        }
        return false;
    }

    private static boolean isInside(List<Node> polygon, Node p) {
        int n = polygon.size();
        if (n < 3) {
            return false;
        }


        Node extreme = new Node(-1);
        extreme.setLat(90);
        extreme.setLng(p.getLng());
        int count = 0, i = 0;
        do {
            int next = (i + 1) % n;
            if (doIntersect(polygon.get(i), polygon.get(next), p, extreme)) {
                if (orientation(polygon.get(i), p, polygon.get(next)) == 0) {
                    return onSegment(polygon.get(i), p,
                            polygon.get(next));
                }

                count++;
            }
            i = next;
        } while (i != 0);
        return (count % 2 == 1);
    }

    public static boolean intersects(List<Node> polygon, Node p, Node p2) {
        int n = polygon.size();
        if (n < 3) {
            return false;
        }
        int count = 0, i = 0;
        do {
            int next = (i + 1) % n;
            if (doIntersect(polygon.get(i), polygon.get(next), p, p2)) {
                if (orientation(polygon.get(i), p, polygon.get(next)) == 0) {
                    return onSegment(polygon.get(i), p,
                            polygon.get(next));
                }

                count++;
            }
            i = next;
        } while (i != 0);
        return (count % 2 == 1);
    }


    public static List<Node> getRestrictedNodes1(final List<Node> pol, double minAlt, double maxAlt) {
        return listOfNodes.parallelStream().filter(node -> isInside(pol, node)).filter(node -> node.getAlt() >= minAlt && node.getAlt() <= maxAlt).collect(Collectors.toList());
    }

    public static List<Node> getRestrictedNodes(final List<Node> pol, double minAlt, double maxAlt) {
        final Set<Node> requiredNodes = new HashSet<>();
        final Node extreme = new Node(-1);

        final List<Node> listOfNodesWithinAltitude = listOfNodes.parallelStream().filter(node -> node.getAlt() >= minAlt && node.getAlt() <= maxAlt).collect(Collectors.toList());


        for (Node node : listOfNodesWithinAltitude) {
            extreme.setLat(90);
            extreme.setLng(node.getLng());
            int noOfIntersections = 0;
            for (int i = 0; i < pol.size() - 1; i++) {
                if (Line2D.linesIntersect(
                        node.getLat(),
                        node.getLng(),
                        extreme.getLat(),
                        extreme.getLng(),

                        pol.get(i).getLat(),
                        pol.get(i).getLng(),
                        pol.get(i + 1).getLat(),
                        pol.get(i + 1).getLng())) {
                    noOfIntersections++;

                }
            }
            if (noOfIntersections % 2 != 0){
                requiredNodes.add(node);
            }
        }
        for (Node node : listOfNodesWithinAltitude) {
            extreme.setLat(node.getLat());
            extreme.setLng(180);
            int noOfIntersections = 0;
            for (int i = 0; i < pol.size() - 1; i++) {
                if (Line2D.linesIntersect(
                        node.getLat(),
                        node.getLng(),
                        extreme.getLat(),
                        extreme.getLng(),

                        pol.get(i).getLat(),
                        pol.get(i).getLng(),
                        pol.get(i + 1).getLat(),
                        pol.get(i + 1).getLng())) {
                    noOfIntersections++;

                }
            }
            if (noOfIntersections % 2 != 0){
                requiredNodes.add(node);
            }
        }
        return requiredNodes.parallelStream().collect(Collectors.toList());
    }

}
