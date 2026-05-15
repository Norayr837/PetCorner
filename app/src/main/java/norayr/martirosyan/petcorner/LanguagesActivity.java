package norayr.martirosyan.petcorner;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;

public class LanguagesActivity extends AppCompatActivity {

    LinearLayout btnEnglish, btnRussian, btnArmenian;

    ImageView checkEnglish, checkRussian, checkArmenian;

    SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_languages);

        prefs = getSharedPreferences("settings", MODE_PRIVATE);

        btnEnglish = findViewById(R.id.btnEnglish);
        btnRussian = findViewById(R.id.btnRussian);
        btnArmenian = findViewById(R.id.btnArmenian);

        checkEnglish = findViewById(R.id.checkEnglish);
        checkRussian = findViewById(R.id.checkRussian);
        checkArmenian = findViewById(R.id.checkArmenian);

        // загрузка сохранённого языка
        String lang = prefs.getString("lang", "en");
        updateUI(lang);

        btnEnglish.setOnClickListener(v -> setLanguage("en"));
        btnRussian.setOnClickListener(v -> setLanguage("ru"));
        btnArmenian.setOnClickListener(v -> setLanguage("hy"));
    }

    private void setLanguage(String lang) {

        prefs.edit().putString("lang", lang).apply();

        LocaleHelper.setLocale(this, lang);

        updateUI(lang);
    }

    private void updateUI(String lang) {

        // сброс всех
        checkEnglish.setImageResource(R.drawable.radio_circle);
        checkRussian.setImageResource(R.drawable.radio_circle);
        checkArmenian.setImageResource(R.drawable.radio_circle);

        // установка выбранного
        switch (lang) {
            case "ru":
                checkRussian.setImageResource(R.drawable.radio_checked);
                break;

            case "hy":
                checkArmenian.setImageResource(R.drawable.radio_checked);
                break;

            default:
                checkEnglish.setImageResource(R.drawable.radio_checked);
                break;
        }
    }
}