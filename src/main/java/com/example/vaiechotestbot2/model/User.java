package com.example.vaiechotestbot2.model;

import lombok.Data;
import lombok.experimental.Accessors;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.sql.Timestamp;

@Data
@Accessors(fluent = true)
@Entity(name = "Users")
public class User {

    @Id
    private Long chatId;

    private int messagesSent;

    private Timestamp lastMessageSentAt = null;

}
