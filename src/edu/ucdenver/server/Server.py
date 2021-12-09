from socket import socket, AF_INET, SOCK_STREAM
from uuid import uuid4
import json

from _thread import *
import threading

player_lock = threading.Lock()

idle_players = []
looking_for_game = []

upcoming_game = []
ready_to_start_count = 0

boardList = ["-"] * 8
currentTurn = 0
gameStatus = True


# thread function
def threaded(client_socket):
    global idle_players, looking_for_game, ready_to_start_count, upcoming_game, boardList, gameStatus

    client_socket.recv(1024)
    msg = "Connected to Tic-Tac-Toe Server\r\n"
    client_socket.send(msg.encode('ascii'))

    unique_id = str(uuid4())

    player_lock.acquire()
    idle_players.append(unique_id)
    player_lock.release()

    msg_to_send = unique_id + "\r\n"
    client_socket.send(msg_to_send.encode('ascii'))

    msg_from_client = " "

    while msg_from_client != "exit":
        player_id = process_client_request(client_socket)

        while True:

            if len(upcoming_game) == 2:

                if upcoming_game[0] == player_id or upcoming_game[1] == player_id:
                    ready_to_start_count = ready_to_start_count + 1
                    while True:

                        if ready_to_start_count == 2:
                            starting_message = "starting game between " + upcoming_game[0] + " and " + upcoming_game[1]
                            starting_message = starting_message + "\r\n"
                            client_socket.send(starting_message.encode('ascii'))

                            # This will
                            player_lock.acquire()
                            looking_for_game.remove(player_id)
                            ready_to_start_count = 0
                            player_lock.release()
                            gameStatus = True

                            # Read in that a client has entered the playing field / board
                            msg_from_client = client_socket.recv(1024).decode()
                            print('RECEIVED >> ' + msg_from_client)

                            # First time initializing the board and deciding who goes first
                            boardStr = ''.join(boardList)
                            board = boardStr + " " + upcoming_game[currentTurn] + " turn" + "\r\n"
                            client_socket.send(board.encode('ascii'))


                            # This will run until someone wins
                            while gameStatus == True:
                                client_move = client_socket.recv(1024).decode()
                                if client_move:
                                    print(client_move)

                                    playerSymbol = client_move.split(" ")[0]
                                    Index = client_move.split(" ")[1]
                                    player_id_return = client_move.split(" ")[2]
                                    player_id_return = player_id_return.replace("\r\n", "")
                                    boardList[int(Index)] = str(playerSymbol)

                                    continue_message = "Player " + player_id_return + " has made a move" + "\r\n"
                                    client_socket.send(continue_message.encode('ascii'))



    else:
        client_socket.close()


def process_client_request(client_socket):
    global idle_players, looking_for_game, upcoming_game
    msg_from_client = client_socket.recv(1024).decode()
    print('RECEIVED >> ' + msg_from_client)

    if "is looking for game" in msg_from_client:
        player_looking_for_game = msg_from_client.split()

        player_lock.acquire()
        idle_players.remove(player_looking_for_game[1])
        player_lock.release()

        player_lock.acquire()
        looking_for_game.append(player_looking_for_game[1])
        upcoming_game.append(player_looking_for_game[1])
        player_lock.release()

    return player_looking_for_game[1]


def server_init():
    serversocket = socket(AF_INET, SOCK_STREAM)          # create a socket object
    serversocket.bind( ('localhost', 10000) )             # bind to the port
    serversocket.listen(5)        # queue up to 5 requests
    while True:

        print("Waiting for clients...")
        clientsocket, addr = serversocket.accept()  # Wait for a client connection
        print("Got a connection from {}".format(str(addr)) + "\n")  # Print the client address
        start_new_thread(threaded, (clientsocket,))

    serversocket.close()


def main():
    server_init()


if __name__ == "__main__":
    main()