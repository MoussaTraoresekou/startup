package wassa.mp.startup.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import wassa.mp.startup.model.User;

public interface UserRepository extends JpaRepository<User,Integer> {
    public User findByEmail(String email);
}
