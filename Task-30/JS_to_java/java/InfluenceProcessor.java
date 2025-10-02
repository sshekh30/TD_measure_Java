import java.io.*;

public class InfluenceProcessor {
    /**
     * Process communication data passed programmatically
     * @param communicationData 2D array where each row is [trainee1, trainee2, trainee3] states
     */
    public static void processCommunicationData(int[][] communicationData) {
        String outputFile = "calculated_influence_values.csv";
        
        try {
            // Transform to three bivariate arrays
            double[][] x_xyz = new double[communicationData.length][2];
            double[][] y_xyz = new double[communicationData.length][2];
            double[][] z_xyz = new double[communicationData.length][2];
            
            for (int i = 0; i < communicationData.length; i++) {
                int x = communicationData[i][0];
                int y = communicationData[i][1];
                int z = communicationData[i][2];
                
                // Concatenate to form XYZ
                int xyz = Integer.parseInt("" + x + y + z);
                
                x_xyz[i][0] = x;
                x_xyz[i][1] = xyz;
                
                y_xyz[i][0] = y;
                y_xyz[i][1] = xyz;
                
                z_xyz[i][0] = z;
                z_xyz[i][1] = xyz;
            }
            
            // Calculate AMI for each trainee
            int[][] nBins = {{10}};
            int nLags = 0;
            
            //System.out.println("Calculating influence values for " + communicationData.length + " time points...\n");
            
            Object[] result_x = ami.ami(x_xyz, nBins, nLags);
            Object[] result_y = ami.ami(y_xyz, nBins, nLags);
            Object[] result_z = ami.ami(z_xyz, nBins, nLags);
            
            // Extract AMI values (first result, ignore correlation)
            double influence_x = 0.0;
            double influence_y = 0.0;
            double influence_z = 0.0;
            
            if (result_x != null) {
                double[][] ami_x = (double[][]) result_x[0];
                influence_x = ami_x[0][0];
            }
            
            if (result_y != null) {
                double[][] ami_y = (double[][]) result_y[0];
                influence_y = ami_y[0][0];
            }
            
            if (result_z != null) {
                double[][] ami_z = (double[][]) result_z[0];
                influence_z = ami_z[0][0];
            }
            
            // Display results
            System.out.println("Trainee1 Influence: " + influence_x);
            System.out.println("Trainee2 Influence: " + influence_y);
            System.out.println("Trainee3 Influence: " + influence_z);
            
            // Write results to output file
            writeResults(outputFile, influence_x, influence_y, influence_z);
            System.out.println("\nResults written to: " + outputFile);
            
        } catch (IOException e) {
            System.err.println("Error writing results: " + e.getMessage());
            e.printStackTrace();
        }
    }
    /**
     * Writes calculated influence values to CSV file
     */
    private static void writeResults(String filename, double influence_x, double influence_y, double influence_z) throws IOException {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(filename))) {
            // Write header
            bw.write("Trainee,Influence\n");
            
            // Write results
            bw.write("Trainee1," + influence_x + "\n");
            bw.write("Trainee2," + influence_y + "\n");
            bw.write("Trainee3," + influence_z + "\n");
        }
    }
}