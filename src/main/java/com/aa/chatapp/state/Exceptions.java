package com.aa.chatapp.state;

public class Exceptions {

    public static class CustomRegistrationException extends RuntimeException {

        public CustomRegistrationException(String message) {
            super(message);
        }
        
        public CustomRegistrationException(String message, Throwable cause) {
            super(message, cause);
        }
    }

}
