/*
 * Copyright 2020 ramidzkh
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package me.ramidzkh.wsmc.mixin;

import me.ramidzkh.wsmc.client.ClientConnectionWrapper;
import me.ramidzkh.wsmc.client.ConnectScreenProperties;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ConnectScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.ServerAddress;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.net.InetAddress;
import java.net.UnknownHostException;

@Mixin(ConnectScreen.class)
public abstract class ConnectScreenMixin implements ConnectScreenProperties {

    private ServerAddress address;

    @Override
    public ServerAddress getServerAddress() {
        return address;
    }

    @Shadow
    protected abstract void connect(String address, int port);

    @Redirect(method = "<init>(Lnet/minecraft/client/gui/screen/Screen;Lnet/minecraft/client/MinecraftClient;Lnet/minecraft/client/network/ServerInfo;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/ConnectScreen;connect(Ljava/lang/String;I)V"))
    private void connect(ConnectScreen self, String address, int port, Screen parent, MinecraftClient client, ServerInfo info) {
        this.address = ServerAddress.parse(info.address);
        connect(address, this.address.getPort());
    }

    @Mixin(targets = "net/minecraft/client/gui/screen/ConnectScreen$1")
    static class ConnectorThreadMixin {
        @Redirect(method = "run", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/ClientConnection;connect(Ljava/net/InetAddress;IZ)Lnet/minecraft/network/ClientConnection;"))
        private ClientConnection connect(InetAddress address, int port, boolean shouldUseNativeTransport) {
            return ClientConnectionWrapper.connect(((ConnectScreenProperties) MinecraftClient.getInstance().currentScreen).getServerAddress(), shouldUseNativeTransport);
        }

        @Redirect(method = "run", at = @At(value = "INVOKE", target = "Ljava/net/InetAddress;getByName(Ljava/lang/String;)Ljava/net/InetAddress;"))
        private InetAddress getByName(String name) {
            try {
                return InetAddress.getByName(name);
            } catch (UnknownHostException exception) {
                return null;
            }
        }
    }
}
