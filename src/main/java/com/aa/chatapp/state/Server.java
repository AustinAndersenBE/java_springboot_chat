package com.aa.chatapp.state;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.function.RouterFunction;
import org.springframework.web.servlet.function.ServerResponse;
import static org.springframework.web.servlet.function.RouterFunctions.route;
import static org.springframework.web.servlet.function.ServerResponse.ok;

@SpringBootApplication
public class Server {

    private static Server instance = null;
    private ConfigurableApplicationContext context;

    private Server() {
        // Start the server and connect to the database
        context = SpringApplication.run(Server.class);
        // Initialize routes or any other server setup here
    }

    public static Server getInstance() {
        if (instance == null) {
            instance = new Server();
        }
        return instance;
    }

    @Bean
    public RouterFunction<ServerResponse> routerFunction() {
        return route()
                .GET("/", req -> ok().body("Welcome to the home page!"))
                .build();
    }

    // Other methods related to server state and behavior
}
