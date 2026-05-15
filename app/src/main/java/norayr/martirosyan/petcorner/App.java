package norayr.martirosyan.petcorner;

import android.app.Application;

import com.cloudinary.android.MediaManager;

import java.util.HashMap;
import java.util.Map;

public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        try {
            Map<String, Object> config = new HashMap<>();

            config.put("cloud_name", "ddxfm3g6f");
            config.put("api_key", "893543371214758");
            config.put("api_secret", "UNsi2hbCpXOL9Gd6MryycJYC10L");

            MediaManager.init(this, config);

        } catch (IllegalStateException e) {
            // уже инициализирован → просто игнорируем
        }
    }
}



