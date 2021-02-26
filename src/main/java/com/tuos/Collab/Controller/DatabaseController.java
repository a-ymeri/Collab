package com.tuos.Collab.Controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DatabaseController {
	
	@RequestMapping("/auth/createUser")
	public String createUser(@RequestParam String usr, @RequestParam String psw ) {
		System.out.println("username: " + usr);
		System.out.println("psw: " + psw);
		return usr + " " + psw;
	}
	
	
}
