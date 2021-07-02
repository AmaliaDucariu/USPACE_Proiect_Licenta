package com.network;

import com.app.Node;
import com.data.NodeStructures.NodeStaticMethods;
import com.displayUtils.Utils;
import com.esri.arcgisruntime.geometry.*;
import com.esri.arcgisruntime.mapping.view.Graphic;
import com.esri.arcgisruntime.symbology.SimpleFillSymbol;
import com.esri.arcgisruntime.symbology.SimpleLineSymbol;

import java.awt.geom.Line2D;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class RestrictedVolume {


    List<String> areaIDs;
    private static final SpatialReference SPATIAL_REFERENCE = Utils.SPATIAL_REFERENCE;
    SimpleLineSymbol lineSymbol = new SimpleLineSymbol(SimpleLineSymbol.Style.SOLID, 0xFFBAE1FF, 1);
    SimpleFillSymbol polygonFillSymbol =
            new SimpleFillSymbol(SimpleFillSymbol.Style.SOLID, 0x75FF6F69, lineSymbol);

    double lowerAlt;
    double upperAlt;

    public RestrictedVolume(final List<String> areaIDs, double lowerAlt, double upperAlt) {
        this.areaIDs = areaIDs;
        this.lowerAlt = lowerAlt;
        this.upperAlt = upperAlt;
    }

    public List<Node> getPolyline() {
        final List<Node> listOfNodes;
        List<Node> allNodes = new ArrayList<>();
        try {
            listOfNodes = NodeStaticMethods.getListOfNodes();
            for (int i = 0; i < areaIDs.size(); i++) {
                for (int j = 0; j < listOfNodes.size(); j++) {
                    if (Integer.parseInt(areaIDs.get(i)) == listOfNodes.get(j).getId()) {
                        allNodes.add(listOfNodes.get(j));
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return allNodes;
    }


    final public List<Graphic> getAllVerticalAreas() {
        List<Node> polylineNodes = getPolyline();
        List<Graphic> allVerticalPolygons = new ArrayList<>();
        final PointCollection upperLid = new PointCollection(SPATIAL_REFERENCE);
        final List<Point> upperLidPoints = polylineNodes.stream().map(node -> new Point(node.getLng(), node.getLat(), upperAlt)).collect(Collectors.toList());
        upperLid.addAll(upperLidPoints);
        final PointCollection lowerLid = new PointCollection(SPATIAL_REFERENCE);
        final List<Point> lowerLidPoints = polylineNodes.stream().map(node -> new Point(node.getLng(), node.getLat(), lowerAlt)).collect(Collectors.toList());
        lowerLid.addAll(lowerLidPoints);
        allVerticalPolygons.add(new Graphic(new Polygon(upperLid), polygonFillSymbol));
        allVerticalPolygons.add(new Graphic(new Polygon(lowerLid), polygonFillSymbol));
        for (int i = 0; i < polylineNodes.size() - 1; i++) {
            final PointCollection points = new PointCollection(SPATIAL_REFERENCE);
            points.add(new Point(polylineNodes.get(i).getLng(), polylineNodes.get(i).getLat(), lowerAlt));
            points.add(new Point(polylineNodes.get(i + 1).getLng(), polylineNodes.get(i + 1).getLat(), lowerAlt));
            points.add(new Point(polylineNodes.get(i + 1).getLng(), polylineNodes.get(i + 1).getLat(), upperAlt));
            points.add(new Point(polylineNodes.get(i).getLng(), polylineNodes.get(i).getLat(), upperAlt));
            allVerticalPolygons.add(new Graphic(new Polygon(points), polygonFillSymbol));
        }
        return allVerticalPolygons;
    }

    final public Set<Integer> getNodesToBeIgnored() {
        //TODO iterate only through restricted nodes
        final Set<Integer> nodesToBeIgnored = new HashSet<>();
        try {
            Map<Integer, Node> idToNode = NodeStaticMethods.getMapIdToNode();
            final List<Node> listOfNodes;
            List<Node> polylineNodes = getPolyline();
            listOfNodes = new ArrayList<>(idToNode.values());
            for (int i = 0; i < listOfNodes.size() - 1; i++) {
                for (int k = 0; k < listOfNodes.get(i).getNeighbours().size() - 1; k++) {
                    for (int j = 0; j < polylineNodes.size() - 1; j++) {
                        if (Line2D.linesIntersect(
                                listOfNodes.get(i).getLat(),
                                listOfNodes.get(i).getLng(),
                                listOfNodes.get(i).getNeighbours().get(k).getLat(),
                                listOfNodes.get(i).getNeighbours().get(k).getLng(),

                                polylineNodes.get(j).getLat(),
                                polylineNodes.get(j).getLng(),
                                polylineNodes.get(j + 1).getLat(),
                                polylineNodes.get(j + 1).getLng())
                                && listOfNodes.get(i).getAlt() >= lowerAlt && listOfNodes.get(i).getAlt() <= upperAlt) {
                            nodesToBeIgnored.add(listOfNodes.get(i).getId());
                        }
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return nodesToBeIgnored;
    }
}


