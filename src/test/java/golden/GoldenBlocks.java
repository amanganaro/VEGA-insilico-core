package golden;

import insilico.core.descriptor.DescriptorBlock;
import insilico.core.descriptor.blocks.AtomCenteredFragments;
import insilico.core.descriptor.blocks.Constitutional;
import insilico.core.descriptor.blocks.FunctionalGroups;
import insilico.core.descriptor.blocks.Rings;

import java.util.List;
import java.util.function.Supplier;

/**
 * Descriptor blocks covered by the golden regression tests.
 * <p>
 * A new block instance is created for every molecule: {@code CreateClone()} is not used on purpose, because in
 * {@code Constitutional} and {@code Rings} it copies the block onto itself.
 */
public final class GoldenBlocks {

    /** A block under test: short stable name (used in file names) and factory. */
    public record Block(String name, Supplier<DescriptorBlock> factory) {
        @Override
        public String toString() {
            return name;
        }
    }

    public static final List<Block> ALL = List.of(
            new Block("AtomCenteredFragments", AtomCenteredFragments::new),
            new Block("Constitutional", Constitutional::new),
            new Block("FunctionalGroups", FunctionalGroups::new),
            new Block("Rings", Rings::new));

    private GoldenBlocks() { }
}
