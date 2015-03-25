package com.benpaoba.freerun;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.benpaoba.freerun.database.FreeRunDatabaseHelper;

public class DistanceInfoDao {
	private SQLiteOpenHelper mOpenHelper = null;
    private SQLiteDatabase db;

    public DistanceInfoDao(Context context) {
    	mOpenHelper = new FreeRunDatabaseHelper(context);
    }

    public void insert(DistanceInfo mDistanceInfo) {
        if (mDistanceInfo == null) {
            return;
        }
        /**
        db = mOpenHelper.getWritableDatabase();
        String sql = "INSERT INTO run_records(distance,longitude,latitude, usedTime) VALUES('"+ 
        mDistanceInfo.getDistance() +
        "','"+ mDistanceInfo.getLongitude() +
        "','"+ mDistanceInfo.getLatitude() +
        "','"+ mDistanceInfo.getTime() +
        "')";
        db.execSQL(sql);
        db.close();*/
    }

    public int getMaxId() {
        db = mOpenHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT MAX(_id) as id from run_records",null);
        if (cursor.moveToFirst()) {
            return cursor.getInt(cursor.getColumnIndex("id"));
        }
        return -1;
    }

    /**
     * 添加数据
     * @param orderDealInfo
     * @return
     */
    public synchronized int insertAndGet(DistanceInfo mDistanceInfo) {
        int result = -1;
        insert(mDistanceInfo);
        result = getMaxId();
        return result;
    }

    /**
     * 根据id获取
     * @param id
     * @return
     */
    public DistanceInfo getById(int id) {
        db = mOpenHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * from run_records WHERE _id = ?",new String[] { String.valueOf(id) });
        DistanceInfo mDistanceInfo = null;
        if (cursor.moveToFirst()) {
            mDistanceInfo = new DistanceInfo();
            mDistanceInfo.setId(cursor.getInt(cursor.getColumnIndex("id")));
            mDistanceInfo.setDistance(cursor.getFloat(cursor.getColumnIndex("distance")));
            mDistanceInfo.setLongitude(cursor.getFloat(cursor.getColumnIndex("longitude")));
            mDistanceInfo.setLatitude(cursor.getFloat(cursor.getColumnIndex("latitude")));
            mDistanceInfo.setTime(cursor.getInt(cursor.getColumnIndex("usedTime")));
        }
        cursor.close();
        db.close();
        return mDistanceInfo;
    }

    /**
     * 更新距离
     * @param orderDealInfo
     */
    public void updateDistance(DistanceInfo mDistanceInfo) {
        if (mDistanceInfo == null) {
            return;
        }
        db = mOpenHelper.getWritableDatabase();
        String sql = "update run_records set distance=" + mDistanceInfo.getDistance() +
        		",longitude = "+mDistanceInfo.getLongitude() + 
        		",latitude = "+mDistanceInfo.getLatitude() +
        		", usedTime = " + mDistanceInfo.getTime() + 
        		" where id = "+ mDistanceInfo.getId();
        db.execSQL(sql);
        db.close();
    }


    /**
     * 删除对应记录
     * @param id
     */
    public void delete(int id) {
        if (id == 0 || id < 0) {
            return;
        }
        db = mOpenHelper.getWritableDatabase();
        String sql = "delete from run_records where id = " + id;
        db.execSQL(sql);
        db.close();
    }
}
