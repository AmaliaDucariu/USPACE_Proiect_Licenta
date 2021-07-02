package com.displayUtils;

import com.app.ApiKey;
import com.app.Node;
import com.app.NodePath;
import com.data.CentralProcessingUnit;
import com.data.Drones.AnimationModel;
import com.data.Drones.Drone;
import com.data.NodeStructures.NodeStaticMethods;
import com.database.Database;
import com.esri.arcgisruntime.ArcGISRuntimeEnvironment;
import com.esri.arcgisruntime.geometry.*;
import com.esri.arcgisruntime.mapping.ArcGISScene;
import com.esri.arcgisruntime.mapping.ArcGISTiledElevationSource;
import com.esri.arcgisruntime.mapping.Basemap;
import com.esri.arcgisruntime.mapping.Surface;
import com.esri.arcgisruntime.mapping.view.*;
import com.esri.arcgisruntime.symbology.*;
import com.network.RestrictedPoints;
import com.network.RestrictedVolume;
import javafx.animation.KeyFrame;
import javafx.animation.ParallelTransition;
import javafx.animation.Timeline;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static javafx.geometry.Pos.TOP_LEFT;

public class Display {
    private static final List<Drone> listDroneTest = new ArrayList<>();
    private static final ParallelTransition seqT = new ParallelTransition();
    private static final Map<Integer, Point> dronePosition = new HashMap<>();

    public static void display(String serialNumber, String droneID) {
        Stage popupWindow = new Stage();
        popupWindow.initModality(Modality.APPLICATION_MODAL);
        popupWindow.setTitle("WARNING: Registration Failed!");

        Label label1 = new Label("The Serial Number " + serialNumber + " OR the Drone Identifier " + droneID + " introduced is not valid!");

        Label label2 = new Label("The Serial Number/Drone Identifier introduced is not registered in our database.");

        VBox layout = new VBox(10);

        layout.getChildren().addAll(label1, label2);

        layout.setAlignment(Pos.CENTER);

        Scene scene1 = new Scene(layout, 600, 100);

        popupWindow.setScene(scene1);

        popupWindow.showAndWait();

    }

    public static void setMenus(Stage stage, SceneView sceneView, GraphicsOverlay graphicsOverlay,GraphicsOverlay graphicsOverlayForDrones ,Map<Integer, Node> idToNode, List<Drone> listDrones) {
        // create JavaFX VBox for user perspective
        Label certificationSerialNumberLabel = new Label("Certificate Serial Number");
        Label droneIdentificationNumberLabel = new Label("Drone Identifier");
        Label departurePointLabel = new Label("Departure Id");
        Label arrivalPointLabel = new Label("Arrival Id");

        Button scenarioOneToScenarioTwoButton = new Button("Users Perspective");
        TextField certificationSerialNumber = new TextField();
        TextField droneIdentificationNumber = new TextField();
        TextField departurePoint = new TextField();
        TextField arrivalPoint = new TextField();
        Button registerButton = new Button("Register");

        VBox userInputMenu = new VBox();
        userInputMenu.setMaxWidth(300);
        userInputMenu.setMaxHeight(200);
        userInputMenu.getChildren().addAll(scenarioOneToScenarioTwoButton, certificationSerialNumberLabel, certificationSerialNumber, droneIdentificationNumberLabel, droneIdentificationNumber, departurePointLabel, departurePoint, arrivalPointLabel, arrivalPoint, registerButton);

        //create JavaFX VBox for USSP perspective
        Label volumeToBeAvoidedLabel = new Label("Volume To Be Avoided");
        Label lowerLevelLabel = new Label("Lower Level");
        Label upperLevelLabel = new Label("Upper Level");

        TextField volumeToBeAvoided = new TextField();
        TextField lowerLevel = new TextField();
        TextField upperLevel = new TextField();
        Button addRestrictedVolume = new Button("Restrict Volume");
        Button clearAllRestrictedVolumes = new Button("Clear All Restricted Volumes");

        VBox usspInputMenu = new VBox();
        usspInputMenu.setMaxWidth(300);
        usspInputMenu.setMaxHeight(200);
        usspInputMenu.getChildren().addAll(volumeToBeAvoidedLabel, volumeToBeAvoided, lowerLevelLabel, lowerLevel, upperLevelLabel, upperLevel, addRestrictedVolume, clearAllRestrictedVolumes);

        stage.show();

        // create a JavaFX scene with a stack pane as the root node, and add it to the scene
        StackPane stackPane = new StackPane();
        Scene fxScene = new Scene(stackPane);
        StackPane.setAlignment(userInputMenu, TOP_LEFT);
        StackPane.setAlignment(usspInputMenu, TOP_LEFT);
        stage.setScene(fxScene);
        // create a scene view to display the scene and add it to the stack pane
        stackPane.getChildren().add(sceneView);
        stackPane.getChildren().add(userInputMenu);//, departurePoint, arrivalPoint, volumeToBeAvoided, registerButton);

        // Add listeners
        registerButton.setOnAction(event -> {
            if (!Database.getAllSerialNumbersAndDroneIDs().get(0).contains(Long.parseLong(certificationSerialNumber.getText())) || !Database.getAllSerialNumbersAndDroneIDs().get(1).contains(Long.parseLong(droneIdentificationNumber.getText()))) {
                Display.display(certificationSerialNumber.getText(), droneIdentificationNumber.getText());
            } else {
                Drone newDroneDrone = new Drone(-1, 10, Integer.parseInt(departurePoint.getText()), Integer.parseInt(arrivalPoint.getText()), 0, new NodePath(idToNode, NodeStaticMethods.getMapOfDistances(idToNode)), new HashSet<>(), 0xFF9E3D64);
                Graphic planeSymbol = new Graphic(new Point(26.063180910236255, 44.56294582848301, 30.0, Utils.SPATIAL_REFERENCE), Utils.PLANE_3D_SYMBOL);
                graphicsOverlayForDrones.getGraphics().add(planeSymbol);
                CentralProcessingUnit cpu = new CentralProcessingUnit(listDroneTest, idToNode);
                cpu.resolveConflictsForOneDrone(newDroneDrone);

                AnimationModel newDrone = new AnimationModel(0);

                newDrone.setFrames(getMissionData(newDroneDrone, idToNode).size());
                newDrone.setKeyframe(0);

                Timeline newDroneTimeline = new Timeline();
                newDroneTimeline.setAutoReverse(true);
                newDroneTimeline.setCycleCount(Timeline.INDEFINITE);
                newDroneTimeline.getKeyFrames().add(new KeyFrame(Duration.millis(Utils.SPEED_TO_MILISEC_FACTOR / newDroneDrone.getSpeed()), e -> animate(newDrone.nextKeyframe(), getMissionData(newDroneDrone, idToNode), planeSymbol, newDroneDrone.getColor(), graphicsOverlayForDrones, -1)));
                seqT.stop();
                seqT.getChildren().add(newDroneTimeline);
                seqT.play();

            }
        });

        final AtomicBoolean isUserPerspective = new AtomicBoolean(true);
        scenarioOneToScenarioTwoButton.setOnAction(event -> {
            isUserPerspective.set(!isUserPerspective.get());
            if (isUserPerspective.get()) {
                scenarioOneToScenarioTwoButton.setText("Users Perspective");
                stackPane.getChildren().remove(usspInputMenu);
                userInputMenu.getChildren().add(0, scenarioOneToScenarioTwoButton);
                stackPane.getChildren().add(userInputMenu);
            } else {
                scenarioOneToScenarioTwoButton.setText("USSP Perspective");
                stackPane.getChildren().remove(userInputMenu);
                usspInputMenu.getChildren().add(0, scenarioOneToScenarioTwoButton);
                stackPane.getChildren().add(usspInputMenu);
            }

        });
        final AtomicReference<List<Graphic>> volume = new AtomicReference<>();
        addRestrictedVolume.setOnAction(event -> {
            List<Node> restrictedNodes = RestrictedPoints.getRestrictedNodes(Arrays.asList(volumeToBeAvoided.getText().split("[, ]+")).parallelStream().map(Integer::parseInt).map(idToNode::get).collect(Collectors.toList()), Double.parseDouble(lowerLevel.getText()), Double.parseDouble(upperLevel.getText()));
            seqT.stop();
            seqT.getChildren().clear();
            graphicsOverlayForDrones.getGraphics().clear();
            listDrones.forEach(drone -> {
                final PointCollection points = new PointCollection(Utils.SPATIAL_REFERENCE);
                final List <Point> paths =  drone.getOrderOfNodes().stream().map(idToNode::get).map(node -> new Point(node.getLng(), node.getLat(), node.getAlt())).collect(Collectors.toList());
                for (Point point : paths){
                    points.add(point);
                    if (Math.abs(point.getX() - dronePosition.get(drone.getUid()).getX()) < 1e-5 &&
                            Math.abs(point.getY() - dronePosition.get(drone.getUid()).getY()) < 1e-5 &&
                            Math.abs(point.getZ() - dronePosition.get(drone.getUid()).getZ()) < 1e-5){
                        break;
                    }
                }
                Polyline polyline = new Polyline(points);
                Graphic graphic = new Graphic(polyline, new SimpleLineSymbol(SimpleLineSymbol.Style.SOLID, 0xFFfcba03, 5));
                graphicsOverlayForDrones.getGraphics().add(graphic);
            });


            final RestrictedVolume restrictedVolume = new RestrictedVolume(Arrays.asList(volumeToBeAvoided.getText().split("[, ]+")), Double.parseDouble(lowerLevel.getText()), Double.parseDouble(upperLevel.getText()));
            final List<Drone> listDronesWhenControlVolumeIsActive = listDrones.stream().map((Drone drone) -> drone.getDroneWithSameSpecificationsDifferentStart(dronePosition.get(drone.getUid()), restrictedNodes.stream().map(Node::getId).collect(Collectors.toSet()))).filter(Objects::nonNull).filter(drone -> drone.getOrderOfNodes().size() > 0).collect(Collectors.toList());
            CentralProcessingUnit cpuWhenVolumeIsRestricted = new CentralProcessingUnit(listDronesWhenControlVolumeIsActive, idToNode);
            cpuWhenVolumeIsRestricted.resolveConflicts();
            Display.animateDrones(
                    listDronesWhenControlVolumeIsActive,
                    idToNode,
                    graphicsOverlayForDrones);

            if (volume.get() != null) {
                final List<Graphic> allGraphics = volume.get();
                allGraphics.addAll(restrictedVolume.getAllVerticalAreas());
                volume.set(allGraphics);
            } else {
                volume.set(restrictedVolume.getAllVerticalAreas());
            }
            graphicsOverlay.getGraphics().addAll(restrictedVolume.getAllVerticalAreas());
        });

        clearAllRestrictedVolumes.setOnAction(event -> {
            if (volume.get() != null) {
                volume.get().forEach(vol -> graphicsOverlay.getGraphics().remove(graphicsOverlay.getGraphics().size() - 1));
                volume.set(null);
            }

        });

    }

    public static void setArcgisMap(SceneView sceneView, GraphicsOverlay graphicsOverlay) {
        ArcGISRuntimeEnvironment.setApiKey(ApiKey.API_KEY);
        final ArcGISScene scene = new ArcGISScene(Basemap.createImagery());
        sceneView.setArcGISScene(scene);
        final Surface surface = new Surface();
        surface.getElevationSources().add(new ArcGISTiledElevationSource(Utils.ELEVATION_SERVICE_URL));
        surface.setElevationExaggeration(2.5f);
        scene.setBaseSurface(surface);
        sceneView.setViewpointCamera(Utils.POINT_OF_VIEW);
        graphicsOverlay.getSceneProperties().setSurfacePlacement(LayerSceneProperties.SurfacePlacement.RELATIVE);
    }

    public static void displayAllVirtualPoints(GraphicsOverlay graphicsOverlay) throws IOException {
        graphicsOverlay.getGraphics().addAll(NodeStaticMethods.getListOfNodes().stream()
                .map(node ->
                        new Graphic(new Point(node.getLng(), node.getLat(), node.getAlt(), Utils.SPATIAL_REFERENCE), Utils.markerSymbol(0xFFFFF9F9, 0xFFFFF9F9))) //new TextSymbol(15, node.getId().toString(), 0xFF21466C, TextSymbol.HorizontalAlignment.RIGHT, TextSymbol.VerticalAlignment.TOP))) //simpleMarkerSymbol))
                .collect(Collectors.toList()));
    }

    public static void displayAircraftPath(GraphicsOverlay graphicsOverlay) {

        final PointCollection airplanePathPoints = new PointCollection(Utils.SPATIAL_REFERENCE);
        airplanePathPoints.add(new Point(Utils.START_LONGITUDE + Utils.STEP_LONGITUDE * 23.5, Utils.START_LATITUDE - Utils.STEP_LATITUDE * 4, 65));
        airplanePathPoints.add(new Point(Utils.START_LONGITUDE + Utils.STEP_LONGITUDE * 23.5, Utils.END_LATITUDE - Utils.STEP_LATITUDE * 21, 65));
        airplanePathPoints.add(new Point(Utils.START_LONGITUDE + Utils.STEP_LONGITUDE * 28, Utils.END_LATITUDE - Utils.STEP_LATITUDE * 21, 65));
        airplanePathPoints.add(new Point(Utils.START_LONGITUDE + Utils.STEP_LONGITUDE * 28, Utils.START_LATITUDE - Utils.STEP_LATITUDE * 4,65));
        graphicsOverlay.getGraphics().add(new Graphic(new Polygon(airplanePathPoints), new SimpleFillSymbol(SimpleFillSymbol.Style.SOLID, 0xFF04A2CA, Utils.lineSymbol)));

        final PointCollection airplanePathPoints1 = new PointCollection(Utils.SPATIAL_REFERENCE);
        airplanePathPoints1.add(new Point(Utils.START_LONGITUDE + Utils.STEP_LONGITUDE * 23.5, Utils.START_LATITUDE - Utils.STEP_LATITUDE * 4, 40));
        airplanePathPoints1.add(new Point(Utils.START_LONGITUDE + Utils.STEP_LONGITUDE * 23.5, Utils.END_LATITUDE - Utils.STEP_LATITUDE * 21, 40));
        airplanePathPoints1.add(new Point(Utils.START_LONGITUDE + Utils.STEP_LONGITUDE * 28, Utils.END_LATITUDE - Utils.STEP_LATITUDE * 21,  40));
        airplanePathPoints1.add(new Point(Utils.START_LONGITUDE + Utils.STEP_LONGITUDE * 28, Utils.START_LATITUDE - Utils.STEP_LATITUDE * 4, 40));
        graphicsOverlay.getGraphics().add(new Graphic(new Polygon(airplanePathPoints1), new SimpleFillSymbol(SimpleFillSymbol.Style.SOLID, 0xFF04A2CA, Utils.lineSymbol)));

        final PointCollection airplanePathPoints2 = new PointCollection(Utils.SPATIAL_REFERENCE);
        airplanePathPoints2.add(new Point(Utils.START_LONGITUDE + Utils.STEP_LONGITUDE * 23.5, Utils.START_LATITUDE - Utils.STEP_LATITUDE * 4, 40));
        airplanePathPoints2.add(new Point(Utils.START_LONGITUDE + Utils.STEP_LONGITUDE * 23.5, Utils.END_LATITUDE - Utils.STEP_LATITUDE * 21, 40));
        airplanePathPoints2.add(new Point(Utils.START_LONGITUDE + Utils.STEP_LONGITUDE * 23.5, Utils.END_LATITUDE - Utils.STEP_LATITUDE * 21, 65));
        airplanePathPoints2.add(new Point(Utils.START_LONGITUDE + Utils.STEP_LONGITUDE * 23.5, Utils.START_LATITUDE - Utils.STEP_LATITUDE * 4, 65));
        graphicsOverlay.getGraphics().add(new Graphic(new Polygon(airplanePathPoints2), new SimpleFillSymbol(SimpleFillSymbol.Style.SOLID, 0xFF04A2CA, Utils.lineSymbol)));

        final PointCollection airplanePathPoints3 = new PointCollection(Utils.SPATIAL_REFERENCE);
        airplanePathPoints3.add(new Point(Utils.START_LONGITUDE + Utils.STEP_LONGITUDE * 28, Utils.START_LATITUDE - Utils.STEP_LATITUDE * 4, 40));
        airplanePathPoints3.add(new Point(Utils.START_LONGITUDE + Utils.STEP_LONGITUDE * 28, Utils.END_LATITUDE - Utils.STEP_LATITUDE * 21, 40));
        airplanePathPoints3.add(new Point(Utils.START_LONGITUDE + Utils.STEP_LONGITUDE * 28, Utils.END_LATITUDE - Utils.STEP_LATITUDE * 21, 65));
        airplanePathPoints3.add(new Point(Utils.START_LONGITUDE + Utils.STEP_LONGITUDE * 28, Utils.START_LATITUDE - Utils.STEP_LATITUDE * 4, 65));
        graphicsOverlay.getGraphics().add(new Graphic(new Polygon(airplanePathPoints3), new SimpleFillSymbol(SimpleFillSymbol.Style.SOLID, 0xFF04A2CA, Utils.lineSymbol)));


    }

    public static void displayBuilding(GraphicsOverlay graphicsOverlay) {
        //BUILDING 1
        List<Point> building1 = new ArrayList<>();
        building1.add(new Point(Utils.START_LONGITUDE + Utils.STEP_LONGITUDE * 4 , Utils.START_LATITUDE +  Utils.STEP_LATITUDE * 4, 0));
        building1.add(new Point(Utils.START_LONGITUDE + Utils.STEP_LONGITUDE * 8, Utils.START_LATITUDE +  Utils.STEP_LATITUDE * 4, 0));
        building1.add(new Point(Utils.START_LONGITUDE + Utils.STEP_LONGITUDE * 8, Utils.START_LATITUDE +  Utils.STEP_LATITUDE * 8, 0));
        building1.add(new Point(Utils.START_LONGITUDE + Utils.STEP_LONGITUDE * 4, Utils.START_LATITUDE +  Utils.STEP_LATITUDE * 8, 0));

        final PointCollection pointsBuilding1 = new PointCollection(Utils.SPATIAL_REFERENCE);
        pointsBuilding1.add(new Point(Utils.START_LONGITUDE + Utils.STEP_LONGITUDE * 4, Utils.START_LATITUDE +  Utils.STEP_LATITUDE * 4, 27));
        pointsBuilding1.add(new Point(Utils.START_LONGITUDE + Utils.STEP_LONGITUDE * 8, Utils.START_LATITUDE +  Utils.STEP_LATITUDE * 4, 27));
        pointsBuilding1.add(new Point(Utils.START_LONGITUDE + Utils.STEP_LONGITUDE * 8, Utils.START_LATITUDE +  Utils.STEP_LATITUDE * 8, 27));
        pointsBuilding1.add(new Point(Utils.START_LONGITUDE + Utils.STEP_LONGITUDE * 4, Utils.START_LATITUDE +  Utils.STEP_LATITUDE * 8, 27));
        graphicsOverlay.getGraphics().add(new Graphic(new Polygon(pointsBuilding1), new SimpleFillSymbol(SimpleFillSymbol.Style.SOLID, 0xFF04A2CA, Utils.lineSymbol)));
        for (int i = 0 ; i < building1.size() - 1; i++){
            final PointCollection points = new PointCollection(Utils.SPATIAL_REFERENCE);
            points.add(building1.get(i));
            points.add(building1.get(i+1));
            points.add(new Point(building1.get(i+1).getX(), building1.get(i+1).getY(), 27));
            points.add(new Point(building1.get(i).getX(), building1.get(i).getY(), 27));
            graphicsOverlay.getGraphics().add(new Graphic(new Polygon(points), new SimpleFillSymbol(SimpleFillSymbol.Style.SOLID, 0xFF04A2CA, Utils.lineSymbol)));
            if (i == building1.size() - 2){
                final PointCollection points2 = new PointCollection(Utils.SPATIAL_REFERENCE);
                points2.add(building1.get(i+1));
                points2.add(building1.get(0));
                points2.add(new Point(building1.get(0).getX(), building1.get(0).getY(), 27));
                points2.add(new Point(building1.get(i+1).getX(), building1.get(i+1).getY(), 27));
                graphicsOverlay.getGraphics().add(new Graphic(new Polygon(points2), new SimpleFillSymbol(SimpleFillSymbol.Style.SOLID, 0xFF04A2CA, Utils.lineSymbol)));
            }

        }

        //BUILDING 2
        List<Point> building2 = new ArrayList<>();
        building2.add(new Point(Utils.START_LONGITUDE + Utils.STEP_LONGITUDE * 4, Utils.START_LATITUDE +  Utils.STEP_LATITUDE * 15, 0));
        building2.add(new Point(Utils.START_LONGITUDE + Utils.STEP_LONGITUDE * 8, Utils.START_LATITUDE +  Utils.STEP_LATITUDE * 15, 0));
        building2.add(new Point(Utils.START_LONGITUDE + Utils.STEP_LONGITUDE * 8, Utils.START_LATITUDE +  Utils.STEP_LATITUDE * 18, 0));
        building2.add(new Point(Utils.START_LONGITUDE + Utils.STEP_LONGITUDE * 4, Utils.START_LATITUDE +  Utils.STEP_LATITUDE * 18, 0));

        final PointCollection pointsBuilding2 = new PointCollection(Utils.SPATIAL_REFERENCE);
        pointsBuilding2.add(new Point(Utils.START_LONGITUDE + Utils.STEP_LONGITUDE * 4, Utils.START_LATITUDE +  Utils.STEP_LATITUDE * 15, 37));
        pointsBuilding2.add(new Point(Utils.START_LONGITUDE + Utils.STEP_LONGITUDE * 8, Utils.START_LATITUDE +  Utils.STEP_LATITUDE * 15, 37));
        pointsBuilding2.add(new Point(Utils.START_LONGITUDE + Utils.STEP_LONGITUDE * 8, Utils.START_LATITUDE +  Utils.STEP_LATITUDE * 18, 37));
        pointsBuilding2.add(new Point(Utils.START_LONGITUDE + Utils.STEP_LONGITUDE * 4, Utils.START_LATITUDE +  Utils.STEP_LATITUDE * 18, 37));
        graphicsOverlay.getGraphics().add(new Graphic(new Polygon(pointsBuilding2), new SimpleFillSymbol(SimpleFillSymbol.Style.SOLID, 0xFF04A2CA, Utils.lineSymbol)));
        for (int i = 0 ; i < building2.size() - 1; i++){
            final PointCollection points = new PointCollection(Utils.SPATIAL_REFERENCE);
            points.add(building2.get(i));
            points.add(building2.get(i+1));
            points.add(new Point(building2.get(i+1).getX(), building2.get(i+1).getY(), 37));
            points.add(new Point(building2.get(i).getX(), building2.get(i).getY(), 37));
            graphicsOverlay.getGraphics().add(new Graphic(new Polygon(points), new SimpleFillSymbol(SimpleFillSymbol.Style.SOLID, 0xFF04A2CA, Utils.lineSymbol)));
            if (i == building2.size() - 2){
                final PointCollection points2 = new PointCollection(Utils.SPATIAL_REFERENCE);
                points2.add(building2.get(i+1));
                points2.add(building2.get(0));
                points2.add(new Point(building2.get(0).getX(), building2.get(0).getY(), 37));
                points2.add(new Point(building2.get(i+1).getX(), building2.get(i+1).getY(), 37));
                graphicsOverlay.getGraphics().add(new Graphic(new Polygon(points2), new SimpleFillSymbol(SimpleFillSymbol.Style.SOLID, 0xFF04A2CA, Utils.lineSymbol)));
            }

        }

       //BUILDING 3
        List<Point> building3 = new ArrayList<>();
        building3.add(new Point(Utils.START_LONGITUDE + Utils.STEP_LONGITUDE * 15, Utils.START_LATITUDE +  Utils.STEP_LATITUDE * 4, 0));
        building3.add(new Point(Utils.START_LONGITUDE + Utils.STEP_LONGITUDE * 18, Utils.START_LATITUDE +  Utils.STEP_LATITUDE * 4, 0));
        building3.add(new Point(Utils.START_LONGITUDE + Utils.STEP_LONGITUDE * 18, Utils.START_LATITUDE +  Utils.STEP_LATITUDE * 8, 0));
        building3.add(new Point(Utils.START_LONGITUDE + Utils.STEP_LONGITUDE * 15, Utils.START_LATITUDE +  Utils.STEP_LATITUDE * 8, 0));

        final PointCollection pointsBuilding3 = new PointCollection(Utils.SPATIAL_REFERENCE);
        pointsBuilding3.add(new Point(Utils.START_LONGITUDE + Utils.STEP_LONGITUDE * 15, Utils.START_LATITUDE +  Utils.STEP_LATITUDE * 4, 43));
        pointsBuilding3.add(new Point(Utils.START_LONGITUDE + Utils.STEP_LONGITUDE * 18, Utils.START_LATITUDE +  Utils.STEP_LATITUDE * 4, 43));
        pointsBuilding3.add(new Point(Utils.START_LONGITUDE + Utils.STEP_LONGITUDE * 18, Utils.START_LATITUDE +  Utils.STEP_LATITUDE * 8, 43));
        pointsBuilding3.add(new Point(Utils.START_LONGITUDE + Utils.STEP_LONGITUDE * 15, Utils.START_LATITUDE +  Utils.STEP_LATITUDE * 8, 43));
        graphicsOverlay.getGraphics().add(new Graphic(new Polygon(pointsBuilding3), new SimpleFillSymbol(SimpleFillSymbol.Style.SOLID, 0xFF04A2CA, Utils.lineSymbol)));
        for (int i = 0 ; i < building3.size() - 1; i++){
            final PointCollection points = new PointCollection(Utils.SPATIAL_REFERENCE);
            points.add(building3.get(i));
            points.add(building3.get(i+1));
            points.add(new Point(building3.get(i+1).getX(), building3.get(i+1).getY(), 43));
            points.add(new Point(building3.get(i).getX(), building3.get(i).getY(), 43));
            graphicsOverlay.getGraphics().add(new Graphic(new Polygon(points), new SimpleFillSymbol(SimpleFillSymbol.Style.SOLID, 0xFF04A2CA, Utils.lineSymbol)));
            if (i == building3.size() - 2){
                final PointCollection points2 = new PointCollection(Utils.SPATIAL_REFERENCE);
                points2.add(building3.get(i+1));
                points2.add(building3.get(0));
                points2.add(new Point(building3.get(0).getX(), building3.get(0).getY(), 43));
                points2.add(new Point(building3.get(i+1).getX(), building3.get(i+1).getY(), 43));
                graphicsOverlay.getGraphics().add(new Graphic(new Polygon(points2), new SimpleFillSymbol(SimpleFillSymbol.Style.SOLID, 0xFF04A2CA, Utils.lineSymbol)));
            }

        }

        //BUILDING 4
        List<Point> building4 = new ArrayList<>();
        building4.add(new Point(Utils.START_LONGITUDE + Utils.STEP_LONGITUDE * 15, Utils.START_LATITUDE +  Utils.STEP_LATITUDE * 15, 0));
        building4.add(new Point(Utils.START_LONGITUDE + Utils.STEP_LONGITUDE * 18, Utils.START_LATITUDE +  Utils.STEP_LATITUDE * 15, 0));
        building4.add(new Point(Utils.START_LONGITUDE + Utils.STEP_LONGITUDE * 18, Utils.START_LATITUDE +  Utils.STEP_LATITUDE * 18, 0));
        building4.add(new Point(Utils.START_LONGITUDE + Utils.STEP_LONGITUDE * 15, Utils.START_LATITUDE +  Utils.STEP_LATITUDE * 18, 0));

        final PointCollection pointsBuilding4 = new PointCollection(Utils.SPATIAL_REFERENCE);
        pointsBuilding4.add(new Point(Utils.START_LONGITUDE + Utils.STEP_LONGITUDE * 15, Utils.START_LATITUDE +  Utils.STEP_LATITUDE * 15, 22));
        pointsBuilding4.add(new Point(Utils.START_LONGITUDE + Utils.STEP_LONGITUDE * 18, Utils.START_LATITUDE +  Utils.STEP_LATITUDE * 15, 22));
        pointsBuilding4.add(new Point(Utils.START_LONGITUDE + Utils.STEP_LONGITUDE * 18, Utils.START_LATITUDE +  Utils.STEP_LATITUDE * 18, 22));
        pointsBuilding4.add(new Point(Utils.START_LONGITUDE + Utils.STEP_LONGITUDE * 15, Utils.START_LATITUDE +  Utils.STEP_LATITUDE * 18, 22));
        graphicsOverlay.getGraphics().add(new Graphic(new Polygon(pointsBuilding4), new SimpleFillSymbol(SimpleFillSymbol.Style.SOLID, 0xFF04A2CA, Utils.lineSymbol)));
        for (int i = 0 ; i < building4.size() - 1; i++){
            final PointCollection points = new PointCollection(Utils.SPATIAL_REFERENCE);
            points.add(building4.get(i));
            points.add(building4.get(i+1));
            points.add(new Point(building4.get(i+1).getX(), building4.get(i+1).getY(), 22));
            points.add(new Point(building4.get(i).getX(), building4.get(i).getY(), 22));
            graphicsOverlay.getGraphics().add(new Graphic(new Polygon(points), new SimpleFillSymbol(SimpleFillSymbol.Style.SOLID, 0xFF04A2CA, Utils.lineSymbol)));
            if (i == building4.size() - 2){
                final PointCollection points2 = new PointCollection(Utils.SPATIAL_REFERENCE);
                points2.add(building4.get(i+1));
                points2.add(building4.get(0));
                points2.add(new Point(building4.get(0).getX(), building4.get(0).getY(), 22));
                points2.add(new Point(building4.get(i+1).getX(), building4.get(i+1).getY(), 22));
                graphicsOverlay.getGraphics().add(new Graphic(new Polygon(points2), new SimpleFillSymbol(SimpleFillSymbol.Style.SOLID, 0xFF04A2CA, Utils.lineSymbol)));
            }

        }
    }


    public static void animateDrones(List<Drone> listDrones, Map<Integer, Node> idToNode, GraphicsOverlay graphicsOverlay) {
        final SimpleRenderer renderer3D = new SimpleRenderer();
        final Renderer.SceneProperties renderProperties = renderer3D.getSceneProperties();
        renderProperties.setHeadingExpression("[HEADING]");
        renderProperties.setPitchExpression("[PITCH]");
        renderProperties.setRollExpression("[ROLL]");
        graphicsOverlay.setRenderer(renderer3D);
        final List<List<Map<String, Point>>> missionsData = listDrones.stream().map(drone -> getMissionData(drone, idToNode)).filter(data -> data.size() > 0).collect(Collectors.toList());
        final List<AnimationModel> animationModels = IntStream.range(0, missionsData.size()).mapToObj(x -> new AnimationModel(0)).collect(Collectors.toList());

        final List<Timeline> animations = IntStream.range(0, missionsData.size()).mapToObj(x -> new Timeline()).collect(Collectors.toList());
        Utils.PLANE_3D_SYMBOL.loadAsync();
        final List<Graphic> planeSymbols = IntStream.range(0, missionsData.size()).mapToObj(x -> new Graphic(new Point(26.063180910236255, 44.56294582848301, 30.0, Utils.SPATIAL_REFERENCE), Utils.PLANE_3D_SYMBOL)).collect(Collectors.toList());
        graphicsOverlay.getGraphics().addAll(planeSymbols);
        //Set animationModels propertis
        IntStream.range(0, missionsData.size()).forEach(index -> {

                animationModels.get(index).setFrames(missionsData.get(index).size());
                animationModels.get(index).setKeyframe(0);


        });
        IntStream.range(0, missionsData.size()).forEach(index -> {
            animations.get(index).setAutoReverse(true);
            animations.get(index).setCycleCount(Timeline.INDEFINITE);
            animations.get(index).getKeyFrames().add(new KeyFrame(Duration.millis(Utils.SPEED_TO_MILISEC_FACTOR / listDrones.get(index).getSpeed()), e -> animate(animationModels.get(index).nextKeyframe(), missionsData.get(index), planeSymbols.get(index), listDrones.get(index).getColor(), graphicsOverlay, listDrones.get(index).getUid())));
        });

        seqT.getChildren().addAll(animations);
        seqT.play();

    }

    public static void setStageProperties(Stage stage) {
        stage.setTitle("U-Space");
        stage.setWidth(800);
        stage.setHeight(700);
    }

    private static List<Map<String, Point>> getMissionData(Drone drone, Map<Integer, Node> idToNode) {
        return drone.getOrderOfNodes().stream()
                .map(nodeID -> {
                    // create a map of parameters (ordinates) to values
                    final Node node = idToNode.get(nodeID);
                    Map<String, Point> ordinates = new HashMap<>();
                    ordinates.put("POSITION", new Point(node.getLng(), node.getLat(), node.getAlt(),
                            Utils.SPATIAL_REFERENCE));
                    return ordinates;
                })
                .collect(Collectors.toList());


    }

    private static void animate(int keyframe, List<Map<String, Point>> missionData, Graphic plane3D, int color, GraphicsOverlay graphicsOverlay, int droneUID) {

        PointCollection points = new PointCollection(Utils.SPATIAL_REFERENCE);
        SimpleLineSymbol lineSymbol = new SimpleLineSymbol(SimpleLineSymbol.Style.SOLID, color, 5);
        Map<String, Point> datum = missionData.get(keyframe);
        Point position = datum.get("POSITION");
        dronePosition.put(droneUID, position);
        if (keyframe > 0) {
            Point previousPosition = missionData.get(keyframe - 1).get("POSITION");
            points.add(previousPosition);
            points.add(position);

            Polyline polyline = new Polyline(points);

            Graphic graphic = new Graphic(polyline, lineSymbol);
            graphicsOverlay.getGraphics().add(graphic);
        }

        plane3D.setGeometry(position);
    }

    public static List<Drone> getListDroneTest() {
        return listDroneTest;
    }
}
