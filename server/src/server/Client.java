package server;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.*;
import java.net.Socket;
import java.util.Iterator;
import java.util.Scanner;

public class Client
{
    private Socket socket;
    private Scanner inputScanner;
    private PrintStream outputStream;
    private final String fileName = "database.json";
    private File databaseFile;

    public Client(Socket s) throws IOException
    {
        socket = s;
        inputScanner = new Scanner(socket.getInputStream());
        outputStream = new PrintStream(socket.getOutputStream());
        checkFile();
    }

    private void checkFile() throws IOException
    {
        databaseFile = new File(fileName);
        databaseFile.createNewFile();
        JSONParser parser = new JSONParser();
        FileReader readFile = null;
        try
        {
            readFile = new FileReader(databaseFile);
            JSONObject object = (JSONObject) parser.parse(readFile);
        }
        catch (FileNotFoundException e)
        {
            Log.logError(e.getMessage());
        }
        catch (ParseException e)
        {
            FileWriter writer = new FileWriter(databaseFile);
            writer.write("{}");
            writer.flush();
            writer.close();
            Log.logInfo("Database file was overwritten");
        }
        if(readFile != null)
        {
            readFile.close();
        }
    }

    public void StartReceive()
    {
        Thread thread = new Thread(new Runnable()
        {
            public void run()
            {
                Log.logInfo("Receive started");
                while(socket.isConnected())
                {
                    String received = inputScanner.nextLine();
                    handleReceivedData(received);
                }
            }
        });
        thread.start();
    }

    public void Send(String text)
    {
        outputStream.println(text);
    }

    private void handleReceivedData(String data)
    {
        Log.logInfo("Client " + socket.getInetAddress().toString() + ", received data: " + data);
        JSONObject json = parseData(data);
        if(json != null && json.containsKey("command"))
        {
            String command = json.get("command").toString();
            switch (command)
            {
                case "list":
                {
                    list();
                    break;
                }
                case "sum":
                {
                    sum();
                    break;
                }
                case "count":
                {
                    count();
                    break;
                }
                case "accountInfo":
                {
                    if(json.containsKey("accountId") && !json.get("accountId").toString().equals(""))
                    {
                        accountInfo(json.get("accountId").toString());
                    }
                    else
                    {
                        Send("ERROR! Account id is required");
                    }
                    break;
                }
                case "depositorInfo":
                {
                    if(json.containsKey("depositor") && !json.get("depositor").toString().equals(""))
                    {
                        depositorInfo(json.get("depositor").toString());
                    }
                    else
                    {
                        Send("ERROR! Depositor is required");
                    }
                    break;
                }
                case "showType":
                {
                    if(json.containsKey("type") && !json.get("type").toString().equals(""))
                    {
                        showType(json.get("type").toString());
                    }
                    else
                    {
                        Send("ERROR! Type is required");
                    }
                    break;
                }
                case "bank":
                {
                    if(json.containsKey("bank") && !json.get("bank").toString().equals(""))
                    {
                        bank(json.get("bank").toString());
                    }
                    else
                    {
                        Send("ERROR! Bank is required");
                    }
                    break;
                }
                case "delete":
                {
                    if(json.containsKey("accountId") && !json.get("accountId").toString().equals(""))
                    {
                        delete(json.get("accountId").toString());
                    }
                    else
                    {
                        Send("ERROR! Account id is required");
                    }
                    break;
                }
                case "add":
                {
                    if(json.containsKey("bank")
                            && !json.get("bank").toString().equals("")
                            && json.containsKey("country")
                            && !json.get("country").toString().equals("")
                            && json.containsKey("type")
                            && !json.get("type").toString().equals("")
                            && json.containsKey("depositor")
                            && !json.get("depositor").toString().equals("")
                            && json.containsKey("accountId")
                            && !json.get("accountId").toString().equals("")
                            && json.containsKey("profitability")
                            && !json.get("profitability").toString().equals("")
                            && json.containsKey("timeConstants")
                            && !json.get("timeConstants").toString().equals("")
                            && json.containsKey("amount")
                            && !json.get("amount").toString().equals("")
                            )
                    {
                        add(json.get("bank").toString(),
                                json.get("country").toString(),
                                json.get("type").toString(),
                                json.get("depositor").toString(),
                                json.get("accountId").toString(),
                                json.get("profitability").toString(),
                                json.get("timeConstants").toString(),
                                json.get("amount").toString());
                    }
                    else
                    {
                        Send("ERROR! All fields is required");
                    }
                    break;
                }
                default: break;
            }
        }
    }

    private JSONObject parseData(String data)
    {
        JSONParser parser = new JSONParser();
        JSONObject json = null;
        try
        {
            json = (JSONObject) parser.parse(data);
            Log.logInfo(json.toJSONString());
        }
        catch (ParseException e)
        {
            Log.logError(e.getMessage());
        }
        return json;
    }

    private JSONObject getList()
    {
        FileReader readFile = null;
        try
        {
            JSONParser parser = new JSONParser();
            readFile = new FileReader(databaseFile);
            JSONObject object = (JSONObject) parser.parse(readFile);
            return object;
        }
        catch (FileNotFoundException e)
        {
            Log.logError(e.getMessage());
        }
        catch (ParseException e)
        {
           //  Log.logError(e.getMessage()); // Why NULL?
        }
        catch (IOException e)
        {
            Log.logError(e.getMessage());
        }
        if(readFile != null)
        {
            try
            {
                readFile.close();
            }
            catch (IOException e)
            {
                Log.logError(e.getMessage());
            }
        }
        return null;
    }

    private void list()
    {
        JSONObject json = getList();
        if(json != null)
        {
            Log.logInfo("Send list");
            Send(getList().toJSONString());
        }
        else
        {
            Log.logError("Getlist returned NULL");
            Send("Fail");
        }
    }

    private void sum()
    {
        JSONObject json = getList();
        if(json != null)
        {
            JSONArray deposits;
            if (json.containsKey("deposits"))
            {
                deposits = (JSONArray) json.get("deposits");
            } else
                {
                deposits = new JSONArray();
                json.put("deposits", deposits);
            }

            Iterator iterator = deposits.iterator();
            double sum = 0;
            while (iterator.hasNext())
            {
                JSONObject tmpJson = (JSONObject) iterator.next();
                if (tmpJson.containsKey("amount"))
                {
                    sum += Double.parseDouble(tmpJson.get("amount").toString());
                }
            }
            Send("Sum: " + sum);
        }
        else
        {
            Log.logError("sum(): getList returned NULL");
            Send("ERROR");
        }
    }

    private void count()
    {
        JSONObject json = getList();
        if(json != null)
        {
            JSONArray deposits;
            if (json.containsKey("deposits"))
            {
                deposits = (JSONArray) json.get("deposits");
                Send("Sum: " + deposits.size());
            }
            else
            {
                Send("Sum: " + 0);
            }
        }
        else
        {
            Log.logError("count(): getList returned NULL");
            Send("ERROR");
        }
    }

    private void accountInfo(String accountId)
    {
        JSONObject json = getList();
        if(json != null)
        {
            JSONArray deposits;
            if (json.containsKey("deposits"))
            {
                deposits = (JSONArray) json.get("deposits");
            } else
            {
                deposits = new JSONArray();
                json.put("deposits", deposits);
            }

            Iterator iterator = deposits.iterator();
            JSONObject accountInfoJson = null;
            while (iterator.hasNext())
            {
                JSONObject tmpJson = (JSONObject) iterator.next();
                if (tmpJson.containsKey("accountId") && tmpJson.get("accountId").equals(accountId))
                {
                    accountInfoJson = tmpJson;
                    break;
                }
            }
            if(accountInfoJson != null)
            {
                Send("Account info: " + accountInfoJson.toJSONString());
            }
            else
            {
                Send("ERROR: Account does not exists");
            }
        }
        else
        {
            Log.logError("accountInfo(): getList returned NULL");
            Send("ERROR");
        }
    }

    private void depositorInfo(String depositor)
    {
        JSONObject json = getList();
        if(json != null)
        {
            JSONArray deposits;
            if (json.containsKey("deposits"))
            {
                deposits = (JSONArray) json.get("deposits");
            } else
            {
                deposits = new JSONArray();
                json.put("deposits", deposits);
            }

            Iterator iterator = deposits.iterator();
            JSONArray depositorInfoJsonArray = new JSONArray();
            while (iterator.hasNext())
            {
                JSONObject tmpJson = (JSONObject) iterator.next();
                if (tmpJson.containsKey("depositor") && tmpJson.get("depositor").equals(depositor))
                {
                    depositorInfoJsonArray.add(tmpJson);
                }
            }
            if(depositorInfoJsonArray.size() > 0)
            {
                Send("Depositor info: " + depositorInfoJsonArray.toJSONString());
            }
            else
            {
                Send("ERROR: No info by Depositor");
            }
        }
        else
        {
            Log.logError("depositorInfo(): getList returned NULL");
            Send("ERROR");
        }
    }

    private void showType(String type)
    {
        JSONObject json = getList();
        if(json != null)
        {
            JSONArray deposits;
            if (json.containsKey("deposits"))
            {
                deposits = (JSONArray) json.get("deposits");
            } else
            {
                deposits = new JSONArray();
                json.put("deposits", deposits);
            }

            Iterator iterator = deposits.iterator();
            JSONArray typeInfoJsonArray = new JSONArray();
            while (iterator.hasNext())
            {
                JSONObject tmpJson = (JSONObject) iterator.next();
                if (tmpJson.containsKey("type") && tmpJson.get("type").equals(type))
                {
                    typeInfoJsonArray.add(tmpJson);
                }
            }
            if(typeInfoJsonArray.size() > 0)
            {
                Send("Type info: " + typeInfoJsonArray.toJSONString());
            }
            else
            {
                Send("ERROR: No info by Type");
            }
        }
        else
        {
            Log.logError("showType(): getList returned NULL");
            Send("ERROR");
        }
    }

    private void bank(String bank)
    {
        JSONObject json = getList();
        if(json != null)
        {
            JSONArray deposits;
            if (json.containsKey("deposits"))
            {
                deposits = (JSONArray) json.get("deposits");
            } else
            {
                deposits = new JSONArray();
                json.put("deposits", deposits);
            }

            Iterator iterator = deposits.iterator();
            JSONArray bankInfoJsonArray = new JSONArray();
            while (iterator.hasNext())
            {
                JSONObject tmpJson = (JSONObject) iterator.next();
                if (tmpJson.containsKey("bank") && tmpJson.get("bank").equals(bank))
                {
                    bankInfoJsonArray.add(tmpJson);
                }
            }
            if(bankInfoJsonArray.size() > 0)
            {
                Send("Bank info: " + bankInfoJsonArray.toJSONString());
            }
            else
            {
                Send("ERROR: No info by Bank");
            }
        }
        else
        {
            Log.logError("bank(): getList returned NULL");
            Send("ERROR");
        }
    }

    private void delete(String accountId)
    {
        JSONObject json = getList();
        if(json != null)
        {
            JSONArray deposits;
            if(json.containsKey("deposits"))
            {
                deposits = (JSONArray)json.get("deposits");
            }
            else
            {
                deposits = new JSONArray();
                json.put("deposits", deposits);
            }

            Iterator iterator = deposits.iterator();
            boolean accountExist = false;
            while(iterator.hasNext())
            {
                JSONObject tmpJson = (JSONObject)iterator.next();
                if(tmpJson.containsKey("accountId") && tmpJson.get("accountId").toString().equals(accountId))
                {
                    accountExist = true;
                    deposits.remove(tmpJson);
                    return;
                }
            }

            if(accountExist)
            {
                try (FileWriter writer = new FileWriter(fileName)){
                    writer.write(json.toJSONString());
                    writer.flush();
                    writer.close();
                    Send("OK");
                }
                catch (IOException e)
                {
                    Log.logError(e.getMessage());
                    Send("ERROR! " + e.getMessage());
                }
            }
            else
            {
                Send("ERROR! accountId does not exists");
            }
        }
        else
        {
            Log.logError("delete(): getList returned NULL");
            Send("Fail");
        }
    }

    private void add(String bank,
                     String country,
                     String type,
                     String depositor,
                     String accountId,
                     String profitability,
                     String timeConstants,
                     String amount)
    {
        try
        {
            double profitabilityInt = Double.parseDouble(profitability);
            double timeConstantsInt = Double.parseDouble(timeConstants);
            double amountInt = Double.parseDouble(amount);
            if(profitabilityInt <= 0 || timeConstantsInt <= 0 || amountInt <= 0)
            {
                Send("ERROR! profitability, timeConstants, amount must be more then 0");
                return;
            }
        }
        catch(NumberFormatException e)
        {
            Send("ERROR! profitability, timeConstants, amount must be numeric");
            return;
        }


        JSONObject json = getList();
        if(json != null)
        {
            JSONArray deposits;
            if(json.containsKey("deposits"))
            {
                deposits = (JSONArray)json.get("deposits");
            }
            else
            {
                deposits = new JSONArray();
                json.put("deposits", deposits);
            }

            Iterator iterator = deposits.iterator();
            while(iterator.hasNext())
            {
                JSONObject tmpJson = (JSONObject)iterator.next();
                if(tmpJson.containsKey("accountId") && tmpJson.get("accountId").toString().equals(accountId))
                {
                    Send("ERROR! accountId already exists");
                    return;
                }
            }


            json.remove("deposits");
            JSONObject newDeposit = new JSONObject();
            newDeposit.put("bank", bank);
            newDeposit.put("country", country);
            newDeposit.put("type", type);
            newDeposit.put("depositor", depositor);
            newDeposit.put("accountId", accountId);
            newDeposit.put("profitability", profitability);
            newDeposit.put("timeConstants", timeConstants);
            newDeposit.put("amount", amount);
            deposits.add(newDeposit);
            json.put("deposits", deposits);
            try (FileWriter writer = new FileWriter(fileName)){
                writer.write(json.toJSONString());
                writer.flush();
                writer.close();
                Send("OK");
            }
            catch (IOException e)
            {
                Log.logError(e.getMessage());
                Send("ERROR! " + e.getMessage());
            }
        }
        else
        {
            Log.logError("add(): getList returned NULL");
            Send("Fail");
        }

    }
}
