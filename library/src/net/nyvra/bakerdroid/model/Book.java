package net.nyvra.bakerdroid.model;

import java.io.BufferedReader;
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

public class Book {
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
	private List<Setting> mSettings;
	
	
	//NOTE: just a test
	public Book(Context context, String path) {
		AssetManager assetManager = context.getAssets();
		Writer writer = new StringWriter();
		char[] buffer = new char[1024];
		try {
			InputStream input = assetManager.open(path.concat("/book.json"));
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
			this.mTitle = object.getString("title");
			this.mUrl = object.getString("url");
			this.mPath = path;
			
			JSONArray authorArray = object.getJSONArray("author");
			this.mAuthor = new String[authorArray.length()];
			for (int i = 0; i < authorArray.length(); i++) {
				this.mAuthor[i] = authorArray.getString(i);
			}
			
			this.mContent = new ArrayList<String>();
			JSONArray contentArray = object.getJSONArray("contents");
			for (int i = 0; i < contentArray.length(); i++) {
				this.mContent.add(contentArray.getString(i));
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	public String getTitle() {
		return mTitle;
	}

	public void setTitle(String mTitle) {
		this.mTitle = mTitle;
	}

	public String[] getAuthor() {
		return mAuthor;
	}

	public void setAuthor(String[] mAuthor) {
		this.mAuthor = mAuthor;
	}

	public String getUrl() {
		return mUrl;
	}

	public void setUrl(String mUrl) {
		this.mUrl = mUrl;
	}

	public List<String> getContent() {
		return mContent;
	}

	public void setContent(List<String> mContent) {
		this.mContent = mContent;
	}

	public String getHPup() {
		return mHpub;
	}

	public void setHPup(String mHPup) {
		this.mHpub = mHPup;
	}

	public String[] getCreator() {
		return mCreator;
	}

	public void setCreator(String[] mCreator) {
		this.mCreator = mCreator;
	}

	public String getPublisher() {
		return mPublisher;
	}

	public void setPublisher(String mPublisher) {
		this.mPublisher = mPublisher;
	}

	public String getDate() {
		return mDate;
	}

	public void setDate(String mDate) {
		this.mDate = mDate;
	}

	public String getOrientation() {
		return mOrientation;
	}

	public void setOrientation(String mOrientation) {
		this.mOrientation = mOrientation;
	}

	public boolean isZoomable() {
		return mZoomable;
	}

	public void setZoomable(boolean mZoomable) {
		this.mZoomable = mZoomable;
	}

	public String getCover() {
		return mCover;
	}

	public void setCover(String mCover) {
		this.mCover = mCover;
	}

	public String getFile() {
		return mFile;
	}

	public void setFile(String mFile) {
		this.mFile = mFile;
	}

	public List<Setting> getSettings() {
		return mSettings;
	}

	public void setSettings(List<Setting> mSettings) {
		this.mSettings = mSettings;
	}

	public String getPath() {
		return mPath;
	}

	public void setPath(String mPath) {
		this.mPath = mPath;
	}
	
	public String getUrlAtPosition(int position) {
		return "file:///android_asset/".concat(this.getPath()).concat("/").concat(this.getContent().get(position));
	}

}
