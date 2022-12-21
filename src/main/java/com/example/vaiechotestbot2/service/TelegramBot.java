package com.example.vaiechotestbot2.service;

import com.example.vaiechotestbot2.config.BotConfig;
import com.example.vaiechotestbot2.model.User;
import com.example.vaiechotestbot2.web.RequestEchoMessage;
import com.example.vaiechotestbot2.web.ResponseEchoMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.Collections;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class TelegramBot extends TelegramLongPollingBot { //TODO migrate to webhook

    private final UserRepositoryService userRepositoryService;
    private final BotConfig config;

    private long delay;

    public void setDelay(long delay) {
        this.delay = delay;
    }

    @Autowired
    public TelegramBot(UserRepositoryService userRepositoryService, BotConfig config) {
        this.userRepositoryService = userRepositoryService;
        this.config = config;
        delay = config.getDelay();
    }

    @Override
    public String getBotUsername() {
        return config.getBotName();
    }

    @Override
    public String getBotToken() {
        return config.getToken();
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            String messageText = update.getMessage().getText();
            long chatId = update.getMessage().getChatId();

            switch (messageText) {
                case "/start":
                    registerUser(update.getMessage());
                    break;
                default:
                    User user = userRepositoryService.findUserById(chatId).get();
                    waitAndSendToServer(user, messageText);
            }
        }
    }

    private void registerUser(Message message) {
        if (userRepositoryService.findUserById(message.getChatId()).isEmpty()) {
            Long chatId = message.getChatId();
            Timestamp time = getSendTime(message.getDate());

            User user = new User().chatId(chatId).messagesSent(0).lastMessageSentAt(time);
            userRepositoryService.save(user);
            log.info("User saved" + user);
        }
    }

    private Timestamp getSendTime(Integer date) {
        return Timestamp.from(Instant.ofEpochSecond(date));
    }

    private void sendMessage(long chatId, String text) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText(text);

        try {
            execute(message);
        } catch (TelegramApiException e) {
            log.error("Error occured: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public void waitAndSend(User user, String message) {
        userRepositoryService.save(user); //TODO
        BotConfig config = new BotConfig();
        config.setDelay(2000L);
        try {
            TimeUnit.MILLISECONDS.sleep(delay);
        } catch (InterruptedException e) {
            log.error("Interrupted before sending to user: chatId: "
                    + user.chatId()
                    + " with message: "
                    + message
                    + ". Exception: " + e.getMessage());
            throw new RuntimeException(e);
        }
        sendMessage(user.chatId(), message);
    }

    private void waitAndSendToServer(User user, String message) {
        //TODO use redis for queue
        try {
            TimeUnit.MILLISECONDS.sleep(delay);
            sendRequestToServer(message, user);
        } catch (InterruptedException e) {
            log.error("Interrupted before sending to server: chatId: "
                    + user.chatId()
                    + " with message: "
                    + message
                    + ". Exception: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    private void sendRequestToServer(String message, User user) {
        long chatId = user.chatId();
//        TODO delete server mock
        user.messagesSent(user.messagesSent() + 1);
        waitAndSend(user, message + " " + user.messagesSent());

        RequestEchoMessage requestEchoMessage = new RequestEchoMessage(message, Long.toString(chatId));
        RestTemplate restTemplate = new RestTemplate();

        String uri = "localhost:8080/receiveEchoMessage"; // some url

        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        headers.add("user-agent",
                "Mozilla/5.0 (Windows NT 10.0; Win64; x64) " +
                        "AppleWebKit/537.36 (KHTML, like Gecko) Chrome/54.0.2840.99 Safari/537.36");

        HttpEntity<String> entity = new HttpEntity<>(requestEchoMessage.toString(), headers);
        ResponseEntity<?> result = restTemplate.exchange(uri, HttpMethod.POST, entity, ResponseEchoMessage.class);


        ResponseEchoMessage echoMessage = (ResponseEchoMessage) result.getBody();
        //Requested service increases message_number
        int messagesSent = echoMessage != null ? echoMessage.getMessage_number() + 1 : 0;

//        waitAndSend(user, message + " " + messagesSent);
    }
}
