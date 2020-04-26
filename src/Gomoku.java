import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class Gomoku {
    private static final int size = 15;
    private Player[][] field = new Player[size][size];
    Player currentPlayer;
    private boolean connectionError = false;

    private boolean isWin() {
        //проверка по горизонтали
        for (int i = 0; i <= size - 5; i++) {
            for (int j = 0; j < size; j++) {
                if (field[i][j] != null &&
                        field[i+1][j] == field[i][j] &&
                        field[i+2][j] == field[i][j] &&
                        field[i+3][j] == field[i][j] &&
                        field[i+4][j] == field[i][j]) {
                    return true;
                }
            }
        }
        //проверка по вертикали
        for (int i = 0; i < size; i++) {
            for (int j = 0; j <= size - 5; j++) {
                if (field[i][j] != null &&
                        field[i][j+1] == field[i][j] &&
                        field[i][j+2] == field[i][j] &&
                        field[i][j+3] == field[i][j] &&
                        field[i][j+4] == field[i][j]) {
                    return true;
                }
            }
        }
        // проверка первой диагонали
        for (int i = 0; i <= size - 5; i++) {
            for (int j = 0; j <= size - 5; j++) {
                if (field[i][j] != null &&
                        field[i+1][j+1] == field[i][j] &&
                        field[i+2][j+2] == field[i][j] &&
                        field[i+3][j+3] == field[i][j] &&
                        field[i+4][j+4] == field[i][j]) {
                    return true;
                }
            }
        }
        // проверка второй диагонали
        for (int i = 0; i <= size - 5 ; i++) {  // Rows 0..10
            for (int j = 4; j < size; j++) {    // Cols 4..14
                if (field[i][j] != null &&
                        field[i+1][j-1] == field[i][j] &&
                        field[i+2][j-2] == field[i][j] &&
                        field[i+3][j-3] == field[i][j] &&
                        field[i+4][j-4] == field[i][j]) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean boardIsFull() {
        for (Player[] aBoard : field) {
            for(Player bBoard: aBoard)
                if (bBoard == null) {
                    return false;
                }
        }
        return true;
    }
    private synchronized boolean legalMove(int locationX, int locationY, Player player) {
        if (player == currentPlayer && field[locationX][locationY] == null) {
            field[locationX][locationY] = currentPlayer;
            currentPlayer = currentPlayer.opponent;
            currentPlayer.otherPlayerMoved(locationX, locationY);
            return true;
        }
        return false;
    }

    class Player extends Thread{
        char mark;
        Player opponent;
        Socket socket;
        BufferedReader input;
        PrintWriter output;
        int PORT;

        Player(Socket socket, char mark, int PORT) {
            this.socket = socket;
            this.mark = mark;
            this.PORT = PORT;

            try {
                input = new BufferedReader(
                        new InputStreamReader(socket.getInputStream()));
                output = new PrintWriter(socket.getOutputStream(), true);
                output.println("BEGIN " + mark);
                output.println("MESSAGE Waiting for opponent to connect");
            } catch (IOException e) {
                output.println("ERROR opponent disconnected!");
                e.printStackTrace();
            }
        }

        public void setOpponent(Player opponent) {
            this.opponent = opponent;
        }

        private void otherPlayerMoved(int locationX, int locationY) {
            output.println("OPPONENT_MOVED (" + locationX + "," + locationY + ")");
            //проверка на возможность проигрыша
            output.println(isWin() ? "DEFEAT"+mark : boardIsFull() ? "TIE" : "");

        }

        public void run() {
            try {
                // игра начинается только тогда, когда все подключаться
                //сообщаем о том, что все подключились
                output.println("MESSAGE All players are ready");

                // сообщаем первому игроку, что его ход
                if (mark == 'B') {
                    output.println("MESSAGE Your move");
                }

                while (true) {
                    String command = input.readLine();
                    if (command.startsWith("MOVE")) {

                        int bracketOpen = command.indexOf('(');
                        int bracketClose = command.indexOf(')');
                        int comma = command.indexOf(',');
                        int locationX = Integer.parseInt(command.substring(bracketOpen+1,comma));
                        int locationY = Integer.parseInt(command.substring(comma+1,bracketClose));
                        //проверяем на возможность выигрыша
                        if (legalMove(locationX, locationY, this)) {
                            output.println("VALID_MOVE");
                            output.println(isWin() ? "VICTORY"+mark : boardIsFull() ? "TIE"+mark : "");
                        } else {
                            if(!connectionError) {
                                output.println("MESSAGE ...");
                            }
                            else {
                                output.println("ERROR opponent has disconnected!");
                            }
                        }
                    } else if (command.startsWith("QUIT")) {
                        return;
                    }
                }
            } catch (IOException e) {
                connectionError = true;
                System.out.println("\nPlayer " + mark + " is disconnected");
            } finally {
                try {
                    socket.close();
                } catch (IOException e) {
                    System.out.println("Problem with closing the socket");
                }
            }
        }
    }
}
