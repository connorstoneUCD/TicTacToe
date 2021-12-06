package edu.ucdenver.client.app;

import edu.ucdenver.client.Client;
import javafx.event.ActionEvent;
import javafx.scene.control.*;

import java.io.IOException;
import java.util.Arrays;

public class Controller {
    public Button btn_LookForGame;
    public Label lbl_PlayerID;
    public Button btn_ConnectToServer;
    public Button btn_TopLeft;
    public Button btn_BotRight;
    public Button btn_BotMid;
    public Button btn_BotLeft;
    public Button btn_MidRight;
    public Button btn_Mid;
    public Button btn_MidLeft;
    public Button btn_TopRight;
    public Button btn_TopMid;
    public Button btn_Return;
    public Label lbl_Match;
    public Tab tab_Game;
    public Tab tab_Connect;
    public TabPane tabPane;

    private Client client;

    public void initialize() {
        client = new Client();

        if (client.connect()) {
            btn_ConnectToServer.setDisable(true); // only activate the "connect to server" button if the initial connection is not successful
            lbl_PlayerID.setText(client.getId());
        }
        tab_Game.setDisable(true);
    }

    public void lookForGame(ActionEvent actionEvent) {
        try {
            client.print("Looking for game....");
            String in = client.sendMessage("client " + client.getId() + " is looking for game");
            if (in.contains("starting game between")) {
                client.print(in);

                if (client.getId().equals(in.split(" ")[3]) || client.getId().equals(in.split(" ")[5])) {
                    if (client.getId().equals(in.split(" ")[3])) client.setOpposingId(in.split(" ")[5]);
                    else client.setOpposingId(in.split(" ")[3]);

                    tab_Game.setDisable(false);
                    tab_Connect.setDisable(true);
                    tabPane.getSelectionModel().select(tab_Game);
                } else {
                    client.print(String.format("Received invalid string from server: '%s'", in));
                }
            }
        } catch (IOException e) {
            client.print("Something went wrong when looking for game.");
        }
    }

    public void connect(ActionEvent actionEvent) {
        if (client.connect()) {
            btn_ConnectToServer.setDisable(true); // only activate the "connect to server" button if the initial connection is not successful
            lbl_PlayerID.setText(client.getId());
        }
    }

    public void topLeftPressed(ActionEvent actionEvent) {
        this.pressButton(0);
    }

    public void topMidPressed(ActionEvent actionEvent) {
        this.pressButton(1);
    }

    public void topRightPressed(ActionEvent actionEvent) {
        this.pressButton(2);
    }

    public void midLeftPressed(ActionEvent actionEvent) {
        this.pressButton(3);
    }

    public void midPressed(ActionEvent actionEvent) {
        this.pressButton(4);
    }

    public void midRightPressed(ActionEvent actionEvent) {
        this.pressButton(5);
    }

    public void botLeftPressed(ActionEvent actionEvent) {
        this.pressButton(6);
    }

    public void botMidPressed(ActionEvent actionEvent) {
        this.pressButton(7);
    }

    public void botRightPressed(ActionEvent actionEvent) {
        this.pressButton(8);
    }

    public void returnToLogin(ActionEvent actionEvent) {
        try {
            client.sendMessage(String.format("%s: %s %s concedes", Arrays.toString(client.getBoard()), client.getOpposingId(), client.getId()));
            client.setOpposingId("");
            tab_Game.setDisable(true);
            tab_Connect.setDisable(false);
            tabPane.getSelectionModel().select(tab_Connect);
        } catch (IOException e) {
            client.print("Something went wrong while conceding a game with " + client.getOpposingId());
        }
    }

    private void pressButton(int index) {
        char[] newBoard = client.getBoard();

        if (newBoard[index] != ' ') {
            try {
                newBoard[index] = client.getSymbol();
                client.setBoard(newBoard);
                client.sendMessage(Arrays.toString(newBoard) + " " + client.getOpposingId());

                if (client.getInput().readLine().contains(client.getOpposingId() + " won") || client.getInput().readLine().contains(client.getOpposingId() + " concedes")) {
                    String alertMsg = "Your opponent " + client.getInput().readLine().split(" ")[2];
                    Alert alert = new Alert(Alert.AlertType.INFORMATION, alertMsg);
                    alert.show();
                    goIdle();
                }
            } catch (IOException e) {
                client.print("Something went wrong when sending a button command.");
            }
        }
    }

    private void goIdle() {
        try {
            client.sendMessage(client.getId() + " going idle");
            client.print("Going idle....");
            client.setOpposingId("");
            tab_Game.setDisable(true);
            tab_Connect.setDisable(false);
            tabPane.getSelectionModel().select(tab_Connect);
        } catch (IOException e) {
            client.print("Something went wrong when going idle.");
        }
    }
}
