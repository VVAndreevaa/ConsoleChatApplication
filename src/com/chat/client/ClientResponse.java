package com.chat.client;

import com.chat.entity.Message;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Set;

import static com.chat.command.Commands.DISCONNECT;

public class ClientResponse {

    private static final Set<String> SUCCESS_MESSAGE_TEXTS = Set.of("Successfully registered in!", "Successfully logged in!");
    private final ClientControl clientControl;

    public ClientResponse(ClientControl clientControl) {
        this.clientControl = clientControl;
    }

    public void showReceivedMessage(Message receivedMessage) {
        if (SUCCESS_MESSAGE_TEXTS.contains(receivedMessage.getText())) {
            clientControl.setIsLogged(true);
        }
        if (receivedMessage.getCommand().equals(DISCONNECT)) {
            clientControl.setIsLogged(false);
        }
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm:ss");
        final String time = simpleDateFormat.format(new Date());
        final String message = receivedMessage.getSender() + " - " + time + " => " + receivedMessage.getText();
        System.out.println(message);
    }
}