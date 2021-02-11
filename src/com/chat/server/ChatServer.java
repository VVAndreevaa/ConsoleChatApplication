package com.chat.server;

import com.chat.controller.MessageController;
import com.chat.entity.Group;
import com.chat.entity.Message;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static com.chat.command.Commands.*;

public class ChatServer {

    private static final int SERVER_PORT = 1234;
    private final List<MessageController> users;
    private final List<Group> groups;
    private ServerSocket serverSocket;
    private ServerResponse serverResponse;
    private boolean isActiveServer;

    public ChatServer() {
        this.users = new ArrayList<>();
        this.groups = new ArrayList<>();
        this.isActiveServer = true;
    }

    public static void main(String[] args) {
        ChatServer chatServer = new ChatServer();
        chatServer.startServer();
    }

    private void startServer() {
        try {
            serverSocket = new ServerSocket(SERVER_PORT);
            Socket socket = serverSocket.accept();
            serverResponse = new ServerResponse(this);
            while (isActiveServer){
                MessageController messageController = new MessageController(this, socket);
                synchronized (users) {
                    users.add(messageController);
                }
                messageController.start();
                socket = serverSocket.accept();
            }
        } catch (IOException e) {
            System.err.println(e.getMessage());
        } finally {
            closeServerResources();
        }
    }

    private void closeServerResources(){
        for(Iterator<MessageController> iter = users.iterator(); iter.hasNext();) {
            MessageController user = iter.next();
            user.sendMessage(new Message(DISCONNECT, "ADMIN", "", "Server closed! Press ENTER to exit."));
            iter.remove();
        }
        for (Iterator<Group> iter = groups.iterator(); iter.hasNext();) {
            Group group = iter.next();
            removeGroup(group);
            iter.remove();
        }
        if (serverSocket != null){
            try {
                serverSocket.close();
            } catch (IOException e) {
                System.err.println(e.getMessage());
            }
        }
    }

    public List<MessageController> getUsers() {
        return users;
    }

    public List<Group> getGroups() {
        return groups;
    }

    public synchronized void newGroup(Group group) {
        groups.add(group);
    }

    public void removeGroup(Group group) {
        groups.remove(group);
    }

    public void removeUser(MessageController messageController) {
        users.remove(messageController);
    }

    public void stopServer() {
        Socket socket;
        isActiveServer = false;
        try {
            socket = new Socket(serverSocket.getInetAddress(), SERVER_PORT);
            socket.close();
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
    }

    public synchronized void commandHandler(Message message, Socket socket){
        String command = message.getCommand();
        switch (command) {
            case CONNECT -> serverResponse.respondToConnect(socket);
            case DISCONNECT -> serverResponse.respondToDisconnect(socket);
            case REGISTER -> serverResponse.respondToRegister(message, socket);
            case LOGIN -> serverResponse.respondToLogin(message, socket);
            case NEW_GROUP -> serverResponse.respondToNewGroup(message, socket);
            case DEL_GROUP -> serverResponse.respondToDeleteGroup(message, socket);
            case JOIN_GROUP, LEAVE_GROUP -> serverResponse.respondToGroupAction(message, socket);
            case SEND -> serverResponse.respondToMessage(message, socket);
            case SEND_TO_GROUP -> serverResponse.respondToGroupMessage(message, socket);
            case ALL_USERS -> serverResponse.respondToAllUsers(socket);
            case ALL_GROUPS -> serverResponse.respondAllGroups(socket);
            case EXIT -> serverResponse.respondToExit(socket);
        }
    }
}