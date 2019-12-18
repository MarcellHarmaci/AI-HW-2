package com.harmaci.AIHW.comparator;

import com.harmaci.AIHW.User;

import java.util.Comparator;

public class UserIDComparator implements Comparator<User> {
	@Override
	public int compare(User o1, User o2) {
		return o1.getUserID() - o2.getUserID();
	}
}
