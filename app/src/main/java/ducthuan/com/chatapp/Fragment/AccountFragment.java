package ducthuan.com.chatapp.Fragment;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import ducthuan.com.chatapp.Activity.MainActivity;
import ducthuan.com.chatapp.Model.User;
import ducthuan.com.chatapp.R;

public class AccountFragment extends Fragment {
    View view;

    CircleImageView imgUser;
    TextView txtUser;

    FirebaseUser firebaseUser;
    DatabaseReference databaseReference;
    StorageReference storageReference;

    User user;

    ProgressDialog progressDialog;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_account, container, false);

        addControls();
        addEvents();

        return view;
    }

    private void addControls() {
        imgUser = view.findViewById(R.id.imgUser);
        txtUser = view.findViewById(R.id.txtUser);

        progressDialog = new ProgressDialog(getContext());
        progressDialog.setMessage("Loading, please wait...");

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        databaseReference = FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.getUid());
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                user = snapshot.getValue(User.class);
                txtUser.setText(user.getUsername());
                if (user.getImage().equals("default")) {
                    imgUser.setImageResource(R.mipmap.ic_launcher);
                } else {
                    Picasso.with(getContext()).load(user.getImage()).into(imgUser);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void addEvents() {
        imgUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                    changePhotoUser();

            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK && data != null) {
            if (requestCode == 001) {
                Bitmap bitmap1 = (Bitmap) data.getExtras().get("data");
                imgUser.setImageBitmap(bitmap1);

                changeImageNew(imgUser);

            } else if (requestCode == 002) {

                Uri uri = data.getData();

                try {
                    Bitmap bitmap1 = MediaStore.Images.Media.getBitmap(getContext().getContentResolver(), uri);
                    imgUser.setImageBitmap(bitmap1);

                    changeImageNew(imgUser);

                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        }

    }

    private void changeImageNew(CircleImageView imgUser) {
        progressDialog.show();
        Calendar calendar = Calendar.getInstance();
        if(!user.getImage().equals("default")){
            storageReference = FirebaseStorage.getInstance().getReferenceFromUrl(user.getImage());
            storageReference.delete();
        }

        storageReference = FirebaseStorage.getInstance().getReference("image_users");
        StorageReference mountainsRef = storageReference.child("image" + calendar.getTimeInMillis() + ".jpg");

        imgUser.setDrawingCacheEnabled(true);
        imgUser.buildDrawingCache();
        Bitmap bitmap = ((BitmapDrawable) imgUser.getDrawable()).getBitmap();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
        byte[] data1 = baos.toByteArray();

        UploadTask uploadTask = mountainsRef.putBytes(data1);

        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {

                Toast.makeText(getContext(), "Đã xảy ra lỗi, vui lòng thử lại", Toast.LENGTH_SHORT).show();

            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                Task<Uri> resul = taskSnapshot.getStorage().getDownloadUrl();
                resul.addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        databaseReference = FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.getUid());
                        HashMap<String, Object> hashMap = new HashMap<>();
                        hashMap.put("image", uri.toString());
                        databaseReference.updateChildren(hashMap);
                        progressDialog.dismiss();
                        Toast.makeText(getContext(), "Upload hình thành công", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }


    private void changePhotoUser() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (getContext().checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_DENIED ||
                    getContext().checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {

                String[] permission = {Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
                requestPermissions(permission, 1000);

            } else {
                handleDialogChoosePhoto();
            }
        }
    }

    private void handleDialogChoosePhoto() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Choose photo from");
        builder.setView(R.layout.custom_dialog_choose_image);
        final AlertDialog dialog = builder.create();
        dialog.show();

        TextView txtCamera = dialog.findViewById(R.id.txtCamera);
        TextView txtGallery = dialog.findViewById(R.id.txtGallery);

        txtCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openCamera();
                dialog.dismiss();
            }
        });
        txtGallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openGallery();
                dialog.dismiss();
            }
        });

    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, 002);
    }

    private void openCamera() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent, 001);

    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 1000 && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            handleDialogChoosePhoto();
        } else {
            Toast.makeText(getContext(), "Deny permission...", Toast.LENGTH_SHORT).show();
        }

    }

}


