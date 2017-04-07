package client;



import javafx.application.Platform;

import java.io.IOException;
import java.io.PrintStream;
import java.net.Socket;
import java.util.Scanner;

public class Client
{
    private String ip;
    private int port;
    private Socket socket;
    private Scanner scannerInput;
    private PrintStream outputStream;
    private Controller fxmlController;

    public Client(String ip, String port, Controller controller)
    {
        this.ip = ip;
        this.port = Integer.parseInt(port);
        this.fxmlController = controller;
    }

    public boolean Connect() throws IOException
    {
        socket = new Socket(ip, port);
        if(socket.isConnected())
        {
            scannerInput = new Scanner(socket.getInputStream());
            outputStream = new PrintStream(socket.getOutputStream());
            startReceive();
        }
        else
        {
            return false;
        }
        return true;
    }

    private void startReceive()
    {
        Thread thread = new Thread(new Runnable()
        {
            public void run()
            {
                while (socket.isConnected())
                {
                    String received = scannerInput.nextLine();
                    handleReceivedData(received);
                }
                socketDisconnected();
            }
        });
        thread.start();
    }

    private void socketDisconnected()
    {
        scannerInput.close();
        outputStream.close();
        try
        {
            socket.close();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        Platform.runLater(new Runnable()
        {
            @Override
            public void run()
            {
                fxmlController.ServerDisconnected();
            }
        });
    }

    private void handleReceivedData(String data)
    {
        Platform.runLater(new Runnable()
        {
            @Override
            public void run()
            {
                fxmlController.ReceivedData(data);
            }
        });
    }

    public void Send(String text)
    {
        System.out.println("Send text: " + text);
        outputStream.println(text);
    }
}
