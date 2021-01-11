package com.example.wallpaperapp;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.List;


public class WallPaperAdapter extends RecyclerView.Adapter<WallPaperAdapter.ViewHolder>  {

    private List<WallPaper> mListWallPaper;
    private Context mContext;
    private String[] mWallpaperName;

    public static final int TYPE_HEADER = 0;
    public static final int TYPE_FOOTER = 1;
    public static final int TYPE_NORMAL = 2;

    private View mHeaderView = null;
    private View mFooterView = null;

    public WallPaperAdapter(List<WallPaper> list, Context context,String[] array){
        mListWallPaper =list;
        mContext = context;
        mWallpaperName = array;
    }

    public void setHeaderView(View headerView) {
        mHeaderView = headerView;
        notifyItemInserted(0);
    }
    public void setFooterView(View footerView){
        mFooterView = footerView;

        notifyItemChanged(getItemCount()-1);
    }
    public View getHeaderView(){
        return mHeaderView;
    }
    public View getFooterView(){
        return mFooterView;
    }
    @Override
    public int getItemViewType(int position) {
        if (mHeaderView == null && mFooterView == null){
            return TYPE_NORMAL;
        }
        if (position == 0 && getHeaderView()!=null){
            return TYPE_HEADER;
        }
        if (position == getItemCount()-1 && getFooterView()!=null){
            return TYPE_FOOTER;
        }
        return TYPE_NORMAL;
    }

    class ViewHolder extends RecyclerView.ViewHolder{

        ImageView wallPaper;
        TextView title;
        View wallPaperView;
        public ViewHolder(View itemView){
            super(itemView);
            if (itemView == mHeaderView){
                return;
            }
            if (itemView == mFooterView){
                return;
            }
            wallPaperView=itemView;
            wallPaper = itemView.findViewById(R.id.image_view_wallpaper);
            title = itemView.findViewById(R.id.text_view_title);
        }
    }



    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType){
        if(mHeaderView != null && viewType == TYPE_HEADER) {
            return new ViewHolder(mHeaderView);
        }
        if(mFooterView != null && viewType == TYPE_FOOTER){
            return new ViewHolder(mFooterView);
        }
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.wallpaper_item, parent, false);
        return new ViewHolder(view);
    }
    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position){

        if(getItemViewType(position) ==TYPE_NORMAL){
            WallPaper wallPaper =null;
            wallPaper = mListWallPaper.get(position);
            holder.wallPaper.setImageBitmap(wallPaper.getWallPaperBitmap());
            holder.title.setText(wallPaper.getName());
        }else{
            return;
        }

        holder.wallPaperView.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                Intent intent = new Intent(mContext,PicViewActivity.class);
                intent.putExtra("wallpaper_position",position);
                intent.putExtra("wallpaper_nameArray",mWallpaperName);
                mContext.startActivity(intent);
            }
        });

    }
    @Override
    public int getItemCount(){
        if(mHeaderView == null && mFooterView == null){
            return mListWallPaper.size();
        }else if(mHeaderView == null && mFooterView != null){
            return mListWallPaper.size() + 1;
        }else if (mHeaderView != null && mFooterView == null){
            return mListWallPaper.size() + 1;
        }else {
            return mListWallPaper.size() + 2;
        }
    }


}
