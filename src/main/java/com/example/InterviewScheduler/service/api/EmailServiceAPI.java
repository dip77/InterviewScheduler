package com.example.InterviewScheduler.service.api;

public interface EmailServiceAPI {
    String authorize() throws Exception ;
    void readMail(String token);

}
