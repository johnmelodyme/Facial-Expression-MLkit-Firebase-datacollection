package com.johnmelodyme.facialexpressionml;
/*
 * Copyright 2020 © John Melody Melissa
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 *
 *  *  I Actually hate creating this app but My baby
 *  encourage me and say that I can ,
 *  SO I do So.
 *
 * @Author : John Melody Melissa
 * @Copyright: John Melody Melissa & Tan Sin Dee © Copyright 2020
 * @INPIREDBYGF: Cindy Tan <3
 * @Class: FacialExpressionActivity.class
 *
 */

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Rect;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.camerakit.CameraKitView;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.face.FirebaseVisionFace;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetector;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetectorOptions;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.Viewport;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;
import com.johnmelodyme.facialexpressionml.Helper.GraphicOverlay;
import com.johnmelodyme.facialexpressionml.Helper.RectOverlay;
import com.johnmelodyme.facialexpressionml.Tools.FileUtil;
import com.johnmelodyme.facialexpressionml.Tools.ScreenGrab;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;
import com.muddzdev.styleabletoast.StyleableToast;
import com.wonderkiln.camerakit.CameraKit;
import com.wonderkiln.camerakit.CameraKitError;
import com.wonderkiln.camerakit.CameraKitEvent;
import com.wonderkiln.camerakit.CameraKitEventListener;
import com.wonderkiln.camerakit.CameraKitImage;
import com.wonderkiln.camerakit.CameraKitVideo;
import com.wonderkiln.camerakit.CameraView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Random;

import cn.pedant.SweetAlert.SweetAlertDialog;
import dmax.dialog.SpotsDialog;

public class FaceExpressionActivity extends AppCompatActivity {
    public static final String TAG = "ML";
    public static final String FILE_NAME = "FacialExpressionDetectionResult.txt";
    private AppCompatActivity GET_DIR = FaceExpressionActivity.this;
    private static final int CAMERA_REQUEST_CODE = 0x1;
    private StorageReference CLOUD_STORAGE;
    private TextToSpeech TEXT_TO_SPEECH;
    private MediaPlayer AI_SAY;
    private android.app.AlertDialog ALERT_PROMPT, LOADING, UPLOADING;
    private FirebaseAuth FIREBASEAUTH;
    private CameraView CAMERA_VIEW;
    private CameraKitView CAMERA_KIT;
    private GraphicOverlay GRAPHIC_OVERLAY;
    private RadioButton OK, SOSO, Pre_Severe, Severe, RADhappy, RADsad, RADneutral, RADother;
    private RadioGroup RG_Emotion;
    private Button Analyse;
    private FrameLayout parentView;
    private TextView Emotion_result, ACCURACY, E_MOTION, EMOJI, EMOJI_RESULT;
    private Bitmap bitmap;
    Thread thread, g;
    private String E, S;
    private String[] Data;
    private int D;
    private Handler handler;
    Random ran = new Random();
    float d_classification;
    int TIME = 0x5;
    private GraphView GRAPH;
    private LineGraphSeries<DataPoint> DATA;

    public void DeclarationInit(){
        CLOUD_STORAGE = FirebaseStorage.getInstance().getReference();
        FIREBASEAUTH = FirebaseAuth.getInstance();
        EMOJI_RESULT = findViewById(R.id.emoji_result);
        Emotion_result = findViewById(R.id.emotion);
        CAMERA_VIEW = findViewById(R.id.CAMERA);
        GRAPHIC_OVERLAY = findViewById(R.id.GO);
        ACCURACY = findViewById(R.id.cal);
        RADhappy = findViewById(R.id.Happy);
        RADneutral = findViewById(R.id.Neutral);
        RADsad = findViewById(R.id.Sad);
        RADother = findViewById(R.id.Other);
        RG_Emotion = findViewById(R.id.RADIO_GROUP_EMOTION);
        OK = findViewById(R.id.ok);
        SOSO = findViewById(R.id.soso);
        Pre_Severe = findViewById(R.id.presevere);
        Severe = findViewById(R.id.severe);
        Analyse = findViewById(R.id.analyse);
        E_MOTION = findViewById(R.id.e_motion);
        EMOJI = findViewById(R.id.emoji);
        handler = new Handler();
        parentView = findViewById(R.id.frameLayout);
        GRAPH = findViewById(R.id.graph);
        d_classification = ran.nextFloat();
        DATA = new LineGraphSeries<>();
        GRAPH.addSeries(DATA);
        Viewport viewport = GRAPH.getViewport();
        viewport.setYAxisBoundsManual(true);
        viewport.setMinY(0x0);
        viewport.setMaxY(0x1);
        viewport.setScrollable(true);
        Data = getResources().getStringArray(R.array.emo);
        D = new Random().nextInt(Data.length);
        E = Data[D];
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.d(TAG, "onCreate: " + FaceExpressionActivity.class.getCanonicalName());
        DeclarationInit();
        // TODO TEXT TO SPEECH:
        TEXT_TO_SPEECH = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int STATUS) {
                if (TextToSpeech.ERROR != STATUS) {
                    TEXT_TO_SPEECH.setLanguage(Locale.UK);
                    Log.d(TAG, "onInit: TTS__STARTED");
                }
            }
        });

        ALERT_PROMPT = new SpotsDialog
                .Builder()
                .setContext(FaceExpressionActivity.this)
                .setMessage("Logging Out...")
                .setCancelable(false)
                .build();

        UPLOADING = new SpotsDialog
                .Builder()
                .setContext(FaceExpressionActivity.this)
                .setMessage("Uploading to the server...")
                .setCancelable(false)
                .build();

        LOADING = new SpotsDialog
                .Builder()
                .setContext(FaceExpressionActivity.this)
                .setMessage("Loading...")
                .setCancelable(false)
                .build();


        //TODO THREAD:
        thread = new Thread() {
            @Override
            public void run() {
                try {
                    while (!isInterrupted()) {
                        Thread.sleep(0x3e8);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                // TODO CONVERT INT TO STRING
                                Random random = new Random();
                                //int RandomData = R.nextInt(EMOTIONS.length);
                                double classification;
                                String[] Data;
                                int D, COUNT;
                                COUNT = 0x0;
                                Data = getResources().getStringArray(R.array.emo);
                                D = new Random().nextInt(Data.length);
                                E = Data[D];
                                classification = random.nextDouble();
                                S = String.valueOf(classification);
                                ACCURACY.setText(S);
                                Emotion_result.setText(E);
                            }
                        });
                    }
                } catch (InterruptedException e) {
                    //Emotion_result.setText("");
                }
            }
        };
        thread.start();
        //thread.stop();

        // TODO CAMERA_VIEW.ADD_CAMERA_LISTENER:
        CAMERA_VIEW.addCameraKitListener(new CameraKitEventListener() {
            @Override
            public void onEvent(CameraKitEvent cameraKitEvent) {
            }
            @Override
            public void onError(CameraKitError cameraKitError) {
            }
            @Override
            public void onImage(CameraKitImage cameraKitImage) {
                LOADING.show();
//                KProgressHUD hud = KProgressHUD.create(MainActivity.this)
//                        .setStyle(KProgressHUD.Style.ANNULAR_DETERMINATE)
//                        .setLabel("Please wait")
//                        .setMaxProgress(30)
//                        .show();
//                hud.show();
                Bitmap bitmap = cameraKitImage.getBitmap();
                bitmap = Bitmap.createScaledBitmap(bitmap, CAMERA_VIEW.getWidth(), CAMERA_VIEW.getHeight(), false);
                CAMERA_VIEW.stop();
                PROCESS_FACE_DETECTION(bitmap);
                Log.d(TAG, "onImage: CAMERAVIEW is Running");
                CAPTURE_DATA_SAVE();
                // TODO EXTERN:
                SAVE_TO_SD();
            }

            @Override
            public void onVideo(CameraKitVideo cameraKitVideo) {
            }
        });

        // TODO CAMERA_VIEW ON CLICK:
        CAMERA_VIEW.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Emotion_result.setText("");
                CAMERA_VIEW.start();
                CAMERA_VIEW.captureImage();
                GRAPHIC_OVERLAY.clear();
            }
        });

        // TODO ANALYSE ONCLICK:
        Analyse.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onClick(View v) {
                AI_SAY();

                final String DONE, load;
                String USER_STRESS_VALUE, USER_EMOTION_PREDICTION;

                USER_STRESS_VALUE = E_MOTION.getText()
                        .toString()
                        .trim();
                USER_EMOTION_PREDICTION =EMOJI.getText()
                        .toString()
                        .trim();

                DONE = v.getResources().getString(R.string.analyse_after_done);
                load = v.getResources().getString(R.string.analyse_after);
                // TODO SAVE_TO_SD:
                //CAPTURE_DATA_SAVE();
                CAMERA_VIEW.start();
                GRAPHIC_OVERLAY.clear();
                Analyse.setText(load);
                AI_SAY();
                if (OK.isChecked() || RADhappy.isChecked()) {
                    Analyse.setText(DONE);
                    E_MOTION.setText("Stress-Level: 0%");
                } else if (SOSO.isChecked() || RADhappy.isChecked()) {
                    Analyse.setText(DONE);
                    E_MOTION.setText("Stress-Level: 25%");
                } else if (Pre_Severe.isChecked() || RADhappy.isChecked()) {
                    Analyse.setText(DONE);
                    E_MOTION.setText("Stress-Level: 50%");
                } else if (Severe.isChecked() || RADhappy.isChecked()) {
                    Analyse.setText(DONE);
                    E_MOTION.setText("Stress-Level: 100%");
                } else if (OK.isChecked() || RADsad.isChecked()) {
                    Analyse.setText(DONE);
                    E_MOTION.setText("Stress-Level: 0%");
                } else if (SOSO.isChecked() || RADsad.isChecked()) {
                    Analyse.setText(DONE);
                    E_MOTION.setText("Stress-Level: 25%");
                } else if (Pre_Severe.isChecked() || RADsad.isChecked()) {
                    Analyse.setText(DONE);
                    E_MOTION.setText("Stress-Level: 50%");
                } else if (Severe.isChecked() || RADsad.isChecked()) {
                    Analyse.setText(DONE);
                    E_MOTION.setText("Stress-Level: 100%");
                } else if (OK.isChecked() || RADneutral.isChecked()) {
                    Analyse.setText(DONE);
                    E_MOTION.setText("Stress-Level: 0%");
                } else if (SOSO.isChecked() || RADneutral.isChecked()) {
                    Analyse.setText(DONE);
                    E_MOTION.setText("Stress-Level: 25%");
                } else if (Pre_Severe.isChecked() || RADneutral.isChecked()) {
                    Analyse.setText(DONE);
                    E_MOTION.setText("Stress-Level: 50%");
                } else if (Severe.isChecked() || RADneutral.isChecked()) {
                    Analyse.setText(DONE);
                    E_MOTION.setText("Stress-Level: 100%");
                } else if (OK.isChecked() || RADother.isChecked()) {
                    Analyse.setText(DONE);
                    E_MOTION.setText("Stress-Level: 0%");
                } else if (SOSO.isChecked() || RADother.isChecked()) {
                    Analyse.setText(DONE);
                    E_MOTION.setText("Stress-Level: 25%");
                } else if (Pre_Severe.isChecked() || RADother.isChecked()) {
                    Analyse.setText(DONE);
                    E_MOTION.setText("Stress-Level: 50%");
                } else if (Severe.isChecked() || RADother.isChecked()) {
                    Analyse.setText(DONE);
                    E_MOTION.setText("Stress-Level: 100%");
                } else {
                    Log.d(TAG, " classification :: null");
                }
                Log.d(TAG,  " User is :  " + E);
                EMOJI.setText("User is :  " + "\"" + E + "\"");
                Show_EMOJI();
                thread.interrupt();
                //CAPTURE_PICTURE_FROM_CAMERA_VIEW();
                CAPTURE_DATA_SAVE();
                // TODO EXTERN:
                SAVE_TO_SD();
                EXPORT_TXT();
            }
        });

        // TODO: setOnLongClickListener : ANALYSE:
        Analyse.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                String click;
                click = getResources().getString(R.string.analyse);
                E_MOTION.setText(" ");
                Analyse.setText(click);
                GRAPHIC_OVERLAY.clear();
                CAMERA_VIEW.stop();
                return true;
            }
        });
    }

    // TODO exportTXT:
    public void EXPORT_TXT() {
        FileOutputStream fos = null;
        String CurrentDateTime;
        CurrentDateTime = java.text.DateFormat.getDateTimeInstance().format(new Date());
        File file =  new File(FaceExpressionActivity.this.getApplicationContext().getFilesDir(), FILE_NAME);
        if (!file.exists()) {
            file.mkdir();
            //writer.append(CurrentDateTime).append("User is :  ").append("\"").append(E).append("\"").append("Accuracies: ").append(S);
        }
        try {
          fos = openFileOutput(FILE_NAME, MODE_APPEND);
          fos.write(Integer.parseInt(CurrentDateTime + "User is " + E + "," + "Accuracy:" + S));
            new StyleableToast
                    .Builder(FaceExpressionActivity.this)
                    .text("Export Result to " + getFilesDir() + "/" + FILE_NAME)
                    .textColor(Color.WHITE)
                    .backgroundColor(Color.rgb(255,20,147))
                    .show();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    // TODO CAPTURE IMAGE:
//    private void CAPTURE_PICTURE_FROM_CAMERA_VIEW() {
//        CAMERA_VIEW.captureImage(new CameraView ){
//            @Override
//            public void callback(CameraKitImage cameraKitImage, final byte[] photo) {
//                File SavePhoto;
//                SavePhoto = new File(Environment.getExternalStorageState(), "User_data_output.png");
//                FileOutputStream outputStream;
//                try {
//                    outputStream = new FileOutputStream(SavePhoto.getPath());
//                    outputStream.write();
//                } catch (FileNotFoundException e) {
//                    e.printStackTrace();
//                }
//            }
//        });
//    }

    //TODO SHOW EMOJI:
    public void Show_EMOJI() {
        if (E.equals("Happy \uD83D\uDE0A")){
            EMOJI_RESULT.setText("\uD83D\uDE0A");
        } else if (E.equals("Sad \uD83D\uDE25")){
            EMOJI_RESULT.setText("\uD83D\uDE25");
        } else if (E.equals("Horny \uD83D\uDE08")){
            EMOJI_RESULT.setText("\uD83D\uDE08");
        } else if (E.equals("Disgust \uD83D\uDE35")){
            EMOJI_RESULT.setText("\uD83D\uDE35");
        } else if (E.equals("Surprised \uD83D\uDE32")){
            EMOJI_RESULT.setText("\uD83D\uDE32");
        } else if (E.equals("Tired \uD83D\uDE2A")){
            EMOJI_RESULT.setText("\uD83D\uDE2A");
        } else if (E.equals("Angry \uD83D\uDE21")){
            EMOJI_RESULT.setText("\uD83D\uDE21");
        } else if (E.equals("Confused \uD83E\uDD14")){
            EMOJI_RESULT.setText("\uD83E\uDD14");
        } else if (E.equals("Neutral \uD83D\uDE10")){
            EMOJI_RESULT.setText("\uD83D\uDE10");
        } else if (E.equals("Excited \uD83D\uDE32")){
            EMOJI_RESULT.setText("\uD83D\uDE32");
        }else {
            Log.d(TAG, "NO EMOTION.....");
        }
    }

    // TODO AI_SAY
    public void AI_SAY() {
        AI_SAY = MediaPlayer.create(FaceExpressionActivity.this, R.raw.analyzing);
        AI_SAY.setVolume(0x64, 0x64);
        AI_SAY.start();
        AI_SAY.stop();
        Log.d(TAG, "AI_SAY: ====> {ANALYZING............}");
    }

    // TODO SAVETOSD:
    public void SAVE_TO_SD() {
        /*
         * TODO SAVE_IMG_TO_PATH
         * Requesting storage permission
         * Once the permission granted, screen shot captured
         * On permanent denial show toast
         */
        Dexter.withActivity(FaceExpressionActivity.this).withPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .withListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse response) {
                        String POS_MSG, NEG_MSG;
                        POS_MSG = getResources().getString(R.string.save_img);
                        NEG_MSG = getResources().getString(R.string.save_img_failed);

                        if (bitmap != null) {
                            String PATH_TO_STORAGE;
                            PATH_TO_STORAGE = Environment.getExternalStorageDirectory().toString() + "/user_data.png";
                            FileUtil.getInstance().storeBitmap(bitmap, PATH_TO_STORAGE);
                            new StyleableToast
                                    .Builder(FaceExpressionActivity.this)
                                    .text(POS_MSG + "\t" + PATH_TO_STORAGE)
                                    .textColor(Color.WHITE)
                                    .backgroundColor(Color.rgb(255,20,147))
                                    .show();
                            Log.d(TAG, "onPermissionGranted: " + POS_MSG + "\t" + PATH_TO_STORAGE);
                        } else {
                            new StyleableToast
                                    .Builder(FaceExpressionActivity.this)
                                    .text(NEG_MSG)
                                    .textColor(Color.WHITE)
                                    .backgroundColor(Color.rgb(255,20,147))
                                    .show();
                            Log.d(TAG, "onPermissionGranted: " + NEG_MSG);
                        }
                    }

                    @Override
                    public void onPermissionDenied(PermissionDeniedResponse USER_RESPONSE) {
                        //Check for permanent denial of permission
                        if (USER_RESPONSE.isPermanentlyDenied()){
                            new StyleableToast
                                    .Builder(FaceExpressionActivity.this)
                                    .text(getString(R.string.sdx_grant_storage))
                                    .textColor(Color.WHITE)
                                    .backgroundColor(Color.rgb(255,20,147))
                                    .show();
                        } else {
                            Log.d(TAG, "onPermissionDenied: {-1}");
                        }
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {
                        token.continuePermissionRequest();
                    }
                }).check();
    }


    // TODO SAVE_TO_IMAGE:
    public void CAPTURE_DATA_SAVE() {
        CAMERA_VIEW.captureImage();
        bitmap = ScreenGrab.getInstance().takeScreenshotForView(CAMERA_VIEW);
        //bitmap = ScreenGrab.getInstance().takeScreenshotForScreen(activity);
        Log.d(TAG, "CAPTURE_DATA_SAVE:  IMAGE CAPTURED");
    }

    @Override
    protected void onActivityResult(int requestcode, int resultcode, Intent data) {
        super.onActivityResult(requestcode, resultcode, data);

        if (requestcode == CAMERA_REQUEST_CODE && resultcode == RESULT_OK){
            UPLOADING.show();
            Uri uri = data.getData();
            assert uri != null;
            StorageReference filepath = CLOUD_STORAGE.child("FACES").child(Objects.requireNonNull(uri.getLastPathSegment()));
            filepath.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    UPLOADING.dismiss();
                    new StyleableToast
                            .Builder(FaceExpressionActivity.this)
                            .text("Exported to the database...{ok}")
                            .textColor(Color.WHITE)
                            .backgroundColor(Color.rgb(255,20,147))
                            .show();
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    UPLOADING.dismiss();
                    new StyleableToast
                            .Builder(FaceExpressionActivity.this)
                            .text("404: Server Error :( ")
                            .textColor(Color.WHITE)
                            .backgroundColor(Color.rgb(255,20,147))
                            .show();
                }
            });
        }
    }

    private boolean isExternalStorageAvailable(){
        if(Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())){
            Log.w(TAG, "SDCARD WRITABLE======>");
            return true;
        } else {
            return false;
        }
    }

    // TODO PROCESS_FACE_DETECTION:
    private void PROCESS_FACE_DETECTION(Bitmap bitmap) {
        FirebaseVisionImage firebaseVisionImage;
        firebaseVisionImage = FirebaseVisionImage.fromBitmap(bitmap);
        FirebaseVisionFaceDetectorOptions firebaseVisionFaceDetectorOptions;
        firebaseVisionFaceDetectorOptions = new FirebaseVisionFaceDetectorOptions
                .Builder()
                .build();
        FirebaseVisionFaceDetector firebaseVisionFaceDetector = FirebaseVision.getInstance()
                .getVisionFaceDetector(firebaseVisionFaceDetectorOptions);
        firebaseVisionFaceDetector.detectInImage(firebaseVisionImage)
                .addOnSuccessListener(new OnSuccessListener<List<FirebaseVisionFace>>() {
                    @Override
                    public void onSuccess(List<FirebaseVisionFace> firebaseVisionFaces) {
                        getFaceResult(firebaseVisionFaces);
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                //ROTIBAKAR("Error: " + exception.getMessage());
                new StyleableToast
                        .Builder(FaceExpressionActivity.this)
                        .text("Error: " + exception.getMessage())
                        .textColor(Color.WHITE)
                        .backgroundColor(Color.rgb(0xff, 0x14, 0x93))
                        .show();
                Log.d(TAG, "FE" + ":" + " Error: " + exception.getMessage());
            }
        });
    }

    // TODO GET_FACE_RESULT:
    private void getFaceResult(List<FirebaseVisionFace> firebaseVisionFaces) {
        int COUNTER = 0x0;
        for (FirebaseVisionFace visionFace : firebaseVisionFaces) {
            Rect rect = visionFace.getBoundingBox();
            RectOverlay rectOverlay = new RectOverlay(GRAPHIC_OVERLAY, rect);
            GRAPHIC_OVERLAY.add(rectOverlay);
            COUNTER = COUNTER + 0x1;
        }
        LOADING.dismiss();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.signout) {
            Log.d(TAG, "onOptionsItemSelected: LOGGING OUT....");
            ALERT_PROMPT.show();
            FirebaseAuth.AuthStateListener AUTH;
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            FIREBASEAUTH.signOut();
            FirebaseAuth.getInstance().signOut();
            Intent backtologin;
            backtologin = new Intent(FaceExpressionActivity.this, Login.class);
            startActivity(backtologin);
            finish();
            /*
            AUTH = new FirebaseAuth.AuthStateListener() {
                @Override
                public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                    FirebaseUser user = FIREBASEAUTH.getCurrentUser();
                    if(user == null){
                        Intent backtologin;
                        backtologin = new Intent(MainActivity.this, Login.class);
                        startActivity(backtologin);
                        finish();
                    }
                }
            };
             */
            return true;
        }

        if (id == R.id.SwitchCamera) {
            if (CAMERA_VIEW.isFacingFront()) {
                CAMERA_VIEW.setFacing(CameraKit.Constants.FACING_BACK);
                Log.d(TAG, "onclick():<> Camera is now facing the BACK ==> {ok} ");
            } else {
                CAMERA_VIEW.setFacing(CameraKit.Constants.FACING_FRONT);
                Log.d(TAG, "onclick():<>  Camera is now facing the Front ==> {ok} ");
            }
            return true;
        }

        if (id == R.id.about) {
            Log.d(TAG, "onOptionsItemSelected: \"Developed by John Melody Melissa\" and Inspired by Sin Dee <3 ");
            new SweetAlertDialog(FaceExpressionActivity.this)
                    .setTitleText("Version 1.1.0")
                    .setContentText("Developed by John Melody Melissa \n Inspired by Sin Dee")
                    .show();
            return true;
        }

        if (id == R.id.freeze) {
            CAMERA_VIEW.start();
            CAMERA_VIEW.captureImage();
            GRAPHIC_OVERLAY.clear();
            Log.d(TAG, "onOptionsItemSelected: FREEZE");
            return false;
        }

        if (id == R.id.Setting) {
            Intent SETTING;
            SETTING = new Intent(FaceExpressionActivity.this, Preference.class);
            startActivity(SETTING);
            Log.d(TAG, "onOptionsItemSelected: SETTING");
            return true;
        }

        if (id == R.id.export){
            EXPORT_TXT();
            return false;
        }

        if (id == R.id.Profile){
            Intent toProfile;
            toProfile = new Intent(FaceExpressionActivity.this, UserProfile.class);
            startActivity(toProfile);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void ROTIBAKAR(String string) {
        Toast.makeText(FaceExpressionActivity.this, string,
                Toast.LENGTH_SHORT)
                .show();
    }

    @Override
    public void onPause() {
        super.onPause();
        CAMERA_VIEW.stop();
    }

    @Override
    protected void onResume() {
        super.onResume();
        CAMERA_VIEW.start();
        new Thread(new Runnable() {
            @Override
            public void run() {
                for (int i = 0x0; i < 0x3e8; i++) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            addEntry();
                        }
                    });
                    try {
                        Thread.sleep(0x64);
                    } catch (InterruptedException e) {
                        Log.d(TAG, "FE" + e);
                    }
                }
            }
        }).start();
    }

    private void addEntry() {
        DATA.appendData(new DataPoint(TIME++, ran.nextDouble()), false
                , 0x64);
    }

    @Override
    public void onBackPressed() {
        new SweetAlertDialog(this, SweetAlertDialog.WARNING_TYPE)
                .setTitleText("Sign Out? ")
                .setConfirmText("Yes.")
                .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(SweetAlertDialog sDialog) {
                        ALERT_PROMPT.show();
                        FIREBASEAUTH.signOut();
                        System.exit(0x0);
                        sDialog.dismissWithAnimation();
                    }
                })
                .setCancelButton("No.", new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(SweetAlertDialog sDialog) {
                        sDialog.dismissWithAnimation();
                    }
                }).show();

    }

    @Override
    public void onStart(){
        super.onStart();
        CAMERA_VIEW.start();
        // TODO: firebase server;
    }
//
//    @Override
//    public void onWindowFocusChanged(boolean hasFocus) {
//        super.onWindowFocusChanged(hasFocus);
//        if (hasFocus) {
//            hideSystemUI();
//        }
//    }
//
//    private void hideSystemUI() {
//        // Enables regular immersive mode.
//        // For "lean back" mode, remove SYSTEM_UI_FLAG_IMMERSIVE.
//        // Or for "sticky immersive," replace it with SYSTEM_UI_FLAG_IMMERSIVE_STICKY
//        View decorView = getWindow().getDecorView();
//        decorView.setSystemUiVisibility(
//                View.SYSTEM_UI_FLAG_IMMERSIVE
//                        // Set the content to appear under the system bars so that the
//                        // content doesn't resize when the system bars hide and show.
//                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
//                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
//                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
//                        // Hide the nav bar and status bar
//                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
//                        | View.SYSTEM_UI_FLAG_FULLSCREEN);
//    }
//
//    // Shows the system bars by removing all the flags
//    // except for the ones that make the content appear under the system bars.
//    private void showSystemUI() {
//        View decorView = getWindow().getDecorView();
//        decorView.setSystemUiVisibility(
//                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
//                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
//                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
//    }
}