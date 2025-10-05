import config.*;
import java.io.*;
import java.util.*;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import service.*;
import servlet.*;

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
        } catch (IOException e) {
            System.out.println("Error writing to file: " + e.getMessage());
        }
    }

    public static void dumpToFile(List<String> data, String fileName)
        throws IOException {
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

    public static void main(String[] Args) throws Exception {
        ConfigManager config = new ConfigManager(
            "resources/runtime.properties"
        );

        SessionEntropyService sessionEntropyService = new SessionEntropyService(
            config
        );

        Server server = new Server(8080);

        ServletContextHandler context = new ServletContextHandler(
            ServletContextHandler.SESSIONS
        );
        context.setContextPath("/");
        server.setHandler(context);

        context.addServlet(
            new ServletHolder(new EntropyServlet(sessionEntropyService)),
            "/entropy/*"
        );
        context.addServlet(
            new ServletHolder(new SessionServlet(sessionEntropyService)),
            "/session/*"
        );
        context.addServlet(
            new ServletHolder(new TeamDynamicsServlet(sessionEntropyService)),
            "/teamdynamics/*"
        );
        server.start();
        System.out.println("Server started on 8080");
        server.join();
    }
}
