package com.example.chat.chat;


import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chat.orm.ChatMessage;

import com.example.chat.R;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.Locale;

public class ChatMessageViewHolder extends RecyclerView.ViewHolder {
    public final  static SimpleDateFormat momentFormat = new SimpleDateFormat("dd.MM HH:mm", Locale.ROOT);
    public final  static SimpleDateFormat format = new SimpleDateFormat("dd.MM.yy", Locale.ROOT);
    public final  static SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.ROOT);

    private ChatMessage chatMessage;
    private final TextView tvAuthor;
    private final TextView tvText;
    private final TextView tvMoment;

    public ChatMessageViewHolder(@NonNull View itemView) {
        super(itemView);
        tvAuthor=itemView.findViewById(R.id.chat_msg_author);
        tvText=itemView.findViewById(R.id.chat_msg_text);
        tvMoment=itemView.findViewById(R.id.chat_msg_moment);
    }

    public void setChatMessage(ChatMessage chatMessage) {
        this.chatMessage = chatMessage;
        tvAuthor.setText(this.chatMessage.getAuthor());
        tvText.setText(this.chatMessage.getText());
        momentPeriod();

    }

    private void momentPeriod(){

        Date date = new Date();
        LocalDate localDate = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        int year  = localDate.getYear();
        int month = localDate.getMonthValue();
        int day   = localDate.getDayOfMonth();

        LocalDate chatDate = this.chatMessage.getMoment().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

        int chatYear = chatDate.getYear();
        int chatMonth = chatDate.getMonthValue();
        int chatDay = chatDate.getDayOfMonth();

         if(format.format(date).equals(format.format(this.chatMessage.getMoment()))){

              tvMoment.setText(timeFormat.format(this.chatMessage.getMoment()));

         }else if(year==chatYear&&month==chatMonth&&day-chatDay==1){

             String dateYesterday=String.format("Yesterday %s",timeFormat.format(this.chatMessage.getMoment()));
             tvMoment.setText(dateYesterday);

         } else if (year==chatYear&&month==chatMonth&&day-chatDay>1&&day-chatDay<7) {

             int dayAgo=day-chatDay;
             String dateAgo=String.format("%d days ago",dayAgo);
             tvMoment.setText(dateAgo);

         } else{

             tvMoment.setText(momentFormat.format(this.chatMessage.getMoment()));
         }

    }


}
