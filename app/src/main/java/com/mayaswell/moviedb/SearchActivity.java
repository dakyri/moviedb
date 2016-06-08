package com.mayaswell.moviedb;

import android.app.SearchManager;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.mayaswell.moviedb.MovieDbAPI.Configuration;
import com.mayaswell.moviedb.MovieDbAPI.GenreSet;
import com.mayaswell.moviedb.MovieDbAPI.Movie;
import com.mayaswell.moviedb.MovieDbAPI.MovieSet;

import rx.Observable;
import rx.Subscriber;
import rx.functions.Func1;

public class SearchActivity extends MainActivity {

	private String currentQuery;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		handleIntent(getIntent());
	}

	@Override
	protected void onStart()
	{
		super.onStart();
	}

	@Override
	protected void onResume()
	{
		super.onResume();
	}

	/**
	 *  Another activity is taking focus (this activity is about to be "paused").
	 */
	@Override
	protected void onPause()
	{
		super.onPause();
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
	}

	@Override
	protected void onStop()
	{
		super.onStop();
	}

	@Override
	protected void onDestroy()
	{
		super.onDestroy();
	}

	@Override
	public void onBackPressed()
	{
		super.onBackPressed();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onPrepareOptionsMenu (Menu menu)
	{
		return super.onPrepareOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		return super.onOptionsItemSelected(item);
	}
	@Override
	protected void onNewIntent(Intent intent) {
		handleIntent(intent);
	}

	private void handleIntent(Intent intent) {
		if (intent.hasExtra(DISPLAY_TYPE)) {
			displayMode = intent.getIntExtra(DISPLAY_TYPE, 0);
			Log.d("search", "got bundle "+displayMode);
		}
		if (intent.hasExtra(SearchManager.QUERY)) {
			String query = intent.getStringExtra(SearchManager.QUERY);

			Log.d("Search", "Search for " + query);
			currentQuery = query;
		} else {
			showError("Error", "No query string on search");
		}
		refreshSearch();
		checkNextItems(0);
	}

	private void refreshSearch() {
		movieCache.clear();
		peopleCache.clear();
		tvCache.clear();
	}

	@Override
	protected void firstLoad() {
		if (genreIndexMovie == null || genreIndexTV == null || configuration == null) {
			Observable<Boolean> configRequest = movieAPI.getConfig().map(new Func1<Configuration, Boolean>() {
				@Override
				public Boolean call(Configuration config) {
					configuration = config;
					return true;
				}
			});
			Observable<Boolean> movieGenreRequest = movieAPI.getGenres("movie", "list").map(new Func1<GenreSet, Boolean>() {
				@Override
				public Boolean call(GenreSet genreSet) {
					genreIndexMovie = createGenreIndex(genreSet);
					return true;
				}
			});
			Observable<Boolean> tvGenreRequest = movieAPI.getGenres("tv", "list").map(new Func1<GenreSet, Boolean>() {
				@Override
				public Boolean call(GenreSet genreSet) {
					genreIndexTV = createGenreIndex(genreSet);
					return true;
				}
			});
			Observable<Boolean> searchDBRequest;
			switch (displayMode) {
				case ItemSetAdapter.Mode.PPL:
					searchDBRequest = fetchNextPeople();
					break;
				case ItemSetAdapter.Mode.TV:
					searchDBRequest = fetchNextTVShows();
					break;
				case ItemSetAdapter.Mode.MOVIE:
				default:
					searchDBRequest = fetchNextMovies();
					break;
			}


			Observable.merge(movieGenreRequest, configRequest, tvGenreRequest)
					.concatWith(searchDBRequest)
					.subscribe(new Subscriber<Boolean>() {
						@Override
						public void onCompleted() {
							BindingHelper.mgIndex = genreIndexMovie;
							BindingHelper.tgIndex = genreIndexTV;
							Log.d("searchstart()", "initial load done");
							itemSetAdapter.clear();
							switch (displayMode) {
								case ItemSetAdapter.Mode.PPL:
									itemSetAdapter.addAll(ItemSetAdapter.Mode.PPL, peopleCache.getList());
									break;
								case ItemSetAdapter.Mode.TV:
									itemSetAdapter.addAll(ItemSetAdapter.Mode.TV, tvCache.getList());
									break;
								case ItemSetAdapter.Mode.MOVIE:
								default:
									itemSetAdapter.addAll(ItemSetAdapter.Mode.MOVIE, movieCache.getList());
									break;
							}
							;
						}

						@Override
						public void onError(Throwable e) {
							showError("Error making initial web requests", e.getMessage());
						}

						@Override public void onNext(Boolean aBoolean) { }
					});
		}
	}
	/**
	 * build an observable to return the next page of uncached popular movie data, mapping the return result into our
	 * cache after adjusting images to have valid paths
	 * @return
	 */
	@Override
	protected Observable<Boolean> fetchNextMovies() {
		int nextPage = movieCache.page+1;
		return movieAPI.searchMovies(nextPage, currentQuery).map(new Func1<MovieSet, Boolean>() {
			@Override
			public Boolean call(MovieSet movieSet) {
				for (Movie m: movieSet.list) {
					m.posterPath = makePosterImageUrl(m.posterPath);
				}
				movieCache.add(movieSet.page, movieSet.totalPages, movieSet.list);
				Log.d("fetchNextMovies", "got page "+movieSet.page+", "+ movieCache.list.size());
				return true;
			}
		});
	}

	/**
	 * ... likewise with tv shows
	 * @return
	 */
	@Override
	protected Observable<Boolean> fetchNextTVShows() {
		int nextPage = tvCache.page+1;
		return movieAPI.searchTVShows(nextPage, currentQuery).map(new Func1<MovieDbAPI.TVShowSet, Boolean>() {
			@Override
			public Boolean call(MovieDbAPI.TVShowSet tvSet) {
				for (MovieDbAPI.TVShow m: tvSet.list) {
					m.posterPath = makePosterImageUrl(m.posterPath);
				}
				tvCache.add(tvSet.page, tvSet.totalPages, tvSet.list);
				Log.d("fetchNextTVShows", "got page "+tvSet.page+", "+ tvCache.list.size());
				return true;
			}
		});
	}

	/**
	 * ... likewise with people
	 * @return
	 */
	@Override
	protected Observable<Boolean> fetchNextPeople() {
		int nextPage = peopleCache.page+1;
		return movieAPI.searchPeople(nextPage, currentQuery).map(new Func1<MovieDbAPI.PersonSet, Boolean>() {
			@Override
			public Boolean call(MovieDbAPI.PersonSet pplSet) {
				for (MovieDbAPI.Person m: pplSet.list) {
					m.profilePath = makeProfileImageUrl(m.profilePath);
				}
				peopleCache.add(pplSet.page, pplSet.totalPages, pplSet.list);
				Log.d("fetchNextTVShows", "got page "+pplSet.page+", "+ peopleCache.list.size());
				return true;
			}
		});
	}


}