package com.mayaswell.moviedb;

import android.support.annotation.NonNull;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.SerializedName;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.ArrayList;

import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * Created by dak on 6/5/2016.
 *
 * we're throwing runtimeexceptions as errors so that error states will pass back up our rx chain
 *
 * make api calls to the moviedb api, returning the results as rx Observables of various structures.
 * class also defines most of the basic models provided by the api
 * api is returning json, which is parsed using Gson, based on introspection on these structures
 */
public class MovieDbAPI {

	/**
	 * the image part of the base configuration for the moviedb api
	 */
	public static class ImageConfiguration {
	@SerializedName("base_url")
		public String baseUrl;
	@SerializedName("secure_base_url")
		public String secureBaseUrl;
	@SerializedName("poster_sizes")
		public ArrayList<String> posterSizes = new ArrayList<String>();
	@SerializedName("profile_sizes")
		public ArrayList<String> profileSizes = new ArrayList<String>();
	@SerializedName("still_sizes")
		public ArrayList<String> stillSizes = new ArrayList<String>();
	@SerializedName("logo_sizes")
		public ArrayList<String> logoSizes = new ArrayList<String>();
	@SerializedName("backdrop_sizes")
		public ArrayList<String> backdropSizes = new ArrayList<String>();
	}

	/**
	 * overall configuration object returned
	 */
	public static class Configuration {
		public ImageConfiguration images;
	}

	/**
	 * wrapper around the return result of a collection of movies, broken into pages
	 */
	public static class MovieSet {
		public int page = 0;
	@SerializedName("total_pages")
		public int totalPages = 0;
	@SerializedName("results")
		public ArrayList<Movie> list = new ArrayList<Movie>();
	}

	/**
	 * wrapper around the return result of a collection of tv shows, broken into pages
	 */
	public static class TVShowSet {
		public int page = 0;
	@SerializedName("total_pages")
		public int totalPages = 0;
	@SerializedName("results")
		public ArrayList<TVShow> list = new ArrayList<>();
	}

	/**
	 * wrapper around the return result of a collection of persons, broken into pages
	 */
	public static class PersonSet {
		public int page = 0;
	@SerializedName("total_pages")
		public int totalPages = 0;
	@SerializedName("results")
		public ArrayList<Person> list = new ArrayList<>();
	}

	/**
	 * wrapper around the return result of a collection of genre codes and corresponding strings , unpaged
	 */
	public static class GenreSet {
	@SerializedName("genres")
		public ArrayList<Genre> list = new ArrayList<>();
	}

	/**
	 * base data for a movie query. returned by search requests and also 'popular'
	 */
	public static class Movie {
		public boolean adult;
	@SerializedName("backdrop_path")
		public String backdropPath;
	@SerializedName("genre_ids")
		public ArrayList<Integer> genreIds = new ArrayList<Integer>();
		public int id;
	@SerializedName("original_language")
		public String language;
	@SerializedName("original_title")
		public String originalTitle;
		public String overview;
	@SerializedName("release_date")
		public String releaseDate;
	@SerializedName("poster_path")
		public String posterPath;
		public float popularity;
		public String title;
		public boolean video;
	@SerializedName("vote_average")
		public float voteAverage;
	@SerializedName("vote_count")
		public int voteCount;

		public boolean equals(Object m) {
			return this == m || (m instanceof Movie && ((Movie) m).id == id);
		}
	}

	/**
	 * base data for a tv show query. returned by search requests and also 'popular'
	 */
	public static class TVShow {
	@SerializedName("backdrop_path")
		public String backdropPath;
	@SerializedName("first_air_date")
		public String firstAirDate;
	@SerializedName("genre_ids")
		public ArrayList<Integer> genreIds = new ArrayList<Integer>();
		public int id;
	@SerializedName("original_language")
		public String language;
	@SerializedName("original_name")
		public String originalName;
		public String overview;
	@SerializedName("origin_country")
		public ArrayList<String> originCountry = new ArrayList<String>();
	@SerializedName("poster_path")
		public String posterPath;
		public float popularity;
		public String name;
	@SerializedName("vote_average")
		public float voteAverage;
	@SerializedName("vote_count")
		public int voteCount;

		public boolean equals(Object m) {
			return this == m || (m instanceof TVShow && ((TVShow) m).id == id);
		}

	}

	/**
	 * base data for an item of interest from a person query. a list of these with each person
	 */
	public static class PersonItem {
		public boolean adult;
	@SerializedName("backdrop_path")
		public String backdropPath;
		public int id;
	@SerializedName("original_title")
		public String originalTitle;
	@SerializedName("release_date")
		public String releaseDate;
	@SerializedName("poster_path")
		public String posterPath;
		public float popularity;
		public String title;
	@SerializedName("vote_average")
		public float voteAverage;
	@SerializedName("vote_count")
		public int voteCount;
	@SerializedName("media_type")
		public String mediaType;
	}

	/**
	 * base data for a person query. returned by search requests and also 'popular'
	 */
	public static class Person {
		public boolean adult;
		public int id;
		public float popularity;
		public String name;
	@SerializedName("profile_path")
		public String profilePath;
	@SerializedName("known_for")
		public ArrayList<PersonItem> knownFor = new ArrayList<PersonItem>();

		public boolean equals(Object m) {
			return this == m || (m instanceof Person && ((Person) m).id == id);
		}
	}

	/**
	 * genre mapping, a code and a name basically
	 */
	public static class Genre {
		public int id;
		public String name;
	}

	/**
	 * status response: if we get a 200 ok, but something else not so kosher with our query
	 */
	public static class StatusResponse {
	@SerializedName("status_code")
		int code;
	@SerializedName("status_message")
		String message;

		public StatusResponse() {
			code = 0;
			message = "";
		}
	}

	private final String key;
	private final String url;
	private final OkHttpClient client;

	/**
	 * do the basic mapping of an okhttp response into a string ... we're assuming the amount of data
	 * is reasonable (which it is for moviedb api), so .string() is a reasonable way to grab all data
	 */
	private Func1<Response, String> responseBodyMapper = new Func1<Response, String>() {
		@Override
		public String call(Response response) {
			int responseCode = response.code();
			Log.d("getMovies", "got response, code "+responseCode);
			if (responseCode != HttpURLConnection.HTTP_OK) {
				throw new RuntimeException("Bad response code "+responseCode);
			}
			String responseBody = "";
			try {
				responseBody = response.body().string();
				response.body().close();
			} catch (IOException e) {
				response.body().close();
				throw new RuntimeException("IO Exception getting response body"+e.getMessage());
			} catch (Exception e) {
				response.body().close();
				throw new RuntimeException("Unexpected Exception getting response body "+e.getClass().toString());
			}

			// we try to convert the response body to a status message. if we can, we throw this as an error
			// otherwise, we let the chaing continue
			Gson gson = new GsonBuilder().create();
			StatusResponse errorResponse = gson.fromJson(responseBody, StatusResponse.class);
			if (errorResponse != null && errorResponse.code != 0 && errorResponse.message != "") {
				throw new RuntimeException("Error status from server: "+errorResponse.message);
			}
			return responseBody;
		}

	};

	public MovieDbAPI(String u, String k) {
		this.url = u;
		this.key = k;
		client = new OkHttpClient();
	}

	/**
	 * create the basic observable of an okhttp response
	 * @param okRequest
	 * @return
	 */
	@NonNull
	private Observable<Response> createObservable(final Request okRequest) {
		return Observable.create(new Observable.OnSubscribe<Response>() {

			@Override
			public void call(Subscriber<? super Response> subscriber) {
				Log.d("MovieDbAPI", "created observervable on "+okRequest.toString()+" on "+Thread.currentThread().getId());
				try {
					Response response = client.newCall(okRequest).execute();
					subscriber.onNext(response);
					if (!response.isSuccessful()) {
						subscriber.onError(new Exception("error"));
					} else {
						subscriber.onCompleted();
					}
				} catch (IOException e) {
					subscriber.onError(e);
				}
			}
		});
	}

	public Observable<Configuration> getConfig() {
		Request okRequest = fetchRequest("configuration");
		Observable<Response> observable = createObservable(okRequest);
		return observable
				.subscribeOn(Schedulers.newThread())
				.map(responseBodyMapper)
				.map(new Func1<String, Configuration>() {
					@Override
					public Configuration call(String responseBody) {
						return parseConfiguration(responseBody);
					}
				})
				.observeOn(AndroidSchedulers.mainThread());
	}

	public Observable<MovieSet> getMovies(String ... mPath) {
		return getMovies(-1, mPath);
	}

	public Observable<MovieSet> searchMovies(int page, String query) {
		return getMovies(searchRequest(page, "movie", query));
	}
	public Observable<MovieSet> getMovies(int page, String ... mPath) {
		return getMovies(fetchRequest(page, "movie", mPath));
	}
	public Observable<MovieSet> getMovies(Request okRequest) {
		Observable<Response> observable = createObservable(okRequest);
		return observable
				.subscribeOn(Schedulers.newThread())
				.map(responseBodyMapper)
				.map(new Func1<String, MovieSet>() {
					@Override
					public MovieSet call(String responseBody) {
						return parseMovieSet(responseBody);
					}
				})
				.observeOn(AndroidSchedulers.mainThread());
	}

	public Observable<TVShowSet> searchTVShows(int page, String query) {
		return getTVShows(searchRequest(page, "tv", query));
	}
	public Observable<TVShowSet> getTVShows(int page, String ... mPath) {
		return getTVShows(fetchRequest(page, "tv", mPath));
	}
	public Observable<TVShowSet> getTVShows(Request okRequest) {
		Observable<Response> observable = createObservable(okRequest);
		return observable
				.subscribeOn(Schedulers.io())
				.map(responseBodyMapper)
				.map(new Func1<String, TVShowSet>() {

					@Override
					public TVShowSet call(String responseBody) {
						return parseTvShowSet(responseBody);
					}
				})
				.observeOn(AndroidSchedulers.mainThread());
	}

	public Observable<PersonSet> searchPeople(int page, String query) { return getPeople(searchRequest(page, "person", query)); }
	public Observable<PersonSet> getPeople(int page, String ... mPath) { return getPeople(fetchRequest(page, "person", mPath)); }
	public Observable<PersonSet> getPeople(Request okRequest) {
		Observable<Response> observable = createObservable(okRequest);
		return observable
				.subscribeOn(Schedulers.io())
				.map(responseBodyMapper)
				.map(new Func1<String, PersonSet>() {

					@Override
					public PersonSet call(String responseBody) {
						return parsePersonSet(responseBody);
					}
				})
				.observeOn(AndroidSchedulers.mainThread());
	}

	public Observable<GenreSet> getGenres(String ... mPath) {
		Request okRequest = fetchRequest("genre", mPath);
		Observable<Response> observable = createObservable(okRequest);
		return observable
				.subscribeOn(Schedulers.io())
				.map(responseBodyMapper)
				.map(new Func1<String, GenreSet>() {

					@Override
					public GenreSet call(String responseBody) {
						return parseGenreSet(responseBody);
					}
				})
				.observeOn(AndroidSchedulers.mainThread());
	}

	public Request fetchRequest(String prefix, String [] paths) {
		return fetchRequest(-1, prefix, paths);
	}
	public Request fetchRequest(String [] paths) {
		return fetchRequest(-1, null, paths);
	}
	public Request fetchRequest(String prefix) {
		return fetchRequest(-1, prefix, null);
	}
	public Request fetchRequest(int page, String prefix, String [] paths) {
		HttpUrl okurl = HttpUrl.parse(url);
		if (okurl == null) {
			throw new RuntimeException("request builder fails on ");
		}
		HttpUrl.Builder b = okurl.newBuilder().addQueryParameter("api_key", key);
		if (page > 0) {
			b.addQueryParameter("page", Integer.toString(page));
		}
		if (prefix != null) {
			b.addEncodedPathSegment(prefix);
		}
		if (paths != null) {
			for (String ps : paths) {
				b.addEncodedPathSegment(ps);
			}
		}
		okurl = b.build();
//		Log.d("fetchRequest", "made url!! "+okurl.toString());
		return new Request.Builder().url(okurl).build();
	}

	public Request searchRequest(int page, String prefix, String query) {
		HttpUrl okurl = HttpUrl.parse(url);
		if (okurl == null) {
			throw new RuntimeException("request builder fails on ");
		}
		HttpUrl.Builder b = okurl.newBuilder().addQueryParameter("api_key", key);
		if (page > 0) {
			b.addQueryParameter("page", Integer.toString(page));
		}

		if (query != null) {
			b.addQueryParameter("query", query);
		}
		b.addEncodedPathSegment("search");
		if (prefix != null) {
			b.addEncodedPathSegment(prefix);
		}
		okurl = b.build();
//		Log.d("searchRequest", "made url!! "+okurl.toString());
		return new Request.Builder().url(okurl).build();
	}

	/**
	 * various parser wrappers: basically pure gson using the structures for the model as defined above
	 */
	@NonNull
	protected GenreSet parseGenreSet(String responseBody) {
		Gson gson = new GsonBuilder().create();
		GenreSet genres = gson.fromJson(responseBody, GenreSet.class);
		if (genres == null) {
			throw new RuntimeException("Unexpected null result processing JSON");
		}
		return genres;
	}

	@NonNull
	protected PersonSet parsePersonSet(String responseBody) {
		Gson gson = new GsonBuilder().create();
		PersonSet peeples = gson.fromJson(responseBody, PersonSet.class);
		if (peeples == null) {
			throw new RuntimeException("Unexpected null result processing JSON");
		}
		return peeples;
	}

	@NonNull
	protected MovieSet parseMovieSet(String responseBody) {
		Gson gson = new GsonBuilder().create();
		MovieSet movies = gson.fromJson(responseBody, MovieSet.class);
		if (movies == null) {
			throw new RuntimeException("Unexpected null result processing JSON");
		}
		return movies;
	}

	@NonNull
	protected TVShowSet parseTvShowSet(String responseBody) {
		Gson gson = new GsonBuilder().create();
		TVShowSet tvshows = gson.fromJson(responseBody, TVShowSet.class);
		if (tvshows == null) {
			throw new RuntimeException("Unexpected null result processing JSON");
		}
		return tvshows;
	}


	@NonNull
	protected Configuration parseConfiguration(String responseBody) {
		Gson gson = new GsonBuilder().create();
		Configuration config = gson.fromJson(responseBody, Configuration.class);
		if (config == null) {
			throw new RuntimeException("Unexpected null result processing JSON");
		}
		return config;
	}

}
