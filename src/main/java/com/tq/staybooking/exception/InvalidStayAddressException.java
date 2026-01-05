package com.tq.staybooking.exception;

/**
 * 1. Under the same package, create InvalidStayAddressException which will be thrown when the given stay address is invalid.
 * 2. Go to the com.tq.staybooking.service package, create a new class called GeoCodingService.
 */
public class InvalidStayAddressException extends RuntimeException{
    public InvalidStayAddressException(String message){
        super(message);
    }
}
