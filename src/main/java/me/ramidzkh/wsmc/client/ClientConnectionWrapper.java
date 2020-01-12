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

package me.ramidzkh.wsmc.client;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelException;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.EmptyHttpHeaders;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.websocketx.WebSocketClientHandshakerFactory;
import io.netty.handler.codec.http.websocketx.WebSocketVersion;
import io.netty.handler.codec.http.websocketx.extensions.compression.WebSocketClientCompressionHandler;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import io.netty.handler.timeout.ReadTimeoutHandler;
import me.ramidzkh.wsmc.netty.WebSocketClientHandshakeHandler;
import me.ramidzkh.wsmc.netty.WebSocketFrameCodec;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.*;

import java.io.UncheckedIOException;
import java.net.InetAddress;
import java.net.URI;
import java.net.UnknownHostException;

@Environment(EnvType.CLIENT)
public class ClientConnectionWrapper {

    public static ClientConnection connect(ServerAddress address, boolean shouldUseNativeTransport) {
        ServerAddressProperties properties = (ServerAddressProperties) address;
        URI uri = properties.getWebsocketUri();

        if (uri == null) {
            try {
                return ClientConnection.connect(InetAddress.getByName(address.getAddress()), address.getPort(), shouldUseNativeTransport);
            } catch (UnknownHostException exception) {
                throw new UncheckedIOException(exception);
            }
        }

        shouldUseNativeTransport &= Epoll.isAvailable();

        ClientConnection connection = new ClientConnection(NetworkSide.CLIENTBOUND);

        Class<? extends SocketChannel> type = shouldUseNativeTransport ? EpollSocketChannel.class : NioSocketChannel.class;
        EventLoopGroup group = shouldUseNativeTransport
                ? new EpollEventLoopGroup(0, new ThreadFactoryBuilder().setNameFormat("Netty Epoll Client IO #%d").setDaemon(true).build())
                : new NioEventLoopGroup(0, new ThreadFactoryBuilder().setNameFormat("Netty Client IO #%d").setDaemon(true).build());

        WebSocketClientHandshakeHandler handler = new WebSocketClientHandshakeHandler(WebSocketClientHandshakerFactory.newHandshaker(uri, WebSocketVersion.V13, null, true, EmptyHttpHeaders.INSTANCE));

        new Bootstrap()
                .channel(type)
                .group(group)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel channel) throws Exception {
                        try {
                            channel.config().setOption(ChannelOption.TCP_NODELAY, true);
                        } catch (ChannelException ignored) {
                        }

                        if (properties.useSsl()) {
                            channel.pipeline().addLast(SslContextBuilder.forClient().trustManager(properties.validateSsl() ? null : InsecureTrustManagerFactory.INSTANCE).build().newHandler(channel.alloc()));
                        }

                        channel.pipeline()
                                .addLast(new HttpClientCodec())
                                .addLast(new HttpObjectAggregator(8192))
                                .addLast(WebSocketClientCompressionHandler.INSTANCE)
                                .addLast(handler)
                                .addLast(new WebSocketFrameCodec())
                                .addLast("timeout", new ReadTimeoutHandler(30))
                                .addLast("splitter", new SplitterHandler())
                                .addLast("decoder", new DecoderHandler(NetworkSide.CLIENTBOUND))
                                .addLast("prepender", new SizePrepender())
                                .addLast("encoder", new PacketEncoder(NetworkSide.SERVERBOUND))
                                .addLast("packet_handler", connection);
                    }
                })
                .connect(uri.getHost(), uri.getPort())
                .syncUninterruptibly();
        handler.getPromise().syncUninterruptibly();

        return connection;
    }
}
