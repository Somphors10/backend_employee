package com.kshrd.admsfileservice.employeemanage.security;

import com.kshrd.admsfileservice.employeemanage.model.entity.AppUser;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Getter
public class AppUserDetails implements UserDetails {
    private final AppUser user;
    private final List<GrantedAuthority> authorities;

    public AppUserDetails(AppUser user) {
        this.user = user;
        List<GrantedAuthority> granted = new ArrayList<>();
        granted.add(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()));
        RolePermissions.forRole(user.getRole())
                .forEach(permission -> granted.add(new SimpleGrantedAuthority(permission)));
        this.authorities = List.copyOf(granted);
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return user.getPassword();
    }

    @Override
    public String getUsername() {
        return user.getUsername();
    }

    @Override
    public boolean isEnabled() {
        return user.isEnabled();
    }
}
