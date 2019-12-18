package com.harmaci.AIHW;

import java.util.Arrays;

public class User {
	private int userID;
	private double[] vector;
	private double rowMean;
	
	public User(int userID, int reviewNum) {
		this.userID = userID;
		vector = new double[reviewNum];
		Arrays.fill(vector, 0);
		rowMean = 0;
	}
	
	public void setReview(int audioBookID, int rating) {
		vector[audioBookID] = rating;
	}
	
	public int getUserID() {
		return userID;
	}
	
	public double[] getVector() {
		return vector;
	}
	
	public double getRowMean() {
		return rowMean;
	}
	
	public double getRating(int audioBookID) {
		return vector[audioBookID];
	}
	
	public void normalizeReviews() {
		int reviewSum = 0;
		int reviewCnt = 0;
		
		// Count reviews and calculate sum
		for (double value : vector) {
			reviewSum += value;
			if (value != 0)
				reviewCnt++;
		}
		
		// Calculate row mean
		if (reviewCnt != 0)
			rowMean = reviewSum / (double) reviewCnt;
		
		// Center review values by subtracting row mean
		for (int i = 0; i < vector.length; i++) {
			if (vector[i] != 0)
				vector[i] -= rowMean;
		}
	}
	
	@Override
	public String toString() {
		StringBuilder retValue = new StringBuilder().append("User ").append(userID).append(": < ");
		int i;
		for (i = 0; i < vector.length - 1; i++) {
			retValue.append(vector[i]).append(", ");
		}
		retValue.append(vector[i]);
		retValue.append(" >");
		return retValue.toString();
	}
}
