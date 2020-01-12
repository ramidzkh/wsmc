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

import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;


public class LambdaChannelInitializer extends ChannelInitializer<SocketChannel> {

    private final Consumer consumer;

    public LambdaChannelInitializer(Consumer consumer) {
        this.consumer = consumer;
    }

    @Override
    protected void initChannel(SocketChannel channel) throws Exception {
        consumer.accept(channel);
    }

    public interface Consumer {
        void accept(SocketChannel channel) throws Exception;
    }
}
