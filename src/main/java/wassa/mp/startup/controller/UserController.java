package wassa.mp.startup.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import wassa.mp.startup.model.User;
import wassa.mp.startup.repository.UserRepository;

import java.util.Objects;

@RestController
@RequestMapping("/user")
public class UserController {
    @Autowired
    UserRepository userRepository;
    @PostMapping("/register")
    public User register(@RequestBody User user){
          return userRepository.save(user);
    }
    @PostMapping("/login")
    public String login(@RequestBody User user){
         User userr=userRepository.findByNom(user.getNom());
         if(!Objects.isNull(userr)){
            return  "connection effectué avzc suvcces ";         }
         return  "errer";
    }

}
