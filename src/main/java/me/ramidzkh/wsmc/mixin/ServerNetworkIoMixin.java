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

import io.netty.channel.ChannelException;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelOption;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.timeout.ReadTimeoutHandler;
import me.ramidzkh.wsmc.netty.LambdaChannelInitializer;
import me.ramidzkh.wsmc.netty.WebSocketFrameCodec;
import me.ramidzkh.wsmc.netty.WebSocketServerHandshakeHandler;
import me.ramidzkh.wsmc.server.ExtraServerProperties;
import net.minecraft.network.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.ServerNetworkIo;
import net.minecraft.server.dedicated.MinecraftDedicatedServer;
import net.minecraft.server.network.ServerHandshakeNetworkHandler;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import java.net.URI;
import java.util.List;

@Mixin(ServerNetworkIo.class)
public class ServerNetworkIoMixin {

    @Shadow
    @Final
    private List<ClientConnection> connections;

    @Shadow
    @Final
    private MinecraftServer server;

    @ModifyArg(method = "bind", at = @At(value = "INVOKE", target = "Lio/netty/bootstrap/ServerBootstrap;childHandler(Lio/netty/channel/ChannelHandler;)Lio/netty/bootstrap/ServerBootstrap;"), index = 0)
    private ChannelHandler getHandler(ChannelHandler handler) {
        URI uri = ((ExtraServerProperties) ((MinecraftDedicatedServer) server).getProperties()).getWebsocketUri();

        if (uri != null) {
            return new LambdaChannelInitializer(channel -> {
                try {
                    channel.config().setOption(ChannelOption.TCP_NODELAY, true);
                } catch (ChannelException ignored) {
                }

                ClientConnection clientConnection = new ClientConnection(NetworkSide.SERVERBOUND);
                clientConnection.setPacketListener(new ServerHandshakeNetworkHandler(server, clientConnection));
                connections.add(clientConnection);

                channel.pipeline()
                        .addLast(new HttpServerCodec())
                        .addLast(new HttpObjectAggregator(65536))
                        .addLast(new WebSocketServerHandshakeHandler(uri))
                        .addLast(new WebSocketFrameCodec())
                        .addLast("timeout", new ReadTimeoutHandler(30))
                        .addLast("legacy_query", new LegacyQueryHandler((ServerNetworkIo) (Object) this))
                        .addLast("splitter", new SplitterHandler())
                        .addLast("decoder", new DecoderHandler(NetworkSide.SERVERBOUND))
                        .addLast("prepender", new SizePrepender())
                        .addLast("encoder", new PacketEncoder(NetworkSide.CLIENTBOUND))
                        .addLast("packet_handler", clientConnection);
            });
        } else {
            return handler;
        }
    }
}
