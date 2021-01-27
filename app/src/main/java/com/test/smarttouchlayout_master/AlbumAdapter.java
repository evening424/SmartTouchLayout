package com.test.smarttouchlayout_master;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

/**
 * 相册
 * @author Jagger 2021-1-20
 */
public class AlbumAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{
    private Context mContext;
    private List<DataBean> mList;
    private onItemEventListener mOnItemEventListener;

    /**
     * 事件接口
     */
    public interface onItemEventListener{
        void onItemClicked(DataBean dataBean, int position);
    }

    public AlbumAdapter(Context context, List<DataBean> list){
        this.mContext = context;
        this.mList = list;
    }

    public void setOnItemEventListener(onItemEventListener listener){
        this.mOnItemEventListener = listener;
    }


    @Override
    public int getItemViewType(int position) {
        // TODO Auto-generated method stub
        return 0;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.album_list_item, null);
        ViewHolderPhotoUnLock holder = new ViewHolderPhotoUnLock(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, final int position) {
        final DataBean item = mList.get(position);
        ((ViewHolderPhotoUnLock)holder).img_photo.setBackgroundResource(item.resId);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mOnItemEventListener != null){
                    mOnItemEventListener.onItemClicked(item, position);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }


    /**
     * 图片
     */
    private class ViewHolderPhotoUnLock extends RecyclerView.ViewHolder {
        public ImageView img_photo;

        public ViewHolderPhotoUnLock(@NonNull View itemView) {
            super(itemView);
            img_photo = itemView.findViewById(R.id.img_photo);
        }
    }
}
