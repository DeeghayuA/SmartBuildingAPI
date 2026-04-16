/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.iit.csa.models;

/**
 *
 * @author deegh
 */
public class Sensor {
    private int id;
    private int roomId;
    private String name;
    private String type; 

    public Sensor() {} //for JSON
    public Sensor(int id, int roomId, String name, String type) { this.id = id; this.roomId = roomId; this.name = name; this.type = type; }

    
    
    
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    
    public int getRoomId() { return roomId; }
    public void setRoomId(int roomId) { this.roomId = roomId; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
}
