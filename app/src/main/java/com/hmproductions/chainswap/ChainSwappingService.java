package com.hmproductions.chainswap;

import android.app.Service;
import android.content.Intent;
import android.graphics.Point;
import android.opengl.GLSurfaceView;
import android.os.IBinder;
import android.util.Log;

import com.google.vr.ndk.base.BufferSpec;
import com.google.vr.ndk.base.BufferViewportList;
import com.google.vr.ndk.base.Frame;
import com.google.vr.ndk.base.GvrApi;
import com.google.vr.ndk.base.GvrLayout;
import com.google.vr.ndk.base.SwapChain;

public class ChainSwappingService extends Service {

    private static final String TAG = ChainSwappingService.class.getSimpleName();

    GvrLayout gvrLayout;
    GLSurfaceView surfaceView;
    GvrApi gvrApi;
    SwapChain swapChain;
    BufferViewportList viewportList;

    private final Point targetSize = new Point();

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        gvrLayout = new GvrLayout(this);

        surfaceView = new GLSurfaceView(this);
        surfaceView.setEGLContextClientVersion(2);
        surfaceView.setEGLConfigChooser(5, 6, 5, 0, 0, 0);
        gvrLayout.setPresentationView(surfaceView);
        gvrLayout.setKeepScreenOn(true);

        gvrApi = gvrLayout.getGvrApi();

        gvrApi.initializeGl();
        Log.v(TAG, "GVR Api initialised");

        gvrApi.getMaximumEffectiveRenderTargetSize(targetSize);

        BufferSpec[] specList = new BufferSpec[1];
        BufferSpec bufferSpec = gvrApi.createBufferSpec();
        bufferSpec.setSize(targetSize);
        specList[0] = bufferSpec;

        viewportList = gvrApi.createBufferViewportList();
        swapChain = gvrApi.createSwapChain(specList);

        startCapturingGraphicalContext();

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void startCapturingGraphicalContext() {
        while (true) {
            Frame frame = swapChain.acquireFrame();

            if (frame == null) {
                Log.v(TAG, "stopping service!");
                stopSelf();
                break;
            } else {
                Log.v(TAG, "acquired new frame");
                frame.submit(viewportList, null);
            }
        }
    }
}
