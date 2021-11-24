package edu.ucdenver.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class Client {
    private int serverPort;
    private String id, serverIP;
    private boolean isConnected;
    private char symbol;
    private PrintWriter output;
    private BufferedReader input;
    private Socket server;

    public String getId() {
        return id;
    }

    public boolean isConnected() {
        return isConnected;
    }

    public char getSymbol() {
        return symbol;
    }

    public void setSymbol(char symbol) {
        this.symbol = symbol;
    }

    public Client(String ip, int port) {
        this.serverIP = ip;
        this.serverPort = port;
        this.isConnected  = false;
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

    public void connect() {
        print(String.format("Connecting to %s....", this.serverIP));
        try {
            this.server = new Socket(this.serverIP, this.serverPort);
            this.isConnected = true;
            this.output = new PrintWriter(server.getOutputStream());
            this.input  = new BufferedReader(new InputStreamReader(server.getInputStream()));
            this.id = this.input.readLine();
            print(String.format("Success! Client ID: %s", this.id));
        } catch (IOException e) {
            this.input = null;
            this.output = null;
            this.server = null;
            this.isConnected = false;
            print("Unsuccessful, try to connect again.");
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
