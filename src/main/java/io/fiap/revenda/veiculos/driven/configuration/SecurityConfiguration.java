package io.fiap.revenda.veiculos.driven.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

@Configuration
public class SecurityConfiguration {

    private final Environment env;

    public SecurityConfiguration(Environment env) {this.env = env;}


    @Bean
    public SecurityWebFilterChain securityWebFilterChain(final ServerHttpSecurity http) {

        var security = http
            .csrf(ServerHttpSecurity.CsrfSpec::disable)
            .formLogin(ServerHttpSecurity.FormLoginSpec::disable)
            .httpBasic(ServerHttpSecurity.HttpBasicSpec::disable)
            .logout(ServerHttpSecurity.LogoutSpec::disable)
            .requestCache(ServerHttpSecurity.RequestCacheSpec::disable)
            .exceptionHandling(Customizer.withDefaults())
            .oauth2ResourceServer(oAuth2ResourceServerSpec ->
                oAuth2ResourceServerSpec.jwt(Customizer.withDefaults()));

        if (env.matchesProfiles("local")) {
            security.authorizeExchange(authorizeExchangeSpec ->
                authorizeExchangeSpec
                    .anyExchange()
                    .permitAll());
        } else {
            security.authorizeExchange(authorizeExchangeSpec ->
                authorizeExchangeSpec.pathMatchers("/veiculos")
                    .authenticated()
                    .anyExchange()
                    .permitAll());
        }

        return security.build();
    }
}
