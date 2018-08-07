package com.hide.project.hideproject;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import es.dmoral.toasty.Toasty;

import static com.hide.project.hideproject.Hashing.getSha1;
import static com.hide.project.hideproject.LoginActivity.PASSWORD_PREF;

public class SettingsActivity extends AppCompatActivity {

    private static final String TAG = "Settings";
    public static final String SETTINGS_PREF = "SettingsPref";
    public static final String SHUFFLE_STORE = "Shuffle";

    final Context mCont =  this;

    Button help;
    Button feedback;
    Button pincode;
    Switch shuffle;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        Toolbar toolbar =  findViewById(R.id.toolbar);
        toolbar.getNavigationIcon().setColorFilter(Color.rgb(255,255,255), PorterDuff.Mode.SRC_IN); // White arrow
        toolbar.setTitle("Settings");

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();  // back to main activity
            }
        });

         help = findViewById(R.id.help);
         feedback = findViewById(R.id.feedback);
         pincode = findViewById(R.id.pincode);
         shuffle = findViewById(R.id.on_off_switch);

         /*
         // at the start set slider on/off by looking at shared prefs
          */
        SharedPreferences settings = getSharedPreferences(SETTINGS_PREF, 0);
        boolean slider = settings.getBoolean(SHUFFLE_STORE, true);
        shuffle.setChecked(slider);

         help.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View v) {
                 Intent intent = new Intent(getBaseContext(), HelpActivity.class);
                 startActivity(intent);
             }
         });

         feedback.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View v) {
                 Intent intent = new Intent (Intent.ACTION_VIEW , Uri.parse("mailto:" + "com.project.hide.hideproject@protonmail.com"));
                 intent.putExtra(Intent.EXTRA_SUBJECT, "Subject");
                 intent.putExtra(Intent.EXTRA_TEXT, "Message");
                 startActivity(intent);
             }
         });

         /*
         Changing pincode functionality
          */

         pincode.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View v) {

                 LayoutInflater layoutInflaterAndroid = LayoutInflater.from(mCont);
                 View dial = layoutInflaterAndroid.inflate(R.layout.input_dialog, null);

                 AlertDialog.Builder userInput = new AlertDialog.Builder(mCont);
                 userInput.setView(dial);
                 Log.d(TAG, "onClick: INPUT DIALOG INFLATE");


                  final EditText dialogEdit = (EditText) dial.findViewById(R.id.userInputDialog);
                  final TextView title = dial.findViewById(R.id.dialogTitle);


                    // Making input error prone, user can't use more or less than 5 numbers
                 userInput.setCancelable(false).setPositiveButton("Submit", new DialogInterface.OnClickListener() {
                             public void onClick(DialogInterface box, int id) {

                                 String newPin = dialogEdit.getText().toString();
                                 int good = newPin.length();
                                 Log.d(TAG, "onClick: PIN ENTRY *******************************");

                                 if(good == 5) {
                                     SharedPreferences passFile = getSharedPreferences(PASSWORD_PREF,0);  // mode private if 0
                                     SharedPreferences.Editor myeditor = passFile.edit();
                                     String salt = getSha1(newPin);

                                     Log.d(TAG, "onClick: PIN SALT *******************************" + salt);

                                     myeditor.putString("PinSalt", salt);
                                     myeditor.apply();

                                     Toasty.success(getBaseContext(),"PIN changed to " + newPin, Toast.LENGTH_SHORT, true).show();
                                 }
                                 else {
                                     box.cancel();
                                     Toasty.warning(getBaseContext(),"PIN must be 5 digits", Toast.LENGTH_SHORT, true).show();
                                 }

                             }
                         })
                         .setNegativeButton("Cancel",
                                 new DialogInterface.OnClickListener() {
                                     public void onClick(DialogInterface box, int id) {
                                         box.cancel();
                                     }
                                 });

                 AlertDialog dialog = userInput.create();
                 dialog.show();
             }
         });

         /*
         Shuffle pin-code functionality
          */

         shuffle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
             @Override
             public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                 Log.d(TAG, "onCheckedChanged: " + isChecked);

                 SharedPreferences settingsFile = getSharedPreferences(SETTINGS_PREF,0);  // mode private if 0
                 SharedPreferences.Editor myEditor = settingsFile.edit();

                 if(isChecked) {

                     myEditor.putBoolean(SHUFFLE_STORE, isChecked);
                     myEditor.apply();
                 }
                 else {
                     myEditor.putBoolean(SHUFFLE_STORE, isChecked);
                     myEditor.apply();
                 }
             }
         });



    }




}
