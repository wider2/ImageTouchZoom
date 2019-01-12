package touch.imagetouch;

import android.content.Context;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;

public class ZoomImageView extends android.support.v7.widget.AppCompatImageView {

    static final int NONE = 0;
    static final int DRAG = 1;
    static final int ZOOM = 2;
    static final int CLICK = 3;

    PointF currentPoint;
    private float normalizedScale = 1;

    private int mode = NONE;

    private Matrix mMatrix = new Matrix();

    private PointF mLastTouch = new PointF();
    private PointF mStartTouch = new PointF();
    private float minScale = 1.0f;
    private float maxScale = 13f;
    private float[] mCriticPoints;

    private float mScale = 1f;
    private float mRight;
    private float mBottom;
    private float mOriginalBitmapWidth;
    private float mOriginalBitmapHeight;
    private float origWidth = 0, origHeight = 0;

    private ScaleGestureDetector mScaleDetector;
    private GestureDetector mGestureDetector;
    private GestureDetector.OnDoubleTapListener doubleTapListener = null;


    public void setOnDoubleTapListener(GestureDetector.OnDoubleTapListener l) {
        doubleTapListener = l;
    }

    public ZoomImageView(Context context) {
        super(context);
        init(context);
    }

    public ZoomImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public ZoomImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }


    @Override
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int bmHeight = getBmHeight();
        int bmWidth = getBmWidth();

        float width = getMeasuredWidth();
        float height = getMeasuredHeight();
        float scale = 1;
        if (origWidth == 0) origWidth = width;
        if (origHeight == 0) origHeight = height;

        if (normalizedScale == 1) {
            width = origWidth;
            height = origHeight;
            scale = 0.6f;
        }
        
        if (width < bmWidth || height < bmHeight) {
            scale = width > height ? height / bmHeight : width / bmWidth;
        }

        mMatrix.setScale(scale, scale);
        mScale = 1f;

        mOriginalBitmapWidth = scale * bmWidth;
        mOriginalBitmapHeight = scale * bmHeight;

        float redundantYSpace = (height - mOriginalBitmapHeight);
        float redundantXSpace = (width - mOriginalBitmapWidth);
        mMatrix.postTranslate(redundantXSpace / 2, redundantYSpace / 2);

        setImageMatrix(mMatrix);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        mScaleDetector.onTouchEvent(event);
        mGestureDetector.onTouchEvent(event);

        mMatrix.getValues(mCriticPoints);
        float translateX = mCriticPoints[Matrix.MTRANS_X];
        float trnslateY = mCriticPoints[Matrix.MTRANS_Y];

        currentPoint = new PointF(event.getX(), event.getY());

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mLastTouch.set(event.getX(), event.getY());
                mStartTouch.set(mLastTouch);
                mode = DRAG;
                break;

            case MotionEvent.ACTION_POINTER_DOWN:
                mLastTouch.set(event.getX(), event.getY());
                mStartTouch.set(mLastTouch);
                mode = ZOOM;
                break;

            case MotionEvent.ACTION_MOVE:
                if (mode == ZOOM || (mode == DRAG && mScale > minScale)) {

                    float deltaX = currentPoint.x - mLastTouch.x;// x difference
                    float deltaY = currentPoint.y - mLastTouch.y;// y difference
                    float scaleWidth = Math.round(mOriginalBitmapWidth * mScale);
                    float scaleHeight = Math.round(mOriginalBitmapHeight * mScale);

                    if (scaleWidth > getWidth()) {
                        if (translateX + deltaX > 0) {
                            deltaX = -translateX;
                        } else if (translateX + deltaX < -mRight) {
                            deltaX = -(translateX + mRight);
                        }
                    } else {
                        deltaX = 0;
                    }

                    if (scaleHeight > getHeight()) {
                        if (trnslateY + deltaY > 0) {
                            deltaY = -trnslateY;
                        } else if (trnslateY + deltaY < -mBottom) {
                            deltaY = -(trnslateY + mBottom);
                        }
                    } else {
                        deltaY = 0;
                    }

                    mMatrix.postTranslate(deltaX, deltaY);
                    mLastTouch.set(currentPoint.x, currentPoint.y);
                }
                break;

            case MotionEvent.ACTION_UP:
                mode = NONE;
                int xDiff = (int) Math.abs(currentPoint.x - mStartTouch.x);
                int yDiff = (int) Math.abs(currentPoint.y - mStartTouch.y);
                if (xDiff < CLICK && yDiff < CLICK)
                    performClick();
                break;

            case MotionEvent.ACTION_POINTER_UP:
                mode = NONE;
                break;
        }
        setImageMatrix(mMatrix);
        invalidate();
        return true;
    }


    private void init(Context context) {
        super.setClickable(true);
        mScaleDetector = new ScaleGestureDetector(context, new ScaleListener());
        mGestureDetector = new GestureDetector(context, new GestureListener());
        mCriticPoints = new float[9];
        setImageMatrix(mMatrix);
        setScaleType(ScaleType.MATRIX);
    }

    private int getBmWidth() {
        Drawable drawable = getDrawable();
        if (drawable != null) {
            return drawable.getIntrinsicWidth();
        }
        return 0;
    }

    private int getBmHeight() {
        Drawable drawable = getDrawable();
        if (drawable != null) {
            return drawable.getIntrinsicHeight();
        }
        return 0;
    }

    private void setScale(float scaleFactor, ScaleGestureDetector detector, MotionEvent e) {

        float newScale = mScale * scaleFactor;
        if (newScale >= maxScale) {  // we reach end of zoom and return back to initial step
            newScale = 1;
            normalizedScale = 1;

            onMeasure((int) origWidth, (int) origHeight);
        }
        if (newScale < maxScale && newScale > minScale) {
            mScale = newScale;
            normalizedScale = mScale;
            float width = getWidth();
            float height = getHeight();
            mRight = (mOriginalBitmapWidth * mScale) - width;
            mBottom = (mOriginalBitmapHeight * mScale) - height;

            float scaledBitmapWidth = mOriginalBitmapWidth * mScale;
            float scaledBitmapHeight = mOriginalBitmapHeight * mScale;

            if (scaledBitmapWidth <= width || scaledBitmapHeight <= height) {
                mMatrix.postScale(scaleFactor, scaleFactor, width / 2, height / 2);
            } else {
                if (detector != null) {
                    mMatrix.postScale(scaleFactor, scaleFactor, detector.getFocusX(), detector.getFocusY());
                } else {
                    mMatrix.postScale(scaleFactor, scaleFactor, e.getX(), e.getY());
                }
            }

            // initial step: we need some more space to make a first zoom in the corner
            if (mScale <= 1.9f) {
                if (width > height) {
                    if (currentPoint.x < width / 2) {
                        mLastTouch.set(currentPoint.x - 200, currentPoint.y);
                    } else {
                        mLastTouch.set(currentPoint.x + 200, currentPoint.y);
                    }
                } else {
                    if (currentPoint.y < height / 2) {
                        mLastTouch.set(currentPoint.x, currentPoint.y - 200);
                    } else {
                        mLastTouch.set(currentPoint.x, currentPoint.y + 200);
                    }
                }
            } else {
                mLastTouch.set(currentPoint.x, currentPoint.y);
            }
        }
    }

    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        @Override
        public boolean onScaleBegin(ScaleGestureDetector detector) {
            mode = ZOOM;
            return true;
        }

        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            float scaleFactor = detector.getScaleFactor();
            setScale(scaleFactor, detector, null);
            return true;
        }
    }

    private class GestureListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            if (doubleTapListener != null) {
                return doubleTapListener.onSingleTapConfirmed(e);
            }
            return performClick();
        }

        @Override
        public void onLongPress(MotionEvent e) {
            performLongClick();
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            return super.onFling(e1, e2, velocityX, velocityY);
        }

        @Override
        public boolean onDoubleTap(MotionEvent e) {
            boolean consumed = false;
            if (doubleTapListener != null) {
                consumed = doubleTapListener.onDoubleTap(e);
            }
            return consumed;
        }

        @Override
        public boolean onDoubleTapEvent(MotionEvent e) {
            if (doubleTapListener != null) {
                setScale(1.2f, null, e);
                //Toast.makeText(getContext(), "onDoubleTapEvent", Toast.LENGTH_SHORT).show();
                return doubleTapListener.onDoubleTapEvent(e);
            }
            return false;
        }
    }

}
