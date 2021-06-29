package it.pgp.dualscrolldrawexample;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.provider.Settings;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

public class CustomView extends View implements View.OnTouchListener {
    float x,x0, y,y0;
    final float H;
    final float W;

    final Paint bgpaint = new Paint();
    final TextPaint txtpaint = new TextPaint();

    public static final int VMIN = 0;
    public static final int VMAX = 255;
    public static final int HMIN = 0;
    public static final int HMAX = 10000;

    int current_v = 0;
    int k0_v; // taken as current brightness level on start

    int current_h = 0;
    int k0_h = 0;

    int multiplier = 1;

    public final int savedBrightness;

    private void setBrightness(int b) {
        try {
            Settings.System.putInt(getContext().getContentResolver(),
                    Settings.System.SCREEN_BRIGHTNESS, b);
        }
        catch(Exception ignored) {}
    }

    public void restoreBrightness() {
        setBrightness(savedBrightness);
    }

    public void setCurrentLevel(ScrollMode scrollMode) {
        switch(scrollMode) {
            case VERTICAL:
                float tmp = k0_v + multiplier*((y0-y)/H)*100; // doing y0-y instead of y-y0 due to top-to-bottom vertical coordinate system on Android
                current_v = (int) tmp;
                if(current_v < VMIN) current_v = VMIN;
                else if(current_v > VMAX) current_v = VMAX;
                setBrightness(current_v);
                break;
            case HORIZONTAL:
                tmp = k0_h + multiplier*((x-x0)/W)*100;
                current_h = (int) tmp;
                if(current_h < HMIN) current_h = HMIN;
                else if(current_h > HMAX) current_h = HMAX;
                break;
            default:
                throw new RuntimeException("ARGH");
        }
    }

    public void doDraw(Canvas canvas) {
        StaticLayout sl;
        switch(scrollMode) {
            case VERTICAL:
                sl = getStaticLayout(""+x+"\n"+y+"\n"+ current_v, canvas, scrollMode);
                float cx = W / 4;
                float cx1 = cx + 100;
                sl.draw(canvas);
//                canvas.restore();
                canvas.drawRect(cx,y,cx1,y+100,getBgPaint());
                break;
            case HORIZONTAL:
                sl = getStaticLayout(""+x+"\n"+y+"\n"+ current_h, canvas, scrollMode);
                float cy = H / 4;
                float cy1 = cy + 100;
                sl.draw(canvas);
//                canvas.restore();
                canvas.drawRect(x,cy,x+100,cy1,getBgPaint());
                break;
            default:
                throw new RuntimeException("BORKAGE");
        }
    }

    public StaticLayout getStaticLayout(String text, Canvas canvas, ScrollMode scrollMode) {
        txtpaint.reset();
        txtpaint.setColor(getResources().getColor((scrollMode==ScrollMode.VERTICAL)?android.R.color.white:android.R.color.holo_green_light));
        txtpaint.setTextSize(122.0f);
        txtpaint.setAntiAlias(true);
        Paint.FontMetrics fm = txtpaint.getFontMetrics();
        return new StaticLayout(text, txtpaint, canvas.getWidth(), Layout.Alignment.ALIGN_NORMAL, 1.0f, 0.0f, false);
    }

    public Paint getBgPaint() {
        bgpaint.reset();
        bgpaint.setColor(getResources().getColor(android.R.color.holo_blue_bright));
        return bgpaint;
    }

    // atan2 spanning from 0° to 360°
    public double atan2pd(double y, double x) {
        double a = Math.atan2(y,x);
        if(a < 0.0) a += Math.PI;
        if (a < 0.0 || a >= (2*Math.PI + 0.001))
            throw new RuntimeException("Wrong atan2 domain, found disallowed value: "+a);
        return a*180.0/Math.PI;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        x = event.getX(0);
        y = event.getY(0);
        int action = event.getActionMasked();
        switch(action) {
            case MotionEvent.ACTION_DOWN:
                setupPoints.clear();
                scrollMode = ScrollMode.OFF;
                multiplier = 1;
                x0 = x;
                y0 = y;
                current_v = k0_v;
                current_h = k0_h;
                setupPoints.add(new Point((int)x,(int)y));
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
            case MotionEvent.ACTION_POINTER_UP:
                switch(scrollMode) {
                    case VERTICAL:
                        y0 = y;
                        k0_v = current_v;
                        break;
                    case HORIZONTAL:
                        x0 = x;
                        k0_h = current_h;
                        break;
                }
                float m = action == MotionEvent.ACTION_POINTER_DOWN?10.0f:0.1f;
                multiplier = (int) (multiplier * m);
                if (multiplier < 1) multiplier = 1;
                else if (multiplier > 100) multiplier = 100;
                break;
            case MotionEvent.ACTION_MOVE:
                switch(scrollMode) {
                    case OFF:
                        if(setupPoints.size()==N_SETUP_POINTS) {
                            // compute atan2 of segment between first and last sampled setup point, and decide move direction
                            Point p0 = setupPoints.get(0);
                            Point pN = setupPoints.get(setupPoints.size()-1);
                            double at2 = atan2pd(p0.y-pN.y, pN.x-p0.x);


                            // UNDEFINED:  (15°,75°), (105°,165°), (195°,255°), (285°,345°)
                            // HORIZONTAL: [0°,15°], [165°,195°], [345°,360°]
                            // VERTICAL:   [75°,105°], [255°, 285°]
                            if((at2 >= 75.0 && at2 <= 105.0) || (at2 >= 255.0 && at2 <= 285.0))
                                scrollMode = ScrollMode.VERTICAL;
                            else if (at2 <= 15.0 || (at2 >= 165.0 && at2 <= 195.0) || at2 >= 345.0)
                                scrollMode = ScrollMode.HORIZONTAL;
                            else scrollMode = ScrollMode.UNDEFINED; // the gesture is not clearly horizontal or vertical, don't do anything
                        }
                        else setupPoints.add(new Point((int)x, (int)y));
                        break;
                    case VERTICAL:
                    case HORIZONTAL:
                        setCurrentLevel(scrollMode);
                        break;
                }
                break;
            case MotionEvent.ACTION_UP:
                setupPoints.clear();
                scrollMode = ScrollMode.OFF;
                multiplier = 1;
                k0_v = current_v;
                k0_h = current_h;
        }
        invalidate();
        return true;
    }

    enum ScrollMode {
        OFF, UNDEFINED, VERTICAL, HORIZONTAL
    }

    final List<Point> setupPoints = new ArrayList<>();
    ScrollMode scrollMode = ScrollMode.OFF;
    public static final int N_SETUP_POINTS = 5;


    public CustomView(Context context, AttributeSet attrs) {
        super(context, attrs);

        DisplayMetrics dm = getResources().getDisplayMetrics();
        W = dm.widthPixels;
        H = dm.heightPixels;

        setOnTouchListener(this);

        int b;
        try {
            b = Settings.System.getInt(context.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS, 0);
        }
        catch(Exception ignored) {
            b = -1;
        }
        savedBrightness = b;
        if(savedBrightness >= 0) k0_v = savedBrightness;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if(scrollMode != ScrollMode.OFF && scrollMode != ScrollMode.UNDEFINED) doDraw(canvas);
    }
}
