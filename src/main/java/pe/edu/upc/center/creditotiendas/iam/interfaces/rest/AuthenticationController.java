package pe.edu.upc.center.creditotiendas.iam.interfaces.rest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pe.edu.upc.center.creditotiendas.iam.application.commandservices.UserCommandService;
import pe.edu.upc.center.creditotiendas.iam.interfaces.rest.resources.SignInResource;
import pe.edu.upc.center.creditotiendas.iam.interfaces.rest.transform.AuthenticatedUserResourceFromEntityAssembler;
import pe.edu.upc.center.creditotiendas.iam.interfaces.rest.transform.SignInCommandFromResourceAssembler;
import pe.edu.upc.center.creditotiendas.shared.interfaces.rest.transform.ResponseEntityAssembler;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

/**
 * Sign-in is the only public endpoint of the API. There is no sign-up: users are created by
 * the system admin (US-14).
 */
@RestController
@RequestMapping(value = "/api/v1/authentication", produces = APPLICATION_JSON_VALUE)
@Tag(name = "Authentication", description = "Sign-in and JWT issuing")
public class AuthenticationController {

    private final UserCommandService userCommandService;

    public AuthenticationController(UserCommandService userCommandService) {
        this.userCommandService = userCommandService;
    }

    @PostMapping("/sign-in")
    @SecurityRequirements
    @Operation(summary = "Sign in with email and password and receive a JWT bearer token")
    public ResponseEntity<?> signIn(@Valid @RequestBody SignInResource resource) {
        var command = SignInCommandFromResourceAssembler.toCommandFromResource(resource);
        var result = userCommandService.handle(command);
        return ResponseEntityAssembler.toResponseEntityFromResult(
                result, AuthenticatedUserResourceFromEntityAssembler::toResourceFromEntity, HttpStatus.OK);
    }
}
