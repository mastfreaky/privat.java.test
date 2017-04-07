package client;

import javafx.fxml.FXML;

import javafx.event.ActionEvent;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.control.TextArea;
import javafx.scene.layout.Pane;
import org.json.simple.JSONObject;

import java.io.IOException;

public class Controller
{
    @FXML private TextField textfieldIp;
    @FXML private TextField textfieldPort;
    @FXML private TextField textfieldAccountInfo;
    @FXML private TextField textfieldDepositorInfo;
    @FXML private TextField textfieldBank;
    @FXML private TextField textFieldDeleteAccountId;
    @FXML private TextField textfiledAddBank;
    @FXML private TextField textfiledAddCountry;
    @FXML private TextField textfiledAddDepositor;
    @FXML private TextField textfiledAddAccountId;
    @FXML private TextField textfiledAddProfitability;
    @FXML private TextField textfiledAddTimeConstaints;
    @FXML private TextField textfiledAddAmount;
    @FXML private TextArea textareaLog;
    @FXML private Pane paneConnection;
    @FXML private Pane paneContent;
    @FXML private ComboBox comboboxTypes;
    @FXML private ComboBox comboboxAddTypes;

    private Client client = null;

    @FXML public void initialize()
    {
        comboboxTypes.getItems().addAll(
                "PosteRestante",
                "Urgent",
                "Calculated",
                "Comulative",
                "Saving",
                "Metal"
        );
        comboboxAddTypes.getItems().addAll(
                "PosteRestante",
                "Urgent",
                "Calculated",
                "Comulative",
                "Saving",
                "Metal"
        );
        comboboxTypes.getSelectionModel().selectFirst();
        comboboxAddTypes.getSelectionModel().selectFirst();
    }

    @FXML protected void buttonListClicked(ActionEvent event)
    {
        if(client != null)
        {
            JSONObject json = new JSONObject();
            json.put("command", "list");
            client.Send(json.toJSONString());
        }
    }

    @FXML protected void buttonSumClicked(ActionEvent event)
    {
        if(client != null)
        {
            JSONObject json = new JSONObject();
            json.put("command", "sum");
            client.Send(json.toJSONString());
        }
    }

    @FXML protected void buttonCountClicked(ActionEvent event)
    {
        if(client != null)
        {
            JSONObject json = new JSONObject();
            json.put("command", "count");
            client.Send(json.toJSONString());
        }
    }

    @FXML protected void buttonAccountInfoClicked(ActionEvent event)
    {
        if(client != null)
        {
            JSONObject json = new JSONObject();
            json.put("command", "accountInfo");
            json.put("accountId", textfieldAccountInfo.getText());
            client.Send(json.toJSONString());
        }
    }

    @FXML protected void buttonDepositorInfoClicked(ActionEvent event)
    {
        if(client != null)
        {
            JSONObject json = new JSONObject();
            json.put("command", "depositorInfo");
            json.put("depositor", textfieldDepositorInfo.getText());
            client.Send(json.toJSONString());
        }
    }

    @FXML protected void buttonShowTypeClicked(ActionEvent event)
    {
        if(client != null)
        {
            JSONObject json = new JSONObject();
            json.put("command", "showType");
            json.put("type", comboboxTypes.getSelectionModel().getSelectedItem().toString());
            client.Send(json.toJSONString());
        }
    }

    @FXML protected void buttonShowBankClicked(ActionEvent event)
    {
        if(client != null)
        {
            JSONObject json = new JSONObject();
            json.put("command", "bank");
            json.put("bank", textfieldBank.getText());
            client.Send(json.toJSONString());
        }
    }

    @FXML protected void buttonDeleteAccountClicked(ActionEvent event)
    {
        if(client != null)
        {
            JSONObject json = new JSONObject();
            json.put("command", "delete");
            json.put("accountId", textFieldDeleteAccountId.getText());
            client.Send(json.toJSONString());
        }
    }

    @FXML protected void buttonAddClicked(ActionEvent event)
    {
        if(client != null)
        {
            JSONObject json = new JSONObject();
            json.put("command", "add");
            json.put("bank", textfiledAddBank.getText());
            json.put("country", textfiledAddCountry.getText());
            json.put("type", comboboxAddTypes.getSelectionModel().getSelectedItem().toString());
            json.put("depositor", textfiledAddDepositor.getText());
            json.put("accountId", textfiledAddAccountId.getText());
            json.put("profitability", textfiledAddProfitability.getText());
            json.put("timeConstants", textfiledAddTimeConstaints.getText());
            json.put("amount", textfiledAddAmount.getText());
            client.Send(json.toJSONString());
        }
    }

    @FXML protected void buttonConnectClicked(ActionEvent event)
    {
        if(client == null)
        {
            if(textfieldIp.getText().equals("") || textfieldPort.getText().equals(""))
            {
                logError("Ip and Port is required");
                return;
            }
            try
            {
                client = new Client(textfieldIp.getText(), textfieldPort.getText(), this);
            }
            catch(RuntimeException e)
            {
                logError(e.getMessage());
                return;
            }
        }
        boolean connected = false;
        try
        {
            connected = client.Connect();
        }
        catch (IOException e)
        {
            logError(e.getMessage());
        }
        if(connected)
        {
            paneConnection.setDisable(true);
            paneContent.setDisable(false);
            logInfo("Connection successfully");
        }
        else
        {
            logError("Connection fail");
        }
    }

    public void ReceivedData(String data)
    {
        logServer(data);
    }

    public void ServerDisconnected()
    {
        if(client != null)
        {
            client = null;
        }
        paneConnection.setDisable(false);
        paneContent.setDisable(true);
        logError("Server disconnected");
    }

    private void logServer(String text)
    {
        writeLog("SERVER: " + text);
    }

    private void logInfo(String text)
    {
        writeLog("INFO: " + text);
    }

    private void logError(String text)
    {
        writeLog("ERROR: " + text);
    }

    private void writeLog(String text)
    {
        text = textareaLog.getText() + "\n\n" + text;
        textareaLog.setText(text);
        textareaLog.appendText("");
    }
}
