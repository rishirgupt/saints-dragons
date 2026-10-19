package com.leon.saintsdragons.fabric.platform;

import com.leon.saintsdragons.platform.NetworkHelper;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Since 1.20.5 custom packets are typed payloads with stream codecs. The mod's messages keep their
 * encode/decode/handle functions; each registered message class is carried inside a {@link Envelope}
 * payload whose codec delegates to those functions.
 */
public final class FabricNetworkHelper implements NetworkHelper {
    private enum Direction {
        SERVERBOUND,
        CLIENTBOUND
    }

    /** The payload actually sent on the wire for a message of type T. */
    private record Envelope<T>(CustomPacketPayload.Type<Envelope<T>> payloadType, T message)
            implements CustomPacketPayload {
        @Override
        public Type<? extends CustomPacketPayload> type() {
            return payloadType;
        }
    }

    private record Binding<T>(CustomPacketPayload.Type<Envelope<T>> payloadType, Direction direction) {
    }

    private final Map<Class<?>, Binding<?>> bindings = new ConcurrentHashMap<>();

    private static <T> StreamCodec<RegistryFriendlyByteBuf, Envelope<T>> codec(
            CustomPacketPayload.Type<Envelope<T>> payloadType, PacketEncoder<T> encoder, PacketDecoder<T> decoder) {
        return StreamCodec.of(
                (buffer, envelope) -> encoder.encode(envelope.message(), buffer),
                buffer -> new Envelope<>(payloadType, decoder.decode(buffer)));
    }

    @Override
    public <T> void registerServerbound(Class<T> type,
                                        Identifier id,
                                        PacketEncoder<T> encoder,
                                        PacketDecoder<T> decoder,
                                        ServerboundHandler<T> handler) {
        CustomPacketPayload.Type<Envelope<T>> payloadType = new CustomPacketPayload.Type<>(id);
        bindings.put(type, new Binding<>(payloadType, Direction.SERVERBOUND));
        PayloadTypeRegistry.serverboundPlay().register(payloadType, codec(payloadType, encoder, decoder));
        ServerPlayNetworking.registerGlobalReceiver(payloadType, (envelope, context) -> {
            ServerPlayer player = context.player();
            context.server().execute(() -> handler.handle(envelope.message(), player));
        });
    }

    @Override
    public <T> void registerClientbound(Class<T> type,
                                        Identifier id,
                                        PacketEncoder<T> encoder,
                                        PacketDecoder<T> decoder,
                                        ClientboundHandler<T> handler) {
        CustomPacketPayload.Type<Envelope<T>> payloadType = new CustomPacketPayload.Type<>(id);
        bindings.put(type, new Binding<>(payloadType, Direction.CLIENTBOUND));
        PayloadTypeRegistry.clientboundPlay().register(payloadType, codec(payloadType, encoder, decoder));
        if (FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT) {
            ClientAccess.register(payloadType, handler);
        }
    }

    @Override
    public void sendToServer(Object message) {
        if (FabricLoader.getInstance().getEnvironmentType() != EnvType.CLIENT) {
            throw new IllegalStateException("Client-only networking method invoked on a non-client environment");
        }
        ClientAccess.send(envelope(message, Direction.SERVERBOUND, "send clientbound packet to server"));
    }

    @Override
    public void sendToPlayer(ServerPlayer player, Object message) {
        ServerPlayNetworking.send(player, envelope(message, Direction.CLIENTBOUND, "send serverbound packet to player"));
    }

    @Override
    public void sendToTracking(Entity entity, Object message) {
        CustomPacketPayload payload = envelope(message, Direction.CLIENTBOUND,
                "send serverbound packet to tracking players");
        for (ServerPlayer tracking : PlayerLookup.tracking(entity)) {
            ServerPlayNetworking.send(tracking, payload);
        }
    }

    @Override
    public void sendToDimension(Level level, Object message) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }
        CustomPacketPayload payload = envelope(message, Direction.CLIENTBOUND, "send serverbound packet to dimension");
        for (ServerPlayer player : PlayerLookup.level(serverLevel)) {
            ServerPlayNetworking.send(player, payload);
        }
    }

    private <T> Envelope<T> envelope(T message, Direction expected, String misuse) {
        Class<?> messageClass = message.getClass();
        @SuppressWarnings("unchecked")
        Binding<T> binding = (Binding<T>) bindings.get(messageClass);
        if (binding == null) {
            throw new IllegalStateException("No network binding registered for " + messageClass.getName());
        }
        if (binding.direction() != expected) {
            throw new IllegalStateException("Attempted to " + misuse + ": " + messageClass);
        }
        return new Envelope<>(binding.payloadType(), message);
    }

    @Environment(EnvType.CLIENT)
    private static final class ClientAccess {
        private ClientAccess() {}

        private static <T> void register(CustomPacketPayload.Type<Envelope<T>> payloadType,
                                         ClientboundHandler<T> handler) {
            ClientPlayNetworking.registerGlobalReceiver(payloadType, (envelope, context) ->
                    context.client().execute(() -> handler.handle(envelope.message())));
        }

        private static void send(CustomPacketPayload payload) {
            ClientPlayNetworking.send(payload);
        }
    }
}
