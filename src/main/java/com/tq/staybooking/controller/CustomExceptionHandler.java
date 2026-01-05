package com.tq.staybooking.controller;

import com.tq.staybooking.exception.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

/**
 * As you may wonder,
 * how does the controller handle the UserAlreadyExist exception here?
 * We can definitely handle that inside of addGuest and addHost.
 * But a cleaner way is to have a dedicated exception handler to handle all kinds of exceptions for every controller.
 * 1. Now let’s create a CustomExceptionHandler class in the com.tq.staybooking.controller package.
 * 2. Go to CustomExceptionHandler and add a new exception handler for UserNotExistException.
 *    Update SecurityConfig to allow /authentcation/guest and /authenticate/host URLs.
 */

/**
 * 统一异常处理（global exception handling
 * 一个类处理所有 controller 的异常 → @ControllerAdvice + @ExceptionHandler
 * service抛出异常，controller不try catch 而是找@ControllerAdvice 统一处理
 *
 * 1. Go to the CustomExceptionHandler class and add the function to handle the StayNotExistException.
 * 2. Go to the SecurityConfig class to allow stay management APIs without authentication.
     * We should require users to log in before using stay management APIs,
     * but let’s postpone the implementation to the next lesson.
 * 3. Update the CustomExceptionHandler to handle InvalidSearchDateException.
 * 4. Go to com.tq.staybooking.controller package and create the SearchController class.
 * 5. Go to CustomExceptionHandler class to include GeoCodingException and InvalidStayAddressException.
 * 6. Update the SecrityConfig to include the search service.
 * 7. Go to CustomExceptionHandler class to include the exceptions you’ve added today.
     * ReservationCollisionException
     * InvalidReservationDateException
     * ReservationNotFoundException
     * StayDeleteException
     * Go to SecurityConfig class and add reservation URLs to the security config.
 *
 */
@ControllerAdvice
public class CustomExceptionHandler{

    @ExceptionHandler(UserAlreadyExistException.class)
    public final ResponseEntity<String> handlerUserAlreadyExistExceptions(Exception ex, WebRequest request){
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.CONFLICT);
    }

    @ExceptionHandler(UserNotExistException.class)
    public final ResponseEntity<String> handleUserNotExistExceptions(Exception ex, WebRequest request){
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(StayNotExistException.class)
    public final ResponseEntity<String> handleStayNotExistExceptions(Exception ex, WebRequest request){
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(GCSUploadException.class)
    public final ResponseEntity<String> handleGCSUploadExceptions(Exception ex, WebRequest request){
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(InvalidSearchDateException.class)
    public final ResponseEntity<String> handleInvalidSearchDateException(Exception ex, WebRequest request){
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(GeoCodingException.class)
    public final ResponseEntity<String> handleGeoCodingException(Exception ex, WebRequest request){
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(InvalidStayAddressException.class)
    public final ResponseEntity<String> handleInvalidStayAddressException(Exception ex, WebRequest resuest){
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ReservationCollisionException.class)
    public final ResponseEntity<String> handleReservationCollisionException(Exception ex, WebRequest request){
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.CONFLICT);
    }

    @ExceptionHandler(InvalidReservationDateException.class)
    public final ResponseEntity<String> handleInvalidReservationDateExceptions(Exception ex, WebRequest request) {
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ReservationNotFoundException.class)
    public final ResponseEntity<String> handleReservationNotFoundExceptions(Exception ex, WebRequest request) {
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(StayDeleteException.class)
    public final ResponseEntity<String> handleStayDeleteExceptions(Exception ex, WebRequest request) {
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.CONFLICT);
    }

}


// A few explanation for the annotation in the exception handler:
    // -> @ControllerAdvice to make Spring use CustomExceptionHandler when there’s any exceptions in the Controller code.
    // -> @ExceptionHandler to match each exception to the corresponding handler function.

// ResponseEntity = 一个 HTTP 盒子（信封）
// 它可以装：
    // -> 任何类型的内容（String、对象、列表、空）
    // -> HTTP 状态码（如 200, 400, 409）
    // -> headers（可选）