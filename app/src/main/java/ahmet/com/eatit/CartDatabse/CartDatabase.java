package ahmet.com.eatit.CartDatabse;


import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(version = 1, entities = Cart.class, exportSchema = false)
public abstract class CartDatabase extends RoomDatabase {

    public abstract CartDAO cartDAO();

    private static CartDatabase instance;

    public static CartDatabase getInstance(Context mContext){
        if (instance == null)
            instance = Room
                    .databaseBuilder(mContext, CartDatabase.class, "CartEatDB1")
                    .allowMainThreadQueries()
                    .build();
        return instance;
    }

}
