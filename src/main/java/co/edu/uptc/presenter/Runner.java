package co.edu.uptc.presenter;

import co.edu.uptc.interfaces.ModelInterface;
import co.edu.uptc.interfaces.PresenterInterface;
import co.edu.uptc.interfaces.ViewInterface;
import co.edu.uptc.model.ServerModel;
import co.edu.uptc.network.server.GameServer;
import co.edu.uptc.view.ServerFrame;

public class Runner {

    private ModelInterface model;
    private ViewInterface view;
    private PresenterInterface presenter;

    public void start() {
        makeMvp();
        startNetworkServer();
        view.start();
    }

    private void makeMvp() {
        model     = new ServerModel();
        presenter = new ServerPresenter();
        view      = ServerFrame.getInstance();
        presenter.setModel(model);
        presenter.setView(view);
        view.setPresenter(presenter);
    }

    private void startNetworkServer() {
        GameServer server = new GameServer(presenter);
        Thread thread     = new Thread(server);
        thread.setDaemon(true);
        thread.start();
    }

}
