package com.harmaci.AIHW;

import com.harmaci.AIHW.comparator.UserIDComparator;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.NoSuchElementException;

public class Main {
	private static final double NEIGHBOR_SELECT_PROPORTION = 1 / (double) 5;	// TODO Try this as (double) 1 / 5
	private static final double NORMALIZATION_CONSTANT = 1;
	
	public static void main(String[] args) {
		InputStreamReader isr = new InputStreamReader(System.in);
		BufferedReader br = new BufferedReader(isr);
		
		int reviewNum = 0;
		int userNum = 0;
		int audioBookNum = 0;
		ArrayList<User> users;
		ArrayList<Review> reviews = new ArrayList<>();
		double[][] similarityMx;
		
		// Reading input
		try {
			// Reading metadata
			String[] firstLine = br.readLine().split("\t");
			reviewNum = Integer.parseInt(firstLine[0]);
			userNum = Integer.parseInt(firstLine[1]);
			audioBookNum = Integer.parseInt(firstLine[2]);
			
			// Reading reviews
			for (int i = 0; i < reviewNum; i++) {
				String[] reviewLine = br.readLine().split("\t");
				reviews.add(new Review(
						i,
						Integer.parseInt(reviewLine[0]),
						Integer.parseInt(reviewLine[1]),
						Integer.parseInt(reviewLine[2])
				));
			}
			br.close();
			
		} catch (NumberFormatException nfe) {
			System.out.println("Parsing exception encountered");
			nfe.printStackTrace();
		} catch (IOException ioe) {
			System.out.println("IO exception encountered");
			ioe.printStackTrace();
		}
		
		// Initialize user data
		users = initUsers(userNum, audioBookNum, reviews);
		users.sort(new UserIDComparator());
		
		// Normalization
		for (User user : users) {
			user.normalizeReviews();
			// TODO Remove printing from final version BUT leave normalization !
			//System.out.println(user.toString());
		}
		
		// Calculate similarities
		similarityMx = calcSimilarityMx(users, userNum);
		
		// Make predictions and recommendations
		ArrayList<int[]> recommendations = new ArrayList<>();
		recommendations = makeRecommendations(users, similarityMx, audioBookNum);
		
		// Print recommendations
		for (int[] recIDs : recommendations) {
			StringBuilder line = new StringBuilder();
			int i;
			for (i = 0; i < recIDs.length - 1; i++) {
				line.append(recIDs[i]).append("\t");
			}
			line.append(recIDs[i]);
			
			System.out.println(line);
		}
	}
	
	private static ArrayList<User> initUsers(int userCnt, int audioBookCnt, ArrayList<Review> reviews) {
		// Create users
		ArrayList<User> users = new ArrayList<>();
		for (int i = 0; i < userCnt; i++)
			users.add(new User(i, audioBookCnt));
		
		// Init review vectors
		for (Review review : reviews) {
			users.get(review.getUserID())
					.setReview(review.getAudioBookID(), review.getRating());
		}
		return users;
	}
	
	private static double[][] calcSimilarityMx(ArrayList<User> users, int userNum) {
		double[][] similarityMx = new double[userNum][userNum];
		
		for (int i = 0; i < userNum; i++) {
			//System.out.println("User " + users.get(i).getUserID() + " similar to user:");
			for (int j = 0; j < userNum; j++) {
				similarityMx[i][j] = centeredCosine(users.get(i), users.get(j));
				//System.out.println(users.get(j).getUserID() + " > " + similarityMx[i][j] + "\t");
			}
		}
		// TODO Remove printing similarity matrix from final version
		
		return similarityMx;
	}
	
	private static double centeredCosine(User user1, User user2) {
		double numerator = 0, denominator = 0;
		double sqrt1 = 0, sqrt2 = 0;
		double[] vec1 = user1.getVector();
		double[] vec2 = user2.getVector();
		
		for (int i = 0; i < vec1.length; i++) {
			numerator += vec1[i] * vec2[i];
			sqrt1 += vec1[i] * vec1[i];
			sqrt2 += vec2[i] * vec2[i];
		}
		
		denominator = Math.sqrt(sqrt1) * Math.sqrt(sqrt2);
		
		// TODO - What if one of the 2 users have no ratings?
		if (denominator == 0)
			return 0;
		
		return numerator / denominator;
	}
	
	private static ArrayList<int[]> makeRecommendations(ArrayList<User> users, double[][] similarityMx, int audioBookNum) {
		ArrayList<double[]> allPredictions = new ArrayList<>();
		ArrayList<int[]> allRecommendations = new ArrayList<>();
		
		// Make predictions for every user
		for (User user : users) {
			allPredictions.add(makePredictionForUser(user, users, similarityMx, audioBookNum));
		}
		
		for (double[] predictions : allPredictions) {
			// Print predictions
			// TODO - Remove from final
			//String out = "";
			//for (double dOut : predictions) {
			//	out += dOut + "\t\t";
			//}
			//System.out.println(out);
			
			allRecommendations.add(getTop10Recommendation(predictions));
		}
		return allRecommendations;
	}
	
	private static int[] getTop10Recommendation(double[] predictions) {
		int[] recommendations = new int[10];
		
		// Copy predictions
		ArrayList<Double> tmpPredictions = new ArrayList<>();
		ArrayList<Double> predictionsClone = new ArrayList<>();
		for (int i = 0; i < predictions.length; i++) {
			tmpPredictions.add(predictions[i]);
			predictionsClone.add(predictions[i]);
		}
		
		// Find top 10 elements (values can appear more then once)
		double[] top10 = new double[10];
		for (int i = 0; i < 10; i++) {
			try {
				top10[i] = Collections.max(tmpPredictions);
				tmpPredictions.remove(top10[i]);
			}
			catch (NoSuchElementException e) {
				top10[i] = -1;
				if (!tmpPredictions.isEmpty())
					tmpPredictions.remove(0);
			}
		}
		
		// Get recommendation indexes
		double lastMax = -1;
		int cntSame = 0;
		for (int i = 0; i < 10; i++) {
			// Not enough audioBooks
			if (top10[i] == -1) {
				recommendations[i] = -1;
				continue;
			}
			
			// Non-repeating estimated rating
			if (top10[i] != lastMax) {
				recommendations[i] = predictionsClone.indexOf(top10[i]);
				lastMax = top10[i];
				cntSame = 0;
			}
			// Repeating estimated rating
			else {
				cntSame++;
				// Clone predictions
				ArrayList<Double> tmpList = new ArrayList<>();
				for (int j = 0; j < predictions.length; j++) {
					tmpList.add(predictions[j]);
				}
				// Find current number's original index (not the 1st occurrence)
				for (int j = 0; j < cntSame; j++) {
					tmpList.remove(top10[i]);
				}
				recommendations[i] = tmpList.indexOf(top10[i]) + cntSame;
			}
		}
		
		return recommendations;
	}
	
	private static double[] makePredictionForUser(User user, ArrayList<User> users, double[][] similarityMx, int audioBookNum) {
		double[] predictedRatings = new double[audioBookNum];
		int i;
		
		// Predict every unrated audioBook's rating
		for (i = 0; i < audioBookNum; i ++) {
			// Skip cycle if this audioBook is rated
			if (user.getRating(i) != 0)
				continue;
			
			ArrayList<User> neighborhood = new ArrayList<>();
			
			// Find user's neighborhood for this audioBook
			for (User neighbor : users) {
				// User not part of it's own neighborhood
				if (neighbor.equals(user))
					continue;
				
				// Must have a rating for this audioBook
				if (neighbor.getRating(i) != 0)
					// Save userID
					neighborhood.add(neighbor);
			}
			
			/* Refine neighborhood */
			int neighborhoodCardinality = (int) Math.ceil(neighborhood.size() * NEIGHBOR_SELECT_PROPORTION);
			
			// Contains index of neighbors ordered by (descending) similarity to this user
			ArrayList<Integer> simOrderedNHoodIndex = similarityOrder(neighborhood, similarityMx[user.getUserID()]);
			
			// Remove neighbor if not similar enough
			int k = 0;
			for (int j = neighborhoodCardinality; j < simOrderedNHoodIndex.size(); j++) {
				Iterator<User> iter = neighborhood.iterator();
				while (iter.hasNext()) {
					User u = iter.next();
					if (u.getUserID() == (simOrderedNHoodIndex.get(j) - k++) ) // k is to fix shortening of neighborhood
						iter.remove();
				}
				// TODO - remove comment below from final
				/*for (User u : neighborhood) {
					if (u.getUserID() == (simOrderedNHoodIndex.get(j) - k++) ) // k is to fix shortening of neighborhood
						neighborhood.remove(u);
				}*/
			}
			
			// Default rating prediction
			predictedRatings[i] = user.getRowMean();
			
			// Improve prediction by neighbor's rating
			double neighborhoodWeight = 0;
			for (User neighbor : neighborhood) {
				neighborhoodWeight += (neighbor.getRating(i) * similarityMx[user.getUserID()][neighbor.getUserID()]);
			}
			predictedRatings[i] += neighborhoodWeight * NORMALIZATION_CONSTANT;
		}
		return predictedRatings;
	}
	
	// TODO fix this method - I think I fixed it earlier
	private static ArrayList<Integer> similarityOrder(ArrayList<User> neighborhood, double[] similarityRow) {
		ArrayList<Integer> neighborIndexByDistance = new ArrayList<>();
		int[] allIndexByDistance = new int[similarityRow.length];
		
		// Transform to list
		ArrayList<Double> simRow = new ArrayList<>();
		for (int i = 0; i < similarityRow.length; i++) {
			simRow.add(similarityRow[i]);
		}
		
		// Get index of max value
		for (int i = 0; i < allIndexByDistance.length; i++) {
			allIndexByDistance[i] = simRow.indexOf(Collections.max(simRow));
			simRow.remove(allIndexByDistance[i]);
			// Add i to fix shortening of list
			allIndexByDistance[i] += i;
		}
		
		// Keep only neighbors' indexes
		for (int index : allIndexByDistance) {
			for (User neighbor : neighborhood) {
				if (neighbor.getUserID() == index) {
					neighborIndexByDistance.add(index);
					break;
				}
			}
		}
		
		return neighborIndexByDistance;
	}
	
	// Round doubles half-up with given decimal places
	public static double round(double value, int places) {
		if (places < 0) throw new IllegalArgumentException();
		
		BigDecimal bd = BigDecimal.valueOf(value);
		bd = bd.setScale(places, RoundingMode.HALF_UP);
		return bd.doubleValue();
	}
}
