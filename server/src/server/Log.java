package server;


public class Log
{
    public static void logInfo(String text)
    {
        writeLog("INFO: " + text);
    }

    public static void logError(String text)
    {
        writeLog("ERROR: " + text);
    }

    public static void writeLog(String text)
    {
        System.out.println(text);
    }
}
