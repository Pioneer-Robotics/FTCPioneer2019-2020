package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.util.ElapsedTime;

@Autonomous(name = "Skystone Low Key Sabotage", group = "ftcPio")
public class SkystoneSelectionSabotage extends Auto {

    public double startRotation;

    ElapsedTime deltaTime = new ElapsedTime();

    double moveTime;

    @Override
    public void runOpMode() {
        StartRobot();

        startRotation = robot.GetRotation();

        waitForStart();
        robot.arm.SetArmStateWait(0.5, 0.5, 1);


        StopMovement();
        StopRobot();
    }
}
