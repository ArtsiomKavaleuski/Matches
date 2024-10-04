import javax.swing.*;
import java.io.IOException;
import java.util.List;

public class Report {
    public static void main(String[] args) {
        Manager manager = new Manager();
        try {
            manager.loadResults();
            WordWorker.writeToDocument(manager.getSortedMatches(manager.VL, 24));
            WordWorker.writeToDocument(manager.getSortedMatches(manager.PL, 27));
            WordWorker.writeToDocument(manager.getSortedMatches(manager.ZVL, 27));
            WordWorker.writeToDocument(manager.getSortedMatches(List.of(manager.groupA.toString(), manager.groupB.toString(), manager.groupC.toString(), manager.groupD.toString()), 2));
        } catch (InterruptedException | IOException e) {
            throw new RuntimeException(e);
        }
        JOptionPane.showMessageDialog(null, "Файлы готовы!",
                "Ready", JOptionPane.INFORMATION_MESSAGE);
    }
}
