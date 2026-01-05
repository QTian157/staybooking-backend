package com.tq.staybooking.exception;

/**
 * 1. Go to com.tq.staybooking.exception package and create a new exception InvalidSearchDateException.
 * 2. Update the CustomExceptionHandler to handle InvalidSearchDateException.
 */
public class InvalidSearchDateException extends RuntimeException{
    public InvalidSearchDateException(String message) {
        super(message);
    }
}
