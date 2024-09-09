import java.io.IOException;

public class Report {
    public static void main(String[] args) {
        Manager manager = new Manager();
        try {
            manager.loadResults();
            WordWorker.writeToDocument(manager.getSortedMatches(manager.PL, 22));
        } catch (InterruptedException | IOException e) {
            throw new RuntimeException(e);
        }
    }
}
