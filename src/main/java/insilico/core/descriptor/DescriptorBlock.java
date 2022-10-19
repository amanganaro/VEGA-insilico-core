package insilico.core.descriptor;

import insilico.core.exception.DescriptorNotFoundException;
import insilico.core.molecule.InsilicoMolecule;
import insilico.core.propertycontainer.PropertyContainer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

/**
 * Parent abstract class for descriptors block.<p>
 * It provides the functionality for accessing the DescList of the
 * descriptors. It does not implement the Calculate() and GenerateDescriptors()
 * methods which must be defined in its descendants.
 *
 * @author Alberto Manganaro (a.manganaro@kode-solutions.net)
 */

public abstract class DescriptorBlock extends PropertyContainer implements Cloneable {

    protected String Name;
    protected ArrayList<Descriptor> DescList;


    /**
     * Constructor.
     */
    public DescriptorBlock() {
        super();
        DescList = new ArrayList<>();
        this.GenerateDescriptors();
        Name = "";
    }

    /**
     * @return the Name of the descriptors block
     */
    public String GetName() {
        return this.Name;
    }


    /**
     * @param Name the Name to be set for the descriptors block
     */
    public void SetName(String Name) {
        this.Name = Name;
    }

    /**
     * Create and adds a new descriptor to the current DescList.
     *
     * @param Name descriptor name
     * @param Description descriptor description
     */
    protected void Add(String Name, String Description) {
        Descriptor d = new Descriptor(Name, Description);
        DescList.add(d);
    }


    /**
     * Create and adds a new descriptor to the current DescList.
     *
     * @param Name descriptor name
     * @param Description descriptor description
     * @param Precision descriptor precision
     */
    protected void Add(String Name, String Description, int Precision) {
        Descriptor d = new Descriptor(Name, Description, Precision);
        DescList.add(d);
    }

    /**
     * Returns the number of descriptors in the DescList.
     *
     * @return number of descriptors
     */
    public int GetSize(){
        return DescList.size();
    }

    /**
     * Returns a descriptor from the current DescList by its name.
     *
     * @param name name to be retrieved
     * @return the Descriptor object
     * @throws DescriptorNotFoundException
     */
    public Descriptor GetByName(String name) throws DescriptorNotFoundException {
        for(Descriptor descriptor : DescList){
            if(descriptor.getName().equalsIgnoreCase(name))
                return descriptor;

        }
        throw new DescriptorNotFoundException("Descriptor " + name + " not found.");
    }

    /**
     * Returns a descriptor from the current DescList by its index.
     *
     * @param index index to be retrieved
     * @return the Descriptor object
     */
    public Descriptor GetByIndex(int index){
        if((index < 0 || index >= DescList.size()))
            return null;
        return DescList.get(index);
    }

    /**
     * Sets the value of a descriptor in the DescList, retrieved by its name.
     *
     * @param name name of the descriptor
     * @param value value to be set
     * @return True if value has been correctly set
     */
    public boolean SetByName(String name, double value){
        Descriptor selectedDescriptor = null;
        int index;
        for(index = 0; index < DescList.size(); index++)
            if(DescList.get(index).getName().equalsIgnoreCase(name)){
                selectedDescriptor = DescList.get(index);
                break;
            }
        if (selectedDescriptor == null)
            return false;
        else {
            selectedDescriptor.setValue(value);
            DescList.set(index, selectedDescriptor);
            return true;
        }
    }

    /**
     * Sets the value of a descriptor in the DescList, retrieved by its index.
     *
     * @param index index of the descriptor
     * @param value value to be set
     * @return True if value has been correctly set
     * @return
     */
    public boolean SetByIndex(int index, double value) {
        if( index < 0 || index >= DescList.size())
            return false;

        Descriptor selectedDescriptor = DescList.get(index);
        if (selectedDescriptor == null)
            return false;
        else {
            selectedDescriptor.setValue(value);
            DescList.set(index, selectedDescriptor);
            return true;
        }
    }

    /**
     * Sets all descriptors in the DescList to a single value.
     *
     * @param value value to be set
     */
    public void SetAllValues(double value){
        int index = 0;
        for(Descriptor descriptor : DescList){
            descriptor.setValue(value);
            DescList.set(index, descriptor);
            index++;
        }
    }

    /**
     * Returns the values of all descriptors in the DescList as an array.
     *
     * @return array of all descriptors values
     */
    public double[] GetAllValues(){
        double[] valueArray = new double[DescList.size()];
        for(int i = 0; i < DescList.size(); i++)
            valueArray[i] = DescList.get(i).getValue();
        return valueArray;
    }

    /**
     * Returns the formatted values of all descriptors in the DescList as an array.
     *
     * @return array of all descriptors formatted values
     */
    public String[] GetAllFormattedValues(){
        String[] valueStringArray = new String[DescList.size()];
        int index = 0;
        for(Descriptor descriptor : DescList){
            valueStringArray[index] = descriptor.getFormattedValue();
            index++;
        }
        return valueStringArray;
    }

    /**
     * Returns the names of all descriptors in the DescList as an array.
     *
     * @return array of all descriptors names
     */
    public String[] GetAllNames(){
        String[] descriptorNames = new String[DescList.size()];
        int index = 0;
        for(Descriptor descriptor : DescList){
            descriptorNames[index] = descriptor.getName();
            index++;
        }
        return descriptorNames;
    }

    public ArrayList<Descriptor> GetAllDescriptors() {
        return this.DescList;
    }

    /**
     * Calculate the descriptors. Abstract method that must be implemented
     * in the descendants classes.
     *
     * @param molecule Molecule to be calculated
     */
    public abstract void Calculate(InsilicoMolecule molecule);

    /**
     * Generate the descriptors. Abstract method that must be implemented
     *      * in the descendants classes.
     */
    protected abstract void GenerateDescriptors();

    /**
     * Returns a clone of the actual object. Abstract method that must be
     * implemented in the descendants classess.
     *
     * @return the cloned iDescriptorBlock object
     * @throws CloneNotSupportedException
     */
    public abstract DescriptorBlock CreateClone()
            throws CloneNotSupportedException;

    /**
     * Protected method to be used in CreateClone() to have a exact
     * copy of all parameters (Name, Parameters, DescList) from a source
     * DescriptorBlock object.
     *
     * @param source object to be used as source
     * @throws CloneNotSupportedException
     */
    @SuppressWarnings("unchecked")
    protected final void CloneDetailsFrom(DescriptorBlock source)
            throws CloneNotSupportedException{
        this.Name = source.Name;
        this.BoolProperties = (HashMap<String, Boolean>) source.BoolProperties.clone();
        this.DescList = new ArrayList<Descriptor>();
        for (Descriptor d : source.DescList)
            this.DescList.add((Descriptor)d.Clone());
    }

    public void MergeProperties(DescriptorBlock SourceBlock) {
        Set<String> AllKeys = SourceBlock.getAllBoolPropertyNames();
        for (String K : AllKeys) {
            if (!getBoolProperty(K))
                setBoolProperty(K, SourceBlock.getBoolProperty(K));
        }
    }


    @Override
    public Object clone() throws CloneNotSupportedException {
        DescriptorBlock newDesc = (DescriptorBlock)super.clone();
        newDesc.DescList = new ArrayList<>();
        for (Descriptor d : this.DescList)
            newDesc.DescList.add((Descriptor)d.Clone());
        return newDesc;
    }
}
