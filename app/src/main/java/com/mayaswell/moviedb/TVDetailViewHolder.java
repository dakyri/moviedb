package com.mayaswell.moviedb;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.mayaswell.moviedb.databinding.TvDetailBinding;

/**
 * Created by dak on 6/8/2016.
 */
public class TVDetailViewHolder {
	private final RelativeLayout main;
	public final int index;
	public TvDetailBinding binding;
	private MovieDbAPI.TVShow tvShow;

	public TVDetailViewHolder(ViewGroup parent, MovieDbAPI.TVShow m) {
		binding = TvDetailBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
		main = (RelativeLayout) binding.getRoot();
		parent.addView(main);
		index = parent.getChildCount()-1;
		Log.d("holder", "index count "+index);
		setToTVShow(m);
	}

	public void setToTVShow(MovieDbAPI.TVShow movie) {
		Log.d("adapter", "bind " + movie.posterPath);
		this.tvShow = movie;
		binding.setTv(movie);
	}

}
