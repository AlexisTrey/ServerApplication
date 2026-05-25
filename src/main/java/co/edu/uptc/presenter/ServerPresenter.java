package co.edu.uptc.presenter;

import co.edu.uptc.interfaces.ModelInterface;
import co.edu.uptc.interfaces.PresenterInterface;
import co.edu.uptc.interfaces.ViewInterface;
import co.edu.uptc.network.server.ClientHandler;

public class ServerPresenter implements PresenterInterface {


    @Override
    public void setModel(ModelInterface model) {

    }

    @Override
    public void setView(ViewInterface view) {

    }

    @Override
    public void onStartGame() {

    }

    @Override
    public void onEndGame() {

    }

    @Override
    public void onSetSpeed(int speedMs) {

    }

    @Override
    public void onPlayerConnect(String studentCode, ClientHandler handler) {

    }

    @Override
    public void onPlayerDisconnect(String studentCode) {

    }

    @Override
    public void onPlayerMove(String studentCode, String direction) {

    }

    @Override
    public void refreshView() {

    }
}
