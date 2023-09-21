package ru.vlasov.fileclouds.customException;

public class EmptyFolderException extends Exception {
    public EmptyFolderException(String message) {
        super(message);
    }
}
