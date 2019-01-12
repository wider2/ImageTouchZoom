package touch.imagetouch;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.widget.ImageView;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    ImageView ivMain;
    private TextView tv_output;
    private ZoomImageView zoomableImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tv_output = (TextView) findViewById(R.id.tv_output);
        ivMain = (ImageView) findViewById(R.id.iv_main);

        zoomableImageView = (ZoomImageView) findViewById(R.id.iv_zoomable);
        zoomableImageView.setOnDoubleTapListener(new GestureDetector.OnDoubleTapListener() {
            @Override
            public boolean onSingleTapConfirmed(MotionEvent motionEvent) {
                return false;
            }

            @Override
            public boolean onDoubleTap(MotionEvent motionEvent) {
                return false;
            }

            @Override
            public boolean onDoubleTapEvent(MotionEvent motionEvent) {
                return false;
            }
        });
    }

}
