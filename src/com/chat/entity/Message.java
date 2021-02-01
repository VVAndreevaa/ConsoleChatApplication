package com.chat.entity;

import java.io.Serializable;

public class Message implements Serializable {

    private final String command;
    private final String sender;
    private final String receiver;
    private final String text;

    public Message(String command, String sender, String receiver, String text) {
        this.command = command;
        this.sender = sender;
        this.receiver = receiver;
        this.text = text;
    }

    public String getCommand() {
        return command;
    }

    public String getSender() {
        return sender;
    }

    public String getReceiver() {
        return receiver;
    }

    public String getText() {
        return text;
    }
}
