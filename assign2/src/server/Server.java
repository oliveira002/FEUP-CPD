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

    private final int PORT;
    protected static final String USER_CREDENTIALS = "src/server/users.txt";
    private static final String TOKENS = "src/server/tokens.txt";
    private ServerSocketChannel serverSocketChannel;
    private Selector selector;
    private Map<SocketChannel, User> connectedClients = new HashMap<SocketChannel, User>();
    private List<User> lostConnectionClients = new ArrayList<>();
    private Deque<User> normalQueue = new ArrayDeque<>();
    private List<User> rankedQueue = new ArrayList<>();
    private final Object normalQueueLock = new Object();
    private final Object rankedQueueLock = new Object();
    private final Object gamePoolLock = new Object();
    private ExecutorService gamePool;
    private final ExecutorService rankedMMPool = Executors.newSingleThreadExecutor();
    private final ExecutorService lostConnectionClientsPool = Executors.newSingleThreadExecutor();
    private int gameNr = 1;

    public Server(int port) {
        this.PORT = port;

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
            LostConnection lostConnection = new LostConnection(this);
            lostConnectionClientsPool.execute(lostConnection);

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

                        putUserInDB(USER_CREDENTIALS, credentials[0], credentials[1], String.valueOf(DEFAULT_ELO));

                        System.out.println("[SUCCESS] Registered successfully as " + credentials[0] + ": " + socketChannel.getRemoteAddress());
                        sendData("[SUCCESS] Registered successfully as " + credentials[0], socketChannel);
                        tempClient = new User(credentials[0], DEFAULT_ELO, UserState.TOKEN_GEN, socketChannel);
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
                        if(!isTokenValid(TOKENS, message)){
                            System.out.println("[FAILURE] Session token is not valid: " + socketChannel.getRemoteAddress());
                            sendData("[FAILURE] Session token is not valid! It's either wrong or has been revoked.", socketChannel);
                            return;
                        }

                        String username = getUsernameFromToken(TOKENS, message);
                        //Should never be null but wtv
                        if(username == null){return;}

                        revokeToken(TOKENS, username);
                        User tempClient = getUserFromListByUsername(lostConnectionClients, username);
                        assert tempClient != null;
                        tempClient.stopLossConnectionTime();
                        tempClient.setChannel(socketChannel);
                        tempClient.state = UserState.WAITING_TOKEN_RESPONSE;
                        lostConnectionClients.remove(tempClient);
                        connectedClients.put(socketChannel, tempClient);
                        client = tempClient;
                        System.out.println("[SUCCESS] Logged in successfully as " + client.username + " using session token: " + socketChannel.getRemoteAddress());
                        sendData("[SUCCESS] Logged in successfully as " + client.username + " using session token. This token has been revoked.", socketChannel);
                    }
                    case TOKEN_GEN -> {
                        UUID uuid = UUID.randomUUID();
                        System.out.println("New session token generated for " + client.username + ": " + uuid);
                        storeToken(TOKENS,client.username, uuid.toString());
                        sendData("This is your one time authentication token. Use it in case of a connection loss.\n"+uuid, socketChannel);
                        client.state = UserState.AUTHENTICATED;
                    }
                    case WAITING_TOKEN_RESPONSE -> {
                        UserState state_aux = getUserQueue(client);

                        if(state_aux == null) {
                            client.state = UserState.AUTHENTICATED;
                            sendData("[QUEUE_SELECT]", socketChannel);
                        }
                        else if (state_aux == UserState.NORMAL_QUEUE) {
                            client.state = UserState.NORMAL_QUEUE;
                            System.out.println("Client " + client.username + " rejoined normal queue");
                            sendData("[NORMAL]", socketChannel);
                            updateNormalQueue(client);
                            handleNormalQueue();
                        }
                        else if (state_aux == UserState.RANKED_QUEUE) {
                            client.state = UserState.RANKED_QUEUE;
                            System.out.println("Client " + client.username + " rejoined ranked queue");
                            sendData("[RANKED]", socketChannel);
                            updateRankedQueue(client);
                        }
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
            client.startLossConectionTimer();
            System.out.println("Client lost connection: " + socketChannel.getRemoteAddress());
            socketChannel.close();
            connectedClients.remove(socketChannel);
            if(client.username != null)
                lostConnectionClients.add(client);
        }
    }

    private void manageNormalQueue(User client) {
        normalQueue.add(client);
        client.startQueueTimer();
        System.out.println("Client " + client.username + " joined normal queue");

        handleNormalQueue();
    }

    private void handleNormalQueue(){
        while(normalQueue.size() >= MAX_PLAYERS){

            List<User> gamePlayers = new ArrayList<>();

            for(int i = 0; i < MAX_PLAYERS; i++){
                User player = normalQueue.poll();
                assert player != null;

                if (player.state == UserState.LOST_CONNECTION) {
                    continue;
                }
                gamePlayers.add(player);
            }

            if(gamePlayers.size() == MAX_PLAYERS) {

                for(User player : gamePlayers){
                    System.out.println(player.username + " removed from normal queue");
                    player.state = UserState.IN_GAME;

                    SelectionKey key = player.getChannel().keyFor(selector);
                    if (key.isValid()) {
                        int ops = key.interestOps();
                        ops &= ~SelectionKey.OP_READ;
                        key.interestOps(ops);
                    }
                }

                Game game = new Game(gamePlayers, GameType.NORMAL, gameNr++);
                game.setGameEndCallback(this); // 'this' refers to the current Server instance
                synchronized (gamePoolLock){
                    gamePool.execute(game);
                }
            }
            else{
                Collections.reverse(gamePlayers);
                for(User player : gamePlayers)
                    normalQueue.addFirst(player);
            }
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
                        System.out.println(player.username + " removed from ranked queue");
                    }
                }
                Game game = new Game(gameTeam, GameType.RANKED, gameNr++);
                game.setGameEndCallback(this); // 'this' refers to the current Server instance
                synchronized (gamePoolLock){
                    gamePool.execute(game);
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

    public void purgeLostConnections(){
        /*System.out.println("ON: "+ connectedClients.toString());
        System.out.println("DC: "+ lostConnectionClients.toString());
        System.out.println("Normal: " + normalQueue.toString());
        System.out.println();*/
        List<User> removeUsersList = new ArrayList<>();
        for (User client : lostConnectionClients){
            if(client.getLossConnectionTime() == MAX_LOSS_CONNECTION_TIME_SECONDS){
                revokeToken(TOKENS, client.username);
                removeUsersList.add(client);
                removeUserFromQueues(client);
            }
        }
        lostConnectionClients.removeAll(removeUsersList);
    }

    private void removeUserFromQueues(User client){
        Iterator<User> iterator = normalQueue.iterator();
        while (iterator.hasNext()) {
            User user = iterator.next();
            if (user.username.equals(client.username)) {
                synchronized (normalQueueLock) {
                    iterator.remove(); // Remove the user
                }
                System.out.println(user.username + " removed from normal queue due to connection loss");
                return; // Stop iterating once the user is found and removed
            }
        }

        Iterator<User> iterator2 = rankedQueue.iterator();
        while (iterator2.hasNext()) {
            User user = iterator2.next();
            if (user.username.equals(client.username)) {
                synchronized (rankedQueueLock){
                    iterator2.remove(); // Remove the user
                }
                System.out.println(user.username + " removed from ranked queue due to connection loss");
                return; // Stop iterating once the user is found and removed
            }
        }
    }

    private UserState getUserQueue(User client){

        for (User user : normalQueue) {
            if (user.username.equals(client.username)) {
                return UserState.NORMAL_QUEUE;
            }
        }

        for (User user : rankedQueue) {
            if (user.username.equals(client.username)) {
                return UserState.RANKED_QUEUE;
            }
        }
        return null;
    }

    private void updateNormalQueue(User client){

        LinkedList<User> userList = new LinkedList<>(normalQueue);
        User userToReplace = null;
        ListIterator<User> iterator = userList.listIterator();
        while (iterator.hasNext()) {
            User user = iterator.next();
            if (user.username.equals(client.username)) {
                userToReplace = user;
                break;
            }
        }

        if (userToReplace != null) {
            iterator.set(client);
            normalQueue = new ArrayDeque<>(userList);
        }

    }

    private void updateRankedQueue(User client){
        int index = -1;
        for (int i = 0; i < rankedQueue.size(); i++) {
            User user = rankedQueue.get(i);
            if (user.username.equals(client.username)) {
                index = i;
                break;
            }
        }

        if (index != -1) {
            rankedQueue.set(index, client);
        }
    }

    @Override
    public void onGameEnd(Game game) {
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

        int port = 8000;
        if(args.length != 0){
            try{
                port = Integer.parseInt(args[0]);
            }
            catch (NumberFormatException e){
                System.out.printf("First argument \"port\" has to be an integer, %s not allowed!%n", args[0]);
                System.out.printf("Using default port: %d%n%n", port);
            }
        }

        Server server = new Server(port);
        server.start();
    }

}
