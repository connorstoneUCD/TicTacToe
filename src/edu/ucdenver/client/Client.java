package edu.ucdenver.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Arrays;

public class Client {
    private int serverPort;
    private String id, opposingId, serverIP;
    private boolean isConnected, turn;
    private String symbol;
    private char[] board = new char[9];
    private PrintWriter output;
    private BufferedReader input;
    private Socket server;

    public String getId() {
        return id;
    }

    public String getOpposingId() {
        return opposingId;
    }

    public boolean isConnected() {
        return isConnected;
    }

    public String getSymbol() {
        return symbol;
    }

    public BufferedReader getInput() {
        return input;
    }

    public char[] getBoard() {
        return board;
    }

    public boolean isTurn() {
        return turn;
    }

    public void setTurn(boolean turn) {
        this.turn = turn;
    }

    public void setOpposingId(String opposingId) {
        this.opposingId = opposingId;
    }

    public void setBoard(char[] board) {
        this.board = board;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public Client(String ip, int port) {
        this.serverIP = ip;
        this.serverPort = port;
        this.isConnected  = false;
        this.turn = false;
        Arrays.fill(this.board, '-');
    }

    public Client() {
        this("127.0.0.1", 10000);
    }

    public void print(String string) {
        System.out.println(string);
    }

    public String sendMessage(String message) throws IOException {
        print(String.format("SENT >> %s", message));
        this.output.println(message);
        this.output.flush();
        String in = this.input.readLine();
        print(String.format("RESPONSE >> %s", in));
        return in;
    }

    public boolean connect() {
        print(String.format("Connecting to %s....", this.serverIP));
        try {
            this.server = new Socket(this.serverIP, this.serverPort);
            this.isConnected = true;
            this.output = new PrintWriter(server.getOutputStream());
            this.input  = new BufferedReader(new InputStreamReader(server.getInputStream()));
            sendMessage("New client requesting ID");
            this.id = this.input.readLine();
            print(String.format("Success! Client ID: %s", this.id));
            return true;
        } catch (IOException e) {
            this.input = null;
            this.output = null;
            this.server = null;
            this.isConnected = false;
            print("Unsuccessful, try to connect again.");
            return false;
        }
    }

    public void disconnect() {
        print(String.format("Client %s terminating connection to server", this.id));
        try {
            sendMessage(String.format("%s disconnecting", this.id));
        } catch (IOException e) {
            print("Something went wrong...");
        }
    }
}
