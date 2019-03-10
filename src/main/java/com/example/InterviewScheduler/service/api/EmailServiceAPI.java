package com.example.InterviewScheduler.service.api;

public interface EmailServiceAPI {
    String authorize() throws Exception ;
    String authorizeCalendar() throws Exception ;
    void readMail(String token);
    void createEvent(String token) throws Exception ;
}
