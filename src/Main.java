public class Main {
     /**
      *
      * @param args: args[0] = path to LC0 folder, args[1] = path to StockFish folder
      */
     public static void main(String[] args) {
          /* Convert all XLSX files to CSV */
          ExcelToCSVConverter.convertToCSV(args[0]);
          ExcelToCSVConverter.convertToCSV(args[1]);

          /* Interpret Lc0 moves */
          MoveAnalyzer.main(args[0], "Lc0");
          MoveAnalyzer.main(args[1], "Stockfish");

          /* Analyze PGNs */
          PGN_Analyzer.main(args[2]);

     }
}