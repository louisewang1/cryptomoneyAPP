package com.example.cryptomoney.utils;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.widget.Toast;

public class DBHelper extends SQLiteOpenHelper {

    public static final String CREATE_BOOK = "create table Tokens("  // offline received, not sent to server
            + "id integer primary key autoincrement, "
            + "addr text, "
            + "amount real)";

    public static final String CREATE_TOKENS_ALL = "create table TokensAll("   //all token addrs seen so far
            + "id integer primary key autoincrement, "
            + "addr text, "
            + "amount real)";

    public static final String CREATE_PK_LIST = "create table PkList("
            + "id integer primary key autoincrement, "
            + "mer_name text, "
            + "N text, "
            + "pk_exp text)";

    public static final String CREATE_TOKEN_SK = "create table TokenSk("
            + "id integer primary key autoincrement, "
            + "addr text, "
            + "N text, "
            + "sk_exp text)";

//    public static final String CREATE_MERCHANTTOKEN_SK = "create table MerchantTokenSk("
//            + "id integer primary key autoincrement, "
//            + "ciphertext text, "
//            + "N text, "
//            + "sk_exp text)";

    private Context mContext;

    public DBHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
        mContext = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_BOOK);
        db.execSQL(CREATE_PK_LIST);
//        db.execSQL(CREATE_FREEMONEY_SK);
//        db.execSQL(CREATE_MERCHANTTOKEN_SK);
        db.execSQL(CREATE_TOKEN_SK);
        db.execSQL(CREATE_TOKENS_ALL);
//        Toast.makeText(mContext, "Create succeeded", Toast.LENGTH_SHORT).show();
       System.out.println("local db created");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("drop table if exists Tokens");
        db.execSQL("drop table if exists PkList");
        db.execSQL("drop table if exists TokenSk");
//        db.execSQL("drop table if exists FreeMoneySk");
//        db.execSQL("drop table if exists MerchantTokenSk");
        db.execSQL("drop table if exists TokensAll");
        onCreate(db);
    }
}

