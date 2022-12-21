package com.example.vaiechotestbot2.model;

import lombok.Data;
import org.springframework.data.util.Pair;

import javax.persistence.Id;
import javax.persistence.ManyToOne;
import java.sql.Timestamp;
import java.util.Queue;

@Data
public class MessageQueue {
    @Id
    @ManyToOne
    long ChatId;

    Queue<Pair<String, Timestamp>> mesagesQueue;
}
