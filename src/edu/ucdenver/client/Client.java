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
    private boolean isConnected;
    private char symbol;
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

    public char getSymbol() {
        return symbol;
    }

    public BufferedReader getInput() {
        return input;
    }

    public char[] getBoard() {
        return board;
    }

    public void setOpposingId(String opposingId) {
        this.opposingId = opposingId;
    }

    public void setBoard(char[] board) {
        this.board = board;
    }

    public void setSymbol(char symbol) {
        this.symbol = symbol;
    }

    public Client(String ip, int port) {
        this.serverIP = ip;
        this.serverPort = port;
        this.isConnected  = false;
        Arrays.fill(this.board, ' ');
    }

    public Client() {
        this("127.0.0.1", 10000);
    }

    public void print(String string) {
        System.out.println(string);
    }

    public void sendMessage(String message) throws IOException {
        this.output.println(message);
        this.output.flush();
        print(String.format("RESPONSE >> %s", this.input.readLine()));
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
