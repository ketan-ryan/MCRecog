package com.mco.mcrecog.capabilities.timers;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class GraphicsTimersProvider implements ICapabilityProvider, INBTSerializable<CompoundTag> {
	public static Capability<GraphicsTimers> GRAPHICS_TIMERS = CapabilityManager.get(new CapabilityToken<>() {});

	private GraphicsTimers graphicsTimers = null;
	private final LazyOptional<GraphicsTimers> optional = LazyOptional.of(this::createGraphicsTimers);

	private GraphicsTimers createGraphicsTimers() {
		if(this.graphicsTimers == null) {
			this.graphicsTimers = new GraphicsTimers();
		}
		return this.graphicsTimers;
	}

	@Override
	public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
		if(cap == GRAPHICS_TIMERS) {
			return optional.cast();
		}
		return LazyOptional.empty();
	}

	@Override
	public CompoundTag serializeNBT() {
		CompoundTag nbt = new CompoundTag();
		createGraphicsTimers().saveNBTData(nbt);
		return nbt;
	}

	@Override
	public void deserializeNBT(CompoundTag nbt) {
		createGraphicsTimers().loadNBTData(nbt);
	}
}
