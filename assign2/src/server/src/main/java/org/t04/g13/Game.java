package server.src.main.java.org.t04.g13;

import java.io.*;
import java.net.Socket;
import java.util.*;

public class Game extends Thread {


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

    public void start() {
        this.initGame();
        for(int i = 0; i < Utils.NUM_QUESTIONS; i++) {
            this.gameRound();
        }
        this.messageEveryone(Utils.GAME_END);
    }
    public void initGame() {
        this.messageEveryone(Utils.GAME_START);
    }

    public void gameRound() {
        Random r = new Random();
        int idx = r.nextInt(questions.size() - 1);
        Question question =  questions.get(idx);
        String ask = "Q: " + question.getQuestion();
        List<String> all = question.getAnswers();
        this.messageEveryone(ask);

        for(int j = 0; j < 4; j++) {
            String opt = Integer.toString(j+1) + ") " + all.get(j) +'\n';
            this.messageEveryone(opt);
        }
        this.messageEveryone(Utils.ANSWER_TIME);

        for(Player play: players) {
            Socket playerSocket = play.getClientSocket();
            String answer = Utils.readResponse(playerSocket);
            System.out.println(answer);
            int answerIdx = Integer.parseInt(answer);
            if(all.get(answerIdx).equals(question.getCorrectAnswer())) {
                Utils.sendMessage(playerSocket,"Correct Answer!!");
            }
            else {
                Utils.sendMessage(playerSocket,"Wrong Answer!!");

            }
        }
    }
    public void displayQuestions() {
        for(int i = 0; i < Utils.NUM_QUESTIONS; i++) {
            Question quest = questions.get(i);
            List<String> all = quest.getAnswers();
            String question = "Q: " + quest.getQuestion();
            this.messageEveryone(question);
            for(int j = 0; j < 4; j++) {
                String opt = Integer.toString(j+1) + ") " + all.get(j) +'\n';
                this.messageEveryone(opt);
            }
            this.messageEveryone("Type your answer:");
        }
    }

    public void messageEveryone(String message) {
        for(Player play: players) {
            Socket socket = play.getClientSocket();
            Utils.sendMessage(socket,message);
        }
    }
}
