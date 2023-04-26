package server.src.main.java.org.t04.g13;

import java.io.*;
import java.util.*;

public class Game extends Thread {

    private static final int MAX_PLAYERS = 5;
    private static final int NUM_QUESTIONS = 3;
    private static final int QUESTION_TIME = 80000; // 10 seconds

    private List<Player> players;
    private List<Question> questions;

    public Game() {
        players = new ArrayList<>();
        try {
            questions = this.parseQuestions();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        Collections.shuffle(questions);
    }

    public List<Question> parseQuestions() throws IOException {
        String filename = "src/questions.csv";
        List<Question> all = new ArrayList<>();

        try {
            BufferedReader reader = new BufferedReader(new FileReader(filename));
            String line;
            while ((line = reader.readLine()) != null) {
                String[] fields = line.split(",");
                String question = fields[0];
                String option1 = fields[1];
                String option2 = fields[2];
                String option3 = fields[3];
                String option4 = fields[4];
                String correctAnswer = fields[5];
                Question quest = new Question(question,option1,option2,option3,option4,correctAnswer);
                all.add(quest);
            }
            reader.close();
        } catch (IOException e) {
            System.err.println("Error reading file: " + e.getMessage());
        }
        return all;
    }

    public void addPlayer(Player player) {
        this.players.add(player);
    }

}
