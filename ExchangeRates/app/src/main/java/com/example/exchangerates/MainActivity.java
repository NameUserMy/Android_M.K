package com.example.exchangerates;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.DatePicker;
import android.widget.LinearLayout;
import android.widget.SearchView;
import android.widget.TextView;


import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.exchangerates.nbu.NbuRateAdapter;
import com.example.exchangerates.orm.NbuRate;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

public class MainActivity extends AppCompatActivity implements DatePickerDialog.OnDateSetListener {

    private String nbuRatesUrl="https://bank.gov.ua/NBUStatService/v1/statdirectory/exchange?json";

    private ExecutorService pool;
    private final List<NbuRate> nbuRates=new ArrayList<>();
    private NbuRateAdapter nbuRateAdapter;
    private RecyclerView rvContainer;
   private DatePickerDialog pickerDialog;

    private TextView date;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            Insets imeBars = insets.getInsets(WindowInsetsCompat.Type.ime());


            v.setPadding(systemBars.left, systemBars.top, systemBars.right,
                    Math.max(systemBars.bottom,imeBars.bottom));

            return insets;

        });

        pool= Executors.newFixedThreadPool(3);


        CompletableFuture.
                supplyAsync(this::loadRates,pool).
                thenAccept(this::parseNbuResponse).
                thenRun(this::showNbuRates);
        rvContainer=findViewById(R.id.rates_rv_container);
        rvContainer.post(()->{
            RecyclerView.LayoutManager layoutManager=new GridLayoutManager(this, 2);
            rvContainer.setLayoutManager(layoutManager);
            nbuRateAdapter=new NbuRateAdapter(nbuRates);
            rvContainer.setAdapter(nbuRateAdapter);
            date.setText(NbuRate.dateFormat.format(nbuRates.get(0).getExchangeDate()));
        });

        date =findViewById(R.id.date_rate);
        date.setOnClickListener(this::picDate);


        SearchView svFilter=findViewById(R.id.rates_sv_filter);
        svFilter.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                return onFilterChange(s);
            }

            @Override
            public boolean onQueryTextChange(String s) {
                return onFilterChange(s);
            }
        });




//        Button animButton=new Button(this);
//        animButton.setText(R.string.main_button_anim);
//        animButton.setBackgroundColor(getColor(R.color.rate_layout_fg));
//        animButton.setOnClickListener(this::onAnimButtonClick);
//        LinearLayout.LayoutParams layoutParams=new LinearLayout.LayoutParams(
//                ViewGroup.LayoutParams.MATCH_PARENT,
//                ViewGroup.LayoutParams.WRAP_CONTENT
//        );
//        layoutParams.bottomMargin=25;
//
//        animButton.setLayoutParams(layoutParams);
//        LinearLayout contanier=findViewById(R.id.main);
//        contanier.addView(animButton);

    }


    private boolean onFilterChange(String s){

        Log.d("onFilterChange",s);

        nbuRateAdapter.setNbuRates( nbuRates.stream()
                .filter(r->r.getCc().toUpperCase().contains(s.toUpperCase())
                 || r.getTxt().toLowerCase().contains(s.toLowerCase()))
                .collect(Collectors.toList())

        );
        return true;

    }

    private void onAnimButtonClick(View view){
        startActivity(new Intent(this,AnimActivity.class));

    }

    private void showNbuRates(){

        runOnUiThread(()->{

            nbuRateAdapter.notifyItemChanged(0,nbuRates.size());
        });

    }
    private void parseNbuResponse(String body){

        try {

            JSONArray arr=new JSONArray(body);
            int len=arr.length();

            for (int i=0; i<len;i++){

                nbuRates.add(NbuRate.froJsonObject(arr.getJSONObject(i)));

            }


        } catch (JSONException ex) {

           Log.d("parseNbuResponse","JsonExeption "+ex.getMessage());

        }

    }
    private String loadRates(){

        try {

            URL url=new URL(nbuRatesUrl);
            InputStream urlStream= url.openStream(); //GET-request

            ByteArrayOutputStream byteBuilder=new ByteArrayOutputStream();
            byte[] buffer=new byte[8192];
            int len;

            while ((len=urlStream.read(buffer))>0){

                byteBuilder.write(buffer,0,len);
            }
            String charsetName=StandardCharsets.UTF_8.name();

            String data=byteBuilder.toString(charsetName);
            urlStream.close();
            return data;

        }catch (MalformedURLException ex){

            Log.d("loadRates","MalformedURLException"+ex.getMessage());

        }catch (IOException ex){

            Log.d("loadRates","IOExeption"+ex.getMessage());
        }
        return  null;
        /*
            android.os.NetworkOnMainThreadException only new thread;
            java.lang.SecurityException: Permission denied (missing INTERNET permission?) need access on www
            setting on manifests file, <uses-permission android:name="android.permission.INTERNET"/>

            android.view.ViewRootImpl$CalledFromWrongThreadException: Only the original thread that created a view hierarchy can touch its views.
        */

    }

    @Override
    protected void onDestroy() {
        pool.shutdown();
        super.onDestroy();
    }
    private void picDate(View view){
        pickerDialog=new DatePickerDialog(
                this,
                this,
                Calendar.getInstance().get(Calendar.YEAR),
                Calendar.getInstance().get(Calendar.MONTH),
                Calendar.getInstance().get(Calendar.DAY_OF_MONTH)
        );
        pickerDialog.show();

    }
    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
        nbuRatesUrl=String.format("https://bank.gov.ua/NBUStatService/v1/statdirectory/exchange?json&date=%d0%d0%d",year,month+1,dayOfMonth);
        String dateIn=String.format("0%d.0%d.%d",dayOfMonth,month+1,year);
        Log.d("Date",nbuRatesUrl);
        CompletableFuture.
                supplyAsync(this::loadRates,pool).
                thenAccept(this::parseNbuResponse).
                thenRun(this::showNbuRates);
        date.setText(dateIn);
        nbuRateAdapter.clearNbuRates();

    }
}

//