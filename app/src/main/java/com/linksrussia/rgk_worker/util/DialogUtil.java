package com.linksrussia.rgk_worker.util;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import com.linksrussia.rgk_worker.App;
import com.linksrussia.rgk_worker.R;
import com.linksrussia.rgk_worker.activities.MeasurementActivity;
import com.linksrussia.rgk_worker.activities.SelectDeviceActivity;
import com.linksrussia.rgk_worker.activities.SeriesActivity;
import com.linksrussia.rgk_worker.db.dao.SeriesDao;
import com.linksrussia.rgk_worker.db.entities.Series;

public class DialogUtil {

    public Dialog infoDialog(Activity activity, String msg) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setMessage(msg).setPositiveButton(R.string.ok, (dialog, id) -> dialog.dismiss());
        return builder.create();
    }

    public Dialog onCreateNoDeviceForkDialog(MeasurementActivity activity) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setMessage(R.string.noDeviceConnected)
                .setPositiveButton(R.string.selectDeviceBtnText, (dialog, id) -> activity.startActivity(new Intent(activity, SelectDeviceActivity.class)))
                .setNegativeButton(R.string.noSelectDeviceBtnText, (dialog, id) -> dialog.dismiss());

        return builder.create();
    }

    public Dialog onCreateAddSeriesDialog(SeriesActivity activity) {
        final SeriesDao seriesDao = App.Companion.getDB().seriesDao();

        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        // Get the layout inflater
        LayoutInflater inflater = activity.getLayoutInflater();

        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        View dialogContent = inflater.inflate(R.layout.dialog_series_add, null);
        builder.setView(dialogContent)
                // Add action buttons
                .setPositiveButton(R.string.add, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        EditText seriesNameEditText = dialogContent.findViewById(R.id.seriesNameEditText);
                        String text = seriesNameEditText.getText().toString();
                        if (!text.trim().isEmpty()) {
                            seriesDao.insertAll(new Series(text));
                            activity.renderData();
                        }
                        seriesNameEditText.setText("");
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        EditText seriesNameEditText = dialogContent.findViewById(R.id.seriesNameEditText);
                        seriesNameEditText.setText("");
                        dialog.dismiss();
                    }
                });

        return builder.create();
    }
}
