package org.firstinspires.ftc.teamcode.pedroPathing;

import com.pedropathing.control.PIDFCoefficients;
import com.pedropathing.follower.Follower;
import com.pedropathing.follower.FollowerConstants;
import com.pedropathing.ftc.FollowerBuilder;
import com.pedropathing.ftc.drivetrains.CoaxialPod;
import com.pedropathing.ftc.drivetrains.SwerveConstants;
import com.pedropathing.geometry.Pose;
import com.pedropathing.paths.PathConstraints;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.HardwareMap;

public class Constants {

    // Measurements from CAD
    // Pedro uses +X forward and +Y left
    private static final double HALF_LENGTH = 131.5;
    private static final double HALF_WIDTH = 119.7;

    // Starting values for steering PID
    private static final PIDFCoefficients POD_PID =
            new PIDFCoefficients(0.3, 0, 0.005, 0);

    public static FollowerConstants followerConstants = new FollowerConstants()
            // Need to update these after tuning
            .mass(8.36)

            .forwardZeroPowerAcceleration(0.0)
            .lateralZeroPowerAcceleration(0.0)

            .translationalPIDFCoefficients(
                    new PIDFCoefficients(0.0, 0, 0.0, 0))
            .headingPIDFCoefficients(
                    new PIDFCoefficients(0.0, 0, 0.0, 0))

            .centripetalScaling(0.0005);

    public static PathConstraints pathConstraints = new PathConstraints(
            0.995,
            100,
            1,
            1
    );

    public static SwerveConstants swerveConstants = new SwerveConstants()
            .maxPower(1)
            // Keep this while tuning the pods
            .zeroPowerBehavior(
                    SwerveConstants.ZeroPowerBehavior.IGNORE_ANGLE_CHANGES
            );

    private static CoaxialPod leftFront(HardwareMap hardwareMap) {
        return new CoaxialPod(
                hardwareMap,
                "leftFrontMotor",
                "leftFrontServo",
                "leftFrontEncoder",
                POD_PID,
                DcMotorSimple.Direction.FORWARD,
                DcMotorSimple.Direction.FORWARD,
                0.0, // offset
                new Pose(HALF_LENGTH, HALF_WIDTH),
                0.0,
                3.3,
                false
        );
    }

    private static CoaxialPod rightFront(HardwareMap hardwareMap) {
        return new CoaxialPod(
                hardwareMap,
                "rightFrontMotor",
                "rightFrontServo",
                "rightFrontEncoder",
                POD_PID,
                DcMotorSimple.Direction.FORWARD,
                DcMotorSimple.Direction.FORWARD,
                0.0,
                new Pose(HALF_LENGTH, -HALF_WIDTH),
                0.0,
                3.3,
                false
        );
    }

    private static CoaxialPod leftBack(HardwareMap hardwareMap) {
        return new CoaxialPod(
                hardwareMap,
                "leftBackMotor",
                "leftBackServo",
                "leftBackEncoder",
                POD_PID,
                DcMotorSimple.Direction.FORWARD,
                DcMotorSimple.Direction.FORWARD,
                0.0,
                new Pose(-HALF_LENGTH, HALF_WIDTH),
                0.0,
                3.3,
                false
        );
    }

    private static CoaxialPod rightBack(HardwareMap hardwareMap) {
        return new CoaxialPod(
                hardwareMap,
                "rightBackMotor",
                "rightBackServo",
                "rightBackEncoder",
                POD_PID,
                DcMotorSimple.Direction.FORWARD,
                DcMotorSimple.Direction.FORWARD,
                0.0,
                new Pose(-HALF_LENGTH, -HALF_WIDTH),
                0.0,
                3.3,
                false
        );
    }

    public static Follower createFollower(HardwareMap hardwareMap) {
        return new FollowerBuilder(followerConstants, hardwareMap)
                .pathConstraints(pathConstraints)

                // Add Pinpoint localizer here once configured
                // .pinpointLocalizer(localizerConstants)

                .swerveDrivetrain(
                        swerveConstants,
                        leftFront(hardwareMap),
                        rightFront(hardwareMap),
                        leftBack(hardwareMap),
                        rightBack(hardwareMap)
                )
                .build();
    }
}