package com.mayaswell.moviedb;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.RelativeLayout;

import com.mayaswell.moviedb.databinding.MovieDetailBinding;

/**
 * Created by dak on 6/8/2016.
 */
public class MovieDetailViewHolder {
	private final RelativeLayout main;
	public final int index;
	public MovieDetailBinding binding;
	private MovieDbAPI.Movie movie;

	public MovieDetailViewHolder(ViewGroup parent, MovieDbAPI.Movie m) {
		binding = MovieDetailBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
		main = (RelativeLayout) binding.getRoot();
		parent.addView(main);
		index = parent.getChildCount()-1;
		setToMovie(m);
	}

	public void setToMovie(MovieDbAPI.Movie movie) {
		this.movie = movie;
		binding.setMovie(movie);
	}

}
