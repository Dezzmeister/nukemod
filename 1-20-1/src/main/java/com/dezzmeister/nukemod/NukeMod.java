package com.dezzmeister.nukemod;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.dezzmeister.nukemod.command.RegisterCommandsEventListener;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(NukeMod.MODID)
public class NukeMod {
	// Directly reference a log4j logger.
	public static final Logger LOGGER = LogManager.getLogger();
	public static final String MODID = "nukemod";

	public NukeMod() {
		FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);

		// Register ourselves for server and other game events we are interested in
		MinecraftForge.EVENT_BUS.register(this);
		MinecraftForge.EVENT_BUS.register(new RegisterCommandsEventListener());
	}

	private void setup(final FMLCommonSetupEvent event) {
		
	}

	// You can use SubscribeEvent and let the Event Bus discover methods to call
	@SubscribeEvent
	public void onServerStarting(ServerStartingEvent event) {
		// do something when the server starts
		LOGGER.info("Nuke mod is starting");
	}
}
