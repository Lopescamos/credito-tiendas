package pe.edu.upc.center.creditotiendas.storemanagement.infrastructure.filestorage;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Cloudinary settings bound from {@code cloudinary.*}.
 */
@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "cloudinary")
public class CloudinarySettings {

    /** {@code cloudinary://<api_key>:<api_secret>@<cloud_name>} (env {@code CLOUDINARY_URL}). Empty disables uploads. */
    private String url;

    /** Root folder in the Cloudinary account, so this app's files do not mix with others. */
    private String rootFolder = "creditotiendas";
}
