package tyrant.ble;

import android.os.Handler;
import android.os.Looper;

import java.util.concurrent.Executor;


/**
 * Created by Administrator on 2015/7/2.
 */
public class AndroidMainThreadExecutor implements Executor {

    private Handler handler;

    public AndroidMainThreadExecutor() {
        handler = new Handler(Looper.getMainLooper());
    }

    @Override
    public void execute(Runnable runnable) {
        if(handler.getLooper().getThread() == Thread.currentThread()) {
            runnable.run();
        } else {
            handler.post(runnable);
        }
    }

}
