package server;


import utils.User;
import utils.UserState;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.nio.channels.*;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


import static utils.Utils.*;


public class Server implements GameEndCallback {

    private static final int PORT = 8000;
    protected static final String USER_CREDENTIALS = "src/server/users.txt";
    private static final String TOKENS = "src/server/tokens.txt";
    private ServerSocketChannel serverSocketChannel;
    private Selector selector;
    private Map<SocketChannel, User> connectedClients = new HashMap<SocketChannel, User>();
    private Deque<User> normalQueue = new ArrayDeque<>();
    private List<User> rankedQueue = new ArrayList<>();
    private final Object normalQueueLock = new Object();
    private final Object rankedQueueLock = new Object();
    private final Object connectedClientsLock = new Object();
    private ExecutorService gamePool;
    private final ExecutorService rankedMMPool = Executors.newSingleThreadExecutor();
    private int gameNr = 1;

    public Server() {
        try{
            serverSocketChannel = ServerSocketChannel.open();
            serverSocketChannel.socket().bind(new InetSocketAddress(PORT));
            serverSocketChannel.configureBlocking(false);
            selector = Selector.open();
            serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
            System.out.println("Server started and listening on port " + PORT);

            gamePool = Executors.newFixedThreadPool(MAX_GAMES);

        }
        catch (IOException e){
            e.printStackTrace();
        }

    }

    /**
     * Manages the connected clients with the usage of java.nio's non-blocking I/O features.
     */
    public void start(){
        try {
            RankedMatchmaking rankedMatchmaking = new RankedMatchmaking(this);
            rankedMMPool.execute(rankedMatchmaking);

            while (true) {
                selector.select();
                Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();

                while (iterator.hasNext()) {
                    SelectionKey key = iterator.next();
                    iterator.remove();

                    if (key.isValid()) {
                        if (key.isAcceptable()) {
                            acceptConnection(key);
                        } else if (key.isReadable()) {
                            readClientMessage(key);
                        } else {
                            throw new UnsupportedOperationException("Key not supported by server.");
                        }
                    } else {
                        throw new UnsupportedOperationException("Key not valid.");
                    }
                }
            }
        } catch (IOException e) {
            //selector.close();
            //serverSocketChannel.close();
            e.printStackTrace();
        }
    }

    /**
     * Handles the connection of clients. When a client connects, he's added to a connectedClients map.
     * @param key The key which triggered the new client connection event.
     * @throws IOException If accepting a new connection fails.
     */
    private void acceptConnection(SelectionKey key) throws IOException {
        SocketChannel socketChannel = ((ServerSocketChannel) key.channel()).accept();
        socketChannel.configureBlocking(false);
        socketChannel.register(selector, SelectionKey.OP_READ);
        System.out.println("New client connected: " + socketChannel.getRemoteAddress());
        connectedClients.put(socketChannel, new User());
    }

    private void readClientMessage(SelectionKey key) throws IOException {

        SocketChannel socketChannel = (SocketChannel) key.channel();
        User client = connectedClients.get(socketChannel);

        try {
            String[] messages = readData(socketChannel);

            assert messages != null;
            for (String message : messages) {
                switch (client.state) {
                    case CONNECTED -> {
                        switch (Integer.parseInt(message)) {
                            case 0 -> {
                                client.state = UserState.TOKEN_LOGIN;
                            }
                            case 1 -> {
                                client.state = UserState.REGISTER;
                            }
                            case 2 -> {
                                client.state = UserState.LOGIN;
                            }
                            case 3 -> {
                                client.state = UserState.DISCONNECTED;
                                System.out.println("Client disconnected: " + socketChannel.getRemoteAddress());
                                socketChannel.keyFor(selector).cancel();
                                socketChannel.close();
                                connectedClients.remove(socketChannel);
                            }
                        }
                    }
                    case REGISTER -> {
                        String[] credentials = parseCredentials(message);
                        User tempClient = getUserFromDB(USER_CREDENTIALS, credentials[0], credentials[1]);

                        if(tempClient != null){
                            System.out.println("[FAILURE] User already exists: " + socketChannel.getRemoteAddress());
                            sendData("[FAILURE] User already exists!", socketChannel);
                            return;
                        }

                        putUserInDB(USER_CREDENTIALS, credentials[0], credentials[1], "500");

                        System.out.println("[SUCCESS] Registered successfully as " + credentials[0] + ": " + socketChannel.getRemoteAddress());
                        sendData("[SUCCESS] Registered successfully as " + credentials[0], socketChannel);
                        tempClient = new User(credentials[0], 0, UserState.TOKEN_GEN, socketChannel);
                        connectedClients.replace(socketChannel, client, tempClient);
                        client = tempClient;
                    }
                    case LOGIN -> {
                        String[] credentials = parseCredentials(message);
                        User tempClient = getUserFromDB(USER_CREDENTIALS, credentials[0], credentials[1]);

                        if(tempClient == null){
                            System.out.println("[FAILURE] Credentials don't match any in our system: " + socketChannel.getRemoteAddress());
                            sendData("[FAILURE] Credentials don't match any in our system!", socketChannel);
                            return;
                        }

                        System.out.println("[SUCCESS] Logged in successfully as " + credentials[0] + ": " + socketChannel.getRemoteAddress());
                        sendData("[SUCCESS] Logged in successfully as " + credentials[0], socketChannel);
                        tempClient.setChannel(socketChannel);
                        connectedClients.replace(socketChannel, client, tempClient);
                        client = tempClient;

                    }
                    case TOKEN_LOGIN -> {
                       System.out.println("Boas2");
                    }
                    case TOKEN_GEN -> {
                        UUID uuid = UUID.randomUUID();
                        System.out.println("New session token generated for " + client.username + ": " + uuid);
                        storeToken(TOKENS,client.username, uuid.toString());
                        sendData("This is your one time authentication token. Use it in case of a connection loss.\n"+uuid, socketChannel);
                        client.state = UserState.AUTHENTICATED;
                    }
                    case AUTHENTICATED -> {
                        switch (message){
                            //Normal queue
                            case "1" -> {
                                client.state = UserState.NORMAL_QUEUE;
                                synchronized (normalQueueLock) {
                                    manageNormalQueue(client);
                                }
                            }
                            //Ranked queue
                            case "2" -> {
                                client.state = UserState.RANKED_QUEUE;
                                synchronized (rankedQueueLock) {
                                    manageRankedQueue(client);
                                }
                            }
                            //Disconnect
                            case "3" -> {
                                client.state = UserState.DISCONNECTED;
                                System.out.println("Client disconnected: " + socketChannel.getRemoteAddress());
                                revokeToken(TOKENS, client.username);
                                socketChannel.keyFor(selector).cancel();
                                socketChannel.close();
                                connectedClients.remove(socketChannel);
                            }
                        }
                    }
                    case NORMAL_QUEUE, RANKED_QUEUE, IN_GAME, DISCONNECTED -> {}
                    case LOST_CONNECTION -> {}
                }
            }
        } catch (SocketException e) {
            client.state = UserState.LOST_CONNECTION;
            System.out.println("Client lost connection: " + socketChannel.getRemoteAddress());
            socketChannel.close();
            connectedClients.remove(socketChannel);
        }
    }

    private void manageNormalQueue(User client) {
        normalQueue.add(client);
        client.startQueueTimer();
        System.out.println("Client " + client.username + " joined normal queue");

        if(normalQueue.size() >= MAX_PLAYERS){

            List<User> gamePlayers = new ArrayList<>();

            for(int i = 0; i < MAX_PLAYERS; i++){
                User player = normalQueue.poll();
                assert player != null;
                System.out.println(player.username + " removed from normal queue");
                player.state = UserState.IN_GAME;

                SelectionKey key = player.getChannel().keyFor(selector);
                if (key.isValid()) {
                    int ops = key.interestOps();
                    ops &= ~SelectionKey.OP_READ;
                    key.interestOps(ops);
                }
                gamePlayers.add(player);
            }

            Game game = new Game(gamePlayers, GameType.NORMAL, gameNr++);
            game.setGameEndCallback(this); // 'this' refers to the current Server instance
            gamePool.execute(game);
        }
    }

    private void manageRankedQueue(User client) {
        rankedQueue.add(client);
        client.startQueueTimer();
        System.out.println("Client " + client.username + " joined ranked queue");
    }

    public void rankedMatchMaking() {

        List<List<User>> allTeams = new ArrayList<>();
        for (User player : rankedQueue) {
            if(!player.getChannel().isConnected()) {
                continue;
            }
            int maxDiff = player.getQueueTime() * 5;
            boolean added = false;

            for (List<User> team : allTeams) {
                int teamSize = team.size();
                int totalElo = team.stream().mapToInt(User::getElo).sum();
                int averageElo = totalElo / teamSize;
                System.out.println("Team ELO:" + averageElo);
                System.out.println("Player ELO - " + (player.getElo() - maxDiff) + " & " + (player.getElo() + maxDiff));

                if (Math.abs(averageElo - player.getElo()) <= maxDiff && teamSize < MAX_PLAYERS) {
                    team.add(player);
                    added = true;
                    break;
                }
            }

            if (!added) {
                List<User> newTeam = new ArrayList<>();
                newTeam.add(player);
                allTeams.add(newTeam);
            }
        }

        for (List<User> gameTeam : allTeams) {
            if (gameTeam.size() == MAX_PLAYERS) {
                Game game = new Game(gameTeam, GameType.RANKED, gameNr++);
                game.setGameEndCallback(this); // 'this' refers to the current Server instance
                gamePool.execute(game);
                for (User player : gameTeam) {
                    player.state = UserState.IN_GAME;

                    SelectionKey key = player.getChannel().keyFor(selector);
                    if (key.isValid()) {
                        int ops = key.interestOps();
                        ops &= ~SelectionKey.OP_READ;
                        key.interestOps(ops);
                    }
                    player.stopQueueTime();
                    synchronized (rankedQueueLock) {
                        rankedQueue.remove(player);
                    }
                }
            }
        }
    }

    private void increaseEloBounds() {
        for (User user : rankedQueue) {
            int queueTime = user.getQueueTime();
            int newEloDiff = ELO_RELAX_FACTOR * queueTime;

            user.minElo -= newEloDiff;
            user.maxElo += newEloDiff;
        }
    }

    @Override
    public void onGameEnd(Game game) throws ClosedChannelException {
        System.out.println("\n[END] Game #" + game.getGameNr() + " has ended");
        List<User> gamePlayers = game.getGamePlayers();
        for (User player : gamePlayers){
            player.state = UserState.AUTHENTICATED;
            SocketChannel playerChannel = player.getChannel();
            SelectionKey key = player.getChannel().keyFor(selector);
            if(key.isValid()) {
                int ops = key.interestOps();
                ops |= SelectionKey.OP_READ;
                key.interestOps(ops);
                User temp = connectedClients.get(playerChannel);
                connectedClients.replace(playerChannel, temp, player);
                selector.wakeup();
            }
            else{
                connectedClients.remove(playerChannel);
            }
        }
    }

    public static void main(String[] args) {
        Server server = new Server();
        server.start();
    }

}
