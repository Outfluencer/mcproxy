package dev.outfluencer.mcproxy.networking.protocol.registry;

import dev.outfluencer.mcproxy.networking.protocol.packets.Packet;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Supplier;

@Getter
@RequiredArgsConstructor
public class PacketRegistry {
    private final Protocol protocol;
    // sorted by version so floorKey() finds the closest older version
    private final TreeMap<Integer, Map<Integer, Supplier<? extends Packet>>> idToFactory = new TreeMap<>();
    private final TreeMap<Integer, Map<Class<? extends Packet>, Integer>> classToId = new TreeMap<>();

    /**
     * Registers a packet with a per-version packet ID map.
     * Use {@link Protocol#map(int, int)} to build the map.
     */
    public <P extends Packet> void registerPacket(Class<P> packetClass, Supplier<P> factory, Protocol.Mapping... mappings) {
        for (Protocol.Mapping mapping : mappings) {
            idToFactory.computeIfAbsent(mapping.protocolVersion(), _ -> new HashMap<>()).put(mapping.packetId(), factory);
            classToId.computeIfAbsent(mapping.protocolVersion(), _ -> new HashMap<>()).put(packetClass, mapping.packetId());
        }
    }

    /**
     * Creates a new packet instance for the given protocol version and packet ID.
     * Falls back to the mapping of the closest older registered version.
     * Returns null if not registered.
     */
    public Packet createPacket(int protocolVersion, int packetId) {
        for (Integer key = idToFactory.floorKey(protocolVersion); key != null; key = idToFactory.lowerKey(key)) {
            Supplier<? extends Packet> factory = idToFactory.get(key).get(packetId);
            if (factory != null) return factory.get();
        }
        return null;
    }

    /**
     * Returns the packet ID for the given class and protocol version, or -1 if not registered.
     * Falls back to the mapping of the closest older registered version.
     */
    public int getPacketId(int protocolVersion, Class<? extends Packet> packetClass) {
        for (Integer key = classToId.floorKey(protocolVersion); key != null; key = classToId.lowerKey(key)) {
            Integer id = classToId.get(key).get(packetClass);
            if (id != null) return id;
        }
        return -1;
    }

    public boolean hasPacket(int protocolVersion, int packetId) {
        for (Integer key = idToFactory.floorKey(protocolVersion); key != null; key = idToFactory.lowerKey(key)) {
            if (idToFactory.get(key).containsKey(packetId)) return true;
        }
        return false;
    }

    public TreeMap<Integer, Map<Class<? extends Packet>, Integer>> getClassToId() {
        return classToId;
    }
}
