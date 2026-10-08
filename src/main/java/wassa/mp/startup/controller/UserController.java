package wassa.mp.startup.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import wassa.mp.startup.dto.LoginReponseDto;
import wassa.mp.startup.dto.UserLoginRequestDto;
import wassa.mp.startup.service.UserService;
@RestController
@RequestMapping("/api/user")
public class UserController {
    @Autowired
    UserService userService;
    @PostMapping("/login")
    public LoginReponseDto login(@RequestBody UserLoginRequestDto user){
        return userService.login(user);
    }

}
