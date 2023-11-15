package es.in2.blockchainconnector.integration.dltadapter.exception;

public class DLTAdapterCommunicationException extends RuntimeException {
    public DLTAdapterCommunicationException(String message) {
        super(message);
    }

    public DLTAdapterCommunicationException(String message, Throwable cause) {
        super(message, cause);
    }

}
