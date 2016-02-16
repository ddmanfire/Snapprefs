package com.marz.snapprefs.FilterStoreUtils;

import android.app.Activity;
import android.support.v4.app.Fragment;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageView;

import com.marz.snapprefs.Logger;
import com.marz.snapprefs.R;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Dalton on 2/15/2016.
 */
public class Tab1FragmentNew extends Fragment implements ImageGalleryAdapter.OnImageClickListener{

    private RecyclerView mRecyclerView;
    private ArrayList<String> mImages = new ArrayList<>();

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {


        final View v = (View) inflater.inflate(R.layout.image_gallery, container, false);

        String ExternalStorageDirectoryPath = Environment.getExternalStorageDirectory().toString();
        String targetPath = ExternalStorageDirectoryPath + "/Snapprefs/Filters/";
        File targetDirector = new File(targetPath);
        File[] files = targetDirector.listFiles();
        if (files != null) {
            if (files.length > 0) {
                for (File file : files) {
                    try {
                        if (file.getCanonicalPath().endsWith(".png"))
                            mImages.add(file.getCanonicalPath());
                    }catch(IOException e){}
                }
            }
        } else {
            targetDirector.mkdir();
        }
        Logger.log("set it up");
        Log.e("test", "set it up");

        bindViews(v);

        setUpRecyclerView();

        return v;
    }

    @Override
    public void onImageClick(int position) {
        //TODO open set filter
    }

    private void bindViews(View view) {
        mRecyclerView = (RecyclerView) view.findViewById(R.id.rv);
        //mToolbar = (Toolbar) findViewById(R.id.toolbar);
    }

    private void setUpRecyclerView() {
        int numOfColumns;
        if (ImageGalleryUtils.isInLandscapeMode(this.getActivity())) {
            numOfColumns = 4;
        } else {
            numOfColumns = 3;
        }
        mRecyclerView.setLayoutManager(new GridLayoutManager(this.getActivity(), numOfColumns));
        mRecyclerView.addItemDecoration(new GridSpacesItemDecoration(ImageGalleryUtils.dp2px(this.getActivity(), 2), numOfColumns));
        ImageGalleryAdapter imageGalleryAdapter = new ImageGalleryAdapter(mImages, getContext());
        imageGalleryAdapter.setOnImageClickListener(this);

        mRecyclerView.setAdapter(imageGalleryAdapter);
    }
}

class ImageGalleryAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    // region Member Variables
    private final List<String> mImages;
    private int mGridItemWidth;
    private int mGridItemHeight;
    private OnImageClickListener mOnImageClickListener;
    private Context context;
    // endregion

    // region Interfaces
    public interface OnImageClickListener {
        void onImageClick(int position);
    }
    // endregion

    // region Constructors
    public ImageGalleryAdapter(List<String> images, Context c) {
        mImages = images;
        context = c;
    }
    // endregion

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.image_thumbnail, viewGroup, false);
        v.setLayoutParams(getGridItemLayoutParams(v));

        return new ImageViewHolder(v);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
        final ImageViewHolder holder = (ImageViewHolder) viewHolder;

        String image = mImages.get(position);

        setUpImage(holder.mImageView, image, position);

        holder.mFrameLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int adapterPos = holder.getAdapterPosition();
                if(adapterPos != RecyclerView.NO_POSITION){
                    if (mOnImageClickListener != null) {
                        mOnImageClickListener.onImageClick(adapterPos);
                    }
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        if (mImages != null) {
            return mImages.size();
        } else {
            return 0;
        }
    }

    // region Helper Methods
    public void setOnImageClickListener(OnImageClickListener listener) {
        this.mOnImageClickListener = listener;
    }

    private ViewGroup.LayoutParams getGridItemLayoutParams(View view) {
        ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
        int screenWidth = ImageGalleryUtils.getScreenWidth(view.getContext());
        int numOfColumns;
        if (ImageGalleryUtils.isInLandscapeMode(view.getContext())) {
            numOfColumns = 4;
        } else {
            numOfColumns = 3;
        }

        mGridItemWidth = screenWidth / numOfColumns;
        mGridItemHeight = screenWidth / numOfColumns;

        layoutParams.width = mGridItemWidth;
        layoutParams.height = mGridItemHeight;

        return layoutParams;
    }

    private void setUpImage(ImageView iv, String imagePath, int position) {
        if (!imagePath.equals("")) {
            //iv.setImageDrawable(context.getResources().getDrawable(R.drawable.logo));
            BitmapWorkerTask bwt = new BitmapWorkerTask(iv, position);
            bwt.execute();
            //TODO add loading here
            //Bitmap bitmap = BitmapFactory.decodeFile(imagePath);
            //iv.setImageBitmap(bitmap);
            Log.e("test", "setting img");
        }


    }

    class BitmapWorkerTask extends AsyncTask<Integer, Void, Bitmap> {
        private final WeakReference<ImageView> imageViewReference;
        private int data = 0;

        public BitmapWorkerTask(ImageView imageView, int position) {
            // Use a WeakReference to ensure the ImageView can be garbage collected
            imageViewReference = new WeakReference<ImageView>(imageView);
            data = position;
        }

        // Decode image in background.
        @Override
        protected Bitmap doInBackground(Integer... params) {
            Bitmap bm = BitmapFactory.decodeFile(mImages.get(data));
            return bm;
        }

        // Once complete, see if ImageView is still around and set bitmap.
        @Override
        protected void onPostExecute(Bitmap bitmap) {
            if (imageViewReference != null && bitmap != null) {
                final ImageView imageView = imageViewReference.get();
                if (imageView != null) {
                    imageView.setImageBitmap(bitmap);
                    Log.e("test", "iv != null");
                }
            }
        }

        public Bitmap decodeSampledBitmapFromUri(String path, int reqWidth,
                                                 int reqHeight) {

            Bitmap bm = null;
            // First decode with inJustDecodeBounds=true to check dimensions
            final BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(path, options);

            // Calculate inSampleSize
            options.inSampleSize = calculateInSampleSize(options, reqWidth,
                    reqHeight);

            // Decode bitmap with inSampleSize set
            options.inJustDecodeBounds = false;
            bm = BitmapFactory.decodeFile(path, options);


            return bm;
        }

        public int calculateInSampleSize(

                BitmapFactory.Options options, int reqWidth, int reqHeight) {
            // Raw height and width of image
            final int height = options.outHeight;
            final int width = options.outWidth;
            int inSampleSize = 1;

            if (height > reqHeight || width > reqWidth) {
                if (width > height) {
                    inSampleSize = Math.round((float) height
                            / (float) reqHeight);
                } else {
                    inSampleSize = Math.round((float) width / (float) reqWidth);
                }
            }

            return inSampleSize;
        }
    }


    // endregion

    // region Inner Classes

    public static class ImageViewHolder extends RecyclerView.ViewHolder {

        // region Member Variables
        private final ImageView mImageView;
        private final FrameLayout mFrameLayout;
        // endregion

        // region Constructors
        public ImageViewHolder(final View view) {
            super(view);

            mImageView = (ImageView) view.findViewById(R.id.iv);
            mFrameLayout = (FrameLayout) view.findViewById(R.id.fl);
        }
        // endregion
    }

    // endregion
}

class ImageGalleryUtils {

    public static int getScreenWidth(@NonNull Context context) {
        Point size = new Point();
        ((Activity) context).getWindowManager().getDefaultDisplay().getSize(size);
        return size.x;
    }

    public static int getScreenHeight(@NonNull Context context) {
        Point size = new Point();
        ((Activity) context).getWindowManager().getDefaultDisplay().getSize(size);
        return size.y;
    }

    public static boolean isInLandscapeMode(@NonNull Context context) {
        boolean isLandscape = false;
        if (context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            isLandscape = true;
        }
        return isLandscape;
    }

    public static int dp2px(Context context, int dp) {
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();

        DisplayMetrics displaymetrics = new DisplayMetrics();
        display.getMetrics(displaymetrics);

        return (int) (dp * displaymetrics.density + 0.5f);
    }
}
