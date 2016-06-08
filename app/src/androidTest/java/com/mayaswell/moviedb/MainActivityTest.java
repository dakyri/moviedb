package com.mayaswell.moviedb;

import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.test.ActivityInstrumentationTestCase2;
import android.test.suitebuilder.annotation.LargeTest;
import android.view.View;
import android.view.ViewGroup;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.hasDescendant;
import static android.support.test.espresso.matcher.ViewMatchers.hasSibling;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.core.AllOf.allOf;

/**
 * Created by dak on 6/8/2016.
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class MainActivityTest  extends ActivityInstrumentationTestCase2<MainActivity> {
	public MainActivityTest() {
		super(MainActivity.class);
	}
	MainActivity mainActivity = null;

	public static Matcher<View> nthChildOf(final Matcher<View> parentMatcher, final int childPosition) {
		return new TypeSafeMatcher<View>() {
			@Override
			public void describeTo(Description description) {
				description.appendText("with " + childPosition + " child view of type parentMatcher");
			}

			@Override
			public boolean matchesSafely(View view) {
				if (!(view.getParent() instanceof ViewGroup)) {
					return parentMatcher.matches(view.getParent());
				}

				ViewGroup group = (ViewGroup) view.getParent();
				return parentMatcher.matches(view.getParent()) && group.getChildAt(childPosition).equals(view);
			}
		};
	}

	@Before
	public void setUp() throws Exception {
		super.setUp();
		injectInstrumentation(InstrumentationRegistry.getInstrumentation());
		mainActivity = getActivity();
		assertNotNull(mainActivity);
	}

	/* todo should mock web server response .. this test is contingent on current response */
	@Test
	public void testClickUnmocked() {
		try { // wait for setup from real server
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		onView(withId(R.id.movieListView)).check(matches(hasDescendant(withText("Deadpool"))));
		onView(withId(R.id.movieListView)).check(matches(hasDescendant(withText("7.2"))));
		onView(withId(R.id.movieListView)).check(matches(hasDescendant(withText("Action, Adventure, Comedy, Romance"))));
		onView(allOf(withId(R.id.itemButton), hasSibling(withText("Deadpool")))).perform(click());
	}
}
