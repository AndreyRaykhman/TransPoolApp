package transpool.logic.user;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Driver {

    private User user;
    private List<Rank> ranks;
    private int totalScore = 0;
    private float averageScore = 0;
    private Map<User, Rank> allRanks;


    public static class Rank {
        private int score;
        private String comment;
        private User user;


        public Rank(User user, int score) {
            this.score = score;
            this.user = user;
        }

        public void setComment(String comment) {
            this.comment = comment;
        }

        public int getStars() {
            return score;
        }

        public String getComment() {
            return comment;
        }
    }

    public Driver(User user) {
        this.user = user;
        this.allRanks = new HashMap<>();
    }

    public User getUser() {
        return user;
    }

    public void addRank(Rank rank){
        this.allRanks.put(rank.user, rank);
        this.ranks.add(rank);
        this.totalScore += rank.score;
        this.averageScore = (float) totalScore / this.ranks.size();
    }

    public float getAverageScore() {
        return averageScore;
    }

    public int getTotalScore() {
        return totalScore;
    }
}
