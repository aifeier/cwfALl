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

package com.google.zxing.client.android.encode;

import java.util.EnumMap;
import java.util.Map;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.client.android.Contents;
import com.google.zxing.client.android.Intents;
import com.google.zxing.client.android.R;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

/**
 * This class does the work of decoding the user's request and extracting all the data
 * to be encoded in a barcode.
 *
 * @author dswitkin@google.com (Daniel Switkin)
 */
final class QRCodeEncoder {

  private static final String TAG = QRCodeEncoder.class.getSimpleName();

  private static final int WHITE = 0xFFFFFFFF;
  private static final int BLACK = 0xFF000000;
  private static final int BLUE1 = 0xFF0000FF;
  private static final int BLUE2 = 0xFF0000DD;
  private static final int BLUE3 = 0xFF0000AA;
  private static final int BLUE4 = 0xFF000088;
  private static final int BLUE5 = 0xFF000077;

  private final Activity activity;
  private String contents;
  private String displayContents;
  private String title;
  private BarcodeFormat format;
  private final int dimension;
  private final boolean useVCard;

  QRCodeEncoder(Activity activity, Intent intent, int dimension, boolean useVCard) throws WriterException {
    this.activity = activity;
    this.dimension = dimension;
    this.useVCard = useVCard;
    String action = intent.getAction();
    if (action.equals(Intents.Encode.ACTION)) {
      encodeContentsFromZXingIntent(intent);
    } else if (action.equals(Intent.ACTION_SEND)) {
//      encodeContentsFromShareIntent(intent);
    }
  }

  String getContents() {
    return contents;
  }

  String getDisplayContents() {
    return displayContents;
  }

  String getTitle() {
    return title;
  }

  boolean isUseVCard() {
    return useVCard;
  }

  // It would be nice if the string encoding lived in the core ZXing library,
  // but we use platform specific code like PhoneNumberUtils, so it can't.
  private boolean encodeContentsFromZXingIntent(Intent intent) {
     // Default to QR_CODE if no format given.
    String formatString = intent.getStringExtra(Intents.Encode.FORMAT);
    format = null;
    if (formatString != null) {
      try {
        format = BarcodeFormat.valueOf(formatString);
      } catch (IllegalArgumentException iae) {
        // Ignore it then
      }
    }
    if (format == null || format == BarcodeFormat.QR_CODE) {
      String type = intent.getStringExtra(Intents.Encode.TYPE);
      if (type == null || type.length() == 0) {
        return false;
      }
      this.format = BarcodeFormat.QR_CODE;
      encodeQRCodeContents(intent, type);
    } else {
      String data = intent.getStringExtra(Intents.Encode.DATA);
      if (data != null && data.length() > 0) {
        contents = data;
        displayContents = data;
        title = activity.getString(R.string.contents_text);
      }
    }
    return contents != null && contents.length() > 0;
  }




  private void encodeQRCodeContents(Intent intent, String type) {
    if (type.equals(Contents.Type.TEXT)) {
      String data = intent.getStringExtra(Intents.Encode.DATA);
      if (data != null && data.length() > 0) {
        contents = data;
        displayContents = data;
        title = activity.getString(R.string.contents_text);
      }
    } 
  }



  Bitmap encodeAsBitmap() throws WriterException {

    int IMAGE_HALFWIDTH = 40;//中间logo的长宽
    Bitmap mBitmap = BitmapFactory.decodeResource(activity.getResources(), R.drawable.logo);
    Matrix m = new Matrix();
    float sx = (float) 2 * IMAGE_HALFWIDTH / mBitmap.getWidth();
    float sy = (float) 2 * IMAGE_HALFWIDTH
    / mBitmap.getHeight();
    m.setScale(sx, sy);//设置缩放信息
    //将logo图片按martix设置的信息缩放
    mBitmap = Bitmap.createBitmap(mBitmap, 0, 0,
            mBitmap.getWidth(), mBitmap.getHeight(), m, false);


    String contentsToEncode = contents;
    if (contentsToEncode == null) {
      return null;
    }
    Map<EncodeHintType,Object> hints = null;
    String encoding = guessAppropriateEncoding(contentsToEncode);
    if (encoding != null) {
      hints = new EnumMap<EncodeHintType,Object>(EncodeHintType.class);
      hints.put(EncodeHintType.CHARACTER_SET, encoding);//设置字符类型
      hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H); // 矫错级别
    }
    MultiFormatWriter writer = new MultiFormatWriter();
    BitMatrix result;
    try {
      result = writer.encode(contentsToEncode, format, dimension, dimension, hints);
    } catch (IllegalArgumentException iae) {
      // Unsupported format
      return null;
    }
    int width = result.getWidth();
    int height = result.getHeight();
    int halfW = width / 2;
    int halfH = height / 2;
    int add = width > height? height/13 : width/13;
    int[] pixels = new int[width * height];
    for (int y = 0; y < height; y++) {
      int offset = y * width;
      for (int x = 0; x < width; x++) {
        int radiusW = Math.abs(halfW - x);
        int radiusH = Math.abs(halfH - y);
        if (x > halfW - IMAGE_HALFWIDTH && x < halfW + IMAGE_HALFWIDTH
                && y > halfH - IMAGE_HALFWIDTH && y < halfH + IMAGE_HALFWIDTH) {
                //该位置用于存放图片信息
               //记录图片每个像素信息
          pixels[offset + x] = mBitmap.getPixel(x - halfW
                  + IMAGE_HALFWIDTH, y - halfH + IMAGE_HALFWIDTH);
        }else if(radiusW < add && radiusH < add)
          pixels[offset + x] = result.get(x, y) ? BLUE1 : WHITE;
        else if(radiusW < add*2 && radiusH < add*2)
          pixels[offset + x] = result.get(x, y) ? BLUE2 : WHITE;
        else if(radiusW < add*3 && radiusH < add*3)
          pixels[offset + x] = result.get(x, y) ? BLUE3 : WHITE;
        else if(radiusW < add*4 && radiusH < add*4)
          pixels[offset + x] = result.get(x, y) ? BLUE4 : WHITE;
        else if(radiusW < add*5 && radiusH < add*5)
          pixels[offset + x] = result.get(x, y) ? BLUE5 : WHITE;
        else
          pixels[offset + x] = result.get(x, y) ? BLACK : WHITE;

      }
    }

    Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
    bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
    return bitmap;
  }

  private static String guessAppropriateEncoding(CharSequence contents) {
    // Very crude at the moment
    for (int i = 0; i < contents.length(); i++) {
      if (contents.charAt(i) > 0xFF) {
        return "UTF-8";
      }
    }
    return null;
  }

}
