package com.example.forest2;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.SQLException;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static com.example.forest2.VvidelActivity.kvart; //квартал
import static com.example.forest2.VvidelActivity.vid;   //выдел
import static com.example.forest2.VvidelActivity.numd;   //№делянки

public class RazryadActivity extends AppCompatActivity implements View.OnClickListener {

    private static final int CM_DELETE_ID = 1;
    public static String vidID;
    public static String drazrID;
    SimpleAdapter sAdapter;
    SharedPreferences sPref;
    DataBaseHelper myDbHelper;
    Cursor c = null;

    final String ATTRIBUTE_PORODA_TEXT = "poroda";
    final String ATTRIBUTE_D_TEXT = "d";
    final String ATTRIBUTE_H_TEXT = "h";
    final String ATTRIBUTE_RAZR_TEXT = "razr";
    final String ATTRIBUTE_TYPE_TEXT = "type";
    ArrayList<Map<String, Object>> data;
    Map<String, Object> m;
    Button btnAddR, btnClearR, btnCalcR;
    EditText edtKvartalR, edtVvidelR, edtHeightD, edtKolD, edtNumdelR;
    ListView lvRazr;

    String Poroda, MainP="";
    Integer D;
    int kolDel = 0, kolMain=0, kolDop=0;

    ArrayList<Integer> listOstIzm = new ArrayList<Integer>();   //лист для хранения оставшихся измерений по породам
    ArrayList<String> listPoroda;   //лист для отображени пород
    ArrayList<String> listD;        //лист для отображения ст.толщины
    ArrayList<String> listAddPoroda = new ArrayList<String>(); //лист для отображения испульзуемых пород (сгруппированных)

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_razryad);

        btnAddR = (Button) findViewById(R.id.btnAddR);
        btnClearR = (Button) findViewById(R.id.btnClearR);
        btnCalcR = (Button) findViewById(R.id.btnCalcR);
        btnAddR.setOnClickListener(this);
        btnClearR.setOnClickListener(this);
        btnCalcR.setOnClickListener(this);
        btnCalcR.setEnabled(false);
        lvRazr = (ListView) findViewById(R.id.lvRazr);
        edtKvartalR = (EditText) findViewById(R.id.edtKvartalR);
        edtVvidelR = (EditText) findViewById(R.id.edtVvidelR);
        edtHeightD = (EditText) findViewById(R.id.edtHeightTree);
        edtKolD = (EditText) findViewById(R.id.edtKolD);
        edtNumdelR = (EditText) findViewById(R.id.edtNumdelR);

        getSettings();  //получаем данных о кол-ве измерений
        LoadDB();   //загрузка таблицы разрядов высот и сортиментной таблицы
        LVonShow(); //отображение списка
        AddSpiners(); //заполнение выпадающих списков значениями
        edtKvartalR.setText(kvart);
        edtVvidelR.setText(vid);
        edtNumdelR.setText(numd);
    }

    //получение значнеий из настроек о колчестве измерений основной и доп. породы
    public void getSettings() {
        sPref = getSharedPreferences("settings",MODE_PRIVATE);
        kolMain = Integer.valueOf(sPref.getString("saved_main","15"));
        kolDop = Integer.valueOf(sPref.getString("saved_dop","5"));
        kolDel = kolMain;
        edtKolD.setText(String.valueOf(kolDel));
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
    //настройка адаптера и привзяка его к списку
    private void LVonShow() {
        data = new ArrayList<Map<String, Object>>();
        //массив имён атрибутов, из которых будут читаться данные
        String[] from = {ATTRIBUTE_PORODA_TEXT, ATTRIBUTE_D_TEXT, ATTRIBUTE_H_TEXT, ATTRIBUTE_RAZR_TEXT, ATTRIBUTE_TYPE_TEXT};
        //массив ID View-компонентов, в которые будут вставлять данные
        int[] to = { R.id.tvP, R.id.tvD, R.id.tvH, R.id.tvR, R.id.tvType};
        //создаём адаптер
        sAdapter = new SimpleAdapter(this, data, R.layout.razr_item, from, to);
        //присваиваем списку адаптер
        lvRazr.setAdapter(sAdapter);
        registerForContextMenu(lvRazr);
    }
    //заполнение спиннеров
    private void AddSpiners() {
        listPoroda = myDbHelper.getValueSpR(DataBaseHelper.DB_TABLE_DIRECTORY_PORODA,DataBaseHelper.D_PORODA_PORODA);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, listPoroda);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        final Spinner spinner = (Spinner) findViewById(R.id.spPoroda);
        spinner.setAdapter(adapter);
        //заголовок
        spinner.setPrompt("Порода дерева");
        //выделяем елемент
        spinner.setSelection(0);
        //установка обработчика нажатия
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                //элемент выпадающего списка
                Poroda = spinner.getSelectedItem().toString();
                ostIzm(Poroda,0);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        listD = myDbHelper.getValueSpR(DataBaseHelper.DB_TABLE_DIRECTORY_WIDTH, DataBaseHelper.D_WIDTH_D);
        ArrayAdapter<String> adapterD = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, listD);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        final Spinner spinnerD = (Spinner) findViewById(R.id.spWidthTree);
        spinnerD.setAdapter(adapterD);

        //установка обработчика нажатия
        spinnerD.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String s;
                //элемент выпадающего списка
                s = spinnerD.getSelectedItem().toString();
                D=Integer.valueOf(s);
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
                if (data.size()>0){
                    Toast.makeText(this, "Для перехода к наcтройкам отчистите список!", Toast.LENGTH_SHORT).show();
                }
                else {
                    Intent intent = new Intent(this, SettingsActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                }
                break;
            case R.id.action_addInfDel:
                Intent intent1 = new Intent(this, DelyankaActivity.class);
                intent1.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent1);
                Toast.makeText(RazryadActivity.this, getString(R.string.addInfDel), Toast.LENGTH_LONG).show();
                break;
            case R.id.action_addClearBD:
                AlertDialog.Builder quitDialog = new AlertDialog.Builder(
                        this);
                quitDialog.setTitle("Вы действительно желаете отчистить базу данных?");

                quitDialog.setPositiveButton("Да", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(RazryadActivity.this, "Отчистка БД возможна только из главной формы приложения!", Toast.LENGTH_SHORT).show();
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
        double h;
        int razr, nD=0, flag, srazr;
        switch (v.getId()){
            case R.id.btnAddR:
                //проверка на заполнение полей
                if (edtHeightD.getText().toString().equals("") )
                    Toast.makeText(RazryadActivity.this, "Пожалуйста! заполните все поля.", Toast.LENGTH_SHORT).show();
                else{
                    h = Double.valueOf(edtHeightD.getText().toString());
                    razr = RastRazrH(Poroda, D, h);
                    if (razr==0){
                        Toast.makeText(RazryadActivity.this, "Расчёт не выполнен! " +
                                "Проверьте правильность заполнения полей!", Toast.LENGTH_SHORT).show();
                    }
                    else
                    {
                        m = new HashMap<String, Object>();
                        m.put(ATTRIBUTE_PORODA_TEXT, Poroda);
                        m.put(ATTRIBUTE_D_TEXT, String.valueOf(D));
                        m.put(ATTRIBUTE_H_TEXT, edtHeightD.getText().toString());
                        m.put(ATTRIBUTE_RAZR_TEXT, String.valueOf(razr));
                        //задание типа для пород 1-я добавленная основная, остальные доп.
                        if (data.size()==0) {
                            MainP = Poroda;
                        }
                        if (MainP.equals(Poroda)==true){
                            m.put(ATTRIBUTE_TYPE_TEXT, "осн.");
                        }
                        else{
                            m.put(ATTRIBUTE_TYPE_TEXT, "доп.");
                        }
                        //добавим его в коллекцию
                        data.add(m);
                        //уведомляем, что данные изменились
                        sAdapter.notifyDataSetChanged();
                        //группировка по породам и заполнеие списка кол-в измерений
                        flag = FindData(Poroda);
                        if (flag == 0) {
                            listAddPoroda.add(Poroda);
                            if (listAddPoroda.size()==1){
                                listOstIzm.add(kolMain);
                            }else
                            {
                                kolDel += kolDop;
                                listOstIzm.add(kolDop);
                            }
                        }
                        ostIzm(Poroda,1);   //функция для расчёта и отображения кол-в оставшихся измерений
                        if (kolDel == 0) btnCalcR.setEnabled(true);
                    }
                }
                break;
            case R.id.btnClearR:    //по кнопке отчистить таблицу
                AlertDialog.Builder quitDialog = new AlertDialog.Builder(
                        RazryadActivity.this);
                quitDialog.setTitle("Вы действительно желаете отчистить таблицу?");

                quitDialog.setPositiveButton("Да", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        data.clear();
                        sAdapter.notifyDataSetChanged();
                        Toast.makeText(RazryadActivity.this, "Таблица отчищена!", Toast.LENGTH_SHORT).show();
                        btnCalcR.setEnabled(false);
                        kolDel = kolMain;
                        listAddPoroda.clear();
                        listOstIzm.clear();
                        edtKolD.setText(String.valueOf(kolDel));
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
            case R.id.btnCalcR:
                String type;
                int numDel,videl, idVidel=0, idRazr=0;
                for (int i=0;i<listAddPoroda.size();i++){
                    srazr=calcSrazr(listAddPoroda.get(i));
                    if (MainP.equals(listAddPoroda.get(i))) type = "Основная";
                    else type = "Дополнительная";
                    numDel = Integer.valueOf(numd);
                    videl = Integer.valueOf(vid);
                    idVidel = myDbHelper.getIDVvidel(numDel,videl); //получаем id записи из таблицы выдел
                    idRazr = myDbHelper.getIdDirRazryad(listAddPoroda.get(i),srazr);    //получаем id записи из справочника Разрядов
                    //заполняем таблицу видовой состав делянки;
                    myDbHelper.addSostavDel(idVidel, type,idRazr);
                }
                vidID = String.valueOf(idVidel);
                drazrID = String.valueOf(idRazr);
                Toast.makeText(this, "Средний разряд расчитан!", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(this, OtvodActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                break;
        }
    }

    //расчёт среднего разряда высот
    public int calcSrazr(String poroda){
        String str, p="", r="",s;
        int n=0, raz=0, z=0, count=0, srazr=0;
        for (int i = 0;i<data.size();i++){
            str = data.get(i).toString();
            s = str.substring(1,str.length()-1);
            str = s;
            String[] words = str.split(", ");
            n=0;
            for(String word : words){
                if (word.substring(0,1).equals("p")) {
                    p = word.substring(7);
                }

                if (word.substring(0,1).equals("r")) {
                    r = word.substring(5);
                    z = Integer.valueOf(r);
                }
            }
            if (p.equals(poroda) == true) {
                raz += z;
                count++;
            }
        }
        if (count != 0) srazr =  raz/count;
        return srazr;
    }

    //функция для получения разряда высот из справочной таблицы разрядов
    private int RastRazrH(String poroda, Integer d, double h) {
        Double maxH, minH;
        int r=0;
        String p;
        c = myDbHelper.getDirectoryRazryad();
        if (c.moveToFirst()) {
            do {
                if (poroda.equals(c.getString(1)) && d == c.getInt(2)){
                    minH=c.getDouble(3);
                    maxH=c.getDouble(4);
                    if (minH<=h && h<=maxH){
                        r = c.getInt(5);
                        break;
                    }
                }
            } while (c.moveToNext());
        }
        return r;
    }

    //функция для получения породы из списка которую мы удалили
    public String getPoroda(int pos){
        String poroda="",str,s;
        str = data.get(pos).toString();
        s = str.substring(1,str.length()-1);
        str = s;
        String[] words = str.split(", ");
        for (String word : words) {
            if (word.substring(0,1).equals("p")) {
                poroda = word.substring(7);
                break;
            }
        }
        return poroda;
    }

    //группировка по добавленным породам
    public int FindData(String poroda){
        int f=0;
        String str, p="",s;
        for (int i = 0;i<data.size()-1;i++) {
            str = data.get(i).toString();
            s = str.substring(1,str.length()-1);
            str = s;
            String[] words = str.split(", ");
            for (String word : words) {
                if (word.substring(0,1).equals("p")){
                    p = word.substring(7);
                    if (p.equals(poroda)==true) {
                        f=1;
                    }
                    break;
                }
            }
            if (f==1) break;
        }
        return f;
    }

    //функция для определения остатка ввода количества деревьев
    private void ostIzm(String poroda, int r) {
        int k=0;
        if (r == 1){
            for (int i=0; i<listAddPoroda.size(); i++) {
                if (poroda == listAddPoroda.get(i)) {
                    k = listOstIzm.get(i);
                    if(k != 0){
                        k--;
                        listOstIzm.set(i, k);
                        kolDel--;
                        edtKolD.setText(String.valueOf(listOstIzm.get(i)));
                    }
                    if (k == 0)
                        Toast.makeText(this, "По " + listAddPoroda.get(i) + " данные заполнены!", Toast.LENGTH_SHORT).show();
                }
            }
        }
        if (r == 0) {
            for (int i = 0; i < listAddPoroda.size(); i++) {
                if (poroda.equals(listAddPoroda.get(i))) {
                    k = listOstIzm.get(i);
                    edtKolD.setText(String.valueOf(k));
                }
            }
        }
        if (kolDel <= 0) {
            kolDel=0;
            Toast.makeText(RazryadActivity.this, "Вы заполнили таблицу!", Toast.LENGTH_SHORT).show();
            btnCalcR.setEnabled(true);
            edtKolD.setText(String.valueOf(kolDel));
        }
    }

    //функцция создания контексного меню для списка
    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        menu.add(0, CM_DELETE_ID, 0, R.string.delete_record);
    }

    //функция считывающаю нажатие на элемент контексного меню
    public boolean onContextItemSelected(MenuItem item) {
        String poroda;
        int p=0;
        if (item.getItemId() == CM_DELETE_ID) {
            // получаем из пункта контекстного меню данные по пункту списка
            AdapterView.AdapterContextMenuInfo acmi = (AdapterView.AdapterContextMenuInfo) item
                    .getMenuInfo();
            //получаем инфу о пункте списка
            acmi = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
            //удаляем Map из коллекции, используя позицию пункта в списке
            poroda = getPoroda(acmi.position);
            data.remove(acmi.position);
            for(int i = 0; i<listAddPoroda.size(); i++){
                if (listAddPoroda.get(i).equals(poroda)==true){
                    p = listOstIzm.get(i);
                    p++;
                    kolDel++;
                    listOstIzm.set(i,p);
                    btnCalcR.setEnabled(false);
                    ostIzm(poroda,0);
                }
            }
            //уведомляем что данные изменились
            sAdapter.notifyDataSetChanged();
            return true;
        }
        return super.onContextItemSelected(item);
    }

    //функция для закрытия приложения
    public void openQuitDialog() {
        AlertDialog.Builder quitDialog = new AlertDialog.Builder(
                RazryadActivity.this);
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
    protected void onRestart() {
        super.onRestart();
        if (data.size()==0){
            getSettings();
            listAddPoroda.clear();
            listOstIzm.clear();
        }
    }


    //при закрытии активити закрываем все подключения
    @Override
    protected void onDestroy() {
        super.onDestroy();
        myDbHelper.close();
        finish();
    }
}