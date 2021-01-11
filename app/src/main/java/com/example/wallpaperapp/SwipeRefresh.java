package com.example.wallpaperapp;

import android.content.Context;
import android.widget.Toast;

import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import java.util.Collections;
import java.util.List;

public class SwipeRefresh implements SwipeRefreshLayout.OnRefreshListener{
    List<WallPaper> listWallPaper;
    String[] wallpaperName;
    Context context;
    WallPaperAdapter mAdapter;
    SwipeRefreshLayout mRefreshLayout;
    public SwipeRefresh(Context c,List<WallPaper> list,String[] array,WallPaperAdapter adapter,SwipeRefreshLayout refreshLayout){
        context=c;
        listWallPaper =list;
        wallpaperName = array;
        mAdapter=adapter;
        mRefreshLayout = refreshLayout;
    }
    @Override
    public void onRefresh() {
        Collections.shuffle(listWallPaper);
        for (int i=0;i<listWallPaper.size();i++){
            wallpaperName[i] = listWallPaper.get(i).getName();
        }
        Toast.makeText(context,"打乱排序",Toast.LENGTH_SHORT).show();
        mAdapter.notifyDataSetChanged();
        mRefreshLayout.setRefreshing(false);
    }
}
