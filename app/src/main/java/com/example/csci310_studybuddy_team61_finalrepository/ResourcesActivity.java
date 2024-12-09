package com.example.csci310_studybuddy_team61_finalrepository;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import java.util.ArrayList;
import java.util.List;

public class ResourcesActivity extends AppCompatActivity {

    private FirebaseStorage storage;
    private StorageReference groupFolderRef;
    private ListView resourceListView;
    private EditText searchBar;
    private Button uploadButton;
    private ArrayAdapter<String> adapter;
    private List<String> resourceList = new ArrayList<>();
    private String groupId;

    private static final int PICK_FILE_REQUEST = 1; // File picker request code

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_resources);

        // Retrieve groupId from intent
        groupId = getIntent().getStringExtra("groupId");
        if (groupId == null || groupId.isEmpty()) {
            Toast.makeText(this, "Group ID not found!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Set up Firebase Storage reference for the group folder
        storage = FirebaseStorage.getInstance();
        groupFolderRef = storage.getReference("groups/" + groupId + "/resources");

        // Initialize UI elements
        resourceListView = findViewById(R.id.resourceListView);
        searchBar = findViewById(R.id.searchBar);
        uploadButton = findViewById(R.id.uploadButton);

        // Fetch resources for the group
        fetchResources();

        // Handle search functionality
        searchBar.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                adapter.getFilter().filter(s.toString());
            }
            @Override
            public void afterTextChanged(android.text.Editable s) {}
        });

        // Handle file upload
        uploadButton.setOnClickListener(v -> openFilePicker());

        // Handle resource click
        resourceListView.setOnItemClickListener((parent, view, position, id) -> {
            String resourceName = (String) parent.getItemAtPosition(position);
            openResource(resourceName);
        });
    }

    private void fetchResources() {
        groupFolderRef.listAll().addOnSuccessListener(listResult -> {
            resourceList.clear();
            for (StorageReference fileRef : listResult.getItems()) {
                resourceList.add(fileRef.getName());
            }
            adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, resourceList);
            resourceListView.setAdapter(adapter);
        }).addOnFailureListener(e -> Toast.makeText(this, "Failed to fetch resources!", Toast.LENGTH_SHORT).show());
    }

    private void openFilePicker() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        startActivityForResult(Intent.createChooser(intent, "Select a File"), PICK_FILE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_FILE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            uploadResource(data.getData());
        }
    }

    private void uploadResource(Uri fileUri) {
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Uploading...");
        progressDialog.show();

        String fileName = System.currentTimeMillis() + "_" + fileUri.getLastPathSegment();
        StorageReference fileRef = groupFolderRef.child(fileName);

        fileRef.putFile(fileUri).addOnSuccessListener(taskSnapshot -> {
            progressDialog.dismiss();
            Toast.makeText(this, "Resource uploaded successfully!", Toast.LENGTH_SHORT).show();
            fetchResources();
        }).addOnFailureListener(e -> {
            progressDialog.dismiss();
            Toast.makeText(this, "Failed to upload resource!", Toast.LENGTH_SHORT).show();
        });
    }

    private void openResource(String resourceName) {
        groupFolderRef.child(resourceName).getDownloadUrl().addOnSuccessListener(uri -> {
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            startActivity(intent);
        }).addOnFailureListener(e -> Toast.makeText(this, "Failed to open resource!", Toast.LENGTH_SHORT).show());
    }
}
