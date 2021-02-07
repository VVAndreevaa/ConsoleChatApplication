package com.chat.command;

import com.chat.exception.InvalidCommandParametersException;

public class CommandSplitter {

    private String[] tokens;
    private int numOfTokens;

    public void splitMessage(String message) {
        this.tokens = message.split("\\s+");
        this.numOfTokens = tokens.length;
    }

    public String getCommand() {
        return tokens[0];
    }

    public int getNumOfTokens() {
        return numOfTokens;
    }

    public String getSecondParameter() {
        if (numOfTokens < 2) {
            throw new InvalidCommandParametersException("Invalid number of command parameters!");
        }
        return tokens[1];
    }

    public String getThirdParameter() {
        if (numOfTokens < 3) {
            throw new InvalidCommandParametersException("Invalid number of command parameters!");
        }
        return tokens[2];
    }

    public String getMessageFromIndex(int index) throws InvalidCommandParametersException {
        if (index > 0 && index < numOfTokens) {
            StringBuilder message = new StringBuilder();
            for (int i = index; i < numOfTokens - 1; i++) {
                message.append(tokens[i]).append(" ");
            }
            message.append(tokens[numOfTokens - 1]);
            return message.toString();
        } else {
            throw new InvalidCommandParametersException("Invalid number of command parameters!");
        }
    }
}
