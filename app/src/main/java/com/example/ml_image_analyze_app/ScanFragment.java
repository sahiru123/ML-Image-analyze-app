package com.example.ml_image_analyze_app;

import static android.app.Activity.RESULT_OK;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

import com.airbnb.lottie.LottieAnimationView;

public class ScanFragment extends Fragment {
    private ImageView imageView;
    private Button scanButton;
    private LottieAnimationView animationView;
    private LottieAnimationView scanningAnimationView;

    private static final int REQUEST_CODE_CAMERA = 100;

    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    openCamera();
                } else {
                    // Handle case where permission is denied
                    // You might want to show a message to the user
                }
            });

    private final ActivityResultLauncher<Intent> cameraLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    // Handle the image capture result here
                    // For example, you can retrieve the captured image from the intent
                    Intent data = result.getData();
                    if (data != null && data.getExtras() != null) {
                        Bitmap imageBitmap = (Bitmap) data.getExtras().get("data");
                        if (imageBitmap != null) {
                            // Set the captured image to the ImageView
                            imageView.setImageBitmap(imageBitmap);
                            // Set scanning animation to visible
                            scanningAnimationView.setVisibility(View.VISIBLE);
                            // Hide the button and other animation
                            scanButton.setVisibility(View.GONE);
                            animationView.setVisibility(View.GONE);
                        }
                    }
                } else {
                    // Handle the case where image capture was not successful
                    // You might want to show an error message to the user
                }
            });


    public ScanFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_scan, container, false);
        imageView = view.findViewById(R.id.imageView);
        scanButton = view.findViewById(R.id.scanbutton);
        animationView = view.findViewById(R.id.animationView);
        scanningAnimationView = view.findViewById(R.id.scanningAnimationView);

        // Initially hide the scanning animation
        scanningAnimationView.setVisibility(View.GONE);

        scanButton.setOnClickListener(v -> openCamera());
        return view;
    }

    public void openCamera() {
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            // Request camera permission if not granted
            requestPermissionLauncher.launch(Manifest.permission.CAMERA);
        } else {
            // Start camera intent
            Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            cameraLauncher.launch(cameraIntent);
        }
    }
}
