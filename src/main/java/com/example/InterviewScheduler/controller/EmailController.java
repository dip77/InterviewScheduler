package com.example.InterviewScheduler.controller;

import com.example.InterviewScheduler.feign.IntentService;
import com.example.InterviewScheduler.response.IntentRequest;
import com.example.InterviewScheduler.service.api.EmailServiceAPI;
import com.google.api.client.auth.oauth2.AuthorizationCodeRequestUrl;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.auth.oauth2.TokenResponse;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.gmail.GmailScopes;
import com.google.api.services.gmail.model.ListMessagesResponse;
import com.google.api.services.gmail.model.Message;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;

import javax.servlet.http.HttpServletRequest;

@RestController
public class EmailController {

    @Autowired
    private EmailServiceAPI emailServiceAPI;


    @Autowired
    private IntentService intentService;


    @GetMapping(value = "/authenticate")
    public RedirectView authenticate(HttpServletRequest request) throws Exception {
        return new RedirectView(emailServiceAPI.authorize());
    }

    @GetMapping(value = "/saveToken", params = "code")
    public String saveToken(@RequestParam(value = "code") String code) {
        System.out.println(code + " received");
        emailServiceAPI.readMail(code);
        return code;
    }

    @GetMapping(value = "/saveTokenCalendar", params = "code")
    public String saveTokenCalendar(@RequestParam(value = "code") String code) throws Exception {
        System.out.println(code + " received");
        emailServiceAPI.createEvent(code);
        return code;
    }

    @GetMapping(value = "/readEmail")
    public void readEmail(@RequestParam(value = "token") String token) {
        emailServiceAPI.readMail(token);
    }

    @GetMapping(value = "/createEvent")
    public RedirectView createEvent() throws Exception {
        return new RedirectView(emailServiceAPI.authorizeCalendar());
    }
}
