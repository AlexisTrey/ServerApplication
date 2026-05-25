package co.edu.uptc.interfaces;

import co.edu.uptc.network.server.ClientHandler;

public interface PresenterInterface {

    void setModel(ModelInterface model);
    void setView(ViewInterface view);
    void onStartGame();
    void onEndGame();
    void onSetSpeed(int speedMs);
    void onPlayerConnect(String studentCode, ClientHandler handler);
    void onPlayerDisconnect(String studentCode);
    void onPlayerMove(String studentCode, String direction);
    void refreshView();

}
