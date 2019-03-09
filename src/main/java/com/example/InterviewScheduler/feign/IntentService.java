package com.example.InterviewScheduler.feign;

import com.example.InterviewScheduler.response.IntentRequest;
import com.example.InterviewScheduler.response.IntentResponse;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;


@FeignClient(name = "Intent",url = "${feign.url}")
public interface IntentService {

    @RequestMapping(method = RequestMethod.POST, value = "/predict")
    IntentResponse getIntent(@RequestBody IntentRequest intentRequest);
}
