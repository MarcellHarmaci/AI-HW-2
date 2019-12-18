package com.harmaci.AIHW;

public class Review {
	private int reviewID;
	private int userID;
	private int audioBookID;
	private int rating;
	
	public Review(int reviewID, int userID, int audioBookID, int rating) {
		this.reviewID = reviewID;
		this.userID = userID;
		this.audioBookID = audioBookID;
		this.rating = rating;
	}
	
	public int getUserID() {
		return userID;
	}
	
	public int getAudioBookID() {
		return audioBookID;
	}
	
	public int getRating() {
		return rating;
	}
}
