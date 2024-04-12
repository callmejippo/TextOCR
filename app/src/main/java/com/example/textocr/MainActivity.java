package com.example.textocr;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.media.Image;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.label.ImageLabel;
import com.google.mlkit.vision.label.ImageLabeler;
import com.google.mlkit.vision.label.ImageLabeling;
import com.google.mlkit.vision.label.defaults.ImageLabelerOptions;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.chinese.ChineseTextRecognizerOptions;
import com.google.mlkit.vision.text.korean.KoreanTextRecognizerOptions;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    public ImageView imageView;
    public TextView textView;
    Context context;
    public LinearLayout linearLayout;

    private TextRecognitionActivity textRecognitionActivity;
    static final int REQUEST_IMAGE_CAPTURE = 1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        context = getApplicationContext();


        linearLayout = findViewById(R.id.linear_label);
        Button captureBtn=findViewById(R.id.capture);
        Button detectBtn=findViewById(R.id.detect);
        imageView=findViewById(R.id.image_view);
        textView=findViewById(R.id.text_display);

        String imageUrl = "https://th.bing.com/th/id/R.ef6b1c102f8c9d91c6d079844f701255?rik=J%2bYLlKfMffoaiw&riu=http%3a%2f%2fwww.pixelstalk.net%2fwp-content%2fuploads%2f2016%2f04%2fDesktop-landscape-wallpaper-HD-1.jpg&ehk=HxXkxFcxONLWomK82z%2fmLxWJIzu%2fX93piKlSHKfiKZA%3d&risl=&pid=ImgRaw&r=0";

        // Load and display the image using Picasso
        Picasso.get().load(imageUrl).into(imageView);


        captureBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //to be filled
//                CopyToClipBoard(textView.getText().toString());
                Bitmap bitmap = ((BitmapDrawable) imageView.getDrawable()).getBitmap();
                InputImage inputImage = InputImage.fromBitmap(bitmap, 0); // 0 degrees rotation
                recognizeLabel(inputImage);
            }
        });

        detectBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //to be filled
                runTextRecognition();
//                Bitmap bitmap = ((BitmapDrawable) imageView.getDrawable()).getBitmap();
//                recognizeTextFirebase(bitmap);


            }
        });


    }


    private void runTextRecognition() {
        Bitmap bitmap = ((BitmapDrawable) imageView.getDrawable()).getBitmap();
        InputImage inputImage = InputImage.fromBitmap(bitmap, 0); // 0 degrees rotation

        List<String> recognizedTexts = new ArrayList<>();

        // Create text recognizers for different languages
        TextRecognizer recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);
        TextRecognizer chineseRecognizer = TextRecognition.getClient(new ChineseTextRecognizerOptions.Builder().build());
        TextRecognizer koreanRecognizer = TextRecognition.getClient(new KoreanTextRecognizerOptions.Builder().build());

        // List of recognizers
        List<TextRecognizer> recognizers = new ArrayList<>();
        recognizers.add(recognizer);
//        recognizers.add(chineseRecognizer);
//        recognizers.add(koreanRecognizer);

        for (TextRecognizer r: recognizers) {
            Task<Text> result =
                    r.process(inputImage)
                            .addOnSuccessListener(new OnSuccessListener<Text>() {
                                @Override
                                public void onSuccess(Text visionText) {
                                    // Task completed successfully
                                    // ...
                                    String text = visionText.getText();
                                    Log.d("TAG", text);

                                    if(!text.isEmpty())
                                    {
                                        recognizedTexts.add(text);
                                        Log.d("TAG HEHE", text);


                                        textView.append(text);
                                    }
                                }
                            })
                            .addOnFailureListener(
                                    new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            // Task failed with an exception
                                            // ...
                                        }
                                    });
        }


    }

    private void recognizeText(InputImage image, List<TextRecognizer> recognizers, TextRecognitionCallback callback) {
        // Initialize a list to store non-null recognized strings
        List<String> recognizedTexts = new ArrayList<>();

        // Iterate through each recognizer
        for (TextRecognizer recognizer : recognizers) {
            recognizer.process(image)
                    .addOnSuccessListener(visionText -> {
                        // If text is recognized successfully and not empty, add it to the list
                        String text = visionText.getText();
                        Log.d("RECOG", recognizer.toString());
                        Log.d("TAG TEXT", text);
                        if (!text.isEmpty() && text != ""){
                            recognizedTexts.add(text);
                        }
                        // If all recognizers have completed, pass the list of recognized texts to the callback
                        if (recognizers.indexOf(recognizer) == recognizers.size() - 1) {
                            callback.onTextRecognized(recognizedTexts);
                        }
                    })
                    .addOnFailureListener(e -> {
                        // Handle failure if any recognizer fails
                        callback.onTextRecognitionFailed();
                    });
        }
    }

    // Callback interface for handling text recognition results
    interface TextRecognitionCallback {
        void onTextRecognized(List<String> recognizedTexts);
        void onTextRecognitionFailed();
    }


    private void  recognizeLabel(InputImage image)
    {
        ImageLabelerOptions options = new ImageLabelerOptions.Builder().setConfidenceThreshold(0.7f).build();
        ImageLabeler labeler = ImageLabeling.getClient(options);

        labeler.process(image).addOnSuccessListener(new OnSuccessListener<List<ImageLabel>>() {
            @Override
            public void onSuccess(List<ImageLabel> imageLabels) {
                Log.d("SUCCESS LABEL", "SUCCESS");
                for (ImageLabel label: imageLabels)
                {

                    String text = label.getText();
                    float confidence = label.getConfidence();
                    int index = label.getIndex();
                    TextView labelTextView = new TextView(getApplicationContext());
                    LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT, // Width
                            LinearLayout.LayoutParams.WRAP_CONTENT // Height
                    );
                    labelTextView.setLayoutParams(layoutParams);

                    labelTextView.setText("Label: " + text + "- Confidence: " + confidence + " - Index: " + index);
                    Log.d("label", text + "-" + confidence + " - "  + index);
                    // Add the TextView to the LinearLayout
                    linearLayout.addView(labelTextView);
                }

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e("ERROR LABEL", "FAILED");
            }
        });
    }


    private void processTextBlock(Text result) {
        // [START mlkit_process_text_block]
        String resultText = result.getText();
        Log.d("Text", resultText);
        for (Text.TextBlock block : result.getTextBlocks()) {
            String blockText = block.getText();
            Point[] blockCornerPoints = block.getCornerPoints();
            Rect blockFrame = block.getBoundingBox();
            for (Text.Line line : block.getLines()) {
                String lineText = line.getText();
                Point[] lineCornerPoints = line.getCornerPoints();
                Rect lineFrame = line.getBoundingBox();
                for (Text.Element element : line.getElements()) {
                    String elementText = element.getText();
                    Point[] elementCornerPoints = element.getCornerPoints();
                    Rect elementFrame = element.getBoundingBox();
                    for (Text.Symbol symbol : element.getSymbols()) {
                        String symbolText = symbol.getText();
                        Point[] symbolCornerPoints = symbol.getCornerPoints();
                        Rect symbolFrame = symbol.getBoundingBox();
                    }
                }
            }
        }
        // [END mlkit_process_text_block]
    }

    private void CopyToClipBoard(String text)
    {
        ClipboardManager clipboardManager = (ClipboardManager) getSystemService(context.CLIPBOARD_SERVICE);
        ClipData clipData = ClipData.newPlainText("Copied", text);
        clipboardManager.setPrimaryClip(clipData);
        Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT).show();
    }

}