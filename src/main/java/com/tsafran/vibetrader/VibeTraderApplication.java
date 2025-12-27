package com.tsafran.vibetrader;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.shell.command.annotation.CommandScan;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@CommandScan
@EnableScheduling
public class VibeTraderApplication {

    public static void main(String[] args) {
        SpringApplication.run(VibeTraderApplication.class, args);
    }

}
