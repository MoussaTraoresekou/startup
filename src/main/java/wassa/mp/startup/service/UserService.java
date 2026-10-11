
package wassa.mp.startup.service;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;

import wassa.mp.startup.dto.LoginReponseDto;
import wassa.mp.startup.dto.UserLoginRequestDto;
import wassa.mp.startup.exception.OperationInterditeException;
import wassa.mp.startup.model.User;
import wassa.mp.startup.repository.UserRepository;

@Service
public class UserService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final JwtService jwtService;

    public UserService(
            UserRepository userRepository,
            AuthenticationManager authenticationManager,
            JwtService jwtService) {
        this.userRepository = userRepository;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }

    public LoginReponseDto login(UserLoginRequestDto request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );
        User user = userRepository.findByEmail(request.getEmail());
        if (user == null) {
            throw new OperationInterditeException(
                    "Utilisateur introuvable."
            );
        }
        String token = jwtService.generetedToken(user);
        return new LoginReponseDto(
                user.getId(),
                token,
                user.getRole().toString(),
                user.getNom(),
                user.getPrenom(),
                user.getEmail()
        );
    }
}
