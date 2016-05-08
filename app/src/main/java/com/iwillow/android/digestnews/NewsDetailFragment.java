package com.iwillow.android.digestnews;


import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.style.URLSpan;
import android.text.style.UnderlineSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.exoplayer.util.Util;
import com.iwillow.android.digestnews.db.Item2ItemRealm;
import com.iwillow.android.digestnews.entity.Category;
import com.iwillow.android.digestnews.entity.Image;
import com.iwillow.android.digestnews.entity.ImageAsset;
import com.iwillow.android.digestnews.entity.Infograph;
import com.iwillow.android.digestnews.entity.Item;
import com.iwillow.android.digestnews.entity.ItemRealm;
import com.iwillow.android.digestnews.entity.Location;
import com.iwillow.android.digestnews.entity.LongRead;
import com.iwillow.android.digestnews.entity.Photo;
import com.iwillow.android.digestnews.entity.Quote;
import com.iwillow.android.digestnews.entity.SlideItem;
import com.iwillow.android.digestnews.entity.Slideshow;
import com.iwillow.android.digestnews.entity.Source;
import com.iwillow.android.digestnews.entity.StatDetail;
import com.iwillow.android.digestnews.entity.Stream;
import com.iwillow.android.digestnews.entity.StringRealmWrapper;
import com.iwillow.android.digestnews.entity.Summary;
import com.iwillow.android.digestnews.entity.TweetItemRealm;
import com.iwillow.android.digestnews.entity.TweetRealm;
import com.iwillow.android.digestnews.entity.Video;
import com.iwillow.android.digestnews.entity.WikiRealm;
import com.iwillow.android.digestnews.http.RxNewsParser;
import com.iwillow.android.digestnews.util.ReferenceUtil;
import com.iwillow.android.digestnews.util.TweetTransformer;
import com.iwillow.android.digestnews.util.URLSpanNoUnderline;
import com.iwillow.android.lib.log.LogUtil;
import com.iwillow.android.lib.util.DimensionUtil;
import com.iwillow.android.lib.view.DonutProgress;
import com.iwillow.android.lib.widget.BaseFragment;

import java.util.ArrayList;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmAsyncTask;
import io.realm.RealmList;
import io.realm.RealmResults;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;


/**
 * Created by https://www.github.com/iwillow on 2016/5/3.
 * <p/>
 * A simple {@link Fragment} subclass.
 * Use the {@link NewsDetailFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class NewsDetailFragment extends BaseFragment {
    private static final String TAG = NewsDetailFragment.class.getSimpleName();

    private static final String UUID = "uuid";
    private static final String COLOR = "color";
    private static final String INDEX = "index";
    private Subscription subscription;
    private String uuid;
    private int color;
    private int index;
    private Realm realm;
    private RealmAsyncTask asyncTransaction;

    private DonutProgress donutProgress;
    private TextView lable;
    private TextView title;
    private ViewGroup summary;
    private ViewGroup statDetail;
    private ViewGroup infographs;
    private ViewGroup longreads;
    private ViewGroup locations;
    private ViewGroup slideshow;
    private ViewGroup videos;
    private ViewGroup wikis;
    private ViewGroup tweets;
    private ViewGroup references;
    private ViewGroup gallery;
    private ImageView singleImage;
    private TextView referCount;
    private ScrollView scrollView;
    private Typeface typefaceBold;
    private Typeface typefaceLight;
    private Typeface typefaceThin;

    public NewsDetailFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param uuid  Parameter 1.
     * @param color Parameter 2.
     * @return A new instance of fragment NewsDetailFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static NewsDetailFragment newInstance(String uuid, int color, int index) {
        NewsDetailFragment fragment = new NewsDetailFragment();
        Bundle args = new Bundle();
        args.putString(UUID, uuid);
        args.putInt(COLOR, color);
        args.putInt(INDEX, index);
        fragment.setArguments(args);
        LogUtil.d(TAG, "uuid:" + uuid);
        return fragment;
    }

    public Subscription loadFromDataBase() {


        return realm.where(ItemRealm.class)
                .equalTo("id", uuid)
                .findAllSortedAsync("published")
                .asObservable()
                .onBackpressureBuffer()
                .distinct()
                .filter(new Func1<RealmResults<ItemRealm>, Boolean>() {
                    @Override
                    public Boolean call(RealmResults<ItemRealm> itemRealms) {
                        boolean result = itemRealms != null && itemRealms.size() > 0;
                        LogUtil.d(TAG, "result1:" + result);
                        return result;
                    }
                })
                .map(new Func1<RealmResults<ItemRealm>, ItemRealm>() {
                    @Override
                    public ItemRealm call(RealmResults<ItemRealm> itemRealms) {
                        LogUtil.d(TAG, "result2:" + itemRealms.get(0));
                        return itemRealms.get(0);
                    }
                })
              /*  .filter(new Func1<ItemRealm, Boolean>() {
                    @Override
                    public Boolean call(ItemRealm itemRealm) {

                        boolean result = itemRealm != null
                                && itemRealm.getMultiSummary() != null
                                && itemRealm.getMultiSummary().size() > 0;
                        LogUtil.d(TAG, "result3:" + result);

                        return result;
                    }
                })*/
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<ItemRealm>() {
                    @Override
                    public void onCompleted() {
                        LogUtil.d(TAG, "load news' UUID:" + uuid + " from database completed");
                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();

                    }

                    @Override
                    public void onNext(ItemRealm itemRealm) {
                        LogUtil.d(TAG, " onNext load news' UUID:" + uuid + " from database ");
                        if (itemRealm != null && itemRealm.getMultiSummary() != null
                                && itemRealm.getMultiSummary().size() > 0) {
                            final ItemRealm item = itemRealm;
                            // item.setChecked(true);
                            cancelAsyncTransaction();
                            asyncTransaction = realm.executeTransactionAsync(new Realm.Transaction() {
                                @Override
                                public void execute(Realm realm) {

                                    //realm.copyToRealmOrUpdate(item);
                                }
                            }, new Realm.Transaction.OnSuccess() {
                                @Override
                                public void onSuccess() {
                                    initItem(item);
                                }
                            }, new Realm.Transaction.OnError() {
                                @Override
                                public void onError(Throwable error) {
                                    error.printStackTrace();
                                }
                            });

                        } else {
                            subscription.unsubscribe();
                            subscription = loadFromNetwork();
                        }
                    }
                });
    }

    public Subscription loadFromNetwork() {

        return RxNewsParser
                .getNewsContent(uuid)
                .onBackpressureBuffer()
                .filter(new Func1<List<Item>, Boolean>() {
                    @Override
                    public Boolean call(List<Item> items) {
                        return items != null && items.size() > 0;
                    }
                })
                .map(new Func1<List<Item>, ItemRealm>() {
                    @Override
                    public ItemRealm call(List<Item> items) {
                        return Item2ItemRealm.convert(items.get(0));
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<ItemRealm>() {
                    @Override
                    public void onCompleted() {
                        LogUtil.d(TAG, "load news' UUID:" + uuid + " from internet completed");
                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                    }

                    @Override
                    public void onNext(ItemRealm itemRealm) {
                        final ItemRealm item = itemRealm;
                        if (item != null && item.getMultiSummary() != null && item.getMultiSummary().size() > 0) {
                            // item.setChecked(true);
                            cancelAsyncTransaction();
                            asyncTransaction = realm.executeTransactionAsync(new Realm.Transaction() {
                                @Override
                                public void execute(Realm realm) {

                                    realm.copyToRealmOrUpdate(item);
                                }
                            }, new Realm.Transaction.OnSuccess() {
                                @Override
                                public void onSuccess() {
                                    initItem(item);
                                }
                            }, new Realm.Transaction.OnError() {
                                @Override
                                public void onError(Throwable error) {
                                    error.printStackTrace();
                                }
                            });

                        } else {
                            LogUtil.d(TAG, "data is null");
                        }

                    }
                });
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            uuid = getArguments().getString(UUID);
            color = getArguments().getInt(COLOR);
            index = getArguments().getInt(INDEX);
        }
        realm = Realm.getDefaultInstance();
    }


    @Override
    protected int getLayoutId() {
        return R.layout.fragment_news_detail;
    }

    @Override
    protected void initView(View rootView) {
        scrollView = $(rootView, R.id.scrollView);
        donutProgress = $(rootView, R.id.index);
        if (index == -1) {
            donutProgress.setVisibility(View.GONE);
        } else {
            donutProgress.setText("" + index);
            donutProgress.setTextColor(color);
            donutProgress.setInnerBackgroundColor(Color.TRANSPARENT);
            donutProgress.setFinishedStrokeColor(color);
            donutProgress.setUnfinishedStrokeColor(color);
        }
        lable = $(rootView, R.id.label);
        lable.setTextColor(color);
        typefaceBold = Typeface.createFromAsset(getContext().getAssets(), "fonts/Roboto-Bold.ttf");
        typefaceLight = Typeface.createFromAsset(getContext().getAssets(), "fonts/Roboto-Light.ttf");
        typefaceThin = Typeface.createFromAsset(getContext().getAssets(), "fonts/Roboto-Thin.ttf");
        lable.setTypeface(typefaceBold);
        title = $(rootView, R.id.title);
        title.setTypeface(typefaceLight);
        summary = $(rootView, R.id.summary);
        summary.removeAllViews();
        statDetail = $(rootView, R.id.statDetail);
        statDetail.removeAllViews();
        infographs = $(rootView, R.id.infographs);
        infographs.removeAllViews();
        longreads = $(rootView, R.id.longreads);
        longreads.removeAllViews();
        locations = $(rootView, R.id.locations);
        locations.removeAllViews();
        slideshow = $(rootView, R.id.slideshow);
        slideshow.removeAllViews();
        videos = $(rootView, R.id.videos);
        videos.removeAllViews();
        wikis = $(rootView, R.id.wikis);
        wikis.removeAllViews();
        tweets = $(rootView, R.id.tweets);
        tweets.removeAllViews();
        references = $(rootView, R.id.sources);
        references.removeAllViews();

        referCount = $(rootView, R.id.referCount);
        // referCount.setTypeface(typefaceThin);
        singleImage = $(rootView, R.id.singleImage);
        gallery = $(rootView, R.id.gallery);
        subscription = loadFromNetwork();

    }

    private void initItem(ItemRealm itemRealm) {


        if (itemRealm != null && itemRealm != null && itemRealm.getMultiSummary() != null) {

            if (index != -1 && itemRealm.isChecked()) {
                donutProgress.setInnerBackgroundColor(color);
                donutProgress.setTextColor(Color.WHITE);
            }

            //label
            RealmList<Category> categories = itemRealm.getCategories();
            String labl = categories == null || categories.isEmpty() ? "World" : categories.get(0).getLabel();
            lable.setText(labl);

            //title
            title.setText("" + itemRealm.getTitle());

            //quote
            summary.removeAllViews();
            RealmList<Summary> summaries = itemRealm.getMultiSummary();
            if (summaries != null && !summaries.isEmpty()) {
                for (Summary item : summaries) {
                    View summaryItemView =
                            LayoutInflater.from(summary.getContext()).inflate(R.layout.item_summary, summary, false);
                    TextView textView = $(summaryItemView, R.id.summaryText);
                    textView.setTypeface(typefaceLight);

                    ViewGroup quoteContainer = $(summaryItemView, R.id.quoteContainer);
                    textView.setText(item.getText());
                    if (item.getQuote() == null || item.getQuote().getText() == null) {
                        quoteContainer.removeAllViews();
                    } else {
                        Quote quote = item.getQuote();
                        TextView quoteSymbol = $(quoteContainer, R.id.quoteSymbol);
                        quoteSymbol.setTextColor(color);
                        TextView quoteText = $(quoteContainer, R.id.quoteText);
                        quoteText.setTextColor(color);
                        // quoteText.setTypeface(typefaceLight);
                        quoteText.setText(quote.getText());
                        TextView quoteSource = $(quoteContainer, R.id.quoteSource);
                        quoteSource.setText(quote.getSource());
                        //quoteSource.setTypeface(typefaceBold);
                        View verticalLine = $(quoteContainer, R.id.verticalLine);
                        verticalLine.setBackgroundColor(color);
                    }
                    summary.addView(summaryItemView);
                }
            }

            //statics
            statDetail.removeAllViews();
            RealmList<StatDetail> statDetails = itemRealm.getStatDetail();
            if (statDetails != null && !statDetails.isEmpty()) {

                for (StatDetail stat : statDetails) {

                    View staticItemView =
                            LayoutInflater.from(statDetail.getContext()).inflate(R.layout.item_statics, statDetail, false);
                    TextView statDetailTitle = $(staticItemView, R.id.statDetailTitle);
                    statDetailTitle.setText(stat.getTitle().getText());
                    statDetailTitle.setTextColor(color);


                    TextView statDetailValue = $(staticItemView, R.id.statDetailValue);
                    statDetailValue.setText(stat.getValue().getText());
                    statDetailValue.setTypeface(typefaceLight);

                    TextView statDetailUnits = $(staticItemView, R.id.statDetailUnits);
                    statDetailUnits.setText(stat.getUnits().getText());
                    statDetailUnits.setTypeface(typefaceLight);

                    TextView statDetailDescription = $(staticItemView, R.id.statDetailDescription);
                    statDetailDescription.setText(stat.getDescription().getText());
                    statDetailDescription.setTypeface(typefaceLight);

                    statDetail.addView(staticItemView);

                }

            }


            //infographs
            infographs.removeAllViews();

            RealmList<Infograph> infographList = itemRealm.getInfographs();
            if (infographList != null && infographList.size() > 0) {
                for (Infograph infograph : infographList) {

                    View infographItemView =
                            LayoutInflater.from(infographs.getContext()).inflate(R.layout.item_infograph, infographs, false);

                    TextView infographTitle = $(infographItemView, R.id.infographTitle);
                    infographTitle.setText(infograph.getTitle());

                    TextView infographCaption = $(infographItemView, R.id.infographCaption);
                    infographCaption.setText(infograph.getTitle());

                    ImageView infographImg = $(infographItemView, R.id.infographImg);

                    String src = getImageSource(infograph.getImages());
                    Glide.with(NewsDetailFragment.this).load(src).centerCrop().crossFade().into(infographImg);

                    infographs.addView(infographItemView);
                }
            }


            //longreads
            longreads.removeAllViews();
            RealmList<LongRead> longReads = itemRealm.getLongreads();
            if (longReads != null && !longReads.isEmpty()) {
                for (LongRead longRead : longReads) {

                    View longReadItemView =
                            LayoutInflater.from(longreads.getContext()).inflate(R.layout.item_topic_in_depth, longreads, false);


                    Image image = longRead.getImages();

                    String src = getImageSource(image);

                    if (src != null && src.length() > 0) {
                        ImageView longreadImg = $(longReadItemView, R.id.longreadImg);

                        Glide.with(NewsDetailFragment.this).load(src).centerCrop().crossFade().into(longreadImg);
                    }


                    TextView longreadTitle = $(longReadItemView, R.id.longreadTitle);

                    longreadTitle.setText(longRead.getTitle());
                    longreadTitle.setTextColor(color);
                    //longreadTitle.setTypeface(typefaceBold);

                    TextView longreadPublisher = $(longReadItemView, R.id.longreadPublisher);
                    longreadPublisher.setText(longRead.getPublisher());
                    //longreadPublisher.setTypeface(typefaceBold);


                    TextView longreadDescription = $(longReadItemView, R.id.longreadDescription);
                    longreadDescription.setText(longRead.getDescription());
                    longreadDescription.setTypeface(typefaceLight);
                    if (!TextUtils.isEmpty(longRead.getUrl())) {
                        final String depthUrl = longRead.getUrl();
                        longReadItemView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Uri uri = Uri.parse(depthUrl);
                                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                                startActivity(intent);
                            }
                        });


                    }

                    longreads.addView(longReadItemView);

                }
            }

            //locations
            locations.removeAllViews();
            RealmList<Location> locationList = itemRealm.getLocations();
            if (locationList != null && !locationList.isEmpty()) {
                for (Location location : locationList) {

                    View locationItemView =
                            LayoutInflater.from(locations.getContext()).inflate(R.layout.item_location, locations, false);
                    TextView caption = $(locationItemView, R.id.caption);
                    caption.setText(location.getCaption());
                    // caption.setTypeface(typefaceBold);
                    locations.addView(locationItemView);
                }
            }

            //slideshow

            Slideshow slideshow1 = itemRealm.getSlideshow();
            if (slideshow1 != null && slideshow1.getPhotos() != null && slideshow1.getPhotos().size() > 0) {
                RealmList<Photo> photos = slideshow1.getPhotos();
                if (photos.size() == 1) {
                    singleImage.setVisibility(View.VISIBLE);
                    String src = photos.get(0).getImages().getUrl();
                    Glide.with(NewsDetailFragment.this).load(src).centerCrop().crossFade().into(singleImage);
                    gallery.setVisibility(View.GONE);
                    final ArrayList<SlideItem> slideItems = new ArrayList<SlideItem>();
                    SlideItem slideItem = new SlideItem();
                    slideItem.caption = photos.get(0).getCaption();
                    slideItem.headline = photos.get(0).getHeadline();
                    slideItem.provider_name = photos.get(0).getProvider_name();
                    slideItem.url = src;
                    slideItems.add(slideItem);
                    singleImage.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent(v.getContext(), SlideShowActivity.class);
                            intent.putParcelableArrayListExtra(SlideShowActivity.DATA, slideItems);
                            intent.putExtra(SlideShowActivity.CURRENT_INDEX, 0);
                            startActivity(intent);
                        }
                    });

                } else {
                    singleImage.setVisibility(View.GONE);
                    slideshow.removeAllViews();

                    final ArrayList<SlideItem> slideItems = new ArrayList<SlideItem>();
                    for (Photo photo : photos) {
                        SlideItem slideItem = new SlideItem();
                        slideItem.caption = photos.get(0).getCaption();
                        slideItem.headline = photos.get(0).getHeadline();
                        slideItem.provider_name = photos.get(0).getProvider_name();
                        slideItem.url = photo.getImages().getUrl();
                        slideItems.add(slideItem);
                    }
                    int currentItem = 0;
                    for (Photo photo : photos) {

                        View photoItemView =
                                LayoutInflater.from(slideshow.getContext()).inflate(R.layout.item_image, slideshow, false);

                        ImageView imageView = $(photoItemView, R.id.photo);
                        final int index = currentItem;
                        imageView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent intent = new Intent(v.getContext(), SlideShowActivity.class);
                                intent.putParcelableArrayListExtra(SlideShowActivity.DATA, slideItems);
                                intent.putExtra(SlideShowActivity.CURRENT_INDEX, index);
                                startActivity(intent);
                            }
                        });

                        //  String src = getImageSource(photo.getImages());
                        String src = photo.getImages().getUrl();
                        Glide.with(NewsDetailFragment.this).load(src).centerCrop().crossFade().into(imageView);

                        slideshow.addView(photoItemView);
                        currentItem++;
                    }
                }

            }

            //videos
            videos.removeAllViews();
            RealmList<Video> videoList = itemRealm.getVideos();
            if (videoList != null && !videoList.isEmpty()) {
                for (Video video : videoList) {

                    View videoItemView =
                            LayoutInflater.from(videos.getContext()).inflate(R.layout.item_video, videos, false);
                    TextView title = $(videoItemView, R.id.title);
                    title.setText("" + video.getTitle());
                    //title.setTypeface(typefaceBold);
                    ImageView playIcon = $(videoItemView, R.id.playIcon);
                    ShapeDrawable shapeDrawable = new ShapeDrawable(new OvalShape());
                    shapeDrawable.getPaint().setColor(color);
                    shapeDrawable.getPaint().setStyle(Paint.Style.FILL);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                        playIcon.setBackground(shapeDrawable);
                    }

                    ImageView imageView = $(videoItemView, R.id.thumbnail);
                    Glide.with(NewsDetailFragment.this).load(video.getThumbnail()).centerCrop().crossFade().into(imageView);
                    RealmList<Stream> streams = video.getStreams();
                    if (streams != null && streams.size() > 0) {
                        String url = null;
                        for (Stream stream : streams) {
                            if (!TextUtils.isEmpty(stream.getUrl())
                                    && !TextUtils.isEmpty(stream.getMime_type())
                                    && !"application/vnd.apple.mpegurl".equalsIgnoreCase(stream.getMime_type())
                                    ) {
                                url = stream.getUrl();
                                break;
                            }
                        }
                        if (url != null) {
                            final String src = url;
                            videoItemView.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    Intent mpdIntent = new Intent(v.getContext(), MediaPlayerActivity.class);
                                    mpdIntent.setData(Uri.parse(src));
                                    mpdIntent.putExtra(MediaPlayerActivity.CONTENT_TYPE_EXTRA, Util.TYPE_OTHER);
                                    mpdIntent.putExtra(MediaPlayerActivity.CONTENT_ID_EXTRA, src);
                                    mpdIntent.putExtra(MediaPlayerActivity.PROVIDER_EXTRA, "");
                                    startActivity(mpdIntent);
                                }
                            });

                        }
                    }

                    videos.addView(videoItemView);


                }

            }

            //wiki
            wikis.removeAllViews();
            RealmList<WikiRealm> wikiRealms = itemRealm.getWikis();
            if (wikiRealms != null && !wikiRealms.isEmpty()) {

                for (WikiRealm wikiRealm : wikiRealms) {

                    View wikiItemView =
                            LayoutInflater.from(wikis.getContext()).inflate(R.layout.item_wiki, wikis, false);

                    TextView wikiTitle = $(wikiItemView, R.id.wikiTitle);
                    wikiTitle.setText("" + wikiRealm.getTitle());
                    // wikiTitle.setTypeface(typefaceBold);
                    TextView wikiText = $(wikiItemView, R.id.wikiText);
                    wikiText.setText("" + wikiRealm.getText());
                    wikiText.setTypeface(typefaceLight);
                    TextView searchTerms = $(wikiItemView, R.id.searchTerms);
                    StringBuilder sb = new StringBuilder();
                    for (StringRealmWrapper sw : wikiRealm.getSearchTerms()) {
                        sb.append(sw.getValue()).append("");
                    }

                    searchTerms.setText("learn more:" + sb.toString());

                    searchTerms.setTextColor(color);
                    searchTerms.setTypeface(typefaceLight);
                    if (!TextUtils.isEmpty(wikiRealm.getUrl())) {
                        final String wikiUrl = wikiRealm.getUrl();
                        wikiItemView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Uri uri = Uri.parse(wikiUrl);
                                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                                startActivity(intent);
                            }
                        });
                    }

                    wikis.addView(wikiItemView);

                }
            }

            //tweet
            tweets.removeAllViews();
            TweetRealm tweetRealm = itemRealm.getTweets();
            if (tweetRealm != null && tweetRealm.getTweets() != null
                    && tweetRealm.getTweets().size() > 0) {
                RealmList<TweetItemRealm> tweetItemRealms = tweetRealm.getTweets();
                for (TweetItemRealm tweetItemRealm : tweetItemRealms) {

                    View tweetItemView =
                            LayoutInflater.from(tweets.getContext()).inflate(R.layout.item_twitter, tweets, false);

                    TextView tweetName = $(tweetItemView, R.id.tweetName);
                    tweetName.setText("" + tweetItemRealm.getUser().getName());
                    // tweetName.setTypeface(typefaceBold);

                    TextView tweetScreenName = $(tweetItemView, R.id.tweetScreenName);
                    //  tweetScreenName.setTypeface(typefaceBold);
                    StringBuilder sb = new StringBuilder();
                    sb.append("<a href=\"https://mobile.twitter.com/").append(tweetItemRealm.getUser().getScreen_name()).append("\">");
                    sb.append("@").append(tweetItemRealm.getUser().getScreen_name()).append("<a>");
                    tweetScreenName.setText(Html.fromHtml(sb.toString()));
                    tweetScreenName.setMovementMethod(LinkMovementMethod.getInstance());
                    tweetScreenName.setLinkTextColor(Color.parseColor("#FF95CEFB"));
                    URLSpanNoUnderline.stripUnderlines(tweetScreenName);

                    TextView tweetTime = $(tweetItemView, R.id.tweetTime);
                    tweetTime.setText("11h");
                    // tweetTime.setTypeface(typefaceBold);

                    TextView tweetText = $(tweetItemView, R.id.tweetText);
                    tweetText.setTypeface(typefaceLight);
                    String text = tweetItemRealm.getText();
                    Spanned s = Html.fromHtml(TweetTransformer.convert(text));
                    tweetText.setText(s);
                    tweetText.setLinkTextColor(Color.parseColor("#FF95CEFB"));
                    tweetText.setMovementMethod(LinkMovementMethod.getInstance());
                    URLSpanNoUnderline.stripUnderlines(tweetText);

                    if (!TextUtils.isEmpty(tweetItemRealm.getId())) {

                        final String replyUrl = "https://twitter.com/intent/tweet?in_reply_to=" + tweetItemRealm.getId();
                        ImageView twitterReply = $(tweetItemView, R.id.twitterReply);
                        twitterReply.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Uri uri = Uri.parse(replyUrl);
                                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                                startActivity(intent);
                            }
                        });

                        final String retweet = "https://twitter.com/intent/retweet?tweet_id=" + tweetItemRealm.getId() + "&related=twitterapi,twittermedia,twitter,support";
                        ImageView twitterRetweet = $(tweetItemView, R.id.twitterRetweet);
                        twitterRetweet.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Uri uri = Uri.parse(retweet);
                                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                                startActivity(intent);
                            }
                        });

                        final String favoriteUrl = "https://twitter.com/intent/favorite?tweet_id=" + tweetItemRealm.getId();
                        ImageView twitterFavorite = $(tweetItemView, R.id.twitterFavorite);
                        twitterFavorite.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Uri uri = Uri.parse(favoriteUrl);
                                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                                startActivity(intent);
                            }
                        });


                    }


                    tweets.addView(tweetItemView);

                }


            }

            RealmList<Source> sources = itemRealm.getSources();

            referCount.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (references.getVisibility() == View.GONE) {
                        references.setVisibility(View.VISIBLE);
                        references.post(new Runnable() {
                            @Override
                            public void run() {
                                scrollView.fullScroll(ScrollView.FOCUS_DOWN);
                            }
                        });
                    } else if (references.getVisibility() == View.VISIBLE) {
                        references.setVisibility(View.GONE);

                    }
                }
            });

            if (sources == null || sources.size() == 0) {
                referCount.setVisibility(View.GONE);
            } else if (sources.size() == 1) {
                referCount.setText("1 Reference");

                final View referencesItemView =
                        LayoutInflater.from(references.getContext()).inflate(R.layout.item_source, references, false);
                TextView publisher = $(referencesItemView, R.id.publisher);
                //publisher.setTypeface(typefaceBold);
                publisher.setText(sources.get(0).getPublisher());
                ViewGroup titleContainer = $(referencesItemView, R.id.titleContainer);

                View referencestitleItemView =
                        LayoutInflater.from(titleContainer.getContext()).inflate(R.layout.item_source_title, titleContainer, false);
                View view = $(referencestitleItemView, R.id.dot);
                ShapeDrawable shapeDrawable = new ShapeDrawable(new OvalShape());
                shapeDrawable.getPaint().setColor(color);
                shapeDrawable.getPaint().setStyle(Paint.Style.FILL);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    view.setBackground(shapeDrawable);
                }
                TextView sourceTitle = $(referencestitleItemView, R.id.sourceTitle);
                sourceTitle.setTypeface(typefaceLight);
                StringBuilder sb = new StringBuilder();
                sb.append("<a href=\"").append(sources.get(0).getUrl()).append("\">").append(sources.get(0).getTitle()).append("</a>");
                sourceTitle.setText(Html.fromHtml(sb.toString()));
                sourceTitle.setLinkTextColor(Color.BLACK);
                sourceTitle.setMovementMethod(LinkMovementMethod.getInstance());
                URLSpanNoUnderline.stripUnderlines(sourceTitle);
                titleContainer.addView(referencestitleItemView);

                references.addView(referencesItemView);

            } else {
                referCount.setText(sources.size() + " References");

                ReferenceUtil.
                        groupSource(sources)
                        .subscribe(new Subscriber<Source>() {
                            String lastPublisher = "the elder";
                            List<List<Source>> bucketList = new ArrayList<>();
                            List<Source> bucket = null;

                            @Override
                            public void onCompleted() {

                                bucketList.add(bucket);

                                for (List<Source> list : bucketList) {

                                    final View referencesItemView =
                                            LayoutInflater.from(references.getContext()).inflate(R.layout.item_source, references, false);
                                    TextView publisher = $(referencesItemView, R.id.publisher);
                                    publisher.setText(list.get(0).getPublisher());
                                    // publisher.setTypeface(typefaceBold);
                                    ViewGroup titleContainer = $(referencesItemView, R.id.titleContainer);
                                    //title
                                    for (Source source : list) {
                                        View referencestitleItemView =
                                                LayoutInflater.from(titleContainer.getContext()).inflate(R.layout.item_source_title, titleContainer, false);
                                        View view = $(referencestitleItemView, R.id.dot);
                                        ShapeDrawable shapeDrawable = new ShapeDrawable(new OvalShape());
                                        shapeDrawable.getPaint().setColor(color);
                                        shapeDrawable.getPaint().setStyle(Paint.Style.FILL);
                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                                            view.setBackground(shapeDrawable);
                                        }
                                        TextView sourceTitle = $(referencestitleItemView, R.id.sourceTitle);
                                        sourceTitle.setTypeface(typefaceLight);
                                        StringBuilder sb = new StringBuilder();
                                        sb.append("<a href=\"").append(source.getUrl()).append("\">").append(source.getTitle()).append("</a>");
                                        sourceTitle.setText(Html.fromHtml(sb.toString()));
                                        sourceTitle.setLinkTextColor(Color.BLACK);
                                        sourceTitle.setMovementMethod(LinkMovementMethod.getInstance());
                                        URLSpanNoUnderline.stripUnderlines(sourceTitle);
                                        titleContainer.addView(referencestitleItemView);
                                        // LogUtil.d(TAG, "---------onCompleted getPublisher: " + source.getPublisher() + " -----");
                                    }
                                    //LogUtil.d(TAG, "---------onCompleted round-----");

                                    references.addView(referencesItemView);
                                }
                            }

                            @Override
                            public void onError(Throwable e) {
                                e.printStackTrace();
                            }

                            @Override
                            public void onNext(Source source) {
                                if (!lastPublisher.equalsIgnoreCase(source.getPublisher())) {
                                    if (bucket == null) {
                                        bucket = new ArrayList<Source>();
                                        bucket.add(source);
                                    } else {
                                        bucketList.add(bucket);
                                        bucket = new ArrayList<Source>();
                                        bucket.add(source);
                                    }
                                    lastPublisher = source.getPublisher();
                                } else {
                                    bucket.add(source);
                                }

                            }
                        });
            }


        }


    }

    private String getImageSource(Image image) {

        String src = null;
        if (image != null) {
            src = image.getUrl();
            RealmList<ImageAsset> imageAssets = image.getImage_assets();
            for (ImageAsset imageAsset : imageAssets) {
                if ("pc:size=square".equalsIgnoreCase(imageAsset.getTag()) || "pc:size=square_large".equalsIgnoreCase(imageAsset.getTag())) {
                    src = imageAsset.getUrl();
                    break;

                }
                src = imageAsset.getUrl();
            }

        }
        return src;

    }

    private void cancelAsyncTransaction() {
        if (asyncTransaction != null && !asyncTransaction.isCancelled()) {
            asyncTransaction.cancel();
            asyncTransaction = null;
        }
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        realm.close();
        cancelAsyncTransaction();
        if (subscription != null
                && !subscription.isUnsubscribed()) {
            subscription.unsubscribe();
        }
    }
}
