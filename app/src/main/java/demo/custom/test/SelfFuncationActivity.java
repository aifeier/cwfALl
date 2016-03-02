package demo.custom.test;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.cwf.app.cwf.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import demo.List.DemoListActivity;
import demo.anim.AnimDemo;
import demo.baidumap.BaseMapActivity;
import demo.custom.CustomLayoutList;
import demo.intent.EventBusDemo;
import demo.intent.VolleyDemoList;
import demo.picture.PictureDemoList;
import demo.qrcode.QRCodeMain;
import lib.BaseActivity;
import lib.MainApplication;
import lib.utils.CommonUtils;
import lib.utils.NotificationUtils;

/**
 * Created by n-240 on 2016/3/2.
 *
 * @author cwf
 */
public class SelfFuncationActivity extends BaseActivity implements AdapterView.OnItemClickListener {

    private ListView mList;
    private List<String> data ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initData();
        mList = (ListView) findViewById(R.id.main_list);
        mList.setAdapter(new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, data));
        mList.setOnItemClickListener(this);
        MainApplication.currentActivity = this;
    }

    private void initData(){
        data = new ArrayList<>();
        data.add("5秒后唤醒屏幕并解锁");
        data.add("显示notification");
        data.add("5秒后唤醒屏幕不解锁显示通知");
        data.add("5秒后唤醒屏幕并解锁");
    }


    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        TimerTask timerTask;
        Timer timer;
        switch(position){
            case 0:
                timerTask = new TimerTask() {
                    @Override
                    public void run() {
                        CommonUtils.wakeUpAndUnlock(getApplicationContext());
                    }
                };
                timer = new Timer();
                timer.schedule(timerTask, 5000);
                break;
            case 1:
                NotificationUtils notificationUtils = new NotificationUtils(getApplicationContext());
                notificationUtils.startNotification("message",EventBusDemo.class,R.drawable.dialog_load, "content" );
//                notificationUtils.showNotification("ok", EventBusDemo.class, R.drawable.file_picker_folder);
                break;
            case 2:
                timerTask = new TimerTask() {
                    @Override
                    public void run() {
                        if(CommonUtils.isSleeping(getApplicationContext())) {
                            CommonUtils.wakeUp(getApplicationContext());
                            startActivity(new Intent(getApplicationContext(), LockNoticationActivity.class));
                        }
                        NotificationUtils notificationUtils = new NotificationUtils(getApplicationContext());
//                        notificationUtils.startNotification("message",EventBusDemo.class,R.drawable.dialog_load, "content" );
                        notificationUtils.showNotification("ok", EventBusDemo.class, R.drawable.file_picker_folder);
                    }
                };
                timer = new Timer();
                timer.schedule(timerTask, 5000);
                break;
            case 3:
                break;
            case 4:
                break;
            case 5:
                break;
            case 6:
                break;
            case 7:
                break;
            default:
                break;
        }

    }


}
