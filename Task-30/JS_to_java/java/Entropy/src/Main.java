package src;

import dao.*;
import config.*;
import extraction.*;

import java.util.*;
import java.io.*;

public class Main {

    //dumping entropy values into csv
    public static void dumpToCSV(double[][] data, String fileName) {

        try (PrintWriter writer = new PrintWriter(new FileWriter(fileName))) {
        
        int totalTime = data[0].length;

        writer.print("Time");
        for (int t = 0; t < totalTime; t++) {
            writer.print("," + t);
        }
        writer.println();

        for (int layer = 0; layer < data.length; layer++) {
            writer.print("Layer " + layer);
            for (int t = 0; t < totalTime; t++) {
            writer.print("," + data[layer][t]);
            }
            writer.println();
        }

        System.out.println("Data exported to: " + fileName);
        } catch(IOException e) {
        System.out.println("Error writing to file: " + e.getMessage());
        }
    }

    public static void main(String[] args) throws IOException {
        ConfigManager config = new ConfigManager("resources/runtime.properties");
        DataSourceDAO dataSource = DataSourceFactory.createDataSource(config);

        // FieldExtractor extractor = new FieldExtractor(dataSource);
        // List<List<List<String>>> layers = extractor.extractSTTCLayers();  

        // System.out.println("====Extracted Layers:====");
        // System.out.println(layers);

        List<String> rawData = dataSource.readData();
        System.out.println("====Raw Data from DataSource:====");
        System.out.println("Number of records: " + rawData.size());
        
        // Print first few records
        for (int i = 0; i < Math.min(5, rawData.size()); i++) {
            System.out.println("Record " + i + ": " + rawData.get(i));
        }
    }
}