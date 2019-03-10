package com.example.InterviewScheduler.service.impl;

import com.example.InterviewScheduler.feign.IntentService;
import com.example.InterviewScheduler.response.IntentRequest;
import com.example.InterviewScheduler.response.IntentResponse;
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
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.CalendarScopes;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventAttendee;
import com.google.api.services.calendar.model.EventDateTime;
import com.google.api.services.gmail.GmailScopes;
import com.google.api.services.gmail.model.ListMessagesResponse;
import com.google.api.services.gmail.model.Message;
import com.google.api.services.gmail.model.Thread;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;

@Service
public class EmailServiceImpl implements EmailServiceAPI {

    @Autowired
    private IntentService intentService;

    @Value("${gmail.client.redirectUri}")
    private String redirectUri;

    @Value("${calendar.client.redirectUri}")
    private String redirectUriCalendar;

    @Value("${gmail.client.clientId}")
    private String clientId;

    @Value("${gmail.client.clientSecret}")
    private String clientSecret;

    @Value("${calendar.client.clientId}")
    private String clientIdCalendar;

    @Value("${calendar.client.clientSecret}")
    private String clientSecretCalendar;


    private static final String APPLICATION_NAME = "InterviewScheduler";
    private static HttpTransport httpTransport;
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    private static com.google.api.services.gmail.Gmail client;
    GoogleClientSecrets clientSecrets;
    GoogleAuthorizationCodeFlow flow;
    Credential credential;


    public String authorize() throws Exception {
        AuthorizationCodeRequestUrl authorizationUrl;
        GoogleClientSecrets.Details web = new GoogleClientSecrets.Details();
        web.setClientId(clientId);
        web.setClientSecret(clientSecret);
        clientSecrets = new GoogleClientSecrets().setWeb(web);
        httpTransport = GoogleNetHttpTransport.newTrustedTransport();
        flow = new GoogleAuthorizationCodeFlow.Builder(httpTransport, JSON_FACTORY, clientSecrets,
                Arrays.asList(GmailScopes.GMAIL_READONLY, CalendarScopes.CALENDAR_EVENTS)).build();
        authorizationUrl = flow.newAuthorizationUrl().setRedirectUri(redirectUri);
        return authorizationUrl.build();
    }

    public String authorizeCalendar() throws Exception {
        AuthorizationCodeRequestUrl authorizationUrl;
        GoogleClientSecrets.Details web = new GoogleClientSecrets.Details();
        web.setClientId(clientIdCalendar);
        web.setClientSecret(clientSecretCalendar);
        clientSecrets = new GoogleClientSecrets().setWeb(web);
        httpTransport = GoogleNetHttpTransport.newTrustedTransport();
        flow = new GoogleAuthorizationCodeFlow.Builder(httpTransport, JSON_FACTORY, clientSecrets,
                Collections.singleton(CalendarScopes.CALENDAR_EVENTS)).build();
        authorizationUrl = flow.newAuthorizationUrl().setRedirectUri(redirectUriCalendar);
        return authorizationUrl.build();
    }


    public void readMail(String token) {
        JSONObject json = new JSONObject();
        JSONArray arr = new JSONArray();
        try {
            TokenResponse response = flow.newTokenRequest(token).setRedirectUri(redirectUri).execute();
            credential = flow.createAndStoreCredential(response, "dip.halani@coviam.com");
            System.out.println(credential);
            client = new com.google.api.services.gmail.Gmail.Builder(httpTransport, JSON_FACTORY, credential)
                    .setApplicationName(APPLICATION_NAME).build();

            String userId = "dip.halani@coviam.com";
            String query = "from:'nilay.shrivastava	@coviam.com'";
            ListMessagesResponse MsgResponse = client.users().messages().list(userId).setQ(query).execute();
            System.out.println("message length:" + MsgResponse.getMessages().size());
            System.out.println("message response: " + MsgResponse.getMessages());
            Message msg = MsgResponse.getMessages().get(0);
            Message message = client.users().messages().get(userId, msg.getId()).execute();
            System.out.println("actual message " + message);
            System.out.println("snippet :" + message.getSnippet());
            Thread thread = client.users().threads().get(userId, message.getThreadId()).execute();

            System.out.println("calling intent service with text " + message.getSnippet());
            IntentRequest intentRequest = new IntentRequest(message.getSnippet());
            IntentResponse intentResponse = intentService.getIntent(intentRequest);
            System.out.println(intentResponse);
            if (!CollectionUtils.isEmpty(intentResponse.getDates())) {
                int size = intentResponse.getDates().size();
                if (thread.getMessages().size() != 1) {
                    String lastSLot = intentResponse.getDates().remove(size - 1);
                    System.out.println(lastSLot + " removed");
                }
                //todo calling invitation API if accept
                createEvent(intentResponse.getDates().get(0));
            }
        } catch (Exception e) {
            System.out.println("failed");
            e.printStackTrace();
        }

    }

    public void createEvent(String dateTime) throws Exception {
        System.out.println("creating event on " + dateTime);
        Calendar service = new Calendar.Builder(httpTransport, JSON_FACTORY, credential)
                .setApplicationName("applicationName").build();
        Event event = new Event()
                .setSummary("Interview With Coviam Technology")
                .setLocation("1076, 24th Main, 11th Cross, HSR Layout, Bengaluru, Karnataka 560102")
                .setDescription("First Round for the post of Software Engineer");
        DateTime startDateTime = new DateTime(dateTime + ".000+05:30");
        System.out.println(startDateTime);
        EventDateTime start = new EventDateTime()
                .setDateTime(startDateTime);
        java.util.Calendar calendar = java.util.Calendar.getInstance();
        calendar.setTimeInMillis(startDateTime.getValue());
        calendar.add(java.util.Calendar.HOUR, 1);
        event.setStart(start);
        DateTime endDateTime = new DateTime(calendar.getTime());
        System.out.println(endDateTime);
        EventDateTime end = new EventDateTime()
                .setDateTime(endDateTime);
        event.setEnd(end);
        EventAttendee[] attendees = new EventAttendee[]{
                new EventAttendee().setEmail("dip.halani@coviam.com"),
                new EventAttendee().setEmail("nilay.shrivastava@coviam.com")
        };
        event.setAttendees(Arrays.asList(attendees));
        String calendarId = "primary";
        event = service.events().insert(calendarId, event).execute();
        System.out.printf("Event created: %s\n", event.getHtmlLink());


    }

    public static void main(String[] args) {
        DateTime startDateTime = new DateTime("2019-02-04T03:30:00.000+05:30");

        //     DateTime startDateTime = new DateTime("2015-05-28T09:00:00-07:00");
        //2015-05-28T09:00:00-07:00
        System.out.println(startDateTime);

    }


}
