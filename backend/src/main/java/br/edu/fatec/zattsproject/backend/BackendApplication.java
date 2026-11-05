package br.edu.fatec.zattsproject.backend; // Mantém o teu package correto aqui no topo

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.data.mongodb.core.SimpleMongoClientDatabaseFactory;

@SpringBootApplication
public class BackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(BackendApplication.class, args);
    }


    @Bean
    public MongoClient mongoClient() {
        return MongoClients.create("mongodb+srv://1234:6AdFhASiWB7ULiq2@clusterdsm.txdmkv8.mongodb.net/zatts_db?retryWrites=true&w=majority");
    }


    @Bean
    public MongoDatabaseFactory mongoDatabaseFactory() {
        return new SimpleMongoClientDatabaseFactory(mongoClient(), "zatts_db");
    }
}