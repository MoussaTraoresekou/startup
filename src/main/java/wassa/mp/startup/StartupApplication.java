package wassa.mp.startup;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import wassa.mp.startup.model.Admin;
import wassa.mp.startup.model.SecteurActivite;
import wassa.mp.startup.model.User;
import wassa.mp.startup.repository.SecteurActiviteRepository;
import wassa.mp.startup.repository.UserRepository;

import java.util.ArrayList;
import java.util.List;

@SpringBootApplication
public class StartupApplication {

    public static void main(String[] args) {
        SpringApplication.run(StartupApplication.class, args);
    }


}
