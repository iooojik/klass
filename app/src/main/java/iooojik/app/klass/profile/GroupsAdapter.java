package iooojik.app.klass.profile;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import iooojik.app.klass.R;
import iooojik.app.klass.profile.teacher.GroupInfo;

public class GroupsAdapter extends RecyclerView.Adapter<GroupsAdapter.ViewHolder> {

    private Context context;
    private List<GroupInfo> classGroupInfos;
    private Fragment fragment;
    private LayoutInflater inflater;

    GroupsAdapter(Context context, List<GroupInfo> classGroupInfos, Fragment fragment){
        this.context = context;
        this.classGroupInfos = classGroupInfos;
        this.fragment = fragment;
        this.inflater = LayoutInflater.from(context);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.group_item, parent, false);
        return new GroupsAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        GroupInfo groupInfo = classGroupInfos.get(position);
        holder.groupName.setText(groupInfo.getName());
        holder.linearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle bundle = new Bundle();
                bundle.putInt("id", Integer.parseInt(groupInfo.getId()));
                bundle.putString("groupAuthor", groupInfo.getAuthorEmail());
                bundle.putString("groupName", groupInfo.getName());
                NavController navController = NavHostFragment.findNavController(fragment);
                navController.navigate(R.id.nav_group, bundle);
            }
        });
    }

    @Override
    public int getItemCount() {
        return classGroupInfos.size();
    }


    class ViewHolder extends RecyclerView.ViewHolder{

        TextView groupName;
        LinearLayout linearLayout;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            this.linearLayout = itemView.findViewById(R.id.linear);
            this.groupName = itemView.findViewById(R.id.groupName);
        }
    }
}