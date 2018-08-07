package com.hide.project.hideproject;

import android.graphics.Color;
import android.graphics.PorterDuff;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;
import android.support.v7.widget.Toolbar;

public class HelpActivity extends AppCompatActivity {

    TextView info1;
    TextView info2;
    Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help);

        info1 = findViewById(R.id.first_info);
        info2 = findViewById(R.id.second_info);

        info1.setText("Icons made by https://www.flaticon.com/authors/kiranshastry from www.flaticon.com" + "\n" + " This application lets you secure your image and video files." + "\n" +  "Your pinCode is always hashed and save locally, it is never stored as a plain text");
        info2.setText("Usage:" + "\n" + "Enter pin" + "\n" + "Press menu button and choose upload files" + "\n" + "Select Items" + "\n" + "Press button on the bottom right two times" + "\n" + "You are Done");

    }
}
