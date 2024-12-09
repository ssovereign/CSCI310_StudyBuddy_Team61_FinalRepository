package com.example.csci310_studybuddy_team61_finalrepository;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.util.Log;
import android.widget.*;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;

public class ResourcesActivity extends AppCompatActivity {

    private static final String TAG = "ResourcesActivity";

    private FirebaseStorage storage;
    private StorageReference groupFolderRef;
    private ListView resourceListView;
    private EditText searchBar;
    private Button uploadButton;

    private ArrayAdapter<String> adapter;
    private List<String> resourceList = new ArrayList<>();
    private String groupName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_resources);

        // Retrieve group name from intent
        groupName = getIntent().getStringExtra("GROUP_NAME");
        if (groupName == null || groupName.isEmpty()) {
            Toast.makeText(this, "Group name not found!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Initialize Firebase Storage reference for the group folder
        storage = FirebaseStorage.getInstance();
        groupFolderRef = storage.getReference("groups/" + groupName + "/resources");

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
                if (adapter != null) {
                    adapter.getFilter().filter(s.toString());
                }
            }

            @Override
            public void afterTextChanged(android.text.Editable s) {}
        });

        // Handle file upload
        uploadButton.setOnClickListener(v -> openFilePicker.launch("*/*"));

        // Handle resource click
        resourceListView.setOnItemClickListener((parent, view, position, id) -> {
            String resourceName = (String) parent.getItemAtPosition(position);
            openResource(resourceName);
        });
    }

    /**
     * Fetch the list of resources for the group from Firebase Storage.
     */
    private void fetchResources() {
        Toast.makeText(this, "Loading resources...", Toast.LENGTH_SHORT).show();
        groupFolderRef.listAll().addOnSuccessListener(listResult -> {
            resourceList.clear();
            for (StorageReference fileRef : listResult.getItems()) {
                resourceList.add(fileRef.getName());
                Log.d(TAG, "Fetched resource: " + fileRef.getName());
            }
            adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, resourceList);
            resourceListView.setAdapter(adapter);
        }).addOnFailureListener(e -> {
            Toast.makeText(this, "Failed to fetch resources!", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "Error fetching resources: ", e);
        });
    }

    private void uploadResource(Uri fileUri) {
        if (fileUri == null) {
            Toast.makeText(this, "No file selected", Toast.LENGTH_SHORT).show();
            return;
        }

        // Get the file name from the URI
        String fileName = getFileName(fileUri);

        // Create a reference for the file in the group's folder
        StorageReference fileRef = groupFolderRef.child(fileName);

        Toast.makeText(this, "Uploading file: " + fileName, Toast.LENGTH_SHORT).show();

        // Upload the file
        fileRef.putFile(fileUri).addOnSuccessListener(taskSnapshot -> {
            Toast.makeText(this, "Resource uploaded successfully!", Toast.LENGTH_SHORT).show();
            fetchResources(); // Refresh the resource list
        }).addOnFailureListener(e -> {
            Toast.makeText(this, "Failed to upload resource!", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "Error uploading resource: ", e);
        });
    }

    /**
     * Extracts the file name from the given URI.
     *
     * @param uri The URI of the file.
     * @return The file name.
     */
    private String getFileName(Uri uri) {
        String fileName = "";
        if (uri.getScheme().equals("content")) {
            try (Cursor cursor = getContentResolver().query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    fileName = cursor.getString(cursor.getColumnIndexOrThrow(OpenableColumns.DISPLAY_NAME));
                }
            }
        } else {
            fileName = uri.getLastPathSegment();
        }
        return fileName;
    }

    /**
     * Open a resource by downloading its URL and launching it in a browser or external app.
     *
     * @param resourceName The name of the resource.
     */
    private void openResource(String resourceName) {
        groupFolderRef.child(resourceName).getDownloadUrl().addOnSuccessListener(uri -> {
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            startActivity(intent);
        }).addOnFailureListener(e -> {
            Toast.makeText(this, "Failed to open resource!", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "Error opening resource: ", e);
        });
    }

    /**
     * File picker launcher using the Activity Result API.
     */
    private final ActivityResultLauncher<String> openFilePicker = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    uploadResource(uri); // Upload the selected file
                } else {
                    Toast.makeText(this, "No file selected", Toast.LENGTH_SHORT).show();
                }
            }
    );
}
