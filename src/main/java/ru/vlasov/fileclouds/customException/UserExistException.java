package ru.vlasov.fileclouds.customException;

public class UserExistException extends Exception {

        public UserExistException(String message){
            super(message);
        }
}
