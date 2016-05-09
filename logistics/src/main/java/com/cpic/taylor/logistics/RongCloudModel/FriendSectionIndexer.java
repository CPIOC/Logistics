package com.cpic.taylor.logistics.RongCloudModel;

import android.widget.SectionIndexer;

import com.cpic.taylor.logistics.RongCloudaAdapter.CompositeAdapter;

import java.util.Arrays;




public class FriendSectionIndexer implements SectionIndexer {
	private String[] mSections;
	private int[] mPositions;
	private int mCount;

	/**
	 * Constructor.
	 * 
	 */
	public FriendSectionIndexer(CompositeAdapter.Partition<Friend>[] data) {

		int size = 0;
		if (data == null || (size = data.length) == 0) {
			throw new NullPointerException();
		}

		// TODO process sections/counts based on current locale and/or specific
		// section titles

		this.mSections = new String[size];
		this.mPositions = new int[size];
		
		//初始化positions的数组为-1，避免Arrays.binarySearch方法在查找0值时异常
		for (int i = 0; i < size; i++) {
			mPositions[i] = -1;
		}
		
		int position = 0;

		for (int i = 0; i < size; i++) {
			if (data[i] != null && data[i].getList() != null && data[i].getList().size() > 0) {
				mSections[i] = String.valueOf(data[i].getList().get(0).getSearchKey());

				mPositions[i] = data[i].getHasHeader() ? position++ : position;
				position += data[i].getList().size();
			}
		}

		mCount = position;
	}

	public Object[] getSections() {
		return mSections;
	}

	public int getPositionForSection(int section) {
		if (section < 0 || section >= mSections.length) {
			return -1;
		}

		return mPositions[section];
	}

	public int getSectionForPosition(int position) {
		if (position < 0 || position >= mCount) {
			return -1;
		}

		int index = Arrays.binarySearch(mPositions, position);

		/*
		 * Consider this example: section positions are 0, 3, 5; the supplied
		 * position is 4. The section corresponding to position 4 starts at
		 * position 3, so the expected return value is 1. Binary search will not
		 * find 4 in the array and thus will return -insertPosition-1, i.e. -3.
		 * To get from that number to the expected value of 1 we need to negate
		 * and subtract 2.
		 */
		return index >= 0 ? index : -index - 2;
	}

	public void setProfileHeader(String header) {
		if (mSections != null) {
			// Don't do anything if the header is already set properly.
			if (mSections.length > 0 && header.equals(mSections[0])) {
				return;
			}

			// Since the section indexer isn't aware of the profile at the top,
			// we need to add a
			// special section at the top for it and shift everything else down.
			String[] tempSections = new String[mSections.length + 1];
			int[] tempPositions = new int[mPositions.length + 1];
			tempSections[0] = header;
			tempPositions[0] = 0;
			for (int i = 1; i <= mPositions.length; i++) {
				tempSections[i] = mSections[i - 1];
				tempPositions[i] = mPositions[i - 1] + 1;
			}
			mSections = tempSections;
			mPositions = tempPositions;
			mCount++;
		}
	}
}
