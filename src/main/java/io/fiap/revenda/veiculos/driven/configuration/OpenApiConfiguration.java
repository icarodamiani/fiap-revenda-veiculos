package io.fiap.revenda.veiculos.driven.configuration;

import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.OAuthFlow;
import io.swagger.v3.oas.annotations.security.OAuthFlows;
import io.swagger.v3.oas.annotations.security.OAuthScope;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.context.annotation.Configuration;

@Configuration
@SecurityScheme(name = "OAuth2", type = SecuritySchemeType.OAUTH2,
    flows = @OAuthFlows(authorizationCode = @OAuthFlow(
        authorizationUrl = "${spring.security.oauth2.resourceserver.jwt.issuer-uri}/protocol/openid-connect/auth",
        tokenUrl = "${spring.security.oauth2.resourceserver.jwt.issuer-uri}/protocol/openid-connect/token",
        scopes = {
            @OAuthScope(name = "openid", description = "openid scope"),
            @OAuthScope(name = "profile", description = "profile scope"),
            @OAuthScope(name = "email", description = "email scope"),
            @OAuthScope(name = "offline_access", description = "offline_access scope")
        })
    )
)
public class OpenApiConfiguration {
}
