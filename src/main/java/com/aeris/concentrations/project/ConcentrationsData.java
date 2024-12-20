package com.aeris.concentrations.project;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import ucar.ma2.Array;
import ucar.nc2.Attribute;
import ucar.nc2.Dimension;
import ucar.nc2.NetcdfFile;
import ucar.nc2.NetcdfFiles;
import ucar.nc2.Variable;
import ucar.nc2.write.Ncdump;

@Component
public class ConcentrationsData {
    
    private boolean initalized = false;

    public boolean isInitalized() {
        return initalized;
    }
    ArrayList<Array> vals = new ArrayList<>(3);
    public ArrayList<Array> getVals() {
        return vals;
    }

    public enum DATA_FIELDS
    {
        X,
        Y,
        CONCENTRATION
    }

    private final String fileName;
    public String getFileName() {
        return fileName;
    }
    private final String filePath;
    public String getFilePath() {
        return filePath;
    }
    public ConcentrationsData(@Value("${netcdf.name}")String fileName, @Value("${netcdf.location}")String filePath) {
        this.fileName = fileName;
        this.filePath = filePath + fileName;
    }

    public String ncDump()
    {
        StringBuilder sb = new StringBuilder();

        try{
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
            sb.append(writer.toString());
        }
        catch(IOException e){
            sb.append("Error reading NetCDF file: ").append(e.getMessage());
        }

        return sb.toString();
    }

    public String details()
    {
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
    
    public boolean init()
    {
        if(!initalized)
        {
            try{
                // Open the NetCDF file
                NetcdfFile ncFile = NetcdfFiles.open(filePath);

                vals.add(DATA_FIELDS.X.ordinal(), ncFile.findVariable("x").read());
                vals.add(DATA_FIELDS.Y.ordinal(), ncFile.findVariable("y").read());
                vals.add(DATA_FIELDS.CONCENTRATION.ordinal(), ncFile.findVariable("concentration").read());

                ncFile.close();

                initalized = true;

            } catch (IOException e)
            {
                System.out.println("Error reading NetCDF file: " + e.getMessage());
            }
        }

        return initalized;
    }
}
