package com.tq.staybooking.exception;

/**
 * 1. Go to com.tq.staybooking.exception package and create StayDeleteException.
 * 2. Go to the StayService class and update the delete() method.
 */
public class StayDeleteException extends RuntimeException{
    public StayDeleteException(String message){
        super(message);
    }
}
