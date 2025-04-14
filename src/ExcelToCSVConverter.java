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
                                   String[] values = new String[row.getLastCellNum()];
                                   int lastNonEmptyIndex = -1;
                                   boolean hasData = false;

                                   for (int cn = 0; cn < row.getLastCellNum(); cn++) {
                                        Cell cell = row.getCell(cn, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
                                        String value = switch (cell.getCellType()) {
                                             case STRING -> cell.getStringCellValue().trim();
                                             case NUMERIC -> String.valueOf(cell.getNumericCellValue());
                                             case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
                                             case FORMULA -> cell.getCellFormula().trim();
                                             default -> "";
                                        };

                                        if (!value.isBlank()) {
                                             lastNonEmptyIndex = cn;
                                             hasData = true;
                                        }

                                        values[cn] = value;
                                   }

                                   // Write only if the row has at least two non-blank columns (x and y)
                                   if (hasData && lastNonEmptyIndex >= 1) {
                                        StringBuilder sb = new StringBuilder();
                                        for (int i2 = 0; i2 <= lastNonEmptyIndex; i2++) {
                                             sb.append(values[i2]);
                                             if (i2 < lastNonEmptyIndex) sb.append(",");
                                        }
                                        writer.println(sb);
                                   }
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
