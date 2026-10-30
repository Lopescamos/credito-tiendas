package pe.edu.upc.center.creditotiendas.storemanagement.application.internal.outboundservices;

/**
 * Stores public files (catalog images) and returns the URL where clients can read them.
 * Implementations decide the storage provider; callers must only keep the returned URL.
 */
public interface FileStorageService {

    /**
     * @param content  file bytes
     * @param folder   logical folder, e.g. {@code products}
     * @param fileName stable name inside the folder; uploading again with the same name replaces the file
     * @return the public HTTPS URL of the stored file
     * @throws FileStorageException if storage is not configured or the upload fails
     */
    String upload(byte[] content, String folder, String fileName);
}
