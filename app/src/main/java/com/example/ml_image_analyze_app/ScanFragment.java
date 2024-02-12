package com.example.ml_image_analyze_app;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import org.tensorflow.lite.Interpreter;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;

// Import statements

public class ScanFragment extends Fragment {
    private static final String TAG = "ScanFragment";
    private static final int CAMERA_PERMISSION_REQUEST_CODE = 100;

    private ImageView imageView;
    private TextView mealNameTextView;
    private ActivityResultLauncher<Void> cameraLauncher;
    private ActivityResultLauncher<String> galleryLauncher;
    private Interpreter tflite;
    private List<String> labels;

    public ScanFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_scan, container, false);

        // Initialize views
        imageView = view.findViewById(R.id.iamgecontainer);
        mealNameTextView = view.findViewById(R.id.mealname);

        // Initialize TensorFlow Lite interpreter and model
        try {
            initializeTFLiteModel();
        } catch (IOException e) {
            Log.e(TAG, "Error initializing TensorFlow Lite model: " + e.getMessage());
        }

        // Initialize ActivityResultLauncher for camera intent
        cameraLauncher = registerForActivityResult(new ActivityResultContracts.TakePicturePreview(), result -> {
            if (result != null) {
                imageView.setImageBitmap(result);
                recognizeDishFromImage(result);
            }
        });

        // Initialize ActivityResultLauncher for gallery intent
        galleryLauncher = registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
            if (uri != null) {
                try {
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(requireContext().getContentResolver(), uri);
                    imageView.setImageBitmap(bitmap);
                    recognizeDishFromImage(bitmap);
                } catch (Exception e) {
                    Log.e(TAG, "Error loading image from gallery: " + e.getMessage());
                }
            }
        });

        // Set click listener for camera button
        Button camBtn = view.findViewById(R.id.cambtn);
        camBtn.setOnClickListener(v -> dispatchTakePictureIntent());

        // Set click listener for gallery button
        Button galleryBtn = view.findViewById(R.id.gallopnbtn);
        galleryBtn.setOnClickListener(v -> dispatchGalleryIntent());

        return view;
    }

    private void initializeTFLiteModel() throws IOException {
        // Load TensorFlow Lite model from assets folder
        tflite = new Interpreter(loadModelFile());
        labels = loadLabels();
    }

    private MappedByteBuffer loadModelFile() throws IOException {
        FileInputStream inputStream = new FileInputStream("");
        FileChannel fileChannel = inputStream.getChannel();
        long startOffset = fileChannel.position();
        long declaredLength = fileChannel.size();
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
    }

    private List<String> loadLabels() throws IOException {
        // Load labels for image classes
        List<String> labels = new ArrayList<>();
        // Load labels from a text file or other source
        return labels;
    }

    private void recognizeDishFromImage(Bitmap imageBitmap) {

        ByteBuffer inputBuffer = ;
        float[][] outputScores = new float[1][labels.size()];
        tflite.run(inputBuffer, outputScores);
        // Postprocess the output (get the predicted label)
        int predictedClassIndex = /* Find the index with highest probability */;
        String predictedLabel = labels.get(predictedClassIndex);
        mealNameTextView.setText(predictedLabel);
    }

    private void dispatchTakePictureIntent() {
        cameraLauncher.launch(null);
    }

    private void dispatchGalleryIntent() {
        galleryLauncher.launch("image/*");
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Camera permission granted, enable camera functionality
                // For example, enable the camera button
                Button camBtn = requireView().findViewById(R.id.cambtn);
                camBtn.setEnabled(true);
            }
        }
    }
}
