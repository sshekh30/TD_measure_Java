package parser;

public class ParserFactory {

    public static DataParser createParser(String dataSourceType) {
        switch (dataSourceType.toLowerCase()) {
            case "file":
                return new FileFormatParser();
            case "kafka":
                throw new IllegalArgumentException("Unknown parser type: " + dataSourceType);
            case "mongo":
                throw new IllegalArgumentException("Unknown parser type: " + dataSourceType);
            default:
                throw new IllegalArgumentException("Unknown parser type: " + dataSourceType);
        }
    }
}