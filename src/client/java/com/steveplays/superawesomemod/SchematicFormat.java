package com.steveplays.superawesomemod;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.Property;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Parses {@code .litematic} files (gzip-compressed NBT) into {@link Schematic} objects.
 */
public final class SchematicFormat {

    private SchematicFormat() {}

    /**
     * Loads a {@code .litematic} file from disk.
     */
    public static Schematic load(Path file) throws IOException {
        CompoundTag root = NbtIo.readCompressed(file, net.minecraft.nbt.NbtAccounter.unlimitedHeap());

        // Metadata
        CompoundTag metadata = root.getCompoundOrEmpty("Metadata");
        String name = metadata.getStringOr("Name", "");
        String author = metadata.getStringOr("Author", "");
        int totalBlocks = metadata.getIntOr("TotalBlocks", 0);

        CompoundTag sizeTag = metadata.getCompoundOrEmpty("EnclosingSize");
        Vec3i enclosingSize = new Vec3i(
            sizeTag.getIntOr("x", 0),
            sizeTag.getIntOr("y", 0),
            sizeTag.getIntOr("z", 0)
        );

        // Regions
        CompoundTag regionsTag = root.getCompoundOrEmpty("Regions");
        List<SchematicRegion> regions = new ArrayList<>();

        for (String regionName : regionsTag.keySet()) {
            CompoundTag regionTag = regionsTag.getCompoundOrEmpty(regionName);
            SchematicRegion region = parseRegion(regionName, regionTag);
            if (region != null) {
                regions.add(region);
            }
        }

        return new Schematic(name, author, enclosingSize, regions, totalBlocks);
    }

    private static SchematicRegion parseRegion(String name, CompoundTag tag) {
        // Position
        CompoundTag posTag = tag.getCompoundOrEmpty("Position");
        BlockPos position = new BlockPos(
            posTag.getIntOr("x", 0),
            posTag.getIntOr("y", 0),
            posTag.getIntOr("z", 0)
        );

        // Size (may have negative components)
        CompoundTag sizeTag = tag.getCompoundOrEmpty("Size");
        Vec3i size = new Vec3i(
            sizeTag.getIntOr("x", 0),
            sizeTag.getIntOr("y", 0),
            sizeTag.getIntOr("z", 0)
        );

        if (size.getX() == 0 || size.getY() == 0 || size.getZ() == 0) {
            return null; // empty region
        }

        // Block state palette
        ListTag paletteTag = tag.getListOrEmpty("BlockStatePalette");
        BlockState[] palette = new BlockState[paletteTag.size()];
        for (int i = 0; i < paletteTag.size(); i++) {
            palette[i] = paletteTag.getCompound(i)
                .map(SchematicFormat::parseBlockState)
                .orElse(Blocks.AIR.defaultBlockState());
        }

        // Block states (packed long array)
        long[] blockData = tag.getLongArray("BlockStates").orElse(new long[0]);
        int volume = Math.abs(size.getX()) * Math.abs(size.getY()) * Math.abs(size.getZ());
        int bitsPerEntry = Math.max(2, Integer.SIZE - Integer.numberOfLeadingZeros(Math.max(1, palette.length - 1)));
        LitematicaBitArray blockStates = new LitematicaBitArray(bitsPerEntry, volume, blockData);

        return new SchematicRegion(name, position, size, palette, blockStates);
    }

    /**
     * Parses a single block state from its NBT palette entry.
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    private static BlockState parseBlockState(CompoundTag tag) {
        String blockName = tag.getStringOr("Name", "minecraft:air");
        Identifier id = Identifier.tryParse(blockName);
        if (id == null) {
            return Blocks.AIR.defaultBlockState();
        }

        Optional<Block> blockOpt = BuiltInRegistries.BLOCK.getOptional(id);
        if (blockOpt.isEmpty()) {
            SuperAwesomeMod.LOGGER.warn("[Schematic] Unknown block: {}", blockName);
            return Blocks.AIR.defaultBlockState();
        }

        Block block = blockOpt.get();
        BlockState state = block.defaultBlockState();

        if (tag.contains("Properties")) {
            CompoundTag props = tag.getCompoundOrEmpty("Properties");
            StateDefinition<Block, BlockState> stateDefinition = block.getStateDefinition();

            for (String key : props.keySet()) {
                Property<?> property = stateDefinition.getProperty(key);
                if (property != null) {
                    String value = props.getStringOr(key, "");
                    Optional<?> parsed = property.getValue(value);
                    if (parsed.isPresent()) {
                        state = state.setValue((Property) property, (Comparable) parsed.get());
                    }
                }
            }
        }

        return state;
    }
}
