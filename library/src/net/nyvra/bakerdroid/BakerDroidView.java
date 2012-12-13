package net.nyvra.bakerdroid;

import java.util.HashMap;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.AsyncTask;
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
import android.webkit.WebSettings.LayoutAlgorithm;
import android.webkit.WebSettings.PluginState;
import android.webkit.WebStorage;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
//import android.os.Build;

/**
 * BakerDroid is the Android implementation of the the Baker framework HPub specification.
 * BakerDroidView overrides ViewPager and offers methods to easily read ebooks. It should be used as an Android widget.
 *
 */
public class BakerDroidView extends ViewPager {
	private HPubDocument mDocument;
	private Context mContext;
	private BakerDroidView mPager;
	private HPubListener mListener;
	private int mInitialPage;
	private int mCurrentItemScrolling;
	private SparseArray<View> mCurrentViews;
	private HashMap<Object, String> mJavascriptInterfaces;

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

	public HPubDocument getDocument() {
		return mDocument;
	}
	
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
				mDocument = new HPubDocument(mContext, pathToBook);
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
	
	public void setOnHpubLoadedListener(HPubListener listener) {
		mListener = listener;
	}
	
	public int getCurrentItemScrolling() {
		View view = mCurrentViews.get(getCurrentItem(), null);
		if (view != null) {
			WebView webView = (WebView) view.findViewById(R.id.webview);
			return webView.getScrollY();
		}
		return -1;
	}
	
	public void setJavascriptInterfaces(HashMap<Object, String> interfaces) {
	    mJavascriptInterfaces = interfaces;
	}
	
	public void addJavascriptInterface(Object jsInterface, String name) {
	    if (mJavascriptInterfaces == null) {
	        mJavascriptInterfaces = new HashMap<Object, String>();
	    }
	    mJavascriptInterfaces.put(jsInterface, name);
	}
	
	/**
	 * BakerDroidView Adapter
	 * 
	 * @author angelocastelanjr
	 *
	 */
	class BakerDroidAdapter extends PagerAdapter {
		BakerWebViewClient webViewCLient;
		BakerWebChromeClient webChromeClient;
		
		@SuppressLint({ "SetJavaScriptEnabled", "NewApi" })
		@Override
		public Object instantiateItem(ViewGroup container, int position) {
			View view = LayoutInflater.from(mContext).inflate(R.layout.webview, null);
			WebView webView = (WebView) view.findViewById(R.id.webview);
			webView.getSettings().setBuiltInZoomControls(true);
//			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
//				webView.getSettings().setDisplayZoomControls(false);
//			}
			webView.getSettings().setJavaScriptEnabled(true);
			webView.getSettings().setDatabaseEnabled(true);
			webView.getSettings().setDatabasePath("/data/data/" + mContext.getPackageName() + "/databases/");
			webView.getSettings().setDomStorageEnabled(true);
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
			new AsyncTask<Void, Void, Void>() {

                @Override
                protected Void doInBackground(Void... params) {
                    SharedPreferences prefs = mContext.getSharedPreferences("WebViewSettings", Context.MODE_PRIVATE);
                    if (prefs.getInt("double_tap_toast_count", 1) > 0) {
                        prefs.edit().putInt("double_tap_toast_count", 0).commit();
                    }
                    return null;
                }
			    
			}.execute();
			
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
	
	public interface HPubListener {
		public void onHPubLoaded();
		public void onPageLoaded(int position, WebView view);
		public void onPageDestroyed(int position, WebView view);
		public void onShowCustomView(View view, CustomViewCallback callback);
		public void onHideCustomView();
	}

}
