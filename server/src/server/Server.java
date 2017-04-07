package server;


import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;

public class Server
{
    private ServerSocket serverSocket;
    private int port;
    private Scanner scannerInput;

    public Server(int port) throws IOException
    {
        this.port = port;
        serverSocket = new ServerSocket(port);

    }

    public void Start()
    {
        Log.logInfo("Server started");
        while(true)
        {
            try
            {
                Socket socket = null;
                while(socket == null)
                {
                    socket = serverSocket.accept();
                }
                Log.logInfo("Client connected. " + socket.getInetAddress().toString());
                Client client = new Client(socket);
                client.StartReceive();
            }
            catch (IOException e)
            {
                Log.logError(e.getMessage());
            }
        }
    }
}
