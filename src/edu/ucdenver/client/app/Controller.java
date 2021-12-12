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
            lbl_PlayerID.setText("Player ID: " + client.getId());
        }
        tab_Game.setDisable(true);
    }

    public void lookForGame(ActionEvent actionEvent) {
        try {
            client.print("Looking for game....");
            String in = client.sendMessage("client " + client.getId() + " is looking for game");
            if (in.contains("starting game between")) {
                if (client.getId().equals(in.split(" ")[3]) || client.getId().equals(in.split(" ")[5])) {
                    if (client.getId().equals(in.split(" ")[3])) {
                        client.setSymbol("X");
                        client.setOpposingId(in.split(" ")[5]);
                    }
                    else {
                        client.setSymbol("O");
                        client.setOpposingId(in.split(" ")[3]);
                    }

                    tab_Game.setDisable(false);
                    tab_Connect.setDisable(true);
                    tabPane.getSelectionModel().select(tab_Game);
                    if (client.sendMessage("client " + client.getId() + " has entered the board").contains(client.getId())) {
                        int opposingPlay = -1;
                        char opposingSymbol = client.getSymbol().equals("X") ? 'O' : 'X';
                        int i = -1;
                        in = client.getInput().readLine();
                        for (char letter : in.toCharArray()) {
                            i++;
                            if (letter == opposingSymbol) break;
                        }
                        updateGUIwithOpposingPlay(i, String.valueOf(opposingSymbol));
                    }
                } else {
                    client.print(String.format("Received invalid string from server: '%s'", in));
                }
            }
        } catch (IOException e) {
            client.print("Something went wrong when looking for game.");
            e.printStackTrace();
        }
    }

    private void updateGUIwithOpposingPlay(int opposingPlay, String opposingSymbol) {
        switch (opposingPlay) {
            case 0: btn_TopLeft.setText(opposingSymbol); break;
            case 1: btn_TopMid.setText(opposingSymbol); break;
            case 2: btn_TopRight.setText(opposingSymbol); break;
            case 3: btn_MidLeft.setText(opposingSymbol); break;
            case 4: btn_Mid.setText(opposingSymbol); break;
            case 5: btn_MidRight.setText(opposingSymbol); break;
            case 6: btn_BotLeft.setText(opposingSymbol); break;
            case 7: btn_BotMid.setText(opposingSymbol); break;
            case 8: btn_BotRight.setText(opposingSymbol); break;
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
            if (btn_Return.getText().equals("Concede")) {
                client.print("Conceding");
                client.sendMessage(String.format("%s concedes", client.getId()));
                client.print("Going idle....");
                client.setOpposingId("");
                tab_Game.setDisable(true);
                tab_Connect.setDisable(false);
                tabPane.getSelectionModel().select(tab_Connect);
            } else {
                goIdle(true);
            }
        } catch (IOException e) {
            client.print("Something went wrong while conceding a game with " + client.getOpposingId());
            e.printStackTrace();
        }
    }

    private boolean checkForWinner(String message, boolean self) {
        if (self) return message.contains(client.getId() + " won");
        else return message.contains(client.getOpposingId() + " won");
    }

    private void popUpWinner(boolean self) {
        String alertMsg = self ? "You won!" : "Your opponent won...";
        Alert alert = new Alert(Alert.AlertType.INFORMATION, alertMsg);
        alert.show();
    }

    private void popUpConceded() {
        String alertMsg = "Your opponent conceded!";
        Alert alert = new Alert(Alert.AlertType.INFORMATION, alertMsg);
        alert.show();
    }

    private boolean checkForTie(String message) {
        return message.contains("nobody");
    }

    private void popUpTie() {
        String alertMsg = "It's a tie!";
        Alert alert = new Alert(Alert.AlertType.INFORMATION, alertMsg);
        alert.show();
    }

    private void pressButton(int index) {
        if (client.getBoard()[index] == '-'){
            try {
                String in = client.sendMessage(client.getSymbol() + " " + index + " " + client.getOpposingId());
                if (in.contains(client.getId())) {
                    switch (index) {
                        case 0: btn_TopLeft.setText(client.getSymbol()); break;
                        case 1: btn_TopMid.setText(client.getSymbol()); break;
                        case 2: btn_TopRight.setText(client.getSymbol()); break;
                        case 3: btn_MidLeft.setText(client.getSymbol()); break;
                        case 4: btn_Mid.setText(client.getSymbol()); break;
                        case 5: btn_MidRight.setText(client.getSymbol()); break;
                        case 6: btn_BotLeft.setText(client.getSymbol()); break;
                        case 7: btn_BotMid.setText(client.getSymbol()); break;
                        case 8: btn_BotRight.setText(client.getSymbol()); break;
                    }

                    if (checkForWinner(in, true)) {
                        client.print("You won!");
                        popUpWinner(true);
                        convertConcedeToReturn();
                        return;
                    }

                    if (checkForTie(in)) {
                        client.print("It's a tie!");
                        popUpTie();
                        convertConcedeToReturn();
                        return;
                    }

                    in = client.getInput().readLine();

                    if (in.contains("concedes")) {
                        client.print("Your opponent conceded!");
                        popUpConceded();
                        convertConcedeToReturn();
                    }

                    int opposingPlay = -1;
                    char opposingSymbol = client.getSymbol().equals("X") ? 'O' : 'X';
                    int i = -1;
                    for (char letter : in.toCharArray()) {
                        i++;
                        if (letter == opposingSymbol) break;
                    }
                    updateGUIwithOpposingPlay(i, String.valueOf(opposingSymbol));

                    if (checkForWinner(in, false)) {
                        client.print("Your opponent won!");
                        popUpWinner(false);
                        convertConcedeToReturn();
                    }

                    if (checkForTie(in)) {
                        client.print("It's a tie!");
                        popUpTie();
                        convertConcedeToReturn();
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void convertConcedeToReturn() {
        btn_Return.setText("Return to Title");
    }

    private void goIdle(boolean sendMessage) {
        try {
            if (sendMessage) client.sendMessage(client.getId() + " going idle");
            client.print("Going idle....");
            client.setOpposingId("");
            tab_Game.setDisable(true);
            tab_Connect.setDisable(false);
            tabPane.getSelectionModel().select(tab_Connect);
        } catch (IOException e) {
            client.print("Something went wrong when going idle.");
            e.printStackTrace();
        }
    }
}
