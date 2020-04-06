package com.example.sendbird;

import android.app.Activity;
import android.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

public class ProgressDialog {

    private static AlertDialog dialog;

    public static void startProgressDialog(Activity myActivity, String message){
        AlertDialog.Builder builder = new AlertDialog.Builder(myActivity);

        LayoutInflater inflater = myActivity.getLayoutInflater();
        View v = inflater.inflate(R.layout.progress_dialog, null);
        TextView tv_message = v.findViewById(R.id.tv_loading_message);
        tv_message.setText(message);

        builder.setView(v);
        builder.setCancelable(false);
        dialog =builder.create();

        dialog.show();
    }
    public static void dismissProgressDialog(){
        dialog.dismiss();
    }
}
