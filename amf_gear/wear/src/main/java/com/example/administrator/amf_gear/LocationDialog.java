package com.example.administrator.amf_gear;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;

/**
 * Created by Administrator on 2018-06-14.
 */

public class LocationDialog extends DialogFragment{
    public interface ConfirmDialogListener {
        void onDialogPositiveClick(DialogFragment dialog);
//        public void onDialogNegativeClick(DialogFragment dialog);
    }

    ConfirmDialogListener dialogListener;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            dialogListener = (ConfirmDialogListener) activity;
        }catch (ClassCastException e){
            throw new ClassCastException(activity.toString() + "must implement ConfirmDialogListener");
        }
    }

    @Override
    public Dialog onCreateDialog(final Bundle savedInstanceState) {
        /*FACTORY CLASS FOR DIALOGS*/
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Is this the correct location?")
                .setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialogListener.onDialogPositiveClick(LocationDialog.this);
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //builder.setOnCancelListener(dialog);
                        dialog.dismiss();
                    }
                });

        // Create the AlertDialog object and return it
        return builder.create();

    }
}
