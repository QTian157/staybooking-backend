package com.tq.staybooking.exception;

/**
 * 1. Go to the com.tq.staybooking.exception package,
     * create GeoCodingException which will be thrown if there’s any exception when we connect to Geolocation API.
 * 2. Under the same package, create InvalidStayAddressException which will be thrown when the given stay address is invalid.
 */
public class GeoCodingException extends RuntimeException{
    public GeoCodingException(String message){
        super(message);
    }
}
