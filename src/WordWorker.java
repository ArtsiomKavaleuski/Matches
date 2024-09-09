import org.apache.poi.hssf.record.cf.BorderFormatting;
import org.apache.poi.ss.usermodel.PrintSetup;
import org.apache.poi.xwpf.model.XWPFHeaderFooterPolicy;
import org.apache.poi.xwpf.usermodel.*;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.*;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.impl.CTPImpl;

import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;


public class WordWorker {
    public void mergeRow(XWPFTableRow secondRow, String text) {
        XWPFTableCell cell2 = secondRow.getTableCells().getFirst();
        cell2.setVerticalAlignment(XWPFTableCell.XWPFVertAlign.CENTER);
        XWPFParagraph par = cell2.getParagraphs().getFirst();
        par.setSpacingBetween(1);
        par.setAlignment(ParagraphAlignment.CENTER);
        par.setSpacingBefore(0);
        par.setSpacingAfter(0);
        XWPFRun cell1Run = par.createRun();
        cell1Run.setBold(true);
        cell1Run.setFontFamily("Cambria");
        cell1Run.setText(text);

        CTTcPr tcpr = secondRow.getTableCells().getFirst().getCTTc().addNewTcPr();
        CTHMerge cthMerge = tcpr.addNewHMerge();
        cthMerge.setVal(STMerge.RESTART);
        for (int i = 1; i < secondRow.getTableCells().size(); i++) {
            tcpr = secondRow.getTableCells().get(i).getCTTc().addNewTcPr();
            cthMerge = tcpr.addNewHMerge();
            cthMerge.setVal(STMerge.CONTINUE);
        }
    }

    public String dateConverter(LocalDate date) {
        String month = null;
        String day = null;
        switch (date.getMonth()) {
            case Month.APRIL:
                month = "апреля";
                break;
            case Month.MAY:
                month = "мая";
                break;
            case Month.JUNE:
                month = "июня";
                break;
            case Month.JULY:
                month = "июля";
                break;
            case Month.AUGUST:
                month = "августа";
                break;
            case Month.SEPTEMBER:
                month = "сентября";
                break;
            case Month.OCTOBER:
                 month = "октября";
                break;
            default:
                month = "month";
                break;
        }

        switch(date.getDayOfWeek()) {
            case MONDAY:
                day = "понедельник";
                break;
            case TUESDAY:
                day = "вторник";
                break;
            case WEDNESDAY:
                day = "среда";
                break;
            case THURSDAY:
                day = "четверг";
                break;
            case FRIDAY:
                day = "пятница";
                break;
            case SATURDAY:
                day = "суббота";
                break;
            case SUNDAY:
                day = "воскресенье";
                break;
        }
        return String.valueOf(date.getDayOfMonth()) + " " + month + " " + "(" + day + ")";
    }

    public static void writeToDocument(List<Match> matches) throws IOException, InterruptedException {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("DD.MM.YYYY");

        List<LocalDate> dates = matches.stream()
                .map(m -> m.date)
                .distinct()
                .sorted()
                .toList();

        int numberOfDates = dates.size();
        HashMap<LocalDate, List<Match>> matchesPerDate = new HashMap<>();
        for (LocalDate d : dates) {
            matchesPerDate.put(d, matches.stream().filter(match -> match.date.equals(d)).toList());
        }


        HashMap<Integer, List<String>> mainCells = new HashMap<>();
        mainCells.put(0, List.of("№", "п/п"));
        mainCells.put(1, List.of("Играющие команды,", "место проведения."));
        mainCells.put(2, List.of("Время"));
        mainCells.put(3, List.of("ТВ"));
        mainCells.put(4, List.of("Судья"));
        mainCells.put(5, List.of("Помощник"));
        mainCells.put(6, List.of("Помощник"));
        mainCells.put(7, List.of("Резервный судья"));
        mainCells.put(8, List.of("Инспектор", "Делегат"));

        try {
            // создаем модель docx документа,
            // к которой будем прикручивать наполнение (колонтитулы, текст)
            XWPFDocument document = new XWPFDocument();
            //CTSectPr ctSectPr = document.getDocument().getBody().addNewSectPr();

            CTBody body = document.getDocument().getBody();
            if (!body.isSetSectPr()) {
                body.addNewSectPr();
            }

            CTSectPr section = body.getSectPr();
            if (!section.isSetPgSz()) {
                section.addNewPgSz();
            }

            CTPageSz pageSize = section.getPgSz();
            pageSize.setOrient(STPageOrientation.LANDSCAPE);
            pageSize.setW(BigInteger.valueOf(15840));
            pageSize.setH(BigInteger.valueOf(12240));

            // создаем обычный параграф, который будет расположен слева,
            // будет синим курсивом со шрифтом 25 размера
            XWPFParagraph paragraph = document.createParagraph();
            paragraph.setAlignment(ParagraphAlignment.CENTER);
            paragraph.setSpacingBetween(1);
            XWPFRun run = paragraph.createRun();
            run.setFontFamily("Cambria");
            run.setFontSize(11);
            run.setBold(true);
            // HEX цвет без решетки #
            run.setText("Чемпионат Республики Беларусь по футболу сезона 2024");
            run.addBreak();
            run.setText("среди команд первой лиги.");
            run.addBreak();
            run.addBreak();
            run.setText(" тур        " +
                    "{} - {} {} года ({} - {})");
            run.addBreak();
            run.addBreak();
            XWPFTable table = document.createTable(13, 9);

            XWPFTableRow firstRow = table.getRows().get(0);

            for (int i = 0; i < firstRow.getTableCells().size(); i++) {
                XWPFTableCell cell = firstRow.getTableCells().get(i);
                cell.setVerticalAlignment(XWPFTableCell.XWPFVertAlign.CENTER);
                XWPFParagraph par = cell.getParagraphs().getFirst();
                par.setSpacingBetween(1);
                par.setAlignment(ParagraphAlignment.CENTER);
                par.setSpacingBefore(0);
                par.setSpacingAfter(0);
                XWPFRun cell1Run = par.createRun();
                cell1Run.setBold(true);
                cell1Run.setFontFamily("Cambria");
                if (mainCells.get(i).size() < 2) {
                    cell1Run.setText(mainCells.get(i).getFirst());
                } else {
                    cell1Run.setText(mainCells.get(i).getFirst());
                    cell1Run.addBreak();
                    cell1Run.setText(mainCells.get(i).getLast());
                }
            }

            WordWorker ww = new WordWorker();

            int counter = 1;
            int n = 1;

            for(LocalDate d: dates) {
                ww.mergeRow(table.getRows().get(counter), ww.dateConverter(d));
                counter++;
                for(Match m : matchesPerDate.get(d)) {
                    XWPFTableRow row = table.getRows().get(counter);
                    counter++;
                    XWPFTableCell cell = row.getTableCells().get(0);
                    cell.setVerticalAlignment(XWPFTableCell.XWPFVertAlign.CENTER);
                    XWPFParagraph par = cell.getParagraphs().getFirst();
                    par.setSpacingBetween(1);
                    par.setAlignment(ParagraphAlignment.LEFT);
                    par.setSpacingBefore(0);
                    par.setSpacingAfter(0);
                    XWPFRun cellRun = par.createRun();
                    cellRun.setBold(true);
                    cellRun.setFontFamily("Cambria");
                    cellRun.setText(String.valueOf(n));
                    n++;

                    XWPFTableCell cell1 = row.getTableCells().get(1);
                    cell1.setVerticalAlignment(XWPFTableCell.XWPFVertAlign.CENTER);
                    XWPFParagraph par1 = cell1.getParagraphs().getFirst();
                    par1.setSpacingBetween(1);
                    par1.setAlignment(ParagraphAlignment.LEFT);
                    par1.setSpacingBefore(0);
                    par1.setSpacingAfter(0);
                    XWPFRun cell1Run = par1.createRun();
                    cell1Run.setBold(true);
                    cell1Run.setFontFamily("Cambria");
                    cell1Run.setText(m.matchDescription.toUpperCase());

                    XWPFTableCell cell2 = row.getTableCells().get(2);
                    cell2.setVerticalAlignment(XWPFTableCell.XWPFVertAlign.CENTER);
                    XWPFParagraph par2 = cell2.getParagraphs().getFirst();
                    par2.setSpacingBetween(1);
                    par2.setAlignment(ParagraphAlignment.LEFT);
                    par2.setSpacingBefore(0);
                    par2.setSpacingAfter(0);
                    XWPFRun cell2Run = par2.createRun();
                    cell2Run.setBold(true);
                    cell2Run.setFontFamily("Cambria");
                    cell2Run.setText(m.dateTime.format(DateTimeFormatter.ofPattern("HH:mm")));

                }
            }


            // сохраняем модель docx документа в файл
            FileOutputStream outputStream = new FileOutputStream("src/resources/Apache POI Word Test.docx");
            document.write(outputStream);
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("Успешно записан в файл");
    }


}
