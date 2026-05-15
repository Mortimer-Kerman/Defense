package net.mortimer_kerman.defense;

import org.jetbrains.annotations.NotNull;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;

import net.minecraft.core.UUIDUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

import java.util.UUID;

public class Payloads
{
    public record RecordPVPPayload(boolean pvpOff) implements CustomPacketPayload
    {
        public static final CustomPacketPayload.Type<@NotNull RecordPVPPayload> ID = new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath(Defense.MOD_ID, "record_pvp_payload"));
        public static final StreamCodec<RegistryFriendlyByteBuf, RecordPVPPayload> CODEC = StreamCodec.composite(ByteBufCodecs.BOOL, RecordPVPPayload::pvpOff, RecordPVPPayload::new);
        @Override
        public @NotNull CustomPacketPayload.Type<? extends @NotNull CustomPacketPayload> type() { return ID; }
    }

    public record NotifyPVPPayload(UUID playerUUID, boolean pvpOff) implements CustomPacketPayload
    {
        public static final CustomPacketPayload.Type<@NotNull NotifyPVPPayload> ID = new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath(Defense.MOD_ID, "notify_pvp_payload"));
        public static final StreamCodec<RegistryFriendlyByteBuf, NotifyPVPPayload> CODEC = StreamCodec.composite(UUIDUtil.STREAM_CODEC, NotifyPVPPayload::playerUUID, ByteBufCodecs.BOOL, NotifyPVPPayload::pvpOff, NotifyPVPPayload::new);
        @Override
        public @NotNull CustomPacketPayload.Type<? extends @NotNull CustomPacketPayload> type() { return ID; }
    }

    public record RecordIconPayload(int iconID) implements CustomPacketPayload
    {
        public static final CustomPacketPayload.Type<@NotNull RecordIconPayload> ID = new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath(Defense.MOD_ID, "record_icon_payload"));
        public static final StreamCodec<RegistryFriendlyByteBuf, RecordIconPayload> CODEC = StreamCodec.composite(ByteBufCodecs.INT, RecordIconPayload::iconID, RecordIconPayload::new);
        @Override
        public @NotNull CustomPacketPayload.Type<? extends @NotNull CustomPacketPayload> type() { return ID; }
    }

    public record NotifyIconPayload(UUID playerUUID, int iconID) implements CustomPacketPayload
    {
        public static final CustomPacketPayload.Type<@NotNull NotifyIconPayload> ID = new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath(Defense.MOD_ID, "notify_icon_payload"));
        public static final StreamCodec<RegistryFriendlyByteBuf, NotifyIconPayload> CODEC = StreamCodec.composite(UUIDUtil.STREAM_CODEC, NotifyIconPayload::playerUUID, ByteBufCodecs.INT, NotifyIconPayload::iconID, NotifyIconPayload::new);
        @Override
        public @NotNull CustomPacketPayload.Type<? extends @NotNull CustomPacketPayload> type() { return ID; }
    }

    public record EnableAfkPayload() implements CustomPacketPayload
    {
        public static final CustomPacketPayload.Type<@NotNull EnableAfkPayload> ID = new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath(Defense.MOD_ID, "enable_afk_payload"));
        public static final StreamCodec<RegistryFriendlyByteBuf, EnableAfkPayload> CODEC = StreamCodec.unit(new EnableAfkPayload());
        @Override
        public @NotNull CustomPacketPayload.Type<? extends @NotNull CustomPacketPayload> type() { return ID; }
    }

    public record RequestAfkUpdatePayload() implements CustomPacketPayload
    {
        public static final CustomPacketPayload.Type<@NotNull RequestAfkUpdatePayload> ID = new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath(Defense.MOD_ID, "request_afk_update_payload"));
        public static final StreamCodec<RegistryFriendlyByteBuf, RequestAfkUpdatePayload> CODEC = StreamCodec.unit(new RequestAfkUpdatePayload());
        @Override
        public @NotNull CustomPacketPayload.Type<? extends @NotNull CustomPacketPayload> type() { return ID; }
    }

    public record NotifyAfkPayload(UUID playerUUID, boolean afk) implements CustomPacketPayload
    {
        public static final CustomPacketPayload.Type<@NotNull NotifyAfkPayload> ID = new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath(Defense.MOD_ID, "notify_afk_payload"));
        public static final StreamCodec<RegistryFriendlyByteBuf, NotifyAfkPayload> CODEC = StreamCodec.composite(UUIDUtil.STREAM_CODEC, NotifyAfkPayload::playerUUID, ByteBufCodecs.BOOL, NotifyAfkPayload::afk, NotifyAfkPayload::new);
        @Override
        public @NotNull CustomPacketPayload.Type<? extends @NotNull CustomPacketPayload> type() { return ID; }
    }

    public record ForceDefensePayload(int action) implements CustomPacketPayload
    {
        public static final CustomPacketPayload.Type<@NotNull ForceDefensePayload> ID = new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath(Defense.MOD_ID, "force_defense_payload"));
        public static final StreamCodec<RegistryFriendlyByteBuf, ForceDefensePayload> CODEC = StreamCodec.composite(ByteBufCodecs.INT, ForceDefensePayload::action, ForceDefensePayload::new);
        @Override
        public @NotNull CustomPacketPayload.Type<? extends @NotNull CustomPacketPayload> type() { return ID; }
    }

    public static class GamerulePayloads
    {
        public record Boolean(Identifier gameruleId, boolean value) implements CustomPacketPayload
        {
            public static final CustomPacketPayload.Type<@NotNull Boolean> ID = new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath(Defense.MOD_ID, "gamerule_boolean_payload"));
            public static final StreamCodec<RegistryFriendlyByteBuf, Boolean> CODEC = StreamCodec.composite(Identifier.STREAM_CODEC, Boolean::gameruleId, ByteBufCodecs.BOOL, Boolean::value, Boolean::new);
            @Override
            public @NotNull CustomPacketPayload.Type<? extends @NotNull CustomPacketPayload> type() { return ID; }
        }

        public record Double(Identifier gameruleId, double value) implements CustomPacketPayload
        {
            public static final CustomPacketPayload.Type<@NotNull Double> ID = new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath(Defense.MOD_ID, "gamerule_double_payload"));
            public static final StreamCodec<RegistryFriendlyByteBuf, Double> CODEC = StreamCodec.composite(Identifier.STREAM_CODEC, Double::gameruleId, ByteBufCodecs.DOUBLE, Double::value, Double::new);
            @Override
            public @NotNull Type<? extends @NotNull CustomPacketPayload> type() { return ID; }
        }

        public record Integer(Identifier gameruleId, int value) implements CustomPacketPayload
        {
            public static final CustomPacketPayload.Type<@NotNull Integer> ID = new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath(Defense.MOD_ID, "gamerule_integer_payload"));
            public static final StreamCodec<RegistryFriendlyByteBuf, Integer> CODEC = StreamCodec.composite(Identifier.STREAM_CODEC, Integer::gameruleId, ByteBufCodecs.INT, Integer::value, Integer::new);
            @Override
            public @NotNull Type<? extends @NotNull CustomPacketPayload> type() { return ID; }
        }
    }

    public static void RegisterPayloads()
    {
        PayloadTypeRegistry.playC2S().register(RecordPVPPayload.ID, RecordPVPPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(NotifyPVPPayload.ID, NotifyPVPPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(RecordIconPayload.ID, RecordIconPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(NotifyIconPayload.ID, NotifyIconPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(EnableAfkPayload.ID, EnableAfkPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(RequestAfkUpdatePayload.ID, RequestAfkUpdatePayload.CODEC);
        PayloadTypeRegistry.playS2C().register(NotifyAfkPayload.ID, NotifyAfkPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(ForceDefensePayload.ID, ForceDefensePayload.CODEC);
        PayloadTypeRegistry.playS2C().register(GamerulePayloads.Boolean.ID, GamerulePayloads.Boolean.CODEC);
        PayloadTypeRegistry.playS2C().register(GamerulePayloads.Double.ID, GamerulePayloads.Double.CODEC);
        PayloadTypeRegistry.playS2C().register(GamerulePayloads.Integer.ID, GamerulePayloads.Integer.CODEC);
    }

    public static final Identifier handshakeID = Identifier.fromNamespaceAndPath(Defense.MOD_ID, "handshake_payload");
}
