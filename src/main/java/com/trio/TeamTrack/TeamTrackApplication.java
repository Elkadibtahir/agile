package com.trio.TeamTrack;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableAsync
@SpringBootApplication
public class TeamTrackApplication {
    public static void main(String[] args) {
        SpringApplication.run(TeamTrackApplication.class, args);
    }
}
