package ai.hamster.simplecamera;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.hardware.Camera;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.ViewGroup;

import java.io.IOException;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private Camera mCamera;
    private Preview mPreview;
    private final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        mPreview = new Preview(this);
        int permissionCheck = ContextCompat.checkSelfPermission(this,
                Manifest.permission.CAMERA);
        if(permissionCheck != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA},
                    0);
        } else {
            mPreview = new Preview(this);
            setContentView(mPreview);
            Log.i(TAG, "onCreate() done");
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        safeCameraOpen(0);
        mPreview.setCamera(mCamera);
        Log.i(TAG, "onResume() done");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        releaseCameraAndPreview();
        Log.i(TAG, "onDestroy() done");
    }

    private boolean safeCameraOpen(int id) {
        boolean qOpened = false;

        try {
            releaseCameraAndPreview();
            mCamera = Camera.open(id);
            qOpened = (mCamera != null);
        } catch (Exception e) {
            Log.e(getString(R.string.app_name), "failed to open Camera");
            e.printStackTrace();
        }

        return qOpened;
    }

    private void releaseCameraAndPreview() {
        mPreview.setCamera(null);
        if (mCamera != null) {
            mCamera.release();
            mCamera = null;
        }
    }
}

//class Preview extends ViewGroup implements SurfaceHolder.Callback {
class Preview extends SurfaceView implements SurfaceHolder.Callback {

//    SurfaceView mSurfaceView;
    SurfaceHolder mHolder;
    Camera mCamera;
    Camera.Parameters mPreviewSize;
    List<Camera.Size> mSupportedPreviewSizes;

    Preview(Context context) {
        super(context);

//        mSurfaceView = new SurfaceView(context);
//        addView(mSurfaceView);

        // Install a SurfaceHolder.Callback so we get notified when the
        // underlying surface is created and destroyed.
//        mHolder = mSurfaceView.getHolder();
        mHolder = getHolder();
        mHolder.addCallback(this);
        mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        Log.i(context.getString(R.string.app_name), "Preview ctor() done.");
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {

    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
//        mCamera = Camera.open();
        assert(mCamera != null);
        try {
            mCamera.setPreviewDisplay(holder);
            mCamera.setDisplayOrientation(90);

            // Preview callback used whenever new viewfinder frame is available
            mCamera.setPreviewCallback(new Camera.PreviewCallback() {
                public void onPreviewFrame(byte[] data, Camera camera)
                {
                    /*
                    if ( (mDrawOnTop == null) || mFinished )
                        return;

                    if (mDrawOnTop.mRGBData == null)
                    {
                        // Initialize the draw-on-top companion
                        Camera.Parameters params = camera.getParameters();
                        mDrawOnTop.mImageWidth = params.getPreviewSize().width;
                        mDrawOnTop.mImageHeight = params.getPreviewSize().height;
                        mDrawOnTop.mRGBData = new int[mDrawOnTop.mImageWidth * mDrawOnTop.mImageHeight];
                        mDrawOnTop.mYUVData = new byte[data.length];
                    }

                    // Pass YUV data to draw-on-top companion
                    System.arraycopy(data, 0, mDrawOnTop.mYUVData, 0, data.length);
                    mDrawOnTop.invalidate();
                */
                    Log.i("class Preview", "doing preview frame ...");
                }
            });
        }
        catch (IOException exception) {
            mCamera.release();
            mCamera = null;
            Log.e("class Preview", "exception: " + exception.toString());
        }
        Log.i("class Preview", "setPreviewCallback() done...");
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        // Now that the size is known, set up the camera parameters and begin
        // the preview.
        Camera.Parameters parameters = mCamera.getParameters();
        //parameters.setPreviewSize(mPreviewSize.width, mPreviewSize.height);
        parameters.setPreviewSize(320, 240);
        requestLayout();
        mCamera.setParameters(parameters);
        Log.i("Preview class", "Preview::surfaceChanged() done.");
        // Important: Call startPreview() to start updating the preview surface.
        // Preview must be started before you can take a picture.
        mCamera.startPreview();
    }


    public void setCamera(Camera camera) {
        if (mCamera == camera) { return; }

        stopPreviewAndFreeCamera();

        mCamera = camera;

        if (mCamera != null) {
            List<Camera.Size> localSizes = mCamera.getParameters().getSupportedPreviewSizes();
            mSupportedPreviewSizes = localSizes;
            requestLayout();

            try {
                mCamera.setPreviewDisplay(mHolder);
            } catch (IOException e) {
                e.printStackTrace();
            }

            // Important: Call startPreview() to start updating the preview
            // surface. Preview must be started before you can take a picture.
            Log.i("class Preview", "startPreview() ... ");
            mCamera.startPreview();
        }
    }

    public void surfaceDestroyed(SurfaceHolder holder) {
        // Surface will be destroyed when we return, so stop the preview.
        if (mCamera != null) {
            // Call stopPreview() to stop updating the preview surface.
            mCamera.stopPreview();
        }
    }

    /**
     * When this function returns, mCamera will be null.
     */
    private void stopPreviewAndFreeCamera() {

        if (mCamera != null) {
            // Call stopPreview() to stop updating the preview surface.
            mCamera.stopPreview();

            // Important: Call release() to release the camera for use by other
            // applications. Applications should release the camera immediately
            // during onPause() and re-open() it during onResume()).
            mCamera.release();

            mCamera = null;
        }
    }

}