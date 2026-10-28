# Guía de construcción: Arquitectura por Capas + DDD + Bounded Contexts

**Referencia analizada:** `1ASI0729-2610-11913/therapy-connect-platform`
**Stack del repo de referencia:** Spring Boot 4.0.6 · Java 25 · PostgreSQL · Maven
**Autor de la guía:** Kevin Edu Lopez Montalvo — U20241D958

---

## 1. La idea central: no se "crean archivos", se recorre un flujo

El error típico al empezar un proyecto DDD por capas es abrir el IDE y crear carpetas vacías de `domain`, `application`, `infrastructure`, `interfaces` y luego rellenarlas al azar. Eso no funciona porque **cada archivo que creas obliga a crear el siguiente**, y el nombre del siguiente está determinado por el nombre del anterior.

El flujo real tiene una dirección fija:

```
Aggregate  →  Commands/Queries  →  Repository (puerto)
    ↓
PersistenceEntity  →  Assembler  →  PersistenceRepository  →  RepositoryImpl
    ↓
CommandService/QueryService (interfaz)  →  ...Impl
    ↓
Resources  →  Transform Assemblers  →  Controller
```

**Regla de oro:** siempre se construye de adentro hacia afuera. El `domain` no sabe que existe JPA, ni Spring, ni REST. Todo lo externo (base de datos, APIs de terceros, JWT, almacenamiento de imágenes) vive en `infrastructure` y se conecta al dominio mediante interfaces que el dominio declara.

---

## 2. Anatomía de un Bounded Context

Cada BC es una carpeta de primer nivel bajo el paquete raíz. En el repo de referencia hay 10:

```
com.open.therapyconnect.platform
├── alertmonitoring        ← BC solo-lectura (solo queries)
├── coursesandlearning
├── deliverytracking       ← BC solo-lectura
├── iam                    ← tiene ACL + seguridad
├── marketplace            ← tiene converters
├── profiles
├── progressandtracking    ← tiene events
├── scheduling
├── sessionandlive
└── shared                 ← base común, NO es un BC de negocio
```

**Observación importante:** no todos los BC tienen las mismas carpetas. `alertmonitoring` y `deliverytracking` solo tienen `queryservices` porque son de solo lectura — no tienen `commands`, ni `commandservices`, ni `POST`. **Solo se crea lo que el enunciado pide.** Crear carpetas "por completitud" es ruido.

Estructura completa de un BC con escritura:

```
<bounded-context>/
├── domain/
│   ├── model/
│   │   ├── aggregates/        Aggregate roots (POJO puro)
│   │   ├── entities/          Entidades con identidad propia (opcional)
│   │   ├── valueobjects/      Records inmutables + enums
│   │   ├── commands/          Records: Create/Update/Delete...Command
│   │   ├── queries/           Records: Get...Query
│   │   └── events/            Domain events (opcional)
│   └── repositories/          Interfaces (puertos), sin anotaciones
│
├── application/
│   ├── commandservices/       Interfaces  XxxCommandService
│   ├── queryservices/         Interfaces  XxxQueryService
│   ├── acl/                   XxxContextFacadeImpl (opcional)
│   └── internal/
│       ├── commandservices/   XxxCommandServiceImpl   @Service
│       ├── queryservices/     XxxQueryServiceImpl     @Service
│       ├── eventhandlers/     Listeners @EventListener (opcional)
│       └── outboundservices/  Puertos salientes: ACL, hashing, tokens
│
├── infrastructure/
│   └── persistence/jpa/
│       ├── entities/          XxxPersistenceEntity    @Entity
│       ├── converters/        VO de 1 campo  → AttributeConverter
│       ├── embeddables/       VO de 2+ campos → @Embeddable
│       ├── assemblers/        XxxPersistenceAssembler (estático)
│       ├── repositories/      XxxPersistenceRepository (JpaRepository)
│       └── adapters/          XxxRepositoryImpl       @Repository
│
└── interfaces/
    ├── rest/
    │   ├── XxxController.java (plural: ProductsController)
    │   ├── resources/         Records de entrada y salida
    │   └── transform/         Assemblers REST
    ├── acl/                   XxxContextFacade (interfaz, opcional)
    └── events/                Integration events (opcional)
```

---

## 3. El flujo de creación, paso a paso

Tomo como ejemplo real el aggregate `Product` del BC `marketplace`. Cada paso indica **qué archivo crear** y **qué archivo te obliga a crear después**.

### Paso 1 — `domain/model/aggregates/Product.java`

Punto de partida. Es un POJO puro: **cero anotaciones JPA**.

```java
@Getter
public class Product extends AbstractDomainAggregateRoot<Product> {

    @Setter private Long id;
    @Setter private Long catalogId;
    @Setter private String name;
    @Setter private Double price;

    // Constructor por defecto — OBLIGATORIO: el Assembler hace new Product() + setters
    public Product() {
        this.name = Strings.EMPTY;
        this.price = 0.0;
    }

    // Constructor desde command — OBLIGATORIO si hay POST
    public Product(CreateProductCommand command) {
        this.catalogId = command.catalogId();
        this.name = command.name();
        this.price = command.price();
    }

    // Método de negocio — solo si se usa
    public Product updateInformation(String name, Double price) {
        if (name != null) this.name = name;
        if (price != null) this.price = price;
        return this;
    }
}
```

**Por qué el constructor vacío es obligatorio:** el `Assembler` reconstruye el aggregate desde la base de datos con `new Product()` seguido de setters. Sin él, la reconstrucción revienta.

**Por qué extiende `AbstractDomainAggregateRoot`:** da soporte para registrar domain events sin arrastrar JPA al dominio.

### Paso 2 — Value objects que el aggregate necesita

Dos sabores, y la diferencia determina cómo se persisten después:

**Record con validación** (`domain/model/valueobjects/ProductId.java`):
```java
public record ProductId(Long value) {
    public ProductId {
        if (Objects.isNull(value))
            throw new IllegalArgumentException("[ProductId] value cannot be null");
        if (value <= 0)
            throw new IllegalArgumentException("[ProductId] value must be greater than zero");
    }
}
```

**Enum** (`domain/model/valueobjects/AvailabilityStates.java`):
```java
public enum AvailabilityStates {
    IN_STOCK(1), STOCK_ON_DEMAND(2), OUT_OF_STOCK(3);

    private final int id;
    AvailabilityStates(int id) { this.id = id; }
    public int getId() { return id; }

    public static AvailabilityStates fromValue(int id) { /* ... */ }
}
```

Los enums **no necesitan converter ni embeddable** — se mapean directo con `@Enumerated` en la persistence entity.

### Paso 3 — Commands y Queries

Todos son `record`. El nombre del aggregate manda.

| Archivo | Patrón |
|---|---|
| `CreateProductCommand.java` | `Create` + Aggregate + `Command` |
| `UpdateProductCommand.java` | `Update` + Aggregate + `Command` |
| `DeleteProductCommand.java` | `Delete` + Aggregate + `Command` |
| `GetProductByIdQuery.java` | `Get` + Aggregate + `ById` + `Query` |
| `GetAllProductsQuery.java` | `GetAll` + Aggregate**s** + `Query` |

```java
public record CreateProductCommand(
        Long catalogId, String name, String description,
        Double price, String imageUrl, String recommendedFor) {}

public record GetProductByIdQuery(Long productId) {}

public record GetAllProductsQuery() {}   // record vacío, sin parámetros
```

### Paso 4 — `domain/repositories/ProductRepository.java`

Interfaz pura, **sin `@Repository`**, sin JPA. Es el puerto que el dominio declara.

```java
public interface ProductRepository {
    Optional<Product> findById(Long id);
    List<Product> findAll();
    Product save(Product product);
    boolean existsById(Long id);
    void deleteById(Long id);
}
```

**Regla clave:** aquí solo declaras los métodos que **realmente vas a usar** en los command/query services. Si una regla de negocio necesita `existsByCode(...)`, lo declaras aquí; si no, no existe.

Con esto termina el `domain`. Nada de lo escrito hasta ahora sabe que existe una base de datos.

---

### Paso 5 — Converters y Embeddables (según el número de campos del VO)

Esta es la regla que más se equivoca:

| Campos del Value Object | Qué crear | Dónde | Cómo se usa |
|---|---|---|---|
| **1 campo** | `AttributeConverter<VO, Tipo>` | `jpa/converters/` | `@Convert(converter = ...)` |
| **2+ campos** | Clase `@Embeddable` | `jpa/embeddables/` | `@Embedded` + `@AttributeOverrides` |
| **Enum** | Nada | — | `@Enumerated` directo |

Converter real del repo (`ProductIdPersistenceConverter.java`):

```java
@Converter(autoApply = true)
public class ProductIdPersistenceConverter implements AttributeConverter<ProductId, Long> {
    @Override
    public Long convertToDatabaseColumn(ProductId productId) {
        return productId == null ? null : productId.value();
    }

    @Override
    public ProductId convertToEntityAttribute(Long dbData) {
        return dbData == null ? null : new ProductId(dbData);
    }
}
```

Embeddable (para un VO tipo `Period` con `startDate` + `endDate`):

```java
@Embeddable
public class PeriodPersistenceEmbeddable {
    @Column(name = "start_date") private LocalDate startDate;
    @Column(name = "end_date")   private LocalDate endDate;

    public PeriodPersistenceEmbeddable() {}
    public PeriodPersistenceEmbeddable(LocalDate startDate, LocalDate endDate) { /* ... */ }
    // getters y setters
}
```

**Nunca** uses `@ElementCollection` ni `@OneToOne` para un value object — genera tablas hijas innecesarias.

### Paso 6 — `infrastructure/persistence/jpa/entities/ProductPersistenceEntity.java`

Aquí sí van todas las anotaciones JPA. Extiende la clase base del `shared`, así que **no redeclara `@Id`, `createdAt` ni `updatedAt`**.

```java
@Entity
@Table(name = "products")
@Getter @Setter @NoArgsConstructor
public class ProductPersistenceEntity extends AuditableAbstractPersistenceEntity {

    @Column(nullable = false)
    private Long catalogId;

    @Column(nullable = false)
    private String name;

    private Double price;

    // Si hubiera un VO de 1 campo:
    // @Convert(converter = ProductCodePersistenceConverter.class)
    // @Column(name = "code", length = 10, nullable = false)
    // private ProductCode code;

    // Si hubiera un enum:
    // @Enumerated(EnumType.ORDINAL)
    // @Column(name = "availability", nullable = false)
    // private AvailabilityStates availability;
}
```

La clase base del `shared`:

```java
@Getter
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class AuditableAbstractPersistenceEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @CreatedDate @Column(nullable = false, updatable = false)
    private Date createdAt;

    @LastModifiedDate @Column(nullable = false)
    private Date updatedAt;

    public void setId(Long id) { this.id = id; }
}
```

Para que `@CreatedDate` y `@LastModifiedDate` funcionen, la clase `Main` debe llevar `@EnableJpaAuditing`.

### Paso 7 — `infrastructure/persistence/jpa/assemblers/ProductPersistenceAssembler.java`

El traductor entre los dos mundos. Clase **no instanciable**, métodos **estáticos**, mapeo **manual** (nada de MapStruct).

```java
public final class ProductPersistenceAssembler {

    private ProductPersistenceAssembler() {}   // no instanciable

    public static Product toDomainFromPersistence(ProductPersistenceEntity entity) {
        if (entity == null) return null;
        var product = new Product();              // ← aquí se usa el constructor vacío
        product.setId(entity.getId());
        product.setCatalogId(entity.getCatalogId());
        product.setName(entity.getName());
        product.setPrice(entity.getPrice());
        return product;
    }

    public static ProductPersistenceEntity toPersistenceFromDomain(Product product) {
        if (product == null) return null;
        var entity = new ProductPersistenceEntity();
        if (product.getId() != null) entity.setId(product.getId());  // null = INSERT nuevo
        entity.setCatalogId(product.getCatalogId());
        entity.setName(product.getName());
        entity.setPrice(product.getPrice());
        return entity;
    }
}
```

El `if (product.getId() != null)` es deliberado: si el id es `null`, JPA genera uno nuevo (INSERT). Si se asignara siempre, los INSERT fallarían.

### Paso 8 — `infrastructure/persistence/jpa/repositories/ProductPersistenceRepository.java`

Spring Data puro. A veces queda vacío:

```java
public interface ProductPersistenceRepository extends JpaRepository<ProductPersistenceEntity, Long> {}
```

Cuando el dominio pidió métodos específicos, aquí se materializan (por derivación de nombre o con `@Query`):

```java
public interface ProfilePersistenceRepository extends JpaRepository<ProfilePersistenceEntity, Long> {

    @Query("select p from ProfilePersistenceEntity p where p.emailAddress = :emailAddress")
    Optional<ProfilePersistenceEntity> findByEmailAddress(@Param("emailAddress") EmailAddress emailAddress);

    long countByEmailAddress(EmailAddress emailAddress);
}
```

### Paso 9 — `infrastructure/persistence/jpa/adapters/ProductRepositoryImpl.java`

El adaptador: implementa el puerto del dominio, delega en Spring Data y traduce con el Assembler. **Aquí se juntan las tres piezas anteriores.**

```java
@Repository
public class ProductRepositoryImpl implements ProductRepository {

    private final ProductPersistenceRepository productPersistenceRepository;

    public ProductRepositoryImpl(ProductPersistenceRepository productPersistenceRepository) {
        this.productPersistenceRepository = productPersistenceRepository;
    }

    @Override
    public Optional<Product> findById(Long id) {
        return productPersistenceRepository.findById(id)
                .map(ProductPersistenceAssembler::toDomainFromPersistence);
    }

    @Override
    public Product save(Product product) {
        var saved = productPersistenceRepository.save(
                ProductPersistenceAssembler.toPersistenceFromDomain(product));
        return ProductPersistenceAssembler.toDomainFromPersistence(saved);
    }

    @Override
    public List<Product> findAll() {
        return productPersistenceRepository.findAll().stream()
                .map(ProductPersistenceAssembler::toDomainFromPersistence)
                .toList();
    }

    @Override public boolean existsById(Long id) { return productPersistenceRepository.existsById(id); }
    @Override public void deleteById(Long id)    { productPersistenceRepository.deleteById(id); }
}
```

Con esto termina `infrastructure`. El dominio sigue sin saber que JPA existe.

---

### Paso 10 — `application/commandservices/ProductCommandService.java` (interfaz)

Un método `handle` **sobrecargado**, uno por cada command del dominio:

```java
public interface ProductCommandService {
    Result<Long, ApplicationError>    handle(CreateProductCommand command);
    Result<Product, ApplicationError> handle(UpdateProductCommand command);
    Result<Long, ApplicationError>    handle(DeleteProductCommand command);
}
```

Y `application/queryservices/ProductQueryService.java`:

```java
public interface ProductQueryService {
    Optional<Product> handle(GetProductByIdQuery query);
    List<Product>     handle(GetAllProductsQuery query);
}
```

**Por qué `Result<T, ApplicationError>` y no excepciones:** el error queda codificado en el tipo, es explícito y componible, y el controller lo traduce a HTTP sin `try/catch`. El `shared` lo define como `sealed interface` con dos records: `Success` y `Failure`.

### Paso 11 — `application/internal/commandservices/ProductCommandServiceImpl.java`

Aquí viven **las reglas de negocio** y las validaciones previas a tocar la base de datos.

```java
@Service
public class ProductCommandServiceImpl implements ProductCommandService {

    private final ProductRepository productRepository;

    public ProductCommandServiceImpl(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Override
    public Result<Long, ApplicationError> handle(CreateProductCommand command) {
        var product = new Product(command);        // ← constructor desde command
        try {
            product = productRepository.save(product);
        } catch (Exception e) {
            return Result.failure(ApplicationError.unexpected("create-product", e.getMessage()));
        }
        return Result.success(product.getId());
    }

    @Override
    public Result<Product, ApplicationError> handle(UpdateProductCommand command) {
        var result = productRepository.findById(command.productId());
        if (result.isEmpty())
            return Result.failure(ApplicationError.notFound("Product", command.productId().toString()));
        try {
            var updated = productRepository.save(
                    result.get().updateInformation(command.name(), command.price()));
            return Result.success(updated);
        } catch (Exception e) {
            return Result.failure(ApplicationError.unexpected("update-product", e.getMessage()));
        }
    }
}
```

Patrón de validación cuando hay una regla de unicidad: **consultar antes de guardar**.

```java
var emailAddress = new EmailAddress(command.email());
if (profileRepository.existsByEmailAddress(emailAddress)) {
    return Result.failure(ApplicationError.conflict(
            "Profile", "A profile with email address '%s' already exists".formatted(command.email())));
}
```

Ese `existsByEmailAddress` es exactamente el método que en el **Paso 4** declaraste en el puerto del dominio. Por eso el flujo es circular en la práctica: una regla de negocio aquí te obliga a volver a `domain/repositories` y agregar el método.

Fábricas de error disponibles en `ApplicationError`: `validationError`, `notFound`, `businessRuleViolation`, `conflict`, `unexpected`.

### Paso 12 — `application/internal/queryservices/ProductQueryServiceImpl.java`

Casi siempre es delegación directa:

```java
@Service
public class ProductQueryServiceImpl implements ProductQueryService {

    private final ProductRepository productRepository;

    public ProductQueryServiceImpl(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Override public Optional<Product> handle(GetProductByIdQuery query) {
        return productRepository.findById(query.productId());
    }

    @Override public List<Product> handle(GetAllProductsQuery query) {
        return productRepository.findAll();
    }
}
```

---

### Paso 13 — `interfaces/rest/resources/` (records de entrada y salida)

Dos reglas de nombrado que se aplican en paralelo:

| Origen | Resource que se crea | Para qué |
|---|---|---|
| `CreateProductCommand` | `CreateProductResource` | Body del POST |
| `UpdateProductCommand` | `UpdateProductResource` | Body del PUT |
| Aggregate `Product` | `ProductResource` | Respuesta al cliente |

```java
public record CreateProductResource(
        Long catalogId, String name, String description,
        Double price, String imageUrl, String recommendedFor) {}

public record ProductResource(
        Long id, Long catalogId, String name, String description,
        Double price, String imageUrl, String recommendedFor) {}
```

Si el enunciado pide validación e i18n, aquí van las anotaciones Jakarta con claves de mensaje:

```java
public record CreateProfileResource(
    @NotBlank(message = "{validation.not-blank}")
    @Schema(description = "Profile first name", example = "John")
    String firstName,

    @NotBlank(message = "{validation.not-blank}")
    @Email(message = "{validation.email}")
    String email
) {}
```

Las claves `{validation.not-blank}` se resuelven contra `messages.properties` / `messages_es.properties` según el header `Accept-Language`.

### Paso 14 — `interfaces/rest/transform/` (assemblers REST)

Dos direcciones, dos nombres:

| Dirección | Nombre del archivo | Método |
|---|---|---|
| Resource → Command | `CreateProductCommandFromResourceAssembler` | `toCommandFromResource(...)` |
| Aggregate → Resource | `ProductResourceFromEntityAssembler` | `toResourceFromEntity(...)` |

```java
public class CreateProductCommandFromResourceAssembler {
    public static CreateProductCommand toCommandFromResource(CreateProductResource resource) {
        return new CreateProductCommand(
                resource.catalogId(), resource.name(), resource.description(),
                resource.price(), resource.imageUrl(), resource.recommendedFor());
    }
}

public class ProductResourceFromEntityAssembler {
    public static ProductResource toResourceFromEntity(Product product) {
        return new ProductResource(
                product.getId(), product.getCatalogId(), product.getName(),
                product.getDescription(), product.getPrice(),
                product.getImageUrl(), product.getRecommendedFor());
    }
}
```

**Para un `Update` el assembler recibe dos parámetros** — el id viene del path, el resto del body:

```java
public static UpdateProductCommand toCommandFromResource(Long productId, UpdateProductResource resource) {
    return new UpdateProductCommand(productId, resource.name(), resource.price());
}
```

### Paso 15 — `interfaces/rest/ProductsController.java`

Último archivo. URL en **minúscula y plural**. Nombre de clase en plural.

```java
@RestController
@RequestMapping(value = "/api/v1/products", produces = APPLICATION_JSON_VALUE)
@Tag(name = "Products", description = "Product management endpoints")
public class ProductsController {

    private final ProductCommandService productCommandService;
    private final ProductQueryService productQueryService;

    public ProductsController(ProductCommandService productCommandService,
                              ProductQueryService productQueryService) {
        this.productCommandService = productCommandService;
        this.productQueryService = productQueryService;
    }

    @PostMapping
    @Operation(summary = "Create a new product")
    public ResponseEntity<?> createProduct(@Valid @RequestBody CreateProductResource resource) {
        var command = CreateProductCommandFromResourceAssembler.toCommandFromResource(resource);
        var result = productCommandService.handle(command)
                .flatMap(productId -> productQueryService.handle(new GetProductByIdQuery(productId))
                        .<Result<Product, ApplicationError>>map(Result::success)
                        .orElseGet(() -> Result.failure(
                                ApplicationError.notFound("Product", productId.toString()))));
        return ResponseEntityAssembler.toResponseEntityFromResult(
                result, ProductResourceFromEntityAssembler::toResourceFromEntity, HttpStatus.CREATED);
    }

    @GetMapping("/{productId}")
    @Operation(summary = "Get product by ID")
    public ResponseEntity<ProductResource> getProductById(@PathVariable Long productId) {
        var product = productQueryService.handle(new GetProductByIdQuery(productId));
        if (product.isEmpty()) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(ProductResourceFromEntityAssembler.toResourceFromEntity(product.get()));
    }
}
```

**Detalle que vale la pena señalar:** el `CreateProductCommand` devuelve solo el `Long` del id. El controller entonces **encadena un query** con `flatMap` para recuperar el aggregate completo y poder devolverlo en el response del POST. Ese es el motivo por el que, aunque el enunciado solo pida un POST, igual se implementa un `GetByIdQuery` — sirve para armar la respuesta del POST.

---

## 4. Dónde va lo externo (APIs de terceros, Cloudinary, Google, SMTP)

Esta es la parte que el repo de referencia no ejercita mucho, pero la regla se deduce con claridad de cómo está organizado `iam`:

> **Todo lo que hable con el mundo exterior vive en `infrastructure`. El dominio nunca lo conoce; lo usa a través de una interfaz que el propio proyecto declara.**

En `iam` se ve el patrón exacto aplicado a hashing y tokens:

```
iam/application/internal/outboundservices/hashing/HashingService.java    ← INTERFAZ (puerto)
iam/application/internal/outboundservices/tokens/TokenService.java       ← INTERFAZ (puerto)

iam/infrastructure/hashing/bcrypt/services/HashingServiceImpl.java       ← IMPLEMENTACIÓN
iam/infrastructure/tokens/jwt/services/TokenServiceImpl.java             ← IMPLEMENTACIÓN
```

El `UserCommandServiceImpl` inyecta `HashingService` (la interfaz), no BCrypt. Si mañana cambias BCrypt por Argon2, el `application` no se entera.

**Aplicando la misma regla a integraciones externas:**

| Integración | Puerto (interfaz) | Implementación |
|---|---|---|
| Subida de imágenes (Cloudinary) | `application/internal/outboundservices/storage/ImageStorageService.java` | `infrastructure/storage/cloudinary/services/CloudinaryImageStorageServiceImpl.java` |
| Google Maps / Geocoding | `application/internal/outboundservices/geocoding/GeocodingService.java` | `infrastructure/geocoding/google/services/GoogleGeocodingServiceImpl.java` |
| Envío de correo | `application/internal/outboundservices/mail/MailService.java` | `infrastructure/mail/smtp/services/SmtpMailServiceImpl.java` |
| Notificaciones push (FCM) | `application/internal/outboundservices/notifications/NotificationService.java` | `infrastructure/notifications/fcm/services/FcmNotificationServiceImpl.java` |

El patrón de carpeta en `infrastructure` es consistente: `infrastructure/<capacidad>/<proveedor>/services/`. La capacidad es el "qué" (hashing, tokens, storage), el proveedor es el "con qué" (bcrypt, jwt, cloudinary, google).

**Las credenciales nunca van en el código** — entran por `application.properties` leyendo variables de entorno, y la clase de configuración que construye el cliente va en `infrastructure/<capacidad>/<proveedor>/configuration/`.

---

## 5. El Bounded Context `shared`

No es un BC de negocio: es la base común que los demás extienden. En el repo de referencia contiene:

```
shared/
├── domain/model/aggregates/
│   └── AbstractDomainAggregateRoot.java       base de TODOS los aggregates
├── application/result/
│   ├── Result.java                            sealed interface Success | Failure
│   └── ApplicationError.java                  fábricas de error tipadas
├── infrastructure/
│   ├── persistence/jpa/entities/
│   │   └── AuditableAbstractPersistenceEntity.java   @Id + createdAt + updatedAt
│   ├── persistence/jpa/configuration/strategy/
│   │   └── SnakeCaseWithPluralizedTablePhysicalNamingStrategy.java
│   ├── documentation/openapi/configuration/
│   │   └── OpenApiConfiguration.java           config de Swagger
│   ├── i18n/configuration/
│   │   └── LocaleConfiguration.java            resolución de Accept-Language
│   └── web/configuration/
│       └── TomcatConfiguration.java
└── interfaces/rest/
    ├── GlobalExceptionHandler.java             @RestControllerAdvice
    ├── resources/{ErrorResource, MessageResource}.java
    └── transform/{ResponseEntityAssembler, ErrorResponseAssembler}.java
```

**Regla de uso en un examen:** el `shared` se recibe hecho y **no se rediseña**. Si el enunciado pide un value object compartido entre BCs, se **agrega**:

1. El record en `shared/domain/model/valueobjects/`
2. Su converter o embeddable en `shared/infrastructure/persistence/jpa/converters|embeddables/`
3. Se **referencia** desde la persistence entity del BC que lo usa

El `SnakeCaseWithPluralizedTablePhysicalNamingStrategy` es el que convierte automáticamente `ProductPersistenceEntity` → tabla `products` y `catalogId` → columna `catalog_id`. Por eso las entities casi no necesitan `@Table(name=...)` ni `@Column(name=...)` explícitos.

---

## 6. Comunicación entre Bounded Contexts

Dos mecanismos, dos propósitos distintos.

### ACL (Anti-Corruption Layer) — llamada síncrona

Se usa cuando un BC necesita **preguntarle algo a otro ahora mismo** y esperar respuesta.

```
<bc-emisor>/interfaces/acl/XxxContextFacade.java         ← la cara pública del BC
<bc-emisor>/application/acl/XxxContextFacadeImpl.java    ← implementación  @Service
<bc-consumidor>/application/internal/outboundservices/acl/ExternalXxxService.java
```

El `ExternalXxxService` es el envoltorio del lado consumidor: traduce los tipos primitivos que devuelve el facade a los value objects del BC que consume.

```java
@Service
public class ExternalProfileService {
    private final ProfilesContextFacade profilesContextFacade;

    public Optional<ProfileId> fetchProfileByEmail(String email) {
        var profileId = profilesContextFacade.fetchProfileIdByEmail(email);
        return profileId == 0L ? Optional.empty() : Optional.of(new ProfileId(profileId));
    }
}
```

**Por qué el facade devuelve `0L` en vez de `null` u `Optional`:** mantiene la frontera entre contextos con tipos primitivos, sin filtrar el modelo interno. El envoltorio del consumidor traduce eso a `Optional` en su propio lenguaje.

### Eventos — notificación asíncrona

Se usa cuando algo pasó y otros deben enterarse, sin acoplar quién escucha.

| Tipo | Dónde se declara | Quién lo escucha |
|---|---|---|
| **Domain event** | `<bc>/domain/model/events/` | El mismo BC |
| **Integration event** | `<bc>/interfaces/events/` | Otros BCs |

```java
// 1. Domain event (interno del BC)
public record NoteCreatedEvent(Note note) {}

// 2. El aggregate lo registra
public void onCreated() {
    registerDomainEvent(NoteCreatedEvent.from(this));
}

// 3. El repository adapter lo publica DESPUÉS de persistir
@Override
public Note save(Note note) {
    boolean isNew = note.getId() == null;
    var saved = NotePersistenceAssembler.toDomainFromPersistence(
            notePersistenceRepository.save(NotePersistenceAssembler.toPersistenceFromDomain(note)));
    if (isNew) {
        saved.onCreated();
        saved.domainEvents().forEach(eventPublisher::publishEvent);
        saved.clearDomainEvents();
    }
    return saved;
}

// 4. El handler del mismo BC reacciona
@Service
public class NoteCreatedEventHandler {
    @EventListener
    public void on(NoteCreatedEvent event) { /* ... */ }
}
```

Si otro BC debe enterarse, el handler del BC emisor **traduce** el domain event a un integration event y lo republica. El BC receptor escucha el **integration event**, nunca el domain event interno.

> **Nota honesta sobre el repo de referencia:** `progressandtracking` declara 5 domain events (`NoteCreatedEvent`, `NoteUpdatedEvent`, `NoteDeletedEvent`, `ParentNoteCreatedEvent`, `TeacherNoteCreatedEvent`) pero sus repository adapters **no tienen cableado el `ApplicationEventPublisher`** — los eventos están declarados y no se publican. El patrón completo descrito arriba corresponde a las convenciones del curso, no a lo que ese repo ejercita hoy.

**Dos handlers con el mismo nombre en BCs distintos:** si el emisor y el receptor tienen ambos una clase `ProfileCreatedEventHandler`, hay que darle nombre explícito al bean para que Spring los distinga:

```java
@Service("profilesProfileCreatedEventHandler")   // en el emisor
@Service("learningProfileCreatedEventHandler")   // en el receptor
```

---

## 7. Data Seeding

Para poblar la base de datos al arrancar. Tres archivos, siempre dentro del BC — **nunca en la clase `Main`**.

```java
// 1. <bc>/domain/model/commands/SeedProductsCommand.java
public record SeedProductsCommand() {}

// 2. <bc>/application/internal/commandservices/ProductCommandServiceImpl.java
@Override
public void handle(SeedProductsCommand command) {
    if (this.productRepository.count() == 0) {
        List.of(
            new Product(new ProductCode("AV-OAT-001"), "Rolled Oats", 40.0),
            new Product(new ProductCode("AV-BER-002"), "Blueberries", 100.0)
        ).forEach(this.productRepository::save);
    }
}

// 3. <bc>/application/internal/eventhandlers/ApplicationReadyEventHandler.java
@Service
@Slf4j
public class ApplicationReadyEventHandler {
    private final ProductCommandService productCommandService;

    @EventListener
    public void on(ApplicationReadyEvent event) {
        productCommandService.handle(new SeedProductsCommand());
    }
}
```

Dos variantes según el origen de los datos:

| Origen | Chequeo |
|---|---|
| Lista fija de aggregates | `if (repository.count() == 0)` + `forEach(save)` |
| Enum del código (`Roles.values()`) | `if (!repository.existsByX(elem))` por cada valor |

La diferencia importa: un enum puede crecer entre releases, así que hay que insertar solo los faltantes. Una lista fija se inserta entera o no se inserta.

El `count()` hay que **declararlo en el puerto del dominio** e **implementarlo en el adapter** delegando a `JpaRepository.count()`. Otro caso donde una necesidad aguas abajo te obliga a volver al `domain`.

---

## 8. Tabla maestra de nomenclatura

Si el aggregate se llama `Product`, todo lo demás se deriva mecánicamente:

| Capa | Carpeta | Archivo |
|---|---|---|
| domain | `model/aggregates/` | `Product.java` |
| domain | `model/valueobjects/` | `ProductId.java`, `AvailabilityStates.java` |
| domain | `model/commands/` | `CreateProductCommand.java` |
| domain | `model/queries/` | `GetProductByIdQuery.java`, `GetAllProductsQuery.java` |
| domain | `model/events/` | `ProductCreatedEvent.java` |
| domain | `repositories/` | `ProductRepository.java` |
| infrastructure | `jpa/converters/` | `ProductIdPersistenceConverter.java` |
| infrastructure | `jpa/embeddables/` | `ProductDimensionsPersistenceEmbeddable.java` |
| infrastructure | `jpa/entities/` | `ProductPersistenceEntity.java` |
| infrastructure | `jpa/assemblers/` | `ProductPersistenceAssembler.java` |
| infrastructure | `jpa/repositories/` | `ProductPersistenceRepository.java` |
| infrastructure | `jpa/adapters/` | `ProductRepositoryImpl.java` |
| application | `commandservices/` | `ProductCommandService.java` |
| application | `queryservices/` | `ProductQueryService.java` |
| application | `internal/commandservices/` | `ProductCommandServiceImpl.java` |
| application | `internal/queryservices/` | `ProductQueryServiceImpl.java` |
| application | `internal/eventhandlers/` | `ProductCreatedEventHandler.java` |
| application | `internal/outboundservices/acl/` | `ExternalProductService.java` |
| application | `acl/` | `MarketplaceContextFacadeImpl.java` |
| interfaces | `rest/resources/` | `CreateProductResource.java`, `ProductResource.java` |
| interfaces | `rest/transform/` | `CreateProductCommandFromResourceAssembler.java` |
| interfaces | `rest/transform/` | `ProductResourceFromEntityAssembler.java` |
| interfaces | `rest/` | `ProductsController.java` *(plural)* |
| interfaces | `acl/` | `MarketplaceContextFacade.java` |
| interfaces | `events/` | `ProductCreatedIntegrationEvent.java` |

Convenciones transversales:
- Clases en `UpperCamelCase`, atributos y métodos en `lowerCamelCase`
- Tablas en **plural y snake_case** (lo hace automático el naming strategy)
- URLs en **minúscula y plural**: `/api/v1/products`
- Todo texto de mensajes y JavaDoc en **inglés**
- `@author` con nombre y apellidos en las clases relevantes

---

## 9. Checklist de verificación

Antes de dar por terminado un aggregate, comprobar:

- [ ] El aggregate **no tiene ninguna anotación JPA** (`@Entity`, `@Id`, `@Column`)
- [ ] El aggregate tiene **constructor vacío** (lo usa el Assembler) y **constructor desde command**
- [ ] El aggregate extiende `AbstractDomainAggregateRoot<T>`
- [ ] Cada value object de **1 campo** tiene su `AttributeConverter`
- [ ] Cada value object de **2+ campos** tiene su `@Embeddable`
- [ ] Los **enums** no tienen converter ni embeddable (van con `@Enumerated`)
- [ ] La persistence entity extiende `AuditableAbstractPersistenceEntity` y **no redeclara** `@Id`, `createdAt`, `updatedAt`
- [ ] El Assembler es `final`, con constructor privado y métodos estáticos
- [ ] El Assembler hace `if (id != null)` antes de asignar el id en `toPersistenceFromDomain`
- [ ] El domain repository **no tiene `@Repository`**; el adapter **sí**
- [ ] Cada método del domain repository **se usa realmente** en algún service
- [ ] Los command services devuelven `Result<T, ApplicationError>`, no lanzan excepciones
- [ ] Hay un Resource por cada command y uno por el aggregate
- [ ] Hay un assembler REST por cada Resource
- [ ] La URL del controller está en minúscula y plural
- [ ] La clase `Main` lleva `@EnableJpaAuditing`
- [ ] Todo lo que habla con un servicio externo está en `infrastructure`, detrás de una interfaz
- [ ] No se creó ninguna carpeta ni archivo que el enunciado no pida

---

## 10. Resumen en una frase

> Se empieza por el **aggregate** (POJO puro), se derivan sus **commands, queries y puerto de repositorio**; luego se baja a **infrastructure** para traducirlo a JPA (converter/embeddable → entity → assembler → repository → adapter); se sube a **application** para la lógica de negocio (interfaz → impl con `Result`); y se termina en **interfaces** con los resources, los assemblers REST y el controller. Lo externo —base de datos, APIs de terceros, tokens, imágenes— siempre entra por `infrastructure` detrás de una interfaz que el proyecto declara, nunca directo en el dominio.
