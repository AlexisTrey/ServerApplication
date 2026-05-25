package co.edu.uptc.view;

import co.edu.uptc.components.dialog.AboutDialog;
import co.edu.uptc.dto.PlayerDto;
import co.edu.uptc.interfaces.PresenterInterface;
import co.edu.uptc.interfaces.ViewInterface;
import co.edu.uptc.pojo.GameStatus;
import co.edu.uptc.pojo.Player;
import co.edu.uptc.util.ThemeManager;
import co.edu.uptc.util.Utilities;

import javax.swing.*;
import java.awt.*;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ServerFrame extends JFrame implements ViewInterface {

    private static ServerFrame instance;
    private PresenterInterface presenter;
    private GamePanel          gamePanel;
    private InfoPanel          infoPanel;

    private ServerFrame() {
        initFrame();
        addComponents();
    }

    public static ServerFrame getInstance() {
        if (instance == null) instance = new ServerFrame();
        return instance;
    }

    private void initFrame() {
        setTitle("Combat Game — Servidor");
        setSize(Utilities.FRAME_WIDTH, Utilities.FRAME_HEIGHT);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        setResizable(false);
    }

    private void addComponents() {
        addMenuBar();
        addGamePanel();
        addInfoPanel();
    }

    private void addMenuBar() {
        JMenuBar bar  = new JMenuBar();
        bar.add(buildGameMenu());
        bar.add(buildThemeMenu());
        bar.add(buildAboutMenu());
        setJMenuBar(bar);
    }

    private JMenu buildGameMenu() {
        JMenu menu = new JMenu("Juego");
        JMenuItem itemStart = new JMenuItem("Iniciar partida");
        JMenuItem itemEnd   = new JMenuItem("Finalizar partida");
        JMenuItem itemExit  = new JMenuItem("Salir");
        itemStart.addActionListener(e -> { if (presenter != null) presenter.onStartGame(); });
        itemEnd.addActionListener(e   -> { if (presenter != null) presenter.onEndGame(); });
        itemExit.addActionListener(e  -> System.exit(0));
        menu.add(itemStart);
        menu.add(itemEnd);
        menu.addSeparator();
        menu.add(itemExit);
        return menu;
    }

    private JMenu buildThemeMenu() {
        JMenu menu  = new JMenu("Tema");
        JMenuItem light = new JMenuItem("Claro");
        JMenuItem dark  = new JMenuItem("Oscuro");
        light.addActionListener(e -> ThemeManager.applyByKey(ThemeManager.LIGHT));
        dark.addActionListener(e  -> ThemeManager.applyByKey(ThemeManager.DARK));
        menu.add(light);
        menu.add(dark);
        return menu;
    }

    private JMenu buildAboutMenu() {
        JMenu menu = new JMenu("Acerca de");
        JMenuItem item = new JMenuItem("Info del proyecto");
        item.addActionListener(e -> showAbout());
        menu.add(item);
        return menu;
    }

    private void addGamePanel() {
        gamePanel = new GamePanel();
        add(gamePanel, BorderLayout.CENTER);
    }

    private void addInfoPanel() {
        infoPanel = new InfoPanel();
        add(infoPanel, BorderLayout.EAST);
    }

    private void showAbout() {
        Map<String, String> info = new LinkedHashMap<>();
        info.put("Proyecto:",  "Combat Game");
        info.put("Versión:",   "1.0.0");
        info.put("Lenguaje:",  "Java 21");
        new AboutDialog(this, "Servidor — Combat Game", info).setVisible(true);
    }

    @Override
    public void setPresenter(PresenterInterface presenter) {
        this.presenter = presenter;
        infoPanel.setPresenter(presenter);
    }

    @Override
    public void start() {
        setVisible(true);
    }

    @Override
    public void refresh() {
        gamePanel.repaint();
    }

    @Override
    public void updateGameState(List<PlayerDto> players) {
        gamePanel.updatePlayers(players);
    }

    @Override
    public void updatePlayerList(List<Player> players) {
        infoPanel.updatePlayerList(players);
    }

    @Override
    public void setGameStatus(GameStatus status) {
        infoPanel.setGameStatus(status);
    }
}
