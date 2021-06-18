package com.example.forest2;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.CursorLoader;
import androidx.loader.content.Loader;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.SQLException;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class DelyankaActivity extends AppCompatActivity implements View.OnClickListener, LoaderManager.LoaderCallbacks<Cursor>{

    private static final int CM_DELETE_ID = 1;

    DataBaseHelper myDbHelper;
    SimpleCursorAdapter scAdapter;

    Button btnAddInfDel, btnDelInfDel;
    ListView lvInfDel;
    EditText edtRegion, edtDistrict, edtLeshoz, edtKvartal, edtSquare;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delyanka);

        edtRegion = (EditText) findViewById(R.id.edtRegion);
        edtDistrict = (EditText) findViewById(R.id.edtDictrict);
        edtLeshoz = (EditText) findViewById(R.id.edtLeshoz);
        edtKvartal = (EditText) findViewById(R.id.edtKvartal);
        edtSquare = (EditText) findViewById(R.id.edtSquare);

        btnAddInfDel = (Button) findViewById(R.id.btnAddInfDel);
        btnAddInfDel.setOnClickListener(this);
        btnDelInfDel = (Button) findViewById(R.id.btnDelInfDel);
        btnDelInfDel.setOnClickListener(this);

        LoadDB();   //загрузка Базы данных
        LVShow();   //создание адаптера и создание связи с listview
    }

    private void LoadDB() throws SQLException {
        myDbHelper = new DataBaseHelper(this);
        try {
            myDbHelper.createDataBase();
        } catch (IOException ioe) {
            throw new Error("Unable to create database");
        }
        myDbHelper.openDataBase();
    }

    private void LVShow() {
        String[] from = new String[] {DataBaseHelper.INFDEL_ID, DataBaseHelper.INFDEL_QVARTAL, DataBaseHelper.INFDEL_REGION, DataBaseHelper.INFDEL_DISTRICT, DataBaseHelper.INFDEL_LESHOZ, DataBaseHelper.INFDEL_SQUARE};
        int[] to = new int[] {R.id.tvNumd, R.id.tvQvartal, R.id.tvRegion, R.id.tvDistrict, R.id.tvLeshoz, R.id.tvSquare};
        scAdapter = new SimpleCursorAdapter(this, R.layout.delyanka_item, null, from, to, 0);
        lvInfDel = (ListView) findViewById(R.id.lvInfDel);
        lvInfDel.setAdapter(scAdapter);
        // добавляем контекстное меню к списку
        registerForContextMenu(lvInfDel);
        // создаем лоадер для чтения данных
        getSupportLoaderManager().initLoader(0, null, this);
    }

    @Override
    public void onClick(View v) {
        int k=0;
        switch (v.getId()){
            case R.id.btnAddInfDel:
                String region, district, leshoz;
                Integer kvartal;
                Double square;

                if (edtRegion.getText().toString().equals("") || edtDistrict.getText().toString().equals("") || edtLeshoz.getText().toString().equals("")
                        || edtKvartal.getText().toString().equals("") || edtSquare.getText().toString().equals("")){
                    Toast.makeText(this, "Пожалуйста! заполните поля", Toast.LENGTH_SHORT).show();
                }
                else {
                    region = edtRegion.getText().toString();
                    district = edtDistrict.getText().toString();
                    leshoz = edtLeshoz.getText().toString();
                    kvartal = Integer.valueOf(edtKvartal.getText().toString());
                    square = Double.valueOf(edtSquare.getText().toString());
                    k = myDbHelper.getKvartalInfDel(kvartal);
                    if (k==1){
                        Toast.makeText(this, "Данный квартал уже используется!", Toast.LENGTH_SHORT).show();
                    }
                    else {
                        myDbHelper.addInfDel(region,district,leshoz,kvartal,square);
                        getSupportLoaderManager().getLoader(0).forceLoad();
                    }
                }
                break;
            case R.id.btnDelInfDel:
                AlertDialog.Builder quitDialog = new AlertDialog.Builder(
                        this);
                quitDialog.setTitle("Отчистить таблицу? Отчистка может повлиять на работу!!!");
                quitDialog.setPositiveButton("Да", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        myDbHelper.delAllData(DataBaseHelper.DB_TABLE_INFDEL);
                        getSupportLoaderManager().getLoader(0).forceLoad();
                        Toast.makeText(DelyankaActivity.this, "Таблица отчищена!", Toast.LENGTH_SHORT).show();
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
        }
    }

    //создание контекстного меню для записей из списка
    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        menu.add(0, CM_DELETE_ID, 0, R.string.delete_record);
    }
    //обработчик выбора для контекстного меню
    public boolean onContextItemSelected(MenuItem item) {
        if (item.getItemId() == CM_DELETE_ID) {
            AlertDialog.Builder quitDialog = new AlertDialog.Builder(
                    this);
            quitDialog.setTitle("Удаление используемой записи повлияет на работу! Удалить?");

            quitDialog.setPositiveButton("Да", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    // получаем из пункта контекстного меню данные по пункту списка
                    AdapterView.AdapterContextMenuInfo acmi = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
                    // извлекаем id записи и удаляем соответствующую запись в БД
                    myDbHelper.delRec(DataBaseHelper.DB_TABLE_INFDEL, DataBaseHelper.INFDEL_ID,acmi.id);
                    // получаем новый курсор с данными
                    getSupportLoaderManager().getLoader(0).forceLoad();
                }
            });
            quitDialog.setNegativeButton("Нет", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    // TODO Auto-generated method stub
                }
            });
            quitDialog.show();
            return true;
        }
        return super.onContextItemSelected(item);
    }

    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int id, @Nullable Bundle args) {
        return new MyCursorLoader(this, myDbHelper);
    }

    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor cursor) {
        scAdapter.swapCursor(cursor);
    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {

    }
    static class MyCursorLoader extends CursorLoader {
        DataBaseHelper myDbHelper;
        public MyCursorLoader(Context context, DataBaseHelper myDbHelper) {
            super(context);
            this.myDbHelper = myDbHelper;
        }

        @Override
        public Cursor loadInBackground() {
            //получаем в курсов все записи из таблицы Инф о делянке
            Cursor cursor = myDbHelper.getInfDelAll();
            try {
                TimeUnit.SECONDS.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return cursor;
        }
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
                Toast.makeText(DelyankaActivity.this, getString(R.string.addInfDel), Toast.LENGTH_LONG).show();
                break;
            case R.id.action_addClearBD:
                AlertDialog.Builder quitDialog = new AlertDialog.Builder(
                        this);
                quitDialog.setTitle("Вы действительно желаете отчистить базу данных?");

                quitDialog.setPositiveButton("Да", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(DelyankaActivity.this, "Отчистка БД возможна только из главной формы приложения!", Toast.LENGTH_SHORT).show();
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
                DelyankaActivity.this);
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        myDbHelper.close();;
        finish();
    }
}