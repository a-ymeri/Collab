package com.tuos.Collab.collabuser;

import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@AllArgsConstructor
public class CollabUserController {

    private CollabUserService collabUserService;

    @PostMapping(path = "api/user/register")
    public String register(@RequestBody CollabUser request) {
        return collabUserService.register(request);
    }

    @DeleteMapping(path = "api/user/delete/{id}")
    public String delete(@PathVariable Long id){
        return collabUserService.delete(id);
    }




}
