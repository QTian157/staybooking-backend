package com.tq.staybooking.controller;

import com.tq.staybooking.model.User;
import com.tq.staybooking.model.UserRole;
import com.tq.staybooking.service.RegisterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * 1. Go to the com.tq.staybooking.controller package and create the RegisterController class.
 * 2. Add the RegisterService as a private field and a constructor for initialization.
 * 3. Add the two RESTful API we will support on the backend for registration: addHost and addGuest.
 * 4. Go to CustomExceptionHandler and add a new exception handler for UserNotExistException.
 */

@RestController
public class RegisterController {
    private RegisterService registerService;

    @Autowired
    public RegisterController(RegisterService registerService){
        this.registerService = registerService;
    }
    // There are a few annotations to support a RESTful API:
        // -> @PostMapping(“/register/guest”) annotation to indicate the API supports POST method and maps to the /register/guest path.
        // -> @RequestBody annotation to help convert the request body from JSON format to a Java object.
    @PostMapping("/register/guest")
    public void addGuest(@RequestBody User user){
        registerService.add(user, UserRole.ROLE_GUEST);
    }

    @PostMapping("/register/host")
    public void addHost(@RequestBody User user){
        registerService.add(user, UserRole.ROLE_HOST);
    }

}
