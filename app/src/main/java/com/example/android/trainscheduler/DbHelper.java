package com.example.android.trainscheduler;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;
import android.util.Log;

/**
 * Created by toshiba pc on 2/3/2018.
 */

public class DbHelper extends SQLiteOpenHelper{
    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "Proif.db";

    public DbHelper(Context context){
        super(context,DATABASE_NAME,null,DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        Log.d("DBHelperTag","onCreate");
//        sqLiteDatabase.execSQL(deleteTable);
        sqLiteDatabase.execSQL(createTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        sqLiteDatabase.execSQL(deleteTable);
        onCreate(sqLiteDatabase);
    }

    public void onDownGrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion){
        onUpgrade(sqLiteDatabase,oldVersion,newVersion);
    }

    public static class DbEntry implements BaseColumns {
        public static final String TABLE_NAME = "stasiun";
        public static final String COLUMN_ID = "id";
        public static final String COLUMN_NAME = "nama";
        public static final String COLUMN_LATITUDE = "latitude";
        public static final String COLUMN_LONGTITUDE = "longtitude";
    }

    public static final String createTable =String.format(
            "CREATE TABLE %s(%s Integer,%s varchar(255), %s Double, %s Double, PRIMARY KEY(%s));",
            DbEntry.TABLE_NAME,DbEntry.COLUMN_ID, DbEntry.COLUMN_NAME, DbEntry.COLUMN_LATITUDE, DbEntry.COLUMN_LONGTITUDE, DbEntry.COLUMN_ID
    );
    public static final String deleteTable =String.format(
            "DROP TABLE IF EXISTS %s;",DbEntry.TABLE_NAME
    );
}
