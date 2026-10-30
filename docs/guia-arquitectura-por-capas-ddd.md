# Guía de construcción: Arquitectura por Capas + DDD + Bounded Contexts

**Referencias analizadas:**
- Flujo base del aggregate (secciones 1-3, 5-10): `1ASI0729-2610-11913/therapy-connect-platform` — Spring Boot 4.0.6 · Java 25 · PostgreSQL
- Patrones reales de integraciones externas y organización fina (sección 4 y 11): `Aplicaciones-Dispositivos-Moviles/SkillSwap-WebServices-Java` (rama `develop`) — backend en producción con 9 Bounded Contexts, Gemini, Cloudinary, Firebase, Brevo y RevenueCat ya integrados

**Autor de la guía:** Kevin Edu Lopez Montalvo — U20241D958

**Uso:** esta guía es de referencia general, para aplicar en cualquier proyecto académico con arquitectura por capas + DDD, no para un enunciado de examen puntual.

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

**Regla de oro:** siempre se construye de adentro hacia afuera. El `domain` no sabe que existe JPA, ni Spring, ni REST. Todo lo externo (base de datos, APIs de terceros, JWT, almacenamiento de imágenes) vive en `infrastructure` y se conecta al dominio/aplicación mediante interfaces que el propio proyecto declara.

---

## 2. Anatomía de un Bounded Context

Cada BC es una carpeta de primer nivel bajo el paquete raíz. En el repo de referencia hay 10:

```
com.open.therapyconnect.platform
├── alertmonitoring        ← BC solo-lectura (solo queries)
├── coursesandlearning
├── deliverytracking       ← BC solo-lectura
├── iam                    ← tiene ACL + seguridad
├── marketplace             ← tiene converters
├── profiles
├── progressandtracking    ← tiene events
├── scheduling
├── sessionandlive
└── shared                 ← base común, NO es un BC de negocio
```

**Observación importante:** no todos los BC tienen las mismas carpetas. `alertmonitoring` y `deliverytracking` solo tienen `queryservices` porque son de solo lectura — no tienen `commands`, ni `commandservices`, ni `POST`. **Solo se crea lo que el proyecto necesita.** Crear carpetas "por completitud" es ruido.

Estructura completa de un BC con escritura, integraciones externas y background jobs:

```
<bounded-context>/
├── domain/
│   ├── model/
│   │   ├── aggregates/        Aggregate roots (POJO puro)
│   │   ├── entities/          Entidades con identidad propia (opcional, solo si hay hijos reales)
│   │   ├── valueobjects/      Records inmutables + enums
│   │   ├── commands/          Records: Create/Update/Delete...Command
│   │   ├── queries/           Records: Get...Query
│   │   └── events/            Domain events (opcional)
│   ├── repositories/          Interfaces (puertos) de persistencia, sin anotaciones
│   └── services/              Puertos + lógica de dominio pura (ver 4.4)
│
├── application/
│   ├── commandservices/       Interfaces  XxxCommandService
│   ├── queryservices/         Interfaces  XxxQueryService
│   ├── acl/                   XxxContextFacadeImpl (opcional, lado emisor — ver 11.1)
│   └── internal/
│       ├── commandservices/   XxxCommandServiceImpl   @Service
│       ├── queryservices/     XxxQueryServiceImpl     @Service
│       ├── eventhandlers/     Listeners @EventListener (opcional)
│       ├── outboundservices/  Puertos salientes de aplicación (ver 4.4 y 11.2)
│       └── emails/ (u otro)   Lógica de "contenido" cuando aplica (ver 4.5)
│
├── infrastructure/
│   ├── persistence/jpa/
│   │   ├── entities/          XxxPersistenceEntity    @Entity
│   │   ├── converters/        VO de 1 campo  → AttributeConverter
│   │   ├── embeddables/       VO de 2+ campos → @Embeddable
│   │   ├── assemblers/        XxxPersistenceAssembler (estático)
│   │   ├── repositories/      XxxPersistenceRepository (JpaRepository)
│   │   └── adapters/          XxxRepositoryImpl       @Repository
│   ├── <capacidad>/[<proveedor>/]   Integraciones externas (ver sección 4 completa)
│   ├── config/                 @Configuration del BC
│   ├── scheduling/             @Scheduled jobs (opcional, ver 4.6)
│   └── startup/                Tareas al arrancar la app, @EventListener(ApplicationReadyEvent) (opcional, ver 4.6)
│
└── interfaces/
    ├── rest/
    │   ├── XxxController.java (plural: ProductsController)
    │   ├── resources/         Records de entrada y salida
    │   └── transform/         Assemblers REST
    ├── acl/                   XxxContextFacade (interfaz, opcional, lado emisor — ver 11.1)
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

Si el proyecto pide validación e i18n, aquí van las anotaciones Jakarta con claves de mensaje:

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

**Detalle que vale la pena señalar:** el `CreateProductCommand` devuelve solo el `Long` del id. El controller entonces **encadena un query** con `flatMap` para recuperar el aggregate completo y poder devolverlo en el response del POST. Ese es el motivo por el que, aunque solo se necesite un POST, igual se implementa un `GetByIdQuery` — sirve para armar la respuesta del POST.

---

## 4. Dónde va lo externo (APIs de terceros, Cloudinary, Google, SMTP, pagos...)

La regla general:

> **Todo lo que hable con el mundo exterior vive en `infrastructure`. El dominio nunca lo conoce; la aplicación tampoco conoce la clase concreta del proveedor. Ambos lo usan a través de una interfaz que el propio proyecto declara.**

### 4.1 — Regla para decidir si va plano o con subcarpeta de proveedor

No es 100% consistente ni siquiera en un repo real en producción (ningún repo de un equipo lo es), pero el patrón dominante es este:

- **Usa subcarpeta `<capacidad>/<proveedor>/`** cuando:
    1. **Coexisten dos o más implementaciones** del mismo puerto — el caso más claro: una implementación "real" (Brevo, Firebase, RevenueCat) y una de respaldo/pruebas (logging, simulada) que se activa por perfil de Spring.
    2. **El nombre de la capacidad es genérico y reutilizable** entre proyectos distintos — `hashing` y `tokens` son conceptos que cualquier backend necesita, así que aunque hoy solo haya un proveedor (BCrypt, JWT), se deja la subcarpeta como punto de extensión explícito (mañana podría ser Argon2 o un token opaco).
- **Déjalo plano** (`<capacidad>/Xxx.java` directo, sin subcarpeta) cuando hay un único proveedor y el nombre de la capacidad ya es específico del dominio del proyecto — una capacidad como `ai` (un solo modelo de IA), `filestorage` (un solo proveedor de almacenamiento) o `taxonomy` (datos locales) normalmente no se va a duplicar con otro proveedor en el mismo proyecto, así que la subcarpeta sería ceremonia sin beneficio.

Si tienes dudas: **si vas a necesitar un fallback o una versión simulada para pruebas, usa subcarpeta desde el principio.** Si es un único proveedor y no vas a cambiarlo, plano está bien.

Ejemplo real de capacidad con subcarpeta de proveedor (`iam`, hashing y tokens):

```
iam/application/internal/outboundservices/hashing/HashingService.java    ← INTERFAZ (puerto)
iam/application/internal/outboundservices/tokens/TokenService.java       ← INTERFAZ (puerto)

iam/infrastructure/hashing/bcrypt/HashingServiceImpl.java                ← IMPLEMENTACIÓN
iam/infrastructure/tokens/jwt/TokenServiceImpl.java                      ← IMPLEMENTACIÓN
```

El `UserCommandServiceImpl` inyecta `HashingService` (la interfaz), nunca `BCrypt` directamente. Si mañana cambias BCrypt por Argon2, el `application` no se entera.

### 4.2 — Dónde vive la configuración (`Settings`)

También varía, y tiene una razón:

- Cuando hay **selección entre proveedores** (ej. decidir si se usa un envío real o solo loguear en consola), el flag de selección vive en la **raíz de la capacidad**: `infrastructure/email/EmailSettings.java`, `infrastructure/push/FirebaseSettings.java` — son genéricos a la capacidad, no a un proveedor puntual.
- Cuando la configuración es **específica de un proveedor** (API key, timeout, secreto), vive **dentro** de la subcarpeta de ese proveedor: `infrastructure/tokens/jwt/TokenSettings.java`, `infrastructure/payments/revenuecat/RevenueCatSettings.java`.
- Cuando no hay selección de proveedor (solo uno existe), la `Settings` vive plana junto a la implementación: `infrastructure/filestorage/CloudinarySettings.java`, `infrastructure/ai/GeminiSettings.java`.

### 4.3 — El patrón completo con un ejemplo real: Cloudinary

Tomando una subida de certificados a Cloudinary como ejemplo end-to-end:

```
credentialverification/
├── application/internal/outboundservices/
│   └── FileStorageService.java            ← PUERTO (interfaz), declarado por la aplicación
│
└── infrastructure/filestorage/
    ├── CloudinarySettings.java            ← @ConfigurationProperties (API key, cloud name...)
    └── CloudinaryStorageService.java      ← IMPLEMENTACIÓN, @Service, implementa FileStorageService
```

El command service inyecta `FileStorageService` (la interfaz), nunca `CloudinaryStorageService` directamente. Si mañana cambian Cloudinary por S3, solo se toca `infrastructure/filestorage/`; `application` y `domain` no se enteran. Lo único que cambia caso a caso es **en qué carpeta vive el puerto** (`application/internal/outboundservices` aquí; en otros casos `domain/services`, ver 4.4) y **si hay subcarpeta de proveedor** (aquí no, porque solo hay un proveedor).

### 4.4 — La regla decisiva: ¿el puerto va en `domain/services` o en `application/internal/outboundservices`?

Esta es la pregunta que más confunde, porque hay integraciones externas "serias" (pagos, IA) que terminan en `domain/services`, y otras más simples (tokens, webhooks) que terminan en `application/outboundservices`. No es azar — hay un criterio.

**La pregunta de una sola línea para decidir sin dudar**, antes de crear el puerto:

> *"Si cambia el proveedor/mecanismo de [capacidad], ¿cambia algo en cómo describirías el negocio del aggregate, o solo cambia cómo está implementado por dentro?"*

- Si completas **"le importaría al negocio"** → `domain/services`. Es una capacidad tan central que, sin ella, el aggregate pierde su razón de ser (cobrar una suscripción, evaluar un proyecto, hashear una contraseña, saber qué skills existen).
- Si completas **"no le importaría"** → `application/internal/outboundservices`. Es un detalle técnico/de orquestación intercambiable sin que el negocio lo note (cómo se identifica una sesión, cómo se valida una firma HTTP, cómo se transporta un mensaje).

Casos resueltos con esta regla:

| Puerto | Dónde va | Por qué |
|---|---|---|
| `PasswordHasher` | `domain/services` | Un usuario no puede autenticarse de forma segura sin esto — es parte de lo que significa "crear una cuenta". |
| `PaymentGateway` (cobrar una suscripción) | `domain/services` | Una `Subscription` sin poder cobrarse no es una suscripción, es un registro vacío. El negocio sí tiene opinión sobre reintentos, qué pasa si falla, etc. |
| Evaluar/generar contenido con IA (ej. evaluar un mini-proyecto) | `domain/services` | Evaluar es la razón de ser de ese aggregate, aunque por debajo llame a un modelo de IA de terceros. |
| `SkillTaxonomy` / catálogo de habilidades | `domain/services` | El aggregate no puede construirse sin saber qué opciones existen — es insumo de una regla de negocio. |
| `TokenGenerator` (token de sesión, para el login) | `application/outboundservices` | Emitir un JWT es un detalle de cómo respondes al login, no una regla de negocio del usuario. |
| `WebhookAuthorizationVerifier` | `application/outboundservices` | Validar una firma HTTP es seguridad/plomería; si cambia el mecanismo de firma, a nadie del negocio le importa. |
| `EmailSender` / `PushNotificationSender` | `application/outboundservices` | El *envío* es un detalle de entrega, distinto de la decisión de negocio de *qué* comunicar (ver 4.5). |

Y los servicios 100% puros, sin ninguna dependencia externa (un validador de formato, una política de puntuación), también caen naturalmente en `domain/services` — ahí no hay ni siquiera duda, porque no hay proveedor que intercambiar.

**No dejes la decisión 50/50.** Si dudas, es señal de que todavía no tienes claro qué hace esa capacidad por el negocio — resuélvelo ahí, antes de crear la carpeta, respondiendo la pregunta en voz alta.

### 4.5 — Por qué una misma integración a veces está partida entre `infrastructure` y `application`

Pasa cuando una integración tiene tanto **"qué se envía/genera"** (depende de datos y reglas del dominio) como **"cómo se transporta/entrega"** (un detalle de infraestructura intercambiable). El caso típico es el correo:

```
application/internal/emails/
└── VerificationEmailComposer.java   ← decide el CONTENIDO del correo

infrastructure/email/
├── EmailSettings.java                ← selecciona el proveedor
├── brevo/BrevoEmailSenderAdapter.java       ← decide el TRANSPORTE (proveedor real)
└── logging/LoggingEmailSenderAdapter.java   ← decide el TRANSPORTE (solo loguea, para dev)
```

No es la misma responsabilidad repetida en dos capas — son dos responsabilidades distintas que suenan parecido porque ambas tienen la palabra "email":

- **El composer (application)** decide **qué dice** el correo: asunto, cuerpo, link, idioma — depende del estado del negocio (nombre del usuario, token, idioma). Es una regla de aplicación, no de transporte. Devuelve un DTO plano (`EmailMessage`), sin saber nada de SMTP ni del proveedor.
- **El sender + adaptadores (infrastructure)** deciden **cómo se entrega** ese mensaje ya armado. No les importa qué dice, solo cómo transportarlo a su destino.

**La regla generalizable:** cuando una integración mezcla "qué se envía" (depende del dominio) con "cómo se transporta" (intercambiable), sepáralas: el "qué" va en `application/internal/<algo>` (o directamente inline en el command service si es simple), y el "cómo" va en el puerto + adaptador de `infrastructure/<capacidad>/`. Si hoy el contenido es simple y se arma inline en el command service, no hace falta crear esa carpeta de composer todavía — créala el día que el contenido se vuelva lo bastante complejo como para merecer su propia clase.

### 4.6 — Otras carpetas de `infrastructure` que no son ni base de datos ni API externa

`infrastructure` no es "la carpeta de las APIs externas" — es la carpeta de **cualquier decisión técnica concreta que el dominio no debería conocer**, llame o no a internet:

- `infrastructure/scheduling/` — jobs con `@Scheduled` (ej. un reasignador de casos vencidos, un expirador de suscripciones). Un cronjob no habla con un proveedor externo, pero sigue siendo "mecanismo", no regla de negocio — por eso vive en infraestructura y no en `application`.
- `infrastructure/startup/` — tareas que corren una vez al arrancar la app con `@EventListener(ApplicationReadyEvent.class)` (ej. retomar evaluaciones que quedaron a medias si el servidor se cayó). Es infraestructura porque depende del ciclo de vida de Spring, no del dominio.
- `infrastructure/security/` — filtros y configuración de Spring Security (`JwtAuthenticationFilter`, `SecurityConfig`), separado de `tokens/jwt/` (que solo genera/valida el JWT en sí).
- `infrastructure/taxonomy/` (o similar) — confirma que "infraestructura" no es sinónimo de "llama a internet": leer un archivo JSON local también es infraestructura, porque es un detalle concreto e intercambiable (mañana podría venir de una base de datos o de una API), y se trata igual, detrás de un puerto de dominio.

El dominio y la aplicación solo conocen interfaces; `infrastructure` es donde se decide el "cómo" real, sea cual sea: una librería de terceros, un archivo local, un cronjob, un filtro HTTP, un listener de arranque.

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

El `SnakeCaseWithPluralizedTablePhysicalNamingStrategy` es el que convierte automáticamente `ProductPersistenceEntity` → tabla `products` y `catalogId` → columna `catalog_id`. Por eso las entities casi no necesitan `@Table(name=...)` ni `@Column(name=...)` explícitos.

Si un value object debe compartirse entre varios BCs, se agrega en `shared/domain/model/valueobjects/` (con su converter/embeddable correspondiente en `shared/infrastructure/persistence/...`) y se referencia desde la persistence entity de cada BC que lo use.

---

## 6. Comunicación entre Bounded Contexts

Dos mecanismos, dos propósitos distintos.

### ACL (Anti-Corruption Layer) — llamada síncrona

Se usa cuando un BC necesita **preguntarle algo a otro ahora mismo** y esperar respuesta. El detalle fino de dónde vive cada pieza del lado consumidor está en la **sección 11.1**; el esqueleto general es:

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

**Para que el `eventPublisher.publishEvent(...)` del paso 3 funcione de verdad** (y no se quede solo declarado sin dispararse), el proyecto necesita un publicador real cableado — típicamente una clase en `shared/infrastructure/events/` que implementa el publicador de dominio sobre `ApplicationEventPublisher` de Spring. Sin esa pieza, los eventos quedan como código muerto: se registran pero nadie los dispara.

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
| domain | `services/` (si el puerto es "central", ver 4.4) | `ProductXxxService.java` |
| infrastructure | `jpa/converters/` | `ProductIdPersistenceConverter.java` |
| infrastructure | `jpa/embeddables/` | `ProductDimensionsPersistenceEmbeddable.java` |
| infrastructure | `jpa/entities/` | `ProductPersistenceEntity.java` |
| infrastructure | `jpa/assemblers/` | `ProductPersistenceAssembler.java` |
| infrastructure | `jpa/repositories/` | `ProductPersistenceRepository.java` |
| infrastructure | `jpa/adapters/` | `ProductRepositoryImpl.java` |
| infrastructure | `<capacidad>/[<proveedor>/]` (ver sección 4) | `GeminiXxx.java`, `CloudinaryXxx.java`, etc. |
| infrastructure | `scheduling/` \| `startup/` | `XxxScheduler.java`, `XxxRecovery.java` |
| application | `commandservices/` | `ProductCommandService.java` |
| application | `queryservices/` | `ProductQueryService.java` |
| application | `internal/commandservices/` | `ProductCommandServiceImpl.java` |
| application | `internal/queryservices/` | `ProductQueryServiceImpl.java` |
| application | `internal/eventhandlers/` | `ProductCreatedEventHandler.java` |
| application | `internal/outboundservices/` (si el puerto es "de apoyo", ver 4.4) | `ProductXxxService.java` |
| application | `internal/outboundservices/acl/` (ver 11.1) | `ExternalProductService.java` |
| application | `acl/` | `MarketplaceContextFacadeImpl.java` |
| interfaces | `rest/resources/` | `CreateProductResource.java`, `ProductResource.java` |
| interfaces | `rest/transform/` | `CreateProductCommandFromResourceAssembler.java`, `ProductResourceFromEntityAssembler.java` |
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
- [ ] **Toda integración externa está detrás de una interfaz** — el command/domain service nunca importa la clase concreta del proveedor
- [ ] **Decidiste conscientemente**, respondiendo la pregunta de 4.4, si el puerto va en `domain/services` o en `application/internal/outboundservices` — y fuiste consistente dentro del BC
- [ ] Si coexisten un proveedor real y uno simulado/logging, **usaste subcarpeta por proveedor** (ver 4.1)
- [ ] Si una integración tiene "qué se envía" y "cómo se transporta", **las separaste** (ver 4.5)
- [ ] Un cronjob o una tarea de arranque va en `infrastructure/scheduling/` o `infrastructure/startup/`, no suelto en `application`
- [ ] Las credenciales nunca están hardcodeadas — entran por `application.properties`/variables de entorno vía una clase `Settings`
- [ ] Si `outboundservices` ya mezcla 3+ capacidades y se volvió difícil de escanear, **la subdividiste por capacidad** (ver 11.3)
- [ ] No se creó ninguna carpeta ni archivo que el proyecto no necesite

---

## 10. Resumen en una frase

> Se empieza por el **aggregate** (POJO puro), se derivan sus **commands, queries y puerto de repositorio**; luego se baja a **infrastructure** para traducirlo a JPA (converter/embeddable → entity → assembler → repository → adapter); se sube a **application** para la lógica de negocio (interfaz → impl con `Result`); y se termina en **interfaces** con los resources, los assemblers REST y el controller. Lo externo —base de datos, APIs de terceros, tokens, pagos, cronjobs— siempre entra por `infrastructure` detrás de una interfaz, pero **esa interfaz vive en `domain/services` cuando la capacidad es el corazón del aggregate, y en `application/internal/outboundservices` cuando es un apoyo técnico de la orquestación** — y cuando una integración mezcla "qué se dice" con "cómo se entrega" (como el correo), esas dos responsabilidades se separan en capas distintas aunque compartan el mismo nombre coloquial.

---

## 11. Profundización: ACL, `outboundservices` y qué lleva interfaz+implementación

### 11.1 — ACL dentro de `application`: lado emisor vs lado consumidor

El mecanismo de ACL de la sección 6 tiene dos mitades que viven en lugares distintos del mismo patrón `application`, y conviene no confundirlas:

- **Lado emisor** (el BC que expone su cara pública a otros): `application/acl/XxxContextFacadeImpl.java` — va **directo** en `application/acl/`, **no** dentro de `outboundservices`. Es un nivel hermano de `commandservices`/`queryservices`, porque el emisor no está "saliendo" a buscar nada — está exponiendo su propia interfaz pública.
- **Lado consumidor** (el BC que usa el facade de otro): `application/internal/outboundservices/acl/ExternalXxxService.java` — aquí **sí** va dentro de `outboundservices`, en su propia subcarpeta `acl/`. Razón: para el consumidor, pedirle algo a otro BC es exactamente el mismo tipo de necesidad que pedirle algo a un proveedor externo — "necesito algo de afuera de mi BC para completar mi caso de uso" — por eso cae en `outboundservices` como una subcarpeta más, junto a `email/`, `push/`, etc.

### 11.2 — Qué va dentro de `outboundservices` en general (no solo ACL)

`outboundservices` es la carpeta de **todo lo que la aplicación necesita para salir del BC**, sea hacia otro BC (ACL) o hacia un proveedor externo. Dentro conviven, por capacidad, cuatro tipos de archivo:

- Los **puertos** en sí (interfaces): `EmailSender`, `PushNotificationSender`, `TokenGenerator`, `FileStorageService`, `WebhookAuthorizationVerifier`...
- Los **DTOs/value types** que esos puertos usan para hablar: `EmailMessage`, `PushNotification`, `AuthenticatedUser`, `CertificateContent`...
- Las **excepciones** propias de esa comunicación: `EmailDeliveryException`...
- Y, si aplica, la subcarpeta `acl/` con el wrapper hacia otro BC (ver 11.1).

**No todo archivo de `outboundservices` (ni de `domain/services`) sigue el patrón interfaz+implementación.** Ese patrón solo aplica a los **puertos de verdad**: algo que representa una acción que podría resolverse de otra forma (generar un token, enviar un correo, hashear una contraseña) — ahí sí hay interfaz en `application`/`domain` e implementación concreta en `infrastructure`. Los `record` que viajan junto a esos puertos (`EmailMessage`, `PushNotification`, `AuthenticatedUser`) son **DTOs planos**: solo cargan datos entre el puerto y quien lo llama, no tienen comportamiento intercambiable, así que no necesitan — ni deben tener — una interfaz propia. Regla rápida: si la clase representa una acción → interfaz + impl; si solo representa datos → record plano, sin interfaz.

### 11.3 — Cuándo subdividir `outboundservices` por capacidad

Se agrupa **por capacidad** (`outboundservices/email/`, `outboundservices/push/`, `outboundservices/tokens/`...), nunca por tipo técnico de archivo (una carpeta `ports/` y otra `dtos/` separadas) — porque un puerto, su DTO y su excepción son una sola pieza conceptual que casi siempre se edita junta; separarlas por tipo técnico obligaría a saltar entre carpetas para ver algo que es un solo concepto.

El umbral para subdividir: mientras la carpeta tenga pocos archivos y pocas capacidades mezcladas (orientativamente, hasta ~8-10 archivos entre 2-3 capacidades), déjala **plana** — es perfectamente legible de un vistazo. Subdivide en subcarpetas por capacidad cuando crece lo suficiente como para que mezclar 3 o más capacidades distintas en una sola carpeta plana empiece a dificultar encontrar algo. Es el mismo criterio que ya aplicas en `infrastructure/<capacidad>/` (sección 4.1) — mantener el mismo principio de organización en ambos lados del puerto (interfaz en `application`, implementación en `infrastructure`) hace que el proyecto se lea de forma consistente.