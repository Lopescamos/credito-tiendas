package pe.edu.upc.center.creditotiendas.storemanagement.application.internal.outboundservices;

/**
 * Raised by {@link FileStorageService} when a file cannot be stored.
 */
public class FileStorageException extends RuntimeException {
    public FileStorageException(String message) {
        super(message);
    }

    public FileStorageException(String message, Throwable cause) {
        super(message, cause);
    }
}
