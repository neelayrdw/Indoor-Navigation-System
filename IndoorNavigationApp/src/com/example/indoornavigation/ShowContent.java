package com.example.indoornavigation;

import java.util.Vector;





import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.app.Activity;
import android.view.Menu;
import android.widget.EditText;

public class ShowContent extends Activity {
	Handler h;
	boolean flag=false;
	String info;
	EditText content;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_show_content);
		Bundle b = getIntent().getExtras();
		info=b.getString("info");
		content = (EditText)findViewById(R.id.edContent);
		content.setText(info);	


		

	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.show_content, menu);
		return true;
	}




	
}
