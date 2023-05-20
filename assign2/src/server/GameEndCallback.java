package server;

import java.nio.channels.ClosedChannelException;

public interface GameEndCallback {
    void onGameEnd(Game game) throws ClosedChannelException;
}
