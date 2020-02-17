package org.firstinspires.ftc.teamcode.Autonomous;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.renderscript.Int2;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.util.ElapsedTime;

import org.firstinspires.ftc.robotcore.external.ClassFactory;
import org.firstinspires.ftc.robotcore.external.hardware.camera.WebcamName;
import org.firstinspires.ftc.robotcore.external.matrices.OpenGLMatrix;
import org.firstinspires.ftc.robotcore.external.matrices.VectorF;
import org.firstinspires.ftc.robotcore.external.navigation.Orientation;
import org.firstinspires.ftc.robotcore.external.navigation.VuforiaLocalizer;
import org.firstinspires.ftc.robotcore.external.navigation.VuforiaTrackable;
import org.firstinspires.ftc.robotcore.external.navigation.VuforiaTrackableDefaultListener;
import org.firstinspires.ftc.robotcore.external.navigation.VuforiaTrackables;
import org.firstinspires.ftc.teamcode.Helpers.Vector2;
import org.firstinspires.ftc.teamcode.Helpers.bTelemetry;
import org.firstinspires.ftc.teamcode.Robot.RobotConfiguration;

import java.util.ArrayList;
import java.util.List;

import static org.firstinspires.ftc.robotcore.external.navigation.AngleUnit.DEGREES;
import static org.firstinspires.ftc.robotcore.external.navigation.AxesOrder.XYZ;
import static org.firstinspires.ftc.robotcore.external.navigation.AxesOrder.YZX;
import static org.firstinspires.ftc.robotcore.external.navigation.AxesReference.EXTRINSIC;

public class VuforiaBitmapSkystoneDetector {

    private static final String VUFORIA_KEY = "AQMfl/L/////AAABmTblKFiFfUXdnoB7Ocz4UQNgHjSNJaBwlaDm9EpX0UI5ISx2EH+5IoEmxxd/FG8c31He17kM5vtS0jyAoD2ev5mXBiITmx4N8AduU/iAw/XMC5MiEB1YBgw5oSO1qd4jvCOgbzy/HcOpN3KoVVnYqKhTLc8n6/IIFGy+qyF7b8WkzscJpybOSAT5wtaZumdBu0K3lHV6n+fqGJDMvkQ5xrCS6HiBtpZScAoekd7iP3IxUik2rMFq5hqMsOYW+qlxKp0cj+x4K9CIOYEP4xZsCBt66UxtDSiNqaiC1DyONtFz4oHJf/4J5aYRjMNwC2BpsVJ/R91WIcC0H0dpP9gtL/09J0bIMjm3plo+ac+OM0H3";

    private VuforiaLocalizer vuforia = null;

    WebcamName camera = null;

    //Define the skystone state
    public SkystoneState lastState = SkystoneState.CENTER;

    //Where the stones are relitive to the front of the bot
    public enum SkystoneState {
        PORT,
        CENTER,
        STARBOARD
    }

    public void Start(OpMode op) {
        //Find the camera
        camera = op.hardwareMap.get(WebcamName.class, RobotConfiguration.camera);
        op.telemetry.addData("Camera Found.", "");
        /*
         * Configure Vuforia by creating a Parameter object, and passing it to the Vuforia engine.
         * We can pass Vuforia the handle to a camera preview resource (on the RC phone);
         * If no camera monitor is desired, use the parameter-less constructor instead (commented out below).
         */
        int cameraMonitorViewId = op.hardwareMap.appContext.getResources().getIdentifier("cameraMonitorViewId", "id", op.hardwareMap.appContext.getPackageName());
        VuforiaLocalizer.Parameters parameters = new VuforiaLocalizer.Parameters(cameraMonitorViewId);
        parameters.vuforiaLicenseKey = VUFORIA_KEY;
        op.telemetry.addData("Camera ID Found.", "");

        /**
         * We also indicate which camera on the RC we wish to use.
         */
        parameters.cameraName = camera;
        op.telemetry.addData("Camera assigned.", "");


        //  Instantiate the Vuforia engine
        vuforia = ClassFactory.getInstance().createVuforia(parameters);

        op.telemetry.addData("Vuforia instansiated.", "");

        vuforia.setFrameQueueCapacity(1);
        vuforia.enableConvertFrameToBitmap();
    }

    VuforiaLocalizer.CloseableFrame frame;

    Bitmap image;

    ElapsedTime elapsedTime = new ElapsedTime();


    public void Update(OpMode op) {
        try {
            elapsedTime.reset();
            //Fetch the latest frame
            frame = vuforia.getFrameQueue().take();
            op.telemetry.addData("a ", elapsedTime.milliseconds());

            elapsedTime.reset();
            //Convert it to a bitmap
            image = vuforia.convertFrameToBitmap(frame);
            op.telemetry.addData("b ", elapsedTime.milliseconds());

            elapsedTime.reset();

//            op.telemetry.addData("yeeet", "");
            op.telemetry.addData("dead center", getBrightnessFromBitmapVerticalLine(op, image, 0.5, 0.1));
            op.telemetry.addData("blob center", getBrightnessFromBitmapVerticalLine(op, image, 0.5, 0.5));
            op.telemetry.addData("a a", getBrightnessFromBitmapVerticalLine(op, image, 0.5, 1));

            op.telemetry.addData("deltaTime ", elapsedTime.milliseconds());
            op.telemetry.addData("image x", image.getWidth());
            op.telemetry.addData("image y", image.getHeight());

            frame.close();
        } catch (InterruptedException e) {
            op.telemetry.addData("yoiiiiink", "");
            op.telemetry.update();
        }
    }

    long skyStoneColorPort = 0;
    long skyStoneColorCenter = 0;
    long skyStoneColorStarboard = 0;

    private SkystoneState getSkystoneState(Bitmap image) {
        skyStoneColorPort = getBrightnessFromBitmapVerticalLine(image, 0.3);
        skyStoneColorCenter = getBrightnessFromBitmapVerticalLine(image, 0.5);
        skyStoneColorStarboard = getBrightnessFromBitmapVerticalLine(image, 0.8);


    }

    Int2 imageScale = new Int2(0, 0);


    private long getBrightnessFromBitmapVerticalLine(Bitmap bitmap, double x_position) {
        imageScale.x = bitmap.getWidth();
        imageScale.y = bitmap.getHeight();

        int x = (int) (imageScale.x * x_position);

        int color = 0;
        long totalAlpha = 0L;

        for (int y = 0; y < imageScale.y; y++) {
            color = bitmap.getPixel(x, y);
            totalAlpha += Color.red(color) + Color.blue(color) + Color.green(color);
        }

        return totalAlpha;
    }


    //Returns an average color from a portion of a bitmap
    //Very very slow!
    private long getAlphaFromBitmap(Bitmap bitmap, int xPixelOffset, double xRange) {

        imageScale.x = bitmap.getWidth();
        imageScale.y = bitmap.getHeight();

        int xPixelCount = (int) (xRange * imageScale.x);

        int color = 0;

        long totalAlpha = 0L;


        for (int x = xPixelOffset; x < xPixelCount; x++) {

            for (int y = 0; y < imageScale.y; y++) {
                color = bitmap.getPixel(x, y);

                totalAlpha += Color.red(color) + Color.blue(color) + Color.green(color);

            }
        }

//        redTotal /= xPixelCount * imageScale.y;
//        greenTotal /= xPixelCount * imageScale.y;
//        blueTotal /= xPixelCount * imageScale.y;


//        bitmap.getPixel()
//        return toColor(redTotal, greenTotal, blueTotal);
        return totalAlpha;
    }

    public int toColor(int r, int g, int b) {
        int rgb = r;
        rgb = (rgb << 8) + g;
        rgb = (rgb << 8) + b;
        return rgb;
    }

}
