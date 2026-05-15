package norayr.martirosyan.petcorner;

import android.content.Context;
import android.util.Log;

import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import java.util.concurrent.TimeUnit;

public class ReminderWorkerHelper {

    private static final long MIN_DELAY = 15_000; // 15 секунд минимум

    public static void setReminder(Context context, Note note) {

        long now = System.currentTimeMillis();

        long delay24h = note.timeMillis - now - (24 * 60 * 60 * 1000);
        long delay1h  = note.timeMillis - now - (60 * 60 * 1000);

        Log.d("REMINDER_DEBUG",
                "now=" + now +
                        " time=" + note.timeMillis +
                        " delay24h=" + delay24h +
                        " delay1h=" + delay1h);

        scheduleIfValid(context, note, delay24h, "24h");
        scheduleIfValid(context, note, delay1h, "1h");
    }

    private static void scheduleIfValid(Context context,
                                        Note note,
                                        long delay,
                                        String suffix) {

        if (delay < MIN_DELAY) {
            Log.d("REMINDER_DEBUG", "SKIP " + suffix + " (too small delay)");
            return;
        }

        Data data = new Data.Builder()
                .putString("title", note.title)
                .putString("text", note.text)
                .putString("noteId", note.id + "_" + suffix)
                .build();

        OneTimeWorkRequest request =
                new OneTimeWorkRequest.Builder(NotificationWorker.class)
                        .setInitialDelay(delay, TimeUnit.MILLISECONDS)
                        .setInputData(data)
                        .addTag(note.id + "_" + suffix)
                        .build();

        WorkManager.getInstance(context).enqueue(request);

        Log.d("REMINDER_DEBUG", "SCHEDULED " + suffix + " delay=" + delay);
    }
}