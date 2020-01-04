package org.firstinspires.ftc.teamcode.Experiments.Functional;


import android.renderscript.Double2;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.util.ElapsedTime;

import org.firstinspires.ftc.teamcode.Helpers.bMath;
import org.firstinspires.ftc.teamcode.Robot.Robot;
import org.firstinspires.ftc.teamcode.Robot.RobotArm;
import org.firstinspires.ftc.teamcode.Robot.RobotWallTrack;

@TeleOp(name = "Teleop2", group = "Sensor")
public class TeleopTester2 extends LinearOpMode {

    RobotWallTrack.SensorGroup targetWallTrackGroup = null;

    Robot robot = new Robot();

    ElapsedTime deltaTime = new ElapsedTime();

    public boolean lockRotation = false;
    boolean aButton1Check = false;

    double targetRotation;

    double moveSpeed;
    double rotateSpeed;
    double raiseSpeed = 0;
    double targetRotationOffset;

    boolean grab = false;
    boolean bButton2Check = false;

    double extension = 0;
    double armAngle = 0;
    double gripAngle = 180;
    double aTad = 0;
    boolean xButton2Check = false;
    boolean idle = false;
    boolean aButton2Check = false;
    boolean pointDown = false;
    double vertExtensionConst = 0;
    double yWanted = 0;
    double xWanted = 0;
    double vertDMove = 0;
    boolean lastD2press = false;

    //this section relates to moving the arm (not rotating the gripper)
    boolean rectControls_wanted = false;
    boolean leftBumper2Check = false;
    double targetGripperPositionY = 0;
    double targetGripperPositionX = 0;

    boolean movementModeCheck;
    boolean movementModeNew;


    double rotationLockValue = 0;

    double lunchboxRot = 0.5;

    boolean servoLastToggle = false;
    boolean fineServoControl = true;


    @Override
    public void runOpMode() throws InterruptedException {
        robot.init(hardwareMap, this);
        fineServoControl = true;


        waitForStart();

//        robot.lunchbox.setPosition(1);
        lunchboxRot = 1;
        targetRotation = robot.GetRotation();
        gripAngle = 90;
        while (opModeIsActive()) {

            ///DRIVER CONTROLS

            //let right bumper put robot in really slow mode for fine control
            if (gamepad1.right_bumper) {
                moveSpeed = 0.15;
            } else {
                moveSpeed = bMath.Clamp(gamepad1.right_trigger + 0.35, 0, 1);
            }

            rotateSpeed = bMath.Clamp(gamepad1.left_trigger + 0.35, 0, 1);


            //targetRotation = bMath.Loop(targetRotation, 360);
            //shouldn't we use deltatime?

            if (gamepad1.x && !aButton1Check) {
                lockRotation = !lockRotation;
                rotationLockValue = robot.GetRotation();
                targetRotationOffset = 0;
            }
            aButton1Check = gamepad1.a;

            if (gamepad1.y != movementModeCheck) {
                if (gamepad1.y) {
                    movementModeNew = !movementModeNew;
                }
                movementModeCheck = gamepad1.y;
            }

            if (movementModeNew) {
                telemetry.addData("Using OLD method of drive", "");
                if (lockRotation) {
                    targetRotation = rotationLockValue;

                    //Ensures that other code cannot change targetRotation while rotationLock is on
                    robot.MoveComplex(new Double2(gamepad1.right_stick_x, gamepad1.right_stick_y), moveSpeed, robot.GetRotation() - (targetRotation));
                } else {
                    robot.MoveSimple(new Double2(gamepad1.right_stick_x, gamepad1.right_stick_y), moveSpeed, -gamepad1.left_stick_x * rotateSpeed);
                }

            } else {
                telemetry.addData("Using NEW method of drive", "");
                targetRotation = rotationLockValue;

                if (lockRotation) {
                    targetRotationOffset += -gamepad1.left_stick_x * rotateSpeed * 180;

                    //Ensures that other code cannot change targetRotation while rotationLock is on
                    robot.MoveComplex(new Double2(gamepad1.right_stick_x, gamepad1.right_stick_y), moveSpeed, robot.GetRotation() - (targetRotation + targetRotationOffset));
                } else {

                    robot.MoveComplex(new Double2(gamepad1.right_stick_x, gamepad1.right_stick_y), moveSpeed, robot.GetRotation() - (targetRotation));

//                robot.MoveComplex(bMath.toRadians() new Double2(gamepad1.right_stick_x, gamepad1.right_stick_y), moveSpeed, robot.GetRotation() - (targetRotation));

//                robot.MoveSimple(new Double2(gamepad1.right_stick_x, gamepad1.right_stick_y), moveSpeed, -gamepad1.left_stick_x * rotateSpeed);
                }

            }

            if (gamepad1.y != movementModeCheck) {
                if (gamepad1.y) {
                    movementModeNew = !movementModeNew;
                }
                movementModeCheck = gamepad1.y;
            }

//            if (gamepad1.x != servoLastToggle) {
//                if (gamepad1.x) {
//                    fineServoControl = !fineServoControl;
//                }
//                servoLastToggle = gamepad1.x;
//            }
//
//            if (fineServoControl) {

            //rotate lunchbox up with the up dpad
            lunchboxRot -= gamepad1.dpad_up ? deltaTime.seconds() * 1 : 0;
            //rotate lunchbox down with the down dpad
            lunchboxRot += gamepad1.dpad_down ? deltaTime.seconds() * 1 : 0;
            lunchboxRot = bMath.Clamp(lunchboxRot, 0, 1);
            telemetry.addData("LunchboxRot Position", lunchboxRot);
//            robot.lunchbox.setPosition(lunchboxRot);

//            } else {
//                robot.lunchbox.setPosition(0.4333);
//                telemetry.addData("LunchboxRot Position Yeeted", lunchboxRot);
//
//            }

            /*
            if (gamepad1.a) {
                lockRotation = !lockRotation;
                sleep(100);
            }

            if (lastLockRotation != lockRotation) {
                lastLockRotation = lockRotation;
                if (lockRotation) {
                    targetRotation = robot.GetRotation();
                }
            }
*/


            //ARM CONTROLS

            //use the left bumper to make the toggle the arm controls (rectangular or polar)
            if (gamepad2.left_bumper && !leftBumper2Check) {
                rectControls_wanted = !rectControls_wanted;
            }
            leftBumper2Check = gamepad2.left_bumper;

            if (rectControls_wanted) { //yes it looks dumb, no it's not a typo, I'm gonna fix it when it's ready
                if (Math.abs(gamepad2.left_stick_y) > 0.1) {
                    yWanted += deltaTime.seconds() * gamepad2.left_stick_y;
                }
                if (Math.abs(gamepad2.left_stick_x) > 0.1) {
                    xWanted += deltaTime.seconds() * gamepad2.left_stick_x;
                }
                double armLengthNeeded = Math.sqrt(xWanted * xWanted + yWanted * yWanted);
                double armAngleNeeded = Math.atan(yWanted / xWanted);
                robot.arm.SetArmState(armAngleNeeded, armLengthNeeded, 1);
            }

            if (!rectControls_wanted) { //yes it looks dumb, no it's not a typo, just leave it alone

                //extend or shorten arm with Dpad
                if ((gamepad2.dpad_up || gamepad2.dpad_down) && !lastD2press) {
                    vertExtensionConst = robot.arm.calcVertExtensionConst();
                }
                lastD2press = gamepad2.dpad_up || gamepad2.dpad_down;

                if (gamepad2.dpad_up) {
                    vertDMove = 0.25;
                    extension = (robot.arm.calcVertExtensionTicks(vertExtensionConst) + 10) / -2613;
                }
                if (gamepad2.dpad_down) {
                    vertDMove = -0.25;
                    extension = (robot.arm.calcVertExtensionTicks(vertExtensionConst) + 10) / -2613;
                }
                if (!gamepad2.dpad_up && !gamepad2.dpad_down) {
                    vertDMove = 0;
                }
            }

            //press the X button to put the grabber in "idle" position
            if (gamepad2.x && !xButton2Check) {
                idle = true;
            }
            xButton2Check = gamepad2.x;

            //press the B button to open or close grabber
            if (gamepad2.b && !bButton2Check) {
                if (idle) {
                    grab = false; //if it's in idle, pressing "B" should open it
                } else {
                    grab = !grab;
                }
                idle = false;
            }
            bButton2Check = gamepad2.b;


            if (idle) {
                robot.arm.SetGripState(RobotArm.GripState.IDLE, gripAngle / 180);
            } else if (grab) {
                robot.arm.SetGripState(RobotArm.GripState.CLOSED, gripAngle / 180);
            } else {
                robot.arm.SetGripState(RobotArm.GripState.OPEN, gripAngle / 180);
            }


            //press a button to make the gripper point down
            if (gamepad2.a && !aButton2Check) {
                pointDown = true;
            }
            aButton2Check = gamepad2.a;

            if (pointDown) {
                gripAngle = 90 - robot.arm.thetaAngle();
            }

            //rotate gripper down with the left dpad
            if (gamepad2.dpad_left) {
                gripAngle += deltaTime.seconds() * 135;
                pointDown = false;
            }

            //rotate gripper up with the right dpad
            if (gamepad2.dpad_right) {
                gripAngle -= deltaTime.seconds() * 135;
                pointDown = false;
            }


            //extend arm by tapping right trigger
            extension += gamepad2.right_trigger * deltaTime.seconds();
            //retract arm by tapping left trigger
            extension -= gamepad2.left_trigger * deltaTime.seconds();


            aTad = gamepad2.y ? 1 : 0;
            extension = bMath.Clamp(extension, 0, 1);
            armAngle = bMath.Clamp(armAngle, 0, 1);
            gripAngle = bMath.Clamp(gripAngle, 0, 180);
            raiseSpeed = bMath.Clamp((gamepad2.dpad_up || gamepad2.dpad_down) ? vertDMove : gamepad2.left_stick_y + aTad, -1, 1);
            robot.arm.SetArmState(extension, raiseSpeed, 1);


            telemetry.addData("Rotation Locked ", lockRotation);
            telemetry.addData("Current Arm Angle", robot.arm.thetaAngle());
            telemetry.addData("Current Rotation ", robot.GetRotation());
            telemetry.addData("Current Target Rotation", targetRotation);
            telemetry.addData("Current Lunchbox", lunchboxRot);
            telemetry.update();

            deltaTime.reset();
        }
        robot.Stop();
    }
}