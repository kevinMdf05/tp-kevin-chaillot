package com.ingnum.rentalservice.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;

@RestController
public class BonjourController {

    @Value("${customer.service.url}")
    private String customerServiceUrl;

    @GetMapping("/customer/{name}")
    public String bonjour(@PathVariable String name) {
        try {
            URL url = new URL(customerServiceUrl);
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(url.openStream())
            );
            String response = in.readLine();
            return response + " " + name;
        } catch (Exception e) {
            return "Erreur : " + e.getMessage();
        }
    }
}
