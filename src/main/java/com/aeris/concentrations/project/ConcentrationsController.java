package com.aeris.concentrations.project;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;

@RestController
@RequestMapping
public class ConcentrationsController {
    
    private final ConcentrationsService concentrationsService;

    @Autowired
    public ConcentrationsController(ConcentrationsService concentrationsService) {
        this.concentrationsService = concentrationsService;
    }

	@GetMapping("/get-info")
	public String getInfo() {
        return concentrationsService.getInfo();
	}

	@GetMapping("/get-data")
	public ArrayList<double[]> getData(@RequestParam int timeIndex, @RequestParam int zIndex) {
       return concentrationsService.getData(timeIndex, zIndex);
	}

	@GetMapping("/get-image")
	public ResponseEntity<byte[]> getImage(@RequestParam int timeIndex, @RequestParam int zIndex) {
        return concentrationsService.getImage(timeIndex, zIndex);
    }
}
