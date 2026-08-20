# swerve-2026

FTC code for our coaxial swerve drivetrain. Built on the Pedro Pathing quickstart.

Status: hardware is assembled, nothing is tuned yet. Constants are scaffolded but
most values are still zeros waiting on tuner output. See the checklist below.

## Drivetrain

Four coaxial swerve pods.

| | |
|---|---|
| Drive motor | goBILDA 5000-series bare, 12V, 5800 RPM no-load, 1.47 kg-cm |
| Drive ratio | 6.2:1 motor to wheel, no internal gearbox |
| Drive encoder | 28 CPR bare, 173.6 ticks per wheel rev |
| Wheel | ~67.5mm (measure with calipers, the printed parts vary) |
| Steering servo | SWYFT Speed SR-Servo-01 v1.1, continuous rotation |
| Steering ratio | 26:28 servo to pod pulley |
| Steering rate | 161.3 RPM at servo, ~149.8 RPM at pod = ~899 deg/s |
| Azimuth encoder | MelonBotics nano, magnetic, analog output |

Geometry (front of the robot is the short side of the pod / 6-hole channel):

- Wheelbase 262.96mm, track width 239.35mm
- Pod positions from center, Pedro coords (+x forward, +y left): `(±131.5, ±119.7)` mm
- CG centered in x and y, 68mm high

Theoretical top speed works out to about 10.8 ft/s. Actual number comes from
ForwardVelocityTuner.

## Electronics

Control Hub only so far.

- 4 drive motors on the motor ports
- 4 steering servos configured as **continuous rotation**
- 4 analog encoders, two per port through MelonBotics JST joiner boards. This uses
  every analog channel on the hub (0-1 and 2-3), so anything else analog needs an
  Expansion Hub.

Config names used throughout the code:

```
leftFrontMotor    leftFrontServo    leftFrontEncoder
rightFrontMotor   rightFrontServo   rightFrontEncoder
leftBackMotor     leftBackServo     leftBackEncoder
rightBackMotor    rightBackServo    rightBackEncoder
```

## Libraries

- FTC SDK 11.1.0
- Pedro Pathing 2.1.2 — path following. It has native coaxial swerve support, so we
  are not writing our own kinematics or module classes.
- SolversLib 0.3.4 (core + pedroPathing) — command scheduler for mechanisms. FTCLib
  core is commented out in `build.dependencies.gradle` on purpose; having both on the
  classpath causes duplicate class errors.
- Panels 1.0.12 — dashboard and telemetry

## Layout

```
TeamCode/src/main/java/org/firstinspires/ftc/teamcode/
├── opmodes/            teleop, autos, and diagnostics
├── pedroPathing/
│   ├── Constants.java  pod definitions and follower constants  <- most of the work
│   └── Tuning.java     Pedro's built-in tuners
├── subsystems/         mechanisms. NOT the drivetrain, the Follower is that
├── tuning/             our own tuners for mechanisms
└── util/               helpers
```

## Setup

```bash
git clone https://github.com/joselaurencio/swerve-2026.git
```

Open in Android Studio and let Gradle sync. `local.properties` is gitignored and gets
generated locally, so don't commit it.

## Tuning progress

Order matters. Do not skip ahead.

- [ ] Localizer installed and configured (required first — you cannot localize off
  drive encoders on swerve, the wheels aren't fixed to the chassis)
- [ ] Robot weighed, `.mass()` filled in
- [ ] `encoderNames` array updated in Tuning.java (~line 1440)
- [ ] Pod Config Test passes — correct wheel spins for each button, all encoders read
- [ ] Analog Min/Max Tuner
- [ ] Angle offsets via LocalizationTest + alignment tool (radians)
- [ ] Swerve Offsets Test — servo directions, then motor directions
- [ ] Swerve Turn Test — encoder inverted flags
- [ ] Pod PIDF — P and D off the ground, F on the tiles
- [ ] Re-enable X-lock, then retune zero power acceleration
- [ ] Localization tuning
- [ ] Forward velocity + zero power acceleration
- [ ] Translational and heading PIDs
- [ ] Field centric teleop

Measured values go here as we get them:

```
                 LF        RF        LB        RB
analog min       -         -         -         -
analog max       -         -         -         -
angle offset     -         -         -         -
motor dir        -         -         -         -
servo dir        -         -         -         -
encoder inv      -         -         -         -
```

## Swerve gotchas

Things that are different from mecanum and either cost us time or would have:

- **Predictive braking does not work on swerve.** Too aggressive, causes oscillation
  because pods can't change direction instantly. Use the PIDF drive algorithm.
- **No kF on the translational or heading PIDs.** Same oscillation problem.
- **Skip the lateral tuners.** On swerve they're equivalent to the forward ones, just
  reuse the values.
- **Retune zero power acceleration if X-lock changes.** X-lock brakes much harder and
  the stopping distance shifts a lot.
- **Low centripetal scaling.** 0.0005 to start. Swerve has better traction so it needs
  less compensation than mecanum.
- **Keep bevel gears facing the same direction on all four pods.** Flipping one 180
  degrees changes its offset and makes debugging miserable.
- If a pod misbehaves during the direction tests, check that its motor, servo, and
  encoder are actually all the same physical pod before re-tuning offsets. A wrong
  harness looks exactly like a bad offset.

## Notes

Worst case reaction time is a 90 degree pod rotation, about 100ms at our steering rate.
Mecanum is instant because the wheels never turn. X-lock helps here — parked at 45
degrees, launching in any direction costs 45 degrees instead of 0 or 90, so response is
slightly slower forward but much better sideways and symmetric overall.

## Reference

- Pedro Pathing docs: https://pedropathing.com/docs/pathing
- Swerve setup: https://pedropathing.com/docs/pathing/tuning/swerve/swerve-setup
- Swerve tuning: https://pedropathing.com/docs/pathing/tuning/swerve/swerve-tuning
- MelonBotics encoder docs: https://docs.melonbotics.com/encoder
- SolversLib: https://docs.seattlesolvers.com
