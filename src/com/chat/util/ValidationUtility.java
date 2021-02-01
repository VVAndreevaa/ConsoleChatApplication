package com.chat.util;

import com.chat.exception.InvalidInputException;

public final class ValidationUtility {

    private ValidationUtility() {
        throw new AssertionError("The Utility Class methods should be accessed statically!");
    }

    public static void requireNonNull(Object obj, String exceptionMessage) {
        if (obj == null) {
            throw new InvalidInputException(exceptionMessage);
        }
    }
}
