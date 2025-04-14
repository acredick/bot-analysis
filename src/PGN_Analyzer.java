import com.github.bhlangonijr.chesslib.game.Game;
import com.github.bhlangonijr.chesslib.pgn.PgnHolder;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PGN_Analyzer {

     public static void main(String arg) {

          File folder = new File(arg);
          if (!folder.exists() || !folder.isDirectory()) {
               System.err.println("Error: The provided path is not a valid directory.");
               return;
          }

          File[] files = folder.listFiles((dir, name) -> name.toLowerCase().endsWith(".pgn"));

          // Initialize a map to track wins and games played for each player
          Map<String, Integer> playerWins = new HashMap<>();
          Map<String, Integer> playerGames = new HashMap<>();
          int totalGames = 0;

          if (files != null) {
               for (File file : files) {
                    try {
                         PgnHolder pgn = new PgnHolder(file.getAbsolutePath());
                         pgn.loadPgn();

                         List<Game> games = pgn.getGames();
                         for (Game game : games) {
                              // Track wins for each player
                              String whitePlayer = game.getWhitePlayer().getName();
                              String blackPlayer = game.getBlackPlayer().getName();

                              // Update the number of games played by each player
                              playerGames.put(whitePlayer, playerGames.getOrDefault(whitePlayer, 0) + 1);
                              playerGames.put(blackPlayer, playerGames.getOrDefault(blackPlayer, 0) + 1);

                              String result = String.valueOf(game.getResult());

                              // Track wins based on the result
                              if ("WHITE_WON".equals(result)) {
                                   playerWins.put(whitePlayer, playerWins.getOrDefault(whitePlayer, 0) + 1);
                              } else if ("BLACK_WON".equals(result)) {
                                   playerWins.put(blackPlayer, playerWins.getOrDefault(blackPlayer, 0) + 1);
                              } else if ("1/2-1/2".equals(result)) {
                                   // Draw (no win increment)
                                   System.out.println("Debug: Draw, no wins.");  // Debug output
                              }

                              // Increment total games count
                              totalGames++;
                         }

                    } catch (Exception e) {
                         System.err.println("Error reading the PGN file - " + file.getName());
                         e.printStackTrace();
                    }
               }

               // Now calculate and print win percentages for players who played at least one game
               if (totalGames > 0) {
                    System.out.println("\nResults:");
                    for (Map.Entry<String, Integer> entry : playerGames.entrySet()) {
                         String playerName = entry.getKey();
                         int gamesPlayed = entry.getValue();
                         int wins = playerWins.getOrDefault(playerName, 0);
                         double winPercentage = ((double) wins / gamesPlayed) * 100;

                         System.out.printf("%-15s : %4d wins    %4d games    %.2f%%\n", playerName, wins, gamesPlayed, winPercentage);
                    }
               } else {
                    System.out.println("No games found to analyze.");
               }

          } else {
               System.err.println("Error: No PGN files found in the specified directory.");
          }
     }
}
