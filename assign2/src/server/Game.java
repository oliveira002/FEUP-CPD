package server;

import utils.Question;
import utils.User;
import utils.UserState;

import java.io.IOException;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static utils.Utils.*;

public class Game implements Runnable{
    private static final String QUESTIONS = "src/server/questions.txt";
    private List<User> gamePlayers;
    private GameType gameType;
    private ExecutorService playersPool;
    private List<Question> allQuestions;
    private List<Question> questions = new ArrayList<>();
    private ScheduledExecutorService answerTimer;

    public Game(List<User> gamePlayers, GameType gameType){
        this.gamePlayers = gamePlayers;
        this.gameType = gameType;
        this.playersPool = Executors.newFixedThreadPool(gamePlayers.size());
        this.allQuestions = getQuestionsFromDB(QUESTIONS);
    }

    @Override
    public void run() {
        generateGameQuestions();

        for(User player : gamePlayers){
            playersPool.execute(() -> {
                try {
                    int questionNr = 0;
                    SocketChannel channel = player.getChannel();

                    System.out.println("[START] Game has started for player: " + player.username);
                    sendData("[START] Game has started, you have " + ANSWER_TIMEOUT_SECONDS + " seconds to answer each question, with a total of " + MAX_QUESTIONS + " questions.", channel);

                    player.state = UserState.WAITING_QUESTION;

                    boolean gameEnded = false;
                    while (!gameEnded) {
                        switch (player.state) {
                            case WAITING_QUESTION -> {

                                if(questionNr == MAX_QUESTIONS){
                                    gameEnded = true;
                                    player.state = UserState.AUTHENTICATED;
                                    break;
                                }

                                Question question = questions.get(questionNr++);

                                StringBuilder questionMsg = new StringBuilder("[QUESTION " + (questionNr) + "] " + question.getQuestion() + "&&");
                                int ansNr = 1;
                                for (String ans : question.getAnswers()) {
                                    questionMsg.append(ansNr).append(": ").append(ans).append("&&");
                                    ansNr++;
                                }

                                sendData(questionMsg.toString(), channel);

                                player.state = UserState.SENDING_ANSWER;
                            }
                            case SENDING_ANSWER -> {

                                answerTimer = Executors.newSingleThreadScheduledExecutor();

                                answerTimer.schedule(() -> {
                                    String[] answer;
                                    try {
                                        answer = readData(channel);
                                    } catch (IOException e) {
                                        throw new RuntimeException(e);
                                    }
                                    assert answer != null;
                                    for (String aux : answer){
                                        System.out.println(aux);
                                    }

                                    player.state = UserState.WAITING_QUESTION;

                                }, ANSWER_TIMEOUT_SECONDS+1, TimeUnit.SECONDS);

                            }
                        }
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
        }
    }

    private void generateGameQuestions(){

        int allQuestionsSize = allQuestions.size();

        if(allQuestionsSize <= MAX_QUESTIONS){
            this.questions = allQuestions;
            return;
        }

        // Generate MAX_QUESTIONS unique random indices within the range of originalList
        Random random = new Random();
        List<Integer> randomIndices = new ArrayList<>();

        while (randomIndices.size() < MAX_QUESTIONS) {
            int randomIndex = random.nextInt(allQuestionsSize);
            if (!randomIndices.contains(randomIndex)) {
                randomIndices.add(randomIndex);
            }
        }

        // Retrieve the questions at the random indices and add them to the result list
        for (int index : randomIndices) {
            questions.add(allQuestions.get(index));
        }
    }

    private void sendQuestion(){

    }

}