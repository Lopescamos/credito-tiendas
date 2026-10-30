package pe.edu.upc.center.creditotiendas.storemanagement.infrastructure.filestorage;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pe.edu.upc.center.creditotiendas.storemanagement.application.internal.outboundservices.FileStorageException;
import pe.edu.upc.center.creditotiendas.storemanagement.application.internal.outboundservices.FileStorageService;

/**
 * Cloudinary implementation of {@link FileStorageService}.
 *
 * <p>When {@code CLOUDINARY_URL} is empty (dev/test without credentials) the app still starts;
 * only uploads fail, with a clear message.</p>
 */
@Slf4j
@Service
public class CloudinaryStorageService implements FileStorageService {

    private final Cloudinary cloudinary;
    private final String rootFolder;

    public CloudinaryStorageService(CloudinarySettings settings) {
        var url = settings.getUrl();
        this.cloudinary = (url == null || url.isBlank()) ? null : new Cloudinary(url);
        this.rootFolder = settings.getRootFolder();
        if (this.cloudinary == null)
            log.warn("CLOUDINARY_URL not set: product image uploads are disabled");
    }

    @Override
    public String upload(byte[] content, String folder, String fileName) {
        if (cloudinary == null)
            throw new FileStorageException("File storage is not configured (CLOUDINARY_URL is empty)");
        try {
            var result = cloudinary.uploader().upload(content, ObjectUtils.asMap(
                    "public_id", "%s/%s/%s".formatted(rootFolder, folder, fileName),
                    "overwrite", true,
                    "invalidate", true,
                    "resource_type", "image"));
            return (String) result.get("secure_url");
        } catch (Exception e) {
            throw new FileStorageException("Cloudinary upload failed for %s/%s".formatted(folder, fileName), e);
        }
    }
}
