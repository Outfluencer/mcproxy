package dev.outfluencer.mcproxy.networking.protocol.registry;

import dev.outfluencer.mcproxy.networking.protocol.packets.common.ClientboundCommonDisconnectPacket;
import dev.outfluencer.mcproxy.networking.protocol.packets.common.ClientboundUpdateTagsPacket;
import dev.outfluencer.mcproxy.networking.protocol.packets.common.ServerboundClientInformationPacket;
import dev.outfluencer.mcproxy.networking.protocol.packets.config.ClientboundFinishConfigurationPacket;
import dev.outfluencer.mcproxy.networking.protocol.packets.config.ClientboundRegistryDataPacket;
import dev.outfluencer.mcproxy.networking.protocol.packets.config.ClientboundSelectKnownPacks;
import dev.outfluencer.mcproxy.networking.protocol.packets.config.ServerboundFinishConfigurationPacket;
import dev.outfluencer.mcproxy.networking.protocol.packets.config.ServerboundSelectKnownPacks;
import dev.outfluencer.mcproxy.networking.protocol.packets.game.ClientboundBundleDelimiterPacket;
import dev.outfluencer.mcproxy.networking.protocol.packets.game.ClientboundCommandsPacket;
import dev.outfluencer.mcproxy.networking.protocol.packets.game.ClientboundCommandSuggestionsPacket;
import dev.outfluencer.mcproxy.networking.protocol.packets.game.ClientboundStartConfigurationPacket;
import dev.outfluencer.mcproxy.networking.protocol.packets.game.ClientboundSystemChatPacket;
import dev.outfluencer.mcproxy.networking.protocol.packets.game.ServerboundChatCommandPacket;
import dev.outfluencer.mcproxy.networking.protocol.packets.game.ServerboundCommandSuggestionPacket;
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
                map(0, 0x00)
            );
        }
    },
    STATUS {
        {
            serverbound.registerPacket(
                ServerboundStatusRequestPacket.class,
                () -> ServerboundStatusRequestPacket.INSTANCE,
                map(0, 0x00)
            );
            serverbound.registerPacket(
                ServerboundPingRequest.class,
                ServerboundPingRequest::new,
                map(0, 0x01)
            );

            clientbound.registerPacket(
                ClientboundStatusResponsePacket.class,
                ClientboundStatusResponsePacket::new,
                map(0, 0x00)
            );
            clientbound.registerPacket(
                ClientboundPongResponsePacket.class,
                ClientboundPongResponsePacket::new,
                map(0, 0x01)
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
                ClientboundCommonDisconnectPacket.class,
                ClientboundCommonDisconnectPacket::new,
                map(MinecraftVersion.V1_21_11, 0x02)
            );
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
                ServerboundClientInformationPacket.class,
                ServerboundClientInformationPacket::new,
                map(MinecraftVersion.V1_20_2, 0x00)
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
                ClientboundCommandsPacket.class,
                ClientboundCommandsPacket::new,
                map(MinecraftVersion.V1_20_2, 0x11),
                map(MinecraftVersion.V1_21_5, 0x10)
            );

            clientbound.registerPacket(
                ClientboundCommandSuggestionsPacket.class,
                ClientboundCommandSuggestionsPacket::new,
                map(MinecraftVersion.V1_20_2, 0x10),
                map(MinecraftVersion.V1_21_5, 0x0F)
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

            clientbound.registerPacket(
                ClientboundSystemChatPacket.class,
                ClientboundSystemChatPacket::new,
                map(MinecraftVersion.V1_21_9, 0x77),
                map(MinecraftVersion.V26_1, 0x79)
            );

            serverbound.registerPacket(
                ServerboundConfigurationAcknowledgedPacket.class,
                ServerboundConfigurationAcknowledgedPacket::new,
                map(MinecraftVersion.V1_21_11, 0x0F),
                map(MinecraftVersion.V26_1, 0x10)
            );
            serverbound.registerPacket(
                ServerboundChatCommandPacket.class,
                ServerboundChatCommandPacket::new,
                map(MinecraftVersion.V1_21_6, 0x06),
                map(MinecraftVersion.V26_1, 0x07 )
            );

            serverbound.registerPacket(
                ServerboundClientInformationPacket.class,
                ServerboundClientInformationPacket::new,
                map(MinecraftVersion.V1_20_5, 0x0A),
                map(MinecraftVersion.V1_21_2, 0x0C),
                map(MinecraftVersion.V1_21_6, 0x0D),
                map(MinecraftVersion.V26_1, 0x0E)
            );

            serverbound.registerPacket(
                ServerboundCommandSuggestionPacket.class,
                ServerboundCommandSuggestionPacket::new,
                map(MinecraftVersion.V1_20_5, 0x0B),
                map(MinecraftVersion.V1_21_2, 0x0D ),
                map(MinecraftVersion.V1_21_6, 0x0E ),
                map(MinecraftVersion.V26_1, 0x0F )
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
