package androidsamples.java.tictactoe;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Arrays;



public class GameFragment extends Fragment {
  private static Context context;
  private static final String TAG = "GameFragment";
  private static final int GRID_SIZE = 9;
  private FirebaseUser currentUser;
  private DatabaseReference mDb;
  private int mWinsCount, mLossCount;
  private boolean didThisPlayerForfeit = false;
  private String gameID;

  ValueEventListener userListener = new ValueEventListener() {
    @Override
    public void onDataChange(DataSnapshot dataSnapshot) {
      User user = dataSnapshot.getValue(User.class);
      assert user != null;
      mWinsCount = user.wins;
      mLossCount = user.losses;
    }

    @Override
    public void onCancelled(DatabaseError databaseError) {
      Log.w(TAG, "loadPost:onCancelled", databaseError.toException());
    }
  };

  private final Button[] mButtons = new Button[GRID_SIZE];
  private NavController mNavController;

  int[][] winPositions = {{0, 1, 2}, {3, 4, 5}, {6, 7, 8},
          {0, 3, 6}, {1, 4, 7}, {2, 5, 8},
          {0, 4, 8}, {2, 4, 6}};

  int thisPlayer = 0;
  int[] gameState = {0, 0, 0, 0, 0, 0, 0, 0, 0};
  private Boolean isGameActive = false;
  private int typeOfGame = 0;

  @Override
  public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setHasOptionsMenu(true); // Needed to display the action menu for this fragment
    GameFragment.context = getContext();
    mDb = FirebaseDatabase.getInstance().getReference();
    currentUser = FirebaseAuth.getInstance().getCurrentUser();
    // Extract the argument passed with the action in a type-safe way
    GameFragmentArgs args = GameFragmentArgs.fromBundle(getArguments());
    Log.d(TAG, "New game type = " + args.getGameType());

    if(args.getGameType().equals("One-Player")) {
      isGameActive = true;
      typeOfGame = 1;
      thisPlayer = 1;
    }
    else{
      typeOfGame = 2;
      gameID = args.getGameId();
      if(!args.getIsPlayer2()) {
        thisPlayer = 1;
        mDb.child("games").child(args.getGameId()).setValue("333333333");
      }
      else {
        thisPlayer = 2;
        mDb.child("openGames").child(args.getGameId()).removeValue();
        isGameActive = true;
        updateRemoteGameState();
      }

      mDb.child("games").child(args.getGameId()).addValueEventListener(gameListener);
    }


    Arrays.fill(gameState, 0);




    // Handle the back press by adding a confirmation dialog
    OnBackPressedCallback callback = new OnBackPressedCallback(true) {
      @Override
      public void handleOnBackPressed() {
        Log.d(TAG, "Back pressed");

        // TODO show dialog only when the game is still in progress
        AlertDialog dialog = new AlertDialog.Builder(requireActivity())
            .setTitle(R.string.confirm)
            .setMessage(R.string.forfeit_game_dialog_message)
            .setPositiveButton(R.string.yes, (d, which) -> {
              didThisPlayerForfeit = true;
              mDb.child("games").child(args.getGameId()).setValue("");
              mDb.child("openGames").child(args.getGameId()).removeValue();

              mDb.child("users").child(currentUser.getUid()).child("losses").setValue(mLossCount+1);

              mNavController.popBackStack();
            })
            .setNegativeButton(R.string.cancel, (d, which) -> d.dismiss())
            .create();
        dialog.show();
      }
    };
    requireActivity().getOnBackPressedDispatcher().addCallback(this, callback);
  }

  private String arrToString() {
    StringBuilder dum = new StringBuilder();
    for (int j : gameState) {
      dum.append(j);
    }
    return dum.toString();
  }

  private int getActivePlayer() {
    int c1=0, c2=0;
    for(int j : gameState) {
      if(j==1) c1++;
      else if(j==2) c2++;
    }
    if((c1+c2)%2 == 0) return 1;
    else return 2;
  }

  private boolean isGridFull() {
    for(int j:gameState) {
      if(j==0 || j==3) return false;
    }

    return true;
  }

  private void incrementWins() {
    mDb.child("users").child(currentUser.getUid()).child("wins").setValue(mWinsCount+1);
  }

  private void incrementLoss() {
    mDb.child("users").child(currentUser.getUid()).child("losses").setValue(mLossCount+1);
  }

  private void showGameOverDialog(String msg) {
    AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
    builder.setMessage(msg)
            .setCancelable(false)
            .setPositiveButton("OK", (dialog, id) -> {
              mNavController.popBackStack();
              mNavController.navigate(R.id.dashboardFragment);
              dialog.dismiss();
            });
    AlertDialog alert = builder.create();
    alert.show();
  }

  private void updateRemoteGameState() {
    mDb.child("games").child(gameID).setValue(arrToString());
  }

  @Override
  public View onCreateView(LayoutInflater inflater,
                           ViewGroup container,
                           Bundle savedInstanceState) {
    return inflater.inflate(R.layout.fragment_game, container, false);
  }

  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);

    mNavController = Navigation.findNavController(view);

    mButtons[0] = view.findViewById(R.id.button0);
    mButtons[1] = view.findViewById(R.id.button1);
    mButtons[2] = view.findViewById(R.id.button2);

    mButtons[3] = view.findViewById(R.id.button3);
    mButtons[4] = view.findViewById(R.id.button4);
    mButtons[5] = view.findViewById(R.id.button5);

    mButtons[6] = view.findViewById(R.id.button6);
    mButtons[7] = view.findViewById(R.id.button7);
    mButtons[8] = view.findViewById(R.id.button8);

    updateUI();

    mDb.child("users").child(currentUser.getUid()).addValueEventListener(userListener);


    for (int i = 0; i < mButtons.length; i++) {
      int finalI = i;
      mButtons[i].setOnClickListener(v -> {
        Log.d(TAG, "Button " + finalI + " clicked");

        if(!isGameActive) return;

        if(!isMoveValid(finalI)) return;

        if(getActivePlayer()!=thisPlayer) return;

        gameState[finalI] = getActivePlayer();
        updateUI();

        if(checkForWin() == thisPlayer) {
          if(typeOfGame == 2) updateRemoteGameState();
          isGameActive = false;
          incrementWins();
          showGameOverDialog("Congratulations!");
          return;
        }

        if(isGridFull()){
          if(typeOfGame == 2) updateRemoteGameState();
          showGameOverDialog("It's a tie.");
          return;
        }

        if(typeOfGame == 1) {
          for(int j =0; j < gameState.length; j++){
            if(gameState[j]==0) {
              gameState[j] = getActivePlayer();
              updateUI();
              if(checkForWin()!=0) {
                isGameActive = false;

                incrementLoss();

                showGameOverDialog("Sorry! You Lost.");
                return;
              }
              if(isGridFull()){
                showGameOverDialog("It's a tie!");
                return;
              }
              break;
            }
          }
        }
        else {
          updateRemoteGameState();
        }
      });
    }
  }

  @Override
  public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
    super.onCreateOptionsMenu(menu, inflater);
    inflater.inflate(R.menu.menu_logout, menu);
    // this action menu is handled in MainActivity
  }

  private boolean isMoveValid(int i) {
    return gameState[i] == 0;
  }

  private int checkForWin() {
    for (int[] winPosition : winPositions) {
      if (gameState[winPosition[0]] == gameState[winPosition[1]] &&
              gameState[winPosition[1]] == gameState[winPosition[2]] &&
              gameState[winPosition[0]] != 0) {
        return gameState[winPosition[0]];
      }
    }
    return 0;
  }

  private void updateUI() {
    for (int i = 0; i < gameState.length; i++) {
      String icon = "";
      if(gameState[i]==1) icon = "X";
      else if(gameState[i]==2) icon = "O";
      mButtons[i].setText(icon);
    }
  }

  private void stringToState(String s) {
    for (int i = 0; i < gameState.length; i++) {
      gameState[i] = Integer.parseInt(String.valueOf(s.charAt(i)));
    }
  }

  ValueEventListener gameListener = new ValueEventListener() {
    @Override
    public void onDataChange(DataSnapshot dataSnapshot) {
      String gameState = dataSnapshot.getValue(String.class);
      if(gameState == null) {
        return;
      }

      if(gameState.isEmpty()) {
        if(didThisPlayerForfeit) return;
        Toast.makeText(GameFragment.context, "Opponent forfeited the game", Toast.LENGTH_LONG).show();
        mDb.child("users").child(currentUser.getUid()).child("wins").setValue(mWinsCount+1);
        mNavController.popBackStack();
        mNavController.navigate(R.id.dashboardFragment);
        return;
      }

      if(gameState.equals("000000000")) {
        isGameActive = true;
        Toast.makeText(GameFragment.context, "Start the game", Toast.LENGTH_LONG).show();
      }

      stringToState(gameState);
      updateUI();
      if(checkForWin() == 3 - thisPlayer) {
        isGameActive = false;
        incrementLoss();
        showGameOverDialog("Sorry! You Lost.");
        return;
      }

      if(isGridFull()){
        showGameOverDialog("It's a tie.");
      }
    }

    @Override
    public void onCancelled(DatabaseError databaseError) {
      Log.w(TAG, "loadPost:onCancelled", databaseError.toException());
    }
  };
}