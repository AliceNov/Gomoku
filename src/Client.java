import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class Client {
    //В - черный
    //W - белый
    private JFrame frame = new JFrame("Gomoky");
    private JLabel messageLabel = new JLabel("");

    private Color myColor;
    private Color opponentColor;

    private Field[][] board = new Field[15][15];
    private Field currentSquare;

    private static int PORT = 1234;
    private static  String HOST = "localhost";
    private Socket clientSocket;
    private BufferedReader in;
    private PrintWriter out;

    static class Field extends JPanel {
        JLabel label = new JLabel((Icon)null);

        Field() {
            setBackground(Color.orange);
            add(label);
        }
    }

    private Client() throws Exception {
           try {
               clientSocket = new Socket(HOST, PORT);
               System.out.println("Connected to the server!");
           } catch (Exception e) {
               e.printStackTrace();
           }

        in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        out = new PrintWriter(clientSocket.getOutputStream(), true);

        messageLabel.setBackground(Color.white);
        frame.getContentPane().add(messageLabel, "North");
        JPanel boardPanel = new JPanel();
        boardPanel.setBackground(Color.black);
        boardPanel.setLayout(new GridLayout(15, 15, 1, 1));
        for (int i = 0; i < board.length; i++) {
            for(int j = 0; j < board[0].length; j++) {
                final int x = i;
                final int y = j;
                board[i][j] = new Field();
                board[i][j].addMouseListener(new MouseAdapter() {
                    public void mousePressed(MouseEvent e) {
                        currentSquare = board[x][y];
                        out.println("MOVE (" + x + ',' + y + ")");}});
                boardPanel.add(board[i][j]);
            }
        }
        frame.getContentPane().add(boardPanel, "Center");
    }

    private void play() throws Exception {
        String response;
        try {
            response = in.readLine();
            if (response.startsWith("WELCOME")) {
                char mark = response.charAt(8);
                if(mark=='W') {
                    myColor = Color.white;
                    opponentColor = Color.black;
                }else{
                    myColor = Color.black;
                    opponentColor = Color.white;
                }
                frame.setTitle("Gomoky");
            }
            while (true) {
                response = in.readLine();
                if (response.startsWith("VALID_MOVE")) {
                    messageLabel.setText("The opponent makes a move, please wait");
                    currentSquare.setBackground(myColor);
                    currentSquare.repaint();
                } else if (response.startsWith("OPPONENT_MOVED")) {

                    int bracketOpen = response.indexOf('(');
                    int bracketClose = response.indexOf(')');
                    int comma = response.indexOf(',');
                    int locationX = Integer.parseInt(response.substring(bracketOpen+1,comma));
                    int locationY = Integer.parseInt(response.substring(comma+1,bracketClose));

                    board[locationX][locationY].setBackground(opponentColor);
                    board[locationX][locationY].repaint();
                    messageLabel.setText("The opponent has made a move, your turn");
                } else if (response.startsWith("VICTORY")) {
                    JOptionPane.showMessageDialog(null, "Player "+response.substring(7)+" won");
                    System.exit(0);
                    break;
                } else if (response.startsWith("DEFEAT")) {
                    JOptionPane.showMessageDialog(null, "Player "+response.substring(6)+" lose");
                    System.exit(0);
                    break;
                } else if (response.startsWith("TIE")) {
                    JOptionPane.showMessageDialog(null, "It`s a tie");
                    System.exit(0);
                    break;
                } else if (response.startsWith("MESSAGE")) {
                    messageLabel.setText(response.substring(8));
                }
                else if (response.startsWith("ERROR")) {
                    JOptionPane.showMessageDialog(null, response.substring(6));
                    break;
                }
            }
            out.println("QUIT");
        }
        finally {
            clientSocket.close();
        }
    }

    public static void  main(String[] args){
        try {
            while (true) {
                try {
                    Client client = new Client();
                    client.frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
                    client.frame.setSize(500, 500);
                    client.frame.setVisible(true);
                    client.frame.setResizable(true);
                    client.play();

                   break;

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            System.out.println("The connection is lost");
            System.exit(1);
        }
    }


}
