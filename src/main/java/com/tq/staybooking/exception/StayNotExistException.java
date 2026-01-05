package com.tq.staybooking.exception;

/**
 * 1. Go to the com.tq.staybooking.exception package and create a new exception class StayNotExistException.
 * 2. Go to the com.tq.staybooking.service package and create a new class StayService.
 */
public class StayNotExistException extends RuntimeException{
    public StayNotExistException(String message){
        super(message);
    }
}
