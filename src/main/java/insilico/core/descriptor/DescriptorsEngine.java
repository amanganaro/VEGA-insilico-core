package insilico.core.descriptor;

import insilico.core.exception.DescriptorNotFoundException;
import insilico.core.molecule.InsilicoMolecule;
import lombok.Data;

import java.util.ArrayList;

@Data
public class DescriptorsEngine {

    private ArrayList<DescriptorBlock> Descriptors;

    public DescriptorsEngine(){
        this.Descriptors = new ArrayList<>();
    }

    /**
     * Returns descriptor by name
     * @param name name of the descriptor to retrieve
     * @return descriptor by name
     * @throws DescriptorNotFoundException
     */
    public Descriptor GetDescriptor(String name) throws DescriptorNotFoundException {
        for(DescriptorBlock descriptorsBlock : Descriptors){
            Descriptor currentDescriptor = descriptorsBlock.GetByName(name);
            if (currentDescriptor != null)
                return currentDescriptor;
        }
        throw new DescriptorNotFoundException("Descriptor " + name + " not found");
    }

    public boolean hasDescriptorBlock(Class DescriptorClass) {
        for (DescriptorBlock d : Descriptors)
            if (d.getClass() == DescriptorClass)
                return true;
        return false;
    }

    /**
     * Gets a stored descriptor block, retrieved by its class. Returns
     * null if no matches are found with the given class.
     *
     * @param descriptorClass the class of the descriptor block
     * @return the retrieved iDescriptorBlock object
     */
    public DescriptorBlock GetDescriptorBlock(Class descriptorClass){
        for (DescriptorBlock descriptor : Descriptors)
            if (descriptor.getClass() == descriptorClass)
                return descriptor;
        return null;
    }

    /**
     * Add descriptor to Descriptor Block, if already in the list merge properties
     * @param descriptorBlock Descriptor Block to be added
     */
    public void AddDescriptorBlock(DescriptorBlock descriptorBlock) throws CloneNotSupportedException {

        if (!this.hasDescriptorBlock(descriptorBlock.getClass()))
            Descriptors.add(descriptorBlock.CreateClone());

        //Merge properties if the block is already in the list
        DescriptorBlock foundBlock = this.GetDescriptorBlock(descriptorBlock.getClass());
        foundBlock.MergeProperties(descriptorBlock);
    }

    public void AddDescriptorBlock(ArrayList<DescriptorBlock> descBlocks)
            throws CloneNotSupportedException {
        for (DescriptorBlock block: descBlocks) {
            AddDescriptorBlock(block);
        }
    }

    public boolean CalculateDescriptors(InsilicoMolecule molecule) {

        if (molecule == null)
            return false;
        if (!molecule.IsValid())
            return false;

        for (DescriptorBlock d : Descriptors) {
            d.SetAllValues(Descriptor.MISSING_VALUE); // Reset all values to MV
            d.Calculate(molecule);
        }
        return true;

    }



}
