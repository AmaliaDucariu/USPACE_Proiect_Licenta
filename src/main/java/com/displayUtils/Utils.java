package com.displayUtils;

import com.app.Node;
import com.data.NodeStructures.NodeStaticMethods;
import com.esri.arcgisruntime.geometry.Point;
import com.esri.arcgisruntime.geometry.SpatialReference;
import com.esri.arcgisruntime.geometry.SpatialReferences;
import com.esri.arcgisruntime.mapping.view.Camera;
import com.esri.arcgisruntime.symbology.ModelSceneSymbol;
import com.esri.arcgisruntime.symbology.SimpleFillSymbol;
import com.esri.arcgisruntime.symbology.SimpleLineSymbol;
import com.esri.arcgisruntime.symbology.SimpleMarkerSymbol;


import java.io.File;
import java.io.IOException;
import java.util.Map;

public class Utils {
    public static final SpatialReference SPATIAL_REFERENCE = SpatialReferences.getWgs84();
    public static final ModelSceneSymbol PLANE_3D_SYMBOL = new ModelSceneSymbol(new File(System.getProperty("data.dir"), "./airplane/bristol/Collada/Bristol.dae").getAbsolutePath(), 0.5);
    public static final String ELEVATION_SERVICE_URL = "https://elevation3d.arcgis.com/arcgis/rest/services/WorldElevation3D/Terrain3D/ImageServer";
    public static final double SPEED_TO_MILISEC_FACTOR = 5.0 * 250;

    public Utils() throws IOException {
    }

    public static SimpleMarkerSymbol markerSymbol(int colorMarker, int colorOutline) {
        SimpleMarkerSymbol simpleMarkerSymbol =
                new SimpleMarkerSymbol(SimpleMarkerSymbol.Style.CIRCLE, colorMarker, 2);
        simpleMarkerSymbol.setOutline(new SimpleLineSymbol(SimpleLineSymbol.Style.SOLID, colorOutline, 1));
        return simpleMarkerSymbol;
    }
    public static final double START_LATITUDE = 44.42930868371913;
    public static final double END_LATITUDE = 44.43396992959724;
    public static final double START_LONGITUDE = 26.116295435985838;
    public static final double END_LONGITUDE = 26.12269561523621;
    public static final double STEP_LATITUDE = 0.0001;
    public static final double STEP_LONGITUDE = 0.0001;
    public static final SimpleLineSymbol lineSymbol = new SimpleLineSymbol(SimpleLineSymbol.Style.SOLID, 0xFFBAE1FF, 1);
    public static final SimpleFillSymbol POLYGON_FILL_SYMBOL =
            new SimpleFillSymbol(SimpleFillSymbol.Style.SOLID, 0xFF000080, lineSymbol);

    public static final Camera POINT_OF_VIEW = new Camera(new Point(26.116121018117052, 44.42885795921079,  300, SPATIAL_REFERENCE), 30, 72.0, 0.0);


    public static Map<Integer, Node> idToNode;

    static {
        try {
            idToNode = NodeStaticMethods.getMapIdToNode();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static final Map<Integer, Map<Integer, Double>> idToNeighbourDistance = NodeStaticMethods.getMapOfDistances(idToNode);
}
