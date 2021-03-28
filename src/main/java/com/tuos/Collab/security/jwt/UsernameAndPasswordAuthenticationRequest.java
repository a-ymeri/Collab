package com.tuos.Collab.security.jwt;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class UsernameAndPasswordAuthenticationRequest {
    private String email;
    private String password;
}
