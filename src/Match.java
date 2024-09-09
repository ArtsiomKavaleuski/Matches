import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;

public class Match {
    int matchId;
    String matchDateTime;
    String matchDescription;
    int homeTeamId;
    int awayTeamId;
    String championshipName;
    String matchStatus;
    int matchRound;
    LocalDateTime dateTime;
    LocalDate date;

    public Match(int matchId, String matchDateTime, String matchDescription, int homeTeamId,
    int awayTeamId, String championshipName, String matchStatus, int matchRound) {
        this.matchId = matchId;
        this.matchDateTime = matchDateTime;
        this.matchDescription = matchDescription;
        this.homeTeamId = homeTeamId;
        this.awayTeamId = awayTeamId;
        this.championshipName = championshipName;
        this.matchStatus = matchStatus;
        this.matchRound = matchRound;
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
