package com.tq.staybooking.exception;

/**
 * 1. After add GCS bucket in application.properties file
 * 2. Go to the com.tq.staybooking.exception package, create a new custom exception called GCSUploadException.
 * 3. Go to the com.tq.staybooking.config package and create a class to provide GCS connections.
 */
public class GCSUploadException extends RuntimeException{
    public GCSUploadException(String message){
        super(message);
    }
}

