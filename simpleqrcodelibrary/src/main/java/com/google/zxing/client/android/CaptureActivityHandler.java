/*
 * Copyright (C) 2008 ZXing authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.zxing.client.android;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.Result;
import com.google.zxing.client.android.camera.CameraManager;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.util.Collection;

/**
 * This class handles all the messaging which comprises the state machine for capture.
 *
 * @author dswitkin@google.com (Daniel Switkin)
 */
public final class CaptureActivityHandler extends Handler {

  private static final String TAG = CaptureActivityHandler.class.getSimpleName();

  private final CaptureActivity activity;
  private final DecodeThread decodeThread;
  private State state;
  private final CameraManager cameraManager;

  private enum State {
    PREVIEW,
    SUCCESS,
    DONE
  }

  CaptureActivityHandler(CaptureActivity activity,
                         Collection<BarcodeFormat> decodeFormats,
                         String characterSet,
                         CameraManager cameraManager) {
    this.activity = activity;
    decodeThread = new DecodeThread(activity, decodeFormats, characterSet,
        new ViewfinderResultPointCallback(activity.getViewfinderView()));
    decodeThread.start();
    state = State.SUCCESS;

    // Start ourselves capturing previews and decoding.
    this.cameraManager = cameraManager;
    cameraManager.startPreview();
    restartPreviewAndDecode();
  }

  @Override
  public void handleMessage(Message message) {
    int msg = message.what;
    if(msg == R.id.auto_focus){
      if (state == State.PREVIEW) {
        cameraManager.requestAutoFocus(this, R.id.auto_focus);
      }
    }else if(msg == R.id.restart_preview){
      Log.d(TAG, "Got restart preview message");
      restartPreviewAndDecode();
    }else if(msg == R.id.decode_succeeded){
      Log.d(TAG, "Got decode succeeded message");
      state = State.SUCCESS;
      Bundle bundle = message.getData();
      Bitmap barcode = bundle == null ? null :
              (Bitmap) bundle.getParcelable(DecodeThread.BARCODE_BITMAP);
      activity.handleDecode((Result) message.obj, barcode);
    }else if(msg == R.id.decode_failed){
      // We're decoding as fast as possible, so when one decode fails, start another.
      state = State.PREVIEW;
      cameraManager.requestPreviewFrame(decodeThread.getHandler(), R.id.decode);
    }else if(msg == R.id.return_scan_result){
      Log.d(TAG, "Got return scan result message");
      activity.setResult(Activity.RESULT_OK, (Intent) message.obj);
      activity.finish();
    }else if(msg == R.id.launch_product_query){
      Log.d(TAG, "Got product query message");
      String url = (String) message.obj;
      Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
      intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
      activity.startActivity(intent);
    }
/*    switch (message.what) {
      case R.id.auto_focus:
        //Log.d(TAG, "Got auto-focus message");
        // When one auto focus pass finishes, start another. This is the closest thing to
        // continuous AF. It does seem to hunt a bit, but I'm not sure what else to do.
        if (state == State.PREVIEW) {
          cameraManager.requestAutoFocus(this, R.id.auto_focus);
        }
        break;
      case R.id.restart_preview:
        Log.d(TAG, "Got restart preview message");
        restartPreviewAndDecode();
        break;
      case R.id.decode_succeeded:
        Log.d(TAG, "Got decode succeeded message");
        state = State.SUCCESS;
        Bundle bundle = message.getData();
        Bitmap barcode = bundle == null ? null :
            (Bitmap) bundle.getParcelable(DecodeThread.BARCODE_BITMAP);
        activity.handleDecode((Result) message.obj, barcode);
        break;
      case R.id.decode_failed:
        // We're decoding as fast as possible, so when one decode fails, start another.
        state = State.PREVIEW;
        cameraManager.requestPreviewFrame(decodeThread.getHandler(), R.id.decode);
        break;
      case R.id.return_scan_result:
        Log.d(TAG, "Got return scan result message");
        activity.setResult(Activity.RESULT_OK, (Intent) message.obj);
        activity.finish();
        break;
      case R.id.launch_product_query:
        Log.d(TAG, "Got product query message");
        String url = (String) message.obj;
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        activity.startActivity(intent);
        break;
    }*/
  }

  public void quitSynchronously() {
    state = State.DONE;
    cameraManager.stopPreview();
    Message quit = Message.obtain(decodeThread.getHandler(), R.id.quit);
    quit.sendToTarget();
    try {
      // Wait at most half a second; should be enough time, and onPause() will timeout quickly
      decodeThread.join(500L);
    } catch (InterruptedException e) {
      // continue
    }

    // Be absolutely sure we don't send any queued up messages
    removeMessages(R.id.decode_succeeded);
    removeMessages(R.id.decode_failed);
  }

  private void restartPreviewAndDecode() {
    if (state == State.SUCCESS) {
      state = State.PREVIEW;
      cameraManager.requestPreviewFrame(decodeThread.getHandler(), R.id.decode);
      cameraManager.requestAutoFocus(this, R.id.auto_focus);
      activity.drawViewfinder();
    }
  }

}
