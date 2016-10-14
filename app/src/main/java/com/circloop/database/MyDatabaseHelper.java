package com.circloop.database;


import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Created by 浩思于微 on 2016/5/12.
 */
public class MyDatabaseHelper extends SQLiteOpenHelper {
    final String CREATE_TABLE_GROUPINFO_SQL ="create table group_info(_id integer primary key autoincrement,group_name,group_desc,ip_num,isGroup)";
    final String CREATE_TABLE_DEVICEINFO_SQL ="create table device_info(_id integer primary key autoincrement,device_type,ip,oid)";
    final String CREATE_TABLE_IPSEG_SQL="create table ip_seg_info(_id integer primary key autoincrement,group_name,ip_begin,ip_end,enable)";//ip段表，包含四个属性
    final String CREATE_TABLE_DELETED_DEVICES_SQL = "create table deleted_devices_info(_id integer primary key autoincrement, group_name, ip_address)";//分组中删除的ip
    public MyDatabaseHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {

        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(CREATE_TABLE_GROUPINFO_SQL);
        sqLiteDatabase.execSQL(CREATE_TABLE_DEVICEINFO_SQL);
        sqLiteDatabase.execSQL(CREATE_TABLE_IPSEG_SQL);
        sqLiteDatabase.execSQL(CREATE_TABLE_DELETED_DEVICES_SQL);

    }
    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
    }
}
