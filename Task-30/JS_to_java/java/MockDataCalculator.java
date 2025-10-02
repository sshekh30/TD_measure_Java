/**
 * Mock calculator that simulates the single Java code which calculates
 * both entropy data and communication data, then calls the processors.
 */
public class MockDataCalculator {
    
    public static void main(String[] args) {
        System.out.println("=== Mock Data Calculator ===\n");
        
        // Simulate entropy calculation for 3 trainees + team (42 time points)
        System.out.println("Calculating entropy data...");
        double[][] entropyData = generateMockEntropyData();
        
        // Simulate communication state calculation (42 time points)
        System.out.println("Calculating communication data...");
        int[][] communicationData = generateMockCommunicationData();
        
        System.out.println("\n--- Processing Entropy Data ---");
        EntropyProcessor.processEntropyData(entropyData);
        
        System.out.println("\n--- Processing Communication Data ---");
        InfluenceProcessor.processCommunicationData(communicationData);
        
        System.out.println("\n=== Processing Complete ===");
    }
    
    /**
     * Simulates entropy calculation
     * Returns 2D array: [timePoint][trainee1, trainee2, trainee3, team]
     */
    private static double[][] generateMockEntropyData() {
        // Using the actual entropy data from your CSV
        double[][] data = new double[42][4];
        
        double[] trainee1 = {0.0, 0.0, 0.0, 0.0, 0.0, 0.8113, 1.0, 1.5, 1.0, 0.8113, 0.8113, 1.0, 0.8113, 0.0, 0.8113, 1.0, 0.8113, 0.0, 0.0, 0.8113, 1.0, 1.5, 1.0, 0.8113, 0.8113, 1.0, 0.8113, 0.0, 0.8113, 1.0, 0.8113, 0.0, 0.0, 0.8113, 1.0, 1.5, 1.0, 0.8113, 0.8113, 1.0, 0.8113, 0.0};
        double[] trainee2 = {0.0, 0.0, 0.0, 0.0, 0.0, 0.8113, 1.5, 1.5, 0.8113, 0.0, 0.8113, 1.0, 1.0, 1.5, 2.0, 1.5, 0.8113, 0.0, 0.0, 0.8113, 1.5, 1.5, 0.8113, 0.0, 0.8113, 1.0, 1.0, 1.5, 2.0, 1.5, 0.8113, 0.0, 0.0, 0.8113, 1.5, 1.5, 0.8113, 0.0, 0.8113, 1.0, 1.0, 1.5};
        double[] trainee3 = {0.0, 0.0, 0.0, 1.5, 0.8113, 1.5, 1.0, 1.5, 1.0, 1.5, 1.5, 1.5, 1.5, 1.5, 1.5, 2.0, 2.0, 1.5, 0.8113, 1.5, 1.0, 1.5, 1.0, 1.5, 1.5, 1.5, 1.5, 1.5, 1.5, 2.0, 2.0, 1.5, 0.8113, 1.5, 1.0, 1.5, 1.0, 1.5, 1.5, 1.5, 1.5, 1.5};
        double[] team = {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.8113, 1.5, 1.5, 0.8113, 0.8113, 1.5, 2.0, 1.5, 1.5, 1.5, 0.8113, 0.0, 0.0, 0.0, 0.8113, 1.5, 1.5, 0.8113, 0.8113, 1.5, 2.0, 1.5, 1.5, 1.5, 0.8113, 0.0, 0.0, 0.0, 0.8113, 1.5, 1.5, 0.8113, 0.8113, 1.5, 2.0, 1.5};
        
        for (int i = 0; i < 42; i++) {
            data[i][0] = trainee1[i];
            data[i][1] = trainee2[i];
            data[i][2] = trainee3[i];
            data[i][3] = team[i];
        }
        
        return data;
    }
    
    /**
     * Simulates communication state calculation
     * Returns 2D array: [timePoint][trainee1, trainee2, trainee3]
     */
    private static int[][] generateMockCommunicationData() {
        // Using the actual communication data from your CSV
        int[][] data = {
            {47,49,51}, {47,49,51}, {47,49,51}, {47,49,51}, {47,49,51}, {47,49,51},
            {47,48,51}, {46,48,50}, {46,48,50}, {46,48,50}, {47,49,51}, {47,49,50},
            {47,48,50}, {47,49,50}, {47,49,51}, {47,49,51}, {47,49,51}, {47,49,51},
            {47,49,51}, {47,49,51}, {47,48,51}, {46,48,50}, {46,48,50}, {46,48,50},
            {47,49,51}, {47,49,50}, {47,48,50}, {47,49,50}, {47,49,51}, {47,49,51},
            {47,49,51}, {47,49,51}, {47,49,51}, {47,49,51}, {47,48,51}, {46,48,50},
            {46,48,50}, {46,48,50}, {47,49,51}, {47,49,50}, {47,48,50}, {47,49,50}
        };
        
        return data;
    }
}