
package com.example.missingpartsdetection.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import com.example.missingpartsdetection.entity.Device;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "device_database.db";
    private static final int DATABASE_VERSION = 1;

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE devices (id INTEGER PRIMARY KEY AUTOINCREMENT, deviceId TEXT, photoPath TEXT, photoPath_Checked TEXT)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS devices");
        onCreate(db);
    }

    public List<Device> getAlldevices() {
        List<Device> devices = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM devices", null);

        if (cursor.moveToFirst()) {
            do {
                int IdIndex = cursor.getColumnIndex("deviceId");
                int photoPathIndex = cursor.getColumnIndex("photoPath");
                if (IdIndex != -1 && photoPathIndex != -1) {
                    String Id = cursor.getString(IdIndex);
                    String photoPath = cursor.getString(photoPathIndex);
                    devices.add(new Device(Id, photoPath));
                } else {
                    Log.e("DatabaseError", "Column not found in devices table");
                }
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return devices;
    }

    public void addDevice(String Id, String photoPath) {
        SQLiteDatabase db = this.getWritableDatabase();
        try {
            ContentValues values = new ContentValues();
            values.put("deviceId", Id);
            values.put("photoPath", photoPath);

            long newRowId = db.insert("devices", null, values);
            if (newRowId != -1) {
                Log.d("DatabaseHelper", "Added worker with ID: " + newRowId);
            } else {
                Log.e("DatabaseError", "Error adding worker");
            }
        } catch (Exception e) {
            Log.e("DatabaseError", "Error adding worker: " + e.getMessage());
        } finally {
            db.close();
        }
    }

    public void deleteDevice(String deviceId) {
        SQLiteDatabase db = this.getWritableDatabase();
        try {
            db.delete("devices", "deviceId = ?", new String[]{deviceId});
            Log.d("DatabaseHelper", "Deleted worker with ID: " + deviceId);
        } catch (Exception e) {
            Log.e("DatabaseError", "Error deleting worker: " + e.getMessage());
        } finally {
            db.close();
        }
    }

    public Device getDeviceById(String Id) {
        SQLiteDatabase db = this.getReadableDatabase();
        Device device = null;
        Cursor cursor = null;
        Log.d("DatabaseQuery", "Querying for Id: " + Id);
        try {
            cursor = db.query("devices", null, "deviceId = ?", new String[]{String.valueOf(Id)}, null, null, null);
            Log.d("DatabaseQuery", "Number of rows returned: " + (cursor != null ? cursor.getCount() : 0));
            if (cursor != null && cursor.moveToFirst()) {
                int IdIndex = cursor.getColumnIndex("deviceId");
                int photoPathIndex = cursor.getColumnIndex("photoPath");

                if (IdIndex != -1 && photoPathIndex != -1) {
                    String photoPath = cursor.getString(photoPathIndex);
                    device = new Device(Id, photoPath);
                } else {
                    Log.e("DatabaseError", "Column not found in devices table");
                }
            }
        } catch (Exception e) {
            Log.e("DatabaseError", "Error fetching worker by ID: " + e.getMessage());
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            db.close();
        }
        return device;
    }

    public void updateDevice(String Id, String photoPath) {
        SQLiteDatabase db = this.getWritableDatabase();
        try {
            ContentValues values = new ContentValues();
            values.put("photoPath", photoPath);

            int updatedRows = db.update("devices", values, "Id = ?", new String[]{Id});
            if (updatedRows > 0) {
                Log.d("DatabaseHelper", "Updated worker with ID: " + Id);
            } else {
                Log.e("DatabaseError", "No worker found with ID: " + Id);
            }
        } catch (Exception e) {
            Log.e("DatabaseError", "Error updating worker: " + e.getMessage());
        } finally {
            db.close();
        }
    }


    public void updateCheckedImagePath(String deviceId, String checkedImage1Path) {
        SQLiteDatabase db = this.getWritableDatabase();
        try {
            ContentValues values = new ContentValues();
            values.put("photoPath_Checked", checkedImage1Path);

            // 更新指定 deviceId 的记录
            int updatedRows = db.update("devices", values, "deviceId = ?", new String[]{deviceId});
            if (updatedRows > 0) {
                Log.d("DatabaseHelper", "Updated checked image paths for worker with ID: " + deviceId);
            } else {
                Log.e("DatabaseError", "No worker found with ID: " + deviceId);
            }
        } catch (Exception e) {
            Log.e("DatabaseError", "Error updating checked image paths: " + e.getMessage());
        } finally {
            db.close();
        }
    }

}