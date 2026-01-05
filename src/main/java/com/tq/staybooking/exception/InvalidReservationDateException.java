package com.tq.staybooking.exception;

/**
 * 1. Go to com.tq.staybooking.controller package and create a new ReservationController class.
 */
public class InvalidReservationDateException extends RuntimeException{
    public InvalidReservationDateException(String message){
        super(message);
    }
}
