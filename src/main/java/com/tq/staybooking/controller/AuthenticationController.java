package com.tq.staybooking.controller;

import com.tq.staybooking.model.Token;
import com.tq.staybooking.model.User;
import com.tq.staybooking.model.UserRole;
import com.tq.staybooking.service.AuthenticationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * 1. Go to com.laioffer.staybooking.controller package and create a new class AuthenticationController.
 * 2. Add AuthenticationService as a private field and implement two separate authentication APIs for host and guest.
 */
@RestController
public class AuthenticationController {
    private AuthenticationService authenticationService;

    @Autowired
    public AuthenticationController(AuthenticationService authenticationService){
        this.authenticationService = authenticationService;
    }

    @PostMapping("/authenticate/guest")
    public Token authenticateGuest(@RequestBody User user){
        return authenticationService.authenticate(user, UserRole.ROLE_GUEST);
    }

    @PostMapping("/authenticate/host")
    public Token authenticateHost(@RequestBody User user){
        return authenticationService.authenticate(user, UserRole.ROLE_HOST);
    }
}
