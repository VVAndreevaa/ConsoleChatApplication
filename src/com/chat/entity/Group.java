package com.chat.entity;

import com.chat.controller.MessageController;

import java.util.ArrayList;
import java.util.List;

public class Group {

    private final String name;
    private final MessageController admin;
    private final List<MessageController> members;

    public Group(String name, MessageController admin) {
        this.name = name;
        this.admin = admin;

        members = new ArrayList<>();
        members.add(this.admin);
    }

    public String getName() {
        return name;
    }
    public MessageController getAdmin() {
        return admin;
    }
    public List<MessageController> getMembers() {
        return members;
    }

    public void addUser(MessageController messageController){
        synchronized (members){
            members.add(messageController);
        }
    }
    public void removeUser(MessageController messageController){
        members.remove(messageController);
    }
    public boolean containsUser(MessageController messageController){
        return members.contains(messageController);
    }

    @Override
    public int hashCode() {
        final int prime = 29;
        int result = 3;
        result = prime * result + ((name == null) ? 0 : name.hashCode());
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
        Group other = (Group) obj;
        if (name == null) {
            return other.name == null;
        } else return name.equals(other.name);
    }
}