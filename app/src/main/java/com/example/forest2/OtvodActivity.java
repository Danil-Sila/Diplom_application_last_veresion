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

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import static com.example.forest2.RazryadActivity.vidID;
import static com.example.forest2.MainActivity.vidID_edit;

public class OtvodActivity extends AppCompatActivity implements View.OnClickListener, LoaderManager.LoaderCallbacks<Cursor>{

    private static final int CM_DELETE_ID = 1;
    public static String videlID="";

    DataBaseHelper myDbHelper;
    SimpleCursorAdapter scAdapter;
    Cursor c = null;

    Button btnAddOT, btnClearOT, btnCalcOT;
    ListView lvOtvod;
    EditText edtNumOT, edtSrRazOT, edtKvartalOT, edtVvidelOT, edtTypeOT;
    ArrayList<String> listPoroda;
    ArrayList<String> srRazr;
    ArrayList<String> typePoroda;
    ArrayList<String> listD;
    String Poroda, Kat;
    int D, i, idDirectoryV=0, idSostavD=0;
    String[] kat = {"Деловая", "Дровяная"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_otvod);

        btnAddOT = (Button) findViewById(R.id.btnAddOT);
        btnClearOT = (Button) findViewById(R.id.btnClearOT);
        btnCalcOT = (Button) findViewById(R.id.btnCalcOT);
        btnAddOT.setOnClickListener(this);
        btnClearOT.setOnClickListener(this);
        btnCalcOT.setOnClickListener(this);
        btnCalcOT.setEnabled(false);

        edtNumOT = (EditText) findViewById(R.id.edtNumOT);
        edtSrRazOT = (EditText) findViewById(R.id.edtSrRazOT);
        edtKvartalOT = (EditText) findViewById(R.id.edtKvartalOT);
        edtTypeOT = (EditText) findViewById(R.id.edtTypeOT);
        edtVvidelOT = (EditText) findViewById(R.id.edtVvidelOT);
        edtNumOT.setEnabled(false);
        edtSrRazOT.setEnabled(false);
        edtKvartalOT.setEnabled(false);
        edtVvidelOT.setEnabled(false);
        edtTypeOT.setEnabled(false);
        if (vidID != "") videlID = vidID;
        if (vidID_edit != "") {
            videlID = vidID_edit;
            btnCalcOT.setEnabled(true);
        }
        LoadDB();   //загрузка БД
        AddSpinnersOtvod(); //заполнение значениями в спинерах
        setNumDel();    //добавление данных о делянке
        LVShow();   //создание адаптра и заполнение списка данынми
    }
    //получение номера делянки,квартала и выдела в соответствующее поле
    private void setNumDel() {
        //Получаем в курсор данные о делянке
        c =  myDbHelper.getValueDel(Integer.valueOf(videlID));
        if (c.moveToFirst()){
            do {
                edtKvartalOT.setText(c.getString(0));
                edtNumOT.setText(c.getString(1));
                edtVvidelOT.setText(c.getString(2));
            }while (c.moveToNext());
        }
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

    //содание адаптера и привязка его к списку для отображения инф из отвода
    private void LVShow() {
        String[] from;
        from  = new String[] {DataBaseHelper.D_PORODA_PORODA, DataBaseHelper.D_WIDTH_D, DataBaseHelper.D_RAZRH_R, DataBaseHelper.OTVOD_KATEGORY, DataBaseHelper.OTVOD_OBYOM};
        int[] to = new int[] {R.id.tvPorodaO, R.id.tvDO, R.id.tvRO, R.id.tvKatO, R.id.tvVO};
        scAdapter = new SimpleCursorAdapter(this, R.layout.otvod_item, null, from, to, 0);
        lvOtvod = (ListView) findViewById(R.id.lvOtvod);
        lvOtvod.setAdapter(scAdapter);
        // добавляем контекстное меню к списку
        registerForContextMenu(lvOtvod);
        // создаем лоадер для чтения данных
        getSupportLoaderManager().initLoader(0, null, this);
    }

    private void AddSpinnersOtvod() {
        //порода
        listPoroda = myDbHelper.getPorodaSostavDel(2,Integer.valueOf(videlID),Poroda);
        ArrayAdapter<String> adapterP = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, listPoroda);
        adapterP.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        final Spinner spinner = (Spinner) findViewById(R.id.spPorodaOT);
        spinner.setAdapter(adapterP);
        //выделяем елемент
        spinner.setSelection(0);
        //установка обработчика нажатия
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                //элемент выпадающего списка
                Poroda = spinner.getSelectedItem().toString();
                srRazr = myDbHelper.getPorodaSostavDel(3,Integer.valueOf(videlID),Poroda);
                typePoroda = myDbHelper.getPorodaSostavDel(4,Integer.valueOf(videlID),Poroda);
                edtSrRazOT.setText(srRazr.get(0));
                edtTypeOT.setText(typePoroda.get(0));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        //ступень толщины
        listD = myDbHelper.getValueSpR(DataBaseHelper.DB_TABLE_DIRECTORY_WIDTH, DataBaseHelper.D_WIDTH_D);
        ArrayAdapter<String> adapterST = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, listD);
        adapterST.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        final Spinner spinnerST = (Spinner) findViewById(R.id.spWidthOT);
        spinnerST.setAdapter(adapterST);
        spinnerST.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                //элемент выпадающего списка
                String s;
                s = spinnerST.getSelectedItem().toString();
                D = Integer.valueOf(s);
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        //категория дерева
        ArrayAdapter<String> adapterKat = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, kat);
        adapterP.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        final Spinner spinnerKat = (Spinner) findViewById(R.id.spKategoryOT);
        spinnerKat.setAdapter(adapterKat);
        //выделяем елемент
        spinnerKat.setSelection(0);
        spinnerKat.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                //элемент выпадающего списка
                Kat = spinnerKat.getSelectedItem().toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
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
                Toast.makeText(OtvodActivity.this, getString(R.string.addInfDel), Toast.LENGTH_LONG).show();
                break;
            case R.id.action_addClearBD:
                AlertDialog.Builder quitDialog = new AlertDialog.Builder(
                        this);
                quitDialog.setTitle("Вы действительно желаете отчистить базу данных?");

                quitDialog.setPositiveButton("Да", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(OtvodActivity.this, "Отчистка БД возможна только из главной формы приложения!", Toast.LENGTH_SHORT).show();
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
        int numD, razr = 0;
        double V;
        switch (v.getId()) {
            case R.id.btnAddOT:
                //расчёт объёма
                razr = Integer.valueOf(srRazr.get(0));  //получение среднего разряда
                idSostavD = myDbHelper.getIDsostavDel(Integer.valueOf(videlID),Poroda); //получение ID записи состава делянки
                V = getV(razr); //получение объёма (кубатуры)
                if (V == 0) Toast.makeText(OtvodActivity.this,
                        "Ошибка! (Проверьте значения полей)", Toast.LENGTH_SHORT).show();
                else {
                    myDbHelper.addOtvod(idSostavD, idDirectoryV, Kat);  //добавление данных в таблицу
                    getSupportLoaderManager().getLoader(0).forceLoad();
                    i++;
                    if (i >= 2) btnCalcOT.setEnabled(true);
                }
                break;
            case R.id.btnClearOT:
                AlertDialog.Builder quitDialog = new AlertDialog.Builder(
                        OtvodActivity.this);
                quitDialog.setTitle("Вы действительно желаете отчистить таблицу?");

                quitDialog.setPositiveButton("Да", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        myDbHelper.delRecOtvod(Integer.valueOf(videlID));   //удаляем записи связанные только с этой делянкой-выделом
                        getSupportLoaderManager().getLoader(0).forceLoad();
                       // Toast.makeText(OtvodActivity.this, "Таблица отчищена!", Toast.LENGTH_SHORT).show();
                        i = 0;
                        btnCalcOT.setEnabled(false);
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
            case R.id.btnCalcOT:
                vidID = "";
                vidID_edit = "";
                Intent intent = new Intent(this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                break;
        }
    }
    //получение объёма
    private double getV(int srH) {
        double v=0;
        c = myDbHelper.getDirV();
        if (c.moveToFirst()){
            do {
                if (Poroda.equals(c.getString(1)) && D == c.getInt(2) && srH == c.getInt(3)) {
                    if (Kat == "Деловая") {
                        v = c.getDouble(4);
                        idDirectoryV = c.getInt(0);
                        break;
                    }
                    if (Kat == "Дровяная") {
                        v = c.getDouble(5);
                        idDirectoryV = c.getInt(0);
                        break;
                    }
                }
            }while (c.moveToNext());
        }
        return v;
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
            // получаем из пункта контекстного меню данные по пункту списка
            AdapterView.AdapterContextMenuInfo acmi = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
            // извлекаем id записи и удаляем соответствующую запись в БД
            myDbHelper.delRec(DataBaseHelper.DB_TABLE_OTVOD, DataBaseHelper.OTVOD_ID,acmi.id);
            if ((i > 0) && (Integer.valueOf(videlID) != 0)) i--;
            Toast.makeText(this, "Запись удалена!", Toast.LENGTH_SHORT).show();
            // получаем новый курсор с данными
            getSupportLoaderManager().getLoader(0).forceLoad();
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
            Cursor cursor=null;
            cursor = myDbHelper.getOtvod(Integer.valueOf(videlID));
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
                OtvodActivity.this);
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
        myDbHelper.close();
        vidID = "";
        vidID_edit = "";
        finish();
    }
}