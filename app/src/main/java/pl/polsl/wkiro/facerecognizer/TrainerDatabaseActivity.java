package pl.polsl.wkiro.facerecognizer;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.Toast;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Mat;

import java.util.ArrayList;
import java.util.List;

import pl.polsl.wkiro.facerecognizer.model.Face;
import pl.polsl.wkiro.facerecognizer.model.FaceDetector;
import pl.polsl.wkiro.facerecognizer.model.PictureHolder;

public class TrainerDatabaseActivity extends AppCompatActivity {

    private Mat frameRgba;
    private Mat frameGray;
    private List<Face> faces;
    private List<Mat> facesRgba;
    private List<Mat> facesGray;
    private List<EditText> editTexts;

    private FaceDetector faceDetector;
    private int sequentialNumber = 1;

    private BaseLoaderCallback loaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS: {
                    System.loadLibrary("face_classifier_native");
                    faceDetector = new FaceDetector(TrainerDatabaseActivity.this);
                    PictureHolder ph = PictureHolder.getInstance();

                    frameRgba = ph.getFrameRgba();
                    frameGray = ph.getFrameGray();

                    faces = faceDetector.detectFaces(frameGray);
                    createFacesForm();
                }
                break;
                default: {
                    super.onManagerConnected(status);
                }
                break;
            }
        }
    };

    public TrainerDatabaseActivity() {
        facesRgba = new ArrayList<>();
        facesGray = new ArrayList<>();
        editTexts = new ArrayList<>();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trainer_database);
    }

    @Override
    public void onResume() {
        super.onResume();
        OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_11, this, loaderCallback);
    }

    public void addToDatabaseClick(View view) {
        if (!formIsValid()) {
            Toast.makeText(this, "You have to fill in all fields.\nOnly letters are acceptable.", Toast.LENGTH_SHORT).show();
            return;
        }
    }

    private void createFacesForm() {
        TableLayout tl = (TableLayout) findViewById(R.id.facesTable);
        sequentialNumber = 1;
        for (Face face : faces) {
            Mat tempFaceRoi = face.extractRoi(frameGray);
            facesGray.add(tempFaceRoi);

            tempFaceRoi = face.extractRoi(frameRgba);
            facesRgba.add(tempFaceRoi);

            TableRow tr = new TableRow(TrainerDatabaseActivity.this);
            ImageView iv = new ImageView(TrainerDatabaseActivity.this);
            Bitmap faceBitmap = Bitmap.createBitmap(tempFaceRoi.cols(), tempFaceRoi.rows(), Bitmap.Config.ARGB_8888);
            Utils.matToBitmap(tempFaceRoi, faceBitmap);
            iv.setImageBitmap(faceBitmap);
            iv.setMinimumWidth(faceBitmap.getWidth());
            iv.setMinimumHeight(faceBitmap.getHeight());
            tr.addView(iv);
            TableRow.LayoutParams params = (TableRow.LayoutParams) iv.getLayoutParams();
            params.setMargins(8, 8, 8, 8);
            iv.setLayoutParams(params);

            EditText et = new EditText(this);
            et.setId(20 + sequentialNumber);
            et.setEms(10);
            editTexts.add(et);
            tr.addView(et);

            tl.addView(tr);

            ++sequentialNumber;
        }
    }

    private boolean formIsValid() {
        for (EditText et : editTexts) {
            if (et.getText().toString().equals("")) {
                return false;
            }
            if (!et.getText().toString().matches("[a-zA-Z]+")) {
                return false;
            }
        }
        return true;
    }
}