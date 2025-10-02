
import java.io.*;
import java.util.*;

public class EntropyProcessor 
{
  /**
     * Process entropy data passed programmatically
     * @param entropyData 2D array where each row is [trainee1, trainee2, trainee3, team] entropy values
     */
  public static void processEntropyData(double[][] entropyData) {
    String outputFile = "calculated_concept_value.csv";
        
    try {
        // Extract individual entropy arrays
        double[] trainee1Entropy = new double[entropyData.length];
        double[] trainee2Entropy = new double[entropyData.length];
        double[] trainee3Entropy = new double[entropyData.length];
        double[] teamEntropy = new double[entropyData.length];
        
        for (int i = 0; i < entropyData.length; i++) 
        {
            trainee1Entropy[i] = entropyData[i][0];
            trainee2Entropy[i] = entropyData[i][1];
            trainee3Entropy[i] = entropyData[i][2];
            teamEntropy[i] = entropyData[i][3];
        }
        
        // Calculate perturbation window
        int time1 = 0;
        int time2 = trainee1Entropy.length - 1;
        
        System.out.println("Processing entropy data for " + entropyData.length + " time points...\n");
        
        // Calculate relaxation times for each
        int[] trainee1Results = LayeredDynamicRelaxationTimes.LayeredDynamicsRelaxationTimes(time1, time2, trainee1Entropy);
        int[] trainee2Results = LayeredDynamicRelaxationTimes.LayeredDynamicsRelaxationTimes(time1, time2, trainee2Entropy);
        int[] trainee3Results = LayeredDynamicRelaxationTimes.LayeredDynamicsRelaxationTimes(time1, time2, trainee3Entropy);
        int[] teamResults = LayeredDynamicRelaxationTimes.LayeredDynamicsRelaxationTimes(time1, time2, teamEntropy);
        
        // Display results
        System.out.println("trainee1: " + formatResult(trainee1Results));
        System.out.println("trainee2: " + formatResult(trainee2Results));
        System.out.println("trainee3: " + formatResult(trainee3Results));
        System.out.println("team: " + formatResult(teamResults));
        
        // Write results to output file
        Map<String, int[]> results = new LinkedHashMap<>();
        results.put("trainee1", trainee1Results);
        results.put("trainee2", trainee2Results);
        results.put("trainee3", trainee3Results);
        results.put("team", teamResults);
        
        writeResults(outputFile, results);
        System.out.println("\nResults written to: " + outputFile);
            
      } 
      catch (IOException e) 
      {
          System.err.println("Error writing results: " + e.getMessage());
          e.printStackTrace();
      }
    }
    
    /**
     * Writes calculated concept values to CSV file
     */
    private static void writeResults(String filename, Map<String, int[]> results) throws IOException {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(filename))) {
            // Write header
            bw.write("Subject,Enaction,Adaptation,Recovery\n");
            
            // Write each result
            for (Map.Entry<String, int[]> entry : results.entrySet()) {
                String subject = entry.getKey();
                int[] values = entry.getValue();
                
                if (values == null) {
                    bw.write(subject + ",null,null,null\n");
                } else {
                    bw.write(subject + "," + values[0] + "," + values[1] + "," + values[2] + "\n");
                }
            }
        }
    }
    
    /**
     * Formats result for console display
     */
    private static String formatResult(int[] result) 
    {
        if (result == null) 
        {
            return "Enaction=null, Adaptation=null, Recovery=null";
        }
        return "Enaction=" + result[0] + ", Adaptation=" + result[1] + ", Recovery=" + result[2];
    }
}