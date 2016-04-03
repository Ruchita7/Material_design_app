package com.example.xyzreader.ui;

import android.app.Fragment;
import android.app.LoaderManager;
import android.app.SharedElementCallback;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ShareCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.graphics.Palette;
import android.text.Html;
import android.text.format.DateUtils;
import android.text.method.LinkMovementMethod;
import android.transition.Slide;
import android.transition.Transition;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.example.xyzreader.R;
import com.example.xyzreader.data.ArticleLoader;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.RequestCreator;
import com.squareup.picasso.Target;


import java.io.InputStream;
import java.util.HashMap;

import butterknife.Bind;
import butterknife.BindDimen;
import butterknife.ButterKnife;

/**
 * A fragment representing a single Article detail screen. This fragment is
 * either contained in a {@link ArticleListActivity} in two-pane mode (on
 * tablets) or a {@link ArticleDetailActivity} on handsets.
 */
public class ArticleDetailFragment extends Fragment implements
        LoaderManager.LoaderCallbacks<Cursor> {

    public static final String ARG_ITEM_ID = "item_id";
    public static final String POSITION="position";
   // private static final String TAG = "ArticleDetailFragment";
    private static final float PARALLAX_FACTOR = 1.25f;
    final String LOG_TAG = ArticleDetailFragment.class.getSimpleName();
    String mTransitionName;
    long mItemId;
    private Cursor mCursor;
    private View mRootView;
    private int mMutedColor = 0xFF333333;
    @Bind(R.id.scrollview)
    ObservableScrollView mScrollView;
    @Bind(R.id.draw_insets_frame_layout)
    DrawInsetsFrameLayout mDrawInsetsFrameLayout;
    private ColorDrawable mStatusBarColorDrawable;
    private int mTopInset;
    @Bind(R.id.photo_container)
    View mPhotoContainerView;
    @Bind(R.id.photo)
    ImageView mPhotoView;
    @Bind(R.id.article_title)
    TextView mTitleView;
    @Bind(R.id.article_byline)
    TextView mBylineView;
    @Bind(R.id.article_body)
    TextView mBodyView;
    private int mScrollY;
    private boolean mIsCard = false;
    @BindDimen(R.dimen.detail_card_top_margin)
     int mStatusBarFullOpacityBottom;
    private float mAspectRatio;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public ArticleDetailFragment() {
    }

    private final Callback mImageCallback = new Callback() {
        @Override
        public void onSuccess() {
            scheduleStartPostponedTransition();
        }

        @Override
        public void onError() {
            scheduleStartPostponedTransition();
        }
    };

    public static ArticleDetailFragment newInstance(long itemId, int position) {
        Bundle arguments = new Bundle();
        arguments.putInt(POSITION, position);
        arguments.putLong(ARG_ITEM_ID, itemId);
        //  arguments.putString(ArticleListActivity.TRANSITION_NAME,transitionName);
        ArticleDetailFragment fragment = new ArticleDetailFragment();
        fragment.setArguments(arguments);
        return fragment;
    }

    static float progress(float v, float min, float max) {
        return constrain((v - min) / (max - min), 0, 1);
    }

    static float constrain(float val, float min, float max) {
        if (val < min) {
            return min;
        } else if (val > max) {
            return max;
        } else {
            return val;
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments().containsKey(ARG_ITEM_ID)) {
            mItemId = getArguments().getLong(ARG_ITEM_ID);
        }

        mIsCard = getResources().getBoolean(R.bool.detail_is_card);
        //mStatusBarFullOpacityBottom = getResources().getDimensionPixelSize(R.dimen.detail_card_top_margin);
        setHasOptionsMenu(true);
    }

    public ArticleDetailActivity getActivityCast() {
        return (ArticleDetailActivity) getActivity();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // In support library r8, calling initLoader for a fragment in a FragmentPagerAdapter in
        // the fragment's onCreate may cause the same LoaderManager to be dealt to multiple
        // fragments because their mIndex is -1 (haven't been added to the activity yet). Thus,
        // we do this in onActivityCreated.
        getLoaderManager().initLoader(0, null, this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mRootView = inflater.inflate(R.layout.fragment_article_detail, container, false);
        ButterKnife.bind(this, mRootView);

        // mDrawInsetsFrameLayout = (DrawInsetsFrameLayout) mRootView.findViewById(R.id.draw_insets_frame_layout);
        mDrawInsetsFrameLayout.setOnInsetsCallback(new DrawInsetsFrameLayout.OnInsetsCallback() {
            @Override
            public void onInsetsChanged(Rect insets) {
                mTopInset = insets.top;
            }
        });

        //   mScrollView = (ObservableScrollView) mRootView.findViewById(R.id.scrollview);
        mScrollView.setCallbacks(new ObservableScrollView.Callbacks() {
            @Override
            public void onScrollChanged() {
                mScrollY = mScrollView.getScrollY();
                getActivityCast().onUpButtonFloorChanged(mItemId, ArticleDetailFragment.this);
                mPhotoContainerView.setTranslationY((int) (mScrollY - mScrollY / PARALLAX_FACTOR));
                updateStatusBar();
            }
        });

        //   mPhotoView = (ImageView) mRootView.findViewById(R.id.photo);

       /* if(getArguments().containsKey(ArticleListActivity.TRANSITION_NAME)&&(getArguments().getString(ArticleListActivity.TRANSITION_NAME)!=null)) {
            mTransitionName = getArguments().getString(ArticleListActivity.TRANSITION_NAME);*/
        // if(mTransitionName!=null)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mPhotoView.setTransitionName(getString(R.string.image_resource) + "_" + getArguments().getInt(POSITION));
        }
        //}
        //    mPhotoContainerView = mRootView.findViewById(R.id.photo_container);

        mStatusBarColorDrawable = new ColorDrawable(0);

        mRootView.findViewById(R.id.share_fab).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String title = mTitleView.getText() + " " + mBylineView.getText();
                String body = title + "\n" + mBodyView.getText();
                startActivity(Intent.createChooser(ShareCompat.IntentBuilder.from(getActivity())
                        .setType("text/plain")
                        .setSubject(title)
                        .setText(body)
                        .getIntent(), getString(R.string.action_share)));
            }
        });


        //   bindViews();
        //  updateStatusBar();
        return mRootView;
    }

    private void updateStatusBar() {
        int color = 0;
        if (mPhotoView != null && mTopInset != 0 && mScrollY > 0) {
            float f = progress(mScrollY,
                    mStatusBarFullOpacityBottom - mTopInset * 3,
                    mStatusBarFullOpacityBottom - mTopInset);
            color = Color.argb((int) (255 * f),
                    (int) (Color.red(mMutedColor) * 0.9),
                    (int) (Color.green(mMutedColor) * 0.9),
                    (int) (Color.blue(mMutedColor) * 0.9));
        }
        mStatusBarColorDrawable.setColor(color);
        mDrawInsetsFrameLayout.setInsetBackground(mStatusBarColorDrawable);
    }

    private void bindViews() {
        if (mRootView == null) {
            return;
        }

        // TextView titleView = (TextView) mRootView.findViewById(R.id.article_title);
        // articleTitle = titleView.getText().toString();

        //    TextView bylineView = (TextView) mRootView.findViewById(R.id.article_byline);
        mBylineView.setMovementMethod(new LinkMovementMethod());
        //   TextView bodyView = (TextView) mRootView.findViewById(R.id.article_body);
        //   bodyView.setTypeface(Typeface.createFromAsset(getResources().getAssets(), "Rosario-Regular.ttf"));

        if (mCursor != null) {
            mRootView.setAlpha(0);
            mRootView.setVisibility(View.VISIBLE);
            mRootView.animate().alpha(1);
            mTitleView.setText(mCursor.getString(ArticleLoader.Query.TITLE));
            mBylineView.setText(Html.fromHtml(
                    DateUtils.getRelativeTimeSpanString(
                            mCursor.getLong(ArticleLoader.Query.PUBLISHED_DATE),
                            System.currentTimeMillis(), DateUtils.HOUR_IN_MILLIS,
                            DateUtils.FORMAT_ABBREV_ALL).toString()
                            + " by <font color='#ffffff'>"
                            + mCursor.getString(ArticleLoader.Query.AUTHOR)
                            + "</font>"));
            mBodyView.setText(Html.fromHtml(mCursor.getString(ArticleLoader.Query.BODY)));
            mAspectRatio = mCursor.getFloat(ArticleLoader.Query.ASPECT_RATIO);
           /* ImageLoaderHelper.getInstance(getActivity()).getImageLoader()
                    .get(mCursor.getString(ArticleLoader.Query.PHOTO_URL), new ImageLoader.ImageListener() {
                        @Override
                        public void onResponse(ImageLoader.ImageContainer imageContainer, boolean b) {
                            Bitmap bitmap = imageContainer.getBitmap();
                            if (bitmap != null) {
                                Palette p = Palette.generate(bitmap, 12);
                                mMutedColor = p.getDarkMutedColor(0xFF333333);
                                mPhotoView.setImageBitmap(imageContainer.getBitmap());

                                mRootView.findViewById(R.id.meta_bar)
                                        .setBackgroundColor(mMutedColor);
                                updateStatusBar();
                            }
                        }

                        @Override
                        public void onErrorResponse(VolleyError volleyError) {

                        }
                    });*/
            //   Uri uri = Uri.parse(mCursor.getString(ArticleLoader.Query.PHOTO_URL));

            RequestCreator requestCreator = Picasso.with(getActivity()).load(mCursor.getString(ArticleLoader.Query.PHOTO_URL));
            requestCreator.noFade();
            requestCreator.into(mPhotoView, mImageCallback);


            ImageLoaderHelper.getInstance(getActivity()).getImageLoader()
                    .get(mCursor.getString(ArticleLoader.Query.PHOTO_URL), new ImageLoader.ImageListener() {
                        @Override
                        public void onResponse(ImageLoader.ImageContainer imageContainer, boolean b) {
                            Bitmap bitmap = imageContainer.getBitmap();
                            if (bitmap != null) {
                                Palette p = Palette.generate(bitmap, 12);
                                mMutedColor = p.getDarkMutedColor(0xFF333333);
                                //  mPhotoView.setImageBitmap(imageContainer.getBitmap());

                                mRootView.findViewById(R.id.meta_bar)
                                        .setBackgroundColor(mMutedColor);
                                updateStatusBar();
                            }
                        }

                        @Override
                        public void onErrorResponse(VolleyError volleyError) {

                        }
                    });
       /*     try     {
                InputStream imageStream = getActivity().getContentResolver().openInputStream(uri);
                Bitmap bitmap = BitmapFactory.decodeStream(imageStream);

                if (bitmap != null) {
                    Palette p = Palette.generate(bitmap, 12);
                    mMutedColor = p.getDarkMutedColor(0xFF333333);
                    mRootView.findViewById(R.id.meta_bar)
                            .setBackgroundColor(mMutedColor);
                    updateStatusBar();
                }*/
            //.into(mPhotoView)
        /*    Picasso.with(getActivity()).load(mCursor.getString(ArticleLoader.Query.PHOTO_URL)).into(new Target() {
                @Override
                public void onBitmapLoaded(final Bitmap bitmap, Picasso.LoadedFrom from) {
                    // Save the bitmap or do something with it here

                    //Set it in the ImageView
                    mPhotoView.setImageBitmap(bitmap);
                    if (bitmap != null) {
                        Palette p = Palette.generate(bitmap, 12);
                        mMutedColor = p.getDarkMutedColor(0xFF333333);
                        mRootView.findViewById(R.id.meta_bar)
                                .setBackgroundColor(mMutedColor);
                        updateStatusBar();
                    }
                    scheduleStartPostponedTransition();
                }

                @Override
                public void onPrepareLoad(Drawable placeHolderDrawable) {

                }

                @Override
                public void onBitmapFailed(Drawable errorDrawable) {

                }
            });*/


     /*   } catch (Exception ex) {
            Log.e("MainActivity", "error in creating palette");
       }*/
        } else {
            mRootView.setVisibility(View.GONE);
            mTitleView.setText("N/A");
            mBylineView.setText("N/A");
            mBodyView.setText("N/A");
        }
    }

    private void scheduleStartPostponedTransition() {
  /*      mPhotoView.getViewTreeObserver().addOnPreDrawListener(
                new ViewTreeObserver.OnPreDrawListener() {
                    @Override
                    public boolean onPreDraw() {
                        mPhotoView.getViewTreeObserver().removeOnPreDrawListener(this);
                        getActivity().startPostponedEnterTransition();
                        return true;
                    }
                });*/
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mPhotoView.getViewTreeObserver().addOnPreDrawListener(
                    new ViewTreeObserver.OnPreDrawListener() {
                        @Override
                        public boolean onPreDraw() {
                            mPhotoView.getViewTreeObserver().removeOnPreDrawListener(this);
                            getActivity().startPostponedEnterTransition();
                            return true;
                        }
                    });
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return ArticleLoader.newInstanceForItemId(getActivity(), mItemId);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        if (!isAdded()) {
            if (cursor != null) {
                cursor.close();
            }
            return;
        }

        mCursor = cursor;
        if (mCursor != null && !mCursor.moveToFirst()) {
            Log.e(LOG_TAG, "Error reading item detail cursor");
            mCursor.close();
            mCursor = null;
        }

        bindViews();

        scheduleStartPostponedTransition();


    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {
        mCursor = null;
        bindViews();
    }

    public int getUpButtonFloor() {
        if (mPhotoContainerView == null || mPhotoView.getHeight() == 0) {
            return Integer.MAX_VALUE;
        }

        // account for parallax
        return mIsCard
                ? (int) mPhotoContainerView.getTranslationY() + mPhotoView.getHeight() - mScrollY
                : mPhotoView.getHeight() - mScrollY;
    }

/*

    @Override
    public void setReturnTransition(Transition transition) {
        super.setReturnTransition(transition);
        Log.v(LOG_TAG,"in setReturnTransition");
    }

    @Override
    public void setExitTransition(Transition transition) {
        super.setExitTransition(transition);
        Log.v(LOG_TAG, "in setExitTransition");
    }



    @Override
    public void setExitSharedElementCallback(SharedElementCallback callback) {
        super.setExitSharedElementCallback(callback);
        Log.v(LOG_TAG, "in setExitSharedElementCallback");
    }*/
}
