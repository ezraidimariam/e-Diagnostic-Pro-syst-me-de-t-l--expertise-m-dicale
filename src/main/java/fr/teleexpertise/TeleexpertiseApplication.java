package fr.teleexpertise;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import fr.teleexpertise.dao.UtilisateurDao;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class TeleexpertiseApplication {

    public static void main(String[] args) {
        SpringApplication.run(TeleexpertiseApplication.class, args);
    }

    @Bean
    public UtilisateurDao utilisateurDao() {
        return new UtilisateurDao();
    }
}
