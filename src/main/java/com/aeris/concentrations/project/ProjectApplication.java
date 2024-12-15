package com.aeris.concentrations.project;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


@RestController
@SpringBootApplication
public class ProjectApplication {

	@GetMapping("/get-info")
	public String getInfo() {
		return "get-info called";
	}

	@GetMapping("/get-data")
	public String getData(@RequestParam int timeIndex, @RequestParam int zIndex) {
		return "get-data called with timeIndex: " + timeIndex + " zIndex: " + zIndex;
	}

	@GetMapping("/get-image")
	public String getImage(@RequestParam int timeIndex, @RequestParam int zIndex) {
		return "get-image called with timeIndex: " + timeIndex + " zIndex: " + zIndex;
	}
	
	
	
	public static void main(String[] args) {
		SpringApplication.run(ProjectApplication.class, args);
	}

}
