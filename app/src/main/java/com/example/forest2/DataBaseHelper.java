package com.example.forest2;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import android.view.ContextMenu;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

public class DataBaseHelper extends SQLiteOpenHelper {

    String DB_PATH = null;
    private static String DB_NAME = "forest41";
    private static int DB_VERSION = 2;
    private SQLiteDatabase myDataBase;
    private final Context myContext;

    //названия таблиц базы данных
    public static final String DB_TABLE_INFDEL = "infdel";  //делянки
    public static final String DB_TABLE_VVIDEL = "vvidel";  //выдел
    public static final String DB_TABLE_SOSTAVDEL = "sostavDel";    //состав делянки
    public static final String DB_TABLE_OTVOD = "otvod";    //отвод деревьев под рубку
    public static final String DB_TABLE_DIRECTORY_WIDTH = "directoryWidth"; //справочник ступеней толщины
    public static final String DB_TABLE_DIRECTORY_V = "directoryV";     //справочник объёмов
    public static final String DB_TABLE_DIRECTORY_RAZRYAD = "directoryRazryad";   //справочник разрядов
    public static final String DB_TABLE_DIRECTORY_RAZRH = "directoryRazrH";     //справочник разрядов высот
    public static final String DB_TABLE_DIRECTORY_PORODA = "directoryPoroda";   //справочник пород

    //названия столбцов из таблицы Информация о делянках DB_TABLE_INFDEL
    public static final String INFDEL_ID = "_id";
    public static final String INFDEL_REGION = "region";
    public static final String INFDEL_DISTRICT = "district";
    public static final String INFDEL_LESHOZ = "leshoz";
    public static final String INFDEL_QVARTAL = "qvartal";
    public static final String INFDEL_SQUARE = "square";

    //названия столбцов из таблицы Выдел DB_TABLE_VVIDEL
    public static final String VVIDEL_ID = "_id";
    public static final String VVIDEL_VIDEL = "videl";
    public static final String VVIDEL_INFDELID = "infDelID";

    //названия столбцов из таблицы Состав делянки DB_TABLE_SOSTAVDEL
    public static final String SOSTAVDEL_ID= "_id";
    public static final String SOSTAVDEL_VVIDELID= "vvidelID";
    public static final String SOSTAVDEL_TYPEPORODA= "typePoroda";
    public static final String SOSTAVDEL_SETRAZRYADID = "setRazryadID";

    //названия столбцов из таблицы отвод DB_TABLE_OTVOD
    public static final String OTVOD_ID = "_id";
    public static final String OTVOD_SOSTAVDELID = "sostavDelID";
    public static final String OTVOD_VID = "vID";
    public static final String OTVOD_KATEGORY = "kategory";
    public static final String OTVOD_OBYOM = "obyom";

    //названия столбцов из таблицы справочник пород DB_TABLE_DIRECTORY_PORODA
    public static final String D_PORODA_ID = "_id";
    public static final String D_PORODA_PORODA = "poroda";

    //названия столбцов из таблицы справочник ст.толщины DB_TABLE_DIRECTORY_WIDTH
    public static final String D_WIDTH_ID = "_id";
    public static final String D_WIDTH_D = "d";

    //названия столбцов из таблицы справочник разрядов высот DB_TABLE_DIRECTORY_RAZRH
    public static final String D_RAZRH_ID = "_id";
    public static final String D_RAZRH_R = "r";

    //названия столбцов из справочника определния разрялов высот
    public static final String D_RAZR_ID = "_id";
    public static final String D_RAZR_PORODAID = "porodaID";
    public static final String D_RAZR_WIDTHID = "dID";
    public static final String D_RAZR_MINH = "minH";
    public static final String D_RAZR_MAXH = "maxH";
    public static final String D_RAZR_RAZRHID = "rID";

    //названия столбцов из справочника определения объёма
    public static final String D_V_ID = "_id";
    public static final String D_V_WIDTHID = "dID";
    public static final String D_V_RAZRYADID = "razryadID";
    public static final String D_V_VDEL = "vDel";
    public static final String D_V_VDR = "vDr";

    //поле для отображени итога по кубатуре
    public static final String SUM_OBYOM = "itog";
    //поле для отображения делянки
    public static final String MAIN_DEL = "del";
    //поле для отображения кол-ва деловой древесины
    public static final String KolDel = "kolDel";
    //поле для отображения кол-ва дровяной древесины
    public static final String KolDr = "kolDr";
    //поле для отображения объёма деловой древесины
    public static final String vDel = "sumDel";
    //поле для отображения объёма дровяной древесины
    public static final String vDr = "sumDr";

    public DataBaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
        this.myContext = context;
        this.DB_PATH = "/data/data/" + context.getPackageName() + "/" + "databases/";
        Log.e("Path 1", DB_PATH);
    }

    public void createDataBase() throws IOException {
        boolean dbExist = checkDataBase();
        if (dbExist) {
        } else {
            this.getWritableDatabase();
            this.close();
            try {
                copyDataBase();
            } catch (IOException e) {
                throw new Error("Error copying database");
            }
        }
    }

    private void copyDataBase() throws IOException {
        InputStream myInput = myContext.getAssets().open(DB_NAME);
        String outFileName = DB_PATH + DB_NAME;
        OutputStream myOutput = new FileOutputStream(outFileName);
        byte[] buffer = new byte[10];
        int length;
        while ((length = myInput.read(buffer)) > 0) {
            myOutput.write(buffer, 0, length);
        }
        myOutput.flush();
        myOutput.close();
        myInput.close();
    }

    private boolean checkDataBase() {
        SQLiteDatabase checkDB = null;
        try {
            String myPath = DB_PATH + DB_NAME;
            checkDB = SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.OPEN_READONLY);
        } catch (SQLiteException e) {
        }
        if (checkDB != null) {
            checkDB.close();
        }
        return checkDB != null ? true : false;
    }

    public void openDataBase() throws SQLException {
        String myPath = DB_PATH + DB_NAME;
        myDataBase = SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.OPEN_READWRITE);
    }

    @Override
    public synchronized void close() {
        if (myDataBase != null)
            myDataBase.close();
        super.close();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        System.out.println("old = "+oldVersion);
        System.out.println("new = "+newVersion);
        if (newVersion > oldVersion)
            try {
                copyDataBase();
            } catch (IOException e) {
                e.printStackTrace();
            }
    }

    //получить все данные из таблицы ифнормация о делянках DB_TABLE_INFDEL
    public Cursor getInfDelAll(){
        return myDataBase.query(DB_TABLE_INFDEL,null,null,null,null,null,null);
    }

    //добавить запись в таблицу инф. о делянке DB_TABLE_INFDEL
    public void addInfDel(String region, String district, String leshoz, int kvartal, double square){
        ContentValues cv = new ContentValues();
        cv.put(INFDEL_REGION, region);
        cv.put(INFDEL_DISTRICT, district);
        cv.put(INFDEL_LESHOZ, leshoz);
        cv.put(INFDEL_QVARTAL, kvartal);
        cv.put(INFDEL_SQUARE, square);
        myDataBase.insert(DB_TABLE_INFDEL,null,cv);
    }

    // удалить запись из таблиц
    public void delRec(String table, String col, long id) {
        myDataBase.delete(table, col + " = " + id, null);
    }

    //функция для удаления всех записей из таблица
    public void delAllData(String table){
        myDataBase.delete(table,null,null);
    }

    // удалить запись из таблицы отвод по id выдела
    public void delRecOtvod(int videlID) {
        Cursor c = null;
        String sql = "SELECT o._id FROM "+DB_TABLE_OTVOD+" AS o"+
                    " INNER JOIN "+DB_TABLE_SOSTAVDEL+" AS s ON o.sostavDelID = s._id"+
                    " WHERE s.vvidelID = "+videlID;
        c = myDataBase.rawQuery(sql,null);
        if (c.moveToFirst()){
            do {
                myDataBase.delete(DB_TABLE_OTVOD, OTVOD_ID+" = "+c.getInt(0),null);
            }while(c.moveToNext());
        }

    }

    public Cursor query(String table, String[] columns, String selection, String[] selectionArgs, String groupBy, String having, String orderBy) {
        return myDataBase.query(table, null, null, null, null, null, null);
    }

    //получение значений столбоцов из таблицыифнормации о делянках
    public ArrayList<String> getValueFromINFDEL(int v, String region, String district, String leshoz, String kvartal){
        ArrayList<String> list = new ArrayList<String>();
        SQLiteDatabase db = this.getReadableDatabase();
        db.beginTransaction();
        String val = "", select="";
        Cursor cursor;
        try{
            switch (v){
                case 0:
                    select = "SELECT * FROM "+DB_TABLE_INFDEL +
                            " WHERE "+INFDEL_QVARTAL+" = "+"'"+kvartal+"'";
                    break;
                case 1:
                    select = "SELECT * FROM "+DB_TABLE_INFDEL+" GROUP BY "+INFDEL_REGION;
                    break;
                case 2:
                    select = "SELECT * FROM "+DB_TABLE_INFDEL+" WHERE "+INFDEL_REGION+" = "+"'"+region+"'"+" GROUP BY "+INFDEL_DISTRICT;
                    break;
                case 3:
                    select = "SELECT * FROM "+DB_TABLE_INFDEL+" WHERE "+INFDEL_REGION+" = "+"'"+region+"' AND "+INFDEL_DISTRICT+" = "+"'"+district+"'"
                            +" GROUP BY "+INFDEL_LESHOZ;
                    break;
                case 4:
                    select = "SELECT * FROM "+DB_TABLE_INFDEL+" " +
                            "WHERE "+INFDEL_REGION+" = "+"'"+region+"' AND "+INFDEL_DISTRICT+" = "+"'"+district+"'"+ " AND "+
                            INFDEL_LESHOZ+" = "+"'"+leshoz+"'";
                           // +" GROUP BY "+INFDEL_QVARTAL;
                    break;
                case 5:
                    select = "SELECT * FROM "+DB_TABLE_INFDEL+" " +
                            "WHERE "+INFDEL_REGION+" = "+"'"+region+"' AND "+INFDEL_DISTRICT+" = "+"'"+district+"'"+ " AND "+
                            INFDEL_LESHOZ+" = "+"'"+leshoz+"'"+" AND "+INFDEL_QVARTAL+" = "+"'"+kvartal+"'";
                    break;
            }

            cursor = db.rawQuery(select,null);
            if(cursor.moveToFirst()){
                do {
                    val = cursor.getString(v);
                    list.add(val);
                }while(cursor.moveToNext());
            }
            db.setTransactionSuccessful();
        }catch (Exception e){
            e.printStackTrace();
        }
        finally {
            db.endTransaction();
        }
        return list;
    }

    //получене записей
    public Cursor getValueDel(int id){
        Cursor c;
        String sql = "SELECT inf.qvartal, inf._id, v.videl"+
                    " FROM "+DB_TABLE_VVIDEL+" AS v"+
                    " INNER JOIN "+DB_TABLE_INFDEL+" AS inf ON v.infDelID = inf._id"+
                    " WHERE v._id = "+id;
        c = myDataBase.rawQuery(sql,null);
        return c;
    }

    //отчистить все тфблицы
    public void delAll(){
        myDataBase.delete(DB_TABLE_OTVOD,null,null);
        myDataBase.delete(DB_TABLE_SOSTAVDEL,null,null);
        myDataBase.delete(DB_TABLE_VVIDEL,null,null);
    }

    //получение данных для формы отвод под рубку таблица DB_TABLE_OTVOD
    public Cursor getOtvod(int vidID){
        Cursor c;
        String sql = "SELECT o._id, p.poroda, d.d, o.kategory, h.r,"+
                    " CASE "+
                    " WHEN o.kategory = 'Деловая' THEN v.vDel"+
                    " WHEN o.kategory = 'Дровяная' THEN v.vDr"+
                    " ELSE null"+
                    " END as "+OTVOD_OBYOM+
                    " FROM "+DB_TABLE_OTVOD+" AS o"+
                    " INNER JOIN "+DB_TABLE_SOSTAVDEL+" AS s ON o.sostavDelID = s._id"+
                    " INNER JOIN "+DB_TABLE_DIRECTORY_RAZRYAD+" AS r ON s.setRazryadID = r._id"+
                    " INNER JOIN "+DB_TABLE_DIRECTORY_RAZRH+" AS h ON r.rID = h._id"+
                    " INNER JOIN "+DB_TABLE_DIRECTORY_PORODA+" AS p ON r.porodaID = p._id"+
                    " INNER JOIN "+DB_TABLE_DIRECTORY_V+" AS v ON o.vID = v._id"+
                    " INNER JOIN "+DB_TABLE_DIRECTORY_WIDTH+" AS d ON v.dID = d._id"+
                    " INNER JOIN "+DB_TABLE_VVIDEL+" AS vid ON s.vvidelID = vid._id"+
                    " WHERE s.vvidelID = "+vidID+
                    " ORDER BY o._id DESC";
        c = myDataBase.rawQuery(sql,null);
        return c;
    }

    //получение данных для главного окна (основного) авктивити с инф о: породе, квратлае,делянке, выделу, итогу(кубатура)
    public Cursor getDataMain(){
        Cursor c;
        String sql = "SELECT vid._id, inf.qvartal, inf._id as del, vid.videl, p.poroda, s.vvidelID,"+
                    " SUM(CASE"+
                    " WHEN o.kategory='Деловая' THEN v.vDel"+
                    " WHEN o.kategory='Дровяная' THEN v.vDr"+
                    " ELSE NULL"+
                    " END) AS itog"+
                    " FROM "+DB_TABLE_OTVOD+" AS o"+
                    " INNER JOIN "+DB_TABLE_SOSTAVDEL+" AS s ON o.sostavDelID = s._id"+
                    " INNER JOIN "+DB_TABLE_DIRECTORY_RAZRYAD+" AS r ON s.setRazryadID = r._id"+
                    " INNER JOIN "+DB_TABLE_DIRECTORY_RAZRH+" AS h ON r.rID = h._id"+
                    " INNER JOIN "+DB_TABLE_DIRECTORY_PORODA+" AS p ON r.porodaID = p._id"+
                    " INNER JOIN "+DB_TABLE_DIRECTORY_V+" AS v ON o.vID = v._id"+
                    " INNER JOIN "+DB_TABLE_DIRECTORY_WIDTH+" AS d ON v.dID = d._id"+
                    " INNER JOIN "+DB_TABLE_VVIDEL+" AS vid ON s.vvidelID = vid._id"+
                    " INNER JOIN "+DB_TABLE_INFDEL+" AS inf ON vid.infDelID = inf._id"+
                    " GROUP BY vid._id"+
                    " ORDER BY vid._id DESC";;
        c = myDataBase.rawQuery(sql, null);
        return c;
    }

    //получение данных для подробной информации
    public Cursor getInform(int vidID){
        Cursor c;
       String sql = "SELECT vid._id, inf.qvartal, inf._id, vid.videl, inf.region, inf.district, inf.leshoz, p.poroda, s.typePoroda,"+
                    " sum(CASE WHEN o.kategory = 'Деловая' THEN v.vDel ELSE NULL END) as sumDel,"+
                    " sum(CASE WHEN o.kategory = 'Дровяная' THEN v.vDr ELSE NULL END) as sumDr,"+
                    " count(CASE WHEN o.kategory = 'Деловая' THEN v.vDel ELSE NULL END) as kolDel,"+
                    " count(CASE WHEN o.kategory = 'Дровяная' THEN v.vDr ELSE NULL END) as kolDr,"+
                    " sum(CASE"+
                    " WHEN o.kategory = 'Деловая' THEN v.vDel"+
                    " WHEN o.kategory = 'Дровяная' THEN v.vDr"+
                    " ELSE NULL END) as itog"+
                    " FROM "+DB_TABLE_OTVOD+" as o"+
                    " INNER JOIN "+DB_TABLE_SOSTAVDEL+" as s ON o.sostavDelID = s._id"+
                    " INNER JOIN "+DB_TABLE_DIRECTORY_RAZRYAD+" as r ON s.setRazryadID = r._id"+
                    " INNER JOIN "+DB_TABLE_DIRECTORY_RAZRH+" as h ON r.rID = h._id"+
                    " INNER JOIN "+DB_TABLE_DIRECTORY_PORODA+" as p ON r.porodaID = p._id"+
                    " INNER JOIN "+DB_TABLE_DIRECTORY_V+" as v ON o.vID = v._id"+
                    " INNER JOIN "+DB_TABLE_DIRECTORY_WIDTH+" as d ON v.dID = d._id"+
                    " INNER JOIN "+DB_TABLE_VVIDEL+" as vid ON s.vvidelID = vid._id"+
                    " INNER JOIN "+DB_TABLE_INFDEL+" as inf ON vid.infDelID = inf._id"+
                    " WHERE vid._id = "+vidID+
                    " GROUP BY o.sostavDelID";
        c = myDataBase.rawQuery(sql,null);
        return c;
    }

    //получение данных для определения объёма
    public Cursor getDirV(){
        Cursor c;
        String sql = "SELECT v._id, p.poroda, d.d, h.r, v.vDel, v.vDr"+
                    " FROM "+DB_TABLE_DIRECTORY_V+" AS v"+
                    " INNER JOIN "+DB_TABLE_DIRECTORY_WIDTH+" AS d ON v.dID = d._id"+
                    " INNER JOIN "+DB_TABLE_DIRECTORY_RAZRYAD+" AS r ON v.razryadID = r._id"+
                    " INNER JOIN "+DB_TABLE_DIRECTORY_PORODA+" AS p ON r.porodaID = p._id"+
                    " INNER JOIN "+DB_TABLE_DIRECTORY_RAZRH+" AS h ON r.rID = h._id";
        c = myDataBase.rawQuery(sql,null);
        return c;
    }

    //получение id записи таблицы соствав делянки по выделу и породе
    public int getIDsostavDel(int vvidelID, String poroda){
        int idSostav;
        Cursor c;
        String sql="SELECT s._id, p.poroda"+
                    " FROM "+DB_TABLE_SOSTAVDEL+" AS s"+
                    " INNER JOIN "+DB_TABLE_DIRECTORY_RAZRYAD+" AS r ON s.setRazryadID = r._id"+
                    " INNER JOIN "+DB_TABLE_DIRECTORY_PORODA+" AS p ON r.porodaID = p._id"+
                    " WHERE s.vvidelID = "+vvidelID+" AND p.poroda = '"+poroda+"'";
        c = myDataBase.rawQuery(sql,null);
        c.moveToFirst();
        idSostav = c.getInt(0);
        return idSostav;
    }



    //получение породы из таблицы видовой соствав делянки DB_TABLE_SOSTAVDEL
    public ArrayList<String> getPorodaSostavDel(int v,int vvidelID,String poroda){
        ArrayList<String> list = new ArrayList<String>();
        String select="";
        SQLiteDatabase db = this.getReadableDatabase();
        db.beginTransaction();
        switch (v){
            case 2:
                select="SELECT s._id, v.infDelID, p.poroda, h.r"+
                        " FROM "+DB_TABLE_SOSTAVDEL+" AS s"+
                        " INNER JOIN "+DB_TABLE_VVIDEL+" AS v ON s.vvidelID = v._id"+
                        " INNER JOIN "+DB_TABLE_DIRECTORY_RAZRYAD+" AS d ON s.setRazryadID = d._id"+
                        " INNER JOIN "+DB_TABLE_DIRECTORY_PORODA+" AS p ON d.porodaID = p._id"+
                        " INNER JOIN "+DB_TABLE_DIRECTORY_RAZRH+" AS h ON d.rID = h._id"+
                        " WHERE s.vvidelID = "+vvidelID+
                        " GROUP BY p.poroda";
                break;
            case 3:
                select="SELECT s._id, v.infDelID, p.poroda, h.r"+
                        " FROM "+DB_TABLE_SOSTAVDEL+" AS s"+
                        " INNER JOIN "+DB_TABLE_VVIDEL+" AS v ON s.vvidelID = v._id"+
                        " INNER JOIN "+DB_TABLE_DIRECTORY_RAZRYAD+" AS d ON s.setRazryadID = d._id"+
                        " INNER JOIN "+DB_TABLE_DIRECTORY_PORODA+" AS p ON d.porodaID = p._id"+
                        " INNER JOIN "+DB_TABLE_DIRECTORY_RAZRH+" AS h ON d.rID = h._id"+
                        " WHERE s.vvidelID = "+vvidelID+" AND p.poroda = '"+poroda+"'";
                break;
            case 4:
                select="SELECT s._id, v.infDelID, p.poroda, h.r, s.typePoroda"+
                        " FROM "+DB_TABLE_SOSTAVDEL+" AS s"+
                        " INNER JOIN "+DB_TABLE_VVIDEL+" AS v ON s.vvidelID = v._id"+
                        " INNER JOIN "+DB_TABLE_DIRECTORY_RAZRYAD+" AS d ON s.setRazryadID = d._id"+
                        " INNER JOIN "+DB_TABLE_DIRECTORY_PORODA+" AS p ON d.porodaID = p._id"+
                        " INNER JOIN "+DB_TABLE_DIRECTORY_RAZRH+" AS h ON d.rID = h._id"+
                        " WHERE s.vvidelID = "+vvidelID+" AND p.poroda = '"+poroda+"'";
                break;
        }

        Cursor cursor;
        try{
            cursor = db.rawQuery(select,null);
            if(cursor.moveToFirst()){
                do {
                    list.add(cursor.getString(v));
                }while(cursor.moveToNext());
            }
            db.setTransactionSuccessful();
        }catch (Exception e){
            e.printStackTrace();
        }
        finally {
            db.endTransaction();
        }
        return list;
    }

    //получние ID записи о делянке
    public int getIdVvidel(String region, String district, String leshoz, String kvartal){
        Integer id;
        Cursor c;
        String sql = "SELECT * FROM "+DB_TABLE_INFDEL+
                " WHERE "+INFDEL_REGION+" = "+"'"+region+"' AND "+INFDEL_DISTRICT+" = "+"'"+district+"'"+ " AND "+
                INFDEL_LESHOZ+" = "+"'"+leshoz+"'"+" AND "+INFDEL_QVARTAL+" = "+"'"+kvartal+"'";
        c = myDataBase.rawQuery(sql,null);
        c.moveToFirst();
        id = c.getInt(0);
        c.close();
        return id;
    }

    //проверка на существование квартала в таблице инф. о делянка DB_TABLE_INFDEL
    public int getKvartalInfDel(int kvartal){
        int v=0;
        Cursor c = myDataBase.rawQuery("SELECT "+INFDEL_QVARTAL+" FROM "+DB_TABLE_INFDEL, null);
            if (c.moveToFirst()){
                do {
                    if (c.getInt(0) == kvartal){
                        v=1;
                        break;
                    }
                }while (c.moveToNext());
            }
        return v;
    }


    //получение данные выдела по делянке
    public Cursor getVvidel(int id){
        Cursor c;
        String sql = "SELECT v."+VVIDEL_ID+", d."+INFDEL_QVARTAL+", v."+VVIDEL_INFDELID+", v."+VVIDEL_VIDEL+
                    " FROM "+DB_TABLE_VVIDEL+" AS v"+
                    " INNER JOIN "+DB_TABLE_INFDEL+" AS d ON v."+VVIDEL_INFDELID+" = d._id"+
                    " WHERE v."+VVIDEL_INFDELID+" = "+id;
        c = myDataBase.rawQuery(sql,null);
        return c;
    }

    //получение ID Выдела
    public int getIDVvidel(int infDelID, int videl){
        int id=0;
        Cursor c;
        String sql = "SELECT * FROM "+DB_TABLE_VVIDEL+" WHERE "+VVIDEL_INFDELID+" = "+infDelID+" AND "+VVIDEL_VIDEL+" = "+videl;
        c = myDataBase.rawQuery(sql,null);
        c.moveToFirst();
        id = c.getInt(0);
        c.close();
        return id;
    }

    //добавить запись в таблицу выдел DB_TABLE_VVIDEL
    public void addRecVvidel(int infDelID, int videl)
    {
        ContentValues cv = new ContentValues();
        cv.put(VVIDEL_INFDELID, infDelID);
        cv.put(VVIDEL_VIDEL, videl);
        myDataBase.insert(DB_TABLE_VVIDEL,null,cv);
    }

    //поиск в таблице выделов на наличие введённого выдела
    public int findVidel(int id, int videl){
        String sql ="SELECT * FROM "+DB_TABLE_VVIDEL;
        Cursor c;
        int f = 0;
        c = myDataBase.rawQuery(sql,null);
        if(c.moveToFirst()){
            do{
                if (id == c.getInt(2) && videl==c.getInt(1)) {
                    f=1;
                    break;
                }
            }while (c.moveToNext());
        }
        return f;
    }

    //получение породы или ступненей толщины из справочников пород и ст.толщины
    public ArrayList<String> getValueSpR(String table, String col){
        ArrayList<String> list = new ArrayList<String>();
        SQLiteDatabase db = this.getReadableDatabase();
        db.beginTransaction();
        String select="SELECT "+col+" FROM "+table;
        Cursor cursor;
        try{
           cursor = db.rawQuery(select,null);
            if(cursor.moveToFirst()){
                do {
                    list.add(cursor.getString(0));
                }while(cursor.moveToNext());
            }
            db.setTransactionSuccessful();
        }catch (Exception e){
            e.printStackTrace();
        }
        finally {
            db.endTransaction();
        }
        return list;
    }

    //получение id записей из таблицы видовой состав делянки DB_TABLE_SOSTAVDEL по vvide_id
    public ArrayList<Integer> getIdSostav(long vid_id){
        Cursor c =null;
        ArrayList<Integer> list = new ArrayList<Integer>();
        String sql = "SELECT "+SOSTAVDEL_ID+ " FROM "+DB_TABLE_SOSTAVDEL+" WHERE "+SOSTAVDEL_VVIDELID+" = "+vid_id;
        c = myDataBase.rawQuery(sql,null);
        if (c.moveToFirst()){
            do {
                list.add(c.getInt(0));
            }while (c.moveToNext());
        }
        return list;
    }

    //получение справочной таблицы для определения разряда высот
    public Cursor getDirectoryRazryad(){
        Cursor c;
        String sql = "SELECT r._id, p.poroda, d.d, r.minH, r.maxH, h.r "+
                    " FROM "+DB_TABLE_DIRECTORY_RAZRYAD+" AS r"+
                    " INNER JOIN "+DB_TABLE_DIRECTORY_PORODA+" AS p ON r.porodaID = p._id"+
                    " INNER JOIN "+DB_TABLE_DIRECTORY_WIDTH+" AS d ON r.dID = d._id"+
                    " INNER JOIN "+DB_TABLE_DIRECTORY_RAZRH+" AS h ON r.rID = h._id";
        c = myDataBase.rawQuery(sql,null);
        return c;
    }

    //получение id записи из справочника разрядов DB_TABLE_DIRECTORY_RAZRYAD
    public int getIdDirRazryad(String poroda, int razr){
        Cursor c;
        int id;
        String sql = "SELECT d._id, p.poroda, h.r "+
                    " FROM "+DB_TABLE_DIRECTORY_RAZRYAD+" AS d"+
                    " INNER JOIN "+DB_TABLE_DIRECTORY_PORODA+" AS p ON d.porodaID = p._id"+
                    " INNER JOIN "+DB_TABLE_DIRECTORY_RAZRH+" AS h ON d.rID = h._id"+
                    " WHERE p.poroda = '"+poroda+"' AND h.r = "+razr;
        c = myDataBase.rawQuery(sql, null);
        c.moveToFirst();
        id = c.getInt(0);
        c.close();
        return id;
    }

    //добавление записи в таблицу видовой состав делянки DB_TABLE_SOSTAVDEL
    public void addSostavDel(int videlID, String type, int razryadID){
        ContentValues cv = new ContentValues();
        cv.put(SOSTAVDEL_VVIDELID, videlID);
        cv.put(SOSTAVDEL_TYPEPORODA, type);
        cv.put(SOSTAVDEL_SETRAZRYADID, razryadID);
        myDataBase.insert(DB_TABLE_SOSTAVDEL,null,cv);
    }

    //добавление даных в отвод DB_TABLE_OTVOD
    public void addOtvod(int sostavDelID, int vID, String kat){
        ContentValues cv = new ContentValues();
        cv.put(OTVOD_SOSTAVDELID,sostavDelID);
        cv.put(OTVOD_VID,vID);
        cv.put(OTVOD_KATEGORY,kat);
        myDataBase.insert(DB_TABLE_OTVOD, null,cv);
    }

    //получить кол-во записей в таблице инф. о делянке
    public int getCountInfDel(){
        Cursor c = null;
        int count = 0;
        c = myDataBase.rawQuery("SELECT COUNT(_id) FROM "+DB_TABLE_INFDEL,null);
        c.moveToFirst();
        count = c.getInt(0);
        c.close();
        return count;
    }

}