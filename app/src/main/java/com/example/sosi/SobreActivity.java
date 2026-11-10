package com.example.sosi;


import android.os.Bundle;



public class SobreActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sobre);
        configurarTopo();
    }
}