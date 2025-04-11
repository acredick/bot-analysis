import java.io.*;
import java.nio.file.*;
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

     public static void main(String arg) {
          File folder = new File(arg); // Replace with actual path
          File[] csvFiles = folder.listFiles((dir, name) -> name.toLowerCase().endsWith(".csv"));

          if (csvFiles == null || csvFiles.length == 0) {
               System.out.println("No CSV files found.");
               return;
          }

          for (File csv : csvFiles) {
               processCSV(csv);
          }

          // Print result
          printMoveTypeFrequency();
     }

     private static void processCSV(File file) {
          try (BufferedReader br = new BufferedReader(new FileReader(file))) {
               List<Double> yValues = new ArrayList<>();

               String line;
               while ((line = br.readLine()) != null) {
                    String[] parts = line.split(",");
                    if (parts.length >= 2) {  // Ensure both x and y values are present
                         try {
                              double y = Double.parseDouble(parts[1].trim());  // Only consider y values
                              yValues.add(y);
                         } catch (NumberFormatException ignored) {
                         }
                    }
               }

               for (int i = 0; i < yValues.size() - 1; i++) {
                    double diff = yValues.get(i + 1) - yValues.get(i);
                    String moveType = classifyMove(diff);
                    moveTypeFrequency.put(moveType, moveTypeFrequency.get(moveType) + 1);
               }

          } catch (IOException e) {
               System.out.println("Error reading file: " + file.getName());
               e.printStackTrace();
          }
     }

     private static String classifyMove(double diff) {
          if (diff >= 90) return "great";                // 90 and above
          if (diff >= 70) return "strong";               // 70 to 89
          if (diff >= 30) return "good";                 // 30 to 69
          if (diff >= -30) return "neutral";             // -30 to 29
          if (diff >= -60) return "mistake";             // -30 to -59
          if (diff >= -70) return "blunder";             // -60 to -69
          return "major blunder";                        // -70 and below
     }

     private static void printMoveTypeFrequency() {
          System.out.println("\n--- Move Type Frequency ---");

          // Total count of all moves
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

               // Calculate percentage
               double percentage = (totalMoves > 0) ? (count / (double) totalMoves) * 100 : 0;

               // Format and print the frequency and percentage
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
