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
                        client.setSymbol('X');
                        client.setOpposingId(in.split(" ")[5]);
                    }
                    else {
                        client.setSymbol('O');
                        client.setOpposingId(in.split(" ")[3]);
                    }

                    tab_Game.setDisable(false);
                    tab_Connect.setDisable(true);
                    tabPane.getSelectionModel().select(tab_Game);
                    in = client.sendMessage("client " + client.getId() + " has entered the board");
                    client.setTurn(in.split(" ")[1].equals(client.getId())); // get the initial board from the server and set if it is this client's turn
                    if (!client.isTurn()) { // if it is not our turn, we wait until it is and then allow for pressing buttons
                        // in = client.getInput().readLine();
                        client.setTurn(in.split(" ")[1].equals(client.getId()));
                        client.setBoard(in.split(" ")[0].replaceAll("([\\[,\\]])", "").toCharArray());
                        int opposingPlay = 0;
                        char opposingSymbol = client.getSymbol() == 'X' ? 'O' : 'X';
                        for (int i = 0; i < client.getBoard().length; i++) {
                            if (client.getBoard()[i] != ' ') {
                                opposingPlay = i;
                                break;
                            }
                        }
                        updateGUIwithOpposingPlay(opposingPlay, opposingSymbol);
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

    private void updateGUIwithOpposingPlay(int opposingPlay, char opposingSymbol) {
        switch (opposingPlay) {
            case 0: btn_TopLeft.setText(String.valueOf(opposingSymbol)); break;
            case 1: btn_TopMid.setText(String.valueOf(opposingSymbol)); break;
            case 2: btn_TopRight.setText(String.valueOf(opposingSymbol)); break;
            case 3: btn_MidLeft.setText(String.valueOf(opposingSymbol)); break;
            case 4: btn_Mid.setText(String.valueOf(opposingSymbol)); break;
            case 5: btn_MidRight.setText(String.valueOf(opposingSymbol)); break;
            case 6: btn_BotLeft.setText(String.valueOf(opposingSymbol)); break;
            case 7: btn_BotMid.setText(String.valueOf(opposingSymbol)); break;
            case 8: btn_BotRight.setText(String.valueOf(opposingSymbol)); break;
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
            e.printStackTrace();
        }
    }

    private void pressButton(int index) {
        if (!client.isTurn()) {
            client.print("Not your turn");
            return;
        }

        char[] newBoard = client.getBoard();
        client.print("SENT REQUEST");

        if (newBoard[index] == '-') {
            client.print(String.valueOf(newBoard[index]));
            try {

                newBoard[index] = client.getSymbol();
                client.print(String.valueOf(newBoard[index]));
                client.setBoard(newBoard);
                String serverMsg = client.sendMessage(client.getSymbol() + " " + index + " " + client.getOpposingId());
                client.setTurn(false);

                // if the message we get back from the server says we won, do the process for going idle
                if (serverMsg.contains(client.getId() + " won") || serverMsg.contains("Nobody won")) {
                    String alertMsg = serverMsg.contains("Nobody won") ? "Nobody won" : "You won!";
                    Alert alert = new Alert(Alert.AlertType.INFORMATION, alertMsg);
                    alert.show();
                    goIdle();
                } else { // otherwise, we know it is a board state so update our board
                    switch (index) {
                        case 0: btn_TopLeft.setText(String.valueOf(client.getSymbol())); break;
                        case 1: btn_TopMid.setText(String.valueOf(client.getSymbol())); break;
                        case 2: btn_TopRight.setText(String.valueOf(client.getSymbol())); break;
                        case 3: btn_MidLeft.setText(String.valueOf(client.getSymbol())); break;
                        case 4: btn_Mid.setText(String.valueOf(client.getSymbol())); break;
                        case 5: btn_MidRight.setText(String.valueOf(client.getSymbol())); break;
                        case 6: btn_BotLeft.setText(String.valueOf(client.getSymbol())); break;
                        case 7: btn_BotMid.setText(String.valueOf(client.getSymbol())); break;
                        case 8: btn_BotRight.setText(String.valueOf(client.getSymbol())); break;
                    }
                }

                // then, we wait for the message coming from the other client and do the same as above but check if they won or conceded
                serverMsg = client.getInput().readLine();
                if (serverMsg.contains("Nobody won") || serverMsg.contains(client.getOpposingId() + " won") || serverMsg.contains(client.getOpposingId() + " concedes")) {
                    String alertMsg = serverMsg.contains("Nobody won") ? "Nobody won" : "Your opponent " + serverMsg.split(" ")[2];
                    Alert alert = new Alert(Alert.AlertType.INFORMATION, alertMsg);
                    alert.show();
                    goIdle();
                } else { // if they didn't win or concede, we need to update our board again with their move
                    int opposingPlay = -1;
                    char opposingSymbol = client.getSymbol() == 'X' ? 'O' : 'X';
                    char[] inBoard = serverMsg.split(" ")[0].replaceAll("([\\[,\\]])", "").toCharArray();
                    for (int i = 0; i < client.getBoard().length; i++) {
                        if (client.getBoard()[i] != inBoard[i]) {
                            opposingPlay = i; // get the move they made
                            break;
                        }
                    }
                    updateGUIwithOpposingPlay(opposingPlay, opposingSymbol);
                    client.setTurn(true); // say it's our turn
                }
            } catch (IOException e) {
                client.print("Something went wrong when sending a button command.");
                e.printStackTrace();
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
            e.printStackTrace();
        }
    }
}
