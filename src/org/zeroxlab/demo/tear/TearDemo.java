/* Copyright (C) 2010 0xlab.org
 * Authored by: Kan-Ru Chen <kanru@0xlab.org>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.zeroxlab.demo.tear;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.content.Context;
import android.util.Log;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.view.MotionEvent;

public class TearDemo extends Activity {

    private TearView mTearView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mTearView = new TearView(this);
        setContentView(mTearView);
    }

    class TearView extends View implements Runnable {

        private Thread mAnim;
        private Paint mPaint = new Paint();
        private float mHsv[] = new float[3];
        private float mHsv2[] = new float[3];
        private int mX = 0;
        private int mStep = 5;
        private WakeLock mWl;
        final private static int mWidth = 40;
        private boolean mStop = false;

        Handler myViewUpdateHandler = new Handler(){
                public void handleMessage(Message msg) {
                    if (!mStop)
                        invalidate();
                    mWl.acquire();
                    super.handleMessage(msg);
                }
            };

        public TearView(Context c) {
            super(c);
            mHsv[0] = 0;
            mHsv[1] = 1;
            mHsv[2] = 1;
            mHsv2[0] = 360;
            mHsv2[1] = 1;
            mHsv2[2] = 1;
            PowerManager pm = (PowerManager) c.getSystemService(Context.POWER_SERVICE);
            mWl = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK,
                                                 "TearDemo");
        }

        @Override
        protected void onDraw(Canvas c) {
            mHsv[0] += 1;
            if (mHsv[0] > 360)
                mHsv[0] = 0;
            mHsv2[0] -= 1;
            if (mHsv2[0] < 0)
                mHsv2[0] = 360;
            mX += mStep;
            if (mX+mWidth >= getWidth() || mX <= 0)
                mStep = -mStep;
            mPaint.setColor(Color.HSVToColor(mHsv));
            c.drawRect(mX, 0, mX+mWidth, getHeight(), mPaint);
            mPaint.setColor(Color.HSVToColor(mHsv2));
            c.drawRect(getWidth()-mX-mWidth, 0, getWidth()-mX, getHeight(), mPaint);
        }

        @Override
        protected void onWindowVisibilityChanged(int v) {
            if (v == View.VISIBLE) {
                mAnim = new Thread(this);
                mAnim.start();
            }
        }

        @Override
        public boolean onTouchEvent (MotionEvent event) {
            mStop = !mStop;
            return false;
        }

        public void run() {
            while (!Thread.currentThread().isInterrupted()) {
                Message m = new Message();
                myViewUpdateHandler.sendMessage(m);
                try {
                    Thread.sleep(1000/60);
                } catch (Exception e) {
                    Log.w("TearDemo", "Animation thread inturrupted");
                }
            }
        }
    }
}
