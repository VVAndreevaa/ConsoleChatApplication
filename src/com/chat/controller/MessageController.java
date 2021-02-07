package com.chat.controller;

import com.chat.entity.Message;
import com.chat.exception.InvalidInputException;
import com.chat.server.ChatServer;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import static com.chat.command.Commands.DISCONNECT;

public class MessageController extends Thread {

    private final ChatServer chatServer;
    private final Socket socket;
    private ObjectInputStream objectInputStream;
    private ObjectOutputStream objectOutputStream;
    private String username;

    public MessageController(ChatServer chatServer, Socket socket) {
        try {
            objectInputStream = new ObjectInputStream(socket.getInputStream());
            objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
        this.chatServer = chatServer;
        this.socket = socket;
        this.username = "";
    }

    public Socket getSocket() {
        return socket;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    @Override
    public void run() {
        try {
            while (true) {
                Message message = (Message) objectInputStream.readObject();
                chatServer.commandHandler(message, socket);
                if (message.getCommand().equals(DISCONNECT)) {
                    break;
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            chatServer.removeUser(this);
            System.err.println(e.getMessage());
        } catch (InvalidInputException e){
            System.err.println(e.getMessage());
        } finally {
            closeResources();
        }
    }

    public void sendMessage(Message message) {
        try {
            objectOutputStream.writeObject(message);
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
    }

    @SuppressWarnings("deprecation")
    public void closeResources() {
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
            this.stop();
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
    }

    @Override
    public int hashCode() {
        final int prime = 29;
        int result = 3;
        result = prime * result + ((username == null) ? 0 : username.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        MessageController other = (MessageController) obj;
        if (username == null) {
            return other.username == null;
        } else return username.equals(other.username);
    }
}