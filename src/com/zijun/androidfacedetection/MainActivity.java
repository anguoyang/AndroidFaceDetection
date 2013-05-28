package com.zijun.androidfacedetection;


import java.io.ByteArrayOutputStream;
import java.io.IOException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ImageFormat;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.os.Bundle;
import android.app.Activity;
import android.app.KeyguardManager;
import android.content.Context;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.WindowManager;
import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Core.MinMaxLocResult;
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc;


import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;

public class MainActivity extends Activity {
	
	private final String tAGString = "Activity";
	
	private BaseLoaderCallback mOpenCVCallBack = new BaseLoaderCallback(this) {
		@Override
		public void onManagerConnected(int status) {
		   switch (status) {
		       case LoaderCallbackInterface.SUCCESS:
		       {
		      Log.i(tAGString, "OpenCV loaded successfully");
		      
		      // Create and set View
		  	
		      setContentView(R.layout.activity_main);
		      
		       } break;
		       default:
		       {
		      super.onManagerConnected(status);
		       } break;
		   }
		    }
	};
//	private Preview mPreview;
	private boolean resumePending;
	
	/*private MenuItem methodItem ;
	private MenuItem numFaces;*/
	//private int method_selection=1;
	//private Mat tmpMat;//Get the template
    //private int flag;
	
	
		
		
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// requestWindowFeature(Window.FEATURE_NO_TITLE);//without title
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		
		if (!OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_5, this, mOpenCVCallBack))
	    {

	        Log.e(tAGString, "Cannot connect to OpenCV Manager");
	    }
		OpenCVLoader.initDebug();
//		mPreview = new Preview(this);
//	    setContentView(mPreview);
		Log.d(tAGString, "OnCreate");
		// setContentView(R.layout.activity_main);
//		flag=1;
//		tmpMat=new Mat();
//		try {
//			tmpMat=Utils.loadResource(this, R.drawable.cleantarget, Highgui.CV_LOAD_IMAGE_COLOR);
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();

	}
   /*public void onWindowFocusChanged(boolean hasFocus)
   {
	   
   }*/
	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		Log.d(tAGString, "Activity Destoried");
		/*******************************88
		 * Careful!!!
		 */
		//mPreview.releaseCamera();
		// Add a function that releases the camera. in mPreivew
		super.onPause();
	}

	@Override
	protected void onResume() {
		Log.d(tAGString, "Resume the funciton");
		// TODO Auto-generated method stub
		KeyguardManager myKeyguardManager = (KeyguardManager) getApplicationContext()
				.getSystemService(Context.KEYGUARD_SERVICE);
		if (myKeyguardManager.inKeyguardRestrictedInputMode()) {
			resumePending = true;
			Log.d(tAGString, "screenLocked");

		} else {
			/**********************
			 * Careful2!!!
			 */
			//mPreview.obtainCamera();
			resumePending = false;
			Log.d(tAGString, "!screenLocked!");

		}
		
		super.onResume();
		if(!OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_5, this,mOpenCVCallBack )){
			Log.e(tAGString, "Can not Connect to OPENCV Manger!");
		}
	}

	public void onWindowFocusChanged(boolean hasFocus) {
		// on test device (LG Optimus) this logic
		// does not appear to be trigger as onResume is called twice
		// once when the phone is woken up and has its lock screen shown
		// and once when the lock screen is disable by the user (passcode
		// entered)
		if (hasFocus && resumePending) {
			Log.d(tAGString, "onWindowFocusChanged -- obtainingCamera");
			/*********************88
			 * Careful 3!!!
			 */
			//mPreview.obtainCamera();
			resumePending = false;
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		Log.d(tAGString, "Menu called");
		menu.add(0, 1, 1, "Use Opencv Based Method(Image)");
		menu.add(0, 2, 2, "Use Local Java Build-in Method(Image)");
		menu.add(0, 3, 3, "About App");
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// TODO Auto-generated method stub
		return super.onOptionsItemSelected(item);
	}



	// The preview class for the main function of the software
	class Preview extends SurfaceView implements Camera.PreviewCallback,
			SurfaceHolder.Callback {
		private static final String TAG_STRING = "Preview";
		// private static final boolean DEBUG=true;
		private static final int layers=5;
		private static final int WINDOW_SIZE=20;// you have to define that more precisely!
		
		
		
		private SurfaceHolder mHolder;
		private Camera mCamera;
		
		//parameters of NCC!
		private org.opencv.core.Point[] centerPoints; 
		private Bitmap tgtBitmap;
        private Bitmap tmpBitmap;
        private Mat tmpMat;
        private Mat grytmpMat;
        private Mat tgtMat;
        private Mat[] scaledMats;
        private org.opencv.core.Size currentSize;
        private double[] resultScore;
        private Mat resultMat;
        
        
		private Paint mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		// pInnerBullsEye and pOuterBullsEye is not declared!
		private boolean windowDraw = false;
       
		
        MinMaxLocResult  locResult;
		//Constructor!
		Preview(Context context)  {
			super(context);
			Log.d(TAG_STRING, "Preview called");
			mHolder = getHolder();
			mHolder.addCallback(this);

			mHolder.setFormat(ImageFormat.NV21);
			mPaint.setColor(Color.RED);
			mPaint.setStyle(Paint.Style.STROKE);
			windowDraw = false;
			setWillNotDraw(windowDraw);
			tmpMat=new Mat();
			tmpBitmap=BitmapFactory.decodeResource(getResources(), R.drawable.cleantarget);
			Utils.bitmapToMat(tmpBitmap, tmpMat);
		
			resultScore=new double[layers];
			grytmpMat=new Mat();
			//=new Mat[layers];
			scaledMats=new Mat[layers];
			
			Imgproc.cvtColor(tmpMat, grytmpMat, Imgproc.COLOR_RGB2GRAY);
			scaledMats[0]=grytmpMat;
			currentSize=new org.opencv.core.Size();
			
			//tmpRect=new MatOfRect(grytmpMat);
			for(int i=0;i<layers-1;i++){
				currentSize.width=(scaledMats[i].cols()/2);
				currentSize.height=(scaledMats[i].rows()/2);
				Imgproc.pyrDown(scaledMats[i], scaledMats[i+1],currentSize);
			}
			centerPoints=new org.opencv.core.Point[layers];
			
			resultMat=new Mat();
		   //TODO: you also have to make pyrUp!
		   //Imgproc.pyrUp(src, dst, dstsize)
			
			
			tgtMat=new Mat();
//			tmpMat=Utils.loadResource(this, R.drawable.cleantarget, Highgui.CV_LOAD_IMAGE_COLOR);
			// TODO Auto-generated constructor stub
			//Imgproc.matchTemplate(image, templ, result, method)
		}

		
		
		
		
		public void obtainCamera() {
			mCamera = Camera.open(CameraInfo.CAMERA_FACING_BACK);// Use the
																	// front
																	// camera
																	// for
																	// debugging
																	// & testing
		}

		public void releaseCamera() {
			if (mCamera != null) {
				mCamera.startPreview();
				mCamera.setPreviewCallbackWithBuffer(null);
				mCamera.release();
				mCamera = null;
			}
			windowDraw = true;
			setWillNotDraw(windowDraw);
		}

		private Camera.Size getBestPreviewSize(int width, int height) {
			Camera.Size result = null;
			Camera.Parameters p = mCamera.getParameters();
			for (Camera.Size size : p.getSupportedPreviewSizes()) {
				if (size.width <= width && size.height <= height) {
					if (result == null) {
						result = size;
					} else {
						int resultArea = result.width * result.height;
						int newArea = size.width * size.height;

						if (newArea > resultArea) {
							result = size;
						}
					}
				}
			}
			return result;
		}

		@Override
		public void surfaceChanged(SurfaceHolder holder, int format, int width,
				int height) {
			// TODO Auto-generated method stub
			Log.d(TAG_STRING, String.format(
					"SurfaceChanged: format=%d,width=%d,height=%d", format,
					width, height));
			if (mCamera != null) {
				mCamera.setDisplayOrientation(90);
				Camera.Parameters parameters = mCamera.getParameters();
				// List<Size> sizes=parameters.getSupportedPreviewSizes();
				Camera.Size optimalSize = getBestPreviewSize(width, height);
				parameters
						.setPreviewSize(optimalSize.width, optimalSize.height);
				mCamera.setParameters(parameters);
				mCamera.startPreview();
				// ATTENTION The format here is included!!!!
				tgtBitmap = Bitmap.createBitmap(optimalSize.width,
						optimalSize.height, Bitmap.Config.ARGB_8888);
				
//				mFaceDetector = new FaceDetector(optimalSize.width,
//						optimalSize.height, MAX_FACES);
				int bufSize = optimalSize.width
						* optimalSize.height
						* ImageFormat.getBitsPerPixel(parameters
								.getPreviewFormat());
				byte[] cbBuffer = new byte[bufSize];
				mCamera.setPreviewCallbackWithBuffer(this);
				mCamera.addCallbackBuffer(cbBuffer);

			}

		}

		@Override
		public void surfaceCreated(SurfaceHolder holder) {
			// TODO Auto-generated method stub
			Log.d(TAG_STRING, "surfaceCreated called: surface is"
					+ mHolder.getSurface().getClass().getName());
			if (mCamera != null) {
				try {
					mCamera.setPreviewDisplay(holder);

				} catch (IOException exception) {
					mCamera.release();
					mCamera = null;
				}

			}
			windowDraw = false;
			setWillNotDraw(windowDraw);
		}

		@Override
		public void surfaceDestroyed(SurfaceHolder holder) {
			// TODO Auto-generated method stub
			Log.d(TAG_STRING, "surfaceDestoried!");

		}

		@Override
		public void onPreviewFrame(byte[] data, Camera camera) {
			// TODO Auto-generated method stub
			Log.d(TAG_STRING, "PreviewFrame!");

			YuvImage yuvImage = new YuvImage(data, ImageFormat.NV21,
					tgtBitmap.getWidth(), tgtBitmap.getHeight(), null);

			Rect rect = new Rect(0, 0, tgtBitmap.getWidth(),
					tgtBitmap.getHeight());

			ByteArrayOutputStream outputStream= new ByteArrayOutputStream();

			if (!yuvImage.compressToJpeg(rect, 100, outputStream)) {
				Log.e(TAG_STRING, "Compress process failed!");
			}
			// BitmapFactory.Options bitOptions=new BitmapFactory.Options();
			// bitOptions.inPreferredConfig=Bitmap.Config.RGB_565;
			// tgtBitmap=BitmapFactory.decodeStream(new
			// ByteArrayInputStream(outputStream.toByteArray()), rect,
			// bitOptions);
			tgtBitmap = BitmapFactory.decodeByteArray(
					(outputStream.toByteArray()), 0, outputStream.size());
            Utils.bitmapToMat(tgtBitmap, tgtMat);
            //TODO: template Matching Using tgtMat and grytmpMat
            
            
            for(int i=0;i<layers;i++){
            Imgproc.matchTemplate(tgtMat, scaledMats[i], resultMat,Imgproc.TM_CCORR_NORMED);
            Core.normalize(resultMat, resultMat, 0, 1, Core.NORM_MINMAX, CvType.CV_8U);
            //Imgproc.blur(src, dst, ksize)
            //MinMaxLocResult locResult=new MinMaxLocResult();
                      locResult=Core.minMaxLoc(resultMat);
                     centerPoints[i]=locResult.maxLoc;
                     resultScore[i]=locResult.maxVal;
                     
            }         
            
            
            
			//if (camera.getParameters().getMaxNumDetectedFaces() != 0) {
//            if (method_selection==2) {
//				
//            	
//				Arrays.fill(mFaces, null);
//				Arrays.fill(faceCenterF, null);
//
//				numFaceDetected = mFaceDetector.findFaces(tgtBitmap, mFaces);
//				if (numFaceDetected > 0) {
//					Log.i(TAG_STRING, "face Detected!" + numFaceDetected);
//				}
//
//				for (int i = 0; i < numFaceDetected; i++) {
//					singleFace = mFaces[i];
//					try {
//						PointF eyesMP = new PointF();
//						singleFace.getMidPoint(eyesMP);
//						eyeDistancef[i] = singleFace.eyesDistance();
//						faceCenterF[i] = eyesMP;
//
//						Log.i("Face",
//								i
//										+ " "
//										+ singleFace.confidence()
//										+ " "
//										+ singleFace.eyesDistance()
//										+ " "
//										+ "Pose: ("
//										+ singleFace
//												.pose(FaceDetector.Face.EULER_X)
//										+ ","
//										+ singleFace
//												.pose(FaceDetector.Face.EULER_Y)
//										+ ","
//										+ singleFace
//												.pose(FaceDetector.Face.EULER_Z)
//										+ ")" + "Eyes Midpoint: (" + eyesMP.x
//										+ "," + eyesMP.y + ")");
//
//					} catch (Exception e) {
//						Log.e("Face", i + " is null");
//					}
//				}
//            }
//				else
//				{
//					//TODO Using OpenCV method to do the face detection!
//				}
//
//					// else{
//					// Log.d(TAG_STRING,"");
//					// }
					invalidate();
					mCamera.addCallbackBuffer(data);

				
			//}
		}

		// @SuppressLint("DrawAllocation")
		@Override
		protected void onDraw(Canvas canvas) {
			// TODO Auto-generated method stub
			Log.d(TAG_STRING,
					"onDraw called: frame size=" + tgtBitmap.getWidth() + ", "
							+ tgtBitmap.getHeight() + ") display size=("
							+ getWidth() + ", " + getHeight() + ")");
			super.onDraw(canvas);
			//if (tgtBitmap != null) {
				//Integer countInteger = 0;

//				for (int i = 0; i < faceCenterF.length; i++) {
//					if (faceCenterF[i] != null) {
//						// See if the calculation is right!
//						// ratio = eyesDistance[i] i* 4.0f / picWidth;
//
//						Rect tmpRect = new Rect(
//								(int) (faceCenterF[i].x - eyeDistancef[i] * 2),
//								(int) (faceCenterF[i].y - eyeDistancef[i] * 2),
//								(int) (faceCenterF[i].x + eyeDistancef[i] * 2),
//								(int) (faceCenterF[i].y + eyeDistancef[i] * 2));
//						canvas.drawRect(tmpRect, mPaint);
//						countInteger = countInteger + 1;
//
//					}
//				}
				for (int i=0;i<layers;i++){
				//TODO Draw the rect of interest
				Rect tempRect=new Rect(
						(int)(centerPoints[i].x-WINDOW_SIZE/2),
						(int)(centerPoints[i].y-WINDOW_SIZE/2),
						(int)(centerPoints[i].x+WINDOW_SIZE/2),
						(int)(centerPoints[i].y+WINDOW_SIZE/2)
						);
				canvas.drawRect(tempRect, mPaint);
				}
				// Log.d(TAG_STRING, countInteger.toString());
			}
		}

	}


