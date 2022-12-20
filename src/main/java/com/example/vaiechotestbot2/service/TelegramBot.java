package com.example.vaiechotestbot2.service;

import com.example.vaiechotestbot2.config.BotConfig;
import com.example.vaiechotestbot2.model.User;
import com.example.vaiechotestbot2.model.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class TelegramBot extends TelegramLongPollingBot {

    private final UserRepository userRepository;
    private final BotConfig config;

    @Autowired
    public TelegramBot(UserRepository userRepository, BotConfig config) {
        this.userRepository = userRepository;
        this.config = config;
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
                    startCommandResieved(chatId);
                    break;
                default:
                    waitAndSend(chatId, messageText, getSendTime(update.getMessage().getDate()));
            }
        }
    }

    private void waitAndSend(long chatId, String messageText, Timestamp sentAt) {
        try {
            TimeUnit.MILLISECONDS.sleep(config.getDelay());

            User user = userRepository.findById(chatId).get();
            userRepository.save(user.lastMessageSentAt(sentAt).messagesSent(user.messagesSent() + 1));
            sendMessage(chatId, messageText + " " + user.messagesSent());
        } catch (InterruptedException e) {
            log.error("Interrupted: chatId: " + chatId + " with message: " + messageText + ". Exception: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }


    private Timestamp getSendTime(Integer date) {
        return Timestamp.from(Instant.ofEpochSecond(date));
    }

    private void registerUser(Message message) {
        if (userRepository.findById(message.getChatId()).isEmpty()) {
            Long chatId = message.getChatId();
            Timestamp time = getSendTime(message.getDate());

            User user = new User().chatId(chatId).messagesSent(0L).lastMessageSentAt(time);
            userRepository.save(user);
            log.info("User saved" + user);
        }
    }

    private void startCommandResieved(long chatId) {
        sendMessage(chatId, "It's ALIVE!");
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

}
