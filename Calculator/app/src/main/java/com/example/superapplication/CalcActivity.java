package com.example.superapplication;

import android.os.Bundle;
import android.util.Log;
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
    private boolean isSecond;
    private boolean isEqual;

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
        tvExpression = findViewById(R.id.calc_tv_expression);
        tvResult = findViewById(R.id.calc_tv_result);
        zero = getString(R.string.calc_btn_0);

        Button btnC = findViewById(R.id.calc_btn_c);
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
        findViewById(R.id.calc_btn_dot).setOnClickListener(this::onDigitClick);


        findViewById(R.id.calc_btn_add).setOnClickListener(this::onArithmeticsClick);
        findViewById(R.id.calc_btn_mul).setOnClickListener(this::onArithmeticsClick);
        findViewById(R.id.calc_btn_div).setOnClickListener(this::onArithmeticsClick);
        findViewById(R.id.calc_btn_sub).setOnClickListener(this::onArithmeticsClick);
        findViewById(R.id.calc_btn_eq).setOnClickListener(this::onArithmeticsClick);
        findViewById(R.id.calc_btn_backspace).setOnClickListener(this::onBackspaceClick);

        onClearClick(btnC);
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putCharSequence("result", tvResult.getText());
        outState.putCharSequence("expretions", tvExpression.getText());
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        tvResult.setText(savedInstanceState.getCharSequence("result"));
        tvExpression.setText(savedInstanceState.getCharSequence("expretions"));

    }

    private void onArithmeticsClick(View view) {
        String operation = view.getTag().toString();

        if (tvExpression.getText().toString().isBlank()) {
            tvExpression.setText(tvResult.getText().toString() + " " + operation);

        } else {

            arithmeticsOperation(operation);

        }
    }
    private void onDigitClick(View view) {


        if (isEqual) {

            tvExpression.setText("");
            isEqual = false;
            tvResult.setText("");

        }

        String result = tvResult.getText().toString();

        if (!tvExpression.getText().toString().isBlank()) {


            if (!this.isSecond) {
                result = "";
                isSecond = true;
            }
            result += ((Button) view).getText();
            tvResult.setText(result);

        }else{

            if (!((Button) view).getText().toString().equals(".")&&result.equals(zero)) {

                result = "";
            }
            result += ((Button) view).getText();
            tvResult.setText(result);

        }

    }

    private void onClearClick(View view) {


        tvExpression.setText("");
        tvResult.setText(zero);
        this.isSecond = false;

    }
    private void onBackspaceClick(View view){

      String backspaceText= tvResult.getText().toString();

      tvResult.setText(backspaceText.substring(0,backspaceText.length()-1));


    }
    private void arithmeticsOperation(String operation) {

        String[] expression = tvExpression.getText().toString().split(" ");
        String result = String.valueOf(result(expression, tvResult.getText().toString()));
        if (operation.equals("=")) {

            tvExpression.setText(tvExpression.getText().toString() + " " + tvResult.getText().toString() + "" + operation);
            tvResult.setText(result);
            isEqual = true;


        } else {
            tvExpression.setText(result + " " + operation);
            tvResult.setText(result);

        }
        isSecond = false;

    }

    private String result(String[] parseExpretion, String second) {

        double firstOperant = Double.parseDouble(parseExpretion[0]);
        double secondOperant = Double.parseDouble(second);

        String value = "";
        switch (parseExpretion[1]) {

            case "+":
                value = String.valueOf(firstOperant + secondOperant);
                break;
            case "-":
                value = String.valueOf(firstOperant - secondOperant);
                break;
            case "x":
                value = String.valueOf(firstOperant * secondOperant);
                break;
            case "/":
                value = String.valueOf(firstOperant / secondOperant);
                break;
        }

        String[] isDouble = value.split("\\.");

        if (Double.parseDouble(isDouble[1]) == 0) {

            return isDouble[0];
        } else {
            if(value.length()>12){

                return   value.substring(0,11);
            }
            return value;
        }

    }
}