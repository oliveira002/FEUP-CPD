package client;

import utils.UserState;
import utils.Utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.nio.channels.SocketChannel;
import java.util.Arrays;
import java.util.Scanner;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static utils.Utils.*;

public class Client {
    private static int PORT;
    private static String HOSTNAME;
    private static final String DEFAULT_ANSWER = "NULL";
    private SocketChannel socketChannel;
    private UserState state;
    private ScheduledExecutorService answerTimer;
    private boolean lastQuestion = false;

    public Client(int port, String hostname){
        this.PORT = port;
        this.HOSTNAME = hostname;

        try {
            socketChannel = SocketChannel.open();
            socketChannel.connect(new InetSocketAddress(HOSTNAME, PORT));
            socketChannel.configureBlocking(true); //Supposed to be in blocking mode for client

            while (!socketChannel.finishConnect()) {
                System.out.println("Attempting to connect to server at " + HOSTNAME + ":" + PORT);
            }

            state = UserState.CONNECTED;
            System.out.println("Connected to the server at " + socketChannel.getRemoteAddress());
        }
        catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void connect(){
        while(true){
            try{
                switch(state){
                    case CONNECTED -> authMenu();
                    case REGISTER, LOGIN -> credentialsMenu();
                    case TOKEN_LOGIN -> tokenLogin();
                    case TOKEN_GEN -> getToken();
                    case WAITING_TOKEN_RESPONSE -> getTokenResponse();
                    case AUTHENTICATED -> queueSelectMenu();
                    case NORMAL_QUEUE, RANKED_QUEUE -> checkGameStart();
                    case WAITING_QUESTION ->  getQuestion();
                    case SENDING_ANSWER -> sendAnswer();
                    case WAITING_ANSWER_EVAL -> answerEval();
                    case GAME_ENDED -> gameEnded();
                }

            } catch (IOException | InterruptedException e) {
                throw new RuntimeException("Unable to communicate with server.", e);
            }
        }
    }

    public void authMenu() throws IOException {

        Scanner scanner = new Scanner(System.in);

        System.out.print(Utils.authMenu);
        String option = scanner.nextLine();

        sendData(option, socketChannel);

        switch(Integer.parseInt(option)){
            case 0 -> state = UserState.TOKEN_LOGIN;
            case 1 -> state = UserState.REGISTER;
            case 2 -> state = UserState.LOGIN;
            case 3 -> {
                state = UserState.DISCONNECTED;
                System.out.println("Disconnected from server at " + socketChannel.getRemoteAddress());
                socketChannel.close();
                System.exit(1);
            }
        }
    }

    private void credentialsMenu() throws IOException, InterruptedException {

        Scanner scanner = new Scanner(System.in);
        System.out.println("\n------ " + (state == UserState.REGISTER ? "Register" : "Log in") + " ------");

        System.out.print("Username: ");
        String username = scanner.nextLine();

        System.out.print("Password: ");
        String password = scanner.nextLine();

        sendData("?username=" + username + "?password=" + password, socketChannel);

        String[] response = readData(socketChannel);

        assert response != null;
        for (String message : response) {
            if(wasOperationSuccessful(message)){
                state = UserState.TOKEN_GEN;
            }
            System.out.println(message);
        }

    }

    private void tokenLogin() throws IOException {

        Scanner scanner = new Scanner(System.in);

        System.out.println("\n------ Token Login ------");
        System.out.print("Token: ");
        String token = scanner.nextLine();

        sendData(token, socketChannel);

        String[] response = readData(socketChannel);

        assert response != null;
        for (String message : response) {
            if(wasOperationSuccessful(message)){
                state = UserState.WAITING_TOKEN_RESPONSE;
            }
            System.out.println(message);
        }

    }

    private void getToken() throws IOException {

        sendData("[TOKEN]", socketChannel);

        String[] response = readData(socketChannel);

        assert response != null;
        for (String message : response) {
            System.out.println(message);
        }
        state = UserState.AUTHENTICATED;

    }

    private void getTokenResponse() throws IOException {
        sendData("dummy", socketChannel);

        String[] response = readData(socketChannel);

        assert response != null;
        for (String message : response) {
            System.out.println(message);
            if(message.startsWith("[QUEUE_SELECT]")) state = UserState.AUTHENTICATED;
            else if(message.startsWith("[NORMAL]")) state = UserState.NORMAL_QUEUE;
            else if(message.startsWith("[RANKED]")) state = UserState.RANKED_QUEUE;
        }
    }

    private void queueSelectMenu() throws IOException {

        Scanner scanner = new Scanner(System.in);

        System.out.print("\n"+Utils.queueMenu);
        String option = scanner.nextLine();

        sendData(option, socketChannel);

        switch(Integer.parseInt(option)){
            case 1 -> state = UserState.NORMAL_QUEUE;
            case 2 -> state = UserState.RANKED_QUEUE;
            case 3 -> {
                state = UserState.DISCONNECTED;
                System.out.println("Disconnected from server at " + socketChannel.getRemoteAddress());
                socketChannel.close();
                System.exit(1);
            }
        }

        System.out.printf("You are in queue for a %s game. When a match is found the game will start.\n", state == UserState.NORMAL_QUEUE ? "normal" : "ranked");

    }

    private void checkGameStart() throws IOException {

        String[] response = readData(socketChannel);

        assert response != null;
        System.out.println();
        for (String message : response) {
            if(hasGameStarted(message)){
                state = UserState.WAITING_QUESTION;
            }
            System.out.println(message);
        }
    }

    private void getQuestion() throws IOException {
        String[] response = readData(socketChannel);
        lastQuestion = false;

        assert response != null;
        System.out.println();
        for (String message : response) {
            if(isLastQuestion(message)){
                lastQuestion = true;
            }
            System.out.println(message);
        }
        state = UserState.SENDING_ANSWER;
    }

    private void sendAnswer() throws IOException {

        System.out.print("Answer: ");

        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        String answer = "";
        long elapsedTime = 0;
        long answerTime = 0;
        long startTime = System.currentTimeMillis();
        while (elapsedTime < ANSWER_TIMEOUT_SECONDS * 1000L) {
            if (reader.ready()) {
                answer = reader.readLine();
                answerTime = System.currentTimeMillis() - startTime;
            }

            elapsedTime = System.currentTimeMillis() - startTime;
        }

        System.out.println((answer.isEmpty()? "\n" : "") + "Time's up! Submitting answers.");

        answer = answer.isEmpty() ? DEFAULT_ANSWER : answer;
        answerTime = answerTime == 0 ? ANSWER_TIMEOUT_SECONDS * 1000L : answerTime;
        sendData(answer +"&&"+ answerTime, socketChannel);

        state = UserState.WAITING_ANSWER_EVAL;
    }

    private void answerEval() throws IOException {

        String[] response = readData(socketChannel);

        assert response != null;
        for (String message : response) {
            System.out.println(isAnswerCorrect(message)? "Correct answer!" : "Incorrect answer!");
        }

        if(lastQuestion)
            state = UserState.GAME_ENDED;
        else
            state = UserState.WAITING_QUESTION;
    }

    private void gameEnded() throws IOException {
        System.out.println("\nGame has ended!");

        String[] response = readData(socketChannel);

        assert response != null;
        for (String message : response) {
            System.out.println(message);
        }

        state = UserState.AUTHENTICATED;
    }

    public static void main(String[] args) {

        int port = 8000;

        if (args.length < 2) {
            System.out.println("Invalid arguments. Usage: Client.java <hostname> <port>");
            return;
        }

        String hostname = args[0];

        try{
            port = Integer.parseInt(args[1]);
        }
        catch (NumberFormatException e){
            System.out.printf("Second argument \"port\" has to be an integer, %s not allowed!%n", args[1]);
            System.out.printf("Using default port: %d%n%n", port);
        }

        Client client = new Client(port, hostname);
        client.connect();
    }
}
