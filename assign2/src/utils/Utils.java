package utils;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Utils {

    public static int MAX_GAMES = 2;
    public static int MAX_PLAYERS = 2;
    public static int MAX_QUESTIONS = 3;
    public static int ANSWER_TIMEOUT_SECONDS = 4;
    public static int CORRECT_ANSWER_POINTS = 5;
    public static int MAX_LOSS_CONNECTION_TIME_SECONDS = 5;
    public static int MAX_ELO_DIFF = 50;
    public static int MAX_WAITING_TIME_SECONDS = 10;
    public static int ELO_RELAX_FACTOR = 5;
    public static int WAITING_TIME_RELAX_FACTOR_SECONDS = 5;

    public static String authMenu =
        """
        ------ Authentication ------
        1. Register
        2. Login
        3. Disconnect
        Pick an option from the above:\s
        """;

    public static String queueMenu =
        """
        ------ Gamemode ------
        1. Normal
        2. Ranked
        3. Disconnect
        Pick an option from the above:\s
        """;

    private static final Object readDBLock = new Object();
    private static final Object insertDBLock = new Object();

    /**
     * Sends a given message through a given channel.
     * @param message The message to send.
     * @param channel The channel to send through.
     * @throws IOException If writing to channel fails.
     * @throws IllegalArgumentException If the message, the channel or both is null.
     */
    public static void sendData(final String message, final SocketChannel channel) throws IOException, IllegalArgumentException {
        if (Objects.isNull(message) || Objects.isNull(channel)) {
            throw new IllegalArgumentException("Required message or channel are null.");
        }

        ByteBuffer buffer = ByteBuffer.wrap(message.trim().getBytes());
        while (buffer.hasRemaining()) {
            channel.write(buffer);
        }
    }

    /**
     * Reads data from a given channel.
     * @param channel The channel from which the data will be read.
     * @return A list containing all messages received, if data was received, null otherwise.
     * @throws IOException If reading from the channel fails.
     */
    public static String[] readData(SocketChannel channel) throws IOException {
        ByteBuffer buffer = ByteBuffer.allocate(1024);
        int bytesRead = channel.read(buffer);

        // Server closed the connection
        if (bytesRead == -1) {
            return null;
        }

        if(bytesRead > 0){
            buffer.flip();
            byte[] data = new byte[buffer.limit()];
            buffer.get(data);
            return (new String(data, StandardCharsets.UTF_8)).split("&&");
        }

        return null;
    }

    /**
     * Ends the connection between a given channel and the server.
     * @param channel The channel from which the connection will end.
     * @throws IOException If closing the channel fails.
     */
    public static void disconnectClient(SocketChannel channel) throws IOException {
        channel.close();
        System.out.println("Client disconnected: " + channel.getRemoteAddress());
    }

    /**
     * Attempts to retrieve a given user from the database. This function is thread synchronized.
     * @param file The file containing all the registered users' info.
     * @param username The username of the user.
     * @param password The password of the user
     * @return An instance of User containing its info, if user was found, null otherwise
     */
    public static User getUserFromDB(String file, String username, String password) {
        synchronized (readDBLock){
            String searchValue = username + "," + password;

            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                String currentLine;

                while ((currentLine = reader.readLine()) != null) {
                    String[] values = currentLine.split(",");
                    String lineValue = values[0] + "," + values[1];

                    if (lineValue.equals(searchValue)) {
                        return new User(values[0], Integer.parseInt(values[2]), UserState.AUTHENTICATED, null);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        }
    }

    /**
     * Registers a new user with given username and password into the database. The verification if the user already exists should be done prior to the call of this function, because if it isn't, a user might be duplicated. This function is thread synchronized.
     * @param file The file containing all the registered users' info.
     * @param username The username of the user.
     * @param password The password of the user.
     * @param elo The elo score of the user.
     */
    public static void putUserInDB(String file, String username, String password, String elo) {

        synchronized (insertDBLock) { // Synchronize on the lock object
            try (FileWriter writer = new FileWriter(file, true)) {
                writer.append(username).append(",").append(password).append(",").append(elo).append("\n");
                System.out.println("User registered on database: " + file);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Parses a given string of credentials (username and password) in HTTP GET format.
     * @param credentials The credentials from which the username and password will be retrieved.
     * @return A string array containing the username and password retrieved, in that order.
     */
    public static String[] parseCredentials(String credentials){
        String[] result = new String[2];
        int usernameIndex = credentials.indexOf("?username=") + 10;
        int passwordIndex = credentials.indexOf("?password=") + 10;

        int usernameEndIndex = credentials.indexOf("?password=");
        int passwordEndIndex = credentials.length();

        String username = credentials.substring(usernameIndex, usernameEndIndex);
        String password = credentials.substring(passwordIndex, passwordEndIndex);

        result[0] = username;
        result[1] = password;

        return result;
    }

    /**
     * Finds if an operation was successful by the header of a given server response.
     * @param response The response from the operation.
     * @return true if the operation was successful, false otherwise.
     */
    public static boolean wasOperationSuccessful(String response){
        return response.startsWith("[SUCCESS]");
    }

    /**
     * Finds if the game has started by the header of a given server response.
     * @param response The response from the server.
     * @return true if the game has started, false otherwise.
     */
    public static boolean hasGameStarted(String response){
        return response.startsWith("[START]");
    }

    /**
     * Finds if the game has ended by the header of a given server response.
     * @param response The response from the server.
     * @return true if the game has ended, false otherwise.
     */
    public static boolean hasGameEnded(String response){
        return response.startsWith("[END]");
    }

    /**
     * Attempts to retrieve a list of questions from the database.
     * @param file The file containing all the questions' info.
     * @return A list of Question objects containing each questions' info.
     */
    public static List<Question> getQuestionsFromDB(String file){
        List<Question> questions = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length == 6) {
                    String question = parts[0].trim();
                    String answer1 = parts[1].trim();
                    String answer2 = parts[2].trim();
                    String answer3 = parts[3].trim();
                    String answer4 = parts[4].trim();
                    String correctAnswer = parts[5].trim();

                    Question q = new Question(question, answer1, answer2, answer3, answer4, correctAnswer);
                    questions.add(q);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return questions;
    }

    /**
     * Finds if a given question is the last question of a game by the header of a given server response.
     * @param response The response from the server.
     * @return true if the question is the last question of a game, false otherwise.
     */
    public static boolean isLastQuestion(String response){
        return response.startsWith("[QUESTION " + MAX_QUESTIONS + "]");
    }

    /**
     * Finds if a given answer is correct by the header of a given server response.
     * @param response The response from the server.
     * @return true if the answer is correct, false otherwise.
     */
    public static boolean isAnswerCorrect(String response){
        return response.startsWith("[CORRECT]");
    }

    /**
     * Updates the elo of a user player.
     * @param file The file where the user's information is stored.
     * @param username The username of the user which elo's going to be updated.
     * @param newELO The new elo.
     */
    public static void updatePlayerELO(String file, String username, int newELO) {
        try {
            File inputFile = new File(file);
            File tempFile = new File(tempFilePath(file));

            BufferedReader reader = new BufferedReader(new FileReader(inputFile));
            BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile));

            List<String> linesToUpdate = new ArrayList<>();

            String line;
            while ((line = reader.readLine()) != null) {
                String[] data = line.split(",");
                if (data.length >= 3 && data[0].equals(username)) {
                    data[2] = String.valueOf(newELO); // Update the ELO value
                    line = String.join(",", data);
                }
                linesToUpdate.add(line);
            }

            // Write all the updated lines to the temporary file
            for (String updatedLine : linesToUpdate) {
                writer.write(updatedLine);
                writer.newLine();
            }

            // Close the readers/writers
            reader.close();
            writer.close();

            // Replace the original file with the temporary file
            if (inputFile.delete()) {
                tempFile.renameTo(inputFile);
            } else {
                throw new IOException("Failed to update player's ELO. Unable to delete original file.");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Generates a temporary file path based on a given file path.
     * @param filePath The original file path.
     * @return Temporary file path ("temp_" appended to the start of the file").
     */
    private static String tempFilePath(String filePath) {
        Path path = Paths.get(filePath);
        String fileName = path.getFileName().toString();
        String transformedFileName = "temp_" + fileName;
        Path transformedPath = path.resolveSibling(transformedFileName);
        return transformedPath.toString();
    }

}

