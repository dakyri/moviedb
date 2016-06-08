package com.mayaswell.moviedb;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.mayaswell.moviedb.MovieDbAPI.Movie;
import com.mayaswell.moviedb.MovieDbAPI.Person;
import com.mayaswell.moviedb.MovieDbAPI.TVShow;
import com.mayaswell.moviedb.databinding.MovieListItemBinding;
import com.mayaswell.moviedb.databinding.PersonListItemBinding;
import com.mayaswell.moviedb.databinding.TvListItemBinding;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Created by dak on 6/5/2016.
 */
public class ItemSetAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
	private int mode = Mode.MOVIE;

	public static class Mode {
		public final static int MOVIE = 0;
		public final static int TV = 1;
		public final static int PPL = 2;
	}

	public interface ClickHandler {
		void onClickMovie(View v);

		void onClickTvShow(View v);

		void onClickPerson(View v);
	}

	public static class MovieViewHolder extends RecyclerView.ViewHolder {
		public MovieListItemBinding binding;
		protected RelativeLayout main;
		private Movie movie;

		public MovieViewHolder(MovieListItemBinding v) {
			super(v.getRoot());
			binding = v;
			main = (RelativeLayout) v.getRoot();
			movie = null;
		}

		public void setToMovie(Movie movie) {
			Log.d("adapter", "bind " + movie.posterPath);
			this.movie = movie;
			binding.setMovie(movie);
			binding.itemButton.setTag(movie);
		}
	}

	public static class TvViewHolder extends RecyclerView.ViewHolder {
		public TvListItemBinding binding;
		protected RelativeLayout main;
		private TVShow tvShow;

		public TvViewHolder(TvListItemBinding v) {
			super(v.getRoot());
			binding = v;
			main = (RelativeLayout) v.getRoot();
			tvShow = null;
		}

		public void setToTvShow(TVShow tvShow) {
			Log.d("adapter", "bind " + tvShow.posterPath);
			this.tvShow = tvShow;
			binding.setTv(tvShow);
			binding.itemButton.setTag(tvShow);
		}
	}

	public static class PersonViewHolder extends RecyclerView.ViewHolder {
		public PersonListItemBinding binding;
		protected RelativeLayout main;
		private Person person;

		public PersonViewHolder(PersonListItemBinding v) {
			super(v.getRoot());
			binding = v;
			main = (RelativeLayout) v.getRoot();
			person = null;
		}

		public void setToPerson(Person p) {
			Log.d("adapter", "bind " + p.profilePath);
			this.person = p;
			binding.setPerson(p);
			binding.itemButton.setTag(p);
		}
	}


	protected ArrayList<Object> dataSet = new ArrayList<Object>();

	public void clear() {
		dataSet.clear();
		notifyDataSetChanged();
	}

	public void addAll(int mode, Collection<?> list) {
		this.mode = mode;
		dataSet.clear();
		dataSet.addAll(list);
		notifyDataSetChanged();
	}

	public void updateFrom(int mode, int mCurrent, Collection<?> list) {
		this.mode = mode;
		dataSet.clear();
		dataSet.addAll(list);
		for (int i = mCurrent; i < list.size(); i++) {
			notifyItemChanged(i);
		}
	}

	public ItemSetAdapter() {
	}

	@Override
	public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		RecyclerView.ViewHolder vh = null;
		if (viewType == Mode.MOVIE) {
			MovieListItemBinding binding = MovieListItemBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
			vh = new MovieViewHolder(binding);
		} else if (viewType == Mode.TV) {
			TvListItemBinding binding = TvListItemBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
			vh = new TvViewHolder(binding);
		} else if (viewType == Mode.PPL) {
			PersonListItemBinding binding = PersonListItemBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
			vh = new PersonViewHolder(binding);
		}
		return vh;
	}

	@Override
	public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
		if (mode == Mode.TV) {
			TVShow m = (TVShow) dataSet.get(position);
			((TvViewHolder) holder).setToTvShow(m);
		} else if (mode == Mode.PPL) {
			Person m = (Person) dataSet.get(position);
			((PersonViewHolder) holder).setToPerson(m);
		} else if (mode == Mode.MOVIE) {
			Movie m = (Movie) dataSet.get(position);
			((MovieViewHolder) holder).setToMovie(m);
		}
	}

	@Override
	public int getItemCount() {
		return dataSet.size();
	}


	@Override
	public int getItemViewType(int position) {
		return mode;
	}
}
