package org.firstinspires.ftc.teamcode.opmodes;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.AnalogInput;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;

/**
 * PURE HARDWARE VERIFICATION -- no Pedro, no kinematics, no servo control.
 *
 * Hold a face button to spin that pod's DRIVE motor:
 *   A = left front      B = right front
 *   X = left back       Y = right back
 *
 * Dpad up/down changes the power level. Dpad left toggles direction.
 *
 * PUT THE ROBOT ON BLOCKS. The steering servos are not commanded here, so the
 * pods are free to castor -- a wheel on the ground will drag the pod around.
 *
 * Missing or misnamed devices do NOT crash init. They are listed in telemetry
 * so you can see exactly which name is wrong.
 */
@TeleOp(name = "Pod Config Test", group = "Diagnostics")
public class PodConfigTest extends OpMode {

    // Must match the Driver Station config exactly, and match Constants.java.
    private static final String[] MOTOR_NAMES = {
            "leftFrontMotor", "rightFrontMotor", "leftBackMotor", "rightBackMotor"
    };
    private static final String[] ENCODER_NAMES = {
            "leftFrontEncoder", "rightFrontEncoder", "leftBackEncoder", "rightBackEncoder"
    };
    private static final String[] LABELS = {"LF (A)", "RF (B)", "LB (X)", "RB (Y)"};

    private final DcMotorEx[] motors = new DcMotorEx[4];
    private final AnalogInput[] encoders = new AnalogInput[4];
    private final StringBuilder initErrors = new StringBuilder();

    private double power = 0.30;
    private boolean reversed = false;

    // Edge detection so a single press does not repeat every loop.
    private boolean lastUp, lastDown, lastLeft;

    @Override
    public void init() {
        for (int i = 0; i < 4; i++) {
            try {
                motors[i] = hardwareMap.get(DcMotorEx.class, MOTOR_NAMES[i]);
                motors[i].setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
                // Read raw ticks. We are not setting a run mode that uses PID here,
                // because at this stage we only care that the encoder counts at all.
                motors[i].setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
                motors[i].setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
            } catch (Exception e) {
                initErrors.append("MISSING MOTOR: ").append(MOTOR_NAMES[i]).append("\n");
            }

            try {
                encoders[i] = hardwareMap.get(AnalogInput.class, ENCODER_NAMES[i]);
            } catch (Exception e) {
                initErrors.append("MISSING ANALOG: ").append(ENCODER_NAMES[i]).append("\n");
            }
        }

        if (initErrors.length() == 0) {
            telemetry.addLine("All 8 devices found. Ready.");
        } else {
            telemetry.addLine(">>> CONFIG PROBLEMS <<<");
            telemetry.addLine(initErrors.toString());
            telemetry.addLine("Fix these names on the Driver Station, then re-init.");
        }
        telemetry.addLine();
        telemetry.addLine("ROBOT ON BLOCKS. Hold A/B/X/Y to spin a pod.");
        telemetry.update();
    }

    @Override
    public void loop() {
        // ---- power / direction adjustment ----
        if (gamepad1.dpad_up && !lastUp) power = Math.min(1.0, power + 0.05);
        if (gamepad1.dpad_down && !lastDown) power = Math.max(0.0, power - 0.05);
        if (gamepad1.dpad_left && !lastLeft) reversed = !reversed;
        lastUp = gamepad1.dpad_up;
        lastDown = gamepad1.dpad_down;
        lastLeft = gamepad1.dpad_left;

        boolean[] pressed = {gamepad1.a, gamepad1.b, gamepad1.x, gamepad1.y};
        double applied = reversed ? -power : power;

        // ---- drive exactly the pods whose buttons are held ----
        for (int i = 0; i < 4; i++) {
            if (motors[i] != null) {
                motors[i].setPower(pressed[i] ? applied : 0.0);
            }
        }

        // ---- telemetry ----
        if (initErrors.length() > 0) {
            telemetry.addLine(">>> CONFIG PROBLEMS <<<");
            telemetry.addLine(initErrors.toString());
        }

        telemetry.addData("Power", "%.2f  %s", power, reversed ? "(REVERSED)" : "(forward)");
        telemetry.addLine("dpad up/down = power, dpad left = flip direction");
        telemetry.addLine();

        for (int i = 0; i < 4; i++) {
            String motorState;
            if (motors[i] == null) {
                motorState = "-- NOT FOUND --";
            } else {
                motorState = String.format("pwr %.2f | ticks %6d | vel %7.1f",
                        motors[i].getPower(),
                        motors[i].getCurrentPosition(),
                        motors[i].getVelocity());
            }

            String encState;
            if (encoders[i] == null) {
                encState = "-- NOT FOUND --";
            } else {
                double v = encoders[i].getVoltage();
                // 3.2 rather than 3.3 -- MelonBotics' own conversion uses 3.2, and
                // the real max is whatever the Analog Min/Max Tuner reports later.
                encState = String.format("%.3f V (~%.1f deg)", v, (v / 3.2) * 360.0);
            }

            telemetry.addLine(LABELS[i]);
            telemetry.addData("  motor", motorState);
            telemetry.addData("  encoder", encState);
        }

        telemetry.update();
    }

    @Override
    public void stop() {
        for (DcMotorEx m : motors) {
            if (m != null) m.setPower(0);
        }
    }
}