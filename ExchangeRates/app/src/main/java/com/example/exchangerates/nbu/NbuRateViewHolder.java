package com.example.exchangerates.nbu;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.exchangerates.R;
import com.example.exchangerates.orm.NbuRate;

public class NbuRateViewHolder extends RecyclerView.ViewHolder {


    private final TextView tvTxt;
    private TextView tvCc;
    private TextView tvRate;
    private  TextView tvreverse;

    private NbuRate nbuRate;

    private String compositeRate;

    public NbuRateViewHolder(@NonNull View itemView) {

        super(itemView);
        tvTxt=itemView.findViewById( R.id.nbu_rate_txt);
        tvCc=itemView.findViewById(R.id.nbu_rate_cc);
        tvRate=itemView.findViewById(R.id.nbu_rate_rate);
        tvreverse=itemView.findViewById(R.id.nbu_rate_reverse);
    }

    private void showData(){


        tvTxt.setText(nbuRate.getTxt());
        tvCc.setText(nbuRate.getCc());
        compositeRate=String.format("1 %s = %.5f HRN",nbuRate.getCc(),nbuRate.getRate());
        tvRate.setText(compositeRate);
        compositeRate=String.format("1 HRN = %.5f %s",1/nbuRate.getRate(),nbuRate.getCc());
        tvreverse.setText(compositeRate);

    }

    public void setNbuRate(NbuRate nbuRate) {
        this.nbuRate = nbuRate;
        showData();
    }
    public NbuRate getNbuRate() {
        return nbuRate;
    }
}
/*

 */