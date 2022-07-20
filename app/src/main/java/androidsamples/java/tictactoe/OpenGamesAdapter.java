package androidsamples.java.tictactoe;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.navigation.NavController;
import androidx.navigation.NavDirections;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class OpenGamesAdapter extends RecyclerView.Adapter<OpenGamesAdapter.ViewHolder> {
  Context c;
  ArrayList<OpenGame> openGames;

  public OpenGamesAdapter(Context c, ArrayList<OpenGame> openGames) {
    // FIXME if needed
    this.c = c;
    this.openGames = openGames;
  }

  @NonNull
  @Override
  public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
    View view = LayoutInflater.from(parent.getContext())
        .inflate(R.layout.fragment_item, parent, false);
    return new ViewHolder(view);
  }

  @Override
  public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {
    // TODO bind the item at the given position to the holder
    holder.mIdView.setText(Integer.toString(position+1));
    holder.mContentView.setText(openGames.get(position).email);
  }

  @Override
  public int getItemCount() {
    return openGames.size();
  }

  public class ViewHolder extends RecyclerView.ViewHolder {
    public final View mView;
    public final TextView mIdView;
    public final TextView mContentView;

    public ViewHolder(View view) {
      super(view);
      mView = view;
      mIdView = view.findViewById(R.id.item_number);
      mContentView = view.findViewById(R.id.content);

      view.setOnClickListener(this::launchLive);
    }

    private void launchLive(View view) {
      String key = openGames.get(Integer.parseInt(mIdView.getText().toString())-1).id;
      DashboardFragmentDirections.ActionGame action = DashboardFragmentDirections.actionGame(key, String.valueOf(R.string.two_player));
      action.setIsPlayer2(true);
      Navigation.findNavController(view).navigate((NavDirections) action);
    }

    @NonNull
    @Override
    public String toString() {
      return super.toString() + " '" + mContentView.getText() + "'";
    }
  }
}