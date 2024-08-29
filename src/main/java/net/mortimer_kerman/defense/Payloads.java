package net.mortimer_kerman.defense;

import io.netty.buffer.ByteBuf;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.encoding.StringEncoding;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

import java.util.UUID;

public class Payloads
{
    public record RecordPVPPayload(boolean pvpOff) implements CustomPayload
    {
        public static final CustomPayload.Id<RecordPVPPayload> ID = new CustomPayload.Id<>(Identifier.of(Defense.MOD_ID, "record_pvp_payload"));
        public static final PacketCodec<RegistryByteBuf, RecordPVPPayload> CODEC = PacketCodec.tuple(PacketCodecs.BOOL, RecordPVPPayload::pvpOff, RecordPVPPayload::new);
        @Override
        public CustomPayload.Id<? extends CustomPayload> getId() { return ID; }
    }

    public record NotifyPVPPayload(UUID playerUUID, boolean pvpOff) implements CustomPayload
    {
        public static final CustomPayload.Id<NotifyPVPPayload> ID = new CustomPayload.Id<>(Identifier.of(Defense.MOD_ID, "notify_pvp_payload"));
        public static final PacketCodec<RegistryByteBuf, NotifyPVPPayload> CODEC = PacketCodec.tuple(CODEC_UUID, NotifyPVPPayload::playerUUID, PacketCodecs.BOOL, NotifyPVPPayload::pvpOff, NotifyPVPPayload::new);
        @Override
        public CustomPayload.Id<? extends CustomPayload> getId() { return ID; }
    }

    public record RecordIconPayload(int iconID) implements CustomPayload
    {
        public static final CustomPayload.Id<RecordIconPayload> ID = new CustomPayload.Id<>(Identifier.of(Defense.MOD_ID, "record_icon_payload"));
        public static final PacketCodec<RegistryByteBuf, RecordIconPayload> CODEC = PacketCodec.tuple(PacketCodecs.INTEGER, RecordIconPayload::iconID, RecordIconPayload::new);
        @Override
        public CustomPayload.Id<? extends CustomPayload> getId() { return ID; }
    }

    public record NotifyIconPayload(UUID playerUUID, int iconID) implements CustomPayload
    {
        public static final CustomPayload.Id<NotifyIconPayload> ID = new CustomPayload.Id<>(Identifier.of(Defense.MOD_ID, "notify_icon_payload"));
        public static final PacketCodec<RegistryByteBuf, NotifyIconPayload> CODEC = PacketCodec.tuple(CODEC_UUID, NotifyIconPayload::playerUUID, PacketCodecs.INTEGER, NotifyIconPayload::iconID, NotifyIconPayload::new);
        @Override
        public CustomPayload.Id<? extends CustomPayload> getId() { return ID; }
    }

    public record EnableAfkPayload() implements CustomPayload
    {
        public static final CustomPayload.Id<EnableAfkPayload> ID = new CustomPayload.Id<>(Identifier.of(Defense.MOD_ID, "enable_afk_payload"));
        public static final PacketCodec<RegistryByteBuf, EnableAfkPayload> CODEC = PacketCodec.unit(new EnableAfkPayload());
        @Override
        public CustomPayload.Id<? extends CustomPayload> getId() { return ID; }
    }

    public record RequestAfkUpdatePayload() implements CustomPayload
    {
        public static final CustomPayload.Id<RequestAfkUpdatePayload> ID = new CustomPayload.Id<>(Identifier.of(Defense.MOD_ID, "request_afk_update_payload"));
        public static final PacketCodec<RegistryByteBuf, RequestAfkUpdatePayload> CODEC = PacketCodec.unit(new RequestAfkUpdatePayload());
        @Override
        public CustomPayload.Id<? extends CustomPayload> getId() { return ID; }
    }

    public record NotifyAfkPayload(UUID playerUUID, boolean afk) implements CustomPayload
    {
        public static final CustomPayload.Id<NotifyAfkPayload> ID = new CustomPayload.Id<>(Identifier.of(Defense.MOD_ID, "notify_afk_payload"));
        public static final PacketCodec<RegistryByteBuf, NotifyAfkPayload> CODEC = PacketCodec.tuple(CODEC_UUID, NotifyAfkPayload::playerUUID, PacketCodecs.BOOL, NotifyAfkPayload::afk, NotifyAfkPayload::new);
        @Override
        public CustomPayload.Id<? extends CustomPayload> getId() { return ID; }
    }

    public record ForceDefensePayload(int action) implements CustomPayload
    {
        public static final CustomPayload.Id<ForceDefensePayload> ID = new CustomPayload.Id<>(Identifier.of(Defense.MOD_ID, "force_defense_payload"));
        public static final PacketCodec<RegistryByteBuf, ForceDefensePayload> CODEC = PacketCodec.tuple(PacketCodecs.INTEGER, ForceDefensePayload::action, ForceDefensePayload::new);
        @Override
        public CustomPayload.Id<? extends CustomPayload> getId() { return ID; }
    }

    public static class GamerulePayloads
    {
        public record Boolean(String gameruleName, boolean value) implements CustomPayload
        {
            public static final CustomPayload.Id<Boolean> ID = new CustomPayload.Id<>(Identifier.of(Defense.MOD_ID, "gamerule_boolean_payload"));
            public static final PacketCodec<RegistryByteBuf, Boolean> CODEC = PacketCodec.tuple(PacketCodecs.STRING, Boolean::gameruleName, PacketCodecs.BOOL, Boolean::value, Boolean::new);
            @Override
            public CustomPayload.Id<? extends CustomPayload> getId() { return ID; }
        }

        public record Double(String gameruleName, double value) implements CustomPayload
        {
            public static final CustomPayload.Id<Double> ID = new CustomPayload.Id<>(Identifier.of(Defense.MOD_ID, "gamerule_double_payload"));
            public static final PacketCodec<RegistryByteBuf, Double> CODEC = PacketCodec.tuple(PacketCodecs.STRING, Double::gameruleName, PacketCodecs.DOUBLE, Double::value, Double::new);
            @Override
            public CustomPayload.Id<? extends CustomPayload> getId() { return ID; }
        }

        public record Integer(String gameruleName, int value) implements CustomPayload
        {
            public static final CustomPayload.Id<Integer> ID = new CustomPayload.Id<>(Identifier.of(Defense.MOD_ID, "gamerule_integer_payload"));
            public static final PacketCodec<RegistryByteBuf, Integer> CODEC = PacketCodec.tuple(PacketCodecs.STRING, Integer::gameruleName, PacketCodecs.INTEGER, Integer::value, Integer::new);
            @Override
            public CustomPayload.Id<? extends CustomPayload> getId() { return ID; }
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

    public static final Identifier handshakeID = Identifier.of(Defense.MOD_ID, "handshake_payload");

    public static PacketCodec<io.netty.buffer.ByteBuf, UUID> CODEC_UUID = new PacketCodec<>()
    {
        public UUID decode(ByteBuf byteBuf)
        {
            return UUID.fromString(StringEncoding.decode(byteBuf, 32767));
        }
        public void encode(ByteBuf byteBuf, UUID uuid)
        {
            StringEncoding.encode(byteBuf, uuid.toString(), 32767);
        }
    };
}
