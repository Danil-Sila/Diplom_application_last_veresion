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
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

public class VvidelActivity extends AppCompatActivity implements View.OnClickListener, LoaderManager.LoaderCallbacks<Cursor>{

    private static final int CM_DELETE_ID = 1;
    public static String kvart="";  //передача квартала в RazryadActivity
    public static String vid="";    //передача выдела в RazryadActivity
    public static String numd="";   //передача номера делянки в RazryadActivity

    DataBaseHelper myDbHelper;
    SimpleCursorAdapter scAdapter;
    Cursor c = null;

    ArrayList<String> listRegion, listDistrict, listLeshoz, listKvartal, listNumdel, listSquare;
    Button btnAddV;
    EditText edtSquareV, edtVvidel, edtNumdelV;
    ListView lvVvidel;
    String region="", district="", leshoz="", kvartal="";
    public static Integer id=0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vvidel);

        edtSquareV = (EditText) findViewById(R.id.edtSquareV);
        edtVvidel = (EditText) findViewById(R.id.edtVvidel);
        edtNumdelV = (EditText) findViewById(R.id.edtNumdelV);

        btnAddV = (Button) findViewById(R.id.btnAddV);
        btnAddV.setOnClickListener(this);
        LoadDB();   //загрузка БД
        AddSpiners();   //заполненеи данными в выпадающих списках
        LVShow();   //создание и списка и привязка к адаптеру
    }
    //подключение БД
    private void LoadDB() throws SQLException {
        myDbHelper = new DataBaseHelper(this);
        try {
            myDbHelper.createDataBase();
        } catch (IOException ioe) {
            throw new Error("Unable to create database");
        }
        myDbHelper.openDataBase();
    }

    //отображение информации о выделе на выбранной делянке
    private void LVShow() {
        String[] from = new String[] {DataBaseHelper.INFDEL_QVARTAL, DataBaseHelper.VVIDEL_INFDELID, DataBaseHelper.VVIDEL_VIDEL};
        int[] to = new int[] {R.id.tvKvartalV, R.id.tvNumdelV, R.id.tvVvidelV};
        scAdapter = new SimpleCursorAdapter(this, R.layout.vvidel_item, null, from, to, 0);
        lvVvidel = (ListView) findViewById(R.id.lvVvidel);
        lvVvidel.setAdapter(scAdapter);
        // добавляем контекстное меню к списку
        registerForContextMenu(lvVvidel);
        // создаем лоадер для чтения данных
        getSupportLoaderManager().initLoader(0, null, this);
    }
    //добавлние регионов
    private void AddSpiners() {
        listRegion = myDbHelper.getValueFromINFDEL(1, region,district,leshoz,kvartal);
        ArrayAdapter<String> adapterRegion = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, listRegion);
        adapterRegion.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        final Spinner spRegion = (Spinner) findViewById(R.id.spRegion);
        spRegion.setAdapter(adapterRegion);
        //заголовок
        spRegion.setPrompt("Регион");
        //выделяем елемент
        spRegion.setSelection(0);
        region = listRegion.get(0);
        //установка обработчика нажатия
        spRegion.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                //элемент выпадающего списка
                 region = spRegion.getSelectedItem().toString();
                 AddSpDistrict();   //заполнение списка с районами
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    //дабавлене районов
    public void AddSpDistrict(){
        listDistrict = myDbHelper.getValueFromINFDEL(2, region,district,leshoz,kvartal);
        ArrayAdapter<String> adapterDistrict = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, listDistrict);
        adapterDistrict.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        final Spinner spDistrict = (Spinner) findViewById(R.id.spDistrict);
        spDistrict.setAdapter(adapterDistrict);
        //заголовок
        spDistrict.setPrompt("Район");
        //выделяем елемент
        spDistrict.setSelection(0);
        //установка обработчика нажатия
        spDistrict.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                //элемент выпадающего списка
                district = spDistrict.getSelectedItem().toString();
                AddSpLeshoz();  //заполнение списка с лесхозами
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    //добавление лесхозов
    public void AddSpLeshoz(){
        listLeshoz = myDbHelper.getValueFromINFDEL(3, region,district,leshoz,kvartal);
        ArrayAdapter<String> adapterLeshoz = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, listLeshoz);
        adapterLeshoz.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        final Spinner spLeshoz = (Spinner) findViewById(R.id.spLeshoz);
        spLeshoz.setAdapter(adapterLeshoz);
        //заголовок
        spLeshoz.setPrompt("Лесхоз");
        //выделяем елемент
        spLeshoz.setSelection(0);
        //установка обработчика нажатия
        spLeshoz.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                //элемент выпадающего списка
                leshoz = spLeshoz.getSelectedItem().toString();
                AddSpKvartal(); //заполнение списка с кварталами
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    //добавление кварталов
    public void AddSpKvartal(){
        listKvartal = myDbHelper.getValueFromINFDEL(4, region,district,leshoz,kvartal);
        ArrayAdapter<String> adapterKvartal = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, listKvartal);
        adapterKvartal.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        final Spinner spKvartal = (Spinner) findViewById(R.id.spKvartal);
        spKvartal.setAdapter(adapterKvartal);
        //заголовок
        spKvartal.setPrompt("Квартал");
        //выделяем елемент
        spKvartal.setSelection(0);
        //установка обработчика нажатия
        spKvartal.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                //элемент выпадающего списка
                kvartal = spKvartal.getSelectedItem().toString();
                AddEdtSquare(); //заполнение поля для площади и номера делянки
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }
    //заполнение полей номера делянки и площади
    private void AddEdtSquare() {
        listSquare = myDbHelper.getValueFromINFDEL(5,region,district,leshoz,kvartal);
        edtSquareV.setText(listSquare.get(0));
        listNumdel = myDbHelper.getValueFromINFDEL(0,region,district,leshoz,kvartal);
        edtNumdelV.setText(listNumdel.get(0));
        id = myDbHelper.getIdVvidel(region,district,leshoz,kvartal);
        getSupportLoaderManager().getLoader(0).forceLoad();
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
                Toast.makeText(VvidelActivity.this, getString(R.string.addInfDel), Toast.LENGTH_LONG).show();
                break;
            case R.id.action_addClearBD:
                AlertDialog.Builder quitDialog = new AlertDialog.Builder(
                        this);
                quitDialog.setTitle("Вы действительно желаете отчистить базу данных?");

                quitDialog.setPositiveButton("Да", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(VvidelActivity.this, "Отчистка БД возможна только из главной формы приложения!", Toast.LENGTH_SHORT).show();
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
        int f;
        switch (v.getId()){
            case R.id.btnAddV:
                if (edtSquareV.getText().toString().equals("") || edtVvidel.getText().toString().equals("")){
                    Toast.makeText(this, "Заполните все поля!", Toast.LENGTH_SHORT).show();
                }else{
                    f = myDbHelper.findVidel(id,Integer.valueOf(edtVvidel.getText().toString()));
                    if (f == 1){
                        Toast.makeText(this, "Данный выдел уже есть на этой делянке!", Toast.LENGTH_SHORT).show();
                    }
                    else{
                        //добавление данных в табоицу выдел
                        myDbHelper.addRecVvidel(id,Integer.valueOf(edtVvidel.getText().toString()));
                        getSupportLoaderManager().getLoader(0).forceLoad();
                        Toast.makeText(this, "Выдел добавлен!", Toast.LENGTH_SHORT).show();
                        kvart = kvartal;
                        vid = edtVvidel.getText().toString();
                        numd =listNumdel.get(0);
                        Intent intent = new Intent(this, RazryadActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                    }
                }
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
            ArrayList<Integer> sostDelID;
            // получаем из пункта контекстного меню данные по пункту списка
            AdapterView.AdapterContextMenuInfo acmi = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
            // извлекаем id записи и удаляем соответствующую запись в БД
            myDbHelper.delRec(DataBaseHelper.DB_TABLE_VVIDEL, DataBaseHelper.VVIDEL_ID,acmi.id);    //удаление выбранной записи из выдела
            //Если в составе делянки была удалённая запись, то удалям всё что с ней связано
            sostDelID = myDbHelper.getIdSostav(acmi.id);
            if (sostDelID.size()>0){
                for (int i = 0; i<sostDelID.size();i++){
                    myDbHelper.delRec(DataBaseHelper.DB_TABLE_SOSTAVDEL, DataBaseHelper.SOSTAVDEL_ID, Long.valueOf(sostDelID.get(i)));
                    myDbHelper.delRec(DataBaseHelper.DB_TABLE_OTVOD, DataBaseHelper.OTVOD_SOSTAVDELID, Long.valueOf(sostDelID.get(i)));
                }
            }
            // получаем новый курсор с данными
            getSupportLoaderManager().getLoader(0).forceLoad();
            Toast.makeText(this, "Запись удалена!", Toast.LENGTH_SHORT).show();
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
            Cursor cursor = myDbHelper.getVvidel(id);
            try {
                TimeUnit.SECONDS.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return cursor;
        }
    }

    //функция для закрытия приложения
    public void openQuitDialog() {
        AlertDialog.Builder quitDialog = new AlertDialog.Builder(
                VvidelActivity.this);
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
    //при рестарте обновлям тадлицу
    @Override
    protected void onRestart() {
        super.onRestart();
        AddSpiners();   //заполненеи данными в выпадающих списках
        getSupportLoaderManager().getLoader(0).forceLoad();
    }

    //при закрытии активити закрываем все подключения
    @Override
    protected void onDestroy() {
        super.onDestroy();
        myDbHelper.close();
        finish();
    }
}