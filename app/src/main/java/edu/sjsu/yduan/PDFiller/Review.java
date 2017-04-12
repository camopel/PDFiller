package edu.sjsu.yduan.PDFiller;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.widget.ImageView;

public class Review extends AppCompatActivity implements
        GestureDetector.OnGestureListener,
        GestureDetector.OnDoubleTapListener{
    private int BlockID;
    private int FieldID;
    private Form form;
    private Block block;
    private Field field;
    private GestureDetectorCompat mDetector;
    private static final int SWIPE_THRESHOLD = 100;
    private static final int SWIPE_VELOCITY_THRESHOLD = 100;
    private PDF pdf;
    private ImageView mImageView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_review);

        String flkey= getString(R.string.Form_Index);
        String bkey= getString(R.string.Block_Index);
        String fkey= getString(R.string.Field_Index);
        Intent intent = getIntent();
        form = (Form)intent.getParcelableExtra(flkey);
        BlockID = (int)intent.getIntExtra(bkey,-1);
        FieldID = (int)intent.getIntExtra(fkey,-1);
        block = form.get(BlockID);
        field = block.get(FieldID);

        ActionBar ab = getSupportActionBar();
        ab.setTitle(block.Name);

        mDetector = new GestureDetectorCompat(this,this);
        mDetector.setOnDoubleTapListener(this);
        mImageView = (ImageView) findViewById(R.id.mReviewIV);
        pdf = new PDF(form);
        pdf.Fill();
        pdf.CreateRender();
        pdf.SetPage(0);
        pdf.RenderPage(mImageView);
    }
    private void Next(){
        int nf = 0;
        int nb = BlockID+1;
        startFillActivity(nb, nf);
        pdf.CloseRenderer();
        this.finish();
    }
    private void Prev(){
        int nb = BlockID-1;
        int nf = form.get(nb).size()-1;
        startFillActivity(nb, nf);
        pdf.CloseRenderer();
        this.finish();
    }
    private void startFillActivity(int b, int f){
        Context context = this.getApplicationContext();
        String bkey=context.getString(R.string.Block_Index);
        String fkey=context.getString(R.string.Field_Index);
        String flkey=context.getString(R.string.Form_Index);
        Intent intent = new Intent(context,FillActivity.class);
        intent.putExtra(bkey,b);
        intent.putExtra(fkey,f);
        intent.putExtra(flkey,form);
        context.startActivity(intent);
    }


    @Override
    public boolean onTouchEvent(MotionEvent event){
        this.mDetector.onTouchEvent(event);
        return super.onTouchEvent(event);
    }
    @Override
    public boolean onDoubleTap(MotionEvent e) {
        Next();
        //Toast.makeText(this, "Double Tap", Toast.LENGTH_SHORT).show();
        return true;
    }
    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        boolean result = false;
        try {
            float diffY = e2.getY() - e1.getY();
            float diffX = e2.getX() - e1.getX();
            if (Math.abs(diffX) > Math.abs(diffY)) {
                if (Math.abs(diffX) > SWIPE_THRESHOLD && Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
                    if (diffX > 0) {
                        onSwipeRight();
                    } else {
                        onSwipeLeft();
                    }
                    result = true;
                }
            }
            else if (Math.abs(diffY) > SWIPE_THRESHOLD && Math.abs(velocityY) > SWIPE_VELOCITY_THRESHOLD) {
                if (diffY > 0) {
                    onSwipeBottom();
                } else {
                    onSwipeTop();
                }
                result = true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }
    public void onSwipeRight(){
        if(!pdf.RenderPrevPage(this.mImageView)) Prev();
        //Toast.makeText(this, "Right", Toast.LENGTH_SHORT).show();
    }
    public void onSwipeLeft(){
        if(!pdf.RenderNextPage(this.mImageView)) Next();
        //Toast.makeText(this, "Lefet", Toast.LENGTH_SHORT).show();
    }
    public void onSwipeBottom(){
        //Toast.makeText(this, "Bottom", Toast.LENGTH_SHORT).show();
    }
    public void onSwipeTop(){
        //Toast.makeText(this, "Top", Toast.LENGTH_SHORT).show();
    }
    @Override
    public boolean onSingleTapConfirmed(MotionEvent e) {
        return false;
    }
    @Override
    public boolean onDown(MotionEvent e) {
        return false;
    }
    @Override
    public void onShowPress(MotionEvent e) {

    }
    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        return false;
    }
    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        return false;
    }
    @Override
    public void onLongPress(MotionEvent e) {

    }
    @Override
    public boolean onDoubleTapEvent(MotionEvent e) {
        return false;
    }
}
