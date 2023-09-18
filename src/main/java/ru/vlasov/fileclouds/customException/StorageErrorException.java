package ru.vlasov.fileclouds.customException;

public class StorageErrorException extends Exception {
    public StorageErrorException(String message) {
        super(message);
    }
}
