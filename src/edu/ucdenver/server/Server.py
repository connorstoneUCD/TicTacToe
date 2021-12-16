from socket import socket, AF_INET, SOCK_STREAM
from uuid import uuid4
import json

from _thread import *
import threading

lock = threading.Lock()

idle_players = []
looking_for_game = []


def soft_equals(v1, v2):
    return v1 == v2 and v1 != '-'


class Player:
    def __init__(self, sock):
        self._id = str(uuid4())
        self._sock = sock

    @property
    def id(self):
        return self._id

    @property
    def sock(self):
        return self._sock


class Game:
    def __init__(self):
        self._board = ['-' for i in range(9)]
        self._player1 = Player('')
        self._player2 = Player('')
        self._turn = 1

    def make_play(self, symbol, index):
        self._board[int(index)] = symbol
        return ''.join(self._board)

    def check_for_winner(self):
        if soft_equals(self._board[0], self._board[1]) and soft_equals(self._board[1], self._board[2]):
            return True
        if soft_equals(self._board[3], self._board[4]) and soft_equals(self._board[4], self._board[5]):
            return True
        if soft_equals(self._board[6], self._board[7]) and soft_equals(self._board[7], self._board[8]):
            return True
        if soft_equals(self._board[0], self._board[3]) and soft_equals(self._board[3], self._board[6]):
            return True
        if soft_equals(self._board[1], self._board[4]) and soft_equals(self._board[4], self._board[7]):
            return True
        if soft_equals(self._board[2], self._board[5]) and soft_equals(self._board[5], self._board[8]):
            return True
        if soft_equals(self._board[0], self._board[4]) and soft_equals(self._board[4], self._board[8]):
            return True
        if soft_equals(self._board[2], self._board[4]) and soft_equals(self._board[4], self._board[7]):
            return True
        return False

    def check_for_tie(self):
        return '-' not in self._board

    def send_to_both_players(self, message):
        self._player1.sock.send(message)
        self.player2.sock.send(message)

    def reset(self):
        self.__init__()

    @property
    def board(self):
        return self._board

    @property
    def player1(self):
        return self._player1

    @player1.setter
    def player1(self, value: Player):
        self._player1 = value

    @property
    def player2(self):
        return self._player2

    @player2.setter
    def player2(self, value: Player):
        self._player2 = value

    @property
    def turn(self):
        return self._turn

    def update_turn(self):
        self._turn = 1 if self._turn == 2 else 2


game = Game()


def get_client_msg(sock):
    msg = sock.recv(1024).decode()
    print(f'RECIEVED >>> {msg}')
    return msg


def threaded(sock: socket, player: Player):
    global idle_players, looking_for_game, game
    sock.recv(1024)
    sock.send('Connected to TicTacToe\n'.encode('ascii'))
    sock.send(f'{player.id}\n'.encode('ascii'))

    msg = get_client_msg(sock)
    while 'going idle' not in msg and msg != '':
        if 'looking for game' in msg:
            idle_players.remove(player)
            looking_for_game.append(player)
            while len(looking_for_game) < 2:
                continue

        if game.player1.id in msg:
            move = msg.split()
            if 'concedes' in msg:
                game.player2.sock.send(f'{game.player1.id} concedes\n'.encode('ascii'))
                msg = get_client_msg(sock)
                continue

            print(f'{game.make_play(move[0], move[1])} {game.player2.id} turn')
            if game.turn == 1:
                if game.check_for_winner():
                    game.send_to_both_players(f'{game.make_play(move[0], move[1])} {game.player2.id} won\n'.encode('ascii'))
                    msg = get_client_msg(sock)
                    continue
                if game.check_for_tie():
                    game.send_to_both_players(f'{game.make_play(move[0], move[1])} {game.player2.id} nobody\n'.encode('ascii'))
                    msg = get_client_msg(sock)
                    continue
                game.send_to_both_players(f'{game.make_play(move[0], move[1])} {game.player2.id} turn\n'.encode('ascii'))
                game.update_turn()
            else:
                sock.send('Not your turn\n'.encode('ascii'))

        if game.player2.id in msg:
            move = msg.split()
            if 'concedes' in msg:
                game.player1.sock.send(f'{game.player1.id} concedes\n'.encode('ascii'))
                msg = get_client_msg(sock)
                continue

            print(f'{game.make_play(move[0], move[1])} {game.player1.id} turn')
            if game.turn == 2:
                if game.check_for_winner():
                    game.send_to_both_players(f'{game.make_play(move[0], move[1])} {game.player1.id} won\n'.encode('ascii'))
                    msg = get_client_msg(sock)
                    continue
                if game.check_for_tie():
                    game.send_to_both_players(f'{game.make_play(move[0], move[1])} {game.player1.id} nobody\n'.encode('ascii'))
                    msg = get_client_msg(sock)
                    continue
                game.send_to_both_players(f'{game.make_play(move[0], move[1])} {game.player1.id} turn\n'.encode('ascii'))
                game.update_turn()
            else:
                sock.send('Not your turn\n'.encode('ascii'))

        if len(looking_for_game) >= 2:
            print(f'starting game between {looking_for_game[0].id} and {looking_for_game[1].id}')
            sock.send(f'starting game between {looking_for_game[0].id} and {looking_for_game[1].id}\n'.encode('ascii'))
            if 'entered the board' in get_client_msg(sock):
                if player == looking_for_game[0]:
                    lock.acquire()
                    game.player1 = player
                    lock.release()
                else:
                    lock.acquire()
                    game.player2 = player
                    lock.release()
                print(f'--------- {game.player1.id} turn')
                sock.send(f'--------- {game.player1.id} turn\n'.encode('ascii'))
            looking_for_game.remove(player)

        msg = get_client_msg(sock)
        print(f'RECEIVED >>> {msg}')
    game.send_to_both_players('going idle\n'.encode('ascii'))
    print('ending game...')
    exit_thread()


def server_init():
    global idle_players
    threads = 0
    server_socket = socket(AF_INET, SOCK_STREAM)          # create a socket object
    server_socket.bind( ('localhost', 10000) )             # bind to the port
    server_socket.listen(5)        # queue up to 5 requests
    while True:

        print("Waiting for clients...")
        client_socket, addr = server_socket.accept()  # Wait for a client connection
        print("Got a connection from {}".format(str(addr)) + "\n")  # Print the client address
        new_player = Player(client_socket)
        idle_players.append(new_player)
        start_new_thread(threaded, (client_socket, new_player))

    server_socket.close()


def main():
    server_init()


if __name__ == "__main__":
    main()
