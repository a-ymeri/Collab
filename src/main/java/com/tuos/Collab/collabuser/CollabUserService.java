package com.tuos.Collab.collabuser;

//import org.springframework.boot.autoconfigure.security.co

import com.tuos.Collab.collabuser.CollabUserRepository;
import lombok.AllArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class CollabUserService implements UserDetailsService {

    private final CollabUserRepository collabUserRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final EmailValidator emailValidator;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        System.out.println(email);
        return collabUserRepository.findByEmail(email).orElseThrow(() -> new UsernameNotFoundException(""));
    }


    public String register(CollabUser collabUser){
        boolean isValidEmail = emailValidator.test(collabUser.getEmail());
        if(!isValidEmail){
            throw new IllegalStateException("Email not valid");
        }
        boolean userExists = collabUserRepository.findByEmail(collabUser.getEmail())
                .isPresent();

        if(userExists){
            throw new IllegalStateException("An account with this email already exists.");
        }

        String encodedPassword = bCryptPasswordEncoder.encode(collabUser.getPassword());

        collabUser.setPassword(encodedPassword);

        collabUserRepository.save(collabUser);
        //TODO: send confirmation token
        return "it works";
    }

    public String delete(Long id) {
        collabUserRepository.deleteById(id);
        return "User deleted";
    }
}
