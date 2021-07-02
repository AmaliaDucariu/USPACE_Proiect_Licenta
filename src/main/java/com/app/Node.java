package com.app;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class Node implements Comparator<Node> {
    private static final double NM_TO_M_FACTOR = 1852;
    private double g;
    private double h;
    private double f;
    private Node parent;
    private final Integer id;
    private int score;
    private double lat = -1;
    private double lng = -1;
    private double alt = -1;
    private static final double DEG_TO_NM_FACTOR = 60;
    private static final double FT_TO_NM_FACTOR = 0.000164578834;

    private List<Node> neighbours;

    public Node(Integer id) {
        this.g = 0;
        this.f = 0;
        this.id = id;
    }

    public void setNeighbours(List<Node> neighbours) {
        this.neighbours = neighbours;
    }

    public List<Node> getNeighbours() {
        return this.neighbours;
    }

    public void setF(double f) {
        this.f = f;
    }

    public void setH(double h) {
        this.h = h;
    }

    public void setG(double g) {
        this.g = g;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public void setAlt(double alt) {
        this.alt = alt;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public void setLng(double lng) {
        this.lng = lng;
    }

    public Integer getId() {
        return this.id;
    }

    public double getH(Node endNode) {
        return Math.abs(this.getAlt() - endNode.getAlt()) + Math.abs(this.getLat() - endNode.getLat()) + Math.abs(this.getLng() - endNode.getLng());
    }

    public int getScore() {
        return score;
    }

    public double getLng() {
        return lng;
    }

    public double getAlt() {
        return alt;
    }

    public double getLat() {
        return lat;
    }

    public boolean isTheSameNode(final Node nextNode) {
        return this.id.equals(nextNode.getId());
    }


    /*
     *  To account for the time buffer (in case the drone needs to standstill and not to search for another node,
     * it is considered that the node has itself as a neighbour, having the distance to itself equal to 0.003703703703703704 NM
     *
     * This distance is the result of an averaging speed of 40kts and a time buffer of aprox 3 sec.
     *
     * */
    public double getDistance(final Node neighbour) {
        return  Math.sqrt(Math.pow((this.lat - neighbour.getLat()) * DEG_TO_NM_FACTOR, 2)
                + Math.pow((this.lng - neighbour.getLng()) * DEG_TO_NM_FACTOR, 2)
                + Math.pow((this.alt - neighbour.getAlt()) * FT_TO_NM_FACTOR, 2)) * NM_TO_M_FACTOR;
    }

    @Override
    public int compare(final Node node, final Node t1) {
        return node.getScore() - t1.getScore();
    }

    public Double getG() {
        return this.g;
    }
}
