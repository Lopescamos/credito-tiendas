package pe.edu.upc.center.creditotiendas.storemanagement.domain.model.commands;

import java.util.Objects;
import java.util.Set;

import static pe.edu.upc.center.creditotiendas.storemanagement.domain.model.commands.StoreManagementCommandRules.requireId;

/**
 * Uploads (or replaces) the image of a catalog item.
 *
 * @param content     raw image bytes, at most {@link #MAX_SIZE_BYTES}
 * @param contentType one of {@link #ALLOWED_CONTENT_TYPES}
 */
public record UploadProductImageCommand(Long productId, byte[] content, String contentType) {

    public static final long MAX_SIZE_BYTES = 5L * 1024 * 1024;
    public static final Set<String> ALLOWED_CONTENT_TYPES = Set.of("image/jpeg", "image/png", "image/webp");

    public UploadProductImageCommand {
        productId = requireId(productId, "productId");
        if (Objects.isNull(content) || content.length == 0)
            throw new IllegalArgumentException("[image] cannot be empty");
        if (content.length > MAX_SIZE_BYTES)
            throw new IllegalArgumentException("[image] cannot exceed 5 MB");
        if (Objects.isNull(contentType) || !ALLOWED_CONTENT_TYPES.contains(contentType))
            throw new IllegalArgumentException("[image] must be JPEG, PNG or WebP");
    }
}
