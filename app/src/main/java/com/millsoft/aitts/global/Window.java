package com.millsoft.aitts.global;

import android.app.AlertDialog;
import android.content.Context;

public class Window {

    private Context context;

    public Window(Context context) {
        this.context = context;
    }


    public void MsgBox(int resId) {
        MsgBox(context.getString(resId));
    }

    /**
     * Show a message dialog
     *
     * @param message
     */
    public void MsgBox(String message) {
        AlertDialog.Builder dlgAlert = new AlertDialog.Builder(this.context);
        dlgAlert.setMessage(message);
        dlgAlert.setPositiveButton("OK", null);
        dlgAlert.setCancelable(true);
        dlgAlert.create().show();
    }

}
