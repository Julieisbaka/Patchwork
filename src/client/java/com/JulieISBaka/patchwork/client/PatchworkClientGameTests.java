package com.JulieISBaka.patchwork.client;

import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;

/** Entry point that delegates visual regression checks to focused client GameTest suites. */
public final class PatchworkClientGameTests implements FabricClientGameTest {
	/** Runs each client test group, or only config-screen coverage when requested. */
	@Override
	public void runTest(ClientGameTestContext context) {
		PatchworkClientConfigGameTests.run(context);
		if (Boolean.getBoolean("patchwork.configScreenTestOnly")) {
			return;
		}
		PatchworkClientPetGameTests.run(context);
		PatchworkClientPotionGameTests.run(context);
		PatchworkClientLightGameTests.run(context);
		PatchworkClientArtworkGameTests.run(context);
	}
}
