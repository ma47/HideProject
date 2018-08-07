package com.hide.project.hideproject;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import com.andrognito.pinlockview.IndicatorDots;
import com.andrognito.pinlockview.PinLockListener;
import com.andrognito.pinlockview.PinLockView;

import es.dmoral.toasty.Toasty;

import static com.hide.project.hideproject.Hashing.getSha1;

public class LoginActivity extends AppCompatActivity {

    private PinLockView mPinLockView;
    private IndicatorDots mIndicatorDots;
    private TextView welcome;
    private final static String TAG = LoginActivity.class.getSimpleName();
    public static final String PASSWORD_PREF = "PassPref";
    public static final String PASSWORD_STORE = "PinSalt";
    private String check;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_login);

        mPinLockView = findViewById(R.id.pin_lock_view);
        mIndicatorDots = findViewById(R.id.indicator_dots);

        mPinLockView.attachIndicatorDots(mIndicatorDots);
        mPinLockView.setPinLockListener(mPinLockListener);

        /*
        Shuffle if settings slider is ON
         */
        SharedPreferences shuffle = getSharedPreferences(SettingsActivity.SETTINGS_PREF, 0);
        boolean isOn = shuffle.getBoolean(SettingsActivity.SHUFFLE_STORE, true);
        if(isOn) {
            mPinLockView.enableLayoutShuffling();
        }


        mPinLockView.setPinLength(5);
        mPinLockView.setTextColor(ContextCompat.getColor(this, R.color.white));

        mIndicatorDots.setIndicatorType(IndicatorDots.IndicatorType.FILL_WITH_ANIMATION);
    }

    @Override
    protected void onStart() { // Invoke PIN Code creation if the user is new
        super.onStart();
        check = getPin();
        if(check.isEmpty()) {
            Toasty.warning(LoginActivity.this, "CREATE 5 DIGIT PASSWORD", Toast.LENGTH_LONG, true).show();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent f = new Intent(Intent.ACTION_MAIN);
        f.addCategory(Intent.CATEGORY_HOME);
        f.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(f);
    }

    /*
        If pincode input by the user that is equal to SHA1 hashed pincode in the device's memory, let the user pass, if not - give other ccommands
        such as incorrect pin or new user pin
     */
    private PinLockListener mPinLockListener = new PinLockListener() {

            @Override
            public void onComplete (String pin){
            Log.d(TAG, "Pin complete: " + pin);
            check = getPin();
            String salt = getSha1(pin);
            if (check.isEmpty()) {
                setPin(pin);
                Toasty.success(LoginActivity.this, "Success! Your PIN is: " + pin, Toast.LENGTH_LONG, true).show();
                Intent intent = new Intent(getBaseContext(), MainActivity.class);
                startActivity(intent);
            }
            else if (check.equals(salt)){
                Intent intent = new Intent(getBaseContext(), MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            }
            else {
                Toasty.error(LoginActivity.this, "INCORRECT PIN CODE!", Toast.LENGTH_SHORT, true).show();
            }
            }

            @Override
            public void onEmpty () {
            Log.d(TAG, "Pin empty");
            Toasty.info(LoginActivity.this, "Enter PIN CODE", Toast.LENGTH_SHORT, true).show();
            }

            @Override
            public void onPinChange ( int pinLength, String intermediatePin){
            Log.d(TAG, "Pin changed, new length " + pinLength + " with intermediate pin " + intermediatePin);
            }
    };

    public void setPin(String pin) {
        SharedPreferences passFile = getSharedPreferences(PASSWORD_PREF,0);  // mode private if 0
        SharedPreferences.Editor myeditor = passFile.edit();
        String salt = getSha1(pin);
        myeditor.putString("PinSalt", salt);
        myeditor.apply();
    }

    public String getPin() {
        SharedPreferences passFile = getSharedPreferences(PASSWORD_PREF,0);
        return passFile.getString(PASSWORD_STORE, "");
    }
}
