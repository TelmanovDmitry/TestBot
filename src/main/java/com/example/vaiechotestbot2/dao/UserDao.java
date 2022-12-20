package com.example.vaiechotestbot2.dao;

import com.example.vaiechotestbot2.model.User;
import org.springframework.data.util.Pair;

import java.sql.Timestamp;
import java.util.Queue;

public class UserDao {

    User user;

    Queue<Pair<String, Timestamp>> messagesQueue;
}
