package com.dezzmeister.nukemod.command;

import com.dezzmeister.nukemod.explosion.BigExplosion;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.arguments.Vec3Argument;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.Explosion;
import net.minecraft.world.World;

public class GNukeCommand {
	
	public static void register(final CommandDispatcher<CommandSource> dispatcher) {		
		dispatcher.register(
			Commands.literal("gnuke")
			.requires((src) -> {
				return src.hasPermissionLevel(1);
			})
			.then(
				Commands.argument("pos", Vec3Argument.vec3())
				.then(
					Commands.argument("radius", FloatArgumentType.floatArg(0.0f))
					.then(
						Commands.argument("killingPower", FloatArgumentType.floatArg(0.0f))
						.then(
							Commands.literal("fire")
							.executes(c -> {
								return nuke(c, FloatArgumentType.getFloat(c, "killingPower"), true);
							})
						)
						.executes(c -> {
							return nuke(c, FloatArgumentType.getFloat(c, "killingPower"), false);
						})
					)
					.then(
						Commands.literal("fire")
						.executes(c -> {
							return nuke(c, 40, true);
						})
					)
					.executes(c -> {
						return nuke(c, 40, false);
					})
				)
			)
		);
	}
	
	private static final int nuke(final CommandContext<CommandSource> context, final float blastPower, final boolean causesFire) {
		final CommandSource source = context.getSource();
		
		try {
			final World world = source.getWorld();
			
			final Vector3d pos = Vec3Argument.getVec3(context, "pos");
			final float radius = FloatArgumentType.getFloat(context, "radius");
			
			final BigExplosion explosion = new BigExplosion(world, null, null, null, pos.x, pos.y, pos.z, radius, blastPower, causesFire, true, Explosion.Mode.DESTROY);
			if (!net.minecraftforge.event.ForgeEventFactory.onExplosionStart(world, explosion)) {
				explosion.doExplosionA();
				explosion.doExplosionB(true);
			}
		} catch (CommandSyntaxException e) {
			e.printStackTrace();
		}
		
		return 1;
	}
}
