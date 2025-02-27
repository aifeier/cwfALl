package demo.List.PullToRefresh;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.cwf.app.cwf.R;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.handmark.pulltorefresh.library.autoloadlist.AutoLoadAdapter;
import com.handmark.pulltorefresh.library.autoloadlist.AutoRefreshListView;
import com.handmark.pulltorefresh.library.autoloadlist.ViewHolder;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import de.greenrobot.event.EventBus;
import de.greenrobot.event.Subscribe;
import demo.List.RecycleView.RecyclerViewActivity4;
import demo.List.RecycleView.tool.NetWorkRequest;
import demo.intent.entity.NewsInfo;
import demo.picture.toolbox.GalleryActivity;
import demo.picture.toolbox.entiy.ImageItem;
import lib.utils.ActivityUtils;

/**
 * Created by n-240 on 2015/10/29.
 */

public class AutoRefreshListActivity extends Activity{

    private AutoRefreshListView autoRefreshListView;
    private AutoLoadAdapter<NewsInfo> mAdapter;
    private int page = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_pulltorefresh);
        EventBus.getDefault().register(this);
        autoRefreshListView = (AutoRefreshListView) findViewById(R.id.pull_refresh_list);
        mAdapter = new AutoLoadAdapter<NewsInfo>(getApplicationContext(), R.layout.listitem) {
            @Override
            public void buildView(ViewHolder holder, NewsInfo data) {
                holder.setValueToButton(R.id.item_button, data.getTitle());
                holder.setValueToTextView(R.id.item_text, data.getDescription());
                holder.setUrlToImageView(R.id.item_img, data.getPicUrl(), R.drawable.error, R.drawable.loading2);
                holder.findViewById(R.id.item_img).setVisibility(View.GONE);
                holder.setUrlToImageView(R.id.item_header_img, data.getPicUrl(), R.drawable.error, R.drawable.loading2);
                final ArrayList<String> lists = new ArrayList<String>();
                lists.add(data.getPicUrl());
                lists.add(data.getPicUrl());
                lists.add(data.getPicUrl());
                lists.add(data.getPicUrl());
                lists.add(data.getPicUrl());
                int i = (int) (Math.random() * ( lists.size() - 1 ));
                for(int j = i;j > 0;j--)
                    lists.remove(j);
                holder.setPicturesToGridView(R.id.item_gridview, lists, R.drawable.error, R.drawable.loading4);
                GridView gridView = (GridView) holder.findViewById(R.id.item_gridview);
                final ArrayList<ImageItem> imageItems = new ArrayList<ImageItem>();
                ImageItem imageItem = new ImageItem();
                imageItem.setImagePath(data.getPicUrl());
                imageItems.add(imageItem);
                imageItems.add(imageItem);
                imageItems.add(imageItem);
                imageItems.add(imageItem);
                imageItems.add(imageItem);
                for(int j = i;j > 0;j--)
                    imageItems.remove(j);
                gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        GalleryActivity.startThisActivity(AutoRefreshListActivity.this, imageItems , position);
                    }
                });
                /*if (page ==1) {
                    holder.setVideoToView(R.id.item_textureview, "http://115.231.144." +
                            "58/6/j/t/s/d/jtsdhetjxhxpaawfotxqrzuknrohqk/hc.yin" +
                            "yuetai.com/CA7A014F2176C6967B183E333F6C36FB.flv?sc=02fb295b193c140b&" +
                            "br=784&vid=2349335&aid=1559&area=KR&vst=0");
                }else if(page ==3){
                    holder.setVideoToView(R.id.item_textureview,"http://hc.yinyuetai.com/uploads/v" +
                            "ideos/common/05830150C2DB1B4F1FE5327FD2E052B0.flv?sc=bac6f524f1a3443a" +
                            "&br=775&vid=2408908&aid=273&area=HT&vst=0");
                }else if(page == 7){
                    holder.setVideoToView(R.id.item_textureview,"http://hc.yinyuetai.com/uploa" +
                            "ds/videos/common/647F0150B778E873B32628782EDAEA15.flv?sc=d4fdbe767cd" +
                            "aa21a&br=781&vid=2409118&aid=39513&area=ML&vst=0");
                }*/
                page ++;
            }

            @Override
            public void getPage(int page) {
                NetWorkRequest.getPage(page);
            }
        };
        autoRefreshListView.setListAdapter(mAdapter);
        ImageView imageView = new ImageView(this);
        imageView.setImageResource(R.drawable.penguins);
        imageView.setClickable(true);
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(AutoRefreshListActivity.this, RecyclerViewActivity4.class));
            }
        });
        autoRefreshListView.addVisiableHeader(imageView, 300);

    }


    @Subscribe
    public void onEventBackground(ArrayList<NewsInfo> list) {
        mAdapter.setmData(list, autoRefreshListView);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }
}
