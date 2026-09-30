package wassa.mp.startup.service;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import wassa.mp.startup.CustumUserDetail;
import wassa.mp.startup.model.User;
import wassa.mp.startup.repository.UserRepository;

import java.util.Objects;
@Component
public class CustumUserServiceDetail implements UserDetailsService {
    private UserRepository userRepository;
    public CustumUserServiceDetail(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user=userRepository.findByEmail(email);
        if(Objects.isNull(user)){
            System.out.println("user not found");
              throw new UsernameNotFoundException(email);
        }
        return new CustumUserDetail(user);
    }
}
