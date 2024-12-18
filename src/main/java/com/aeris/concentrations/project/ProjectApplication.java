package com.aeris.concentrations.project;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ucar.nc2.*;
import ucar.nc2.dataset.*;
import ucar.ma2.*;

import java.io.IOException;
import java.util.ArrayList;



@RestController
@SpringBootApplication
public class ProjectApplication {

    String fileName = "concentration.timeseries.nc";
    String filePath = "src/main/resources/static/" + fileName;

	@GetMapping("/get-info")
	public String getInfo() {
        StringBuilder sb = new StringBuilder();

        try {

            // Open the NetCDF file
            NetcdfFile ncFile = NetcdfFiles.open(filePath);

            // Dump basic information about the NetCDF file
            sb.append("netcdf ").append(fileName).append(" {\n\n");

            // Dump the dimensions
            sb.append("dimensions:\n");
            for (Dimension dim : ncFile.getDimensions()) {
                sb.append("    ").append(dim.getShortName()).append(" = ")
                        .append(dim.getLength()).append(" ;\n");
            }

            // Dump the variables
            sb.append("\nvariables:\n");
            for (Variable var : ncFile.getVariables()) {
                sb.append("    ").append(var.getFullName()).append(" ")
                        .append(var.getDataType()).append(" ")
                        .append(var.getDimensionsString()).append(" ;\n");

                // Dump variable attributes
                for (Attribute attr : var.getAttributes()) {
                    sb.append("        ").append(var.getFullName())
                            .append(":").append(attr.getName()).append(" = ")
                            .append(attr.getValues()).append(" ;\n");
                }
            }

            // Dump the data for each variable
            sb.append("\ndata:\n");
            for (Variable var : ncFile.getVariables()) {
                sb.append("    ").append(var.getFullName()).append(" =\n");
                Array data = var.read();
                int[] shape = data.getShape();

                // Iterate over the data and print it (for small datasets)
                // For large datasets, you might want to only print a subset
                int[] index = new int[shape.length];
                int totalSize = 1;
                for (int i : shape) {
                    totalSize *= i;
                }

                // We will print only the first few elements to avoid overwhelming the output
                int printLimit = 100;  // Limit the output to the first 100 elements
                for (int i = 0; i < totalSize && i < printLimit; i++) {
                    if (i > 0 && i % shape[shape.length - 1] == 0) {
                        sb.append("\n");
                    }

                    sb.append(data.getDouble(i)).append(", ");
                }

                sb.append(";\n\n");
            }

            // Close the NetCDF file
            ncFile.close();

            sb.append("}\n");

        } catch (IOException e) {
            sb.append("Error reading NetCDF file: ").append(e.getMessage());
        }

        return sb.toString();
	}

	@GetMapping("/get-data")
	public ArrayList<double[]> getData(@RequestParam int timeIndex, @RequestParam int zIndex) {
        StringBuilder sb = new StringBuilder();
        ArrayList<double[]> points = new ArrayList<>();
        try{
            // Open the NetCDF file
            NetcdfFile ncFile = NetcdfFiles.open(filePath);

            Array x_vals = ncFile.findVariable("x").read();
            Array y_vals = ncFile.findVariable("y").read();
            Array concentration = ncFile.findVariable("concentration").read();

            int num_x_vals = x_vals.getShape()[0];
            int num_y_vals = y_vals.getShape()[0];
            for( int i = 0; i < num_x_vals; i++)
            {
                for( int j = 0; j < num_y_vals; j++)
                {
                    //ArrayDouble point = {x_vals[i], y_vals[j]};
                    double x_point = x_vals.getDouble(i);
                    double y_point = y_vals.getDouble(j);
                    Index index = concentration.getIndex();
                    index.set(timeIndex, zIndex, j, i);
                    double concentration_point = concentration.getDouble(index);
                    points.add(new double[]{x_point, y_point, concentration_point});
                }
            }

        } catch (IOException e)
        {
            sb.append("Error reading NetCDF file: ").append(e.getMessage());
        }

		return points;
	}

	@GetMapping("/get-image")
	public String getImage(@RequestParam int timeIndex, @RequestParam int zIndex) {
		return "get-image called with timeIndex: " + timeIndex + " zIndex: " + zIndex;
	}
	
	
	
	public static void main(String[] args) {
		SpringApplication.run(ProjectApplication.class, args);
	}

}