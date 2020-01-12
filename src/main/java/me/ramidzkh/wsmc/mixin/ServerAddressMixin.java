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

import me.ramidzkh.wsmc.client.ServerAddressProperties;
import net.minecraft.network.ServerAddress;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.net.URI;
import java.net.URISyntaxException;

@Mixin(ServerAddress.class)
public class ServerAddressMixin implements ServerAddressProperties {

    @Shadow
    @Final
    private int port;

    private URI websocket;
    private boolean ssl;
    private boolean validate;

    @Inject(method = "parse", at = @At("RETURN"))
    private static void parse(String address, CallbackInfoReturnable<ServerAddress> callbackInfoReturnable) {
        ServerAddress parsed = callbackInfoReturnable.getReturnValue();

        try {
            URI uri = new URI(address);
            ServerAddressProperties extra = (ServerAddressProperties) parsed;

            switch (uri.getScheme()) {
                case "ws":
                    extra.setWebsocketUri(uri);
                    break;
                case "wss":
                    extra.setWebsocketUri(uri);
                    extra.setUseSsl(true);
                    extra.setValidateSsl(true);
                    break;
                case "wss+insecure":
                    extra.setWebsocketUri(uri);
                    extra.setUseSsl(true);
                    extra.setValidateSsl(false);
                    break;
                default:
                    // TODO: Error
                    break;
            }
        } catch (URISyntaxException ignored) {
        }
    }

    /**
     * @author ramidzkh
     * @reason Also use websocket port
     */
    @Overwrite
    public int getPort() {
        return websocket != null && websocket.getPort() != -1 ? websocket.getPort() : port;
    }

    @Override
    public URI getWebsocketUri() {
        return websocket;
    }

    @Override
    public void setWebsocketUri(URI uri) {
        this.websocket = uri;
    }

    @Override
    public boolean useSsl() {
        return ssl;
    }

    @Override
    public void setUseSsl(boolean ssl) {
        this.ssl = ssl;
    }

    @Override
    public boolean validateSsl() {
        return validate;
    }

    @Override
    public void setValidateSsl(boolean validateSsl) {
        this.validate = validateSsl;
    }
}
