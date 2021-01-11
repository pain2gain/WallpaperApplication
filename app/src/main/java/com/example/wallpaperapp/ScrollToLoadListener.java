package com.example.wallpaperapp;

import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import java.util.ArrayList;
import java.util.List;

import static android.widget.AbsListView.OnScrollListener.SCROLL_STATE_IDLE;

public abstract class ScrollToLoadListener extends  RecyclerView.OnScrollListener{
    List<WallPaper> listWallPaper ;
    public ScrollToLoadListener(List<WallPaper> list){
        listWallPaper = list;
    }

    @Override
    public void onScrollStateChanged( RecyclerView recyclerView, int newState) {
        super.onScrollStateChanged(recyclerView, newState);
    }

    @Override
    public void onScrolled( RecyclerView recyclerView, int dx, int dy) {
        super.onScrolled(recyclerView, dx, dy);
        int[] first = new int[2];
        StaggeredGridLayoutManager layoutManager = (StaggeredGridLayoutManager) recyclerView.getLayoutManager();
        layoutManager.findLastCompletelyVisibleItemPositions(first);
        if (layoutManager != null &&
                first[0]== Integer.valueOf(Double.valueOf(listWallPaper.size()).intValue()-1)||
                first[1]== Integer.valueOf(Double.valueOf(listWallPaper.size()).intValue())-1) {

            onLoadMore();
        }
    }
    /**抽象方法 加载更多图片*/
    public abstract void onLoadMore();
}
