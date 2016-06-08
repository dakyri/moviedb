package com.mayaswell.moviedb;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.mayaswell.moviedb.databinding.PersonDetailBinding;

/**
 * Created by dak on 6/8/2016.
 */
public class PersonDetailViewHolder {
	private final RelativeLayout main;
	public final int index;
	public PersonDetailBinding binding;
	private MovieDbAPI.Person person;

	public PersonDetailViewHolder(ViewGroup parent, MovieDbAPI.Person m) {
		binding = PersonDetailBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
		main = (RelativeLayout) binding.getRoot();
		parent.addView(main);
		index = parent.getChildCount()-1;
		setToPerson(m);
	}

	public void setToPerson(MovieDbAPI.Person person) {
		this.person = person;
		binding.setPerson(person);
	}

}
