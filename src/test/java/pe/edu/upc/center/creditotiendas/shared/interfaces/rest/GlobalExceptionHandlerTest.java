package pe.edu.upc.center.creditotiendas.shared.interfaces.rest;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.*;
import pe.edu.upc.center.creditotiendas.shared.application.result.ApplicationError;
import pe.edu.upc.center.creditotiendas.shared.application.result.Result;
import pe.edu.upc.center.creditotiendas.shared.infrastructure.i18n.configuration.LocaleConfiguration;
import pe.edu.upc.center.creditotiendas.shared.interfaces.rest.transform.ResponseEntityAssembler;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Exercises the shared error pipeline (GlobalExceptionHandler, ErrorResponseAssembler,
 * ResponseEntityAssembler and locale resolution) against a throwaway controller.
 */
class GlobalExceptionHandlerTest {

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new TestController())
                .setControllerAdvice(new GlobalExceptionHandler())
                .setLocaleResolver(new LocaleConfiguration().localeResolver())
                .build();
    }

    @Test
    void invalidBodyReturns400WithFieldDetails() throws Exception {
        mockMvc.perform(post("/test/items").contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.details").value(containsString("name:")));
    }

    @Test
    void malformedJsonReturns400InsteadOf500() throws Exception {
        mockMvc.perform(post("/test/items").contentType(MediaType.APPLICATION_JSON).content("{not json"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
    }

    @Test
    void wrongPathVariableTypeReturns400() throws Exception {
        mockMvc.perform(get("/test/items/abc"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
    }

    @Test
    void illegalArgumentFromValueObjectReturns400() throws Exception {
        mockMvc.perform(get("/test/illegal-argument"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.details").value("[Ruc] must have 11 digits"));
    }

    @Test
    void accessDeniedReturns403InsteadOf500() throws Exception {
        mockMvc.perform(get("/test/access-denied"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("FORBIDDEN"));
    }

    @Test
    void unexpectedExceptionReturns500WithoutLeakingInternalMessage() throws Exception {
        mockMvc.perform(get("/test/boom"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.code").value("UNEXPECTED_ERROR"))
                .andExpect(jsonPath("$.details").value(not(containsString("SQL"))));
    }

    @Test
    void wrongHttpMethodKeepsSpringStatus405() throws Exception {
        mockMvc.perform(delete("/test/items/1"))
                .andExpect(status().isMethodNotAllowed())
                .andExpect(jsonPath("$.code").value("METHOD_NOT_ALLOWED"));
    }

    @Test
    void resultFailureIsMappedToStatusAndSpanishMessageByDefault() throws Exception {
        mockMvc.perform(get("/test/items/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("CREDIT_SALE_NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("No se encontró el recurso solicitado (credit sale)"))
                .andExpect(jsonPath("$.details").value("99"));
    }

    @Test
    void resultFailureUsesEnglishWhenRequested() throws Exception {
        mockMvc.perform(get("/test/items/99").header("Accept-Language", "en-US"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("The requested credit sale was not found"));
    }

    @Test
    void businessRuleViolationReturns422() throws Exception {
        mockMvc.perform(get("/test/business-rule"))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.code").value("BUSINESS_RULE_VIOLATION"));
    }

    @Test
    void resultSuccessUsesGivenStatus() throws Exception {
        mockMvc.perform(get("/test/items/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("item-1"));
    }

    record CreateItemResource(@NotBlank String name) {
    }

    record ItemResource(Long id, String name) {
    }

    @RestController
    @RequestMapping("/test")
    static class TestController {

        @PostMapping("/items")
        ResponseEntity<?> create(@Valid @RequestBody CreateItemResource resource) {
            return ResponseEntity.status(HttpStatus.CREATED).body(new ItemResource(1L, resource.name()));
        }

        @GetMapping("/items/{id}")
        ResponseEntity<?> getById(@PathVariable Long id) {
            Result<Long, ApplicationError> result = id == 1L
                    ? Result.success(id)
                    : Result.failure(ApplicationError.notFound("CreditSale", id.toString()));
            return ResponseEntityAssembler.toResponseEntityFromResult(
                    result, value -> new ItemResource(value, "item-" + value), HttpStatus.OK);
        }

        @GetMapping("/business-rule")
        ResponseEntity<?> businessRule() {
            Result<Long, ApplicationError> result = Result.failure(
                    ApplicationError.businessRuleViolation("credit-limit", "Amount exceeds available credit"));
            return ResponseEntityAssembler.toResponseEntityFromResult(result, v -> v, HttpStatus.OK);
        }

        @GetMapping("/illegal-argument")
        void illegalArgument() {
            throw new IllegalArgumentException("[Ruc] must have 11 digits");
        }

        @GetMapping("/access-denied")
        void accessDenied() {
            throw new AccessDeniedException("denied");
        }

        @GetMapping("/boom")
        void boom() {
            throw new IllegalStateException("SQL error: relation credit_sales does not exist");
        }
    }
}
