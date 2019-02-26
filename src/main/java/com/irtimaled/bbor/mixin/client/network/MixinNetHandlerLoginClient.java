package com.irtimaled.bbor.mixin.client.network;

import com.irtimaled.bbor.client.events.ConnectedToRemoteServer;
import com.irtimaled.bbor.common.EventBus;
import net.minecraft.client.network.NetHandlerLoginClient;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.login.server.SPacketLoginSuccess;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(NetHandlerLoginClient.class)
public abstract class MixinNetHandlerLoginClient {
    @Shadow
    @Final
    private NetworkManager networkManager;

    @Inject(method = "handleLoginSuccess", at = @At(value = "RETURN"))
    private void handleLoginSuccess(SPacketLoginSuccess packet, CallbackInfo ci) {
        EventBus.publish(new ConnectedToRemoteServer(this.networkManager));
    }
}