package com.example.xyzreader.ui;

import android.animation.Animator;
import android.app.ActivityOptions;
import android.app.LoaderManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.Loader;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.support.v7.widget.Toolbar;
import android.text.format.DateUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.xyzreader.R;
import com.example.xyzreader.data.ArticleLoader;
import com.example.xyzreader.data.ItemsContract;
import com.example.xyzreader.data.UpdaterService;
import com.squareup.picasso.Picasso;

/**
 * An activity representing a list of Articles. This activity has different presentations for
 * handset and tablet-size devices. On handsets, the activity presents a list of items, which when
 * touched, lead to a {@link ArticleDetailActivity} representing item details. On tablets, the
 * activity presents a grid of items as cards.
 */
public class ArticleListActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<Cursor> {

    private Toolbar mToolbar;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private RecyclerView mRecyclerView;
    private boolean mIsRefreshing = false;
    Adapter mAdapter = null;
    public static final String TRANSITION_NAME = null;
    private String mTransitionName;
    Context mContext;
    int mItemPosition;
    StaggeredGridLayoutManager sglm;
    Parcelable mListState;

    final String LIST_POSITION="list_item_position";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_article_list);
       /* if(savedInstanceState!=null)    {
            if(savedInstanceState.get("POSITION")!=null)
            Log.v(ArticleListActivity.class.getSimpleName(),"position is"+savedInstanceState.get("POSITION"));
        }*/


        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);


        mToolbar = (Toolbar) findViewById(R.id.toolbar);

        mContext = this;
        //       final View toolbarContainerView = findViewById(R.id.toolbar_container);

        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh_layout);


        mRecyclerView = (RecyclerView) findViewById(R.id.recycler_view);


        mAdapter = new Adapter(null);
        mRecyclerView.setAdapter(mAdapter);

        int columnCount = getResources().getInteger(R.integer.list_column_count);
        sglm =
                new StaggeredGridLayoutManager(columnCount, StaggeredGridLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(sglm);
        getLoaderManager().initLoader(0, null, this);

        if (sharedPreferences.contains(ArticleDetailFragment.POSITION)) {
            // Log.v(ArticleListActivity.class.getSimpleName(),"position is"+ getIntent().getLongExtra("POSITION",0));
            mItemPosition = sharedPreferences.getInt(ArticleDetailFragment.POSITION, 0);

        }
        if (savedInstanceState == null) {
            refresh();
        }
        else
        {
            if(savedInstanceState.get(LIST_POSITION)!=null)
            {
                mListState = savedInstanceState.getBundle(LIST_POSITION);
             //   sglm.onRestoreInstanceState(mListState);
            }
        }
    }

    private void refresh() {
        startService(new Intent(this, UpdaterService.class));
    }

    @Override
    protected void onStart() {
        super.onStart();
        registerReceiver(mRefreshingReceiver,
                new IntentFilter(UpdaterService.BROADCAST_ACTION_STATE_CHANGE));
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        mListState = sglm.onSaveInstanceState();
        outState.putParcelable(LIST_POSITION, mListState);
        super.onSaveInstanceState(outState);
    }


    @Override
    protected void onStop() {
        super.onStop();
        unregisterReceiver(mRefreshingReceiver);
    }


    private BroadcastReceiver mRefreshingReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (UpdaterService.BROADCAST_ACTION_STATE_CHANGE.equals(intent.getAction())) {
                mIsRefreshing = intent.getBooleanExtra(UpdaterService.EXTRA_REFRESHING, false);
                updateRefreshingUI();
            }
        }
    };

    private void updateRefreshingUI() {
        mSwipeRefreshLayout.setRefreshing(mIsRefreshing);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return ArticleLoader.newAllArticlesInstance(this);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        /*Adapter adapter = new Adapter(cursor);
        adapter.setHasStableIds(true);
        mRecyclerView.setAdapter(adapter);*/
        mAdapter.swapCursor(cursor);
    /*    int columnCount = getResources().getInteger(R.integer.list_column_count);
        StaggeredGridLayoutManager sglm =
                new StaggeredGridLayoutManager(columnCount, StaggeredGridLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(sglm);*/
        if(mListState==null)
        {
            mRecyclerView.smoothScrollToPosition(mItemPosition);
        }
        else
        {
            mRecyclerView.smoothScrollToPosition(0);
        }

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // mRecyclerView.setAdapter(null);
        mAdapter.swapCursor(null);
    }



    @Override
    protected void onResume() {
        super.onResume();
        if (mListState != null) {
            sglm.onRestoreInstanceState(mListState);
        }
    }

    private class Adapter extends RecyclerView.Adapter<ViewHolder> {
        private Cursor mCursor;

        public Adapter(Cursor cursor) {
            mCursor = cursor;
        }

        @Override
        public long getItemId(int position) {
            mCursor.moveToPosition(position);
            return mCursor.getLong(ArticleLoader.Query._ID);
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = getLayoutInflater().inflate(R.layout.list_item_article, parent, false);
            final ViewHolder vh = new ViewHolder(view);
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(Intent.ACTION_VIEW,
                            ItemsContract.Items.buildItemUri(getItemId(vh.getAdapterPosition())), ArticleListActivity.this, ArticleDetailActivity.class);
                    intent.putExtra(ArticleDetailFragment.POSITION, vh.getAdapterPosition());
                    ActivityOptionsCompat transitionActivityOptions = ActivityOptionsCompat.makeSceneTransitionAnimation(ArticleListActivity.this, vh.thumbnailView, getString(R.string.image_resource) + "_" + vh.getAdapterPosition());
                    startActivity(intent, transitionActivityOptions.toBundle());
                }
            });
            return vh;
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, int position) {

            mCursor.moveToPosition(position);

            holder.titleView.setText(mCursor.getString(ArticleLoader.Query.TITLE));

            holder.subtitleView.setText(
                    DateUtils.getRelativeTimeSpanString(
                            mCursor.getLong(ArticleLoader.Query.PUBLISHED_DATE),
                            System.currentTimeMillis(), DateUtils.HOUR_IN_MILLIS,
                            DateUtils.FORMAT_ABBREV_ALL).toString()
                            + " by "
                            + mCursor.getString(ArticleLoader.Query.AUTHOR));
           /*holder.thumbnailView.setImageUrl(
                    mCursor.getString(ArticleLoader.Query.THUMB_URL),
                    ImageLoaderHelper.getInstance(ArticleListActivity.this).getImageLoader());*/
            Picasso.with(mContext).load(mCursor.getString(ArticleLoader.Query.THUMB_URL)).into(holder.thumbnailView);
            holder.thumbnailView.setAspectRatio(mCursor.getFloat(ArticleLoader.Query.ASPECT_RATIO));

            mTransitionName = getString(R.string.image_resource) + "_" + position;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                holder.thumbnailView.setTransitionName(mTransitionName);
            }
        /*    holder.view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {


                    Intent intent = new Intent(Intent.ACTION_VIEW,
                            ItemsContract.Items.buildItemUri(getItemId(holder.getAdapterPosition())),ArticleListActivity.this, ArticleDetailActivity.class);

                    intent.putExtra(TRANSITION_NAME,mTransitionName);
                    ActivityOptionsCompat transitionActivityOptions = ActivityOptionsCompat.makeSceneTransitionAnimation(ArticleListActivity.this, holder.thumbnailView, mTransitionName);
                    startActivity(intent, transitionActivityOptions.toBundle());
                }
            });*/
                       /*startActivity(new Intent(Intent.ACTION_VIEW,
                            ItemsContract.Items.buildItemUri(getItemId(holder.getAdapterPosition()))));*/
            //     String transition = holder.titleView.getText().toString();
            // intent.setAction(Intent.ACTION_VIEW);
            //  View sharedView = blueIconImageView;


        }

        @Override
        public int getItemCount() {
            if (null == mCursor) return 0;
            return mCursor.getCount();
        }

        public void swapCursor(Cursor cursor) {
            mCursor = cursor;
            notifyDataSetChanged();
        }
    }
    //  }

    public class ViewHolder extends RecyclerView.ViewHolder {
        //  public DynamicHeightNetworkImageView thumbnailView;
        public AspectRatioImageViewer thumbnailView;
        public TextView titleView;
        public TextView subtitleView;
        //  public final View view;

        public ViewHolder(View view) {
            super(view);
            //    this.view = view;
            thumbnailView = (AspectRatioImageViewer) view.findViewById(R.id.thumbnail);
            titleView = (TextView) view.findViewById(R.id.article_title);
            subtitleView = (TextView) view.findViewById(R.id.article_subtitle);

        }
    }
}
