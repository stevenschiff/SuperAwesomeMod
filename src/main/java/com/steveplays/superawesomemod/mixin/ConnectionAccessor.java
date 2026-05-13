package com.steveplays.superawesomemod.mixin;

import io.netty.channel.Channel;
import net.minecraft.network.Connection;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Connection.class)
public interface ConnectionAccessor {

    @Accessor("channel")
    Channel getChannel();

    @Mutable
    @Accessor("channel")
    void setChannel(Channel channel);
}
