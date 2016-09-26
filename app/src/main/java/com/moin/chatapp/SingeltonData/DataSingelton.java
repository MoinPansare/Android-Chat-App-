package com.moin.chatapp.SingeltonData;


import android.graphics.Bitmap;


import java.util.ArrayList;

/**
 * Created by macpro on 3/28/16.
 */
public class DataSingelton {
    public static DataSingelton my_SingeltonData;



    public String UserNotificationToken;

    private DataSingelton() {

    }

    public static DataSingelton getMy_SingeltonData_Reference() {
        if (my_SingeltonData == null) {
            my_SingeltonData = new DataSingelton();
        }
        return my_SingeltonData;
    }
}
