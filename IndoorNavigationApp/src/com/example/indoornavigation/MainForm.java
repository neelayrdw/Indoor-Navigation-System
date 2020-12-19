package com.example.indoornavigation;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Vector;

import dataPack.MapInfo;


import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

public class MainForm extends Activity {
	Button proceedbtn;
	MapInfo mi;
	boolean flag=false;
	ImageView welcomeImage;
	Bitmap bitImage;
	ProgressDialog progressDialog;
	int mapID;
	boolean flag1=false;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main_form);
		welcomeImage=(ImageView)findViewById(R.id.welcomeImage);
		bitImage= Bitmap.createBitmap(500, 500,Config.ARGB_8888);


		try{
			System.out.println("In mapform!!!!");
			File f= new File("myFile.dat");

			System.out.println("In mapform!!!");
			//startDiualog1();


			try{
				FileInputStream fin=null;
				InputStream is = getAssets().open("Map.dat");
				FileOutputStream fo = openFileOutput("myFile.dat", MODE_PRIVATE);
				while(is.available()!=0){
					int b = is.read();
					fo.write(b);
				}
				is.close();
				fo.close();
				try{
					fin=openFileInput("myFile.dat");
				}catch(Exception e){
					 is = getAssets().open("Map.dat");
					 fo = openFileOutput("myFile.dat", MODE_PRIVATE);
					while(is.available()!=0){
						int b = is.read();
						fo.write(b);
					}
					is.close();
					fo.close();
				}

				

				//FileInputStream fin=openFileInput("myFile.dat");
				ObjectInputStream in= new ObjectInputStream(fin);
				mi=(MapInfo)in.readObject();
				in.close();
				fin.close();
				welcomeImage.setImageResource(R.drawable.welcome);
				welcomeImage.invalidate();
				

			}catch(Exception e){
				System.out.println("Error  ::"+e);
				
			}



			proceedbtn=(Button)findViewById(R.id.proceedbtn);

			proceedbtn.setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
					// TODO Auto-generated method stub

					Intent intent = new Intent(MainForm.this, MapActivity.class);
					startActivity(intent);  
				}
			});




		}catch(Exception e1)
		{
			System.out.println("Errorr  ::"+e1);

		}

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main_form, menu);
		return true;
	}




}
