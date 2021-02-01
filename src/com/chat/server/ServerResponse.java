package com.chat.server;

import com.chat.controller.MessageController;
import com.chat.controller.UserController;
import com.chat.entity.Group;
import com.chat.entity.Message;

import java.net.Socket;
import java.util.List;

import static com.chat.command.Commands.*;

import static com.chat.util.ValidationUtility.*;

public class ServerResponse {

    private static final String ADMIN = "ADMIN";
    private final ChatServer chatServer;
    private final UserController userController;

    public ServerResponse(ChatServer chatServer) {
        this.chatServer = chatServer;
        this.userController = new UserController();
    }

    private MessageController getUserByUsername(String username) {
        for (MessageController messageController : chatServer.getUsers()) {
            if (messageController.getUsername().equals(username)) {
                return messageController;
            }
        }
        return null;
    }

    private MessageController getUserBySocket(Socket socket) {
        for (MessageController messageController : chatServer.getUsers()) {
            if (messageController.getSocket().equals(socket)) {
                return messageController;
            }
        }
        return null;
    }

    private Group getGroupByName(String name) {
        for (Group group : chatServer.getGroups()) {
            if (group.getName().equals(name)) {
                return group;
            }
        }
        return null;
    }

    public void respondToConnect(Socket socket) {
        MessageController user = getUserBySocket(socket);
        requireNonNull(user, "");
        user.sendMessage(new Message("send", ADMIN, "", "You have successfully connected."));
    }

    public void respondToDisconnect(Socket socket) {
        MessageController user = getUserBySocket(socket);
        requireNonNull(user, "");
        chatServer.removeUser(user);
        user.sendMessage(new Message("disconnect", ADMIN, "", "Disconnect: " + user.getUsername()));
    }

    public void respondToRegister(Message receivedMessage, Socket socket) {
        MessageController user = getUserBySocket(socket);
        requireNonNull(user, "");
        String message;
        if (userController.registerUser(receivedMessage.getSender(), receivedMessage.getText())) {
            user.setUsername(receivedMessage.getSender());
            message = "Successfully registered in!";
        } else {
            message = "This username already exists!";
        }
        user.sendMessage(new Message("send", ADMIN, receivedMessage.getSender(), message));
    }

    public void respondToLogin(Message receivedMessage, Socket socket) {
        MessageController user = getUserBySocket(socket);
        requireNonNull(user, "");
        MessageController temp = getUserByUsername(receivedMessage.getSender());
        String message;

        if (temp == null) {
            if (userController.isSuccessfulLogin(receivedMessage.getSender(), receivedMessage.getText())) {
                user.setUsername(receivedMessage.getSender());
                message = "Successfully logged in!";
            } else {
                message = "Login failed. Wrong username or password.";
            }
        } else {
            message = "This account is already in use.";
        }
        user.sendMessage(new Message("send", ADMIN, receivedMessage.getSender(), message));
    }

    private void sendToGroup(Group group, Message receivedMessage) {
        for (MessageController messageController : group.getMembers()) {
            messageController.sendMessage(receivedMessage);
        }
    }

    public void respondToNewGroup(Message receivedMessage, Socket socket) {
        MessageController user = getUserBySocket(socket);
        requireNonNull(user, "");
        Group group = new Group(receivedMessage.getText(), user);
        String message;

        if (!chatServer.getGroups().contains(group)) {
            chatServer.newGroup(group);
            message = "New group: " + group.getName();
        } else {
            message = "Name is already used.";
        }
        user.sendMessage(new Message("send", ADMIN, "", message));
    }

    public void respondToDeleteGroup(Message receivedMessage, Socket socket) {
        MessageController user = getUserBySocket(socket);
        requireNonNull(user, "");
        Group group = getGroupByName(receivedMessage.getText());
        String message;

        if (group != null) {
            if (group.getAdmin().equals(user)) {
                notifyDeletedGroup(group, user);
                chatServer.removeGroup(group);
                message = "Group " + group.getName() + " has been deleted successfully!";
            } else {
                message = "You have no permissions to do that.";
            }
        } else {
            message = "There is no group with this name.";
        }
        user.sendMessage(new Message("send", ADMIN, "", message));
    }

    private void notifyDeletedGroup(Group group, MessageController messageController) {
        final String messageToMembers = "Group " + group.getName() + " has been deleted by admin!";
        Message newMessage = new Message("send", ADMIN, "", messageToMembers);
        group.removeUser(messageController);
        sendToGroup(group, newMessage);
    }

    public void respondToGroupAction(Message receivedMessage, Socket socket) {
        MessageController user = getUserBySocket(socket);
        requireNonNull(user, "");
        Group group = getGroupByName(receivedMessage.getText());
        String message;
        if (group != null) {
            message = groupActionsHandler(group, user, receivedMessage, receivedMessage.getCommand());
        } else {
            message = "There is no such group.";
        }
        user.sendMessage(new Message("send", ADMIN, "", message));
    }

    public String groupActionsHandler(Group group, MessageController user, Message receivedMessage, String command) {
        String message = "";

        if (group.containsUser(user)) {
            switch (command) {
                case JOIN_GROUP -> message = "You are already in the group.";
                case LEAVE_GROUP -> message = leavingActionsBasedOnUserPermissions(group, user, receivedMessage);
            }
        } else {
            switch (command) {
                case JOIN_GROUP -> message = joinGroupActions(group, user);
                case LEAVE_GROUP -> message = "You are not a member of this group!";
            }
        }
        return message;
    }

    public String joinGroupActions(Group group, MessageController user) {
        String message;
        Message messageToMembers = new Message("send", group.getName(), "", "New member: \"" + user.getUsername() + "\" in the group " + group.getName());
        sendToGroup(group, messageToMembers);
        group.addUser(user);
        message = "Welcome to \"" + group.getName() + "\" group!";

        return message;
    }

    // Removes the group if the user leaving it is an admin, otherwise removes the user from the group
    public String leavingActionsBasedOnUserPermissions(Group group, MessageController user, Message receivedMessage) {
        String message;
        if (group.getAdmin().equals(user)) {
            chatServer.removeGroup(group);
            message = "You deleted \"" + group.getName() + "\" group!";
            notifyDeletedGroup(group, user);
        } else {
            message = "You left the group \"" + receivedMessage.getText() + "\"";
            group.removeUser(user);
            Message messageToMembers = new Message("send", group.getName(), "", user.getUsername() + " has left the group " + group.getName());
            sendToGroup(group, messageToMembers);
        }
        return message;
    }

    public void respondToMessage(Message receivedMessage, Socket socket) {
        MessageController user = getUserBySocket(socket);
        requireNonNull(user, "");
        MessageController receiver = getUserByUsername(receivedMessage.getReceiver());

        if (receiver != null) {
            receiver.sendMessage(new Message("send",
                    user.getUsername(),
                    receivedMessage.getReceiver(),
                    receivedMessage.getText()));
        } else {
            user.sendMessage(new Message("send", ADMIN, "", "There is no active user with that name!"));
        }
    }

    public void respondToGroupMessage(Message receivedMessage, Socket socket) {
        MessageController user = getUserBySocket(socket);
        requireNonNull(user, "");
        Group group = getGroupByName(receivedMessage.getReceiver());
        String message = user.getUsername() + ": " + receivedMessage.getText();

        if (group != null) {
            if (group.containsUser(user)) {
                Message messageToMembers = new Message("send", group.getName(), "", message);
                sendToGroup(group, messageToMembers);
                return;
            } else {
                message = "You are not in the group.";
            }
        } else {
            message = "There is no group with that name";
        }
        user.sendMessage(new Message("send", ADMIN, "", message));
    }

    public void respondToAllUsers(Socket socket) {
        MessageController user = getUserBySocket(socket);
        requireNonNull(user, "");
        int counter = 1;
        StringBuffer message = new StringBuffer("Online users:" + "\n");
        for (MessageController messageController : chatServer.getUsers()) {
            message.append(counter).append(". ").append(messageController.getUsername()).append("\n");
            counter++;
        }
        user.sendMessage(new Message("send", ADMIN, "", message.toString()));
    }

    public void respondAllGroups(Socket socket) {
        MessageController user = getUserBySocket(socket);
        requireNonNull(user, "");
        List<Group> list = chatServer.getGroups();
        int counter = 1;
        StringBuffer message = new StringBuffer("Groups:" + "\n");
        String activeUsersCountMessage;
        for (Group group : list) {
            activeUsersCountMessage = " with " + group.getMembers().size() + " users";
            message.append(counter).append(". ").append(group.getName()).append(activeUsersCountMessage).append("\n");
            counter++;
        }
        if (counter == 1) {
            message = new StringBuffer("No online groups." + "\n");
        }
        user.sendMessage(new Message("send", ADMIN, "", message.toString()));
    }

    public void respondToExit(Socket socket) {
        MessageController user = getUserBySocket(socket);
        requireNonNull(user, "");
        if (user.getUsername().equals("admin")) {
            chatServer.stopServer();
        } else {
            user.sendMessage(new Message("send", ADMIN, "", "You are not authorized to do this!"));
        }
    }
}