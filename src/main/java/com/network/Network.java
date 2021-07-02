package com.network;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

public class Network {

    // TODO: Change this to a constructor approach
    /*private static final double START_LATITUDE = 44.43281066076443;
    private static final double END_LATITUDE = 44.448729900512445;
    private static final double START_LONGITUDE = 26.076613939511805;
    private static final double END_LONGITUDE = 26.095692746067976;
    private static final double START_HEIGHT = 30;
    private static final double END_HEIGHT = 90;
    private static final double STEP_LATITUDE = 0.0002;
    private static final double STEP_LONGITUDE = 0.0002;
    private static final double STEP_HEIGHT = 15;*/

    private static final double START_LATITUDE = 44.42930868371913;
    private static final double END_LATITUDE = 44.43396992959724;
    private static final double START_LONGITUDE = 26.116295435985838;
    private static final double END_LONGITUDE = 26.12269561523621;
    private static final double START_HEIGHT = 5;
    private static final double END_HEIGHT = 80;
    private static final double STEP_LATITUDE = 0.0001;
    private static final double STEP_LONGITUDE = 0.0001;
    private static final double STEP_HEIGHT = 5;
    final static Path POINTS_PATH = Paths.get("src/main/java/com/data/Points.txt");
    final static Path NEIGHBOURS_PATH = Paths.get("src/main/java/com/data/Neighbours.txt");


    /*
     * Return a set of coordinates for each id of each node in the following form:
     *
     * id=[lat, lng, height]
     *
     * ex : 8=[44.56294582848301, 26.063180910236255, 220.0]
     *
     * */
    private static Map<Integer, List<Double>> get3DMatrix() {
        Map<Integer, List<Double>> coordinates = new HashMap<>();
        int id = 0;

        for (double i = START_HEIGHT; i < END_HEIGHT; i += STEP_HEIGHT) {
            for (double j = START_LONGITUDE; j < END_LONGITUDE / 2 + START_LONGITUDE / 2; j += STEP_LONGITUDE) {
                for (double k = START_LATITUDE; k < END_LATITUDE / 2 + START_LATITUDE / 2; k += STEP_LATITUDE) {
                    if (!((0 < i && i < 30 && START_LATITUDE + STEP_LATITUDE * 3 < k && k < START_LATITUDE + STEP_LATITUDE * 9 && START_LONGITUDE + STEP_LONGITUDE * 3 < j && j < START_LONGITUDE + STEP_LONGITUDE * 9)
                            || (0 < i && i < 40 && START_LATITUDE + STEP_LATITUDE * 14 < k && k < START_LATITUDE + STEP_LATITUDE * 19 && START_LONGITUDE + STEP_LONGITUDE * 3 < j && j < START_LONGITUDE + STEP_LONGITUDE * 9)
                            || (0 < i && i < 45 && START_LATITUDE + STEP_LATITUDE * 3 < k && k < START_LATITUDE + STEP_LATITUDE * 9 && START_LONGITUDE + STEP_LONGITUDE * 14 < j && j < START_LONGITUDE + STEP_LONGITUDE * 19)
                            || (0 < i && i < 25 && START_LATITUDE + STEP_LATITUDE * 14 < k && k < START_LATITUDE + STEP_LATITUDE * 19 && START_LONGITUDE + STEP_LONGITUDE * 14 < j && j < START_LONGITUDE + STEP_LONGITUDE * 19)
                            || (35 < i && i < 75 && START_LONGITUDE + STEP_LONGITUDE * 23 < j && j < START_LONGITUDE + STEP_LONGITUDE * 28))) {
                        coordinates.put(id, Arrays.asList(k, j, i));
                        id++;
                    }
                }
            }
        }

        return coordinates;
    }


    /*
     *
     * Take the correspondence between id and [lat, lng, height] and constructs the neighbours for each id
     * in the form:
     *   id=[List of neighbour's ids]
     *
     * ex:
     *   Input: 8=[44.56294582848301, 26.063180910236255, 220.0]
     *          9=[44.56294582848301, 26.063180910236255, 210.0]
     *          10=[44.56294582848301, 26.063180910236255, 230.0]
     *   Output: 8=[9, 10]
     *           9=[8]
     *           10=[9]
     *
     * */
    private static void writeNeighbours() throws IOException {
        final Map<Integer, List<Double>> coordinates = get3DMatrix();
        Map<Integer, List<Integer>> neighbours = coordinates.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, set ->

                {
                    if (set.getValue().get(2) >= START_HEIGHT + STEP_HEIGHT * 4) {
                        return coordinates.entrySet().stream().filter(nextSet ->
                                Math.abs(set.getValue().get(2) - nextSet.getValue().get(2)) <= STEP_HEIGHT &&
                                        Math.abs(set.getValue().get(1) - nextSet.getValue().get(1)) <= STEP_LONGITUDE + STEP_LONGITUDE / 10 &&
                                        Math.abs(set.getValue().get(0) - nextSet.getValue().get(0)) <= STEP_LATITUDE + STEP_LATITUDE / 10 &&
                                        !set.getKey().equals(nextSet.getKey())).map(Map.Entry::getKey)
                                .collect(Collectors.toList());
                    } else {
                        return coordinates.entrySet().stream().filter(nextSet ->
                                Math.abs(set.getValue().get(2) - nextSet.getValue().get(2)) == STEP_HEIGHT &&
                                        Math.abs(set.getValue().get(1) - nextSet.getValue().get(1)) <= STEP_LONGITUDE + STEP_LONGITUDE / 10 &&
                                        Math.abs(set.getValue().get(0) - nextSet.getValue().get(0)) <= STEP_LATITUDE + STEP_LATITUDE / 10 &&
                                        !set.getKey().equals(nextSet.getKey())).map(Map.Entry::getKey)
                                .collect(Collectors.toList());
                    }

                })
        );

        String s = neighbours.entrySet().stream().map(String::valueOf).collect(Collectors.joining("\n"));
        Files.write(NEIGHBOURS_PATH, s.getBytes(StandardCharsets.UTF_8));


    }


    /*
     * Writes a set of coordinates for each id of each node in the following form:
     *
     * id=[lat, lng, height]
     *
     * ex : 8=[44.56294582848301, 26.063180910236255, 220.0]
     */
    private static void write3DMatrix() throws IOException {
        final String s = get3DMatrix().entrySet().stream().map(String::valueOf).collect(Collectors.joining("\n"));
        Files.write(POINTS_PATH, s.getBytes(StandardCharsets.UTF_8));
    }

    public static void constructDataBase() throws IOException {
        write3DMatrix();
        writeNeighbours();
    }


}
