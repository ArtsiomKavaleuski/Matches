import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

public class Match {
    int matchId;
    String matchDateTime;
    String matchDescription;
    int homeTeamId;
    int awayTeamId;
    String championshipName;
    String matchStatus;
    String city;
    String stadium;
    int matchRound;
    LocalDateTime dateTime;
    LocalDate date;
    ArrayList<String> officials;

    public Match(int matchId, String matchDateTime, String matchDescription, int homeTeamId,
    int awayTeamId, String championshipName, String matchStatus, int matchRound, String city, String stadium, ArrayList<String> officials) {
        this.matchId = matchId;
        this.matchDateTime = matchDateTime;
        this.matchDescription = matchDescription.substring(0, matchDescription.length() - 3);
        this.homeTeamId = homeTeamId;
        this.awayTeamId = awayTeamId;
        this.championshipName = championshipName;
        this.matchStatus = matchStatus;
        this.matchRound = matchRound;
        this.city = city;
        this.stadium = stadium;
        this.officials = officials;
        this.dateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(Long.parseLong(matchDateTime)), ZoneId.of("Europe/Moscow"));
        this.date = LocalDate.of(dateTime.getYear(), dateTime.getMonth(), dateTime.getDayOfMonth());
    }

    @Override
    public String toString() {
        return  matchId +
                "," + matchDateTime +
                "," + matchDescription +
                "," + homeTeamId +
                "," + awayTeamId +
                "," + championshipName +
                "," + matchStatus +
                "," + matchRound +
                "," + dateTime;
    }
}
