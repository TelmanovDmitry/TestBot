package com.example.vaiechotestbot2.web;

import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class ResponseEchoMessage {
    private Boolean is_ok;

    private int message_number;
}
