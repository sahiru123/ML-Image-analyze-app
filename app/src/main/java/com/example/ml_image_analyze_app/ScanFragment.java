package com.example.ml_image_analyze_app;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
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

import com.clarifai.channel.ClarifaiChannel;
import com.clarifai.credentials.ClarifaiCallCredentials;
import com.clarifai.grpc.api.Concept;
import com.clarifai.grpc.api.Data;
import com.clarifai.grpc.api.Image;
import com.clarifai.grpc.api.Input;
import com.clarifai.grpc.api.MultiOutputResponse;
import com.clarifai.grpc.api.PostModelOutputsRequest;
import com.clarifai.grpc.api.UserAppIDSet;
import com.clarifai.grpc.api.V2Grpc;
import com.clarifai.grpc.api.status.StatusCode;
import com.google.protobuf.ByteString;


import java.io.ByteArrayOutputStream;
import java.util.List;


public class ScanFragment extends Fragment {
    private static final String TAG = "ScanFragment"; // Define a tag for logging
    private static final int CAMERA_PERMISSION_REQUEST_CODE = 100;
    private static final int WIDTH = 1024;
    private static final int HEIGHT = 768;
    private TextView mealNameTextView;

    private ImageView imageView;
    private ActivityResultLauncher<Void> cameraLauncher;
    private ActivityResultLauncher<String> galleryLauncher;
    static final String PAT = "f094eaac4c304f83822ea724493dbafd";
    static final String USER_ID = "clarifai";
    static final String APP_ID = "main";
    static final String MODEL_ID = "food-item-recognition";
    static final String MODEL_VERSION_ID = "1d5fd481e0cf4826aa72ec3ff049e044";
 

    public ScanFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_scan, container, false);

        Button camBtn = view.findViewById(R.id.cambtn);
        Button galleryBtn = view.findViewById(R.id.gallopnbtn);
        mealNameTextView = view.findViewById(R.id.mealname);

        imageView = view.findViewById(R.id.iamgecontainer);
        cameraLauncher = registerForActivityResult(new ActivityResultContracts.TakePicturePreview(), result -> {
            if (result != null && result instanceof Bitmap) {
                Bitmap imageBitmap = (Bitmap) result;
                imageView.setImageBitmap(imageBitmap);
                analyzeImage(imageBitmap, mealNameTextView);
            } else {
                Log.e(TAG, "Error: Unable to capture image.");
            }
        });

        galleryLauncher = registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
            if (uri != null) {
                try {
                    Bitmap originalBitmap = MediaStore.Images.Media.getBitmap(requireContext().getContentResolver(), uri);
                    Bitmap resizedBitmap = resizeBitmap(originalBitmap, WIDTH, HEIGHT); // Adjust TARGET_WIDTH and TARGET_HEIGHT as needed
                    imageView.setImageBitmap(resizedBitmap);
                    analyzeImage(resizedBitmap, mealNameTextView);
                } catch (Exception e) {
                    Log.e(TAG, "Error loading image from gallery: " + e.getMessage());
                }
            }
        });


        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(), new String[]{Manifest.permission.CAMERA},
                    CAMERA_PERMISSION_REQUEST_CODE);
        } else {
            setCameraButtonClickListener(camBtn);
        }

        galleryBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "Gallery button clicked");
                dispatchGalleryIntent();
            }
        });

        return view;
    }

    private void setCameraButtonClickListener(Button camBtn) {
        camBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "Camera button clicked");
                dispatchTakePictureIntent();
            }
        });
    }
    private Bitmap resizeBitmap(Bitmap originalBitmap, int targetWidth, int targetHeight) {
        float scaleRatio = Math.min(
                (float) targetWidth / originalBitmap.getWidth(),
                (float) targetHeight / originalBitmap.getHeight()
        );
        int width = Math.round(scaleRatio * originalBitmap.getWidth());
        int height = Math.round(scaleRatio * originalBitmap.getHeight());
        return Bitmap.createScaledBitmap(originalBitmap, width, height, false);
    }

    private void dispatchTakePictureIntent() {
        cameraLauncher.launch(null);
    }

    private void dispatchGalleryIntent() {
        galleryLauncher.launch("image/*");
    }

    private void analyzeImage(Bitmap bitmap, TextView mealNameTextView) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                // Resize the image
                int targetWidth = 800; // Set your desired width
                int targetHeight = 600; // Set your desired height
                Bitmap resizedBitmap = Bitmap.createScaledBitmap(bitmap, targetWidth, targetHeight, true);

                // Perform image analysis
                ByteString byteString = ByteString.copyFrom(Base64.decode(bitmapToBase64(resizedBitmap), Base64.DEFAULT));
                Image image = Image.newBuilder().setBase64(byteString).build();

                V2Grpc.V2BlockingStub stub = V2Grpc.newBlockingStub(ClarifaiChannel.INSTANCE.getGrpcChannel())
                        .withCallCredentials(new ClarifaiCallCredentials(PAT));

                MultiOutputResponse postModelOutputsResponse = stub.postModelOutputs(
                        PostModelOutputsRequest.newBuilder()
                                .setUserAppId(UserAppIDSet.newBuilder().setUserId(USER_ID).setAppId(APP_ID))
                                .setModelId(MODEL_ID)
                                .setVersionId(MODEL_VERSION_ID)
                                .addInputs(
                                        Input.newBuilder().setData(
                                                Data.newBuilder().setImage(image)
                                        )
                                )
                                .build()
                );

                // Process the response on the UI thread
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        handleModelOutputsResponse(postModelOutputsResponse, mealNameTextView);
                    }
                });
            }
        }).start();
    }



    private void handleModelOutputsResponse(MultiOutputResponse postModelOutputsResponse, TextView mealNameTextView) {
        if (postModelOutputsResponse.getStatus().getCode() == StatusCode.SUCCESS) {
            List<Concept> concepts = postModelOutputsResponse.getOutputs(0).getData().getConceptsList();
            Concept highestConcept = null;
            double highestValue = Double.MIN_VALUE;

            for (Concept concept : concepts) {
                Log.d(TAG, "Concept: " + concept.getName() + ", Value: " + concept.getValue());

                if (concept.getValue() > highestValue) {
                    highestValue = concept.getValue();
                    highestConcept = concept;
                }
            }

            if (highestConcept != null) {
                String highestConceptName = highestConcept.getName();
                mealNameTextView.setText(highestConceptName);
            }
        } else {
            Log.e(TAG, "Failed to analyze image. Status: " + postModelOutputsResponse.getStatus());
        }
    }


    private String bitmapToBase64(Bitmap bitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
        byte[] byteArray = byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(byteArray, Base64.DEFAULT);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Camera permission granted, set click listener for camera button
                Button camBtn = requireView().findViewById(R.id.cambtn);
                setCameraButtonClickListener(camBtn);
            }
        }
    }
}
