package wkwk.csv;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class CsvReader {
    List<String> lines = null;

    public void csvLoad() {
        Path path = Paths.get("BotTextData.csv");
        try {
            this.lines = Files.readAllLines(path, StandardCharsets.UTF_8);
        } catch (IOException e) {
            path = Paths.get("src/main/resources/BotTextData.csv");
            try {
                this.lines = Files.readAllLines(path, StandardCharsets.UTF_8);
            } catch (IOException ex) {
                System.out.println("ファイル読み込みに失敗");
            }
        }
    }
}
