package server;


import java.io.IOException;

public class Main
{
    public static void main(String[] args)
    {
        Server server = null;
        try
        {
            server = new Server(8063);
            Log.logInfo("Server created");
            server.Start();
        }
        catch (IOException e)
        {
            Log.logError(e.getMessage());
        }
    }


}
