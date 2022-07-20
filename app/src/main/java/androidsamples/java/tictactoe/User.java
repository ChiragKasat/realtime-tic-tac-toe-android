package androidsamples.java.tictactoe;

public class User {
    public String email;
    public int wins;
    public int losses;

    public User() {
    }

    public User(String email) {
        this.email = email;
        this.wins = 0;
        this.losses = 0;
    }
}
