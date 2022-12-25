package com.example.dfsbot.service;

import com.example.dfsbot.config.BotConfig;
import com.example.dfsbot.model.State;
import com.example.dfsbot.model.User;
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
import java.util.Arrays;
import java.util.Optional;

@Slf4j
@Component
public class TelegramBot extends TelegramLongPollingBot {

    private final UserRepositoryService userRepositoryService;
    private final BotConfig config;

    private final String start = "/start";
    private final String info = "/info";
    private final String example = "/example";
    private final String commands = "/commands";
    private final String ort = "/ort";
    private final String notOrt = "/notOrt";
    private final String topOrder = "/topologicalOrder";
    private final String bridges = "/findBridges";
    private final String points = "/findArticulationPoints";

    //logs
    private final String savedUser = "User saved";
    private final String errorOccurred = "Error occurred: ";

    @Autowired
    public TelegramBot(UserRepositoryService userRepositoryService, BotConfig config) {
        this.userRepositoryService = userRepositoryService;
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
            var message = update.getMessage();
            String messageText = message.getText();
            long chatId = message.getChatId();

            if (messageText.equals(start)) {
                registerUser(message);
                String welcome = "To start our conversation write " + info;
                sendMessage(chatId, welcome);
                return;
            }

            Optional<User> userOpt = userRepositoryService.findUserById(chatId);

            if (userOpt.isEmpty()) {
                String noRegistration = "You should register first.\n Type " + start;
                sendMessage(chatId, noRegistration);
                return;
            }

            User user = userOpt.get();
            Timestamp time = getSendTime(message.getDate());

            switch (messageText) {
                case info:
                    userRepositoryService.save(user.lastMessageSentAt(time));
                    sendInfoData(chatId, update.getMessage().getChat().getFirstName());
                    break;
                case example:
                    userRepositoryService.save(user.lastMessageSentAt(time));
                    sendExample(chatId);
                    break;
                case commands:
                    userRepositoryService.save(user.lastMessageSentAt(time));
                    sendCommands(chatId);
                    break;
                case ort:
                    userRepositoryService.save(user.isOrt(true).lastMessageSentAt(time));
                    break;
                case notOrt:
                    userRepositoryService.save(user.isOrt(false).lastMessageSentAt(time));
                    break;
                case topOrder:
                    userRepositoryService.save(user.state(State.TOPORDER).lastMessageSentAt(time));
                    break;
                case bridges:
                    userRepositoryService.save(user.state(State.BRIDGES).lastMessageSentAt(time));
                    break;
                case points:
                    userRepositoryService.save(user.state(State.POINTS).lastMessageSentAt(time));
                    break;
                default:
                    userRepositoryService.save(user.lastMessageSentAt(time));
                    Boolean isOrt = user.isOrt();
                    State state = user.state();
                    parseAndExecute(chatId, messageText, isOrt, state);
            }
        }
    }

    private void sendCommands(long chatId) {
        String availableCommands = "Available commands:\n" + info + "\n" + example + "\n" + commands + "\n"
                + ort + "\n" + notOrt + "\n" + topOrder + "\n" + bridges + "\n" + points;
        sendMessage(chatId, availableCommands);
    }

    private void sendExample(long chatId) {
        String toExAnnotation = "Example of calling method to represent graph vertexes in topological order:";
        sendMessage(chatId, toExAnnotation);
        sendMessage(chatId, ort);
        sendMessage(chatId, topOrder);

        String toExData = "6 7\n1 2\n2 3\n3 1\n1 4\n4 5\n4 6\n5 6\n";
        sendMessage(chatId, toExData);

        String toExAnswer = "Bot will return:\n4 6 3 1 2 5";
        sendMessage(chatId, toExAnswer);
    }

    private void sendInfoData(long chatId, String firstName) {
        firstName = firstName == null ? "Hi" : "Hi, " + firstName;
        sendMessage(chatId, firstName + config.getInfoMessage());
    }

    private void registerUser(Message message) {
        if (userRepositoryService.findUserById(message.getChatId()).isEmpty()) {
            Long chatId = message.getChatId();
            Timestamp time = getSendTime(message.getDate());

            User user = new User().chatId(chatId).lastMessageSentAt(time).isOrt(false).state(State.NONE);
            userRepositoryService.save(user);
            log.info(savedUser + user);
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
            log.error(errorOccurred + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    private void parseAndExecute(long chatId, String message, Boolean isOrt, State state) {
        try {
            var dataArray = Arrays.stream(message.split("\\s+\\n*")).mapToInt(Integer::parseInt).toArray();

            int n = dataArray[0];
            int m = dataArray[1];

            Graph g = new Graph(n);

            int i = 2;
            for (int j = 0; j < m; j++) {
                g.addEdge(dataArray[i], dataArray[i + 1], isOrt);
                i += 2;
            }

            String noneState = "Choose problem first";

            switch (state) {
                case TOPORDER:
                    sendMessage(chatId, g.topologicalSort());
                    break;
                case BRIDGES:
                    sendMessage(chatId, g.findBridges());
                    break;
                case POINTS:
                    sendMessage(chatId, g.findArticulationPoints());
                    break;
                default:
                    sendMessage(chatId, noneState);
            }
        } catch (NumberFormatException e) {
            String errorNFE1 = "Wrong Data\n";
            String errorNFE2 = "\nTry again.";
            sendMessage(chatId, errorNFE1 + e.getLocalizedMessage() + errorNFE2);
        }
    }
}
