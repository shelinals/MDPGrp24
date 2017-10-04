package mdpgrp24.ntu.com.acrm;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

public class MDFActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mdf);
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        String mdfString1 = extras.getString("FSTRING1");
        String mdfString2 = extras.getString("FSTRING2");

        TextView tv1 = (TextView) findViewById(R.id.mdf_string_partone);
        TextView tv2 = (TextView) findViewById(R.id.mdf_string_parttwo);

        tv1.setText(mdfString1);
        tv2.setText(mdfString2);
    }
}
