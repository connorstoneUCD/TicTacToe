# TicTacToe
## Applications
>### TicTacToe
> >- Client Application  
> >- JavaFX app
> >- Java 8 SDK
> >- Developed and tested using IntelliJ IDEA major release 2020
> 
> ### server.py
> >- Server Application
> >- Python module
> >- Python Interpreter 3.8
> >- Developed and tested using PyCharm
> 
## Operation
>- server.py should be run first to start the server and listen for client connections.
> - Two client applications should then be started.
> >- Client server communication will run on loopback through 'localhost'.
> - Click "Look for Game" on each client to match the clients in a game of TicTacToe.
> - Player 1 will see the game board in their UI indicating it is their turn.
> - Moves are made by clicking on a square in the UI game board.
> - Once player 1 selects a position on the TicTacToe board, player 2 sees player 1's move on their board and can select their move.  
> - Players continue taking turns selecting positions on the game board.
> - Either player my end the game by clicking "Concede".
> > - If a player concedes, once they have clicked "Return to Title", both players are returned to the title screen.  
> - Play continues until a player wins, or the game is a tie.
> - When the game is over, a pop-up window displays the outcome (won by a player, tie, concession)  
> - The pop-up window is dismissed by pressing "OK".  
> - Both players now have to option to click "Return to Title".  
> - At this point the game is complete.  
> - Close the client windows (or terminate their applications).  
> - Terminate the server.  
> > - Note: due to an unresolved issue, the server must be terminated before restarting the clients or the game will not proceed properly.  
> > - Terminating the server before both clients have closed will result in a slightly delayed release of the server listening socket.