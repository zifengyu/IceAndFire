package com.sina.dfh98.iceandfire;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.util.ArrayList;

import cn.waps.AppConnect;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;


public class MyTextView extends View {
	private Paint mPaint = new Paint();
	private String baselineCharacter;
	private float baseline; 
	private String content = "";
	private ArrayList<LineInfo> lineList = new ArrayList<LineInfo>();
	private int currentLine = 0;	
	int cpl;
	int lpp;

	private BookMark bookMark;

	public MyTextView(Context context) {
		this(context,null);
	}


	public MyTextView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}

	public MyTextView(Context context, AttributeSet attrs) {
		this(context,attrs,0);
	}

	public void setTheme(int num) {
		switch (num) {
		case 0:
			mPaint.setColor(Color.WHITE);
			setBackgroundColor(Color.BLACK);
			break;
		case 1:
			mPaint.setColor(Color.BLACK);
			setBackgroundColor(Color.WHITE);
			break;
		}
	}

	private void init() {

		mPaint.setAntiAlias(true);
		setTheme(1);
		mPaint.setStyle(Style.STROKE);
		mPaint.setTextSize(32);	

		baselineCharacter = getResources().getString(R.string.baseline_character);		
		baseline = 1.05f * mPaint.measureText(baselineCharacter);

		bookMark = new BookMark(1, 0, 0);		
		//navigate(bookMark);

	}

	public void setBookMark(BookMark bk) {
		bookMark.vol = bk.vol;
		bookMark.chapter = bk.chapter;
		bookMark.character = bk.character;		
		navigate(bookMark);
	}

	public BookMark getBookMark() {
		return bookMark;
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (event.getAction() == MotionEvent.ACTION_UP) {
			if (event.getX() > getWidth() / 2) {
				toNextPage();
			} else {
				toPreviousPage();
			}			
		}

		return true;
	}

	public void toNextPage() {
		if (currentLine + lpp < lineList.size()) {
			currentLine += lpp;
			bookMark.character = lineList.get(currentLine).startIndex;
			invalidate();
		} else {
			toNextChapter();			
		}		
	}

	public void toPreviousPage() {
		if (currentLine > 0) {
			currentLine -= lpp;
			if (currentLine < 0)
				currentLine = 0;
			bookMark.character = lineList.get(currentLine).startIndex;			
			invalidate();
		} else {
			toPreviousChapter();			
		}	
	}

	public void toNextChapter() {
		bookMark.toNextChapter();		
		navigate(bookMark);	
		if (MainActivity.score >= 20 && MainActivity.score < 100) {
			Field field;		
			try {
				field = R.array.class.getField("book" + bookMark.vol + "_titles");
				String[] titles = getResources().getStringArray(field.getInt(R.array.class));
				if (titles[bookMark.chapter].contains(getResources().getString(R.string.ad_keyword))) {

					Context context = MainActivity.getContext();
					AppConnect inst = AppConnect.getInstance(context);
					if (inst != null) {
						//inst.showPopAd(context);
						inst.showOffers(context);

					}
				}
			} catch (Exception ex) {

			}
		}

	}


	public void toPreviousChapter() {
		bookMark.toPreviousChapter();		
		navigate(bookMark);
	}


	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);		
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);		

		//canvas.drawColor(Color.BLACK);


		//int height = canvas.getHeight();
		int width = getWidth() - 5;

		int cpl = (int)(width / baseline);
		float dx = 0;
		if (cpl > 1)
			dx = (width - cpl * baseline) / 2;
		float x = dx;

		float y =  baseline;

		autoSplit();

		for (int i = currentLine; i < lineList.size(); ++i) {
			String line = lineList.get(i).lineString;
			for (int j = 0; j < line.length(); ++j) {
				String character = line.substring(j, j + 1);
				x = dx + baseline * j + (baseline - mPaint.measureText(character)) / 2;
				canvas.drawText(character, x, y, mPaint);
			}

			y += baseline;

			if (y > getHeight() - 5)
				break;

			if (i == (lineList.size() - 1)) {

			}
		}


	}

	private void autoSplit() {
		lineList.clear();
		int length = content.length();
		cpl = (int)((getWidth() - 5) / baseline);
		lpp = (int)((getHeight() - 5)/ baseline);
		int start = 0, end = 1;		
		while (start < length) {
			if (end - start == cpl || content.substring(end - 1, end).equals(" ") ) {
				lineList.add(new LineInfo(start, content.substring(start,end)));

				if (content.substring(end - 1, end).equals(" ") && lineList.get(lineList.size() - 1).lineString.trim().length() > 0)
					lineList.add(new LineInfo(end - 1, ""));
				//if (start <= currentCharacter && end > currentCharacter) {
				if (start <= bookMark.character && end > bookMark.character) {
					currentLine = (lineList.size() - 1) / lpp * lpp;
					//currentCharacter = lineList.get(currentLine).startIndex;
					//bookMark.character = lineList.get(currentLine).startIndex;
				}
				start = end;
			}
			if (end == length) {
				String line = content.substring(start,end);
				if (line.trim().length() > 0) {
					lineList.add(new LineInfo(start, line));
					if (start <= bookMark.character && end > bookMark.character) {
						currentLine = (lineList.size() - 1) / lpp * lpp;
						//currentCharacter = lineList.get(currentLine).startIndex;
						//bookMark.character = lineList.get(currentLine).startIndex;
					}
				}
				break;
			}
			++end;
		}		
	}

	public void navigate(BookMark bookMark) {
		try {
			int len = readContent(bookMark.vol, bookMark.chapter);
			if (bookMark.character < 0)
				bookMark.character = len - 1;

			//autoSplit();
			//bookMark.character = lineList.get(currentLine).startIndex;

			setTitle();

			invalidate();
		} catch (Exception e) {
			e.printStackTrace();

		}
	}

	private class LineInfo {
		public LineInfo(int index, String line) {
			startIndex = index;
			lineString = line;
		}
		public int startIndex;
		public String lineString;
	}

	private int readContent(int vol, int chapter) throws Exception {
		//Field field = R.string.class.getField("b" + vol + "_ch" + chapter);			
		//content = getResources().getString(field.getInt(R.string.class));
		if (MainActivity.getContext() != null) {			
			StringBuilder sb = new StringBuilder();
			BufferedReader reader = null;			
			try {  
				reader = new BufferedReader(new InputStreamReader(MainActivity.getContext().getAssets().open("book" + vol +"_ch" + chapter)));
				String line;
				while ((line = reader.readLine()) != null) {

					sb.append(line);
				}
			} catch (Exception e) {  
				e.printStackTrace();  
			} finally {
				if (reader != null)
					reader.close();
			}
			content = sb.toString();			
		}
		return content.length();		
	}

	public void setFontSize(int size) {
		String[] sizes = getResources().getStringArray(R.array.entry_values_fontsizes);
		mPaint.setTextSize(Integer.parseInt(sizes[size]));
		if (baselineCharacter != null)
			baseline = 1.05f * mPaint.measureText(baselineCharacter);
		invalidate();
	}

	private void setTitle() {
		Field field;		
		try {
			field = R.array.class.getField("book" + bookMark.vol + "_titles");
			String[] titles = getResources().getStringArray(field.getInt(R.array.class));			
			if (MainActivity.chapterButton != null) {
				MainActivity.chapterButton.setText(titles[bookMark.chapter]);				
			}
		} catch (Exception e) {

		}				
	}
}
