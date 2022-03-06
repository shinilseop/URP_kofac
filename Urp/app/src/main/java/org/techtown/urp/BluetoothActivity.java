package org.techtown.urp;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.Window;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.graphics.Color;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

public class BluetoothActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        DisplayMetrics dm = getApplicationContext().getResources().getDisplayMetrics();
        int width = (int) (dm.widthPixels * 0.75);
        getWindow().getAttributes().width = width;

        setContentView(R.layout.activity_bluetooth);

        RecyclerView recyclerView = findViewById(R.id.recyclerView);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(layoutManager);

        DeviceAdapter adapter = new DeviceAdapter();

        adapter.addItem(new Device("WITLAB_W", "74:F0:7D:F5:B6:D1"));
        adapter.addItem(new Device("null", "7F:C3:29:E3:33:67"));
        adapter.addItem(new Device("null", "6A:06:99:43:38:DD"));
        adapter.addItem(new Device("null", "F0:DF:26:E9:13:11"));

        recyclerView.setAdapter(adapter);
    }


}