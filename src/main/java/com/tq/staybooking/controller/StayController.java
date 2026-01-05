package com.tq.staybooking.controller;

import com.tq.staybooking.model.Reservation;
import com.tq.staybooking.model.Stay;
import com.tq.staybooking.model.User;
import com.tq.staybooking.service.ReservationService;
import com.tq.staybooking.service.StayService;
//import jakarta.servlet.ServletException;
//import jakarta.servlet.http.HttpServletRequest;
//import jakarta.servlet.http.Part;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.Part;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

/**
 * 1. Add the StayService as a private field and create the constructor.
 * 2. Implement the methods for all stay management APIs.
 * 3. Go to the CustomExceptionHandler class and add the function to handle the StayNotExistException.
 * 4. Go to StayController and add a new API to support list reservation by stay function.
 * 5. Go to CustomExceptionHandler class to include the exceptions you’ve added today.
     *  ReservationCollisionException
     *  InvalidReservationDateException
     *  ReservationNotFoundException
     *  StayDeleteException
 */
@RestController
public class StayController {
    private StayService stayService;

    private ReservationService reservationService;

    @Autowired
    public StayController(StayService stayService,  ReservationService reservationService) {
        this.stayService = stayService;
        this.reservationService = reservationService;
    }

//    @GetMapping(value ="/stays")
//    public List<Stay> listStays(@RequestParam(name = "host") String hostName) {
//        return stayService.listByUser(hostName);
//    }
//
//    @GetMapping(value = "/stays/id")
//    public Stay getStay(@RequestParam(name = "stay_id") Long stayId,
//                        @RequestParam(name = "host") String hostName){
//        return stayService.findByIdAndHost(stayId, hostName);
//    }
//
//    @PostMapping("/stays")
//    public void addStay(@RequestBody Stay stay){
//        stayService.add(stay);
//    }
//
//    @DeleteMapping("/stays")
//    public void deleteStay(@RequestParam(name = "stay_id") Long stayId,
//                           @RequestParam(name = "host") String hostName){
//        stayService.delete(stayId, hostName);
//    }

    //这个 Authentication 不是你创建的，
    //是 Spring Security 在请求到达 Controller 之前，
    //从 SecurityContext 里“自动拿出来塞给你的”。

    @GetMapping("/stays")
//    @PreAuthorize("hasRole('HOST')")
    public List<Stay> listStays(Authentication authentication) {
        String hostName = authentication.getName();
        return stayService.listByUser(hostName);
    }
    // 教案 用的Principle 不是 Authentication
//    @GetMapping(value = "/stays")
//    public List<Stay> listStays(Principal principal) {
//        return stayService.listByUser(principal.getName());
//    }

    @GetMapping("/stays/{id}")
    public Stay getStay(@PathVariable("id") Long stayId,
                        Authentication authentication){
        String hostName = authentication.getName();
        return stayService.findByIdAndHost(stayId, hostName);
    }

    @PostMapping("/stays")
    public void addStay(@RequestBody Stay stay,
                        Authentication authentication){
        String hostName = authentication.getName();
        stay.setHost(new User.Builder().setUsername(hostName).build());
        stayService.add(stay);
    }

    @DeleteMapping("/stays/{id}")
    public void deleteStay(@PathVariable("id") Long stayId,
                           Authentication authentication){
        String hostName = authentication.getName();
        stayService.delete(stayId, hostName);
    }

//    @PostMapping("/stays")
//    public void addStay(
//            @RequestParam("name") String name,
//            @RequestParam("address") String address,
//            @RequestParam("description") String description,
//            @RequestParam("host") String host,
//            @RequestParam("guest_number") int guestNumber,
//            @RequestParam("images") MultipartFile[] images) {
//
//        Stay stay = new Stay.Builder().setName(name)
//                .setAddress(address)
//                .setDescription(description)
//                .setGuestNumber(guestNumber)
//                .setHost(new User.Builder().setUsername(host).build())
//                .build();
//        stayService.add(stay, images);

//    @PostMapping(value="/stays",consumes="multipart/form-data")
    @PostMapping(value="/stays", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public void addStay(
            @RequestParam("name") String name,
            @RequestParam("address") String address,
            @RequestParam("description") String description,
            @RequestParam("guest_number") int guestNumber,
            @RequestPart("images") MultipartFile[] images,
            Authentication authentication, HttpServletRequest request) throws IOException, ServletException{
        String hostName = authentication.getName();
        //==============debug=========================================
        // 方法中 HttpServletRequest request是不用加的参数 但是为了debug用
        System.out.println(">>> Content-Type = " + request.getContentType());
        for (Part p : request.getParts()) {
            System.out.println(">>> part name=" + p.getName() + ", size=" + p.getSize());
        }

        Stay stay = new Stay.Builder()
                .setName(name)
                .setAddress(address)
                .setDescription(description)
                .setGuestNumber(guestNumber)
                .setHost(new User.Builder().setUsername(hostName).build())
                .build();
        stayService.add(stay, images);

    }

//@PostMapping(value="/stays", consumes="multipart/form-data")
//public void addStay(
//        @RequestParam("name") String name,
//        @RequestParam("address") String address,
//        @RequestParam("description") String description,
//        @RequestParam("guest_number") int guestNumber,
//        @RequestParam("images") MultipartFile[] images,
//        @RequestParam("host") String hostName){
//
//    Stay stay = new Stay.Builder()
//            .setName(name)
//            .setAddress(address)
//            .setDescription(description)
//            .setGuestNumber(guestNumber)
//            .setHost(new User.Builder().setUsername(hostName).build())
//            .build();
//    stayService.add(stay, images);
//
//}
    @GetMapping(value = "/stays/reservations/{stayId}")
    public List<Reservation> listReservation(@PathVariable Long stayId){
        return reservationService.listByStay(stayId);
    }

}
