package fu.prm391.sampl.finalproject_movieapp.adapter;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

import fu.prm391.sampl.finalproject_movieapp.Model.VideoUploadDetails;
import fu.prm391.sampl.finalproject_movieapp.R;
import fu.prm391.sampl.finalproject_movieapp.UpdateMovieActivity;

public class MyAdapter extends RecyclerView.Adapter<MyViewHolder> {
    private Context mContext;
    private List<VideoUploadDetails> listMovie;
    private LayoutInflater inflater;
    private DatabaseReference videoRef;

    public MyAdapter(List<VideoUploadDetails> listMovie, Context context) {
        this.listMovie = listMovie;
        this.inflater = LayoutInflater.from(context);
        this.mContext = context;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.recyclerview_row,parent,false);
        MyViewHolder myViewHolder = new MyViewHolder(view);
        return myViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        final VideoUploadDetails currentVideo = listMovie.get(position);
        holder.getTextView().setText("NAME: "+currentVideo.getVideoName()+"\n"
                        +"CATEGORY: "+currentVideo.getVideoCategory()+"\n"
                        +"TYPE: "+currentVideo.getVideoType()+"\n"
                        +"DESCRIPTION: "+currentVideo.getVideoDescription()+"\n");
        Glide.with(mContext).load(currentVideo.getVideoThumb()).into(holder.imageView);


        //btnDelete
        holder.getBtnDelete().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                videoRef = FirebaseDatabase.getInstance().getReference().child("videos");
                videoRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for(DataSnapshot item: snapshot.getChildren()) {
                            if(item.getValue(VideoUploadDetails.class).getVideoId().equals(currentVideo.getVideoId())){
                                item.getRef().removeValue();
                                Toast.makeText(mContext, "Delete Successfully !!!" ,Toast.LENGTH_SHORT).show();
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                    }
                });
            }
        });
        //btnUpdate
        holder.getBtnUpdate().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickGoToUpdate(currentVideo);
            }
        });
    }

    private void onClickGoToUpdate(VideoUploadDetails currentVideo){
        Intent intent = new Intent(mContext, UpdateMovieActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        Bundle bundle = new Bundle();
        bundle.putSerializable("selectedVideo",currentVideo);
        intent.putExtras(bundle);
        mContext.startActivity(intent);
    }



    @Override
    public int getItemCount() {
        if(listMovie!=null){
            return listMovie.size();
        }
        return 0;
    }
}
