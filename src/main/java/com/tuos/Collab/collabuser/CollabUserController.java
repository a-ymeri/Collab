package com.tuos.Collab.collabuser;

import com.tuos.Collab.security.jwt.UsernameAndPasswordAuthenticationRequest;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Map;

@RestController
@AllArgsConstructor
public class CollabUserController {

    private CollabUserService collabUserService;

    @PostMapping(path = "api/register")
    public ResponseEntity register(@RequestBody RegistrationRequest registrationRequest) {
        CollabUser user = new CollabUser(registrationRequest.getUsername(),registrationRequest.getEmail(),registrationRequest.getPassword());
        try{
            collabUserService.register(user);
        }catch(IllegalStateException e){
            System.out.println("em");
            return ResponseEntity.status( HttpStatus.BAD_REQUEST).body("That email already exists.");
        }

        return ResponseEntity.status(HttpStatus.OK).body("Email created successfully");
    }

    @DeleteMapping(path = "api/user/delete/{id}")
    public String delete(@PathVariable Long id){
        return collabUserService.delete(id);
    }

    @PutMapping(path="api/user/changeUsername")
    public ResponseEntity changeUsername(@RequestBody Map<String,String> request, Principal principal){
        String newName = request.get("newName");
        try{
            collabUserService.update(newName,principal.getName());
        }catch(IllegalStateException e){
            return ResponseEntity.status( HttpStatus.BAD_REQUEST).body("That email already exists.");
        }
        return ResponseEntity.ok("Name changed successfully!");
    }



}
