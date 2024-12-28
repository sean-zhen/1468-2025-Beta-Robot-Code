// Copyright 2021-2024 FRC 6328
// http://github.com/Mechanical-Advantage
//
// This program is free software; you can redistribute it and/or
// modify it under the terms of the GNU General Public License
// version 3 as published by the Free Software Foundation or
// available in the root directory of this project.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
// GNU General Public License for more details.

package frc.robot;

import static frc.robot.subsystems.vision.VisionConstants.*;

import com.pathplanner.lib.auto.AutoBuilder;
import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.wpilibj.GenericHID;
import edu.wpi.first.wpilibj.Joystick;
import edu.wpi.first.wpilibj.XboxController;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.Commands;
import edu.wpi.first.wpilibj2.command.button.JoystickButton;
import edu.wpi.first.wpilibj2.command.sysid.SysIdRoutine;
import frc.robot.commands.*;
import frc.robot.generated.TunerConstants;
import frc.robot.subsystems.drive.Drive;
import frc.robot.subsystems.drive.GyroIO;
import frc.robot.subsystems.drive.GyroIOPigeon2;
import frc.robot.subsystems.drive.ModuleIO;
import frc.robot.subsystems.drive.ModuleIOSim;
import frc.robot.subsystems.drive.ModuleIOTalonFX;
import frc.robot.subsystems.vision.Vision;
import frc.robot.subsystems.vision.VisionIO;
import frc.robot.subsystems.vision.VisionIOPhotonVision;
import frc.robot.subsystems.vision.VisionIOPhotonVisionSim;
import org.littletonrobotics.junction.networktables.LoggedDashboardChooser;

/**
 * This class is where the bulk of the robot should be declared. Since Command-based is a
 * "declarative" paradigm, very little robot logic should actually be handled in the {@link Robot}
 * periodic methods (other than the scheduler calls). Instead, the structure of the robot (including
 * subsystems, commands, and button mappings) should be declared here.
 */
public class RobotContainer {
	// Subsystems
	private final Drive drive;
	private final Vision vision;

	// Controller
	final Joystick driverLeftJoystick = new Joystick(0);
	final Joystick driverRightJoystick = new Joystick(1);
  
  	final Joystick operatorJoystick = new Joystick(3);  
  	final Joystick testOprJoystick = new Joystick(2);  // this was for testing purposes only - Tom 2024

  	// Dashboard inputs
  	private final LoggedDashboardChooser<Command> autoChooser;

  	/** The container for the robot. Contains subsystems, OI devices, and commands. */
  	public RobotContainer() {
		switch (Constants.currentMode) {
			case REAL:
				// Real robot, instantiate hardware IO implementations
				drive = new Drive(
					new GyroIOPigeon2(),
					new ModuleIOTalonFX(TunerConstants.FrontLeft),
					new ModuleIOTalonFX(TunerConstants.FrontRight),
					new ModuleIOTalonFX(TunerConstants.BackLeft),
					new ModuleIOTalonFX(TunerConstants.BackRight));
				vision = new Vision(
                	drive::addVisionMeasurement,
					new VisionIOPhotonVision(camera0Name, robotToCamera0),
					new VisionIOPhotonVision(camera1Name, robotToCamera1));
				
				break;

			case SIM:
				// Sim robot, instantiate physics sim IO implementations
				drive = new Drive(
					new GyroIO() {},
					new ModuleIOSim(TunerConstants.FrontLeft),
					new ModuleIOSim(TunerConstants.FrontRight),
					new ModuleIOSim(TunerConstants.BackLeft),
					new ModuleIOSim(TunerConstants.BackRight));
				vision = new Vision(
                	drive::addVisionMeasurement,
                	new VisionIOPhotonVisionSim(camera0Name, robotToCamera0, drive::getPose),
                	new VisionIOPhotonVisionSim(camera1Name, robotToCamera1, drive::getPose));
				
				break;

			default:
				// Replayed robot, disable IO implementations
				drive = new Drive(
					new GyroIO() {},
					new ModuleIO() {},
					new ModuleIO() {},
					new ModuleIO() {},
					new ModuleIO() {});
				vision = new Vision(
					drive::addVisionMeasurement, 
					new VisionIO() {}, 
					new VisionIO() {});
				
				break;
    	}

		// Set up auto routines
		autoChooser = new LoggedDashboardChooser<>("Auto Choices", AutoBuilder.buildAutoChooser());

		// Set up SysId routines
		autoChooser.addOption("Drive Wheel Radius Characterization", DriveCommands.wheelRadiusCharacterization(drive));
		autoChooser.addOption("Drive Simple FF Characterization", DriveCommands.feedforwardCharacterization(drive));
		autoChooser.addOption("Drive SysId (Quasistatic Forward)", drive.sysIdQuasistatic(SysIdRoutine.Direction.kForward));
		autoChooser.addOption("Drive SysId (Quasistatic Reverse)", drive.sysIdQuasistatic(SysIdRoutine.Direction.kReverse));
		autoChooser.addOption("Drive SysId (Dynamic Forward)", drive.sysIdDynamic(SysIdRoutine.Direction.kForward));
		autoChooser.addOption("Drive SysId (Dynamic Reverse)", drive.sysIdDynamic(SysIdRoutine.Direction.kReverse));

		// Configure the button bindings
		configureButtonBindings();
  	}

	/**
	 * Use this method to define your button->command mappings. Buttons can be created by
	 * instantiating a {@link GenericHID} or one of its subclasses ({@link
	 * edu.wpi.first.wpilibj.Joystick} or {@link XboxController}), and then passing it to a {@link
	 * edu.wpi.first.wpilibj2.command.button.JoystickButton}.
	 */
  	private void configureButtonBindings() {
    	// Driver Buttons
        final JoystickButton resetGyro = new JoystickButton(driverRightJoystick, 1);    //TODO: Update button number, confirm with Daniel which button -Sean
        final JoystickButton xPattern = new JoystickButton(driverRightJoystick, 2);     //TODO: Remove this if not necessary for 2025 game -Sean
        final JoystickButton lockToZero = new JoystickButton(driverRightJoystick, 3);   //TODO: Remove this if not necessary -Sean
		final JoystickButton autoAim = new JoystickButton(driverRightJoystick,4);

        // Default command, normal field-relative drive
        drive.setDefaultCommand(
        	DriveCommands.joystickDrive(
            	drive,
                () -> -driverLeftJoystick.getY(),
                () -> -driverLeftJoystick.getX(),
                () -> -driverRightJoystick.getX()));
   
        // Reset gyro to 0°
        resetGyro.onTrue(
            Commands.runOnce(
                () -> drive.setPose(new Pose2d(drive.getPose().getTranslation(), new Rotation2d())), 
                drive)
                	.ignoringDisable(true));
   
    	// Switch to X pattern
        xPattern.onTrue(Commands.runOnce(drive::stopWithX, drive));		//TODO: Remove this if not necessary for 2025 game -Sean 
                   
        // Lock to 0° when held		//TODO: Remove this if not necessary -Sean
        lockToZero.whileTrue(
            DriveCommands.joystickDriveAtAngle(
            	drive,
                () -> -driverLeftJoystick.getY(),
                () -> -driverLeftJoystick.getX(),
                () -> new Rotation2d()));

		// // Auto aim command example
    	// @SuppressWarnings("resource")
    	// PIDController aimController = new PIDController(0.2, 0.0, 0.0);
    	// aimController.enableContinuousInput(-Math.PI, Math.PI);
		// autoAim.whileTrue(
        //     Commands.startRun(
        //         () -> {
        //           aimController.reset();
        //         },
        //         () -> {
        //           drive.run(0.0, aimController.calculate(vision.getTargetX(0).getRadians()));
        //         },
        //         drive));
  	}
   
	/**
	 * Use this to pass the autonomous command to the main {@link Robot} class.
	 *
	 * @return the command to run in autonomous
	 */
	public Command getAutonomousCommand() {
		return autoChooser.get();
	}
}
