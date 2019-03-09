package com.example.InterviewScheduler.response;


import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class IntentResponse {
    private String intent;
    private List<String> dates;
}
