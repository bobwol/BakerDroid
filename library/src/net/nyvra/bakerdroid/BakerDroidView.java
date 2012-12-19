package net.nyvra.bakerdroid;

import java.util.HashMap;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Build;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebChromeClient.CustomViewCallback;
import android.webkit.WebSettings;
import android.webkit.WebSettings.LayoutAlgorithm;
import android.webkit.WebSettings.PluginState;
import android.webkit.WebStorage;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

/**
 * BakerDroid is the Android implementation of the the Baker framework HPub specification.
 * BakerDroidView overrides ViewPager and offers methods to easily read ebooks. It should be used as an Android widget.
 *
 */
public class BakerDroidView extends ViewPager {
    
    /**
     * The HPub document being showed.
     */
	private HPubDocument mDocument;
	
	/**
	 * The activity context.
	 */
	private Context mContext;
	
	/**
	 * A BakerDroidView reference to itself
	 */
	private BakerDroidView mPager;
	
	/**
	 * Listener used to dispatch HPub events
	 */
	private HPubListener mListener;
	
	/**
	 * Indicates the page which should be displayed first when the HPub is opened
	 */
	private int mInitialPage;
	
	/**
	 * Indicates the scrolling of the current page being displayed
	 */
	private int mCurrentItemScrolling;
	
	/**
	 * A reference to the three views being displayed in the ViewPager
	 */
	private SparseArray<View> mCurrentViews;
	
	/**
	 * A HashMap of the javascript interfaces that will be added to the WebViews
	 */
	private HashMap<Object, String> mJavascriptInterfaces;
	
	/**
     * An enum used to indicate the storage mode: assets folder or external storage.
     *
     */
    public enum StorageMode {STORAGE_ASSETS_FOLDER, STORAGE_EXTERNAL}
    
    /**
     *  The storage mode
     */
    private StorageMode mStorageMode = StorageMode.STORAGE_ASSETS_FOLDER;
    
    private boolean mToastSupressed = false;
    
    public StorageMode getStorageMode() {
        return mStorageMode;
    }
    
    public void setStorageMode(StorageMode storageMode) {
        mStorageMode = storageMode;
    }

	public BakerDroidView(Context context, AttributeSet attrs) {
		super(context, attrs);
		mContext = context;
		mPager = this;
		mCurrentViews = new SparseArray<View>();
	}
	
	public BakerDroidView(Context context) {
		super(context);
		mContext = context;
		mPager = this;
		mCurrentViews = new SparseArray<View>();
	}

	/**
	 * The method used to get the document being showed in BakerDroidView
	 * 
	 * @return The current document
	 */
	public HPubDocument getDocument() {
		return mDocument;
	}
	
	/**
	 * The method used to get the current WebView
	 * 
	 * @return The current page WebView.
	 */
	public WebView getCurrentPageWebView() {
	    if (mCurrentViews != null && mCurrentViews.get(getCurrentItem()) != null) {
	        WebView view = (WebView) mCurrentViews.get(getCurrentItem()).findViewById(R.id.webview);
	        return view;
	    } else {
	        return null;
	    }
	}

	/**
	 * Load the document, showing its content when finished.
	 * 
	 * @param pathToBook The path where the document is stored
	 * @param initialPage The page that will be showed when the document is loaded
	 * @param currentItemScrolling The Y scroll position of the initial page
	 */
	public void loadDocument(final String pathToBook, int initialPage, int currentItemScrolling) {
		mInitialPage = initialPage;
		mCurrentItemScrolling = currentItemScrolling;
		
		new AsyncTask<Void, Void, Void>() {

			@Override
			protected Void doInBackground(Void... params) {
				mDocument = new HPubDocument(mContext, pathToBook, mStorageMode);
				return null;
			}
			
			protected void onPostExecute(Void result) {
				setAdapter(new BakerDroidAdapter());
				setOffscreenPageLimit(1);
				setCurrentItem(mInitialPage);
				if (mListener != null) {
					mListener.onHPubLoaded();
				}
			};
			
		}.execute();
		
	}
	
	/**
	 * Set the HPubListener
	 * 
	 * @param listener
	 */
	public void setHpubListener(HPubListener listener) {
		mListener = listener;
	}
	
	/**
	 * 
	 * @return The current page Y scrolling
	 */
	public int getCurrentItemScrolling() {
		View view = mCurrentViews.get(getCurrentItem(), null);
		if (view != null) {
			WebView webView = (WebView) view.findViewById(R.id.webview);
			return webView.getScrollY();
		}
		return -1;
	}
	
	/**
	 * Method used to add a Javascript interface to be added to the WebViews
	 * 
	 * @param jsInterface The interface object
	 * @param name The interface name
	 */
	public void addJavascriptInterface(Object jsInterface, String name) {
	    if (mJavascriptInterfaces == null) {
	        mJavascriptInterfaces = new HashMap<Object, String>();
	    }
	    mJavascriptInterfaces.put(jsInterface, name);
	}
	
	/**
	 * Used to set a HashMap of JS interfaces
	 * 
	 * @param interfaces A HashMap with the interface objects and their names
	 */
	public void setJavascriptInterfaces(HashMap<Object, String> interfaces) {
        mJavascriptInterfaces = interfaces;
    }
	
	/**
	 * BakerDroidView Adapter
	 * 
	 * @author castelanjr
	 *
	 */
	class BakerDroidAdapter extends PagerAdapter {
		BakerWebViewClient webViewCLient;
		BakerWebChromeClient webChromeClient;
		
		@SuppressWarnings("deprecation")
        @SuppressLint({ "SetJavaScriptEnabled", "NewApi" })
		@Override
		public Object instantiateItem(ViewGroup container, int position) {
			View view = LayoutInflater.from(mContext).inflate(R.layout.webview, null);
			RelativeLayout layout = (RelativeLayout) view.findViewById(R.id.layout);
			
			Drawable drawable;
			if (mContext.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
			    drawable = Drawable.createFromPath(mDocument.getPath() + "/" + mDocument.getBackgroundLandscape());
			} else {
			    drawable = Drawable.createFromPath(mDocument.getPath() + "/" + mDocument.getBackgroundPortrait());
			}
			
			if (Build.VERSION.SDK_INT >= 16) {
			    layout.setBackground(drawable);
			} else {
			    layout.setBackgroundDrawable(drawable);
			}
			
			
			WebView webView = (WebView) view.findViewById(R.id.webview);
			webView.getSettings().setBuiltInZoomControls(false);
//			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
//				webView.getSettings().setDisplayZoomControls(false);
//			}
			webView.getSettings().setJavaScriptEnabled(true);
			webView.getSettings().setDatabaseEnabled(true);
			webView.getSettings().setDatabasePath("/data/data/" + mContext.getPackageName() + "/databases/");
			webView.getSettings().setDomStorageEnabled(true);
            webView.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
			webView.getSettings().setLoadWithOverviewMode(true);
			webView.getSettings().setUseWideViewPort(true);
			webView.getSettings().setLayoutAlgorithm(LayoutAlgorithm.NORMAL);
			webView.getSettings().setPluginState(PluginState.ON);
			webView.setInitialScale(1);
			
			webViewCLient = new BakerWebViewClient();
			webView.setWebViewClient(webViewCLient);
			
			webChromeClient = new BakerWebChromeClient();
			webView.setWebChromeClient(webChromeClient);
			
			if (mJavascriptInterfaces != null) {
    			for (Object obj : mJavascriptInterfaces.keySet()) {
    			    webView.addJavascriptInterface(obj, mJavascriptInterfaces.get(obj));
    			}
			}
			
			webView.loadUrl(mDocument.getUrlAtPosition(position));
			
			ProgressBar progress = (ProgressBar) view.findViewById(R.id.progressbar);
			webView.setTag(progress);
			mCurrentViews.put(position, view);
			container.addView(view);
			return view;
		}
		
		@Override
		public void destroyItem(ViewGroup container, int position, Object object) {
		    if (mListener != null) {
                mListener.onPageDestroyed(position, (WebView) ((View) object).findViewById(R.id.webview));
            }
			mCurrentViews.remove(position);
			((ViewPager) container).removeView((View) object);
			object = null;
		}

		@Override
		public int getCount() {
			return mDocument.getContent().size();
		}

		@Override
		public boolean isViewFromObject(View view, Object object) {
			return view == object;
		}
		
	}
	
	private class BakerWebViewClient extends WebViewClient {
		boolean alreadyLoaded = false;
		
		@Override
		public void onPageStarted(WebView view, String url, Bitmap favicon) {
			super.onPageStarted(view, url, favicon);
			
			ProgressBar progress = (ProgressBar) view.getTag();
			if (progress != null) {
				view.setVisibility(View.GONE);
				progress.setVisibility(View.VISIBLE);
			}
		}
		
		@Override
		public void onPageFinished(WebView view, String url) {
			super.onPageFinished(view, url);
			if (!mToastSupressed) {
    			new Thread(new Runnable() {
    
                    @Override
                    public void run() {
                        SharedPreferences prefs = mContext.getSharedPreferences("WebViewSettings", Context.MODE_PRIVATE);
                        if (prefs.getInt("double_tap_toast_count", 1) > 0) {
                            prefs.edit().putInt("double_tap_toast_count", 0).commit();
                        }
                    }
    			    
    			}).start();
    			mToastSupressed = true;
			}
			
			int position = mDocument.getPositionFromPage(url);
			if (position == mInitialPage) {
				if (!alreadyLoaded && mCurrentItemScrolling > 0) {
					alreadyLoaded = true;
			        StringBuilder sb = new StringBuilder("javascript:window.scrollTo(0, ");
			        sb.append(mCurrentItemScrolling);
			        sb.append("/ window.devicePixelRatio);");
			        view.loadUrl(sb.toString());
			    }
			}
			if (mListener != null) {
			    mListener.onPageLoaded(position, view);
			}
			ProgressBar progress = (ProgressBar) view.getTag();
			if (progress != null) {
				view.setVisibility(View.VISIBLE);
				progress.setVisibility(View.GONE);
			}
		}
		
		@Override
	    public boolean shouldOverrideUrlLoading(WebView view, String url) {
			int position = mDocument.getPositionFromPage(url);
	        if (position != -1) {
	            mPager.setCurrentItem(position, true);
	            return true;
	        } else {
	        	return false;
	        }
	    }
	}
	
	private class BakerWebChromeClient extends WebChromeClient {
	    
	    @Override
	    public boolean onJsAlert(WebView view, String url, String message, JsResult result) {
	        result.confirm();
            return true;
	    }
		
		@Override
		public void onShowCustomView(View view, CustomViewCallback callback) {
			super.onShowCustomView(view, callback);
			mListener.onShowCustomView(view, callback);
		}
		
		@Override
		public void onHideCustomView() {
			super.onHideCustomView();
			mListener.onHideCustomView();
		}
		
		@Override
	    public void onExceededDatabaseQuota(String url, String databaseIdentifier, long currentQuota, long estimatedSize,
	        long totalUsedQuota, WebStorage.QuotaUpdater quotaUpdater) {
	        quotaUpdater.updateQuota(estimatedSize * 2);
	    }
	}
	
	/**
	 * BakerDroidView default listener to dispatch events
	 * 
	 * @author castelanjr
	 *
	 */
	public interface HPubListener {
	    
	    /**
	     * Event dispatched when the HPub is loaded
	     */
		public void onHPubLoaded();
		
		/**
		 * Event dispatched when the page is completely loaded
		 * 
		 * @param position the page position
		 * @param view the WebView of the page
		 */
		public void onPageLoaded(int position, WebView view);
		
		/**
		 * Event dispatched when the page is destroyed
		 * 
		 * @param position the page position
		 * @param view the WebView of the page
		 */
		public void onPageDestroyed(int position, WebView view);
		
		/**
		 * Notify the host application that the current page would like to show a custom View.
		 * 
		 * @param view is the view to be shown
		 * @param callback the callback
		 */
		public void onShowCustomView(View view, CustomViewCallback callback);
		
		/**
		 * Notify the host application that the current page would like to hide the custom View.
		 */
		public void onHideCustomView();
	}

}
