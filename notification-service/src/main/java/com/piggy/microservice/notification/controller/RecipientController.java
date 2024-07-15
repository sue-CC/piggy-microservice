//package com.piggy.microservice.notification.controller;
//
//import com.piggy.microservice.notification.domain.Recipient;
//import com.piggy.microservice.notification.service.RecipientService;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.web.bind.annotation.*;
//
//import javax.validation.Valid;
//
//@RestController
//@RequestMapping("/recipients")
//public class RecipientController {
//
//    @Autowired
//    private RecipientService recipientService;
//
//    @RequestMapping(path = "/{name}" , method = RequestMethod.GET)
//    public Object getNotificationSetting(@PathVariable("name") String name){
//        return recipientService.findByAccountName(name);
//    }
//
//    @RequestMapping(path = "/{name}", method = RequestMethod.PUT)
//    public Object updateNotificationSetting(@PathVariable("name") String name, @Valid @RequestBody Recipient recipient){
//        return recipientService.save(name, recipient);
//    }
//}
