package com.linksrussia.rgk_worker.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.linksrussia.rgk_worker.App;
import com.linksrussia.rgk_worker.db.dao.MeasureDao;
import com.linksrussia.rgk_worker.db.entities.Measure;

import java.util.Date;
import java.util.List;
import java.util.function.Consumer;

public class DataReceiver extends BroadcastReceiver {
    public static final String INTENT_ACTION = "DataReceiver";
    public static final String DATA_EXTRA = "DATA_EXTRA";

    private final long currentSeriesId;
    private final Consumer<List<Measure>> renderCallback;

    final MeasureDao measureDao = App.Companion.getDB().measureDao();

    public DataReceiver(long currentSeriesId, Consumer<List<Measure>> renderCallback) {
        this.currentSeriesId = currentSeriesId;
        this.renderCallback = renderCallback;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Double distance = intent.getDoubleExtra(DATA_EXTRA, 0);

        Measure measure = new Measure();
        measure.setTimeMills(new Date().getTime());
        measure.setSeriesId(currentSeriesId);
        measure.setDistance(distance);
        measureDao.insertAll(measure);

        renderCallback.accept(measureDao.getBySeries(currentSeriesId));
    }
}
