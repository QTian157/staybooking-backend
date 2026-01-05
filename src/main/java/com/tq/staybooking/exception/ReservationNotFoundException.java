package com.tq.staybooking.exception;

/**
 * 1. Go to com.tq.staybooking.service package and create ReservationService.
 */
public class ReservationNotFoundException extends RuntimeException{
    public ReservationNotFoundException(String message){
        super(message);
    }
}
