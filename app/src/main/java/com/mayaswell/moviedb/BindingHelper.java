package com.mayaswell.moviedb;

import android.content.Context;
import android.databinding.BindingAdapter;
import android.renderscript.Int2;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.mayaswell.moviedb.MovieDbAPI.PersonItem;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

/**
 * Created by dak on 6/6/2016.
 */
public final class BindingHelper {
	public static Map<Integer, String> mgIndex = null;
	public static Map<Integer, String> tgIndex = null;

	@BindingAdapter("imageUrl")
	public static void setImageUrl(ImageView imageView, String url) {
		Log.d("imageUrl helper", "load "+url);
		Context context = imageView.getContext();
		Glide.with(context).load(url).into(imageView);
	}

	@BindingAdapter("moziGenreList")
	public static void setMoziGenreList(TextView textView, Collection<Integer> genres) {
		Log.d("moziGenreList helper", "load "+genres.size());
		if (mgIndex == null) {
			return;
		}
		String s = "";
		String sep = "";
		for (Integer i: genres) {
			String gn = mgIndex.get(i);
			if (gn != null) {
				s += sep + gn;
				sep = ", ";
			}
		}
		textView.setText(s);
	}

	@BindingAdapter("tvGenreList")
	public static void setTvGenreList(TextView textView, Collection<Integer> genres) {
		Log.d("moziGenreList helper", "load "+genres.size());
		if (tgIndex == null) {
			return;
		}
		String s = "";
		String sep = "";
		for (Integer i: genres) {
			String gn = tgIndex.get(i);
			if (gn != null) {
				s += sep + gn;
				sep = ", ";
			}
		}
		textView.setText(s);
	}

	@BindingAdapter("personFameList")
	public static void setPersonFameList(TextView textView, Collection<PersonItem> items) {
		if (items == null || items.size() == 0) {
			textView.setText("");
		}
		String s = "Known for:\n";
		for (PersonItem pi: items) {
			s += pi.title + " ("+pi.mediaType+", "+pi.releaseDate+")"+"\n";
		}
		textView.setText(s);
	}
}
