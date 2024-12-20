package com.aeris.concentrations.project;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.jogamp.opengl.util.texture.TextureData;

//import com.jogamp.opengl.GLCapabilities;
//import com.jogamp.opengl.GLProfile;

import ucar.nc2.*;
import ucar.ma2.Array;
import ucar.ma2.*;
import ucar.nc2.write.Ncdump;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.jzy3d.maths.*;
import org.jzy3d.colors.colormaps.ColorMapRainbow;
import org.jzy3d.colors.colormaps.IColorMap;
import org.jzy3d.plot3d.primitives.Scatter;
import org.jzy3d.plot3d.primitives.Shape;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import org.jzy3d.chart.Chart;
import org.jzy3d.chart.factories.AWTChartFactory;
import org.jzy3d.chart.factories.OffscreenChartFactory;
import org.jzy3d.plot3d.primitives.Surface;
import org.jzy3d.plot3d.rendering.canvas.*;


import javax.imageio.ImageIO;



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

            // Create a StringWriter to capture the output
            StringWriter writer = new StringWriter();
            PrintWriter printWriter = new PrintWriter(writer);

            // Use NcDump to print the NetCDF file contents
            Ncdump.ncdump(ncFile, "", printWriter, null);

            // Close the NetCDF file
            ncFile.close();

            // Return the output as a string
            return writer.toString();

            // // Dump basic information about the NetCDF file
            // sb.append("netcdf ").append(fileName).append(" {\n\n");

            // // Dump the dimensions
            // sb.append("dimensions:\n");
            // for (Dimension dim : ncFile.getDimensions()) {
            //     sb.append("    ").append(dim.getShortName()).append(" = ")
            //             .append(dim.getLength()).append(" ;\n");
            // }

            // // Dump the variables
            // sb.append("\nvariables:\n");
            // for (Variable var : ncFile.getVariables()) {
            //     sb.append("    ").append(var.getFullName()).append(" ")
            //             .append(var.getDataType()).append(" ")
            //             .append(var.getDimensionsString()).append(" ;\n");

            //     // Dump variable attributes
            //     for (Attribute attr : var.getAttributes()) {
            //         sb.append("        ").append(var.getFullName())
            //                 .append(":").append(attr.getName()).append(" = ")
            //                 .append(attr.getValues()).append(" ;\n");
            //     }
            // }

            // // Dump the data for each variable
            // sb.append("\ndata:\n");
            // for (Variable var : ncFile.getVariables()) {
            //     sb.append("    ").append(var.getFullName()).append(" =\n");
            //     Array data = var.read();
            //     int[] shape = data.getShape();

            //     // Iterate over the data and print it (for small datasets)
            //     // For large datasets, you might want to only print a subset
            //     int[] index = new int[shape.length];
            //     int totalSize = 1;
            //     for (int i : shape) {
            //         totalSize *= i;
            //     }

            //     // We will print only the first few elements to avoid overwhelming the output
            //     int printLimit = 100;  // Limit the output to the first 100 elements
            //     for (int i = 0; i < totalSize && i < printLimit; i++) {
            //         if (i > 0 && i % shape[shape.length - 1] == 0) {
            //             sb.append("\n");
            //         }

            //         sb.append(data.getDouble(i)).append(", ");
            //     }

            //     sb.append(";\n\n");
            // }

            // // Close the NetCDF file
            // ncFile.close();

            // sb.append("}\n");

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
                    double x_point = x_vals.getDouble(i);
                    double y_point = y_vals.getDouble(j);
                    Index index = concentration.getIndex();
                    index.set(timeIndex, zIndex, j, i);
                    double concentration_point = concentration.getDouble(index);
                    points.add(new double[]{x_point, y_point, concentration_point});
                }
            }

            ncFile.close();

        } catch (IOException e)
        {
            sb.append("Error reading NetCDF file: ").append(e.getMessage());
        }

		return points;
	}

	@GetMapping("/get-image")
	public ResponseEntity<byte[]> getImage(@RequestParam int timeIndex, @RequestParam int zIndex) {
        StringBuilder sb = new StringBuilder();
        //ArrayList<double[]> points = new ArrayList<>();
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

        try{
            // Open the NetCDF file
            NetcdfFile ncFile = NetcdfFiles.open(filePath);

            Array x_vals = ncFile.findVariable("x").read();
            Array y_vals = ncFile.findVariable("y").read();
            Array concentration = ncFile.findVariable("concentration").read();

            int num_x_vals = x_vals.getShape()[0];
            int num_y_vals = y_vals.getShape()[0];

            List<Coord3d> points = Coord3d.list(num_x_vals * num_y_vals);

            
            for( int i = 0; i < num_x_vals; i++)
            {
                for( int j = 0; j < num_y_vals; j++)
                {
                    float x_point = x_vals.getFloat(i);
                    float y_point = y_vals.getFloat(j);
                    Index index = concentration.getIndex();
                    index.set(timeIndex, zIndex, j, i);
                    float concentration_point = concentration.getFloat(index);
                    points.add(new Coord3d(x_point, y_point, concentration_point*1));
                }
            }

            ncFile.close();

            // A ColorMap will allow you to color the surface based on the data values
            IColorMap colorMap = new ColorMapRainbow();
            // Create a Surface using the Coord3d and ColorMap
            float fi = 0;
            //Shape surface = Surface.shape(points, colorMap, fi);

            Scatter surface = new Scatter(points);

            // Create a chart
            Chart chart = new Chart(new OffscreenChartFactory(3000, 3000),  Quality.Advanced());
            chart.getScene().getGraph().add(surface);

            chart.add(surface);

            File n = new File("src/main/resources/static/test.png");
            chart.getCanvas().screenshot(n);
            
            BufferedImage image = textureDataToBufferedImage((TextureData) chart.screenshot());

            // Convert BufferedImage to byte array
            ImageIO.write(image, "PNG", byteArrayOutputStream);

        } catch (IOException e)
        {
            sb.append("Error reading NetCDF file: ").append(e.getMessage());
        }

        byte[] imageBytes = byteArrayOutputStream.toByteArray();

        // Set the appropriate HTTP headers and return the image as a response
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, MediaType.IMAGE_PNG_VALUE)
                .body(imageBytes);
	}

    /**
     * Convert TextureData to BufferedImage
     *
     * @param textureData The TextureData to be converted
     * @return The resulting BufferedImage
     */
    public static BufferedImage textureDataToBufferedImage(TextureData textureData) {
        // Get the width and height of the texture
        int width = textureData.getWidth();
        int height = textureData.getHeight();

        // Create a BufferedImage to store the result
        BufferedImage bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_4BYTE_ABGR);

        return bufferedImage;
    }
	
	public static void main(String[] args) {
		SpringApplication.run(ProjectApplication.class, args);
	}

}