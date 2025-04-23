package com.example.chat;

import android.Manifest;
import android.app.AlertDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
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

import java.io.FileInputStream;

import java.io.FileOutputStream;
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
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
    private static final String  chatUrl="https://chat.momentfor.fun/";
    private static final String authorFileName="author.name";
    private static final String chanelId="CHAT-CHANEL";
    private static final String appDatabase="chat_db";
    private ExecutorService pool;
    private  final List<ChatMessage> messages=new ArrayList<>();
    private EditText etAuthor;
    private EditText etMessage;
    private SwitchCompat scRemember;
    private final Handler hendler =new Handler();
    private RecyclerView rvContent;
    private boolean isFirstSend;
    private  int postGranted=-1;
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
        etAuthor.setText(loadAuthor());
        etMessage =  findViewById(R.id.chat_et_message);
        scRemember=findViewById(R.id.switch_remember);
        scRemember.setChecked(true);
        isFirstSend=true;

        rvContent=findViewById(R.id.chat_rv_content);
        chatMessageAdapter=new ChatMessageAdapter(messages);
        LinearLayoutManager layoutManager=new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);

        rvContent.setLayoutManager(layoutManager);
        rvContent.setAdapter(chatMessageAdapter);
        findViewById(R.id.chat_btn_send).setOnClickListener(this::onSendClick);
        CompletableFuture.runAsync(this::restoreMessage,pool);
        registerChannel();
        Intent intent=getIntent();
        if(intent!=null){

            String messageId=intent.getStringExtra("message_id");

            if(messageId!=null){
                Log.i("chat","Forwarded from notification " + messageId );
            }
        }

    }

    private void registerChannel(){
        NotificationChannel channel =new NotificationChannel(
                chanelId,
                "Chat notificatons",
                NotificationManager.IMPORTANCE_DEFAULT
        );

        channel.setDescription("Notifications about new incoming messages");
        getSystemService(NotificationManager.class).createNotificationChannel(channel);
    }
    private  void makeNotification(){

        if (ActivityCompat.checkSelfPermission(this,android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                postGranted=0;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS},234);
            }

            return;
        }

        postGranted=1;
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.putExtra("message_id","123");
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent,
                PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder buildeer=new NotificationCompat.Builder(this,chanelId)
                .setSmallIcon(android.R.drawable.btn_star_big_on)
                .setContentTitle("Chat")
                .setContentText("New incoming message")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                // Set the intent that fires when the user taps the notification.
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);
        NotificationManagerCompat.from(this).notify(123,buildeer.build());

    }

    public void onRequestPermissionsResult(int requestCode, String[] permissions,int[] grantResults){
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
           if(requestCode==234){

               if(grantResults[0]!= PackageManager.PERMISSION_GRANTED){

                   Toast.makeText(this,"No message",Toast.LENGTH_SHORT).show();
                   postGranted=0;

               }else {

                   postGranted=1;
               }

           }
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
        if(isFirstSend){

            isFirstSend=false;
            if(scRemember.isChecked()){

                saveAuthor(author);


            }

        }
        CompletableFuture.runAsync(
        ()->sendChatMessage(new ChatMessage(author,message)),
        pool);
        etAuthor.setEnabled(false);
        etMessage.setText("");
    }
    private void saveMessages(){
        try(SQLiteDatabase db=openOrCreateDatabase(appDatabase,Context.MODE_PRIVATE,null)){

            db.execSQL("CREATE TABLE IF NOT EXISTS chat_history(id ROWID," +
                    "author VARCHAR(128), " +
                    "text VARCHAR(512)," +
                    "moment DATETIME)");

            for (ChatMessage chatMessage:messages){
                db.execSQL("INSERT INTO chat_history VALUES(?,?,?,?)",
                        new Object[]{
                                Integer.parseInt(chatMessage.getId() ),
                                chatMessage.getAuthor(),
                                chatMessage.getText(),
                                chatMessage.getMoment()

                        });
            }

        }catch (Exception ex){

            Log.e("saveMessages",ex.getClass().getName()+" "+ex.getMessage());
        }

    }
    public synchronized void addCollect(ChatMessage message){
        messages.add(message);
    }
    private void restoreMessage(){

        try(SQLiteDatabase db=openOrCreateDatabase(appDatabase,Context.MODE_PRIVATE,null);
            Cursor cursor= db.rawQuery("SELECT * FROM chat_history",null))
        {
            if (cursor.moveToFirst()){

                do {
                    if( messages.stream().noneMatch(cm->cm.getId().equals(cursor.getString(0))) ){
                        addCollect( ChatMessage.fromCursor( cursor ));
                    }
                }while (cursor.moveToNext());

            }
        }catch (Exception ex){

            Log.e("saveMessages",ex.getClass().getName()+" "+ex.getMessage());
        }

    }
    private void processChatResponse(List<ChatMessage> parsedMessage){
        int oldsize=messages.size();
        for (ChatMessage m: parsedMessage){
            if( messages.stream().noneMatch(cm->cm.getId().equals(m.getId())) ){
                addCollect(m);
            }
        }
        int newSize=messages.size();
        if(newSize>oldsize){
            messages.sort(Comparator.comparing(ChatMessage::getMoment));
            runOnUiThread(()-> {
                chatMessageAdapter.notifyItemRangeChanged(oldsize, newSize);
                rvContent.scrollToPosition(newSize-1);
                if(oldsize!=0&&postGranted!=0){
                    makeNotification();
                }
            });

        }


    }
    private void saveAuthor(String name){

        try(FileOutputStream fos= openFileOutput(authorFileName, Context.MODE_PRIVATE)){
            fos.write(name.getBytes(StandardCharsets.UTF_8));
        }  catch (IOException e) {
            Log.e("saveAuthor","IOException "+e.getMessage());
        }


    }
    private String loadAuthor(){

        try(FileInputStream fis= openFileInput(authorFileName)){
           return Services.readAllText(fis);
        }  catch (IOException e) {
            Log.e("loadAuthor","IOException "+e.getMessage());
        }

        return "";
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
        saveMessages();
        super.onDestroy();
    }
}