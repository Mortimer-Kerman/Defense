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

    public static void RegisterPayloads()
    {
        PayloadTypeRegistry.playC2S().register(RecordPVPPayload.ID, RecordPVPPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(NotifyPVPPayload.ID, NotifyPVPPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(RecordIconPayload.ID, RecordIconPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(NotifyIconPayload.ID, NotifyIconPayload.CODEC);
    }

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
