package com.chat.client;

import com.chat.command.CommandSplitter;
import com.chat.entity.Message;

import java.awt.geom.IllegalPathStateException;
import java.util.Scanner;

import static com.chat.command.Commands.*;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

public class ClientControl {

    private static final long TIMEOUT = 10;
    private final Scanner scanner;
    private final CommandSplitter commandSplitter;
    private ChatClient chatClient;
    private boolean isLogged;

    public ClientControl(Scanner scanner) {
        this.scanner = scanner;
        this.commandSplitter = new CommandSplitter();
        isLogged = false;
    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        new ClientControl(scanner).start();
        scanner.close();
    }

    public void start() {
        String message;
        connectClient();
        authenticateClient();
        do {
            if (!isLogged) {
                break;
            }
            message = scanner.nextLine();
            analyzeCurrentCommand(message);
        } while (isLogged);
    }

    private void connectClient() {
        String message, command;
        do {
            System.out.println("Please enter a connect command: ");
            message = scanner.nextLine();
            commandSplitter.splitMessage(message);
            command = commandSplitter.getCommand();
        } while (!command.equals(CONNECT));
        processUserConnectCommand();
        connectMessage(command, commandSplitter.getSecondParameter(), Integer.parseInt(commandSplitter.getThirdParameter()));
        try {
            MILLISECONDS.sleep(TIMEOUT);
        } catch (InterruptedException e) {
            System.err.println(e.getMessage());
        }
    }

    private void processUserConnectCommand() {
        boolean isNumeric = false;
        String message;
        do {
            if (commandSplitter.getNumOfTokens() == 3) {
                isNumeric = commandSplitter.getThirdParameter().chars().allMatch(Character::isDigit);
            }
            if (commandSplitter.getNumOfTokens() < 3 || !isNumeric) {
                System.out.println("Please enter a valid connect command: ");
                message = scanner.nextLine();
                commandSplitter.splitMessage(message);
            }
        } while (commandSplitter.getNumOfTokens() != 3 || !isNumeric);
    }


    private void authenticateClient() {
        String message, command;
        do {
            System.out.println("Please register or login to continue.");
            message = scanner.nextLine();
            commandSplitter.splitMessage(message);
            command = processUserAuthenticateCommand();
            getInSystem(command, commandSplitter.getSecondParameter(), commandSplitter.getThirdParameter());
            try {
                MILLISECONDS.sleep(TIMEOUT);
            } catch (InterruptedException e) {
                System.err.println(e.getMessage());
            }
        } while (!isLogged);
    }

    private String processUserAuthenticateCommand() {
        String message, command = commandSplitter.getCommand();
        while (commandSplitter.getNumOfTokens() < 3) {
            System.out.println("Please enter a valid authentication command: ");
            message = scanner.nextLine();
            commandSplitter.splitMessage(message);
            command = commandSplitter.getCommand();
        }
        return command;
    }

    private void analyzeCurrentCommand(String message) {
        commandSplitter.splitMessage(message);
        String command = commandSplitter.getCommand();
        try {
            switch (command) {
                case DISCONNECT -> {
                    isLogged = false;
                    chatClient.send(new Message(DISCONNECT, "", "", ""));
                }
                case SEND, SEND_TO_GROUP -> sendMessage(command, commandSplitter.getSecondParameter(), commandSplitter.getMessageFromIndex(2));
                case NEW_GROUP, DEL_GROUP, JOIN_GROUP, LEAVE_GROUP -> groupMessageCommand(command, commandSplitter.getSecondParameter());
                case ALL_USERS, ALL_GROUPS, EXIT -> globalServerMessage(command);
                default -> System.out.println("Invalid command!");
            }
        } catch (IllegalPathStateException e) {
            System.err.println(e.getMessage());
        }
    }

    private void groupMessageCommand(String command, String groupName) {
        chatClient.send(new Message(command, "", "", groupName));
    }

    private void sendMessage(String command, String receiver, String message) {
        chatClient.send(new Message(command, "", receiver, message));
    }

    private void globalServerMessage(String command) {
        chatClient.send(new Message(command, "", "", ""));
    }

    private void getInSystem(String command, String username, String password) {
        chatClient.send(new Message(command, username, "", password));
    }

    private void connectMessage(String command, String host, int port) {
        chatClient = new ChatClient(host, port, this);
        chatClient.start();
        chatClient.send(new Message(command, "", "", ""));
    }

    public void setIsLogged(boolean isLogged) {
        this.isLogged = isLogged;
    }
}
