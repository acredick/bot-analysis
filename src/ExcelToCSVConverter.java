import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.*;

public class ExcelToCSVConverter {
     public static void convertToCSV(String arg) {
          File folder = new File(arg);
          File[] files = folder.listFiles((dir, name) -> name.endsWith(".xlsx"));

          if (files == null || files.length == 0) {
               System.out.println("No .xlsx files found.");
               return;
          }

          for (File excelFile : files) {
               try (FileInputStream fis = new FileInputStream(excelFile);
                    Workbook workbook = new XSSFWorkbook(fis)) {

                    for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
                         Sheet sheet = workbook.getSheetAt(i);
                         String sheetName = sheet.getSheetName();
                         File csvFile = new File(excelFile.getParent(), excelFile.getName().replace(".xlsx", "_" + sheetName + ".csv"));

                         try (PrintWriter writer = new PrintWriter(csvFile)) {
                              for (Row row : sheet) {
                                   StringBuilder sb = new StringBuilder();
                                   for (Cell cell : row) {
                                        switch (cell.getCellType()) {
                                             case STRING -> sb.append(cell.getStringCellValue());
                                             case NUMERIC -> sb.append(cell.getNumericCellValue());
                                             case BOOLEAN -> sb.append(cell.getBooleanCellValue());
                                             case FORMULA -> sb.append(cell.getCellFormula());
                                             default -> sb.append("");
                                        }
                                        sb.append(",");
                                   }
                                   if (!sb.isEmpty()) sb.setLength(sb.length() - 1);
                                   writer.println(sb);
                              }
                         }
                    }

               } catch (IOException e) {
                    System.err.println("Error processing file: " + excelFile.getName());
                    e.printStackTrace();
               }
          }
     }
}
