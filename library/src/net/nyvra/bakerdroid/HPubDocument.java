package net.nyvra.bakerdroid;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.res.AssetManager;

public class HPubDocument {
	//Required Parameters
	public static final String P_TITLE = "title";
	public static final String P_AUTHOR = "author";
	public static final String P_URL = "url";
	public static final String P_CONTENTS = "contents";
	
	//Optional parameters
	public static final String P_HPUB = "hpub";
	public static final String P_CREATOR = "creator";
	public static final String P_PUBLISHER = "publisher";
	public static final String P_DATE = "date";
	public static final String P_ORIENTATION = "orientation";
	public static final String P_ZOOMABLE = "zoomable";
	public static final String P_COVER = "cover";
	
	//Settings
	public static final String P_BAKER_BACKGROUND_PORTRAIT = "-baker-background-image-portrait";
	public static final String P_BAKER_BACKGROUND_LANDSCAPE = "-baker-background-image-landscape";
	
	private String mPath;
	private String mTitle;
	private String[] mAuthor;
	private String mUrl;
	private List<String> mContent;
	private String mHpub;
	private String[] mCreator;
	private String mPublisher;
	private String mDate;
	private String mOrientation;
	private boolean mZoomable = false;
	private String mCover;
	private String mFile;
	private List<DocumentSetting> mSettings;
	private BakerDroidView.StorageMode mStorageMode;
	private String mBackgroundPortrait, mBackgroundLandscape;
	
	public HPubDocument(Context context, String path, BakerDroidView.StorageMode storageMode) {
	    mStorageMode = storageMode;
	    
		Writer writer = null;
			
		writer = new StringWriter();
		char[] buffer = new char[1024];
		try {
			InputStream input;
			if (mStorageMode == BakerDroidView.StorageMode.STORAGE_ASSETS_FOLDER) {
				AssetManager assetManager = context.getAssets();
				input = assetManager.open(path.concat("/book.json"));
			} else {
				input = new FileInputStream(path.concat("/book.json"));
			}
			Reader reader = new BufferedReader(new InputStreamReader(input, "UTF-8"));
			int n;
			while ((n = reader.read(buffer)) != -1) {
				writer.write(buffer, 0, n);
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		try {
			JSONObject object = new JSONObject(writer.toString());
			this.mTitle = object.getString(P_TITLE);
			this.mUrl = object.getString(P_URL);
			this.mPath = path;
			
			JSONArray array = object.getJSONArray(P_AUTHOR);
			this.mAuthor = new String[array.length()];
			for (int i = 0; i < array.length(); i++) {
				this.mAuthor[i] = array.getString(i);
			}
			
			this.mContent = new ArrayList<String>();
			array = object.getJSONArray(P_CONTENTS);
			for (int i = 0; i < array.length(); i++) {
				this.mContent.add(array.getString(i));
			}
			
			if (object.has(P_HPUB)) {
				this.mHpub = object.getString(P_HPUB);
			}
			
			if (object.has(P_CREATOR)) {
				array = object.getJSONArray(P_CREATOR);
				this.mCreator = new String[array.length()];
				for (int i = 0; i < array.length(); i++) {
					this.mCreator[i] = array.getString(i);
				}
			}
			
			if (object.has(P_PUBLISHER)) {
				this.mPublisher = object.getString(P_PUBLISHER);
			}
			
			if (object.has(P_DATE)) {
				this.mDate = object.getString(P_DATE);
			}
			
			if (object.has(P_ORIENTATION)) {
				this.mOrientation = object.getString(P_ORIENTATION);
			}
			
			if (object.has(P_ZOOMABLE)) {
				this.mZoomable = object.getBoolean(P_ZOOMABLE);
			}
			
			if (object.has(P_COVER)) {
				this.mCover = object.getString(P_COVER);
			}
			
			if (object.has(P_BAKER_BACKGROUND_LANDSCAPE)) {
			    this.setBackgroundLandscape(object.getString(P_BAKER_BACKGROUND_LANDSCAPE));
			}
			
			if (object.has(P_BAKER_BACKGROUND_PORTRAIT)) {
			    this.setBackgroundPortrait(object.getString(P_BAKER_BACKGROUND_PORTRAIT));
			}
			
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	public String getTitle() {
		return mTitle;
	}

	public String[] getAuthor() {
		return mAuthor;
	}

	public String getUrl() {
		return mUrl;
	}

	public List<String> getContent() {
		return mContent;
	}

	public String getHPup() {
		return mHpub;
	}

	public String[] getCreator() {
		return mCreator;
	}

	public String getPublisher() {
		return mPublisher;
	}

	public String getDate() {
		return mDate;
	}

	public String getOrientation() {
		return mOrientation;
	}

	public boolean isZoomable() {
		return mZoomable;
	}

	public String getCover() {
		return mCover;
	}

	public String getFile() {
		return mFile;
	}

	public List<DocumentSetting> getSettings() {
		return mSettings;
	}

	public String getPath() {
		return mPath;
	}
	
	public String getUrlAtPosition(int position) {
		if (mStorageMode == BakerDroidView.StorageMode.STORAGE_ASSETS_FOLDER) {
			return "file:///android_asset/".concat(this.getPath()).concat("/").concat(this.getContent().get(position));
		} else {
			return String.format("file:///%s/%s", new Object[] {mPath, mContent.get(position)});
		}
	}
	
	public String getFullURL(String pageName) {
		if (mStorageMode == BakerDroidView.StorageMode.STORAGE_ASSETS_FOLDER) {
			return "file:///android_asset/".concat(this.getPath()).concat("/").concat(pageName);
		} else {
			String str = String.format("file:///%s/%s", new Object[] {mPath, pageName});
			return str;
		}
	}
	
	public String getPathAtPosition(int position) {
		return mPath + "/" + this.getContent().get(position);
	}
	
	public int getPositionFromPage(String pageName) {
		for (int i = 0; i < this.getContent().size(); i++) {
			if (this.getUrlAtPosition(i).equals(pageName)) {
				return i;
			}
		}
		return -1;
	}

    public String getBackgroundPortrait() {
        return mBackgroundPortrait;
    }

    public void setBackgroundPortrait(String mBackgroundPortrait) {
        this.mBackgroundPortrait = mBackgroundPortrait;
    }

    public String getBackgroundLandscape() {
        return mBackgroundLandscape;
    }

    public void setBackgroundLandscape(String mBackgroundLandscape) {
        this.mBackgroundLandscape = mBackgroundLandscape;
    }

}
