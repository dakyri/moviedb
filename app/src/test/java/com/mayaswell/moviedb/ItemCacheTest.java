package com.mayaswell.moviedb;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Created by dak on 6/8/2016.
 */
public class ItemCacheTest {

	protected ItemCache<Integer> testCache = null;
	@Before
	public void setUp() throws Exception {

	}

	@Test
	public void testGetList() throws Exception {
		testCache = new ItemCache<Integer>();
		List<Integer> testData = Arrays.asList(0, 1, 2, 3, 5, 6);
		testCache.add(3, 6, testData);
		assertNotNull(testCache.getList());
	}

	@Test
	public void testAdd() throws Exception {
		testCache = new ItemCache<Integer>();
		List<Integer> testData = Arrays.asList(0, 1, 2, 3, 5, 6);
		testCache.add(3, 6, testData);
		assertEquals(6, testCache.getList().size());
		testCache.add(3, 8, testData);
		assertEquals(6, testCache.getList().size());
		testCache.add(4, 5, Arrays.asList(0, 2, 7, 10, 11));
		assertEquals(9, testCache.getList().size());
	}

	@Test
	public void testHasMorePages() throws Exception {
		testCache = new ItemCache<Integer>();
		List<Integer> testData = Arrays.asList(0, 1, 2, 3, 5, 6);
		testCache.add(3, 6, testData);
		assert(testCache.hasMorePages());
		testCache.add(7, 6, testData);
		assert(!testCache.hasMorePages());
	}

	@Test
	public void testClear() throws Exception {
		testCache = new ItemCache<Integer>();
		List<Integer> testData = Arrays.asList(0, 1, 2, 3, 5, 6);
		testCache.add(3, 6, testData);
		assertEquals(6, testCache.getList().size());
		testCache.clear();
		assertEquals(0, testCache.getList().size());
	}
}