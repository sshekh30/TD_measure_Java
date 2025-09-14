package src;

import dao.*;
import config.*;
import parser.*;
import entropy.*;
import visualizer.*;


import java.util.*;
import java.io.*;

public class Main {

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

    public static void dumpToFile(List<String> data, String fileName) throws IOException {
       try (FileWriter writer = new FileWriter(fileName)) {
           writer.write("[\n");
           for (int i = 0; i < data.size(); i++) {
               writer.write("  " + data.get(i));
               if (i < data.size() - 1) {
                   writer.write(",");
               }
               writer.write("\n");
           }
           writer.write("]\n");
           System.out.println("Data dumped to: " + fileName);
       }
    }

    private static String[][][] convertToArray(List<List<List<String>>> listData) {
        String[][][] arrayData = new String[listData.size()][][];
        for (int i = 0; i < listData.size(); i++) {
            List<List<String>> timePoint = listData.get(i);
            arrayData[i] = new String[timePoint.size()][];
            for (int j = 0; j < timePoint.size(); j++) {
                List<String> layer = timePoint.get(j);
                arrayData[i][j] = layer.toArray(new String[0]);
            }
        }
        return arrayData;
    }

    public static void main(String[] args) throws IOException {
        ConfigManager config = new ConfigManager("resources/runtime.properties");
        DataSourceDAO dataSource = DataSourceFactory.createDataSource(config);
        DataParser parser = ParserFactory.createParser(config.getDataSourceType());

        List<String> rawData = dataSource.readData();
        List<List<List<String>>> layers = parser.parseToSTTCLayers(rawData);
        String[][][] layerData = convertToArray(layers);  
        
        List<double[]> entropies = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
          AggregationStrategy strategy = new LayerAggregationStrategy(i);
          double[] layerEntropy = GeneralizedEntropyCalculator.computeWindowedEntropy(layerData, 4, strategy);
          entropies.add(layerEntropy);
          System.out.println("Layer " + i + " entropy: " + Arrays.toString(layerEntropy));
        }
        EntropyVisualizer viz = new EntropyVisualizer();
        viz.generateVisualization(entropies, "team_dynamics_entropy.html");
          
        // System.out.println(layers);

        // dumpToFile(rawData, "mongodb_raw_data.json");
    }
}
