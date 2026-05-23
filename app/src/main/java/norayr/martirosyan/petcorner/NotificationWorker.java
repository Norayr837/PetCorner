package norayr.martirosyan.petcorner;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

public class NotificationWorker extends Worker {

    public NotificationWorker(@NonNull Context context,
                              @NonNull WorkerParameters params) {
        super(context, params);
    }

    @NonNull
    @Override
    public Result doWork() {

        Log.d("WORKER_DEBUG", "Worker started");

        try {

            String title = getInputData().getString("title");
            String text = getInputData().getString("text");
            String noteId = getInputData().getString("noteId");


            if (title == null) title = "Reminder";
            if (text == null) text = "";
            if (noteId == null) noteId = "0";


            if (title.trim().isEmpty() && text.trim().isEmpty()) {
                Log.d("WORKER_DEBUG", "Empty notification data");
                return Result.failure();
            }

            NotificationHelper.showNotification(
                    getApplicationContext(),
                    title,
                    text,
                    noteId
            );

            Log.d("WORKER_DEBUG", "Notification shown successfully");

            return Result.success();

        } catch (Exception e) {

            Log.e("WORKER_DEBUG", "Worker crashed", e);

            return Result.retry();
        }
    }
}