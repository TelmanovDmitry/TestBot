package com.example.vaiechotestbot2.controller;

import com.example.vaiechotestbot2.model.User;
import com.example.vaiechotestbot2.service.TelegramBot;
import com.example.vaiechotestbot2.service.UserRepositoryService;
import com.example.vaiechotestbot2.web.JsonDelay;
import com.example.vaiechotestbot2.web.RequestEchoMessage;
import com.example.vaiechotestbot2.web.ResponseEchoMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
public class BotController {

    private final UserRepositoryService userRepositoryService;
    private final TelegramBot bot;

    @Autowired
    public BotController(UserRepositoryService userRepositoryService, TelegramBot bot) {
        this.userRepositoryService = userRepositoryService;
        this.bot = bot;
    }

    @PostMapping("/receiveEchoMessage")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEchoMessage receiveEchoMessage(@RequestBody RequestEchoMessage echoMessage) {
        long sender = Long.parseLong(echoMessage.getUser_sender());
        Optional<User> userOp = userRepositoryService.findUserById(sender);
        if(userOp.isPresent()) {
            User user = userOp.get();
            int messagesSent = user.messagesSent() + 1;

            user.messagesSent(messagesSent).lastMessageSentAt(user.lastMessageSentAt());

            bot.waitAndSend(user, echoMessage.getMessage() + " " + messagesSent);

            return new ResponseEchoMessage(true, messagesSent);
        }
        return new ResponseEchoMessage(false, 0);
    }

    @PutMapping("/updateQueueDelay")
    @ResponseStatus(HttpStatus.OK)
    public void change(@RequestBody JsonDelay newDelay) {
        bot.setDelay(newDelay.getNew_delay());
    }

}
