package kevin.park.connectfour.ui;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import kevin.park.connectfour.R;

public class GameActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.connect_menu);
        Initialize();
    }

    public void Initialize(){

    }
}
