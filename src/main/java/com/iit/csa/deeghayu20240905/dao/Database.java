/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.iit.csa.deeghayu20240905.dao;

/**
 *
 * @author deegh
 */
import com.iit.csa.deeghayu20240905.models.*;
import java.util.*;
public class Database {
    
    public static Map<String, Room> rooms = new HashMap<>();
    public static Map<String, Sensor> sensors = new HashMap<>();
    
    
    public static Map<String, List<SensorReading>> sensorReadings = new HashMap<>();// Maps a Sensor String ID to a List of its SensorReadings

    
    static {
        // Create Room
        String roomId = "LIB-301";
        Room room = new Room(roomId, "Library Quiet Study", 50);
        rooms.put(roomId, room);

        // Create Sensor
        String sensorId = "TEMP-001";
        Sensor sensor = new Sensor(sensorId, "Temperature", "ACTIVE", 22.5, roomId);
        sensors.put(sensorId, sensor);
        
        // Link the sensor to the room's list
        room.getSensorIds().add(sensorId);

        // Initialize empty readings list for this sensor
        sensorReadings.put(sensorId, new ArrayList<>());

        // Add a sample reading
        String readingId = UUID.randomUUID().toString();
        long currentEpochTime = System.currentTimeMillis();
        sensorReadings.get(sensorId).add(new SensorReading(readingId, currentEpochTime, 22.5));
    }
}
