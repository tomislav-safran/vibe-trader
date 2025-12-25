package com.tsafran.vibetrader;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.shell.command.annotation.CommandScan;

@SpringBootApplication
@CommandScan
public class VibeTraderApplication {

    public static void main(String[] args) {
        SpringApplication.run(VibeTraderApplication.class, args);
    }

}
