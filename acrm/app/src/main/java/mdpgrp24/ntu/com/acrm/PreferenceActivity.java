package mdpgrp24.ntu.com.acrm;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class PreferenceActivity extends Activity {

	private static final String MY_PREFS_NAME = "MDPGROUP24";
	FunctionPreference PrefFunc;
	EditText edittext_f1;
	EditText edittext_f2;
	String function_pref_string_f1;
	String function_pref_string_f2;



	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_preference);

		PrefFunc = new FunctionPreference(getApplicationContext());
		edittext_f1 = (EditText) findViewById(R.id.edittext_f1);
		edittext_f2 = (EditText) findViewById(R.id.edittext_f2);


		String restoredTextf1 = PrefFunc.getFunctionsDetails().get("f1");
		String restoredTextf2 = PrefFunc.getFunctionsDetails().get("f2");
		if (restoredTextf1 != null) {
			edittext_f1.setText(restoredTextf1);
		}
		if (restoredTextf2 != null){
			edittext_f2.setText(restoredTextf2);
		}

		Button saveAll = (Button)findViewById(R.id.btn_save);
		saveAll.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				function_pref_string_f1 = edittext_f1.getText().toString();
				function_pref_string_f2 = edittext_f2.getText().toString();
				PrefFunc.createFunctions(function_pref_string_f1, function_pref_string_f2);
				onBackPressed();

			}
		});
	}
}
