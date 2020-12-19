package com.example.indoornavigation;

import it.imgview.android.library.imagezoom.ImageViewTouch;

import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.util.Locale;
import java.util.Vector;

import javax.xml.transform.Source;

import dataPack.MapInfo;
import dataPack.SingleBTDevice;
import dataPack.SingleLocation;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.RectF;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.Toast;

public class MapActivity extends Activity   {
	 TextToSpeech tts;
	ImageViewTouch mImage;
	Bitmap b, mutableBitmap;
	Canvas c;
	MapInfo mi;
	int touchX, touchY;
	Djikstra d;
	boolean debugFlag = false;
	int mode = 0;
	private BluetoothAdapter mBtAdapter;
	int locationX=-1, locationY=-1;
	double finalscaleX=-1,finalscaleY=-1;
	int start=-1, end=-1;
//    private ArrayAdapter<String> mPairedDevicesArrayAdapter;
//    private ArrayAdapter<String> mNewDevicesArrayAdapter;
	private Vector<String> allDevices;
	Vector<Integer> path = null;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_map);
		//read map file

		 tts=new TextToSpeech(MapActivity.this, new TextToSpeech.OnInitListener() {

	            @Override
	            public void onInit(int status) {
	                // TODO Auto-generated method stub
	                if(status == TextToSpeech.SUCCESS){
	                    int result=tts.setLanguage(Locale.US);
	                    if(result==TextToSpeech.LANG_MISSING_DATA ||
	                            result==TextToSpeech.LANG_NOT_SUPPORTED){
	                        Log.e("error", "This Language is not supported");
	                    }
	                    else{
	                        //ConvertTextToSpeech();
	                    }
	                }
	                else
	                    Log.e("error", "Initilization Failed!");
	            }
	        });
		d= new Djikstra();
		mi = new MapInfo();
		try {
			FileInputStream fin = openFileInput("myFile.dat");
			ObjectInputStream in = new ObjectInputStream(fin);
			mi = (MapInfo) in.readObject();
			in.close();
			fin.close();
		} catch (Exception e) {
			System.out.println("Error is" + e);
			e.printStackTrace();
		}
		
		b = BitmapFactory.decodeResource(getResources(),R.drawable.map);
		
        mutableBitmap = Bitmap.createBitmap(b.getWidth(), b.getHeight(), Config.ARGB_8888);
        c = new Canvas(mutableBitmap);
        c.drawBitmap(b, 0, 0, null);
        System.out.println("Height:  "+mutableBitmap.getHeight()+"Weight:  "+mutableBitmap.getWidth());
        finalscaleX=mutableBitmap.getWidth()/500.;
        finalscaleY=mutableBitmap.getWidth()/500.;
     
        System.out.println("ScaleX:  "+finalscaleX+"ScaleY:  "+finalscaleY);
        
        mImage = (ImageViewTouch) findViewById( R.id.image );
        		// Take Data from File

		displayImage();
		
		allDevices = new Vector<String>();

		// Register for broadcasts when a device is discovered
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        this.registerReceiver(mReceiver, filter);

        // Register for broadcasts when discovery has finished
        filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        this.registerReceiver(mReceiver, filter);

        // Get the local Bluetooth adapter
        mBtAdapter = BluetoothAdapter.getDefaultAdapter();

     // If the adapter is null, then Bluetooth is not supported
        if (mBtAdapter == null) {
            Toast.makeText(this, "Bluetooth is not available", Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        mImage.setOnLongClickListener(new View.OnLongClickListener() {
			
			@Override
			public boolean onLongClick(View v) {
				// TODO Auto-generated method stub
				registerForContextMenu(v);
				openContextMenu(v);
				return false;
			}
		});
        
        
		mImage.setOnTouchListener(new View.OnTouchListener() {
			
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				int[] values = new int[2];
	            //v.getLocationOnScreen(values);
	            //v.getLocationInWindow(values);
				float f = mImage.getScale();
				
				RectF rect =  mImage.mBitmapRect;

				if(event.getAction()!=1) {
					return false;
				}
				
				
				
				double xS = rect.left;
				double yS = rect.top;
				
				double xM = event.getX();
				double yM = event.getY();
				
				double xE = xM - xS;
				double yE = yM - yS;
				
				
				double xScale = 500. / rect.width();

				xE = xE * xScale;
				yE = yE * xScale;
				
				if(xE < 0 || xE >= 500 || yE < 0 || yE >= 500) return false;
				touchX = (int)(xE*finalscaleX); 
				touchY = (int)(yE*finalscaleY);
				System.out.println("Scale Factor  ::" + xE + " " + yE);
				//Toast.makeText(getApplicationContext(), "xScale  :  "+xScale,Toast.LENGTH_LONG).show();
				return true;
			}
		});
		
		
		//doDiscovery();
		
	}
	 private void ConvertTextToSpeech(String text) {
	        // TODO Auto-generated method stub
	    // String   text ="Hello This Test MSG" ;
	        if(text==null||"".equals(text))
	        {
	            text = "Content not available";
	            tts.speak(text, TextToSpeech.QUEUE_FLUSH, null);
	        }else
	            tts.speak(text, TextToSpeech.QUEUE_FLUSH, null);
	    }
	
	
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.context_menu, menu);

	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
		switch (item.getItemId()) {
			case R.id.ctx_mnu_Source:
				//displayImage();
				boolean foundPoint=false;
				int i=0;
				System.out.println("Location size is" + mi.locList.size());
	
				for (SingleLocation sl : mi.locList) {
					int vx = Math.abs((int)(sl.x*finalscaleX) - touchX);
					int vy = Math.abs((int)(sl.y*finalscaleY) - touchY);
	
					if ((vx <= 15) && (vy <= 15)) {
						start = i;
						foundPoint = true;
						System.out.println("Start Index ::" + start);
						displayImage();

						break;
					}
					i++;
				}
				if (!foundPoint) {
					ConvertTextToSpeech("Wrong Location point");
					Toast.makeText(getApplicationContext(), "Wrong Location point",Toast.LENGTH_LONG).show();
					//start = end = -1;
				}
				if((start != -1) && (end != -1)){
					initGraph();
					findShortestPath();
				}
				// editTodo();
				return true;
			case R.id.ctx_mnu_Dest:
				foundPoint=false;
				i=0;
				System.out.println("Location size is" + mi.locList.size());
	
				for (SingleLocation sl : mi.locList) {
					int vx = Math.abs((int)(sl.x*finalscaleX) - touchX);
					int vy = Math.abs((int)(sl.y*finalscaleY) - touchY);
	
					if ((vx <= 15) && (vy <= 15)) {
						end = i;
						foundPoint = true;
						System.out.println("Dest Index ::" + start);
						displayImage();
						

						break;
					}
					i++;
				}
				if (!foundPoint) {
					ConvertTextToSpeech("Wrong Location point");
					Toast.makeText(getApplicationContext(), "Wrong Location point",Toast.LENGTH_LONG).show();
					//start = end = -1;
				}
				if((start != -1) && (end != -1)){
					initGraph();
					findShortestPath();
					//path = null;
				}
				return true;
			case R.id.ctx_mnu_Clear:
				start = end = -1;
				path = null;
				displayImage();
				return true;
			case R.id.ctx_mnu_Cancel:
				return true;
		}
		return true;
	}
	
	public void findShortestPath() {

		System.out.println("In select Route Start And End " + start + "   "
				+ end);
		if (start == -1 || end == -1) {
			return;
		}
		if (start == end) {
			return;
		}
		try {

			d.start = start;
			d.find();

		} catch (Exception e) {
			System.out.println("Error in select Error" + e);
			e.printStackTrace();
		}
		path = d.getPath(end);

		if (path.get(0) != start) {
			Toast.makeText(getApplicationContext(), "No Path Is Found....!!", Toast.LENGTH_LONG).show();
			return;
		}else{
			displayImage();
		}
	}
	public void initGraph() {
		d = new Djikstra();
		d.N = mi.locList.size();
		d.weight = new int[d.N][d.N];
		for (int i = 0; i < d.N; i++) {
			SingleLocation slI = mi.locList.get(i);
			for (int j = 0; j < d.N; j++) {
				SingleLocation slJ = mi.locList.get(j);
				if (slI.linkedTo.contains(slJ)) {

					// d.weight[i][j] = distance(slI, slJ);
					for (int k = 0; k < slI.linkedTo.size(); k++) {
						SingleLocation slTemp = slI.linkedTo.get(k);
						if (slTemp.equals(slJ)) {
							d.weight[i][j] = slI.linkedWeight.get(k);
						}
					}
				} else {
					d.weight[i][j] = -1;
				}
			}
		}
	}

	public int distance(SingleLocation sl1, SingleLocation sl2) {
		int dx = (int)((sl1.x - sl2.x)*finalscaleX);
		int dy = (int)((sl1.y - sl2.y)*finalscaleY);
		int distance = (int) (Math.sqrt(dx * dx + dy * dy));
		return distance;
	}

	public void displayImage() {
		Paint paint = new Paint();
		paint.setStyle(Paint.Style.FILL_AND_STROKE);
		c.drawBitmap(b, 0, 0, null);
		mImage.setImageBitmap(mutableBitmap);
		System.out.println("Size : " + mi.locList.size());
		
		int index = 0;
		for (SingleLocation sl : mi.locList) {
			paint.setColor(Color.BLACK);
			if(index == start){
				paint.setColor(Color.RED);
			}
			if(index == end){
				paint.setColor(Color.GREEN);
			}
			c.drawCircle((int)(sl.x*finalscaleX), (int)(sl.y*finalscaleY), 5, paint);
			c.drawText(sl.locationName, ((int)(sl.x*finalscaleX) + 5),(int) (sl.y*finalscaleY), paint);
			index++;
		}
		paint.setColor(Color.BLACK);
		paint.setStrokeWidth(3);
		if(path != null){
			for(int i=0;i<path.size()-1;i++){
				c.drawLine((int)(mi.locList.elementAt(path.elementAt(i)).x*finalscaleX),(int)( mi.locList.elementAt(path.elementAt(i)).y*finalscaleY),(int)( mi.locList.elementAt(path.elementAt(i+1)).x*finalscaleX),(int)( mi.locList.elementAt(path.elementAt(i+1)).y*finalscaleY), paint);
			}
		}
		paint.setColor(Color.MAGENTA);
		paint.setStrokeWidth(1);
		paint.setStyle(Style.STROKE);
		if(locationX!=-1){

			c.drawCircle((int)(locationX*finalscaleX), (int)(locationY*finalscaleY), 8, paint);
		}
		
		float targetScale = mImage.getScale();
		mImage.mCurrentScaleFactor = targetScale;
		mImage.zoomToNew(targetScale, 100, 100);
		
		mImage.onZoomAnimationCompleted( mImage.getScale() );
		mImage.invalidate();
		
		//mImage.invalidate();
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.map, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// TODO Auto-generated method stub
		switch (item.getItemId()) {
		case R.id.mnu_DebugOn:
				debugFlag = true;
			break;
		case R.id.mnu_debugOff:
				debugFlag = false;
			break;
		case R.id.Once:
				mode = 0;
				doDiscovery();
			break;
		case R.id.Continuous:
				mode = 1;
				doDiscovery();
			break;
			
				
		default:
			break;
		}
		return super.onOptionsItemSelected(item);
	}
	
	@Override
    protected void onDestroy() {
        super.onDestroy();

        // Make sure we're not doing discovery anymore
        if (mBtAdapter != null) {
            mBtAdapter.cancelDiscovery();
        }

        // Unregister broadcast listeners
        this.unregisterReceiver(mReceiver);
    }

    /**
     * Start device discover with the BluetoothAdapter
     */
    private void doDiscovery() {
        // If we're already discovering, stop it
        if (mBtAdapter.isDiscovering()) {
            mBtAdapter.cancelDiscovery();
        }

        // Request discover from BluetoothAdapter
        mBtAdapter.startDiscovery();
    }

    // The BroadcastReceiver that listens for discovered devices and
    // changes the title when discovery is finished
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            // When discovery finds a device
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // Get the BluetoothDevice object from the Intent
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                allDevices.addElement(device.getAddress());
                showToast("Discovered New : ");
            // When discovery is finished, change the Activity title
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
            	searchCurrentLocation();
            	allDevices.clear();
            	if(mode == 1){
            		doDiscovery();
            	}
            }
        }
    };
	
    
    void searchCurrentLocation(){
    	Vector<Integer> remiveIndices = new Vector<Integer>();
    	for (int i = 0; i < allDevices.size(); i++) {
			boolean found = false;
			String currentDevice = allDevices.get(i);
			System.out.println("Evaluating: " + currentDevice);

			for (SingleBTDevice sbt : mi.btList) {
				//System.out.println(" Comparing With " + sbt.BluetoothId);
				if (sbt.BluetoothId.trim().equalsIgnoreCase(currentDevice.trim())) {
					found = true;
					break;
				}
			}

			if (!found) {
				//allDevices.remove(i);
				remiveIndices.addElement(i);
			}
		}

    	for(int i=remiveIndices.size()-1;i>=0;i--){
    		allDevices.removeElementAt(remiveIndices.elementAt(i));
    	}
    	
//		System.out.println("Modified List::");
//		for (int i = 0; i < allDevices.size(); i++) {
//			System.out.println("   " + allDevices.elementAt(i));
//
//		}

		boolean foundLocation = false;
		SingleLocation location = null;

		for (SingleLocation sl : mi.locList) {
			if (sl.btDevice.size() != allDevices.size()) {
				continue;
			}

			// now check if all bluetooth ids present

			System.out.print("Comparing With : " + sl.locationName
					+ " ");
			for (SingleBTDevice sbt : sl.btDevice) {
				System.out.print(", " + sbt.BluetoothId);
			}
			System.out.println();

			boolean present = true;
			for (String bt : allDevices) {
				if (!isPresent(sl.btDevice, bt)) {
					present = false;
					break;
				}
			}

			if (present) {
				foundLocation = true;
				location = sl;
				break;
			}
		}

		if (foundLocation) {
			//mapImageView.setImageBitmap(drawCircleOnLocationFound(location.x, location.y, location.locationName));
			Toast.makeText(getApplicationContext(),
					"Location Is Found --> " + location.locationName,
					Toast.LENGTH_LONG).show();
			
			ConvertTextToSpeech("Found  Location  " + location.locationName);
			
			Toast.makeText(getApplicationContext(),
					"OFFERS: " + location.Offers,
					Toast.LENGTH_LONG).show();
			
			ConvertTextToSpeech("OFFERS Are " + location.Offers);
			System.out.println("Found Location: "
					+ location.locationName);
			locationX = (int)(location.x*finalscaleX);
			locationY = (int)(location.y*finalscaleY);
			displayImage();
		} else {
			System.out.println("No Mapping Location Found!");
			ConvertTextToSpeech("No Mapping Location Found! ");
			Toast.makeText(getApplicationContext(), "Location Is not Found",
					Toast.LENGTH_LONG).show();
		}
    }
    public boolean isPresent(String str, Vector<String> vecString) {
		boolean found = false;
		for (String str2Compare : vecString) {
			if (str2Compare.equalsIgnoreCase(str)) {
				found = true;
				break;
			}
		}
		return found;
	}

	public boolean isPresent(Vector<SingleBTDevice> vecBTDevices, String str) {
		boolean found = false;
		for (SingleBTDevice sbt : vecBTDevices) {
			if (sbt.BluetoothId.equalsIgnoreCase(str)) {
				found = true;
				break;
			}
		}
		return found;
	}
    void showToast(String header){
    	if(!debugFlag){
    		return;
    	}
    	String op = header + "\n";
    	for(String s : allDevices){
    		op += s + "\n";
    	}
    	Toast.makeText(getApplicationContext(), op, Toast.LENGTH_SHORT).show();
    	System.out.println(op);
    }
    

    
	
}
