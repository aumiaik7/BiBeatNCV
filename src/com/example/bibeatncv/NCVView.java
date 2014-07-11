package com.example.bibeatncv;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.Buffer;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.media.AudioManager;
import android.media.SoundPool;
import android.media.SoundPool.OnLoadCompleteListener;
import android.os.Handler;
import android.view.Display;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.WindowManager;
import android.widget.Toast;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;

public class NCVView extends SurfaceView implements SurfaceHolder.Callback {

	int abc = 0;
	private SurfaceHolder sh;
	private final Paint paint = new Paint();
	private final Paint paintGrid = new Paint();
	private final Paint paintText = new Paint();
	private final Paint paintTextR = new Paint();
	private final Paint paintTextInfo = new Paint();
	//private final Paint paintTextpulseWidth = new Paint();
	private final Paint paintTextDesign = new Paint();
	private final Paint paintTextPID = new Paint();
	private final Paint paintPIDBox = new Paint();
	private final Paint paintRecord = new Paint();
	private final Paint paintNextPrev = new Paint();
	//;private final Paint pulseWidth = new Paint();
	
	public byte[] controlOutData = new byte[]{1,0,0,0,0,0,0,0};
	

	Handler handler;

	private final Paint paintLine = new Paint();

	private Bitmap mBmBackground;
	private float offset = 128;
	Context ctx;
	NCVThread thread;

	UsbDevice Mydevice;
	boolean IntClamed = false;
	UsbInterface intf;
	UsbEndpoint endpoint;
	UsbDeviceConnection connection;
	private static final byte[] receivedDataInt = new byte[8];
	private static final byte[] receivedData = new byte[800];

	private static int[] receivedDataInte = new int[800];
	boolean DeviceAttached = false;

	private static SoundPool soundPool;

	private int soundID;
	boolean loaded = false;

	AudioManager audioManager;
	float actualVolume;
	float maxVolume;
	float volume;

	int topMargin = 70;
	int leftMargin = 80;
	int bottomMargin = 170;

	int graphWidth = 800;
	int graphHeight = 560;

	private int canvasWidth = 960;
	private int canvasHeight = 800;
	
	private int graphMid = 350;
	
	int flagControlTrans =-1;
	int flagInterruptTrans =-1;
	
	
	Bitmap icon = BitmapFactory.decodeResource(getResources(),R.drawable.icon);
	RectF patientIDRect = new RectF(930, 325, 1230, 375) ;
	RectF recordRect = new RectF(370, 10, 580, 60);
	RectF infoRect = new RectF(930, 400, 1230, 480) ;
	//RectF pulseWidthRect = new RectF(1220, 3, 1275, 50);
	
	//public boolean stimulationButtonShow = true;
	
	RectF nextRect = new RectF(1080, 70, 1150, 130);
	RectF prevRect = new RectF(980, 70, 1050, 130);
	
	
	String recordString = "Record";
	//String stimulationString = "Stimulation";
	String timeString = "Time";
	int recordX = 430;// 380;//425;
	int recordY = 43;// 45;
	int idX = 968;// 380;//425;
	int idY = 362;// 45;
	
	int stimuTimeX = 940;// 380;//425;
	int stimuTimeY = 430;// 45;
	
	//int pwX = 931;// 380;//425;
	//int pwY = 460;// 45;
	
	int recordStat = 0;
	int id = 0;
	int flagRecord = 0;
	Date dNow = new Date();
	String today = String.format("%tB_%<te_%<tY", dNow);
	String monthFolder = String.format("%tB", dNow );
	String yearFolder = String.format("%tY", dNow );
	private BufferedReader in = null;
	private BufferedWriter out = null;

	File root = android.os.Environment.getExternalStorageDirectory();
	String browsedFilePath = "";
	int parsedData[][];
	String info[][];
	int dataCount;
	int countFlag = 0;

	boolean prevButtonEnabled = false;
	boolean nextButtonEnabled = false;
	boolean browseStat = false;
	int browsedID = 0;
	

	private boolean fourtyOrEighty = false; // false for 40ms true for 80ms
	private int gain = 1; // 333 = 1, 1000 = 2, 3333 = 3, 10000 = 4;
	private float gainMult = 0.015f;

	private boolean dataReceived = false;
	int lineFlag = 0;

	private float x, y, x1, y1, x2, y2;

	int ding;
	double localMax;
	int i2, i3;

	DecimalFormat df = new DecimalFormat();
	
	float stimulationTime = 0.1f;
	int perDivision = 1;
	

	/*AlertDialog.Builder builder;
	final CharSequence[] items = {" 1","2","3","4","5","6","7","8","9","10"};
	AlertDialog alert;
	int selectedPulseWidhthIndex = 0;*/
	
	public NCVView(Context context) {

		super(context);
		//builder = new AlertDialog.Builder(context);

		handler = new Handler();

		ctx = context;
		setFocusable(true);
		sh = getHolder();
		sh.addCallback(this);

		// Signal Line Color and type
		paint.setColor(Color.RED);
		paint.setStyle(Style.STROKE);
		paint.setAntiAlias(true);
		paint.setStrokeWidth(1.5f);

		// Graph
		paintGrid.setColor(Color.GRAY);
		paintGrid.setStyle(Style.STROKE);

		// Text
		paintText.setColor(Color.BLACK);
		paintText.setTextSize(32);
		
		// Info Text
		paintTextInfo.setColor(Color.BLACK);
		paintTextInfo.setTextSize(22);
		paintTextInfo.setAntiAlias(true);

		// Text design and develop
		paintTextDesign.setColor(Color.BLACK);
		paintTextDesign.setTextSize(15);
		paintTextR.setAntiAlias(true);
		
		// Text Record
		paintTextR.setColor(Color.RED);
		paintTextR.setTextSize(28);
		paintTextR.setAntiAlias(true);
		
		// Text Patient ID
		paintTextPID.setColor(Color.BLACK);
		paintTextPID.setTextSize(35);
		paintTextPID.setAntiAlias(true);
		
		/*
		// Pulse width change Text
		paintTextpulseWidth.setColor(Color.WHITE);
		paintTextpulseWidth.setTextSize(33);
		paintTextpulseWidth.setAntiAlias(true);
		*/

		// Signal Line Color and type
		paintLine.setColor(Color.MAGENTA);
		paintLine.setStyle(Style.STROKE);
		paintLine.setAntiAlias(true);
		paintLine.setStrokeWidth(2.0f);

		// Record Button
		paintRecord.setColor(Color.GREEN);
		paintRecord.setStyle(Style.FILL);
		paintRecord.setAntiAlias(true);
		// paint.setStrokeWidth(1.5f);
		
		// PID box
		paintPIDBox.setColor(Color.GREEN);
		paintPIDBox.setStyle(Style.STROKE);
		paintPIDBox.setAntiAlias(true);
		// paint.setStrokeWidth(1.5f);
		
			
		// Next and Prev buttonn
		paintNextPrev.setColor(Color.GREEN);
		paintNextPrev.setStyle(Style.FILL);
		paintNextPrev.setAntiAlias(true);
		
		/*
		// Stimulation Button
		pulseWidth.setColor(Color.RED);
		pulseWidth.setStyle(Style.FILL);
		pulseWidth.setAntiAlias(true);
		*/

		df.setMinimumFractionDigits(2);
		df.setMaximumFractionDigits(2);

		// USB Enumeration and Interface Claiming
		UsbManager manager = (UsbManager) context
				.getSystemService(Context.USB_SERVICE);
		HashMap<String, UsbDevice> deviceList = manager.getDeviceList();

		for (UsbDevice device : deviceList.values()) {
			if (device.getVendorId() == 5824 && device.getProductId() == 1503) {

				Mydevice = device;
				DeviceAttached = true;

				intf = Mydevice.getInterface(0);
				endpoint = intf.getEndpoint(0);

				connection = manager.openDevice(Mydevice);
				IntClamed = connection.claimInterface(intf, true);
				break;
			}
		}

		// Load the sound
		soundPool = new SoundPool(10, AudioManager.STREAM_MUSIC, 0);

		soundPool.setOnLoadCompleteListener(new OnLoadCompleteListener() {
			public void onLoadComplete(SoundPool soundPool, int sampleId,
					int status) {
				loaded = true;
			}
		});

		soundID = soundPool.load(context, R.raw.crack_, 1);

		// Sound Volume
		audioManager = (AudioManager) context
				.getSystemService(context.AUDIO_SERVICE);
		actualVolume = (float) audioManager
				.getStreamVolume(AudioManager.STREAM_MUSIC);
		maxVolume = (float) audioManager
				.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
		volume = actualVolume / maxVolume;
		audioManager.setStreamVolume(AudioManager.STREAM_MUSIC,
				(int) maxVolume, AudioManager.FLAG_PLAY_SOUND);
		
		patientID();

	}

	@Override
	public boolean onTouchEvent(MotionEvent ev) {
		// mScaleDetector.onTouchEvent(ev);
		final int action = ev.getAction();

		switch (action & MotionEvent.ACTION_MASK) {
		case MotionEvent.ACTION_DOWN: {
			float x0 = ev.getX();
			float y0 = ev.getY();

			if (x0 >= 370 && x0 <= 580 && y0 >= 10 && y0 <= 60 ) {
				if (recordString == "Record") {
					recordString = "Stop Recording";
					recordX = 380;// 425;
					recordY = 43;// 45;
					paintTextR.setColor(Color.BLACK);
					recordStat += 3;

				} else {
					recordString = "Record";
					recordX = 430;// 380;//425;
					recordY = 43;// 45;
					paintTextR.setColor(Color.RED);
					recordStat--;
				}
				Canvas c = null;
				c = sh.lockCanvas();
				lineFlag = 0;
				thread.doDraw(c);
				if (c != null) {
					sh.unlockCanvasAndPost(c);
				}
			}

			else if (x0 >= 980 && x0 <= 1050 && y0 >= 70 && y0 <= 130 && prevButtonEnabled)// prev canvas.drawRect(980, 70, 1050, 130, paintNextPrev); 
			{
				countFlag--;
				receivedDataInte = parsedData[countFlag];
				stimulationTime =Float.parseFloat( info[countFlag][0]);
				perDivision = Integer.parseInt(info[countFlag][1]);

				if (countFlag == 0) {
					prevButtonEnabled = false;
				}

				nextButtonEnabled = true;
				browseStat = true;
				
				Canvas c = null;
				c = sh.lockCanvas();
				lineFlag = 0;
				thread.doDraw(c);
				if (c != null) {
					sh.unlockCanvasAndPost(c);
				}
			}

			else if (x0 >= 1080 && x0 <= 1150 && y0 >= 70 && y0 <= 130 && nextButtonEnabled)// next canvas.drawRect(1080, 70, 1150, 130, paintNextPrev);
			{
				countFlag++;
				receivedDataInte = parsedData[countFlag];
				stimulationTime =Float.parseFloat( info[countFlag][0]);
				perDivision = Integer.parseInt(info[countFlag][1]);

				
				if (countFlag == dataCount - 1) {
					nextButtonEnabled = false;
				}

				prevButtonEnabled = true;
				browseStat = true;
				
				Canvas c = null;
				c = sh.lockCanvas();
				lineFlag = 0;
				thread.doDraw(c);
				if (c != null) {
					sh.unlockCanvasAndPost(c);
				}


			}
			
			/*
			else if(x0 >= 910 && x0 <= 1250 && y0 >= 425 && y0 <= 475 ) //(910, 425, 1250, 475)
			{
				
				  builder.setTitle("Select Pulse Width");
	                builder.setSingleChoiceItems(items, selectedPulseWidhthIndex, new DialogInterface.OnClickListener() {
	                public void onClick(DialogInterface dialog, int item) {
	                   
	                    
	                    switch(item)
	                    {
	                        case 0:
	                                // Your code when first option seletced
	                        		selectedPulseWidhthIndex = 0;
	                        		controlOutData[0] = 1;
	                        		controlTransfer(controlOutData);
	                        	
	                                 break;
	                        case 1:
                                // Your code when first option seletced
                        		selectedPulseWidhthIndex = 1;
                        		controlOutData[0] = 2;
                        		controlTransfer(controlOutData);
                        	
                                 break;
                        
	                        case 2:
                                // Your code when first option seletced
                        		selectedPulseWidhthIndex = 2;
                        		controlOutData[0] = 3;
                        		controlTransfer(controlOutData);
                        	
                                 break;
                        
	                        case 3:
                                // Your code when first option seletced
                        		selectedPulseWidhthIndex = 3;
                        		controlOutData[0] = 4;
                        		controlTransfer(controlOutData);
                        	
                                 break;
	                        case 4:
                                // Your code when first option seletced
                        		selectedPulseWidhthIndex = 4;
                        		controlOutData[0] = 5;
                        		controlTransfer(controlOutData);
                        	
                                 break;
                        
	                        case 5:
                                // Your code when first option seletced
                        		selectedPulseWidhthIndex = 5;
                        		controlOutData[0] = 6;
                        		controlTransfer(controlOutData);
                        	
                                 break;
	                        case 6:
                                // Your code when first option seletced
                        		selectedPulseWidhthIndex = 6;
                        		controlOutData[0] = 7;
                        		controlTransfer(controlOutData);
                        	
                                 break;
	                        case 7:
                                // Your code when first option seletced
                        		selectedPulseWidhthIndex = 7;
                        		controlOutData[0] = 8;
                        		controlTransfer(controlOutData);
                        	
                                 break;
	                        case 8:
                                // Your code when first option seletced
                        		selectedPulseWidhthIndex = 8;
                        		controlOutData[0] = 9;
                        		controlTransfer(controlOutData);
                        	
                                 break;
	                        case 9:
                                // Your code when first option seletced
                        		selectedPulseWidhthIndex = 9;
                        		controlOutData[0] = 10;
                        		controlTransfer(controlOutData);
                        	
                                 break;
	                     
                        
                        
	                        
	                    }
	                    alert.dismiss();
	                    }
	                });
	                alert = builder.create();
			        alert.show();

			
			}*/
			

			break;

		}
		case MotionEvent.ACTION_MOVE: {
			if (ev.getPointerCount() == 1) {

				x = ev.getX();
				y = ev.getY();
				if (x >= leftMargin && x <= canvasWidth - leftMargin
						&& y >= topMargin && y <= canvasHeight - bottomMargin) {

					Canvas c = null;
					c = sh.lockCanvas();
					lineFlag = 1;
					thread.doDraw(c);
					if (c != null) {
						sh.unlockCanvasAndPost(c);
					}
				}

				break;
			} else if (ev.getPointerCount() == 2) {

				int pointerIndex = ev.findPointerIndex(0);
				x1 = ev.getX(pointerIndex);
				y1 = ev.getY(pointerIndex);

				pointerIndex = ev.findPointerIndex(1);
				x2 = ev.getX(pointerIndex);
				y2 = ev.getY(pointerIndex);
				if (x1 >= leftMargin && x1 <= canvasWidth - leftMargin
						&& y1 >= topMargin && y1 <= canvasHeight - bottomMargin
						&& x2 >= leftMargin && x2 <= canvasWidth - leftMargin
						&& y2 >= topMargin && y2 <= canvasHeight - bottomMargin) {
					Canvas c = null;
					c = sh.lockCanvas();
					lineFlag = 2;
					thread.doDraw(c);
					if (c != null) {
						sh.unlockCanvasAndPost(c);
					}
				}

			}
			break;

		}
		}

		return true;
	}

	
	///Control Out Transfer
	public void controlTransfer(byte[] data)
	{
		if(DeviceAttached)
		{
			controlOutData = data;
			connection.controlTransfer(0x21, 0x09, 0x00, 0x00, controlOutData, 0x08, 10000);
			
			
		}
		
	}
	
	public void setStimulationTime(float st)
	{
		stimulationTime = st;
		
		Canvas c = null;
		c = sh.lockCanvas();
		lineFlag = 0;
		thread.doDraw(c);
		if (c != null) {
			sh.unlockCanvasAndPost(c);
		}
	}
	
	public void setPerDivision(int pd)
	{
		perDivision = pd;
		
		Canvas c = null;
		c = sh.lockCanvas();
		lineFlag = 0;
		thread.doDraw(c);
		if (c != null) {
			sh.unlockCanvasAndPost(c);
		}
		
	}
	
	public void drawFromFile() throws IOException {
		in = new BufferedReader(new FileReader(browsedFilePath));
		final String data = in.readLine();
		
		String browsedFilePathParts [] = browsedFilePath.split("_");
		String getID[] = browsedFilePathParts[0].split("\\/");
		browsedID = Integer.parseInt(getID[getID.length-1]);
		
		
		if (data != null) {
			if (data.contains(";")) {
				String[] dataParts = data.split(";");
				dataCount = dataParts.length;
				
				if (dataCount >= 1) {
					if(dataCount > 1)
					nextButtonEnabled = true;
					
					browseStat = true;
					// nextButton.setEnabled(true);
				}

				// System.out.println("Size = "+parts.length);
				parsedData = new int[dataCount][802];
				info = new String[dataCount][2];
				// System.out.println("Size = " + dataInString.length);

				for (int i = 0; i < dataCount; i++) {
					String[] dataInString = dataParts[i].split(" ");
					info[i][0] = dataInString[0]; 
					info[i][1] = dataInString[1]; 
					for (int j = 2; j < 800; j++) {
						
						parsedData[i][j] = Integer.parseInt(dataInString[j]);
						if(i == 0)
						{
							receivedDataInte[j] = parsedData[0][j];
							
							
						}
						
					}
				}

				stimulationTime =Float.parseFloat( info[countFlag][0]);
				perDivision = Integer.parseInt(info[countFlag][1]);
				dataReceived = true;
				handler.post(new Runnable() {
					public void run() {
						Canvas c = null;
						c = sh.lockCanvas();
						thread.doDraw(c);
						if (c != null) {
							sh.unlockCanvasAndPost(c);
						}
					}
				});
				
				
				
				
			

				// ldn.setData(parsedData[0]);
				// recordLabel.setText("1/"+dataCount);
			} else {
				handler.post(new Runnable() {
					public void run() {
						Toast.makeText(ctx,
								"No Valid Data is saved in this file!",
								Toast.LENGTH_LONG).show();
					}
				});

			}

		} else {
			handler.post(new Runnable() {
				public void run() {
					Toast.makeText(ctx, "No Valid Data is saved in this file!",
							Toast.LENGTH_LONG).show();
				}
			});

		}

	}

	public NCVThread getThread() {
		return thread;
	}

	public void patientID()
	{
		
			try {
					in = new BufferedReader(
							new FileReader(new File(root
									.getAbsolutePath()
									+ "/BiBeatNCV/info/id.txt")));
					id = Integer.parseInt(in.readLine()) + 1;
	
					out = new BufferedWriter(
							new FileWriter(new File(root
									.getAbsolutePath()
									+ "/BiBeatNCV/info/id.txt")));
					out.write("" + id);
					out.flush();
					out.close();
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (NumberFormatException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		
		
	}
	
	public void surfaceCreated(SurfaceHolder holder) {

		// Resulation 960x540

		Canvas canvas = new Canvas();
		canvas = sh.lockCanvas();
		// Draw
		// canvas.drawBitmap(mBmBackground, leftMargin, topMargin, paint);
		
		
		canvas.drawColor(Color.WHITE);
		canvas.drawText("Designed and Developed by", 980, 680, paintTextDesign);
		canvas.drawBitmap(icon, 1165, 620, new Paint());  
		canvas.drawRoundRect(recordRect, 10, 10, paintRecord);
		canvas.drawText(recordString, recordX, recordY, paintTextR);
		// canvas.drawText("BiBeat NCV", 400, 50, paintText);
		// Grid Draw
		for (int i = leftMargin; i <= canvasWidth - leftMargin; i += 20) {
			canvas.drawLine(i, topMargin, i, canvasHeight - bottomMargin,
					paintGrid);
		}
		for (int i = topMargin; i <= canvasHeight - bottomMargin; i += 20) {
			canvas.drawLine(leftMargin, i, canvasWidth - leftMargin, i,
					paintGrid);
		}
		
		
		
		canvas.drawRoundRect(patientIDRect, 5, 5, paintPIDBox);
		//canvas.drawRect(1000, 400, 1200, 450, paintRecord);
		canvas.drawText("Patient ID: "+id, idX, idY, paintTextPID);
		
		canvas.drawRoundRect(infoRect, 5, 5, paintPIDBox);
		canvas.drawText("Stimulation Time: "+stimulationTime + " ms", stimuTimeX, stimuTimeY, paintTextInfo);
		canvas.drawText("Per Division in X-Axis: "+perDivision + " ms", stimuTimeX, stimuTimeY+30, paintTextInfo);
		//canvas.drawRect(1000, 400, 1200, 450, paintRecord);
		//canvas.drawText("Patient ID: "+id, idX, idY, paintTextPID);

		
		/*canvas.drawRoundRect(pulseWidthRect, 5, 5, pulseWidth);
		canvas.drawText("Change Pulse Width", pwX, pwY, paintTextpulseWidth);*/
	
		/*
		 * double sum = 0; double dt = 1/400.0; double time[] = new double[400];
		 * 
		 * for(int i=0; i<400; i++){ sum += dt; time[i] = sum;
		 * 
		 * }
		 * 
		 * Arrays.fill(receivedData, (byte) 100); for(int i=30, j=0; i<= 50;
		 * i++,j++) {
		 * 
		 * receivedData[i] = (byte) (100*(Math.sin(2*(Math.PI)*10*time[j])) +
		 * 100); //receivedData[i] = 50; }
		 */
		// canvas.drawLine(leftMargin+20,topMargin,leftMargin+20,400+topMargin,
		// paint);
		// Draw End
		sh.unlockCanvasAndPost(canvas);

		thread = new NCVThread(sh, ctx, new Handler());
		thread.setRunning(true);
		thread.start();

	}

	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
		thread.setSurfaceSize(width, height);
	}

	public void surfaceDestroyed(SurfaceHolder holder) {
		boolean retry = true;
		thread.setRunning(false);
		while (retry) {
			try {
				thread.join();
				retry = false;
			} catch (InterruptedException e) {
			}
		}
		
	
		
	}

	public void setFlag(boolean fl) {
		fourtyOrEighty = fl;
		lineFlag = 0;
		Canvas c = null;
		c = sh.lockCanvas();
		thread.doDraw(c);
		if (c != null) {
			sh.unlockCanvasAndPost(c);
		}

	}

	public void setGainFlag(int g) {
		gain = g;
		if (gain == 1)
			gainMult = 0.015f;
		else if (gain == 2)
			gainMult = 0.005f;
		else if (gain == 3)
			gainMult = 0.0015f;
		else if (gain == 4)
			gainMult = 0.0005f;

		lineFlag = 0;
		Canvas c = null;
		c = sh.lockCanvas();
		thread.doDraw(c);
		if (c != null) {
			sh.unlockCanvasAndPost(c);
		}

	}

	class NCVThread extends Thread {

		private boolean run = false;

		WindowManager wm = (WindowManager) ctx
				.getSystemService(Context.WINDOW_SERVICE);
		Display disp = wm.getDefaultDisplay();

		public NCVThread(SurfaceHolder surfaceHolder, Context context,
				Handler handler) {
			sh = surfaceHolder;
			handler = handler;
			ctx = context;
		}

		public void doDrawP(Canvas c, float x, float y) {
			// TODO Auto-generated method stub
			c.drawLine(x, 0, x, canvasHeight, paint);
		}

		// If Anythis is needed to do At first
		public void doStart() {
			synchronized (sh) {

			}
		}
		
		

		public void run() {
			while (run) {
				if (DeviceAttached) {
					
				
					
					
					  if(!connection.claimInterface(intf, true)) { 
						  
						  
						
						   /* Intent intent = new Intent(Intent.ACTION_MAIN);
							intent.addCategory(Intent.CATEGORY_HOME);
							intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
							ctx.startActivity(intent);*/
							System.exit(0);
						  
						  /*Intent intent = new Intent(ctx,MainActivity.class);
						  intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
						  intent.putExtra("EXIT", true);
						  ctx.startActivity(intent);*/
						 // return true;

						
							
					  }
					 
					

					flagInterruptTrans = connection.bulkTransfer(endpoint,
							receivedDataInt, receivedDataInt.length, 500);
					// If Successfully Receive Byte via Interrupt Transfer then
					// Perform a Control Transfer
					if (flagInterruptTrans >= 0 && receivedDataInt[0] == 71) {
						 flagControlTrans = connection.controlTransfer(0xA1, 0x01, 0x00,
								0x00, receivedData, 800, 10000);
						flagInterruptTrans =-1; 
							
						lineFlag = 0;


						for (int i = 0; i < 800; i++) {
							receivedDataInte[i] = receivedData[i] & 0xff;
						}
						
						
						if(flagControlTrans >= 0){
							flagControlTrans =-1; 
							browseStat = false;
							nextButtonEnabled = false;
							prevButtonEnabled = false;
							
							Canvas c = null;
							dataReceived = true;
							try {
								c = sh.lockCanvas();
								synchronized (sh) {
									doDraw(c);
								}
							} finally {
								if (c != null) {
									sh.unlockCanvasAndPost(c);
								}
							}
							if (recordString == "Stop Recording") {
								//If Record Button clicked the write data to sd card
								writeRecordedData();
							}
						}
						
						
						
					}
					
					
							
				}
			}
		}

		
		public void writeRecordedData()
		{
			

			if (recordStat % 2 == 1) {
				try {
					if (flagRecord == 0) {

						File dir = new File(
								root.getAbsolutePath()
										+ "/BiBeatNCV/Data/"
										+ yearFolder + "/"
										+ monthFolder);
						dir.mkdirs();
						out = new BufferedWriter(
								new FileWriter(new File(dir, id
										+ "_" + today + ".txt")));
						flagRecord = 1;
					}

					out.append(stimulationTime+ " ");
					out.append(perDivision+ " ");
					for (int i = 0; i < receivedDataInte.length; i++) {
						out.append(receivedDataInte[i] + " ");
					}
					out.append(";");
					out.flush();
					
					
					
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				// out.close();
			}
			
		}
		
		
		public void setRunning(boolean b) {
			run = b;
		}

		public void setSurfaceSize(int width, int height) {
			synchronized (sh) {
				// canvasWidth = width;
				// canvasHeight = height;
				doStart();
			}
		}

		private void drawBackground(Canvas canvas) {
			
			canvas.drawColor(Color.WHITE);
			canvas.drawText("Designed and Developed by", 980, 680, paintTextDesign);
			canvas.drawBitmap(icon, 1165, 620, new Paint());  

			
			
			// canvas.drawText("BiBeat NCV", 400, 50, paintText);
			// Grid Draw
			for (int i = leftMargin; i <= canvasWidth - leftMargin; i += 20) {
				canvas.drawLine(i, topMargin, i, canvasHeight - bottomMargin,
						paintGrid);
			}
			for (int i = topMargin; i <= canvasHeight - bottomMargin; i += 20) {
				canvas.drawLine(leftMargin, i, canvasWidth - leftMargin, i,
						paintGrid);
			}
			
						
			canvas.drawRoundRect(patientIDRect, 5, 5, paintPIDBox);
			
			
			canvas.drawRoundRect(infoRect, 5, 5, paintPIDBox);
			canvas.drawText("Stimulation Time: "+stimulationTime + " ms", stimuTimeX, stimuTimeY, paintTextInfo);
			canvas.drawText("Per Division in X-Axis: "+perDivision + " ms", stimuTimeX, stimuTimeY+30, paintTextInfo);
			//canvas.drawRect(1000, 400, 1200, 450, paintRecord);
			if(!browseStat)
			{
				canvas.drawRoundRect(recordRect, 10, 10, paintRecord);
				canvas.drawText(recordString, recordX, recordY, paintTextR);
				canvas.drawText("Patient ID: "+id, idX, idY, paintTextPID);
			}
			
			/////
			if(prevButtonEnabled){
				canvas.drawRoundRect(prevRect, 20, 20, paintRecord);
				//canvas.drawRect(980, 70, 1050, 130, paintNextPrev);
				canvas.drawText("<<", 995, 108, paintText);
			}
			
			if(nextButtonEnabled){
				canvas.drawRoundRect(nextRect, 20, 20, paintRecord);
				//canvas.drawRect(1080, 70, 1150, 130, paintNextPrev); //1230
				canvas.drawText(">>", 1100, 108, paintText);
			
			}
			
			if(browseStat){
				
				canvas.drawText("Showing Data "+(countFlag+1)+"/"+dataCount, 960, 170, paintTextR);
				canvas.drawText("Patient ID: "+browsedID, idX, idY, paintTextPID);
			}
			
			/*canvas.drawRoundRect(pulseWidthRect, 5, 5, pulseWidth);
			canvas.drawText("Change Pulse Width", pwX, pwY, paintTextpulseWidth);*/
			/*
			if(stimulationButtonShow)
			{
				canvas.drawRoundRect(stimulaionRect, 5, 5, stimulation);
				canvas.drawText("Give Stimulation", stimuX, stimuY, paintTextStimu);
				
			}
			*/
			
		}

		public int checkCurvePattern(int i) {

			localMax = 0;
			i2 = 0;

			boolean flag = false;
			boolean flag2 = false;
			for (int j = i; j <= i + 100; j++) {
				if (receivedDataInte[j + 1] > receivedDataInte[j]) {
					continue;
				} else if (receivedDataInte[j + 1] < receivedDataInte[j]) {
					// ding++;

					if (!flag) {

						localMax = receivedDataInte[j];
						flag = true;
					} else {

						if (receivedDataInte[j + 1] <= receivedDataInte[i]) {
							i2 = j + 1;
							flag2 = true;
							// i3=i2;
							break;
						}
						continue;

					}
				}
			}
			if (flag2) {
				if ((i2 - i) >= 1 && (i2 - i) <= 100
						&& (localMax - receivedDataInte[i]) >= 30)
					return i2;

				else
					return -1;
			} else
				return -1;

		}

		public void ding() {

			ding = 0;
			// int peakPos[] = new int[2];
			int checkPattern;

			for (int i = 20; i < (receivedDataInte.length / 2) - 100; i++) {
				if ((receivedDataInte[i + 1] - receivedDataInte[i]) >= 1) {

					checkPattern = checkCurvePattern(i);

					if (checkPattern != -1) {
						i = (int) (checkPattern + 60);
						ding++;
						continue;
					}
					/*
					 * if(ding < 2) {//line(k,0,k,height); peakPos[ding] = i+5;
					 * i+=20; //System.out.println("Ding"); ding++; } else
					 * ding++; //tk.beep();
					 */

				}
			}

		}

		private void doDraw(Canvas canvas) {

			// canvas.drawBitmap(mBmBackground, leftMargin, topMargin, paint);

			drawBackground(canvas);

			// 80 ms
			if (fourtyOrEighty && dataReceived) {
				// Draw Signal
				for (int i = 0; i < receivedDataInte.length - 1; i++) {

					canvas.drawLine(
							(i + leftMargin),
							(float) ((offset - (receivedDataInte[i])) * 2 + (graphMid)),
							(i + leftMargin + 1),
							(float) ((offset - (receivedDataInte[i + 1])) * 2 + (graphMid)),
							paint);
					ding();
				}
			}
			// 40 ms
			else if (!fourtyOrEighty && dataReceived) {
				// Draw Signal
				for (int i = 0; i < receivedDataInte.length - 1; i += 2) {

					canvas.drawLine(
							(i + leftMargin),
							(float) ((offset - (receivedDataInte[i / 2])) * 2 + (graphMid)),
							(i + leftMargin + 2),
							(float) ((offset - (receivedDataInte[(i + 2) / 2])) * 2 + (graphMid)),
							paint);
					// canvas.drawLine((i+leftMargin),
					// (float)((offset-(receivedDataInte[i/2]))*2+
					// (canvasHeight/2)), (i+leftMargin+2) ,
					// (float)((offset-(receivedDataInte[(i+2)/2] ))*2+
					// (canvasHeight/2)) , paint);
					ding();

					//canvas.drawText("Curve: " + ding, 90, 640, paintText);
				}

			}

			if (lineFlag == 1) {

				canvas.drawLine(x, topMargin, x, canvasHeight - bottomMargin,
						paintLine);
				if (!fourtyOrEighty)
					canvas.drawText(
							"Time: " + df.format(((x - leftMargin) * 0.05f))
									+ " ms", 390, 680, paintText);
				else
					canvas.drawText(
							"Time: " + df.format(((x - leftMargin) * 0.1f))
									+ " ms", 390, 680, paintText);
			}

			else if (lineFlag == 2) {

				canvas.drawLine(leftMargin, y1, canvasWidth - leftMargin, y1,
						paintLine);
				canvas.drawLine(leftMargin, y2, canvasWidth - leftMargin, y2,
						paintLine);

				canvas.drawText(
						"Amplitude: " + df.format(Math.abs(y1 - y2) * gainMult)
								+ " mv", 390, 680, paintText);

			}

			
			
			if (ding == 1 && lineFlag == 0) {
				if (loaded) {
					soundPool.play(soundID, volume, volume, 1, 0, 1f);
				}
			}
			ding = 0;
			
			
				

		}

	}
}