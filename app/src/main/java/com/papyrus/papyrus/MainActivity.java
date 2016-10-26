package com.papyrus.papyrus;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.Message;
import android.provider.MediaStore;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class MainActivity extends AppCompatActivity {
    private DrawingView drawView;
    private ImageButton currPaint, drawBtn, eraseBtn, newBtn, saveBtn;
    private static final String TAG = "MainActivity";
    public BlockingQueue<byte[]> outcomeMessageQueue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        try {
            super.onCreate(savedInstanceState);

            this.outcomeMessageQueue = new ArrayBlockingQueue<>(1200);
            BlockingQueue<byte[]> incomeMessageQueue = new ArrayBlockingQueue<>(1200);

            setContentView(R.layout.activity_main);
            drawView = (DrawingView) findViewById(R.id.drawing);
            drawView.setQueue(outcomeMessageQueue);

            int port = 11500;
            UDP_Server server = new UDP_Server(port, outcomeMessageQueue);
            UDP_Client client = new UDP_Client(port, incomeMessageQueue);

            DataProcessor incomeDataProcessor = new DataProcessor(incomeMessageQueue, drawView);

            ExecutorService executorService = Executors.newFixedThreadPool(3);
            executorService.submit(client);
            executorService.submit(server);
            executorService.submit(incomeDataProcessor);
            try {
                outcomeMessageQueue.put("REGISTER".getBytes());
            } catch (InterruptedException e) {
            }


            LinearLayout paintLayout = (LinearLayout) findViewById(R.id.paint_colors2);
            currPaint = (ImageButton) paintLayout.getChildAt(4);
            currPaint.setImageResource(R.drawable.paint_pressed);

            drawBtn = (ImageButton) findViewById(R.id.brush_btn);
            drawBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    drawView.setErase(false);
                }
            });


            eraseBtn = (ImageButton) findViewById(R.id.erase_btn);
            eraseBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    drawView.setErase(true);
                    Log.i(TAG, "Erase is true");
                }
            });

            newBtn = (ImageButton) findViewById(R.id.new_btn);
            newBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    drawView.setErase(false);
                    AlertDialog.Builder newDialog = new AlertDialog.Builder(MainActivity.this);
                    newDialog.setTitle("New drawing");
                    newDialog.setMessage("Start new drawing (you will lose the current drawing)?");
                    newDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            drawView.startNew();
                            dialog.dismiss();
                        }
                    });
                    newDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });
                    newDialog.show();
                }
            });

            saveBtn = (ImageButton) findViewById(R.id.save_btn);
            saveBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    drawView.setErase(true);
                    AlertDialog.Builder saveDialog = new AlertDialog.Builder(MainActivity.this);
                    saveDialog.setTitle("Save drawing");
                    saveDialog.setMessage("Save drawing to device Gallery?");
                    saveDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            //save drawing
                            drawView.setDrawingCacheEnabled(true);
                            //attempt to save

                            String imgSaved = MediaStore.Images.Media.insertImage(
                                    MainActivity.this.getContentResolver(), drawView.getDrawingCache(),
                                    UUID.randomUUID().toString() + ".png", "drawing");
                            String file_path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).getAbsolutePath();
                            Log.i(TAG, file_path);
                            //feedback
                            if (imgSaved != null) {
                                Toast savedToast = Toast.makeText(getApplicationContext(),
                                        "Drawing saved to Gallery!", Toast.LENGTH_SHORT);
                                savedToast.show();
                            } else {
                                Toast unsavedToast = Toast.makeText(getApplicationContext(),
                                        "Oops! Image could not be saved.", Toast.LENGTH_SHORT);
                                unsavedToast.show();
                            }
                            drawView.destroyDrawingCache();
                        }
                    });
                    saveDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });
                    saveDialog.show();
                }
            });
        } catch (Exception e) {
            Log.e(TAG, e.toString());
        }
    }

    public void paintClicked(View view) {
        drawView.setErase(false);
        //use chosen color
        if (view != currPaint) {
            //update color
            ImageButton imgView = (ImageButton) view;
            String color = view.getTag().toString();
            Log.i(TAG, color);
            drawView.setColor(color);
            imgView.setImageResource(R.drawable.paint_pressed);
            currPaint.setImageResource(R.drawable.paint);
            currPaint = (ImageButton) view;
            Log.i(TAG, currPaint.toString());
        }
    }
}
