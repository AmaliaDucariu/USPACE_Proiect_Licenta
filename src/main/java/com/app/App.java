package com.app;


import com.data.CentralProcessingUnit;
import com.data.Drones.Drone;
import com.data.NodeStructures.NodeStaticMethods;
import com.displayUtils.Display;
import com.displayUtils.Utils;
import com.esri.arcgisruntime.geometry.Point;
import com.esri.arcgisruntime.geometry.PointCollection;
import com.esri.arcgisruntime.geometry.Polyline;
import com.esri.arcgisruntime.mapping.view.Graphic;
import com.esri.arcgisruntime.mapping.view.GraphicsOverlay;
import com.esri.arcgisruntime.mapping.view.LayerSceneProperties;
import com.esri.arcgisruntime.mapping.view.SceneView;
import com.esri.arcgisruntime.symbology.SimpleLineSymbol;
import com.network.Network;
import javafx.application.Application;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.*;


public class App extends Application {

    private final SceneView sceneView = new SceneView();
    final GraphicsOverlay graphicsOverlay = new GraphicsOverlay();
    final GraphicsOverlay graphicsOverlayForDrones = new GraphicsOverlay();


    public static void main(String[] args) {
        Application.launch(args);

    }

    @Override
    public void start(Stage stage) throws IOException {
        // Construct DataBase
        //Network.constructDataBase();
        graphicsOverlayForDrones.getSceneProperties().setSurfacePlacement(LayerSceneProperties.SurfacePlacement.RELATIVE);
        Display.setStageProperties(stage);
        Display.setArcgisMap(sceneView, graphicsOverlay);
        Display.displayBuilding(graphicsOverlay);
        Display.displayAllVirtualPoints(graphicsOverlay);
        Display.displayAircraftPath(graphicsOverlay);

        Map<Integer, Node> idToNode = NodeStaticMethods.getMapIdToNode();
        Map<Integer, Map<Integer, Double>> idToNeighbourDistance = NodeStaticMethods.getMapOfDistances(idToNode);
        Set<Integer> nodesToBeIgnored = new HashSet<>();



        Drone droneTest = new Drone(1, 10, 12, 1111, 0, new NodePath(idToNode, idToNeighbourDistance), nodesToBeIgnored, 0xFF04A2CA);

        Drone droneTest2 = new Drone(2, 10, 12, 123, 0, new NodePath(idToNode, idToNeighbourDistance), nodesToBeIgnored,0xFF3AAA80);


        Drone droneTest7 = new Drone(7,10, 12, 2469, 0, new NodePath(idToNode, idToNeighbourDistance), nodesToBeIgnored, 0xFFB2F5D9);

        Drone droneTest8 = new Drone(8,10, 1, 6478, 0, new NodePath(idToNode, idToNeighbourDistance), nodesToBeIgnored, 0xFF191A1A);

        List<Drone> listDroneTest = new ArrayList<>();
        listDroneTest.add(droneTest);
        listDroneTest.add(droneTest2);
        listDroneTest.add(droneTest7);
        listDroneTest.add(droneTest8);
        CentralProcessingUnit cpu = new CentralProcessingUnit(listDroneTest, idToNode);
        cpu.resolveConflicts();
        sceneView.getGraphicsOverlays().add(graphicsOverlay);
        sceneView.getGraphicsOverlays().add(graphicsOverlayForDrones);
        Display.animateDrones(listDroneTest, idToNode, graphicsOverlayForDrones);
        Display.setMenus(stage, sceneView, graphicsOverlay,graphicsOverlayForDrones,  idToNode, listDroneTest);

    }

    /**
     * Stops and releases all resources used in application.
     */
    @Override
    public void stop() {
        sceneView.dispose();
    }

}
