import com.github.bhlangonijr.chesslib.game.Game;
import com.github.bhlangonijr.chesslib.move.MoveList;
import com.github.bhlangonijr.chesslib.pgn.PgnHolder;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.*;

public class NoveltyDetector {

    private static final int OPENING_MOVE_LIMIT = 10;

    static class RareOpening {
        String file;
        String white;
        String black;
        String opening;
        double rarityScore;

        @Override
        public String toString() {
            return "File: " + file + "\n" +
                    "Game: " + white + " vs " + black + "\n" +
                    "Opening: " + opening + "\n" +
                    String.format("Rarity Score: %.2f%%\n", rarityScore);
        }
    }

    public static void main(String[] args) throws Exception {
        if (args.length < 2) {
            System.out.println("Usage: java NoveltyDetector <reference_pgn_path> <user_pgn_path>");
            return;
        }

        String referencePGNPath = args[0]; //path to folder storing the reference pgn dataset
        String userPGNPath = args[1]; //path to folder storing our pgns

        Map<String, Integer> referenceOpeningFrequency = extractOpeningFrequencies(referencePGNPath);
        detectOpeningNovelty(userPGNPath, referenceOpeningFrequency);
    }

    private static Map<String, Integer> extractOpeningFrequencies(String referencePGNPath) throws Exception {
        Map<String, Integer> openingFrequency = new HashMap<>();
        File file = new File(referencePGNPath);

        if (!file.exists()) {
            System.err.println("Reference PGN file not found: " + referencePGNPath);
            return openingFrequency;
        }

        PgnHolder pgn = new PgnHolder(referencePGNPath);
        pgn.loadPgn();

        for (Game game : pgn.getGames())  {
            MoveList moves = game.getHalfMoves();
            StringBuilder sb = new StringBuilder();

            for (int i = 0; i < Math.min(OPENING_MOVE_LIMIT, moves.size()); i++) {
                sb.append(moves.get(i)).append(" ");
            }

            String sequence = sb.toString().trim();
            openingFrequency.put(sequence, openingFrequency.getOrDefault(sequence, 0) + 1);
        }

        System.out.println("Reference openings loaded: " + openingFrequency.size());
        return openingFrequency;
    }

    private static void detectOpeningNovelty(String userPGNDirPath, Map<String, Integer> referenceOpeningFrequency) throws Exception {
        File folder = new File(userPGNDirPath);
        File[] files = folder.listFiles((dir, name) -> name.toLowerCase().endsWith(".pgn"));

        if (files == null || files.length == 0) {
            System.out.println("No PGN files found in user directory.");
            return;
        }

        int totalGames = 0;
        int novelGames = 0;
        List<RareOpening> rareOpenings = new ArrayList<>();

        for (File file : files) {
            PgnHolder pgn = new PgnHolder(file.getAbsolutePath());
            pgn.loadPgn();

            for (Game game : pgn.getGames()) {
                totalGames++;

                MoveList moves = game.getHalfMoves();
                StringBuilder sb = new StringBuilder();

                for (int i = 0; i < Math.min(OPENING_MOVE_LIMIT, moves.size()); i++) {
                    sb.append(moves.get(i)).append(" ");
                }

                String sequence = sb.toString().trim();
                int frequency = referenceOpeningFrequency.getOrDefault(sequence, 0);

                double rarity = 1.0 - (frequency / (double) Collections.max(referenceOpeningFrequency.values()));

                if (rarity > 0.8) {
                    novelGames++;
                    System.out.println("Rare opening detected:");
                    System.out.println("  File: " + file.getName());
                    System.out.println("  Game: " + game.getWhitePlayer().getName() + " vs " + game.getBlackPlayer().getName());
                    System.out.println("  Opening: " + sequence);
                    System.out.printf("  Rarity Score: %.2f%%\n", rarity * 100);
                    System.out.println();

                    RareOpening ro = new RareOpening();
                    ro.file = file.getName();
                    ro.white = game.getWhitePlayer().getName();
                    ro.black = game.getBlackPlayer().getName();
                    ro.opening = sequence;
                    ro.rarityScore = rarity * 100;
                    rareOpenings.add(ro);
                }
            }
        }

        System.out.println("Total user games analyzed: " + totalGames);
        System.out.println("Games with rare openings (rarity > 80%%): " + novelGames);
        double percentNovelty = (totalGames > 0) ? (novelGames / (double) totalGames) * 100 : 0;
        System.out.printf("Novelty rate: %.2f%%\n", percentNovelty);

        try (PrintWriter writer = new PrintWriter(new FileWriter("rare_openings.txt"))) {
            for (RareOpening ro : rareOpenings) {
                writer.println(ro);
                writer.println();
            }
        }

        System.out.println("Rare openings exported to rare_openings.txt");
    }
}
