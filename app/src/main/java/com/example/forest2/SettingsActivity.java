package com.example.forest2;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class SettingsActivity extends AppCompatActivity implements View.OnClickListener {

    EditText edtMainP, edtDopP;
    Button btnSave;
    SharedPreferences sPref;

    final String SAVED_MAIN = "saved_main";
    final String SAVED_DOP = "saved_dop";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        edtMainP = (EditText) findViewById(R.id.edtMainP);
        edtDopP = (EditText) findViewById(R.id.edtDopP);

        btnSave = (Button) findViewById(R.id.btnSave);
        btnSave.setOnClickListener(this);
        loadText();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btnSave:
                if (edtMainP.getText().toString().isEmpty() || edtDopP.getText().toString().isEmpty()){
                    Toast.makeText(this, "Заполните пустые поля!", Toast.LENGTH_SHORT).show();
                }
                else{
                    SharedPreferences.Editor ed = sPref.edit();
                    ed.putString(SAVED_MAIN,edtMainP.getText().toString());
                    ed.putString(SAVED_DOP,edtDopP.getText().toString());
                    ed.apply();
                    Toast.makeText(this,"Настройки сохранены",Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    //загрузка настроек из SharedPreferences
    public void loadText() {
        sPref = getSharedPreferences("settings",MODE_PRIVATE);
        String main = sPref.getString(SAVED_MAIN,"15");
        String dop = sPref.getString(SAVED_DOP,"5");
        edtMainP.setText(main);
        edtDopP.setText(dop);
        Toast.makeText(this,"Настройки загружены",Toast.LENGTH_SHORT).show();
    }
}