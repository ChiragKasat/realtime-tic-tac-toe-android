package androidsamples.java.tictactoe;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.NavDirections;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.UUID;

public class DashboardFragment extends Fragment {

  private static final String TAG = "DashboardFragment";
  private NavController mNavController;
  private FirebaseAuth mAuth;
  private DatabaseReference mDatabase;
  private TextView mWinsCount, mLossCount;

  ValueEventListener userListener = new ValueEventListener() {
    @Override
    public void onDataChange(DataSnapshot dataSnapshot) {
      User user = dataSnapshot.getValue(User.class);
      assert user != null;
      mWinsCount.setText(Integer.toString(user.wins));
      mLossCount.setText(Integer.toString(user.losses));
    }

    @Override
    public void onCancelled(DatabaseError databaseError) {
      Log.w(TAG, "loadPost:onCancelled", databaseError.toException());
    }
  };


  /**
   * Mandatory empty constructor for the fragment manager to instantiate the
   * fragment (e.g. upon screen orientation changes).
   */
  public DashboardFragment() {
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    Log.d(TAG, "onCreate");

    setHasOptionsMenu(true); // Needed to display the action menu for this fragment
  }

  @Override
  public View onCreateView(LayoutInflater inflater,
                           ViewGroup container,
                           Bundle savedInstanceState) {
    return inflater.inflate(R.layout.fragment_dashboard, container, false);
  }

  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    mWinsCount = view.findViewById(R.id.wins_count);
    mLossCount = view.findViewById(R.id.loss_count);
    mNavController = Navigation.findNavController(view);
    mAuth = FirebaseAuth.getInstance();
    mDatabase = FirebaseDatabase.getInstance().getReference();

    FirebaseUser currentUser = mAuth.getCurrentUser();

    if(currentUser == null) {
      NavDirections action = DashboardFragmentDirections.actionNeedAuth();
      mNavController.navigate(action);
      return;
    }else {
      mDatabase.child("users").child(currentUser.getUid()).addValueEventListener(userListener);
    }


    ArrayList<OpenGame> openGames = new ArrayList<>();

    RecyclerView gamesList = view.findViewById(R.id.list);
    gamesList.setLayoutManager(new LinearLayoutManager(getActivity()));
    OpenGamesAdapter adapter = new OpenGamesAdapter(getContext(), openGames);
    gamesList.setAdapter(adapter);

    mDatabase.child("openGames").addValueEventListener(new ValueEventListener() {
      @Override
      public void onDataChange(@NonNull DataSnapshot snapshot) {
        openGames.clear();
        for(DataSnapshot ds:snapshot.getChildren()){
          OpenGame openGame = ds.getValue(OpenGame.class);
          openGames.add(openGame);
        }
        adapter.notifyDataSetChanged();
      }

      @Override
      public void onCancelled(@NonNull DatabaseError error) {

      }
    });

    // Show a dialog when the user clicks the "new game" button
    view.findViewById(R.id.fab_new_game).setOnClickListener(v -> {

      // A listener for the positive and negative buttons of the dialog
      DialogInterface.OnClickListener listener = (dialog, which) -> {
        String gameType = "No type";
        String key = "";
        if (which == DialogInterface.BUTTON_POSITIVE) {
          gameType = getString(R.string.two_player);
          key = mDatabase.child("openGames").push().getKey();
          OpenGame game = new OpenGame(key, currentUser.getEmail());
          mDatabase.child("openGames").child(key).setValue(game);
        } else if (which == DialogInterface.BUTTON_NEGATIVE) {
          gameType = getString(R.string.one_player);
        }
        Log.d(TAG, "New Game: " + gameType);

        // Passing the game type as a parameter to the action
        // extract it in GameFragment in a type safe way
        DashboardFragmentDirections.ActionGame action = DashboardFragmentDirections.actionGame(key, gameType);
        mNavController.navigate((NavDirections) action);
      };

      // create the dialog
      AlertDialog dialog = new AlertDialog.Builder(requireActivity())
          .setTitle(R.string.new_game)
          .setMessage(R.string.new_game_dialog_message)
          .setPositiveButton(R.string.two_player, listener)
          .setNegativeButton(R.string.one_player, listener)
          .setNeutralButton(R.string.cancel, (d, which) -> d.dismiss())
          .create();
      dialog.show();
    });
  }

  @Override
  public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
    super.onCreateOptionsMenu(menu, inflater);
    inflater.inflate(R.menu.menu_logout, menu);
    // this action menu is handled in MainActivity
  }
}