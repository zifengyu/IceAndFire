package com.sina.dfh98.iceandfire;

import java.lang.reflect.Field;

import cn.waps.AppConnect;
import cn.waps.UpdatePointsNotifier;

import com.sina.dfh98.iceandfire.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Color;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;


public class MainActivity extends Activity implements UpdatePointsNotifier {

	private static Context mContext;

	private static final String MY_PREFS = "com.sina.dfh98.iceandfire.preference";
	private SharedPreferences mySharedPreferences;
	MyTextView textView;
	public static int score;	
	private int fontSize;
	private int currentBook;

	AlertDialog fontSizeDialog;
	//AlertDialog chapterDialog;
	//public static ActionBar actionBar;
	public static Button chapterButton;

	private BookMark[] bookMarks = new BookMark[5];

	private int colorTheme;

	public static Context getContext(){  
		return mContext;  
	}  

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mContext = this;
		//actionBar = getActionBar();


		mySharedPreferences = getSharedPreferences(MY_PREFS, Activity.MODE_PRIVATE);
		loadProperties();	

		setContentView(R.layout.main);	

		chapterButton = (Button) findViewById(R.id.button_chapter);
		chapterButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				BookMark bk = textView.getBookMark();	

				Field field;		
				try {
					field = R.array.class.getField("book" + bk.vol + "_titles");
					int chapId = field.getInt(R.array.class);

					field = R.string.class.getField("book" + bk.vol + "_name");
					int volId = field.getInt(R.string.class);

					AlertDialog chapterDialog = new AlertDialog.Builder(v.getContext())
					.setTitle(getResources().getString(volId))
					.setSingleChoiceItems(chapId, bk.chapter, new OnClickListener(){
						@Override
						public void onClick(DialogInterface dialog, int which) {
							dialog.dismiss();
							BookMark bk = textView.getBookMark();	
							if (which != bk.chapter) {					
								textView.setBookMark(new BookMark(bk.vol, which, 0));
							}

						}})
						.create();

					chapterDialog.show();
				} catch (Exception ex) {

				}
			}
		});

		textView = (MyTextView)findViewById(R.id.myView);
		textView.setFontSize(fontSize);
		textView.setBookMark(loadBookMark());				

		fontSizeDialog = new AlertDialog.Builder(this)
		.setTitle(getResources().getString(R.string.config_fontsize))
		.setSingleChoiceItems(R.array.entries_fontsizes, fontSize, new OnClickListener(){
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
				if (which != fontSize) {					
					fontSize = which; 
					textView.setFontSize(fontSize);
				}

			}})
			.create();

		setColorTheme(colorTheme);

		if (score > 20) {
			AppConnect.getInstance(this);
		} else {
			++score;
		}
		if (score >= 20 && score < 100) {
			AppConnect inst = AppConnect.getInstance(this);
			if (inst != null) {
				inst.getPoints(this);
				inst.initPopAd(this);
			}
		}
		
		AppConnect inst = AppConnect.getInstance(this);
		if (inst != null) {
			//inst.showPopAd(context);
			inst.showOffers(this);

		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	protected void onStart() {
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
				WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

		super.onResume();
	}

	@Override
	protected void onStop() {		
		saveBookMark();
		saveProperties();
		super.onStop();		
	}

	@Override
	protected void onDestroy() {
		//Release WAPS
		if (score > 20) {
			AppConnect.getInstance(this).finalize();
		}		
		super.onDestroy();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		AppConnect inst;
		switch (item.getItemId()) {	
		case R.id.menu_book1:
			saveBookMark2(currentBook, textView.getBookMark());
			currentBook = 0;
			textView.setBookMark(bookMarks[currentBook]);
			break;
		case R.id.menu_book2:
			saveBookMark2(currentBook, textView.getBookMark());
			currentBook = 1;
			textView.setBookMark(bookMarks[currentBook]);
			break;
		case R.id.menu_book3:
			saveBookMark2(currentBook, textView.getBookMark());
			currentBook = 2;
			textView.setBookMark(bookMarks[currentBook]);
			break;
		case R.id.menu_book4:
			saveBookMark2(currentBook, textView.getBookMark());
			currentBook = 3;
			textView.setBookMark(bookMarks[currentBook]);
			break;
		case R.id.menu_book5:
			saveBookMark2(currentBook, textView.getBookMark());
			currentBook = 4;
			textView.setBookMark(bookMarks[currentBook]);
			break;			
		case R.id.menu_theme_config:
			colorTheme = 1 - colorTheme;
			setColorTheme(colorTheme);
			break;
		case R.id.menu_fontsize_config:
			fontSizeDialog.show();
			break;
		case R.id.menu_feedback:
			inst = AppConnect.getInstance(this);
			if (inst != null)
				inst.showFeedback();
			break;
		}
		return true;
	}

	private void saveBookMark2(int index, BookMark bookMark) {
		bookMarks[index].chapter = bookMark.chapter;
		bookMarks[index].character = bookMark.character;		
	}

	private BookMark loadBookMark() {
		if (mySharedPreferences != null) {
			for (int i = 0; i < 5; ++i) {
				int vol = i + 1;
				int chapter = mySharedPreferences.getInt("bookmark_chapter" + i, 0);
				int character = mySharedPreferences.getInt("bookmark_character" + i, 0);
				bookMarks[i] = new BookMark(vol, chapter, character);
			}
			currentBook = mySharedPreferences.getInt("current_book", 0);
		}		
		return bookMarks[currentBook];
	}

	private void saveBookMark() {
		if (textView != null && mySharedPreferences != null) {
			bookMarks[currentBook] = textView.getBookMark();
			Editor editor = mySharedPreferences.edit();
			editor.putInt("current_book", currentBook);

			for (int i = 0; i < 5; ++i) {			
				editor.putInt("bookmark_chapter" + i, bookMarks[i].chapter);
				editor.putInt("bookmark_character" + i, bookMarks[i].character);
			}
			editor.commit();
		}
	}

	private void loadProperties() {
		if (mySharedPreferences != null) {
			score = mySharedPreferences.getInt("score", 0);
			fontSize = mySharedPreferences.getInt("font_size", 2);
			colorTheme = mySharedPreferences.getInt("color_theme", 0);
		}
	}

	private void saveProperties() {
		if (mySharedPreferences != null) {
			Editor editor = mySharedPreferences.edit();
			editor.putInt("score", score);
			editor.putInt("font_size", fontSize);
			editor.putInt("color_theme", colorTheme);
			editor.commit();
		}
	}

	@Override
	public void getUpdatePoints(String arg0, int arg1) {
		score = arg1;
		//System.out.println(arg0 + ":" + arg1);

	}

	@Override
	public void getUpdatePointsFailed(String arg0) {
		//System.out.println(arg0);

	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		int action = event.getAction();
		if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
			if (action == KeyEvent.ACTION_DOWN)  
				textView.toNextPage();

			return true;

		} else if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
			if (action == KeyEvent.ACTION_DOWN)
				textView.toPreviousPage();

			return true;

		} else {

			return super.onKeyDown(keyCode, event);

		}
	}

	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {	

		if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN || keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
			return true;
		} 
		return super.onKeyUp(keyCode, event);
	}

	private void setColorTheme(int num) {
		textView.setTheme(num);

		switch (num) {
		case 0:
			chapterButton.setTextColor(Color.WHITE);			
			chapterButton.setBackgroundColor(Color.BLACK);
			break;
		case 1:
			chapterButton.setTextColor(Color.WHITE);			
			chapterButton.setBackgroundColor(Color.BLACK);
			break;
		}

	}




}