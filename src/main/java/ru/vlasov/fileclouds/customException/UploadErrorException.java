package ru.vlasov.fileclouds.customException;

public class UploadErrorException extends Exception {
    public UploadErrorException(String message) {
        super(message);
    }
}
