package wassa.mp.startup.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import wassa.mp.startup.dto.UserRequestDto;
import wassa.mp.startup.model.User;
import wassa.mp.startup.repository.UserRepository;
import wassa.mp.startup.service.UserService;

import java.util.Objects;

@RestController
@RequestMapping("/user")
public class UserController {
    @Autowired
    UserService userService;
    @PostMapping("/register")
    public User register(@RequestBody UserRequestDto user){
          return userService.register(user);
    }
    @PostMapping("/login")
    public String login(@RequestBody UserRequestDto user){
        return userService.verify(user);
    }

}
