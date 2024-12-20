package com.aeris.concentrations.project;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

import org.jzy3d.chart.Chart;
import org.jzy3d.chart.factories.OffscreenChartFactory;
import org.jzy3d.colors.colormaps.ColorMapRainbow;
import org.jzy3d.colors.colormaps.IColorMap;
import org.jzy3d.maths.Coord3d;
import org.jzy3d.plot3d.primitives.Shape;
import org.jzy3d.plot3d.primitives.Surface;
import org.jzy3d.plot3d.rendering.canvas.Quality;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.jogamp.opengl.util.texture.TextureData;

import ucar.ma2.Array;
import ucar.ma2.Index;

@Service
public class ConcentrationsService {

    private final ConcentrationsData concentrationsData;

    @Autowired
    public ConcentrationsService(ConcentrationsData concentrationsData) {
        this.concentrationsData = concentrationsData;
    }

    public String filePath = "src/main/resources/static/concentration.timeseries.nc";
    
    public String getInfo()
    {
        return concentrationsData.ncDump();
    }

    public ArrayList<double[]> getData(int timeIndex, int zIndex) {
        ArrayList<double[]> points = new ArrayList<>();
    
        if(concentrationsData.init())
        {
            ArrayList<Array> vals = concentrationsData.getVals();
            Array x_vals = vals.get(ConcentrationsData.DATA_FIELDS.X.ordinal());
            Array y_vals = vals.get(ConcentrationsData.DATA_FIELDS.Y.ordinal());
            Array concentration = vals.get(ConcentrationsData.DATA_FIELDS.CONCENTRATION.ordinal());
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
        }

        return points;
	}

	public ResponseEntity<byte[]> getImage(int timeIndex, int zIndex) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

        if(concentrationsData.init()) {

            ArrayList<Array> vals = concentrationsData.getVals();
            Array x_vals = vals.get(ConcentrationsData.DATA_FIELDS.X.ordinal());
            Array y_vals = vals.get(ConcentrationsData.DATA_FIELDS.Y.ordinal());
            Array concentration = vals.get(ConcentrationsData.DATA_FIELDS.CONCENTRATION.ordinal());
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

            // A ColorMap will allow you to color the surface based on the data values
            IColorMap colorMap = new ColorMapRainbow();
            // Create a Surface using the Coord3d and ColorMap
            float fi = 0;
            Shape surface = Surface.shape(points, colorMap, fi);

            // Create a chart
            Chart chart = new Chart(new OffscreenChartFactory(3000, 3000),  Quality.Advanced());
            chart.getScene().getGraph().add(surface);

            chart.add(surface);

            try{
                File n = new File("src/main/resources/static/test.png");
                chart.getCanvas().screenshot(n);
                
                BufferedImage image = textureDataToBufferedImage((TextureData) chart.screenshot());

                // Convert BufferedImage to byte array
                ImageIO.write(image, "PNG", byteArrayOutputStream);
            }
            catch (IOException e)
            {
                System.out.println("Unable to generate graph!");
            }

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
}
