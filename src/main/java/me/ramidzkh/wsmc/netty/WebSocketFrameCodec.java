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

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;

public class WebSocketFrameCodec extends ChannelDuplexHandler {

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        if (msg instanceof BinaryWebSocketFrame) {
            ctx.fireChannelRead(((BinaryWebSocketFrame) msg).content().retain());
        } else if (msg instanceof TextWebSocketFrame) {
            ctx.fireChannelRead(((TextWebSocketFrame) msg).text());
        } else {
            ctx.fireChannelRead(msg);
        }
    }

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) {
        if (msg instanceof CharSequence) {
            ctx.write(new TextWebSocketFrame(msg.toString()), promise);
        } else if (msg instanceof ByteBuf) {
            ctx.write(new BinaryWebSocketFrame((ByteBuf) msg), promise);
        } else {
            ctx.write(msg, promise);
        }
    }
}
