import java.io.*;
import java.util.*;

public class MoveAnalyzer {

     private static final Map<String, Integer> moveTypeFrequency = new HashMap<>();

     static {
          moveTypeFrequency.put("great", 0);
          moveTypeFrequency.put("strong", 0);
          moveTypeFrequency.put("good", 0);
          moveTypeFrequency.put("neutral", 0);
          moveTypeFrequency.put("mistake", 0);
          moveTypeFrequency.put("blunder", 0);
          moveTypeFrequency.put("major blunder", 0);
     }

     public static void main(String arg, String bot_name) {
          resetMoveTypeFrequency();
          File folder = new File(arg);
          File[] csvFiles = folder.listFiles((dir, name) -> name.toLowerCase().endsWith(".csv"));

          if (csvFiles == null || csvFiles.length == 0) {
               System.out.println("No CSV files found.");
               return;
          }

          for (File csv : csvFiles) {
               processCSV(csv, bot_name);
          }

          printMoveTypeFrequency(bot_name);
     }

     private static void resetMoveTypeFrequency() {
          moveTypeFrequency.put("great", 0);
          moveTypeFrequency.put("strong", 0);
          moveTypeFrequency.put("good", 0);
          moveTypeFrequency.put("neutral", 0);
          moveTypeFrequency.put("mistake", 0);
          moveTypeFrequency.put("blunder", 0);
          moveTypeFrequency.put("major blunder", 0);
     }

     private static void processCSV(File file, String bot_name) {
          try (BufferedReader br = new BufferedReader(new FileReader(file))) {
               List<Double> yValues = new ArrayList<>();

               String line;
               while ((line = br.readLine()) != null) {
                    line = line.trim();  // Trim whitespace
                    if (line.isEmpty()) continue;  // Skip empty lines

                    String[] parts = line.split(",");
                    if (parts.length >= 2) {
                         try {
                              double y = Double.parseDouble(parts[1].trim());
                              yValues.add(y);
                         } catch (NumberFormatException e) {
                              System.out.println("Skipping malformed row: " + line);
                         }
                    }
               }

               for (int i = 0; i < yValues.size() - 1; i++) {
                    double diff = yValues.get(i + 1) - yValues.get(i);
                    if ("stockfish".equalsIgnoreCase(bot_name)) {
                         diff *= -1; // Reverse the diff for Stockfish bot
                    }
                    String moveType = classifyMove(diff);
                    moveTypeFrequency.put(moveType, moveTypeFrequency.get(moveType) + 1);
               }

          } catch (IOException e) {
               System.out.println("Error reading file: " + file.getName());
               e.printStackTrace();
          }
     }

     private static String classifyMove(double diff) {
          if (diff >= 600) return "great";
          if (diff >= 300) return "strong";
          if (diff >= 100) return "good";
          if (diff >= -100) return "neutral";
          if (diff >= -300) return "mistake";
          if (diff >= -600) return "blunder";
          return "major blunder";
     }

     private static void printMoveTypeFrequency(String bot_name) {
          System.out.println("\n--- " + bot_name + " Move Type Frequency ---");

          int totalMoves = moveTypeFrequency.values().stream().mapToInt(Integer::intValue).sum();

          List<String> moveOrder = Arrays.asList(
                  "great",
                  "strong",
                  "good",
                  "neutral",
                  "mistake",
                  "blunder",
                  "major blunder"
          );

          for (String move : moveOrder) {
               String label = String.format("%-15s", capitalizeWords(move));
               int count = moveTypeFrequency.getOrDefault(move, 0);

               double percentage = (totalMoves > 0) ? (count / (double) totalMoves) * 100 : 0;

               System.out.printf("%-15s: %5d   %.2f%%\n", label, count, percentage);
          }
     }

     private static String capitalizeWords(String input) {
          String[] words = input.split(" ");
          StringBuilder result = new StringBuilder();
          for (String word : words) {
               result.append(Character.toUpperCase(word.charAt(0)))
                       .append(word.substring(1))
                       .append(" ");
          }
          return result.toString().trim();
     }

}
