package com.example.superapplication;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.annotation.NonNull;

public class CalcActivity extends AppCompatActivity {


    private TextView tvExpression;
    private TextView tvResult;
    private String zero;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_calc);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        tvExpression=findViewById(R.id.calc_tv_expression);
        tvResult=findViewById(R.id.calc_tv_result);
        zero= getString(R.string.calc_btn_0);
        Button btnC=findViewById(R.id.calc_btn_c);
        btnC.setOnClickListener(this::onClearClick);
        findViewById(R.id.calc_btn_0).setOnClickListener(this::onDigitClick);
        findViewById(R.id.calc_btn_1).setOnClickListener(this::onDigitClick);
        findViewById(R.id.calc_btn_2).setOnClickListener(this::onDigitClick);
        findViewById(R.id.calc_btn_3).setOnClickListener(this::onDigitClick);
        findViewById(R.id.calc_btn_4).setOnClickListener(this::onDigitClick);
        findViewById(R.id.calc_btn_5).setOnClickListener(this::onDigitClick);
        findViewById(R.id.calc_btn_6).setOnClickListener(this::onDigitClick);
        findViewById(R.id.calc_btn_7).setOnClickListener(this::onDigitClick);
        findViewById(R.id.calc_btn_8).setOnClickListener(this::onDigitClick);
        findViewById(R.id.calc_btn_9).setOnClickListener(this::onDigitClick);

        onClearClick(btnC);
    }
    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {

        super.onSaveInstanceState(outState);
        outState.putCharSequence("result",tvResult.getText());


    }
    @Override
    protected
    void onRestoreInstanceState(@NonNull Bundle savedInstanceState)
    {
        super.onRestoreInstanceState(savedInstanceState);
        tvResult.setText(savedInstanceState.getCharSequence("result"));

    }

    private void onDigitClick(View view){

        String result=tvResult.getText().toString();
        if(result.equals(zero)){

            result="";
        }
        result+=((Button)view).getText();
        tvResult.setText(result);


    }
    private void onClearClick(View view){


        tvExpression.setText("");
        tvResult.setText(zero);

    }
}