package it.pgp.dualscrolldrawexample;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
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

    public static final int MIN = 0;
    public static final int MAX = 10000;

    int current_v = 0;
    int k0_v = 0; // this should be taken as current brightness level on start

    int current_h = 0;
    int k0_h = 0;

    int multiplier = 1;

    public void setCurrentLevel(ScrollMode scrollMode) {
        switch(scrollMode) {
            case VERTICAL:
                float tmp = k0_v + multiplier*((y0-y)/H)*100; // doing y0-y instead of y-y0 due to top-to-bottom vertical coordinate system on Android
                current_v = (int) tmp;
                if(current_v < MIN) current_v = MIN;
                else if(current_v > MAX) current_v = MAX;
                break;
            case HORIZONTAL:
                tmp = k0_h + multiplier*((x-x0)/W)*100;
                current_h = (int) tmp;
                if(current_h < MIN) current_h = MIN;
                else if(current_h > MAX) current_h = MAX;
                break;
            default:
                throw new RuntimeException("ARGH");
        }
    }

    double atan2approximatingLineNPoints(List<Point> lp1) {
        // preliminary, convert y coordinates of the points
        List<Point> lp = new ArrayList<>(lp1);
        for(Point p : lp) {
            p.y = (int) (H-p.y);
        }
        int n = lp.size();
        float m, c, sum_x = 0, sum_y = 0, sum_xy = 0, sum_x2 = 0;
        for (int i = 0; i < n; i++) {
            Point p = lp.get(i);
            sum_x += p.x;
            sum_y += p.y;
            sum_xy += p.x * p.y;
            sum_x2 += p.x * p.x;
        }

        m = (n * sum_xy - sum_x * sum_y) / (n * sum_x2 - sum_x*sum_x);
        c = (sum_y - m * sum_x) / n;

        // sample two points and compute atan2
        // y1 = m*0 + c      y2 = m*1 + c
        return atan2pd(m,0);
    }

    public void doDraw(Canvas canvas) {
        StaticLayout sl;
        switch(scrollMode) {
            case VERTICAL:
                sl = getStaticLayout(""+x+"\n"+y+"\n"+ current_v, canvas, scrollMode);
                float cx = W / 4;
                float cx1 = cx + 100;
                sl.draw(canvas);
                canvas.restore();
                canvas.drawRect(cx,y,cx1,y+100,getBgPaint());
                break;
            case HORIZONTAL:
                sl = getStaticLayout(""+x+"\n"+y+"\n"+ current_h, canvas, scrollMode);
                float cy = H / 4;
                float cy1 = cy + 100;
                sl.draw(canvas);
                canvas.restore();
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
                                    /*// compute mean of atan2 and decide move direction
                                    double at2 = 0;
                                    Point p0 = setupPoints.get(0);
                                    for(int ii=1;ii<setupPoints.size();ii++) {
                                        Point p = setupPoints.get(ii);
                                        at2 += atan2pd(p0.y-p.y, p.x-p0.x);
                                    }
                                    at2 /= (N_SETUP_POINTS-1);*/


//                                    double at2 = atan2approximatingLineNPoints(setupPoints);


                            // compute mean of atan2 and decide move direction
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
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if(scrollMode != ScrollMode.OFF && scrollMode != ScrollMode.UNDEFINED) doDraw(canvas);
    }
}
