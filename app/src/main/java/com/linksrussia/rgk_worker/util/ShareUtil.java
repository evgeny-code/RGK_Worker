package com.linksrussia.rgk_worker.util;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import androidx.core.content.FileProvider;

import com.linksrussia.rgk_worker.BuildConfig;
import com.linksrussia.rgk_worker.db.entities.Measure;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class ShareUtil {
    public static final SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy hh:mm:ss");

    public static void sendData(Activity activity, List<Measure> measures) {
        StringBuffer csvBuffer = new StringBuffer();

        csvBuffer.append("№;");
        csvBuffer.append("Дата и время;");
        csvBuffer.append("Расстояние (m)");
        csvBuffer.append('\n');

        int counter = 1;
        for (Measure measure : measures) {
            csvBuffer.append(counter++);
            csvBuffer.append(';');

            csvBuffer.append(dateFormat.format(new Date(measure.getTimeMills())));
            csvBuffer.append(';');

            csvBuffer.append(measure.getDistance());

            csvBuffer.append('\n');
        }


        File outputDir = activity.getCacheDir(); // context being the Activity pointer
        try {
            File outputFile = File.createTempFile("measure-data-", ".csv", outputDir);
            writeToFile(outputFile, csvBuffer.toString());

            Uri uri = FileProvider.getUriForFile(
                    activity,
                    BuildConfig.APPLICATION_ID,
                    outputFile);


            Intent share = new Intent();
            share.setAction(Intent.ACTION_SEND);
            share.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION & Intent.FLAG_ACTIVITY_NEW_TASK);
            share.setType("text/csv");
            share.putExtra(Intent.EXTRA_STREAM, uri);
            activity.startActivity(Intent.createChooser(share, "Отправить как файл"));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static void writeToFile(File outputFile, String data) {
        try {
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(new FileOutputStream(outputFile));
            outputStreamWriter.write("\ufeff");
            outputStreamWriter.write(data);
            outputStreamWriter.close();
        } catch (IOException e) {
            Log.e("Exception", "File write failed: " + e.toString());
        }
    }
}
