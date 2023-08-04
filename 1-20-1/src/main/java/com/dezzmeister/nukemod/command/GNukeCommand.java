package com.dezzmeister.nukemod.command;

import com.dezzmeister.nukemod.explosion.BigExplosion;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.context.CommandContext;

import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.coordinates.Vec3Argument;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class GNukeCommand {
	
	public static void register(final CommandDispatcher<CommandSourceStack> dispatcher) {		
		dispatcher.register(
			Commands.literal("gnuke")
			.requires((src) -> {
				return src.hasPermission(1);
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
	
	private static final int nuke(final CommandContext<CommandSourceStack> context, final float blastPower, final boolean causesFire) {
		final CommandSourceStack source = context.getSource();
		
		try {
			final Level world = source.getLevel();
			
			final Vec3 pos = Vec3Argument.getVec3(context, "pos");
			final float radius = FloatArgumentType.getFloat(context, "radius");
			
			final BigExplosion explosion = new BigExplosion(world, null, null, null, pos.x, pos.y, pos.z, radius, causesFire, Explosion.BlockInteraction.DESTROY, blastPower, true);
			if (!net.minecraftforge.event.ForgeEventFactory.onExplosionStart(world, explosion)) {
				explosion.explode();
				explosion.finalizeExplosion(true);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return 1;
	}
}
