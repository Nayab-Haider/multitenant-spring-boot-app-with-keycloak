package com.demo.api.configuration;

import org.keycloak.representations.AccessToken;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.AuditorAware;

import java.util.Optional;

public class AuditorAwareImpl implements AuditorAware<String> {

    @Autowired
    private AccessToken accessToken;

    @Override
    public Optional<String> getCurrentAuditor() {
        try {
            String userId = accessToken.getPreferredUsername();
            return Optional.of(userId != null ? userId : "");
        } catch (BeanCreationException e) {
            return Optional.of("scheduler");
        }


    }

}