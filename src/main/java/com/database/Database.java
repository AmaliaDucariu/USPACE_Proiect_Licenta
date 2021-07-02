package com.database;

import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Database {
    public static List<List<Long>> getAllSerialNumbersAndDroneIDs() {
        final List<Long> allSerialNumbers = new ArrayList<>();
        final List<Long> allDroneIDs = new ArrayList<>();
        Statement statement = null;
        ResultSet resultSet1 = null;
        ResultSet resultSet2 = null;

        try {
            Class.forName("com.mysql.cj.jdbc.Driver").getDeclaredConstructor().newInstance();
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        try {
            Connection conn =
                    DriverManager.getConnection("jdbc:mysql://localhost/DroneOperators?" +
                            "user=<username>&password=<password>");

            statement = conn.createStatement();
            resultSet1 = statement.executeQuery("SELECT Serial_Number FROM Operators");
            while(resultSet1.next()) {
                allSerialNumbers.add(resultSet1.getLong(1));
            }

            resultSet2 = statement.executeQuery("SELECT UID FROM Drones");
            while(resultSet2.next()) {
                allDroneIDs.add(resultSet2.getLong(1));
            }


        } catch (SQLException ex) {

            System.out.println("SQLException: " + ex.getMessage());
            System.out.println("SQLState: " + ex.getSQLState());
            System.out.println("VendorError: " + ex.getErrorCode());
        } finally {

            if (resultSet1 != null) {
                try {
                    resultSet1.close();
                } catch (SQLException sqlEx) {
                    sqlEx.printStackTrace();
                }

            }
            if (resultSet2 != null) {
                try {
                    resultSet2.close();
                } catch (SQLException sqlEx) {
                    sqlEx.printStackTrace();
                }

            }
            if (statement != null) {
                try {
                    statement.close();
                } catch (SQLException sqlEx) {
                    sqlEx.printStackTrace();
                }
            }
        }
        return Arrays.asList(allSerialNumbers,allDroneIDs);
    }
}
