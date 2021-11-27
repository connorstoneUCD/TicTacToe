from socket import socket, AF_INET, SOCK_STREAM


def server_init():
    serversocket = socket(AF_INET, SOCK_STREAM)          # create a socket object
    serversocket.bind( ('localhost', 10000) )             # bind to the port
    serversocket.listen(5)                               # queue up to 5 requests
    while True:
        print("Waiting for clients...")
        clientsocket, addr = serversocket.accept()       # Wait for a client connection
        print("Got a connection from {}".format(str(addr)))  # Print the client addess

        print(clientsocket.recv(1024))
        msg = "Connected to Tic-Tac-Toe Server\r\n"
        clientsocket.send(msg.encode('ascii'))

        msg = "2\r\n"
        clientsocket.send(msg.encode('ascii'))

        if msg == "exit":
            clientsocket.close()

    serversocket.close()


def main():
    server_init()


if __name__ == "__main__":
    main()