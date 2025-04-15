package com.example.chat;

import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chat.chat.ChatMessageAdapter;
import com.example.chat.orm.ChatMessage;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import  android.os.Handler;

public class MainActivity extends AppCompatActivity {
    private static final String  chatUrl="https://chat.momentfor.fun/";
    private ExecutorService pool;
    private final List<ChatMessage> messages=new ArrayList<>();
    private EditText etAuthor;
    private EditText etMessage;

    private final Handler hendler =new Handler();

    private RecyclerView rvContent;
    private ChatMessageAdapter chatMessageAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());

            Insets imeBars = insets.getInsets(WindowInsetsCompat.Type.ime());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right,Math.max(systemBars.bottom,imeBars.bottom));

            return insets;
        });

        pool= Executors.newFixedThreadPool(3);
        updateChate();


        etAuthor = findViewById(R.id.chat_et_author);
        etMessage =  findViewById(R.id.chat_et_message);

        rvContent=findViewById(R.id.chat_rv_content);
        chatMessageAdapter=new ChatMessageAdapter(messages);
        LinearLayoutManager layoutManager=new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);

        rvContent.setLayoutManager(layoutManager);
        rvContent.setAdapter(chatMessageAdapter);
        findViewById(R.id.chat_btn_send).setOnClickListener(this::onSendClick);

    }
    private void updateChate(){

        CompletableFuture
                .supplyAsync(()->Services.fetchUrl(chatUrl),pool)
                .thenApply(this::parseChatResponse)
                .thenAccept(this::processChatResponse);

        hendler.postDelayed(this::updateChate,2000);
    }
    private void onSendClick(View view){

        String alertMessage=null;
        String author =etAuthor.getText().toString();
        String message=etMessage.getText().toString();

        if(author.isBlank()){

            alertMessage=getString(R.string.chat_msg_no_author);

        }else if(message.isBlank()){

            alertMessage=getString(R.string.chat_msg_no_text);

        }
        if(alertMessage!=null){

        new AlertDialog.Builder(this, android.R.style.Theme_Material_Dialog_Alert)
                .setTitle(R.string.chat_msg_no_send)
                .setMessage(alertMessage)
                .setIcon(android.R.drawable.ic_delete)
                .setPositiveButton(R.string.chat_msg_no_send_btn,(dlg,btn)->{})
                .setCancelable(false)
                .show();
          return;
        }
        CompletableFuture.runAsync(
        ()->sendChatMessage(new ChatMessage(author,message)),
        pool);
        etAuthor.setEnabled(false);
        etMessage.setText("");
    }
    private void sendChatMessage(ChatMessage chatMessage){

        String charset= StandardCharsets.UTF_8.name();

        try {
            String body=String.format(Locale.ROOT,"author=%s&msg=%s",
                    URLEncoder.encode(chatMessage.getAuthor(), charset),
                    URLEncoder.encode(chatMessage.getText(), charset)

            );

            URL url=new URL(chatUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true); //wait response
            connection.setDoOutput(true); //send data body
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type","application/x-www-form-urlencoded");
            connection.setRequestProperty("Accept","application/json");
            connection.setRequestProperty("Connection","close");
            connection.setRequestProperty("X-Powered-By","The-super-puper-chat");
            connection.setChunkedStreamingMode(0);

            OutputStream bodyStream=connection.getOutputStream();
            bodyStream.write(body.getBytes(charset) );
            bodyStream.flush();
            bodyStream.close();


            int statusCode=connection.getResponseCode();

            if(statusCode==201){
               // if need body connection.getInputStream();
                updateChate();

            }else{

                //response in error send
              InputStream errorStream= connection.getErrorStream();
              Log.e("sendChatMessage",Services.readAllText(errorStream) );
              errorStream.close();

            }

            connection.disconnect();

        } catch (UnsupportedEncodingException e) {
            Log.e("sendChatMessage","UnsuportedEncodingExeption"+ e.getMessage());
        } catch (MalformedURLException e) {
            Log.e("sendChatMessage","MalformedURLException"+ e.getMessage());
        } catch (IOException e) {

            Log.e("sendChatMessage","IOException"+ e.getMessage());
        }


    }
    private void processChatResponse(List<ChatMessage> parsedMessage){
        int oldsize=messages.size();
        for (ChatMessage m: parsedMessage){
            if( messages.stream().noneMatch(cm->cm.getId().equals(m.getId())) ){
                messages.add(m);
            }
        }
        int newSize=messages.size();
        if(newSize>oldsize){
            messages.sort(Comparator.comparing(ChatMessage::getMoment));
            runOnUiThread(()-> {
                chatMessageAdapter.notifyItemRangeChanged(oldsize, newSize);
                rvContent.scrollToPosition(newSize-1);
            });
        }


    }
    private List<ChatMessage> parseChatResponse(String body){
        List<ChatMessage> res = new ArrayList<>();
        try {

            JSONObject root=new JSONObject(body);

            if(root.getInt("status")==1){
                JSONArray arr=root.getJSONArray("data");

                int len=arr.length();

                for (int i=0; i<len;i++){

                    res.add(ChatMessage.froJsonObject( arr.getJSONObject(i) ));

                }

            }else{
                Log.i("Status 204","No Content");
            }


        } catch (JSONException ex) {

            Log.d("parseChatResponse","JsonExeption "+ex.getMessage());

        }

        return res;

    }

    protected void onDestroy() {
        hendler.removeMessages(0);
        pool.shutdown();
        super.onDestroy();
    }
}