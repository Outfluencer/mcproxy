package dev.outfluencer.mcproxy.networking.protocol.registry;

import dev.outfluencer.mcproxy.networking.protocol.packets.common.ClientboundCommonDisconnectPacket;
import dev.outfluencer.mcproxy.networking.protocol.packets.common.ClientboundUpdateTagsPacket;
import dev.outfluencer.mcproxy.networking.protocol.packets.config.ClientboundFinishConfigurationPacket;
import dev.outfluencer.mcproxy.networking.protocol.packets.config.ClientboundRegistryDataPacket;
import dev.outfluencer.mcproxy.networking.protocol.packets.config.ClientboundSelectKnownPacks;
import dev.outfluencer.mcproxy.networking.protocol.packets.config.ServerboundFinishConfigurationPacket;
import dev.outfluencer.mcproxy.networking.protocol.packets.config.ServerboundSelectKnownPacks;
import dev.outfluencer.mcproxy.networking.protocol.packets.game.ClientboundBundleDelimiterPacket;
import dev.outfluencer.mcproxy.networking.protocol.packets.game.ClientboundStartConfigurationPacket;
import dev.outfluencer.mcproxy.networking.protocol.packets.game.ServerboundConfigurationAcknowledgedPacket;
import dev.outfluencer.mcproxy.networking.protocol.packets.handshake.ServerboundHandshakePacket;
import java.util.Map;

import dev.outfluencer.mcproxy.networking.protocol.packets.login.*;
import dev.outfluencer.mcproxy.networking.protocol.packets.status.ClientboundPongResponsePacket;
import dev.outfluencer.mcproxy.networking.protocol.packets.status.ClientboundStatusResponsePacket;
import dev.outfluencer.mcproxy.networking.protocol.packets.status.ServerboundPingRequest;
import dev.outfluencer.mcproxy.networking.protocol.packets.status.ServerboundStatusRequestPacket;


public enum Protocol {

    HANDSHAKE {
        {
            serverbound.registerPacket(
                ServerboundHandshakePacket.class,
                ServerboundHandshakePacket::new,
                map(MinecraftVersion.V1_21_11, 0x00)
            );
        }
    },
    STATUS {
        {
            serverbound.registerPacket(
                ServerboundStatusRequestPacket.class,
                () -> ServerboundStatusRequestPacket.INSTANCE,
                map(MinecraftVersion.V1_21_11, 0x00)
            );
            serverbound.registerPacket(
                ServerboundPingRequest.class,
                ServerboundPingRequest::new,
                map(MinecraftVersion.V1_21_11, 0x01)
            );

            clientbound.registerPacket(
                ClientboundStatusResponsePacket.class,
                ClientboundStatusResponsePacket::new,
                map(MinecraftVersion.V1_21_11, 0x00)
            );
            clientbound.registerPacket(
                ClientboundPongResponsePacket.class,
                ClientboundPongResponsePacket::new,
                map(MinecraftVersion.V1_21_11, 0x01)
            );
        }
    },
    LOGIN {
        {
            serverbound.registerPacket(
                ServerboundHelloPacket.class,
                ServerboundHelloPacket::new,
                map(MinecraftVersion.V1_21_11, 0x00)
            );
            serverbound.registerPacket(
                ServerboundLoginEncryptionResponsePacket.class,
                ServerboundLoginEncryptionResponsePacket::new,
                map(MinecraftVersion.V1_21_11, 0x01)
            );
            serverbound.registerPacket(
                ServerboundLoginAcknowledgedPacket.class,
                ServerboundLoginAcknowledgedPacket::new,
                map(MinecraftVersion.V1_21_11, 0x03)
            );

            clientbound.registerPacket(
                ClientboundLoginDisconnectPacket.class,
                ClientboundLoginDisconnectPacket::new,
                map(MinecraftVersion.V1_21_11, 0x00)
            );
            clientbound.registerPacket(
                ClientboundLoginEncryptionRequestPacket.class,
                ClientboundLoginEncryptionRequestPacket::new,
                map(MinecraftVersion.V1_21_11, 0x01)
            );
            clientbound.registerPacket(
                ClientboundLoginFinishedPacket.class,
                ClientboundLoginFinishedPacket::new,
                map(MinecraftVersion.V1_21_11, 0x02)
            );
            clientbound.registerPacket(
                ClientboundLoginCompressionPacket.class,
                ClientboundLoginCompressionPacket::new,
                map(MinecraftVersion.V1_21_11, 0x03)
            );
        }
    },
    CONFIG {
        {
            clientbound.registerPacket(
                ClientboundFinishConfigurationPacket.class,
                ClientboundFinishConfigurationPacket::new,
                map(MinecraftVersion.V1_21_11, 0x03)
            );
            clientbound.registerPacket(
                ClientboundRegistryDataPacket.class,
                ClientboundRegistryDataPacket::new,
                map(MinecraftVersion.V1_21_11, 0x07)
            );

            clientbound.registerPacket(
                ClientboundSelectKnownPacks.class,
                ClientboundSelectKnownPacks::new,
                map(MinecraftVersion.V1_21_11, 0x0E)
            );
            clientbound.registerPacket(
                ClientboundUpdateTagsPacket.class,
                ClientboundUpdateTagsPacket::new,
                map(MinecraftVersion.V1_21_11, 0x0D)
            );

            serverbound.registerPacket(
                ServerboundFinishConfigurationPacket.class,
                ServerboundFinishConfigurationPacket::new,
                map(MinecraftVersion.V1_21_11, 0x03)
            );
            serverbound.registerPacket(
                ServerboundSelectKnownPacks.class,
                ServerboundSelectKnownPacks::new,
                map(MinecraftVersion.V1_21_11, 0x07)
            );
        }
    },
    GAME {
        {
            clientbound.registerPacket(
                ClientboundBundleDelimiterPacket.class,
                ClientboundBundleDelimiterPacket::new,
                map(MinecraftVersion.V1_21_11, 0x00)
            );
            clientbound.registerPacket(
                ClientboundCommonDisconnectPacket.class,
                ClientboundCommonDisconnectPacket::new,
                map(MinecraftVersion.V1_21_11, 0x20)
            );
            clientbound.registerPacket(
                ClientboundStartConfigurationPacket.class,
                ClientboundStartConfigurationPacket::new,
                map(MinecraftVersion.V1_21_11, 0x74),
                map(MinecraftVersion.V26_1, 0x76)
            );

            serverbound.registerPacket(
                ServerboundConfigurationAcknowledgedPacket.class,
                ServerboundConfigurationAcknowledgedPacket::new,
                map(MinecraftVersion.V1_21_11, 0x0F),
                map(MinecraftVersion.V26_1, 0x10)

            );
        }
    };

    public record Mapping(int protocolVersion, int packetId) {

    }

    public final PacketRegistry clientbound = new PacketRegistry(this);
    public final PacketRegistry serverbound = new PacketRegistry(this);

    /**
     * Builds a version-to-packetId map from interleaved (version, id) pairs.
     * Example: map(MINECRAFT_1_8, 0x00, MINECRAFT_1_9, 0x01)
     */
    public static Mapping map(int protocolVersion, int packetId) {
        return new Mapping(protocolVersion, packetId);
    }

    static void main() {
        for (Protocol protocol : Protocol.values()) {
            System.out.println("=== " + protocol.name() + " ===");
            printRegistry("  serverbound", protocol.serverbound);
            printRegistry("  clientbound", protocol.clientbound);
        }
    }

    private static void printRegistry(String label, PacketRegistry registry) {
        System.out.println(label + ":");
        registry.getClassToId().forEach((version, entries) -> {
            System.out.println("    version " + version + ":");
            entries.entrySet().stream()
                .sorted(Map.Entry.comparingByValue())
                .forEach(e -> System.out.printf("      0x%02X -> %s%n", e.getValue(), e.getKey().getSimpleName()));
        });
        if (registry.getClassToId().isEmpty()) {
            System.out.println("    (empty)");
        }
    }
}
