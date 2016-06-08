package com.mayaswell.moviedb;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Created by dak on 6/6/2016.
 */
public class ItemCache<T> {
	protected ArrayList<T> list = new ArrayList<T>();
	public int page = -1;
	public int nPage = 0;

	public void add(int page, int nPage, Collection<T> set) {
		this.page = page;
		this.nPage = nPage;

		for (T m: set) {
			boolean found = false;
			for (T pm: list) {
				if (pm.equals(m)) {
					found = true;
					break;
				}
			}
			if (!found) {
				list.add(m);
			}
		}
	}


	public ArrayList<T> getList() {
		return list;
	}

	public boolean hasMorePages() {
		return page < nPage;
	}

	public void clear() {
		page = -1;
		nPage = 0;
		list.clear();
	}
}
