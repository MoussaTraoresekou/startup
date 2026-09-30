package wassa.mp.startup.service;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import wassa.mp.startup.dto.UserLoginRequestDto;
import wassa.mp.startup.model.User;
import wassa.mp.startup.repository.UserRepository;

import java.util.Objects;

@Service
public class UserService {
    private AuthenticationManager authenticationManager;
    private UserRepository userRepository;
    private BCryptPasswordEncoder bCryptPasswordEncoder;
    private JwtService jwtService;
    public UserService(UserRepository userRepository, BCryptPasswordEncoder bCryptPasswordEncoder,AuthenticationManager authenticationManager, JwtService jwtService) {
        this.userRepository = userRepository;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }
    /*
    public User register(UserRequestDto user){
        User userEntity = new User();
        userEntity.setNom(user.getNom());
        userEntity.setPrenom(user.getPrenom());
        userEntity.setPassword(bCryptPasswordEncoder.encode(user.getPassword()));
        return  userRepository.save(userEntity);
    }

     */

    public String login (UserLoginRequestDto user) {
          Authentication authentication= authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(user.getEmail(), user.getPassword())
        );
        User user1=userRepository.findByEmail(user.getEmail());
        if(authentication.isAuthenticated()){
            return  jwtService.generetedToken(user1);
        }
        return  "errer";
    }
}
