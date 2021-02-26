package com.tuos.Collab.Controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DatabaseController {
	
	@RequestMapping("/auth/createUser")
	public void  createUser(@RequestParam String usr, @RequestParam String psw ) {
		if(userExists(usr, psw)) {
			System.out.println("user exists");
		}else {
			//user create
		}
		System.out.println("username: " + usr);
		System.out.println("psw: " + psw);
		//return usr + " " + psw;
	}
	
	@RequestMapping("/auth/deleteUser")
	public String deleteUser(@RequestParam String usr, @RequestParam String psw ) {
		System.out.println("username: " + usr);
		System.out.println("psw: " + psw);
		return usr + " " + psw;
	}
	

	private boolean userExists(@RequestParam String usr, @RequestParam String psw ) {
		System.out.println("username: " + usr);
		System.out.println("psw: " + psw);
		return true;
	}
	
	
	
	
}
