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

import java.util.Collections;

@Service
public class EmailServiceImpl implements EmailServiceAPI {

    @Autowired
    private IntentService intentService;

    @Value("${gmail.client.redirectUri}")
    private String redirectUri;

    @Value("${gmail.client.clientId}")
    private String clientId;

    @Value("${gmail.client.clientSecret}")
    private String clientSecret;


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
                Collections.singleton(GmailScopes.GMAIL_READONLY)).build();
        authorizationUrl = flow.newAuthorizationUrl().setRedirectUri(redirectUri);
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

            }
        } catch (Exception e) {
            System.out.println("failed");
            e.printStackTrace();
        }

    }

}
