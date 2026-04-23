/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.iit.csa.deeghayu20240905.exceptions;

/**
 *
 * @author deegh
 */
public class RoomNotEmptyException extends RuntimeException{
    public RoomNotEmptyException(String message) {
        super(message);
    }
}
