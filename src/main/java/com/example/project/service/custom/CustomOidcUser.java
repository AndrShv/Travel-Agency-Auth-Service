package com.example.project.service.custom;


import com.example.project.model.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.OidcUserInfo;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class CustomOidcUser implements OidcUser {

    private final OidcUser delegate;
    private final User user;

    public CustomOidcUser(OidcUser delegate, User user) {
        this.delegate = delegate;
        this.user = user;
    }

    @Override
    public Map<String, Object> getClaims() {
        return delegate.getClaims();
    }

    @Override
    public OidcUserInfo getUserInfo() {
        return delegate.getUserInfo();
    }

    @Override
    public OidcIdToken getIdToken() {
        return delegate.getIdToken();
    }

    @Override
    public Map<String, Object> getAttributes() {
        Map<String, Object> attributes = new HashMap<>(delegate.getAttributes());
        // Заменяем атрибуты данными из БД
        attributes.put("name", user.getUsername());
        attributes.put("email", user.getEmail());
        attributes.put("db_id", user.getId().toString());
        attributes.put("role", user.getRole().name());
        return attributes;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        if (user.getRole() != null) {
            return Collections.singletonList(
                    new SimpleGrantedAuthority("ROLE_" + user.getRole().name())
            );
        }
        return delegate.getAuthorities();
    }

    @Override
    public String getName() {
        return user.getEmail();
    }
    public User getUser() {
        return user;
    }

    public String getEmail() {
        return user.getEmail();
    }

    public BigDecimal getBalance() {
        return user.getBalance();
    }
}

