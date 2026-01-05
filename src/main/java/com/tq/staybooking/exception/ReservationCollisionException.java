package com.tq.staybooking.exception;

/**
 *
 * 1.Go to com.tq.staybooking.exception package and create ReservationCollisionException.
 * 2. Under the same package, create another exception class ReservationNotFoundException.
 */

public class ReservationCollisionException extends RuntimeException{
    public ReservationCollisionException(String message){
        super(message);
    }
}
