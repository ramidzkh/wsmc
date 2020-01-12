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

import me.ramidzkh.wsmc.server.ExtraServerProperties;
import net.minecraft.server.dedicated.AbstractPropertiesHandler;
import net.minecraft.server.dedicated.ServerPropertiesHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.net.URI;
import java.util.Properties;

@Mixin(ServerPropertiesHandler.class)
public abstract class ServerPropertiesHandlerMixin extends AbstractPropertiesHandler<ServerPropertiesHandler> implements ExtraServerProperties {

    private URI uri;

    public ServerPropertiesHandlerMixin(Properties properties) {
        super(properties);
    }

    @Override
    public URI getWebsocketUri() {
        return uri;
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    private void initUri(CallbackInfo callbackInfo) {
        String uri = getString("websocket-uri", "");

        if (uri != null && !uri.isEmpty()) {
            this.uri = URI.create(uri);
        }
    }
}
