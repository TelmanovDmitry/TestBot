package com.example.vaiechotestbot2.web;

import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class RequestEchoMessage {
    private String message;
    private String user_sender;
}
