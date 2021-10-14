package solana.server.alarm;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Properties;
import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import org.json.JSONException;
import org.json.JSONObject;

public class SolanaServerAlarm {

    private static String MAILSERVER = "127.0.0.1";
    private static String EMAIL = "";
    private static String SERVERURL = "";
    private static String LL = "";
    private static String DS = "";

    private static String charset = "UTF-8";
    private static HttpURLConnection con;
    private static URL urlObj;
    private static JSONObject jObj = null;
    private static StringBuilder result;

    /*
    A SIMPLE APP THAT WILL BE RUN BY CRONTAB ON A FEW MINUTE INTERVAL THAT WILL
    SEND AN EMAIL TO YOU IF YOUR SERVER IS NOT IN GOOD ORDER
    AS SEEN BY THE API CALL TO CHECK THE HEALTH OF YOUR SERVER    
     */
    public static void main(String[] args) {

        if (args.length != 2) {
            System.out.println("****************************************");
            System.out.println("ERROR - YOU MUST SPECIFY BOTH THE URL INCLUDING THE RPC PORT TO USE TO CHECK THE SERVER HEALTH");
            System.out.println("USE THE NORMAL URI FOR EXAMPLE:");
            System.out.println("http://127.0.0.1:8899");
            System.out.println("****************************************");
            System.out.println("THE SECOND COMMAND LINE PARAMETER MUST BE THE EMAIL ADDRESS TO SEND THE ERROR REPORT TO");
            System.out.println("****************************************");
            System.out.println("USE A LINE IN THE CRONTAB TO EXECUTE THIS PROGRAM EVERY FEW MINUTES SO THAT YOU WILL GET AN EMAIL AS SOON AS A PROBLEM IS DETECTED");
            System.out.println("****************************************");
            System.out.println("HERE IS EXAMPLE LINE TO ADD AT BOTTOM OF /etc/crontab TO CHECK THE SERVER EVERY 10 MINUTES");
            System.out.println("1,11,21,31,41,51 * * * * root /opt/chksvr/chksvr.sh > /dev/null");
            System.out.println("****************************************");
            System.out.println("THE /opt/chksvr/chksvr.sh FILE MUST HAVE THE JAVA JAR FILE AND LIBS DIRECTORY ALL IN THIS SAME LOCATION AND MUST HAVE THE TWO LINES BELOW: ");
            System.out.println("#!/bin/sh");
            System.out.println("java -jar /opt/chksvr/solana-server-alarm.jar http://127.0.0.1:8899 serveradmin@ibm.com");
            System.out.println("****************************************");
            System.exit(1);
        }

        SERVERURL = args[0];
        EMAIL = args[1];

        System.getProperties();
        //////////////////////////////////////////////////////////////////////////////////////////
        DS = System.getProperty("file.separator");
        LL = System.getProperty("line.separator");

        String json = "{\"jsonrpc\":\"2.0\",\"id\":1, \"method\":\"getHealth\"}";

        JSONObject jo = makeHttpRequest(SERVERURL, json);

        if (jo.has("result")) {

            String ok = jo.getString("result");

            if (ok.trim().toLowerCase().equals("ok")) {
                                
                System.out.println("SERVER IS AOK SO NO ALARM EMAIL WAS SENT");
                
            } else {

                String payload = "YOUR SERVER IS REPORTING A NON-HEALTHY STATUS"
                        + "\r\n\r\n"
                        + "THE EXACT MESSAGE THAT THE SERVER REPORTED:"
                        + "\r\n\r\n";

                JSONObject jodata = null;
                String data = "";

                if (jo.has("error")) {

                    JSONObject joerror = jo.getJSONObject("error");

                    if (joerror.has("data")) {

                        jodata = joerror.getJSONObject("data");

                        if (jodata.has("numSlotsBehind")) {

                            data = "NUMBER OF SLOTS BEHIND: " + jodata.getInt("numSlotsBehind");
                        }
                    }

                    if (joerror.has("message")) {

                        payload += joerror.getString("message");

                    }

                    if (data.trim().length() > 0) {

                        payload += "\r\n\r\n" + data;
                    }
                }

                // SEND EMAIL NOW AS NO OK WAS FOUND 
                email(EMAIL, "SOLANA SERVER HEALTH ERROR REPORT", payload, false);
            }
        }
    }

    private static JSONObject makeHttpRequest(String url, String paramsJSON) {

        StringBuilder response = null;

        try {
            //Change the URL with any other publicly accessible POST resource, which accepts JSON request body
            URL urlobject = new URL(url);

            HttpURLConnection con = (HttpURLConnection) urlobject.openConnection();
            con.setRequestMethod("POST");
            con.setRequestProperty("Content-Type", "application/json");
            con.setRequestProperty("Accept", "application/json");
            con.setDoOutput(true);

            //JSON String need to be constructed for the specific resource. 
            try (OutputStream os = con.getOutputStream()) {
                byte[] input = paramsJSON.getBytes(StandardCharsets.UTF_8);
                os.write(input);
            }

            int code = con.getResponseCode();

            // build response string
            try (BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream(), "utf-8"))) {
                response = new StringBuilder();
                String responseLine = null;
                while ((responseLine = br.readLine()) != null) {
                    response.append(responseLine.trim());
                }
            }
        } catch (Exception e) {
        }

        // try parse the string to a JSON object
        try {
            jObj = new JSONObject(response.toString());
        } catch (JSONException e) {
            System.out.println("JSON Parser Error parsing data " + e.toString());
        }

        return jObj;
    }

    public static void email(String email, String sub, String msg, boolean html) {
        /**
         * *******************************************
         */
        Properties props = new Properties();
        props.put("mail.smtp.host", MAILSERVER);
        Session s = Session.getDefaultInstance(props);
        MimeMessage message = new MimeMessage(s);
        try {
            message.setSentDate(new Date());
            message.setFrom(new InternetAddress(EMAIL));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(email, false));
            message.setSubject(sub);
            message.setText(msg);
            if (html) {
                message.setHeader("Content-Type", "text/html; charset=UTF-8");
            } else {
                message.setHeader("Content-Type", "text/plain; charset=UTF-8");
            }
            Transport.send(message);
        } catch (Exception e) {
            email(EMAIL, "SOLANA SERVER ALARM ERROR", "ERROR:" + "\r\n" + e.getMessage(), false);
            System.out.println("ERROR SENDING EMAIL");
        }
    }

}

