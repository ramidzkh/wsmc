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

package me.ramidzkh.wsmc.netty;

import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.websocketx.CloseWebSocketFrame;
import io.netty.handler.codec.http.websocketx.PingWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshaker;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshakerFactory;

import java.net.URI;

public class WebSocketServerHandshakeHandler extends SimpleChannelInboundHandler<Object> {

    private final URI uri;
    private WebSocketServerHandshaker handshaker;

    public WebSocketServerHandshakeHandler(URI uri) {
        this.uri = uri;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext context, Object msg) {
        if (msg instanceof FullHttpRequest) {
            handle(context, (FullHttpRequest) msg);
        } else if (msg instanceof CloseWebSocketFrame) {
            handshaker.close(context.channel(), ((CloseWebSocketFrame) msg).retain());
        } else if (msg instanceof PingWebSocketFrame) {
            context.write(((PingWebSocketFrame) msg).retain());
        } else {
            context.fireChannelRead(msg);
        }
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext context) {
        context.flush();
    }

    private void handle(ChannelHandlerContext context, FullHttpRequest request) {
        if (!request.decoderResult().isSuccess()) {
            context.write(HttpResponseStatus.BAD_REQUEST).addListener(ChannelFutureListener.CLOSE);
        } else {
            handshaker = new WebSocketServerHandshakerFactory(uri.toString(), null, true).newHandshaker(request);

            if (handshaker == null) {
                WebSocketServerHandshakerFactory.sendUnsupportedVersionResponse(context.channel());
            } else {
                handshaker.handshake(context.channel(), request);
            }
        }
    }
}
