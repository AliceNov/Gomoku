import java.io.IOException;
import java.net.ServerSocket;

public class Server {
    private static int PORT = 1234;
    private static ServerSocket listеner = null;

    private  static ServerSocket startServer(){
        try {
            listеner = new ServerSocket(PORT);
            System.out.println("Server is running");
        } catch (IOException e) {
            System.out.println("Some problem with setting PORT on server");
            e.printStackTrace();
        }
        return listеner;
    }
    public static void main(String[] args) throws Exception{
        Gomoku gameServer = new Gomoku();
        
        listеner = startServer();
        while (true){
            //playerB - игрок с черными фишками
            //playerW - игрок с белыми фишками 
            Gomoku.Player playerB = gameServer.new Player(listеner.accept(),'B', PORT);
            Gomoku.Player playerW = gameServer.new Player(listеner.accept(),'W', PORT);

            listеner.close();
            // устанвливаем каждому игроку соперника
            playerB.setOpponent(playerW);
            playerW.setOpponent(playerB);
            gameServer.currentPlayer = playerB;

            playerB.start();
            playerW.start();
        }


    }
}
