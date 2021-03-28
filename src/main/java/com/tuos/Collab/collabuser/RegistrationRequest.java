package com.tuos.Collab.collabuser;

import com.tuos.Collab.security.jwt.UsernameAndPasswordAuthenticationRequest;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class RegistrationRequest extends UsernameAndPasswordAuthenticationRequest {
    private String username;
}
