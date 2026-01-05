package com.tq.staybooking.exception;

/**
 * Similar to the RegisterService, we should throw a UserNotExist exception when the given user credential is invalid.
 * So go to the com.laioffer.staybooking.exception package and create a UserNotExistException class.
 *
 * Go back to the AuthenticationService and
 * add the authenticate method to check the user credential and return the Token if everything is OK.
 */
public class UserNotExistException extends RuntimeException{
    public UserNotExistException(String message){
        super(message);
    }
}
