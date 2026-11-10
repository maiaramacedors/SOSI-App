package com.example.sosi;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;



public class ContatoActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contato);
        configurarTopo();
    }
}