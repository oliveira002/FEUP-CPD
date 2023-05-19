package server;

import utils.Question;
import utils.User;
import utils.UserState;

import java.io.IOException;
import java.nio.channels.SocketChannel;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static server.Server.USER_CREDENTIALS;
import static utils.Utils.*;

public class Game implements Runnable{
    private static final String QUESTIONS = "src/server/questions.txt";
    private List<User> gamePlayers;
    private GameType gameType;
    private int gameNr;
    private ExecutorService playersPool;
    private List<Question> allQuestions;
    private List<Question> questions = new ArrayList<>();
    private final Object updateEloLock = new Object();


    public Game(List<User> gamePlayers, GameType gameType, int gameNr){
        this.gamePlayers = gamePlayers;
        this.gameType = gameType;
        this.playersPool = Executors.newFixedThreadPool(gamePlayers.size());
        this.allQuestions = getQuestionsFromDB(QUESTIONS);
        this.gameNr = gameNr;
    }

    @Override
    public void run() {
        generateGameQuestions();

        System.out.println("[START] Game #" + gameNr + " has started with players:");

        for(User player : gamePlayers){
            playersPool.execute(() -> {
                try {
                    int eloGained = 0;
                    int questionNr = 0;
                    SocketChannel channel = player.getChannel();

                    System.out.println(player.username);
                    sendData("[START] Game has started, you have " + ANSWER_TIMEOUT_SECONDS + " seconds to answer each question, with a total of " + MAX_QUESTIONS + " questions.", channel);

                    player.state = UserState.WAITING_QUESTION;

                    boolean gameEnded = false;
                    String[] answer = new String[1];
                    answer[0] = "ola";
                    Question question = new Question(null, null, null, null,null,null);
                    while (!gameEnded) {
                        switch (player.state) {
                            case WAITING_QUESTION -> {

                                if(questionNr == MAX_QUESTIONS){
                                    player.state = UserState.GAME_ENDED;
                                    continue;
                                }

                                question = questions.get(questionNr++);

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

                                answer = readData(channel);
                                while(answer == null){
                                    answer = readData(channel);
                                }

                                player.state = UserState.WAITING_ANSWER_EVAL;

                            }
                            case WAITING_ANSWER_EVAL -> {

                                String answer_aux = answer[0];
                                String time = answer[1];
                                boolean isAnswerCorrect;
                                int answerChoice;

                                if(Objects.equals(answer_aux, "NULL")){
                                    isAnswerCorrect = false;
                                    sendData("[INCORRECT]", channel);
                                }
                                else{
                                    answerChoice = Integer.parseInt(answer_aux)-1;
                                    isAnswerCorrect = Objects.equals(question.getAnswers().get(answerChoice), question.getCorrectAnswer());

                                    if(isAnswerCorrect) sendData("[CORRECT]", channel);
                                    else {
                                        sendData("[INCORRECT]", channel);
                                    }
                                }
                                eloGained += calculatePoints(isAnswerCorrect, Double.parseDouble(time)/1000.0);

                                System.out.println("\nPlayer: "+player.username
                                        +"\nAnswer: "+answer_aux
                                        +"\nTime: "+(Double.parseDouble(time)/1000.0)
                                        +"\nPoints: " + calculatePoints(isAnswerCorrect, Double.parseDouble(time)/1000.0)
                                        +"\nTotal elo gained: "+eloGained);
                                player.state = UserState.WAITING_QUESTION;
                            }
                            case GAME_ENDED -> {
                                gameEnded = true;

                                if(gameType == GameType.RANKED){
                                    player.elo += eloGained;
                                    if(eloGained >= 0)
                                        sendData("You gained " + (eloGained) + " elo points this game.", channel);
                                    else
                                        sendData("You lost " + Math.abs(eloGained) + " elo points this game.", channel);

                                    synchronized (updateEloLock){
                                        updatePlayerELO(USER_CREDENTIALS, player.username, player.elo);
                                    }
                                }
                                else sendData("This was a normal game, so you did not gain any elo points.", channel);
                                player.state = UserState.AUTHENTICATED;
                            }
                        }
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
        }


        try {
            boolean isIdle = playersPool.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);

            if(isIdle){
                System.out.println("[END] Game #" + gameNr + " has ended");
                playersPool.shutdown();
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
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

    private static int calculatePoints(boolean isCorrect, double timeTaken) {
        if (!isCorrect) {
            return -2*CORRECT_ANSWER_POINTS/3;
        }

        // Calculate the percentage of time taken compared to the timeout duration
        double timePercentage = timeTaken / ANSWER_TIMEOUT_SECONDS;

        // Calculate points based on the percentage of time taken, the less time, the more points
        return (int) (CORRECT_ANSWER_POINTS + (CORRECT_ANSWER_POINTS * (1-timePercentage)));
    }

}