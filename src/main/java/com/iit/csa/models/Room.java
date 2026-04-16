/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.iit.csa.models;

/**
 *
 * @author deegh
 */
public class Room {
    private int id;
    private String name;
    private String floor;
    
    public Room(int id, String name, String floor) {
        this.id = id;
        this.name = name;
        this.floor = floor;
    }    
    //
    
    
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getFloor() { return floor; }
    public void setFloor(String floor) { this.floor = floor; }
    
}
