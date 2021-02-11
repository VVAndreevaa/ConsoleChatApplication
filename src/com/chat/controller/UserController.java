package com.chat.controller;

import java.io.*;

public class UserController {

    private static final String USERS_FILE_PATH = "users.txt";

    public boolean registerUser(String username, String password) {
        new File(USERS_FILE_PATH);
            if (!isRegisteredUsername(username)) {
                synchronized (this) {
                    try (PrintWriter writer = new PrintWriter(new FileOutputStream(USERS_FILE_PATH, true))) {
                        writer.println(username + " " + password);
                        writer.flush();
                        return true;
                    } catch (IOException e) {
                        System.err.println(e.getMessage());
                    }
                }
            }
            return false;
    }

    public boolean isSuccessfulLogin(String username, String password) {
        new File(USERS_FILE_PATH);
        try (BufferedReader reader = new BufferedReader(new FileReader(USERS_FILE_PATH))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] tokens = line.split(" ");
                if (tokens[0].equals(username) && tokens[1].equals(password)) {
                    return true;
                }
            }
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
        return false;
    }

    private boolean isRegisteredUsername(String username) {
        try (BufferedReader br = new BufferedReader(new FileReader(USERS_FILE_PATH))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] tokens = line.split(" ");
                if (tokens[0].equals(username)) {
                    return true;
                }
            }
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
        return false;
    }
}