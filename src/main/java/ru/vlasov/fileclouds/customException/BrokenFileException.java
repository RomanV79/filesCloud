package ru.vlasov.fileclouds.customException;

public class BrokenFileException extends Exception {
    public BrokenFileException(String message) {
        super(message);
    }
}
