package com.chat.client;

import com.chat.entity.Message;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;

import static com.chat.command.Commands.DISCONNECT;

public class ChatClient extends Thread {

    private final ClientControl clientControl;
    private ObjectInputStream objectInputStream;
    private ObjectOutputStream objectOutputStream;
    private Socket socket;
    private ClientResponse clientResponse;
    private boolean isActiveUser;

    public ChatClient(String addressName, int port, ClientControl clientControl) {
        this.clientControl = clientControl;
        try {
            socket = new Socket(InetAddress.getByName(addressName), port);
            objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
            objectInputStream = new ObjectInputStream(socket.getInputStream());
            clientResponse = new ClientResponse(clientControl);
            isActiveUser = true;
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
    }

    @Override
    public void run() {
        try {
            while (isActiveUser) {
                Message message = (Message) objectInputStream.readObject();
                if (message.getCommand().equals(DISCONNECT)) {
                    isActiveUser = false;
                }
                clientResponse.showReceivedMessage(message);
            }
        } catch (IOException | ClassNotFoundException e) {
            System.err.println(e.getMessage());
        } finally {
            closeClientThreadResources();
        }
    }

    public void send(Message message) {
        try {
            objectOutputStream.writeObject(message);
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
    }

    public void closeClientThreadResources() {
        try {
            if (socket != null) {
                socket.close();
            }
            if (objectInputStream != null) {
                objectInputStream.close();
            }
            if (objectOutputStream != null) {
                objectOutputStream.close();
            }
            if (clientControl != null) {
                clientControl.setIsLogged(false);
            }
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
    }
}