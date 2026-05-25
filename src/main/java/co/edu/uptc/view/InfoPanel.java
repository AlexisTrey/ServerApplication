package co.edu.uptc.view;

import co.edu.uptc.components.button.CustomButton;
import co.edu.uptc.components.fonts.AppFonts;
import co.edu.uptc.interfaces.PresenterInterface;
import co.edu.uptc.pojo.GameStatus;
import co.edu.uptc.pojo.Player;
import co.edu.uptc.pojo.Role;
import co.edu.uptc.util.Utilities;

import javax.swing.*;
import java.awt.*;

import java.util.List;

public class InfoPanel extends JPanel {

    private PresenterInterface presenter;
    private JLabel             lblStatus;
    private JTextArea          areaPlayers;
    private JSpinner           spinnerSpeed;
    private CustomButton btnStart;
    private CustomButton       btnEnd;

    public InfoPanel() {
        setPreferredSize(new Dimension(
                Utilities.INFO_PANEL_WIDTH,
                Utilities.GAME_PANEL_HEIGHT));
        setBorder(BorderFactory.createTitledBorder("Server Info"));
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        addStatusSection();
        addHorizontalSeparator();
        addPlayersSection();
        addHorizontalSeparator();
        addControlsSection();
    }

    private void addStatusSection() {
        JPanel panel = buildRow();
        panel.add(new JLabel("Estado:  ", SwingConstants.LEFT));
        lblStatus = new JLabel("WAITING");
        lblStatus.setFont(AppFonts.BODY_BOLD);
        panel.add(lblStatus);
        add(panel);
    }

    private void addPlayersSection() {
        JPanel header = buildRow();
        header.add(createLabel("Jugadores conectados:", AppFonts.BODY_BOLD));
        add(header);

        areaPlayers = new JTextArea("--");
        areaPlayers.setEditable(false);
        areaPlayers.setFont(AppFonts.BODY);
        areaPlayers.setBackground(getBackground());

        JScrollPane scroll = new JScrollPane(areaPlayers);
        scroll.setPreferredSize(new Dimension(
                Utilities.INFO_PANEL_WIDTH - 20, 260));
        scroll.setMaximumSize(new Dimension(
                Utilities.INFO_PANEL_WIDTH - 20, 260));
        scroll.setBorder(BorderFactory.createEtchedBorder());
        add(scroll);
    }

    private void addControlsSection() {
        add(buildSpeedRow());
        add(Box.createVerticalStrut(8));
        btnStart = new CustomButton("Iniciar partida")
                .onClick(e -> { if (presenter != null) presenter.onStartGame(); });
        btnEnd   = new CustomButton("Finalizar partida")
                .onClick(e -> { if (presenter != null) presenter.onEndGame(); });
        btnEnd.setEnabled(false);
        JPanel btnPanel = buildRow();
        btnPanel.add(btnStart);
        add(btnPanel);
        JPanel endPanel = buildRow();
        endPanel.add(btnEnd);
        add(endPanel);
    }

    private JPanel buildSpeedRow() {
        JPanel panel = buildRow();
        panel.add(createLabel("Velocidad (ms):", AppFonts.BODY));
        SpinnerNumberModel model = new SpinnerNumberModel(
                Utilities.DEFAULT_SPEED_MS, 100, 2000, 50);
        spinnerSpeed = new JSpinner(model);
        spinnerSpeed.setMaximumSize(new Dimension(90, 30));
        spinnerSpeed.addChangeListener(e -> {
            if (presenter != null)
                presenter.onSetSpeed((int) spinnerSpeed.getValue());
        });
        panel.add(spinnerSpeed);
        return panel;
    }

    public void updatePlayerList(List<Player> players) {
        if (players.isEmpty()) {
            areaPlayers.setText("Sin jugadores");
            return;
        }
        StringBuilder sb = new StringBuilder();
        for (Player p : players) {
            sb.append("P").append(p.getShortId())
                    .append(" [").append(p.getStudentCode()).append("]");
            if (p.getRole() != null) {
                String roleLabel = (p.getRole() == Role.ATTACKER) ? "ATK" : "DEF";
                sb.append("  ").append(roleLabel)
                        .append("  Pts:").append(p.getScore());
            }
            sb.append("\n");
        }
        areaPlayers.setText(sb.toString());
    }

    public void setGameStatus(GameStatus status) {
        lblStatus.setText(status.name());
        lblStatus.setForeground(statusColor(status));
        btnStart.setEnabled(status == GameStatus.WAITING);
        btnEnd.setEnabled(status == GameStatus.IN_PROGRESS);
    }

    public void setPresenter(PresenterInterface presenter) {
        this.presenter = presenter;
    }

    private Color statusColor(GameStatus status) {
        return switch (status) {
            case WAITING     -> new Color(200, 130, 0);
            case IN_PROGRESS -> new Color(0, 140, 0);
            case FINISHED    -> new Color(160, 0, 0);
        };
    }

    private void addHorizontalSeparator() {
        JSeparator sep = new JSeparator(SwingConstants.HORIZONTAL);
        sep.setMaximumSize(new Dimension(Utilities.INFO_PANEL_WIDTH - 20, 8));
        add(sep);
        add(Box.createVerticalStrut(4));
    }

    private JPanel buildRow() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        panel.setOpaque(false);
        panel.setMaximumSize(new Dimension(Utilities.INFO_PANEL_WIDTH, 40));
        return panel;
    }

    private JLabel createLabel(String text, Font font) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(font);
        return lbl;
    }
}