package com.amiccom.adv;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;


/**
 * Created by tclin on 2014/11/20.
 */
public class ErrorDialog extends DialogFragment {
    public static final String TAG = ErrorDialog.class.getSimpleName();

    private onClickListener listener;
    private onOneClickListener oneClickListener;
    private int message;
    private boolean onlyMessage = false;
    private boolean settingButtonTextflag = false;

    private void setOnClickListener(onClickListener listener) {
        this.listener = listener;
    }

    private void setOnOneClickListener(onOneClickListener listener) {
        oneClickListener = listener;
    }

    private void setOnlyMessage () {
        onlyMessage = true;
    }

    public static ErrorDialog newInstance(onClickListener listener, int message, boolean cancelable) {
        final ErrorDialog dialog = new ErrorDialog();
        dialog.message = message;
        dialog.setCancelable(cancelable);
        dialog.setOnClickListener(listener);
        return dialog;
    }

    public static ErrorDialog newInstance(onOneClickListener listener, int message, boolean cancelable) {
        final ErrorDialog dialog = new ErrorDialog();
        dialog.message = message;
        dialog.setCancelable(cancelable);
        dialog.setOnOneClickListener(listener);
        return dialog;
    }

    public static ErrorDialog newInstance(onOneClickListener listener, int message, boolean cancelable, boolean settingButtonText) {
        final ErrorDialog dialog = new ErrorDialog();
        if(settingButtonText)
            dialog.settingButtonTextflag = true;
        dialog.message = message;
        dialog.setCancelable(cancelable);
        dialog.setOnOneClickListener(listener);
        return dialog;
    }

    public static ErrorDialog newInstance(int message, boolean cancelable) {
        final ErrorDialog dialog = new ErrorDialog();
        dialog.message = message;
        dialog.setCancelable(cancelable);
        dialog.setOnlyMessage();
        return dialog;
    }
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        if(!onlyMessage) {
            if (listener != null) {
                return new AlertDialog.Builder(getActivity())
                        .setTitle(null)
                        .setMessage(message)
                        .setPositiveButton(R.string.dialog_ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                listener.onOK();
                            }
                        })
                        .setNegativeButton(R.string.dialog_cancel, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                listener.onCancel();
                            }
                        })
                        .create();
            } else {
                if(settingButtonTextflag) {
                    return new AlertDialog.Builder(getActivity())
                            .setTitle(null)
                            .setMessage(message)
                            .setPositiveButton(R.string.dialog_cancel, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    oneClickListener.onDismiss();
                                }
                            })
                            .create();
                }
                else {
                    return new AlertDialog.Builder(getActivity())
                            .setTitle(null)
                            .setMessage(message)
                            .setPositiveButton(R.string.dialog_ok, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    oneClickListener.onDismiss();
                                }
                            })
                            .create();
                }
            }
        }
        else {
            return new AlertDialog.Builder(getActivity())
                    .setTitle(null)
                    .setMessage(message)
                    .create();
        }
    }

    public interface onClickListener {
        void onOK();

        void onCancel();
    }

    public interface onOneClickListener {
        void onDismiss();

    }
}