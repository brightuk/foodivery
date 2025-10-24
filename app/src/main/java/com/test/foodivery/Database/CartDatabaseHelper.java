package com.test.foodivery.Database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.test.foodivery.Model.CartModel;

import java.util.ArrayList;
import java.util.List;

public class CartDatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "CartDatabase.db";
    private static final int DATABASE_VERSION = 3;
    private static final String TABLE_NAME = "cart_items";

    // Column names
    private static final String COL_ID = "id";
    private static final String COL_PRODUCT_ID = "product_id";
    private static final String COL_PRODUCT_NAME = "product_name";
    private static final String COL_PRODUCT_PRICE = "product_price";
    private static final String COL_PRODUCT_MESSAGE = "product_message";
    private static final String COL_PRODUCT_OPTIONS = "product_options";
    private static final String COL_PRODUCT_IMAGE = "product_image";
    private static final String COL_PRODUCT_GST = "product_gst";
    private static final String COL_GST_INC = "gst_Inc";
    private static final String COL_GST_SHOP_ID = "gst_ShopId";
    private static final String COL_ALL_VARIANT_IDS = "get_allVariantIds";
    private static final String COL_PRODUCT_WEIGHT = "get_Product_Weight";
    private static final String COL_QUANTITY = "quantity";

    public CartDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createTableQuery = "CREATE TABLE " + TABLE_NAME + " (" +
                COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_PRODUCT_ID + " TEXT NOT NULL, " +
                COL_PRODUCT_NAME + " TEXT, " +
                COL_PRODUCT_PRICE + " TEXT, " +
                COL_PRODUCT_MESSAGE + " TEXT, " +
                COL_PRODUCT_OPTIONS + " TEXT, " +
                COL_PRODUCT_IMAGE + " TEXT, " +
                COL_PRODUCT_GST + " TEXT, " +
                COL_GST_INC + " TEXT, " +
                COL_GST_SHOP_ID + " TEXT, " +
                COL_ALL_VARIANT_IDS + " TEXT, " +
                COL_PRODUCT_WEIGHT + " TEXT, " +
                COL_QUANTITY + " INTEGER DEFAULT 1, " +
                "UNIQUE(" + COL_PRODUCT_ID + ") ON CONFLICT REPLACE)";

        try {
            db.execSQL(createTableQuery);
        } catch (Exception e) {
            Log.e("DB_CREATE", "Error creating table", e);
        }
    }

    public int getCartItemCount() {
        int count = 0;
        SQLiteDatabase db = null;
        Cursor cursor = null;
        try {
            db = this.getReadableDatabase();
            cursor = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_NAME, null);
            if (cursor != null && cursor.moveToFirst()) {
                count = cursor.getInt(0);
            }
        } catch (Exception e) {
            Log.e("DB_COUNT", "Error counting cart items", e);
        } finally {
            if (cursor != null) {
                try {
                    cursor.close();
                } catch (Exception e) {
                    Log.e("DB_CLOSE", "Error closing cursor", e);
                }
            }
            if (db != null) {
                try {
                    db.close();
                } catch (Exception e) {
                    Log.e("DB_CLOSE", "Error closing database", e);
                }
            }
        }
        return count;
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        try {
            if (oldVersion < 2) {
                db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
                onCreate(db);
            } else if (oldVersion < 3) {
                // Safe column addition
                try {
                    Cursor cursor = db.rawQuery("PRAGMA table_info(" + TABLE_NAME + ")", null);
                    boolean columnExists = false;
                    if (cursor != null) {
                        while (cursor.moveToNext()) {
                            String columnName = cursor.getString(cursor.getColumnIndexOrThrow("name"));
                            if (COL_QUANTITY.equals(columnName)) {
                                columnExists = true;
                                break;
                            }
                        }
                        cursor.close();
                    }

                    if (!columnExists) {
                        db.execSQL("ALTER TABLE " + TABLE_NAME + " ADD COLUMN " + COL_QUANTITY + " INTEGER DEFAULT 1");
                    }
                } catch (Exception e) {
                    Log.w("DB_UPGRADE", "Error checking/adding quantity column", e);
                }
            }
        } catch (Exception e) {
            Log.e("DB_UPGRADE", "Error during database upgrade", e);
            // Recreate table if upgrade fails
            try {
                db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
                onCreate(db);
            } catch (Exception ex) {
                Log.e("DB_RECREATE", "Error recreating table", ex);
            }
        }
    }

    public boolean insertCartItem(String productId, String productName, String price,
                                  String productMessage, String selectedOptions, String productImage, String productGst,
                                  String gstInc, String shopId, String allVariantIds, String productWeight) {
        SQLiteDatabase db = null;
        try {
            db = this.getWritableDatabase();

            // 1. Delete all cart items from other shops
            try {
                db.delete(TABLE_NAME, COL_GST_SHOP_ID + "!=?", new String[]{shopId});
            } catch (Exception e) {
                Log.e("DB_DELETE", "Error deleting items from other shops", e);
            }

            // 2. Insert new cart item
            ContentValues values = new ContentValues();
            values.put(COL_PRODUCT_ID, productId);
            values.put(COL_PRODUCT_NAME, productName);
            values.put(COL_PRODUCT_PRICE, price);
            values.put(COL_PRODUCT_MESSAGE, productMessage);
            values.put(COL_PRODUCT_OPTIONS, selectedOptions);
            values.put(COL_PRODUCT_IMAGE, productImage);
            values.put(COL_PRODUCT_GST, productGst);
            values.put(COL_GST_INC, gstInc);
            values.put(COL_GST_SHOP_ID, shopId);
            values.put(COL_ALL_VARIANT_IDS, allVariantIds);
            values.put(COL_PRODUCT_WEIGHT, productWeight);

            int existingQty = getQuantityForProductIdInternal(db, productId);
            values.put(COL_QUANTITY, existingQty > 0 ? existingQty : 1);

            long result = db.insertWithOnConflict(TABLE_NAME, null, values, SQLiteDatabase.CONFLICT_REPLACE);
            return result != -1;
        } catch (Exception e) {
            Log.e("DB_INSERT", "Error inserting cart item", e);
            return false;
        } finally {
            if (db != null) {
                try {
                    db.close();
                } catch (Exception e) {
                    Log.e("DB_CLOSE", "Error closing database", e);
                }
            }
        }
    }

    public boolean addOrIncreaseCartItem(String productId, String productName, String price,
                                         String productMessage, String selectedOptions, String productImage, String productGst,
                                         String gstInc, String shopId, String allVariantIds, String productWeight) {
        SQLiteDatabase db = null;
        try {
            db = this.getWritableDatabase();

            // 1. Delete all cart items from other shops
            try {
                db.delete(TABLE_NAME, COL_GST_SHOP_ID + "!=?", new String[]{shopId});
            } catch (Exception e) {
                Log.e("DB_DELETE", "Error deleting items from other shops", e);
            }

            // 2. Insert or update with incremented quantity
            ContentValues values = new ContentValues();
            values.put(COL_PRODUCT_ID, productId);
            values.put(COL_PRODUCT_NAME, productName);
            values.put(COL_PRODUCT_PRICE, price);
            values.put(COL_PRODUCT_MESSAGE, productMessage);
            values.put(COL_PRODUCT_OPTIONS, selectedOptions);
            values.put(COL_PRODUCT_IMAGE, productImage);
            values.put(COL_PRODUCT_GST, productGst);
            values.put(COL_GST_INC, gstInc);
            values.put(COL_GST_SHOP_ID, shopId);
            values.put(COL_ALL_VARIANT_IDS, allVariantIds);
            values.put(COL_PRODUCT_WEIGHT, productWeight);

            int existingQty = getQuantityForProductIdInternal(db, productId);
            int newQty = existingQty > 0 ? existingQty + 1 : 1;
            values.put(COL_QUANTITY, newQty);

            long result = db.insertWithOnConflict(TABLE_NAME, null, values, SQLiteDatabase.CONFLICT_REPLACE);
            return result != -1;
        } catch (Exception e) {
            Log.e("DB_INSERT", "Error inserting/incrementing cart item", e);
            return false;
        } finally {
            if (db != null) {
                try {
                    db.close();
                } catch (Exception e) {
                    Log.e("DB_CLOSE", "Error closing database", e);
                }
            }
        }
    }

    public List<CartModel> getAllCartItems() {
        List<CartModel> cartList = new ArrayList<>();
        SQLiteDatabase db = null;
        Cursor cursor = null;

        try {
            db = this.getReadableDatabase();
            cursor = db.query(TABLE_NAME, null, null, null, null, null, null);

            if (cursor != null && cursor.moveToFirst()) {
                do {
                    try {
                        CartModel item = new CartModel(
                                getStringColumnSafe(cursor, COL_PRODUCT_ID, ""),
                                getStringColumnSafe(cursor, COL_PRODUCT_NAME, ""),
                                getStringColumnSafe(cursor, COL_PRODUCT_PRICE, "0"),
                                getStringColumnSafe(cursor, COL_PRODUCT_MESSAGE, ""),
                                getStringColumnSafe(cursor, COL_PRODUCT_OPTIONS, ""),
                                getStringColumnSafe(cursor, COL_PRODUCT_IMAGE, ""),
                                getStringColumnSafe(cursor, COL_PRODUCT_GST, "0"),
                                getStringColumnSafe(cursor, COL_GST_INC, "0"),
                                getStringColumnSafe(cursor, COL_GST_SHOP_ID, ""),
                                getStringColumnSafe(cursor, COL_ALL_VARIANT_IDS, ""),
                                getStringColumnSafe(cursor, COL_PRODUCT_WEIGHT, "0"),
                                getIntColumnSafe(cursor, COL_QUANTITY, 1)
                        );
                        cartList.add(item);
                    } catch (Exception e) {
                        Log.e("DB_PARSE", "Error parsing cart item", e);
                    }
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e("DB_QUERY", "Error getting cart items", e);
        } finally {
            if (cursor != null) {
                try {
                    cursor.close();
                } catch (Exception e) {
                    Log.e("DB_CLOSE", "Error closing cursor", e);
                }
            }
            if (db != null) {
                try {
                    db.close();
                } catch (Exception e) {
                    Log.e("DB_CLOSE", "Error closing database", e);
                }
            }
        }
        return cartList;
    }

    public boolean deleteItem(String productId) {
        SQLiteDatabase db = null;
        try {
            db = this.getWritableDatabase();
            int result = db.delete(TABLE_NAME, COL_PRODUCT_ID + " = ?", new String[]{productId});
            return result > 0;
        } catch (Exception e) {
            Log.e("DB_DELETE", "Error deleting item", e);
            return false;
        } finally {
            if (db != null) {
                try {
                    db.close();
                } catch (Exception e) {
                    Log.e("DB_CLOSE", "Error closing database", e);
                }
            }
        }
    }

    public String getCurrentCartShopId() {
        SQLiteDatabase db = null;
        Cursor cursor = null;
        String shopId = null;
        try {
            db = this.getReadableDatabase();
            cursor = db.rawQuery("SELECT " + COL_GST_SHOP_ID + " FROM " + TABLE_NAME + " LIMIT 1", null);
            if (cursor != null && cursor.moveToFirst()) {
                shopId = cursor.getString(0);
            }
        } catch (Exception e) {
            Log.e("DB_QUERY", "Error getting shop ID", e);
        } finally {
            if (cursor != null) {
                try {
                    cursor.close();
                } catch (Exception e) {
                    Log.e("DB_CLOSE", "Error closing cursor", e);
                }
            }
            if (db != null) {
                try {
                    db.close();
                } catch (Exception e) {
                    Log.e("DB_CLOSE", "Error closing database", e);
                }
            }
        }
        return shopId;
    }

    public void clearCart() {
        SQLiteDatabase db = null;
        try {
            db = this.getWritableDatabase();
            db.delete(TABLE_NAME, null, null);
        } catch (Exception e) {
            Log.e("DB_CLEAR", "Error clearing cart", e);
        } finally {
            if (db != null) {
                try {
                    db.close();
                } catch (Exception e) {
                    Log.e("DB_CLOSE", "Error closing database", e);
                }
            }
        }
    }

    public boolean doesDatabaseExist(Context context) {
        SQLiteDatabase db = null;
        try {
            db = SQLiteDatabase.openDatabase(context.getDatabasePath(DATABASE_NAME).getPath(),
                    null, SQLiteDatabase.OPEN_READONLY);
            return db != null;
        } catch (Exception e) {
            return false;
        } finally {
            if (db != null) {
                try {
                    db.close();
                } catch (Exception e) {
                    Log.e("DB_CLOSE", "Error closing database", e);
                }
            }
        }
    }

    private int getQuantityForProductIdInternal(SQLiteDatabase db, String productId) {
        int qty = 0;
        Cursor c = null;
        try {
            c = db.query(TABLE_NAME, new String[]{COL_QUANTITY}, COL_PRODUCT_ID + "=?", new String[]{productId}, null, null, null);
            if (c != null && c.moveToFirst()) {
                qty = c.getInt(0);
            }
        } catch (Exception e) {
            Log.e("DB_QUERY", "Error getting quantity", e);
        } finally {
            if (c != null) {
                try {
                    c.close();
                } catch (Exception e) {
                    Log.e("DB_CLOSE", "Error closing cursor", e);
                }
            }
        }
        return qty;
    }

    private int getIntColumnSafe(Cursor cursor, String columnName, int defaultValue) {
        try {
            int index = cursor.getColumnIndex(columnName);
            if (index >= 0) {
                return cursor.getInt(index);
            }
        } catch (Exception e) {
            Log.e("DB_COLUMN", "Error getting int column: " + columnName, e);
        }
        return defaultValue;
    }

    private String getStringColumnSafe(Cursor cursor, String columnName, String defaultValue) {
        try {
            int index = cursor.getColumnIndex(columnName);
            if (index >= 0) {
                String value = cursor.getString(index);
                return value != null ? value : defaultValue;
            }
        } catch (Exception e) {
            Log.e("DB_COLUMN", "Error getting string column: " + columnName, e);
        }
        return defaultValue;
    }

    public boolean updateQuantity(String productId, int newQuantity) {
        if (newQuantity < 1) {
            return deleteItem(productId);
        }
        SQLiteDatabase db = null;
        try {
            db = this.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put(COL_QUANTITY, newQuantity);
            int updated = db.update(TABLE_NAME, values, COL_PRODUCT_ID + "=?", new String[]{productId});
            return updated > 0;
        } catch (Exception e) {
            Log.e("DB_UPDATE", "Error updating quantity", e);
            return false;
        } finally {
            if (db != null) {
                try {
                    db.close();
                } catch (Exception e) {
                    Log.e("DB_CLOSE", "Error closing database", e);
                }
            }
        }
    }
}