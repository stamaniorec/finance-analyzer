package controllers;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.authc.credential.DefaultPasswordService;
import org.apache.shiro.subject.Subject;

import entities.User;
import helpers.AuthenticationHelper;

@XmlRootElement
@Path("/auth")
public class AuthenticationController {
	
	final static DefaultPasswordService passwordService = new DefaultPasswordService();
	public static Subject currentSubject;
	
	@GET
	@Path("/current-user")
	public User getCurrUser(){
		return AuthenticationHelper.getCurrentUser();
	}
	
	@POST
	@Path("/authenticate")
	@Consumes(MediaType.APPLICATION_JSON)
	public void SignIn(User user){
		currentSubject = SecurityUtils.getSubject();
	    UsernamePasswordToken token = new UsernamePasswordToken(user.getUsername(), user.getPassword());
	    currentSubject.login(token);
	}
	
	@POST
	@Path("/register")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public User signUp(User user) {
	    UsernamePasswordToken token = new UsernamePasswordToken(user.getUsername(), user.getPassword());
		user.setPassword(passwordService.encryptPassword(user.getPassword()));
		User newUser = (new UserController()).createUser(user);
		currentSubject = SecurityUtils.getSubject();
	    currentSubject.login(token);
	    return newUser;
		
	}
	
	@DELETE
	@Path("/logout")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public void logout() {
		Subject currentUser = SecurityUtils.getSubject();
		currentUser.logout();
	}
}
