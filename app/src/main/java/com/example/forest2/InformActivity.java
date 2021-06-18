package com.example.forest2;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;

import static com.example.forest2.MainActivity.vidID_edit;

import java.io.IOException;


public class InformActivity extends AppCompatActivity {

    DataBaseHelper myDbHelper;
    SimpleCursorAdapter scAdapter;
    ListView lvInform;
    Cursor cursor = null;
    EditText edtKvartalInf, edtDistrictInf, edtVvidelInf, edtRegionInf, edtNumdelInf, edtLeshozInf;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inform);

        edtKvartalInf = (EditText) findViewById(R.id.edtKvartalInf);
        edtNumdelInf = (EditText) findViewById(R.id.edtNumdelInf);
        edtVvidelInf = (EditText) findViewById(R.id.edtVvidelInf);
        edtRegionInf = (EditText) findViewById(R.id.edtRegionInf);
        edtLeshozInf = (EditText) findViewById(R.id.edtLeshozInf);
        edtDistrictInf = (EditText) findViewById(R.id.edtDistrictInf);

        LoadDB();   //подключение к БД
        LvInfShow();    //заполнение данными списка
    }


    //подключение БД
    private void LoadDB() {
        myDbHelper = new DataBaseHelper(this);
        try {
            myDbHelper.createDataBase();
        } catch (IOException ioe) {
            throw new Error("Unable to create database");
        }
        myDbHelper.openDataBase();
    }

    //заполнеиеи списка
    public void LvInfShow(){
        //получаем курсор
        cursor = myDbHelper.getInform(Integer.valueOf(vidID_edit));
        cursor.moveToFirst();
        edtKvartalInf.setText(cursor.getString(1));
        edtNumdelInf.setText(cursor.getString(2));
        edtVvidelInf.setText(cursor.getString(3));
        edtRegionInf.setText(cursor.getString(4));
        edtDistrictInf.setText(cursor.getString(5));
        edtLeshozInf.setText(cursor.getString(6));
        //формируем столбцы сопоставления
        String[] from = new String[] {DataBaseHelper.D_PORODA_PORODA, DataBaseHelper.SOSTAVDEL_TYPEPORODA,
                DataBaseHelper.KolDel, DataBaseHelper.KolDr, DataBaseHelper.vDel, DataBaseHelper.vDr, DataBaseHelper.SUM_OBYOM};
        int[] to = new int[] {R.id.tvPorodaInf, R.id.tvTypeInf, R.id.tvKolDelInf, R.id.tvKolDrInf,
                R.id.tvVdelInf, R.id.tvVdrInf, R.id.tvItogInf};
        //создаём адаптер и настраиваем список
        scAdapter = new SimpleCursorAdapter(this, R.layout.inform_item, cursor,from,to);
        lvInform = (ListView) findViewById(R.id.lvInform);
        lvInform.setAdapter(scAdapter);
    }

    //создание и обработка пунктов меню
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch (id){
            case R.id.action_settings:
                Intent intent = new Intent(this, SettingsActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                break;
            case R.id.action_addInfDel:
                Intent intent1 = new Intent(this, DelyankaActivity.class);
                intent1.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent1);
                Toast.makeText(InformActivity.this, getString(R.string.addInfDel), Toast.LENGTH_LONG).show();
                break;
            case R.id.action_addClearBD:
                AlertDialog.Builder quitDialog = new AlertDialog.Builder(
                        this);
                quitDialog.setTitle("Вы действительно желаете отчистить базу данных?");

                quitDialog.setPositiveButton("Да", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(InformActivity.this, "Отчистка БД возможна только из главной формы приложния!", Toast.LENGTH_SHORT).show();
                    }
                });
                quitDialog.setNegativeButton("Нет", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // TODO Auto-generated method stub
                    }
                });
                quitDialog.show();
                break;
            case R.id.action_addExit:
                openQuitDialog();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    //функция для закрытия приложения
    public void openQuitDialog() {
        AlertDialog.Builder quitDialog = new AlertDialog.Builder(
                InformActivity.this);
        quitDialog.setTitle("Вы действительно желаете выйти?");

        quitDialog.setPositiveButton("Да", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // TODO Auto-generated method stub
                finishAffinity();
                System.exit(0);
            }
        });
        quitDialog.setNegativeButton("Нет", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // TODO Auto-generated method stub
            }
        });

        quitDialog.show();
    }

    //при закрытии активити закрываем все подключения
    @Override
    protected void onDestroy() {
        super.onDestroy();
        myDbHelper.close();
        finish();
    }

}