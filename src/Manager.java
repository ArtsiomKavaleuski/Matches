import com.google.gson.*;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.*;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

public class Manager {
    public static String ZVL = "Женская Высшая лига 2024";
    public static String VL = "Беларусбанк - Высшая лига 2024";
    public static String BK23 = "BETERA - Кубок Беларуси 2023/2024";
    public static String BK24 = "BETERA - Кубок Беларуси 2024/2025";
    public static String PL = "Первая лига 2024";
    public static String ZK24 = "Женский Кубок Беларуси 2024";
    public static String ZSK24 = "Женский Суперкубок 2024";
    public static String BSK24 = "BETERA - Суперкубок Беларуси 2024";
    HashMap<Integer, Match> matches = new HashMap<>();
    HashMap<Integer, String> ref = new HashMap<>();
    HashMap<Integer, String> first = new HashMap<>();
    HashMap<Integer, String> second = new HashMap<>();
    HashMap<Integer, String> fourth = new HashMap<>();
    HashMap<Integer, String> insp = new HashMap<>();
    HashMap<Integer, String> del = new HashMap<>();
    int pages = 0;

    public URI getMatchURI() {
        return URI.create("https://comet.abff.by/data-backend/api/public/areports/run/"
                + pages + "/1000/?API_KEY=bf55c36fddd21f35ec790ea33710c04fc0627559d37aa6e1" +
                "857488ac40f09a78129f63d6ddd792e01fe59a7f8d2418a04dec8d628ed498295ac5360361e07234");
    }

    public URI getRefURI() {
        return URI.create("https://comet.abff.by/data-backend/api/public/areports/run/"
                + pages + "/1000/?API_KEY=f06b6785f05192bd0b92c9bd87271f8c7b57b367519029b8e9b546d0409ddb37f41" +
                "fd918045fd181eb5e2c9e18d0d40c2f7ef852db052335d907eb8a99dffaed");
            }

    public void add(Match match) {
        matches.putIfAbsent(match.matchId, match);
    }

    public void addOfficial(String role, int matchID, String name) {
        String[] splitName = name.split(" ");
        String lastName = splitName[1];
        String firstName = splitName[0];
        String shortName = firstName.substring(0,1) + "." + lastName;
        switch (role) {
            case "Арбитр":
                ref.put(matchID, shortName);
                break;
            case "1й ассистент арбитра":
                first.put(matchID, shortName);
                break;
            case "2й ассистент арбитра":
                second.put(matchID, shortName);
                break;
            case "Четвёртый арбитр":
                fourth.put(matchID, shortName);
                break;
            case "Инспектор":
                insp.put(matchID, shortName);
                break;
            case "Делегат":
                del.put(matchID, shortName);
                break;
        }
    }

    public HashMap<Integer, Match> getMatches() {
        return matches;
    }

    public List<Match> getSortedMatches(String champName) {
        return matches.values().stream()
                .filter(m -> m.championshipName.equals(champName))
                .toList();
    }

    public List<Match> getSortedMatches(String champName, int round) {
        return matches.values().stream()
                .filter(m -> m.championshipName.equals(champName))
                .filter(m -> m.matchRound == round)
                .sorted(new MatchByDateComparator())
                .toList();
    }

    public List<String> getChampionships() {
        return matches.values().stream()
                .map(m -> m.championshipName)
                .distinct()
                .toList();
    }

    public void sortMatchesToFiles() {
        for (int i = 0; i < getChampionships().size(); i++) {
            String championship = getChampionships().get(i);
            String championshipNew = championship;
            if(championship.contains("/")) {
                championshipNew = new String(championship.replace("/", "-"));
            }
            try {
                File file = new File("src/resources", championshipNew + ".csv");
                try (Writer fileWriter = new FileWriter(file)) {
                    for (Match match : getSortedMatches(championship)) {
                        fileWriter.write(match.toString() + "\n");
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    public static void writeIntoExcel(File file) throws FileNotFoundException, IOException{
        Workbook book = new XSSFWorkbook();
        Sheet sheet = book.createSheet("Birthdays");

        // Нумерация начинается с нуля
        Row row = sheet.createRow(0);

        // Мы запишем имя и дату в два столбца
        // имя будет String, а дата рождения --- Date,
        // формата dd.mm.yyyy
        Cell name = row.createCell(0);
        name.setCellValue("John");

        Cell birthdate = row.createCell(1);

        DataFormat format = book.createDataFormat();
        CellStyle dateStyle = book.createCellStyle();
        dateStyle.setDataFormat(format.getFormat("dd.mm.yyyy"));
        birthdate.setCellStyle(dateStyle);


        // Нумерация лет начинается с 1900-го
        birthdate.setCellValue(new Date(110, 10, 10));

        // Меняем размер столбца
        sheet.autoSizeColumn(1);

        // Записываем всё в файл
        book.write(new FileOutputStream(file));
        book.close();
    }

    public void loadResults() throws InterruptedException, IOException {
        JsonArray matches = null;
        JsonArray referees = null;
        while (true) {
            HttpRequest request = HttpRequest.newBuilder()
                    .GET()
                    .uri(getMatchURI())
                    .version(HttpClient.Version.HTTP_1_1)
                    .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.7")
                    .build();
            HttpClient client = HttpClient.newHttpClient();
            HttpResponse.BodyHandler<String> handler = HttpResponse.BodyHandlers.ofString();
            HttpResponse<String> response = client.send(request, handler);
            JsonElement jsonElement = JsonParser.parseString(response.body());
            JsonArray jsonArray = jsonElement.getAsJsonObject().get("results").getAsJsonArray();
            if (jsonArray.isEmpty()) {
                pages = 0;
                break;
            }
            if(matches == null){
                matches = jsonArray;
            } else {
                matches.addAll(jsonArray);
            }
            pages++;
        }

        while (true) {
            HttpRequest request = HttpRequest.newBuilder()
                    .GET()
                    .uri(getRefURI())
                    .version(HttpClient.Version.HTTP_1_1)
                    .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.7")
                    .build();
            HttpClient client = HttpClient.newHttpClient();
            HttpResponse.BodyHandler<String> handler = HttpResponse.BodyHandlers.ofString();
            HttpResponse<String> response = client.send(request, handler);
            JsonElement jsonElement = JsonParser.parseString(response.body());
            JsonArray jsonArray = jsonElement.getAsJsonObject().get("results").getAsJsonArray();
            if (jsonArray.isEmpty()) {
                pages = 0;
                break;
            }
            if(referees == null){
                referees = jsonArray;
            } else {
                referees.addAll(jsonArray);
            }
            pages++;
        }

        for (JsonElement j : referees) {
            if (!j.getAsJsonObject().get("roleStatus").equals("ОТКАЗАНО") && (j.getAsJsonObject().get("name").getAsString().equals("Женская Высшая лига 2024") ||
                    j.getAsJsonObject().get("name").getAsString().contains("Суперкубок") ||
                    j.getAsJsonObject().get("name").getAsString().contains("Беларусбанк") ||
                    j.getAsJsonObject().get("name").getAsString().contains("Первая лига 2024") ||
                    j.getAsJsonObject().get("name").getAsString().contains("Кубок"))) {
                int matchId = Integer.parseInt(j.getAsJsonObject().get("uid").getAsString());
                String officialName = j.getAsJsonObject().get("personName").getAsString();
                String role = j.getAsJsonObject().get("registrationType").getAsString();
                int matchID = Integer.parseInt(j.getAsJsonObject().get("matchId").getAsString());

                addOfficial(role, matchID, officialName);
            }
        }

        for (JsonElement j : matches) {
            if (j.getAsJsonObject().get("name").getAsString().equals("Женская Высшая лига 2024") ||
                    j.getAsJsonObject().get("name").getAsString().contains("Суперкубок") ||
                    j.getAsJsonObject().get("name").getAsString().contains("Беларусбанк") ||
                    j.getAsJsonObject().get("name").getAsString().contains("Первая лига 2024") ||
                    j.getAsJsonObject().get("name").getAsString().contains("Кубок")) {
                int matchId = Integer.parseInt(j.getAsJsonObject().get("uid").getAsString());
                String matchDateTime = j.getAsJsonObject().get("matchDate").getAsString();
                String matchDescription = j.getAsJsonObject().get("matchDescription").getAsString();
                int homeTeamId = Integer.parseInt(j.getAsJsonObject().get("homeTeam").getAsString());
                int awayTeamId = Integer.parseInt(j.getAsJsonObject().get("awayTeam").getAsString());
                String championshipName = j.getAsJsonObject().get("name").getAsString();
                String matchStatus = j.getAsJsonObject().get("matchStatus").getAsString();
                int matchRound = Integer.parseInt(j.getAsJsonObject().get("round").getAsString());
                String city = j.getAsJsonObject().get("facilityPlaceName").getAsString();
                String stadium = j.getAsJsonObject().get("facility").getAsString();
                ArrayList<String> ofNames = new ArrayList<>();
                ofNames.add(ref.get(matchId));
                ofNames.add(first.get(matchId));
                ofNames.add(second.get(matchId));
                ofNames.add(fourth.get(matchId));
                ofNames.add(insp.get(matchId));
                ofNames.add(del.get(matchId));

                Match match = new Match(matchId, matchDateTime, matchDescription, homeTeamId,
                        awayTeamId, championshipName, matchStatus, matchRound, city, stadium, ofNames);
                add(match);
            }
        }

    }

    public static void main(String[] args) throws IOException, InterruptedException {
        Manager manager = new Manager();
        manager.loadResults();
        manager.sortMatchesToFiles();

        System.out.println(ZoneId.getAvailableZoneIds().stream().filter(z -> z.contains("Moscow")).collect(Collectors.joining()));
        for (String s : manager.getChampionships()) {
            System.out.println(s);
        }

        System.out.println(manager.getSortedMatches(manager.PL, 2));

        try {
            File file = new File("src/resources", "test.xlsx");
            writeIntoExcel(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    //System.out.println(manager.matches.values());

        /*
        //List results = jsonArray.asList();

        File dir = new File("src/resources");
        File file = new File(dir, "test.txt");

        try (Writer fileWriter = new FileWriter(file)) {
                for(JsonElement j : jsonArray) {
                    fileWriter.write(String.valueOf(j.getAsJsonObject()) + "\n");
                }
            }

         */


}
