package com.mayaswell.moviedb;

import android.app.AlertDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.widget.ViewAnimator;

import rx.Observable;
import rx.Subscriber;
import rx.functions.Action1;
import rx.functions.Func1;

import com.mayaswell.moviedb.MovieDbAPI.Configuration;
import com.mayaswell.moviedb.MovieDbAPI.MovieSet;
import com.mayaswell.moviedb.MovieDbAPI.Movie;
import com.mayaswell.moviedb.MovieDbAPI.GenreSet;
import com.mayaswell.moviedb.MovieDbAPI.Genre;
import com.mayaswell.moviedb.MovieDbAPI.TVShowSet;
import com.mayaswell.moviedb.MovieDbAPI.TVShow;
import com.mayaswell.moviedb.MovieDbAPI.Person;
import com.mayaswell.moviedb.MovieDbAPI.PersonSet;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements ItemSetAdapter.ClickHandler {

	protected static final String DISPLAY_TYPE = "displayMode";

	protected MovieDbAPI movieAPI = null;
	protected Map<Integer, String> genreIndexMovie = null;
	protected Map<Integer, String> genreIndexTV = null;

	protected LinearLayoutManager layoutManager;
	protected ItemSetAdapter itemSetAdapter;
	protected RecyclerView itemListView;

	protected Configuration configuration = null;
	protected ItemCache<Movie> movieCache = new ItemCache<Movie>();
	protected ItemCache<TVShow> tvCache = new ItemCache<TVShow>();
	protected ItemCache<Person> peopleCache = new ItemCache<Person>();
	protected int displayMode = ItemSetAdapter.Mode.MOVIE;
	protected ViewAnimator viewAnimator = null;

	protected MovieDetailViewHolder movieDetailViewHolder = null;
	protected TVDetailViewHolder tvDetailViewHolder = null;
	protected PersonDetailViewHolder personDetailViewHolder = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		viewAnimator = (ViewAnimator) findViewById(R.id.viewAnimator);
		AlphaAnimation animation = new AlphaAnimation(0.0f, 1.0f);
		animation.setDuration(2000);
		viewAnimator.setInAnimation(animation);
		animation = new AlphaAnimation(1.0f, 0.0f);
		animation.setDuration(2000);
		viewAnimator.setOutAnimation(animation);

		itemSetAdapter = new ItemSetAdapter();
		itemListView = (RecyclerView) findViewById(R.id.movieListView);
		layoutManager = new LinearLayoutManager(this);
		itemListView.setLayoutManager(layoutManager);
		itemListView.setAdapter(itemSetAdapter);
		itemListView.addOnScrollListener(new RecyclerView.OnScrollListener() {
			@Override
			public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
				super.onScrolled(recyclerView, dx, dy);
				int nItems = layoutManager.getItemCount();
				int nVisible = layoutManager.getChildCount();
				int firstItem = layoutManager.findFirstVisibleItemPosition();
				int lastItem = layoutManager.findLastCompletelyVisibleItemPosition();
				if (lastItem == nItems-1 && dy > 0) {
					checkNextItems(firstItem);
				}
			}
		});

		if (movieAPI == null) {
			String movieDbURL = getResources().getString(R.string.movieDbURL);
			String movieDbKey = getResources().getString(R.string.movieDbKey);
			movieAPI = new MovieDbAPI(movieDbURL, movieDbKey);
		}
	}

	@Override
	protected void onStart()
	{
		super.onStart();
		firstLoad();
	}

	protected void firstLoad() {
		if (genreIndexMovie == null || genreIndexTV == null || configuration == null) {
			Observable<Boolean> configRequest = movieAPI.getConfig().map(new Func1<Configuration, Boolean>() {
				@Override
				public Boolean call(Configuration config) {
					Log.d("startup", "got config");
					configuration = config;
					return true;
				}
			});
			Observable<Boolean> movieGenreRequest = movieAPI.getGenres("movie", "list").map(new Func1<GenreSet, Boolean>() {
				@Override
				public Boolean call(GenreSet genreSet) {
					Log.d("startup", "got mozi genres");
					genreIndexMovie = createGenreIndex(genreSet);
					return true;
				}
			});
			Observable<Boolean> tvGenreRequest = movieAPI.getGenres("tv", "list").map(new Func1<GenreSet, Boolean>() {
				@Override
				public Boolean call(GenreSet genreSet) {
					Log.d("startup", "got tv genres");
					genreIndexTV = createGenreIndex(genreSet);
					return true;
				}
			});
			Observable<Boolean> initialMovieRequest = fetchNextMovies();


			Observable.merge(movieGenreRequest, configRequest, tvGenreRequest)
					.concatWith(initialMovieRequest)
					.subscribe(new Subscriber<Boolean>() {
				@Override
				public void onCompleted() {
					BindingHelper.mgIndex = genreIndexMovie;
					BindingHelper.tgIndex = genreIndexTV;
					Log.d("onStart()", "initial load done");
					itemSetAdapter.clear();
					itemSetAdapter.addAll(ItemSetAdapter.Mode.MOVIE, movieCache.getList());
				}

				@Override
				public void onError(Throwable e) {
					showError("Error making initial web requests", e.getMessage());
				}

				@Override public void onNext(Boolean aBoolean) { }
			});
		}
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
		Log.d("main ", "save instance state");
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
		if (viewAnimator.getDisplayedChild() != 0) {
			viewAnimator.setDisplayedChild(0);
		} else {
			super.onBackPressed();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.main, menu);

		// Associate searchable configuration with the SearchView
		SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
		SearchView searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();
		searchView.setSearchableInfo( searchManager.getSearchableInfo(getComponentName()));
		searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
			@Override
			public boolean onQueryTextSubmit(String query) {
				startSearch(query);
				return true;
			}

			@Override
			public boolean onQueryTextChange(final String s) {
				return false;
			}
		});
		return true;
	}

	/**
	 * xxxx should be called but isn't
	 * @return
	 */
	@Override
	public boolean onSearchRequested() {
		Bundle appData = new Bundle();
		appData.putInt(DISPLAY_TYPE, displayMode);
		startSearch(null, false, appData, false);
		return true;
	}

	/**
	 * manually invoke the SearchActivity (from the OnQueryTextListener
	 * @param query
	 */
	private void startSearch(final String query) {
		// Doesn't call through onSearchRequest
		Intent intent = new Intent(this, SearchActivity.class);
		intent.putExtra(DISPLAY_TYPE, displayMode);
		intent.putExtra(SearchManager.QUERY, query);
		startActivity(intent);
	}
	@Override
	public boolean onPrepareOptionsMenu (Menu menu)
	{
		return super.onPrepareOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch (item.getItemId()) {
			case R.id.menu_show_movie:
				setDisplayMode(ItemSetAdapter.Mode.MOVIE);
				return true;
			case R.id.menu_show_tv:
				setDisplayMode(ItemSetAdapter.Mode.TV);
				return true;
			case R.id.menu_show_ppl:
				setDisplayMode(ItemSetAdapter.Mode.PPL);
				return true;
		}
		return super.onOptionsItemSelected(item);
	}

	protected void setDisplayMode(int mode) {
		if (displayMode == mode) {
			return;
		}
		displayMode = mode;
		if (viewAnimator.getDisplayedChild() != 0) {
			viewAnimator.setDisplayedChild(0);
		}
		switch (displayMode) {
			case ItemSetAdapter.Mode.MOVIE: {
				itemSetAdapter.addAll(ItemSetAdapter.Mode.MOVIE, movieCache.getList());
				if (movieCache.page == 0 || movieCache.getList().size() == 0) {
					checkNextItems(0);
				}
				break;
			}
			case ItemSetAdapter.Mode.TV: {
				itemSetAdapter.addAll(ItemSetAdapter.Mode.TV, tvCache.getList());
				if (tvCache.page == 0 || tvCache.getList().size() == 0) {
					checkNextItems(0);
				}
				break;
			}
			case ItemSetAdapter.Mode.PPL: {
				itemSetAdapter.addAll(ItemSetAdapter.Mode.PPL, peopleCache.getList());
				if (peopleCache.page == 0 || peopleCache.getList().size() == 0) {
					checkNextItems(0);
				}
				break;
			}
		}
	}

	protected void showError(String title, String message) {
		AlertDialog.Builder d = new AlertDialog.Builder(this);
		d.setTitle(title);
		d.setMessage(message);
		d.setPositiveButton("ok", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				dialog.dismiss();
			}
		});
		d.show();
	}

	protected Map<Integer, String> createGenreIndex(GenreSet genreSet) {
		Map<Integer, String> m = new HashMap<Integer, String>();
		for (Genre g: genreSet.list) {
			m.put(g.id, g.name);
		}
		return m;
	}

	protected String makePosterImageUrl(String posterPath) {
		if (posterPath == null) return "";
		MovieDbAPI.ImageConfiguration ic = configuration.images;
		String ps = ic.posterSizes.size() >= 3? ic.posterSizes.get(2):
				ic.posterSizes.size() >= 1? ic.posterSizes.get(ic.posterSizes.size()-1): "";
		String f = ic.baseUrl + ps + posterPath;
		return f;
	}

	protected String makeProfileImageUrl(String posterPath) {
		if (posterPath == null) return "";
		MovieDbAPI.ImageConfiguration ic = configuration.images;
		String ps = ic.profileSizes.size() >= 3? ic.profileSizes.get(2):
				ic.profileSizes.size() >= 1? ic.profileSizes.get(ic.profileSizes.size()-1): "";
		String f = ic.baseUrl + ps + posterPath;
		return f;
	}

	protected Action1<Throwable> onNetworkError = new Action1<Throwable>() {
		@Override
		public void call(Throwable e) {
			showError("Error fetching next from server, ", e.getMessage());
		}
	};

	/**
	 * check the cache of the current item type being displayed, and fetch the next page of data if available
	 * with the result of that, set the adapter, if that is the type still being displayed
	 * @param firstItem
	 */
	protected void checkNextItems(int firstItem) {
		switch (displayMode) {
			case ItemSetAdapter.Mode.MOVIE: {
				final int mCurrent = movieCache.getList().size();
				if (!movieCache.hasMorePages()) return;
				fetchNextMovies().subscribe(new Action1<Boolean>() {
					@Override
					public void call(Boolean aBoolean) {
						if (displayMode == ItemSetAdapter.Mode.MOVIE)
							itemSetAdapter.updateFrom(ItemSetAdapter.Mode.MOVIE, mCurrent, movieCache.getList());
					}
				}, onNetworkError);
				break;
			}
			case ItemSetAdapter.Mode.TV: {
				final int mCurrent = tvCache.getList().size();
				if (!tvCache.hasMorePages()) return;
				fetchNextTVShows().subscribe(new Action1<Boolean>() {
					@Override
					public void call(Boolean aBoolean) {
						if (displayMode == ItemSetAdapter.Mode.TV)
							itemSetAdapter.updateFrom(ItemSetAdapter.Mode.TV, mCurrent, tvCache.getList());
					}
				}, onNetworkError);
				break;
			}
			case ItemSetAdapter.Mode.PPL: {
				final int mCurrent = peopleCache.getList().size();
				if (!peopleCache.hasMorePages()) return;
				fetchNextPeople().subscribe(new Action1<Boolean>() {
					@Override
					public void call(Boolean aBoolean) {
						if (displayMode == ItemSetAdapter.Mode.PPL)
							itemSetAdapter.updateFrom(ItemSetAdapter.Mode.PPL, mCurrent, peopleCache.getList());
					}
				}, onNetworkError);
				break;
			}

		}
	}

	/**
	 * build an observable to return the next page of uncached popular movie data, mapping the return result into our
	 * cache after adjusting images to have valid paths
	 * @return
	 */
	protected Observable<Boolean> fetchNextMovies() {
		int nextPage = movieCache.page+1;
		return movieAPI.getMovies(nextPage, "popular").map(new Func1<MovieSet, Boolean>() {
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
	protected Observable<Boolean> fetchNextTVShows() {
		int nextPage = tvCache.page+1;
		return movieAPI.getTVShows(nextPage, "popular").map(new Func1<TVShowSet, Boolean>() {
			@Override
			public Boolean call(TVShowSet tvSet) {
				for (TVShow m: tvSet.list) {
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
	protected Observable<Boolean> fetchNextPeople() {
		int nextPage = peopleCache.page+1;
		return movieAPI.getPeople(nextPage, "popular").map(new Func1<PersonSet, Boolean>() {
			@Override
			public Boolean call(PersonSet pplSet) {
				for (Person m: pplSet.list) {
					m.profilePath = makeProfileImageUrl(m.profilePath);
				}
				peopleCache.add(pplSet.page, pplSet.totalPages, pplSet.list);
				Log.d("fetchNextTVShows", "got page "+pplSet.page+", "+ peopleCache.list.size());
				return true;
			}
		});
	}

	@Override
	public void onClickMovie(View v) {
		Movie m = (Movie)v.getTag();
		if (this.movieDetailViewHolder  == null) {
			movieDetailViewHolder = new MovieDetailViewHolder(viewAnimator, m);
		} else {
			movieDetailViewHolder.setToMovie(m);
		}
		viewAnimator.setDisplayedChild(movieDetailViewHolder.index);
	}

	@Override
	public void onClickTvShow(View v) {
		TVShow t = (TVShow)v.getTag();
		if (this.tvDetailViewHolder  == null) {
			tvDetailViewHolder = new TVDetailViewHolder(viewAnimator, t);
		} else {
			tvDetailViewHolder.setToTVShow(t);
		}
		viewAnimator.setDisplayedChild(tvDetailViewHolder.index);
	}

	@Override
	public void onClickPerson(View v) {
		Person p = (Person)v.getTag();
		if (this.personDetailViewHolder  == null) {
			personDetailViewHolder = new PersonDetailViewHolder(viewAnimator, p);
		} else {
			personDetailViewHolder.setToPerson(p);
		}
		viewAnimator.setDisplayedChild(personDetailViewHolder.index);
	}
}