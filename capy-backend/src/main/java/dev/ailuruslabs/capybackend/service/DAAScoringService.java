package dev.ailuruslabs.capybackend.service;

import dev.ailuruslabs.capybackend.algorthms.BoyerMooreStringSearch;
import dev.ailuruslabs.capybackend.application.ScoringEngine;
import dev.ailuruslabs.capybackend.domain.MatchPair;
import dev.ailuruslabs.capybackend.domain.UserProfile;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class DAAScoringService implements ScoringEngine {

    @Override
    public List<MatchPair> scorePool(List<UserProfile> processingPool) {
        List<MatchPair> pairs = new ArrayList<>();

        for (int i = 0; i < processingPool.size(); i++) {
            for (int j = i + 1; j < processingPool.size(); j++) {
                UserProfile userA = processingPool.get(i);
                UserProfile userB = processingPool.get(j);

                double score = calculateCompatibility(userA, userB);
                if (score > 0) {
                    pairs.add(new MatchPair(userA, userB, score));
                }
            }
        }

        if (pairs.size() > 1) {
            mergeSort(pairs, 0, pairs.size() - 1);
        }

        return pairs;
    }

    private double calculateCompatibility(UserProfile userA, UserProfile userB) {
        int mAB = countMatches(userA.offeredSkills(), userB.requestedSkills());
        int mBA = countMatches(userB.offeredSkills(), userA.requestedSkills());

        if (mAB == 0 && mBA == 0) return 0.0;

        double min = Math.min(mAB, mBA);
        double max = Math.max(mAB, mBA);

        // FIX: Flat 5 points if they can teach at least one skill, as per the proposal's math
        double baseScore = (mAB > 0 ? 5.0 : 0.0) + (mBA > 0 ? 5.0 : 0.0);
        double mutualBonus = max > 0 ? 10.0 * (min / max) : 0.0;

        return baseScore + mutualBonus;
    }

    private int countMatches(Iterable<String> offered, Iterable<String> requested) {
        int count = 0;
        for (String offer : offered) {
            BoyerMooreStringSearch bm = new BoyerMooreStringSearch(offer.toLowerCase());
            for (String req : requested) {
                if (bm.search(req.toLowerCase())) {
                    count++;
                    break;
                }
            }
        }
        return count;
    }

    private void mergeSort(List<MatchPair> list, int left, int right) {
        if (left < right) {
            int mid = left + (right - left) / 2;
            mergeSort(list, left, mid);
            mergeSort(list, mid + 1, right);
            merge(list, left, mid, right);
        }
    }

    private void merge(List<MatchPair> list, int left, int mid, int right) {
        int n1 = mid - left + 1;
        int n2 = right - mid;

        List<MatchPair> leftList = new ArrayList<>(n1);
        List<MatchPair> rightList = new ArrayList<>(n2);

        for (int i = 0; i < n1; ++i) leftList.add(list.get(left + i));
        for (int j = 0; j < n2; ++j) rightList.add(list.get(mid + 1 + j));

        int i = 0, j = 0, k = left;
        while (i < n1 && j < n2) {
            if (leftList.get(i).compareTo(rightList.get(j)) <= 0) {
                list.set(k, leftList.get(i));
                i++;
            } else {
                list.set(k, rightList.get(j));
                j++;
            }
            k++;
        }

        while (i < n1) list.set(k++, leftList.get(i++));
        while (j < n2) list.set(k++, rightList.get(j++));
    }
}
