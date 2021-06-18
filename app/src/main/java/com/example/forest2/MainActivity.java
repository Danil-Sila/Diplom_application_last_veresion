package com.example.forest2;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.CursorLoader;
import androidx.loader.content.Loader;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import static com.example.forest2.RazryadActivity.vidID;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, LoaderManager.LoaderCallbacks<Cursor>{

    private static final int CM_INFORM_ID = 1;
    private static final int CM_DELETE_ID = 2;
    private static final int CM_UPDATE_ID = 3;

    //переменнная для передачи id выдела для получения подробной информации
    public static String vidID_edit = "";

    DataBaseHelper myDbHelper;
    Button btnAddV, btnClearRez;
    SimpleCursorAdapter scAdapter;
    ListView lvDel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnAddV = (Button) findViewById(R.id.btnAddDel);
        btnClearRez = (Button) findViewById(R.id.btnClearDel);
        btnAddV.setOnClickListener(this);
        btnClearRez.setOnClickListener(this);

        LoadDB();//подключение к БД
        lvDelShow();
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

    //функция для отображения списка в информации о расчетах
    private void lvDelShow() {
        String[] from = new String[] {DataBaseHelper.INFDEL_QVARTAL, DataBaseHelper.MAIN_DEL, DataBaseHelper.VVIDEL_VIDEL,
                DataBaseHelper.SUM_OBYOM, DataBaseHelper.VVIDEL_ID};
        int[] to = new int[] {R.id.tvQvartalM, R.id.tvNumDelM, R.id.tvVidelM, R.id.tvItogM};
        // создааем адаптер и настраиваем список
        scAdapter = new SimpleCursorAdapter(this, R.layout.main_item, null, from, to, 0);
        lvDel = (ListView) findViewById(R.id.lvDel);
        lvDel.setAdapter(scAdapter);
        // добавляем контекстное меню к списку
        registerForContextMenu(lvDel);
        // создаем лоадер для чтения данных
        getSupportLoaderManager().initLoader(0, null, this);
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
                Toast.makeText(MainActivity.this, getString(R.string.addInfDel), Toast.LENGTH_LONG).show();
                break;
            case R.id.action_addClearBD:
                AlertDialog.Builder quitDialog = new AlertDialog.Builder(
                        this);
                quitDialog.setTitle("Вы действительно желаете отчистить базу данных?");

                quitDialog.setPositiveButton("Да", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        myDbHelper.delAllData(DataBaseHelper.DB_TABLE_OTVOD);
                        myDbHelper.delAllData(DataBaseHelper.DB_TABLE_SOSTAVDEL);
                        myDbHelper.delAllData(DataBaseHelper.DB_TABLE_INFDEL);
                        getSupportLoaderManager().getLoader(0).forceLoad();
                        Toast.makeText(MainActivity.this, "БД отчищена!", Toast.LENGTH_SHORT).show();
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

    @Override
    public void onClick(View v) {
        int count;
        switch (v.getId()){
            case R.id.btnAddDel:
                //получение количества записей из таблицы информации о делянках
                count = myDbHelper.getCountInfDel();
                if (count > 0){
                    vidID_edit = "";
                    Intent intent = new Intent(this, VvidelActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                }
                else Toast.makeText(this, "Заполните информацию о делянках!", Toast.LENGTH_SHORT).show();
                break;
            case R.id.btnClearDel:
                AlertDialog.Builder quitDialog = new AlertDialog.Builder(
                        this);
                quitDialog.setTitle("Вы действительно желаете отчистить таблицу?");

                quitDialog.setPositiveButton("Да", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        myDbHelper.delAll();
                        Toast.makeText(MainActivity.this, "Таблица очищена!", Toast.LENGTH_SHORT).show();
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
                break;
        }
    }

    //создание и обработка пунктов меню
    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        menu.add(0, CM_INFORM_ID, 0, R.string.inform_record);
        menu.add(0, CM_DELETE_ID, 0, R.string.delete_record);
        menu.add(0, CM_UPDATE_ID, 0, R.string.update_record);
    }

    public boolean onContextItemSelected(MenuItem item) {
        if (item.getItemId() == CM_INFORM_ID) {
            // получаем из пункта контекстного меню данные по пункту списка
            AdapterView.AdapterContextMenuInfo acmi = (AdapterView.AdapterContextMenuInfo) item
                    .getMenuInfo();
           // Toast.makeText(this, "Номер записи"+String.valueOf(acmi.id), Toast.LENGTH_SHORT).show();
            vidID_edit = String.valueOf(acmi.id);
            Intent intentInf = new Intent(this,InformActivity.class);
            intentInf.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intentInf);
            return true;
        }
        if (item.getItemId() == CM_DELETE_ID) {
            AlertDialog.Builder quitDialog = new AlertDialog.Builder(
                    this);
            quitDialog.setTitle("Вы действительно желаете удалить запись?");

            quitDialog.setPositiveButton("Да", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    ArrayList<Integer> sostavID;
                    // получаем из пункта контекстного меню данные по пункту списка
                    AdapterView.AdapterContextMenuInfo acmi = (AdapterView.AdapterContextMenuInfo) item
                            .getMenuInfo();
                    sostavID = myDbHelper.getIdSostav(acmi.id);
                    for (int i=0;i<sostavID.size();i++){
                        myDbHelper.delRec(DataBaseHelper.DB_TABLE_OTVOD, DataBaseHelper.OTVOD_SOSTAVDELID, Long.valueOf(sostavID.get(i)));
                        myDbHelper.delRec(DataBaseHelper.DB_TABLE_SOSTAVDEL, DataBaseHelper.SOSTAVDEL_ID, Long.valueOf(sostavID.get(i)));
                    }
                    myDbHelper.delRec(DataBaseHelper.DB_TABLE_VVIDEL,DataBaseHelper.VVIDEL_ID, acmi.id);
                    // получаем новый курсор с данными
                    getSupportLoaderManager().getLoader(0).forceLoad();
                    Toast.makeText(MainActivity.this, "Запись удалена!", Toast.LENGTH_SHORT).show();
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
        if (item.getItemId() == CM_UPDATE_ID) {
            // получаем из пункта контекстного меню данные по пункту списка
            AdapterView.AdapterContextMenuInfo acmi = (AdapterView.AdapterContextMenuInfo) item
                    .getMenuInfo();
            vidID_edit = String.valueOf(acmi.id);
            Intent intent = new Intent(this, OtvodActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            return true;
        }
        return super.onContextItemSelected(item);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new MyCursorLoader(this, myDbHelper);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        scAdapter.swapCursor(cursor);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    static class MyCursorLoader extends CursorLoader {
        DataBaseHelper myDbHelper;
        public MyCursorLoader(Context context, DataBaseHelper myDbHelper) {
            super(context);
            this.myDbHelper = myDbHelper;
        }

        @Override
        public Cursor loadInBackground() {
            Cursor cursor = myDbHelper.getDataMain();
            try {
                TimeUnit.SECONDS.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return cursor;
        }
    }

    @Override
    public void onBackPressed() {
        // TODO Auto-generated method stub
        //super.onBackPressed();
        openQuitDialog();
    }

    public void openQuitDialog() {
        AlertDialog.Builder quitDialog = new AlertDialog.Builder(
                MainActivity.this);
        quitDialog.setTitle("Вы действительно желаете выйти?");

        quitDialog.setPositiveButton("Да", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // TODO Auto-generated method stub
                finish();
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
    protected void onRestart() {
        super.onRestart();
        getSupportLoaderManager().getLoader(0).forceLoad();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        myDbHelper.close();
        vidID = "";
        vidID_edit = "";
        finish();
    }
}