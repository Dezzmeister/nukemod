package com.dezzmeister.nukemod.command;

import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class RegisterCommandsEventListener {
	
	@SubscribeEvent
	public void registerCommands(RegisterCommandsEvent event) {
		NukeCommand.register(event.getDispatcher());
		GNukeCommand.register(event.getDispatcher());
	}
}
