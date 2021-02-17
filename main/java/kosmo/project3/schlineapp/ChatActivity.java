package kosmo.project3.schlineapp;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;

public class ChatActivity extends  Activity{

    private String html = "";
    private Handler mHandler;

    private Socket socket;

    private BufferedReader networkReader;
    private BufferedWriter networkWriter;

    private String ip = "192.168.25.47"; // IP
    private int port = 9999; // PORT번호

    @Override
    protected void onStop() {
        super.onStop();
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        mHandler = new Handler();

        try {
            setSocket(ip, port);
        } catch (IOException e1) {
            e1.printStackTrace();
        }

        checkUpdate.start();

        final EditText et = (EditText) findViewById(R.id.EditText01);
        Button btn = (Button) findViewById(R.id.Button01);
        final TextView tv = (TextView) findViewById(R.id.TextView01);

        btn.setOnClickListener(
                new View.OnClickListener() {

            public void onClick(View v) {
                if (et.getText().toString() != null || !et.getText().toString().equals("")) {
                    PrintWriter out = new PrintWriter(networkWriter, true);
                    String return_msg = et.getText().toString();
                    out.println(return_msg);
                }
            }
        });
    }

    private Thread checkUpdate = new Thread() {

        public void run() {
            try {
                String line;
                Log.w("ChattingStart", "Start Thread");
                while (true) {
                    Log.w("Chatting is running", "chatting is running");
                    line = networkReader.readLine();
                    html = line;
                    mHandler.post(showUpdate);
                }
            } catch (Exception e) {

            }
        }
    };

    private Runnable showUpdate = new Runnable() {

        public void run() {
            Toast.makeText(ChatActivity.this, "Coming word: " + html, Toast.LENGTH_SHORT).show();
        }

    };

    public void setSocket(String ip, int port) throws IOException {

        try {
            socket = new Socket(ip, port);
            networkWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            networkReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        } catch (IOException e) {
            System.out.println(e);
            e.printStackTrace();
        }

    }

}