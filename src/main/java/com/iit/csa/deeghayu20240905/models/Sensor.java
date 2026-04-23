/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.iit.csa.deeghayu20240905.models;

/**
 *
 * @author deegh
 */
public class Sensor {
    private String id;
    private String roomId;
    private String status;
    private String type; 
    private double currentValue;

    public Sensor() {} //for JSON
    public Sensor(String id, String type, String status, double currentValue, String roomId){ 
        this.id = id; 
        this.roomId = roomId; 
        this.currentValue = currentValue;
        this.status = status;
        this.type = type; }

    
    
    
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public String getRoomId() { return roomId; }
    public void setRoomId(String roomId) { this.roomId = roomId; }
    
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public double getCurrentValue() { return currentValue; }
    public void setCurrentValue(double currentValue) { this.currentValue = currentValue; }
    
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
}
