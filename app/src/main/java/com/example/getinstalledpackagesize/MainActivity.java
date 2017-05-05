package com.example.getinstalledpackagesize;

import android.content.Context;
import android.content.Intent;
import android.content.pm.IPackageStatsObserver;
import android.content.pm.PackageManager;
import android.content.pm.PackageStats;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.os.RemoteException;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    public static final String TAG = "MainActivity";

    public boolean FINISHED_CALCULATION = false;
    private PackageStats mPstats;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ArrayList<PackageItem> itemList = getInstalledPackages();

        ListViewAdapter adapter = new ListViewAdapter();
        ListView listView = (ListView)findViewById(R.id.listview);
        adapter.setList(itemList);
        listView.setAdapter(adapter);
    }


    private ArrayList<PackageItem> getInstalledPackages() {
        ArrayList<PackageItem> itemList = new ArrayList<PackageItem>();

        Intent intent = new Intent(Intent.ACTION_MAIN,null);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        List<ResolveInfo> resolveInfoList = getPackageManager().queryIntentActivities(intent, 0);

        for(ResolveInfo resolveInfo : resolveInfoList){
            PackageItem item = new PackageItem();
            item.icon = resolveInfo.loadIcon(getPackageManager());
            item.label = (String) resolveInfo.loadLabel(getPackageManager());
            item.packageName = resolveInfo.activityInfo.packageName;

            FINISHED_CALCULATION = false;
            getPackageSize(item.packageName);
            while (true) {
                if (FINISHED_CALCULATION) {
                    break;
                }
                SystemClock.sleep(100);
            }
            if (mPstats != null) {
                item.codeSize = mPstats.codeSize;
                item.cacheSize = mPstats.cacheSize;
                item.dataSize = mPstats.dataSize;
            }

            itemList.add(item);
        }
        return itemList;
    }

    private PackageStats getPackageSize(String packageName) {
        PackageStats stats = null;
        PackageManager pm = getPackageManager();
        Method getPackageSizeInfo;
        try {
            getPackageSizeInfo = pm.getClass().getMethod("getPackageSizeInfo", String.class, IPackageStatsObserver.class);
            getPackageSizeInfo.invoke(pm, packageName,
                    new IPackageStatsObserver.Stub() {
                        @Override
                        public void onGetStatsCompleted(PackageStats pStats, boolean succeeded) throws RemoteException {
                            FINISHED_CALCULATION = true;
                            if (pStats.codeSize > 0) {
                                mPstats = pStats;
                            } else {
                                mPstats = null;
                            }
                        }
                    });
        }
        catch (Exception e) {
            Log.d(TAG, "catch : "+e);
            e.printStackTrace();
        }

        return stats;
    }

    public class ListViewAdapter extends BaseAdapter {
        private ArrayList<PackageItem> itemList = new ArrayList<PackageItem>() ;

        public ListViewAdapter() {
        }

        public void setList(ArrayList<PackageItem> list) {
            itemList = list;
        }

        @Override
        public int getCount() {
            return itemList.size() ;
        }

        @Override
        public Object getItem(int position) {
            return itemList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            final Context context = parent.getContext();
            if (convertView == null) {
                LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(R.layout.listview_item, parent, false);
            }

            ImageView ivIcon = (ImageView) convertView.findViewById(R.id.iv_icon);
            TextView tvLabel = (TextView) convertView.findViewById(R.id.tv_label);
            TextView tvPackageName = (TextView) convertView.findViewById(R.id.tv_packagename);
            TextView tvCodeSize = (TextView) convertView.findViewById(R.id.tv_code_size);
            TextView tvcacheSize = (TextView) convertView.findViewById(R.id.tv_cache_size);
            TextView tvDataSize = (TextView) convertView.findViewById(R.id.tv_data_size);


            PackageItem item = itemList.get(position);
            ivIcon.setImageDrawable(item.icon);
            tvLabel.setText(item.label);
            tvPackageName.setText(item.packageName);
            tvCodeSize.setText("code size: " + item.codeSize);
            tvcacheSize.setText("cache size: " + item.cacheSize);
            tvDataSize.setText("data size: " + item.dataSize);

            return convertView;
        }


    }

    public class PackageItem {
        public Drawable icon;
        public String label;
        public String packageName;

        public long codeSize;
        public long cacheSize;
        public long dataSize;
    }



}
